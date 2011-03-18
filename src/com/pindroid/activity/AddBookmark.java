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

package com.pindroid.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.http.auth.AuthenticationException;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.BookmarkTaskArgs;
import com.pindroid.client.PinboardApi;
import com.pindroid.client.NetworkUtilities;
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
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pindroid.action.AddBookmarkTask;

public class AddBookmark extends AppBaseActivity {

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

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_bookmark);
		
		mEditUrl = (EditText) findViewById(R.id.add_edit_url);
		mEditDescription = (EditText) findViewById(R.id.add_edit_description);
		mDescriptionProgress = (ProgressBar) findViewById(R.id.add_description_progress);
		mEditNotes = (EditText) findViewById(R.id.add_edit_notes);
		mEditTags = (EditText) findViewById(R.id.add_edit_tags);
		mRecommendedTags = (TextView) findViewById(R.id.add_recommended_tags);
		mRecommendedProgress = (ProgressBar) findViewById(R.id.add_recommended_tags_progress);
		mPopularTags = (TextView) findViewById(R.id.add_popular_tags);
		mPopularProgress = (ProgressBar) findViewById(R.id.add_popular_tags_progress);
		mPrivate = (CheckBox) findViewById(R.id.add_edit_private);
		mToRead = (CheckBox) findViewById(R.id.add_edit_toread);
		
		mRecommendedTags.setMovementMethod(LinkMovementMethod.getInstance());
		mPopularTags.setMovementMethod(LinkMovementMethod.getInstance());

		if(savedInstanceState == null){
			Intent intent = getIntent();
			
			if(Intent.ACTION_SEARCH.equals(intent.getAction())){
				if(intent.hasExtra(SearchManager.QUERY)){
					Intent i = new Intent(mContext, MainSearchResults.class);
					i.putExtras(intent.getExtras());
					startActivity(i);
					finish();
				} else {
					onSearchRequested();
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
					finish();				
				} else if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
					Intent viewBookmark = new Intent(this, ViewBookmark.class);
					viewBookmark.setData(data);
					
					Log.d("View Bookmark Uri", data.toString());
					startActivity(viewBookmark);
					finish();
				} else if(tagname != null) {
					Intent viewTags = new Intent(this, BrowseBookmarks.class);
					viewTags.setData(data);
					
					Log.d("View Tags Uri", data.toString());
					startActivity(viewTags);
					finish();
				}
			} else if(Intent.ACTION_SEND.equals(intent.getAction())){
				String extraUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
				String extraDescription = intent.getStringExtra(Constants.EXTRA_DESCRIPTION);
				String extraNotes = intent.getStringExtra(Constants.EXTRA_NOTES);
				String extraTags = intent.getStringExtra(Constants.EXTRA_TAGS);
				Boolean extraPrivate = intent.getBooleanExtra(Constants.EXTRA_PRIVATE, privateDefault);
				Boolean extraToRead = intent.getBooleanExtra(Constants.EXTRA_TOREAD, toreadDefault);
				error = intent.getBooleanExtra(Constants.EXTRA_ERROR, false);
				
				String url = StringUtils.getUrl(extraUrl);
				mEditUrl.setText(url);
				
				if(extraDescription != null)
					mEditDescription.setText(extraDescription);
				
				if(extraNotes != null)
					mEditNotes.setText(extraNotes);
				
				if(extraTags != null)
					mEditTags.setText(extraTags);
				
				mPrivate.setChecked(extraPrivate);
				mToRead.setChecked(extraToRead);
				
				if(mEditDescription.getText().toString().equals(""))
					new GetWebpageTitleTask().execute(url);
				
				bookmark = new Bookmark();
				bookmark.setUrl(url);
				bookmark.setDescription(extraDescription);
				bookmark.setNotes(extraNotes);
				bookmark.setShared(!extraPrivate);
				bookmark.setTagString(extraTags);
				bookmark.setToRead(extraToRead);
				
				if(error){
					update = intent.getBooleanExtra(Constants.EXTRA_UPDATE, false);
					
					if(update) {
						oldBookmark = new Bookmark();
						oldBookmark.setAccount(mAccount.name);
						oldBookmark.setDescription(intent.getStringExtra(Constants.EXTRA_DESCRIPTION + ".old"));
						oldBookmark.setNotes(intent.getStringExtra(Constants.EXTRA_NOTES + ".old"));
						oldBookmark.setUrl(intent.getStringExtra(Intent.EXTRA_TEXT + ".old"));
						oldBookmark.setShared(!intent.getBooleanExtra(Constants.EXTRA_PRIVATE + ".old", false));
						oldBookmark.setTagString(intent.getStringExtra(Constants.EXTRA_TAGS + ".old"));
						oldBookmark.setTime(intent.getLongExtra(Constants.EXTRA_TIME + ".old", 0));
						oldBookmark.setToRead(intent.getBooleanExtra(Constants.EXTRA_TOREAD + ".old", false));
					}
				}
				
				new GetTagSuggestionsTask().execute(url);
				
			} else if(Intent.ACTION_EDIT.equals(intent.getAction())){
				int id = Integer.parseInt(intent.getData().getLastPathSegment());
				try {
					Bookmark b = BookmarkManager.GetById(id, mContext);
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
			setTitle(getString(R.string.add_bookmark_edit_title));
		else setTitle(getString(R.string.add_bookmark_add_title));
		
		mEditUrl.setOnFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					String url = mEditUrl.getText().toString();
					
					if(mEditDescription.getText().toString().equals("")) {
						new GetWebpageTitleTask().execute(url);
					}
					new GetTagSuggestionsTask().execute(url);
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
    	
    	finish();
	}
	
	private void setDefaultValues(){   	
    	mPrivate.setChecked(privateDefault);
    	mToRead.setChecked(toreadDefault);
	}
	
    private void save() {

		String url = mEditUrl.getText().toString();
		
		if(mEditDescription.getText().toString().equals("")) {
			mEditDescription.setText(url);
		}
		
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
		
		bookmark = new Bookmark(url, mEditDescription.getText().toString(), 
				mEditNotes.getText().toString(), tagstring.trim(),
				!mPrivate.isChecked(), mToRead.isChecked(), updateTime);
		
		BookmarkTaskArgs args = new BookmarkTaskArgs(bookmark, oldBookmark, mAccount, mContext, update);
		
		new AddBookmarkTask().execute(args);
		
		if(update){
			BookmarkManager.UpdateBookmark(bookmark, mAccount.name, this);
			
			for(Tag t : oldBookmark.getTags()){
				if(!bookmark.getTags().contains(t)) {
					TagManager.UpleteTag(t, mAccount.name, this);
				}
			}
		} else {
			BookmarkManager.AddBookmark(bookmark, mAccount.name, this);
		}
		
		for(Tag t : bookmark.getTags()){   				
			TagManager.UpsertTag(t, mAccount.name, this);
		}
		
		finish();
    }
    
    private void revertBookmark(){
    	
    	if(update) {
			BookmarkManager.UpdateBookmark(oldBookmark, mAccount.name, this);
			
			for(Tag t : bookmark.getTags()){
				if(!oldBookmark.getTags().contains(t)) {
					TagManager.UpleteTag(t, mAccount.name, this);
				}
			}
			
			for(Tag t : oldBookmark.getTags()){   				
				TagManager.UpsertTag(t, mAccount.name, this);
			}
    	} else {
    		BookmarkManager.DeleteBookmark(bookmark, this);
    		
			for(Tag t : bookmark.getTags()){
				TagManager.UpleteTag(t, mAccount.name, this);
			}
    	}
    }
    
    public void onBackPressed(){
    	if(error) {
    		revertBookmark();
    	}
    	
    	super.onBackPressed();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
		
		if(result && isMyself()) {
			inflater.inflate(R.menu.add_bookmark_menu, menu);
		}
		
	    return result;
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
        	
        	finish();
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
    
    public class GetWebpageTitleTask extends AsyncTask<String, Integer, String>{
    	private String url;
    	
    	@Override
    	protected String doInBackground(String... args) {
    		
    		if(args.length > 0 && args[0] != null && args[0] != "") {
	    		url = args[0];
		
	    		return NetworkUtilities.getWebpageTitle(url);
    		} else return "";
    		
    	}
    	
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
				return PinboardApi.getSuggestedTags(url, mAccount, mContext);
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
}