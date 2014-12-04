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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.iangclifton.android.floatlabel.FloatLabel;
import com.pindroid.R;
import com.pindroid.action.GetWebpageTitleTask;
import com.pindroid.client.PinboardApi;
import com.pindroid.listadapter.TagAutoCompleteCursorAdapter;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.ui.TagSpan;
import com.pindroid.util.AccountHelper;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.SpaceTokenizer;

import android.accounts.Account;
import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AddBookmarkFragment extends Fragment implements PindroidFragment {
	
	private EditText mEditUrl;
	private EditText mEditDescription;
	private ProgressBar mDescriptionProgress;
	private EditText mEditNotes;
	private MultiAutoCompleteTextView mEditTags;
	private TextView mRecommendedTags;
	private TextView mPopularTags;
	private CompoundButton mPrivate;
	private CompoundButton mToRead;
	private Bookmark bookmark;
	private Bookmark oldBookmark;
	private Boolean update = false;
	private String username = null;

    private Boolean firstRun = true;
	
	private long updateTime = 0;
	
	private AsyncTask<String, Integer, String> titleTask;
	private AsyncTask<String, Integer, ArrayList<Tag>> tagTask;
	
	private OnBookmarkSaveListener bookmarkSaveListener;
	
	public interface OnBookmarkSaveListener {
		public void onBookmarkSave(Bookmark b);
		public void onBookmarkCancel(Bookmark b);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

        firstRun = savedInstanceState == null;

        setHasOptionsMenu(true);
		
		mEditUrl = ((FloatLabel) getView().findViewById(R.id.add_edit_url)).getEditText();
		mEditDescription = ((FloatLabel) getView().findViewById(R.id.add_edit_description)).getEditText();
		mDescriptionProgress = (ProgressBar) getView().findViewById(R.id.add_description_progress);
		mEditNotes = ((FloatLabel) getView().findViewById(R.id.add_edit_notes)).getEditText();
		mEditTags = (MultiAutoCompleteTextView)((FloatLabel) getView().findViewById(R.id.add_edit_tags)).getEditText();
		mRecommendedTags = (TextView) getView().findViewById(R.id.add_recommended_tags);
		mPopularTags = (TextView) getView().findViewById(R.id.add_popular_tags);
		mPrivate = (CompoundButton) getView().findViewById(R.id.add_edit_private);
		mToRead = (CompoundButton) getView().findViewById(R.id.add_edit_toread);
		
		mRecommendedTags.setMovementMethod(LinkMovementMethod.getInstance());
		mPopularTags.setMovementMethod(LinkMovementMethod.getInstance());
		
		if(username != null){
			CursorAdapter autoCompleteAdapter = new TagAutoCompleteCursorAdapter(getActivity(), R.layout.autocomplete_view, null, 
					new String[]{Tag.Name, Tag.Count}, new int[]{R.id.autocomplete_name, R.id.autocomplete_count}, 0);

			autoCompleteAdapter.setFilterQueryProvider(new FilterQueryProvider() {
	            public Cursor runQuery(CharSequence constraint) {
	            	return TagManager.GetTagsAsCursor((constraint != null ? constraint.toString() : ""), 
	            			username, Tag.Count + " DESC, " + Tag.Name + " ASC", getActivity());
	            }
	        });

			mEditTags.setAdapter(autoCompleteAdapter);
			mEditTags.setTokenizer(new SpaceTokenizer());
		}


		mEditUrl.setOnFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					String url = mEditUrl.getText().toString().trim();
					
					if(url != null && !url.equals("")) {
						if(mEditDescription.getText().toString().equals("")) {
							titleTask = new GetTitleTask().execute(url);
						}
						tagTask = new GetTagSuggestionsTask().execute(url);
					}
				}
			}
		});
	}
	
	@Override
	public void onStart(){
		super.onStart();
		
		refreshView();
	}
	
	public void loadBookmark(Bookmark b, Bookmark oldB){
		if(b != null) {
            bookmark = b.copy();
        }
		
		if(oldB != null) {
            oldBookmark = oldB.copy();
        }
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public void refreshView(){
		if(bookmark != null){
			mEditUrl.setText(bookmark.getUrl());
			
			if(bookmark.getDescription() != null) {
                mEditDescription.setText(bookmark.getDescription());
            }
			
			if(bookmark.getNotes() != null) {
                mEditNotes.setText(bookmark.getNotes());
            }
			
			if(bookmark.getTagString() != null) {
                mEditTags.setText(bookmark.getTagString());
            }
			else {
                mEditTags.setText("");
            }

            if(firstRun) {
                mPrivate.setChecked(!bookmark.getShared());
                mToRead.setChecked(bookmark.getToRead());
            }
			
			if(mEditDescription.getText().toString().equals("")) {
                titleTask = new GetTitleTask().execute(bookmark.getUrl());
            }

			tagTask = new GetTagSuggestionsTask().execute(bookmark.getUrl());
			
			getActivity().setTitle(R.string.add_bookmark_edit_title);
		} else {
			if(!this.isHidden()){
				mEditUrl.requestFocus();
			}

            if(firstRun) {
                setDefaultValues();
            }
			
			getActivity().setTitle(R.string.add_bookmark_add_title);
		}
	}
	
	private void setDefaultValues(){   	
    	mPrivate.setChecked(SettingsHelper.getPrivateDefault(getActivity()));
    	mToRead.setChecked(SettingsHelper.getToReadDefault(getActivity()));
	}
	
    private void save() {
    	
    	String url = mEditUrl.getText().toString();
    	String description = mEditDescription.getText().toString();
    	
    	if(description.equals(""))
    		description = getResources().getString(R.string.add_bookmark_default_title);
    	
    	if(url.equals("")) {
    		Toast.makeText(getActivity(), R.string.add_bookmark_blank_url, Toast.LENGTH_LONG).show();
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
		if(bookmark != null && oldBookmark != null && bookmark.getId() != 0) {
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
		
		bookmark.setAccount(username);
		
		if(update){
			BookmarkManager.UpdateBookmark(bookmark, username, getActivity());
			
			for(Tag t : oldBookmark.getTags()){
				if(!bookmark.getTags().contains(t)) {
					TagManager.UpleteTag(t, username, getActivity());
				}
			}
		} else {
			BookmarkManager.AddBookmark(bookmark, username, getActivity());
		}
		
		for(Tag t : bookmark.getTags()){   				
			TagManager.UpsertTag(t, username, getActivity());
		}
    }
    
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.add_bookmark_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_addbookmark_save:
	    	save();
			bookmarkSaveListener.onBookmarkSave(bookmark);
			return true;
	    case R.id.menu_addbookmark_cancel:
        	bookmarkSaveListener.onBookmarkCancel(bookmark);
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
        	
        	if(tag != null && !tag.equals("")) {
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
	
    		if(getActivity() != null) {
	    		try {
	    			Account account = AccountHelper.getAccount(username, getActivity());
	    			
					return PinboardApi.getSuggestedTags(url, account, getActivity());
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
			return null;
    	}
    	
    	protected void onPreExecute() {
    		mRecommendedTags.setVisibility(View.GONE);
    		mPopularTags.setVisibility(View.GONE);
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
        	} 	
        }

		private void addTag(SpannableStringBuilder builder, Tag t) {
			int flags = 0;
			
			if (builder.length() != 0) {
				builder.append("   ");
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
        return inflater.inflate(R.layout.add_bookmark_fragment, container, false);
    }
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			bookmarkSaveListener = (OnBookmarkSaveListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnBookmarkSaveListener");
		}
	}

	public void refresh() {
		// TODO Auto-generated method stub
		
	}
}
