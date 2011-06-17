/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */


package com.pindroid.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.http.auth.AuthenticationException;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.AddBookmarkTask;
import com.pindroid.action.BookmarkTaskArgs;
import com.pindroid.action.GetWebpageTitleTask;
import com.pindroid.activity.BrowseBookmarks;
import com.pindroid.activity.FragmentBaseActivity;
import com.pindroid.activity.MainSearchResults;
import com.pindroid.activity.ViewBookmark;
import com.pindroid.client.PinboardApi;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.ui.TagSpan;
import com.pindroid.util.StringUtils;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AddBookmarkFragment extends Fragment {

	private FragmentBaseActivity base;
	
	private EditText mEditUrl;
	private EditText mEditDescription;
	private ProgressBar mDescriptionProgress;
	private EditText mEditNotes;
	private EditText mEditTags;
	private TextView mRecommendedTags;
	private ProgressBar mRecommendedProgress;
	private TextView mPopularTags;
	private ProgressBar mPopularProgress;
	private CheckBox mPrivate;
	private CheckBox mToRead;
	private Bookmark bookmark;
	private Boolean update = false;
	private Boolean error = false;
	
	private Bookmark oldBookmark;
	
	private long updateTime = 0;
	
	private AsyncTask<String, Integer, String> titleTask;
	private AsyncTask<String, Integer, ArrayList<Tag>> tagTask;
	

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		base = (FragmentBaseActivity)getActivity();
		
		setHasOptionsMenu(true);
		
		mEditUrl = (EditText) base.findViewById(R.id.add_edit_url);
		mEditDescription = (EditText) base.findViewById(R.id.add_edit_description);
		mDescriptionProgress = (ProgressBar) base.findViewById(R.id.add_description_progress);
		mEditNotes = (EditText) base.findViewById(R.id.add_edit_notes);
		mEditTags = (EditText) base.findViewById(R.id.add_edit_tags);
		mRecommendedTags = (TextView) base.findViewById(R.id.add_recommended_tags);
		mRecommendedProgress = (ProgressBar) base.findViewById(R.id.add_recommended_tags_progress);
		mPopularTags = (TextView) base.findViewById(R.id.add_popular_tags);
		mPopularProgress = (ProgressBar) base.findViewById(R.id.add_popular_tags_progress);
		mPrivate = (CheckBox) base.findViewById(R.id.add_edit_private);
		mToRead = (CheckBox) base.findViewById(R.id.add_edit_toread);
		
		mRecommendedTags.setMovementMethod(LinkMovementMethod.getInstance());
		mPopularTags.setMovementMethod(LinkMovementMethod.getInstance());

		if(savedInstanceState == null){
			Intent intent = base.getIntent();
			
			if(Intent.ACTION_SEARCH.equals(intent.getAction())){
				if(intent.hasExtra(SearchManager.QUERY)){
					Intent i = new Intent(base.mContext, MainSearchResults.class);
					i.putExtras(intent.getExtras());
					startActivity(i);
					base.finish();
				} else {
					base.onSearchRequested();
				}
			} else if(Intent.ACTION_VIEW.equals(intent.getAction()) || (!intent.hasExtra(Intent.EXTRA_TEXT) && !Intent.ACTION_EDIT.equals(intent.getAction()))) {
				
				Uri data = intent.getData();
				String path = null;
				String tagname = null;
				
				if(data != null) {
					path = data.getPath();
					tagname = data.getQueryParameter("tagname");
				}
				
				if(data.getScheme() == null || !data.getScheme().equals("content")){
					Intent i = new Intent(Intent.ACTION_VIEW, data);
					
					startActivity(i);
					base.finish();				
				} else if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
					Intent viewBookmark = new Intent(base, ViewBookmark.class);
					viewBookmark.setData(data);
					
					Log.d("View Bookmark Uri", data.toString());
					startActivity(viewBookmark);
					base.finish();
				} else if(tagname != null) {
					Intent viewTags = new Intent(base, BrowseBookmarks.class);
					viewTags.setData(data);
					
					Log.d("View Tags Uri", data.toString());
					startActivity(viewTags);
					base.finish();
				}
			} else if(Intent.ACTION_SEND.equals(intent.getAction())){
				Bookmark b = new Bookmark();
				
				String url = StringUtils.getUrl(intent.getStringExtra(Intent.EXTRA_TEXT));
				b.setUrl(url);
				
				if(url.equals("")) {
					Toast.makeText(base, R.string.add_bookmark_invalid_url, Toast.LENGTH_LONG).show();
				}
				
				b.setDescription(intent.getStringExtra(Constants.EXTRA_DESCRIPTION));
				b.setNotes(intent.getStringExtra(Constants.EXTRA_NOTES));
				b.setTagString(intent.getStringExtra(Constants.EXTRA_TAGS));
				b.setShared(!intent.getBooleanExtra(Constants.EXTRA_PRIVATE, base.privateDefault));
				b.setToRead(intent.getBooleanExtra(Constants.EXTRA_TOREAD, base.toreadDefault));
				error = intent.getBooleanExtra(Constants.EXTRA_ERROR, false);
	
				try{
					Bookmark old = BookmarkManager.GetByUrl(b.getUrl(), base);
					b = old.copy();
				} catch(Exception e) {
					
				}

				mEditUrl.setText(b.getUrl());
				
				if(b.getDescription() != null)
					mEditDescription.setText(b.getDescription());
				
				if(b.getNotes() != null)
					mEditNotes.setText(b.getNotes());
				
				if(b.getTagString() != null)
					mEditTags.setText(b.getTagString());
				
				mPrivate.setChecked(!b.getShared());
				mToRead.setChecked(b.getToRead());
				
				if(mEditDescription.getText().toString().equals(""))
					titleTask = new GetTitleTask().execute(b.getUrl());
				
				bookmark = b.copy();
				
				if(error){
					update = intent.getBooleanExtra(Constants.EXTRA_UPDATE, false);
					
					if(update) {
						oldBookmark = new Bookmark();
						oldBookmark.setAccount(base.mAccount.name);
						oldBookmark.setDescription(intent.getStringExtra(Constants.EXTRA_DESCRIPTION + ".old"));
						oldBookmark.setNotes(intent.getStringExtra(Constants.EXTRA_NOTES + ".old"));
						oldBookmark.setUrl(intent.getStringExtra(Intent.EXTRA_TEXT + ".old"));
						oldBookmark.setShared(!intent.getBooleanExtra(Constants.EXTRA_PRIVATE + ".old", false));
						oldBookmark.setTagString(intent.getStringExtra(Constants.EXTRA_TAGS + ".old"));
						oldBookmark.setTime(intent.getLongExtra(Constants.EXTRA_TIME + ".old", 0));
						oldBookmark.setToRead(intent.getBooleanExtra(Constants.EXTRA_TOREAD + ".old", false));
					}
				}
				
				tagTask = new GetTagSuggestionsTask().execute(b.getUrl());
				
			} else if(Intent.ACTION_EDIT.equals(intent.getAction())){
				int id = Integer.parseInt(intent.getData().getLastPathSegment());
				try {
					Bookmark b = BookmarkManager.GetById(id, base);
					oldBookmark = b.copy();
					
					mEditUrl.setText(b.getUrl());
					mEditDescription.setText(b.getDescription());
					mEditNotes.setText(b.getNotes());
					mEditTags.setText(b.getTagString());
					mPrivate.setChecked(!b.getShared());
					mToRead.setChecked(b.getToRead());
					updateTime = b.getTime();
					
					update = true;
				} catch (ContentNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				mEditUrl.requestFocus();
				setDefaultValues();
			}
		}
		
		if(update)
			base.setTitle(getString(R.string.add_bookmark_edit_title));
		else base.setTitle(getString(R.string.add_bookmark_add_title));
		
		mEditUrl.setOnFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					String url = mEditUrl.getText().toString();
					
					if(mEditDescription.getText().toString().equals("")) {
						titleTask = new GetTitleTask().execute(url);
					}
					tagTask = new GetTagSuggestionsTask().execute(url);
				}
			}
		});

	}
	
	public void saveHandler(View v) {
		save();
	}
	
	public void cancelHandler(View v) {
    	if(error) {
    		revertBookmark();
    	}
    	
    	base.finish();
	}
	
	private void setDefaultValues(){   	
    	mPrivate.setChecked(base.privateDefault);
    	mToRead.setChecked(base.toreadDefault);
	}
	
    private void save() {
    	
    	String url = mEditUrl.getText().toString();
    	String description = mEditDescription.getText().toString();
    	
    	if(url.equals("")) {
    		Toast.makeText(base, R.string.add_bookmark_blank_url, Toast.LENGTH_LONG).show();
    		return;
    	}	
    	
    	if(titleTask != null)
    		titleTask.cancel(true);
    	
    	if(tagTask != null)
    		tagTask.cancel(true);

		
		if(!url.startsWith("http")){
			url = "http://" + url;
		}

		if(!update) {
			Date d = new Date();
			updateTime = d.getTime();
		}
		
		String tagstring = "";
		String[] tags = mEditTags.getText().toString().trim().split(" ");

		for(String s : tags){
			if(!s.equals("") && !s.equals(" "))
				tagstring += (s + " ");
		}
		
		int oldid = 0;
		if(bookmark != null && bookmark.getId() != 0) {
			oldid = bookmark.getId();
			update = true;
			oldBookmark = bookmark.copy();
		}
			
		
		bookmark = new Bookmark(url, description, 
				mEditNotes.getText().toString(), tagstring.trim(),
				!mPrivate.isChecked(), mToRead.isChecked(), updateTime);
		
		bookmark.setId(oldid);
		
		BookmarkTaskArgs args = new BookmarkTaskArgs(bookmark, oldBookmark, base.mAccount, base, update);
		
		new AddBookmarkTask().execute(args);
		
		if(update){
			BookmarkManager.UpdateBookmark(bookmark, base.mAccount.name, base);
			
			for(Tag t : oldBookmark.getTags()){
				if(!bookmark.getTags().contains(t)) {
					TagManager.UpleteTag(t, base.mAccount.name, base);
				}
			}
		} else {
			BookmarkManager.AddBookmark(bookmark, base.mAccount.name, base);
		}
		
		for(Tag t : bookmark.getTags()){   				
			TagManager.UpsertTag(t, base.mAccount.name, base);
		}
		
		base.finish();
    }
    
    private void revertBookmark(){
    	
    	if(update) {
			BookmarkManager.UpdateBookmark(oldBookmark, base.mAccount.name, base);
			
			for(Tag t : bookmark.getTags()){
				if(!oldBookmark.getTags().contains(t)) {
					TagManager.UpleteTag(t, base.mAccount.name, base);
				}
			}
			
			for(Tag t : oldBookmark.getTags()){   				
				TagManager.UpsertTag(t, base.mAccount.name, base);
			}
    	} else {
    		BookmarkManager.DeleteBookmark(bookmark, base);
    		
			for(Tag t : bookmark.getTags()){
				TagManager.UpleteTag(t, base.mAccount.name, base);
			}
    	}
    }
    
    @Override
    public void onStop(){
    	if(error) {
    		revertBookmark();
    	}
    	
    	super.onStop();
    }
    
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(base.isMyself()) {
			inflater.inflate(R.menu.add_bookmark_menu, menu);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_addbookmark_save:
	    	save();
			return true;
	    case R.id.menu_addbookmark_cancel:
        	if(error) {
        		revertBookmark();
        	}
        	
        	base.finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
    TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
        	String currentTagString = mEditTags.getText().toString();
        	
        	ArrayList<String> currentTags = new ArrayList<String>();
        	Collections.addAll(currentTags, currentTagString.split(" "));
        	
        	if(tag != null && tag != "") {
        		if(!currentTags.contains(tag)) {
		        	currentTags.add(tag);
        		} else {
        			currentTags.remove(tag);
        		}
        		mEditTags.setText(TextUtils.join(" ", currentTags.toArray()).trim());
        	}
        }
    };
    
    public class GetTitleTask extends GetWebpageTitleTask{    	
    	protected void onPreExecute(){
    		mDescriptionProgress.setVisibility(View.VISIBLE);
    	}
    	
        protected void onPostExecute(String result) {
        	mEditDescription.setText(Html.fromHtml(result));
        	mDescriptionProgress.setVisibility(View.GONE);
        }
    }
    
    public class GetTagSuggestionsTask extends AsyncTask<String, Integer, ArrayList<Tag>>{
    	private String url;
    	
    	@Override
    	protected ArrayList<Tag> doInBackground(String... args) {
    		url = args[0];
	
    		try {
				return PinboardApi.getSuggestedTags(url, base.mAccount, base);
			} catch (AuthenticationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
    	}
    	
    	protected void onPreExecute() {
    		mRecommendedTags.setVisibility(View.GONE);
    		mPopularTags.setVisibility(View.GONE);
    		mRecommendedProgress.setVisibility(View.VISIBLE);
    		mPopularProgress.setVisibility(View.VISIBLE);
    	}
    	
        protected void onPostExecute(ArrayList<Tag> result) {
        	        	
        	if(result != null) {
        		SpannableStringBuilder recommendedBuilder = new SpannableStringBuilder();
        		SpannableStringBuilder popularBuilder = new SpannableStringBuilder();

        		for(Tag t : result) {
        			if(t.getType().equals("recommended")) {
        				addTag(recommendedBuilder, t);
        			} else if(t.getType().equals("popular")) {
        				addTag(popularBuilder, t);
        			}
        		}
        		
        		mRecommendedTags.setText(recommendedBuilder);
        		mPopularTags.setText(popularBuilder);
        		
        		mRecommendedTags.setVisibility(View.VISIBLE);
        		mPopularTags.setVisibility(View.VISIBLE);
        		mRecommendedProgress.setVisibility(View.GONE);
        		mPopularProgress.setVisibility(View.GONE);
        	} 	
        }

		private void addTag(SpannableStringBuilder builder, Tag t) {
			int flags = 0;
			
			if (builder.length() != 0) {
				builder.append("  ");
			}
			
			int start = builder.length();
			builder.append(t.getTagName());
			int end = builder.length();
			
			TagSpan span = new TagSpan(t.getTagName());
			span.setOnTagClickListener(tagOnClickListener);

			builder.setSpan(span, start, end, flags);
		}
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.add_bookmark_fragment, container, false);
    }
}
