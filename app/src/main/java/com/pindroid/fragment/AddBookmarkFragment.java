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

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.pindroid.R;
import com.pindroid.event.AccountChangedEvent;
import com.pindroid.model.Bookmark;
import com.pindroid.model.Tag;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.ui.AddBookmarkView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

@EFragment(R.layout.add_bookmark_fragment)
@OptionsMenu(R.menu.add_bookmark_menu)
public class AddBookmarkFragment extends Fragment {
	
	@ViewById(R.id.add_bookmark_view) AddBookmarkView addBookmarkView;

    private Bookmark bookmark;
	private String username = null;

	private OnBookmarkSaveListener bookmarkSaveListener;
	
	public interface OnBookmarkSaveListener {
		void onBookmarkSave(Bookmark b);
		void onBookmarkCancel();
	}

	@Override
	public void onStart(){
		super.onStart();
        updateTitle();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
	
	public void loadBookmark(Bookmark b){
		bookmark = b;
        addBookmarkView.bind(bookmark);
        updateTitle();
	}

    @Subscribe(sticky = true)
    public void onAccountChanged(AccountChangedEvent event) {
        this.username = event.getNewAccount();
    }

	private void updateTitle(){
		if(bookmark != null){
            if(bookmark.getId() == 0) {
                getActivity().setTitle(R.string.add_bookmark_add_title);
            } else {
                getActivity().setTitle(R.string.add_bookmark_edit_title);
            }
		} else {
			getActivity().setTitle(R.string.add_bookmark_add_title);
		}
	}

    @OptionsItem(R.id.menu_addbookmark_save)
    void saveBookmark() {

        Bookmark b = addBookmarkView.getBookmark();

        if(bookmark != null && bookmark.getId() != 0){
            BookmarkManager.UpdateBookmark(b, username, getActivity());

            for(Tag t : bookmark.getTags()){
                if(!b.getTags().contains(t)) {
                    TagManager.UpleteTag(t, username, getActivity());
                }
            }
        } else {
            BookmarkManager.AddBookmark(b, username, getActivity());
        }

        for(Tag t : b.getTags()){
            TagManager.UpsertTag(t, username, getActivity());
        }

        bookmarkSaveListener.onBookmarkSave(b);
    }

    @OptionsItem(R.id.menu_addbookmark_cancel)
    void cancel() {
        bookmarkSaveListener.onBookmarkCancel();
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
