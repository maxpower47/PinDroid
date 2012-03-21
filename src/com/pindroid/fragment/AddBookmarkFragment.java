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

import com.pindroid.R;
import com.pindroid.action.GetWebpageTitleTask;
import com.pindroid.activity.FragmentBaseActivity;
import com.pindroid.client.PinboardApi;
import com.pindroid.client.TooManyRequestsException;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.ui.TagSpan;
import com.pindroid.util.SpaceTokenizer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AddBookmarkFragment extends Fragment {

	private FragmentBaseActivity base;
	
	private EditText mEditUrl;
	private EditText mEditDescription;
	private ProgressBar mDescriptionProgress;
	private EditText mEditNotes;
	private MultiAutoCompleteTextView mEditTags;
	private TextView mRecommendedTags;
	private ProgressBar mRecommendedProgress;
	private TextView mPopularTags;
	private ProgressBar mPopularProgress;
	private CheckBox mPrivate;
	private CheckBox mToRead;
	private Bookmark bookmark;
	private Bookmark oldBookmark;
	private Boolean update = false;
	
	private long updateTime = 0;
	
	private AsyncTask<String, Integer, String> titleTask;
	private AsyncTask<String, Integer, ArrayList<Tag>> tagTask;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		base = (FragmentBaseActivity)getActivity();
		
		setHasOptionsMenu(true);
		
		mEditUrl = (EditText) getView().findViewById(R.id.add_edit_url);
		mEditDescription = (EditText) getView().findViewById(R.id.add_edit_description);
		mDescriptionProgress = (ProgressBar) getView().findViewById(R.id.add_description_progress);
		mEditNotes = (EditText) getView().findViewById(R.id.add_edit_notes);
		mEditTags = (MultiAutoCompleteTextView) getView().findViewById(R.id.add_edit_tags);
		mRecommendedTags = (TextView) getView().findViewById(R.id.add_recommended_tags);
		mRecommendedProgress = (ProgressBar) getView().findViewById(R.id.add_recommended_tags_progress);
		mPopularTags = (TextView) getView().findViewById(R.id.add_popular_tags);
		mPopularProgress = (ProgressBar) getView().findViewById(R.id.add_popular_tags_progress);
		mPrivate = (CheckBox) getView().findViewById(R.id.add_edit_private);
		mToRead = (CheckBox) getView().findViewById(R.id.add_edit_toread);
		
		mRecommendedTags.setMovementMethod(LinkMovementMethod.getInstance());
		mPopularTags.setMovementMethod(LinkMovementMethod.getInstance());
		
		if(base.mAccount != null){
			String[] tagArray = new String[5];
			tagArray = TagManager.GetTagsAsArray(base.mAccount.name, null, base).toArray(tagArray);
			ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(base, R.layout.autocomplete_view, tagArray);
			mEditTags.setAdapter(autoCompleteAdapter);
			mEditTags.setTokenizer(new SpaceTokenizer());
		}

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
	
	@Override
	public void onStart(){
		super.onStart();
		
		if(bookmark != null){
			mEditUrl.setText(bookmark.getUrl());
			
			if(bookmark.getDescription() != null)
				mEditDescription.setText(bookmark.getDescription());
			
			if(bookmark.getNotes() != null)
				mEditNotes.setText(bookmark.getNotes());
			
			if(bookmark.getTagString() != null)
				mEditTags.setText(bookmark.getTagString());
			
			mPrivate.setChecked(!bookmark.getShared());
			mToRead.setChecked(bookmark.getToRead());
			
			if(mEditDescription.getText().toString().equals(""))
				titleTask = new GetTitleTask().execute(bookmark.getUrl());

			tagTask = new GetTagSuggestionsTask().execute(bookmark.getUrl());
		} else {
			mEditUrl.requestFocus();
			setDefaultValues();
		}
	}
	
	public void loadBookmark(Bookmark b, Bookmark oldB){
		if(b != null)
			bookmark = b.copy();
		
		if(oldB != null)
			oldBookmark = oldB.copy();
	}
	
	public void saveHandler(View v) {
		save();
	}
	
	public void cancelHandler(View v) {    	
    	base.finish();
	}
	
	private void setDefaultValues(){   	
    	mPrivate.setChecked(base.privateDefault);
    	mToRead.setChecked(base.toreadDefault);
	}
	
    private void save() {
    	
    	String url = mEditUrl.getText().toString();
    	String description = mEditDescription.getText().toString();
    	
    	if(description.equals(""))
    		description = getResources().getString(R.string.add_bookmark_default_title);
    	
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
		
		if(!update) {
			Date d = new Date();
			updateTime = d.getTime();
		}
			
		bookmark = new Bookmark(url, description, 
				mEditNotes.getText().toString(), tagstring.trim(),
				!mPrivate.isChecked(), mToRead.isChecked(), updateTime);
		
		bookmark.setId(oldid);
		
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
        		mEditTags.setSelection(mEditTags.getText().length());
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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TooManyRequestsException e) {
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
