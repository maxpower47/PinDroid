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

import android.accounts.Account;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FilterQueryProvider;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pindroid.R;
import com.pindroid.client.NetworkUtilities;
import com.pindroid.client.PinboardClient;
import com.pindroid.event.AccountChangedEvent;
import com.pindroid.listadapter.TagAutoCompleteCursorAdapter;
import com.pindroid.model.TagSuggestions;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.ui.TagSpan;
import com.pindroid.util.AccountHelper;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.SpaceTokenizer;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.MaterialMultiAutoCompleteTextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.add_bookmark_fragment)
@OptionsMenu(R.menu.add_bookmark_menu)
public class AddBookmarkFragment extends Fragment {
	
	@ViewById(R.id.add_edit_url) MaterialEditText mEditUrl;
    @ViewById(R.id.add_edit_description) MaterialEditText mEditDescription;
    @ViewById(R.id.add_description_progress) ProgressBar mDescriptionProgress;
    @ViewById(R.id.add_edit_notes) MaterialEditText mEditNotes;
    @ViewById(R.id.add_edit_tags) MaterialMultiAutoCompleteTextView mEditTags;
    @ViewById(R.id.add_recommended_tags) TextView mRecommendedTags;
    @ViewById(R.id.add_popular_tags) TextView mPopularTags;
    @ViewById(R.id.add_edit_private) CompoundButton mPrivate;
    @ViewById(R.id.add_edit_toread) CompoundButton mToRead;

    private Bookmark bookmark;
	private Bookmark oldBookmark;
	private Boolean update = false;
	private String username = null;

    private Boolean firstRun = true;
	
	private long updateTime = 0;

	private OnBookmarkSaveListener bookmarkSaveListener;
	
	public interface OnBookmarkSaveListener {
		void onBookmarkSave(Bookmark b);
		void onBookmarkCancel(Bookmark b);
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        firstRun = savedInstanceState == null;

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
	}

    @FocusChange(R.id.add_edit_url)
    public void urlFocusChanged(View urlView, boolean hasFocus) {
        if(!hasFocus){
            String url = mEditUrl.getText().toString().trim();

            if(!url.equals("")) {
                if(mEditDescription.getText().toString().equals("")) {
                    getWebpageTitle(url);
                }
                getTagSuggestions(url);
            }
        }
    }
	
	@Override
	public void onStart(){
		super.onStart();
		refreshView();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
	
	public void loadBookmark(Bookmark b, Bookmark oldB){
		if(b != null) {
            bookmark = b.copy();
        }
		
		if(oldB != null) {
            oldBookmark = oldB.copy();
        }
	}

    public void onEvent(AccountChangedEvent event) {
        this.username = event.getNewAccount();
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
                getWebpageTitle(bookmark.getUrl());
            }

			getTagSuggestions(bookmark.getUrl());

            if(bookmark.getId() == 0) {
                getActivity().setTitle(R.string.add_bookmark_add_title);
            } else {
                getActivity().setTitle(R.string.add_bookmark_edit_title);
            }
			

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
				!mPrivate.isChecked(), mToRead.isChecked(), new Date(updateTime));
		
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

    @OptionsItem(R.id.menu_addbookmark_save)
    void saveBookmark() {
        save();
        bookmarkSaveListener.onBookmarkSave(bookmark);
    }

    @OptionsItem(R.id.menu_addbookmark_cancel)
    void cancel() {
        bookmarkSaveListener.onBookmarkCancel(bookmark);
    }
	
    final TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
        	String currentTagString = mEditTags.getText().toString();
        	
        	ArrayList<String> currentTags = new ArrayList<>();
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

    public void getWebpageTitle(String url) {
        mDescriptionProgress.setVisibility(View.VISIBLE);
        loadWebpageTitle(url);
    }

    @Background
    public void loadWebpageTitle(String url) {
        updateWebpageTitle(NetworkUtilities.getWebpageTitle(url));
    }

    @UiThread
    public void updateWebpageTitle(String title) {
        mEditDescription.setText(Html.fromHtml(title));
        mDescriptionProgress.setVisibility(View.GONE);
    }

    public void getTagSuggestions(String url) {
        mRecommendedTags.setVisibility(View.GONE);
        mPopularTags.setVisibility(View.GONE);
        loadTagSuggestions(url);
    }

    @Background
    public void loadTagSuggestions(String url) {
        try {
            Account account = AccountHelper.getAccount(username, getActivity());

            if(!url.startsWith("http")){
                url = "http://" + url;
            }

            updateTagSuggestions(PinboardClient.get().getTagSuggestions(AccountHelper.getAuthToken(getActivity(), account), url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public void updateTagSuggestions(List<TagSuggestions> suggestions) {
        if(suggestions != null) {
            SpannableStringBuilder recommendedBuilder = new SpannableStringBuilder();
            SpannableStringBuilder popularBuilder = new SpannableStringBuilder();

            for(TagSuggestions ts : suggestions) {
                for(String t : ts.getPopular()) {
                    addTag(popularBuilder, t);
                }

                for(String t : ts.getRecommended()) {
                    addTag(recommendedBuilder, t);
                }
            }

            mRecommendedTags.setText(recommendedBuilder);
            mPopularTags.setText(popularBuilder);

            mRecommendedTags.setVisibility(View.VISIBLE);
            mPopularTags.setVisibility(View.VISIBLE);
        }
    }

    private void addTag(SpannableStringBuilder builder, String t) {
        int flags = 0;

        if (builder.length() != 0) {
            builder.append("   ");
        }

        int start = builder.length();
        builder.append(t);
        int end = builder.length();

        TagSpan span = new TagSpan(t);
        span.setOnTagClickListener(tagOnClickListener);

        builder.setSpan(span, start, end, flags);
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
}
