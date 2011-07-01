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

import com.pindroid.R;
import com.pindroid.activity.BrowseBookmarks;
import com.pindroid.activity.FragmentBaseActivity;
import com.pindroid.activity.MainSearchResults;
import com.pindroid.activity.ViewBookmark;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;

public class MainFragment extends ListFragment {

	private FragmentBaseActivity base;
	
	private OnMainActionListener mainActionListener;
	
	public interface OnMainActionListener {
		public void onMyBookmarksSelected();
		public void onMyUnreadSelected();
		public void onMyTagsSelected();
		public void onMyNetworkSelected();
		public void onRecentSelected();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		base = (FragmentBaseActivity)getActivity();
		
		String[] MENU_ITEMS = new String[] {getString(R.string.main_menu_my_bookmarks),
				getString(R.string.main_menu_my_unread_bookmarks),
				getString(R.string.main_menu_my_tags),
				getString(R.string.main_menu_recent_bookmarks),
				getString(R.string.main_menu_network_bookmarks)};
		
		if(base.secretToken == null || base.secretToken.equals("")) {
			MENU_ITEMS = new String[] {getString(R.string.main_menu_my_bookmarks),
					getString(R.string.main_menu_my_unread_bookmarks),
					getString(R.string.main_menu_my_tags),
					getString(R.string.main_menu_recent_bookmarks)};
		}
		
		setListAdapter(new ArrayAdapter<String>(base, R.layout.main_view, MENU_ITEMS));

		Intent intent = base.getIntent();

		if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			if(intent.hasExtra(SearchManager.QUERY)){
				Intent i = new Intent(base, MainSearchResults.class);
				i.putExtras(base.getIntent().getExtras());
				startActivity(i);
				base.finish();
			} else {
				base.onSearchRequested();
			}
		} else if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			
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
		}
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	if(position == 0){
		    		mainActionListener.onMyBookmarksSelected();
		    	} else if(position == 1){
		    		mainActionListener.onMyUnreadSelected();
		    	} else if(position == 2){
		    		mainActionListener.onMyTagsSelected();
		    	} else if(position == 3){
		    		mainActionListener.onRecentSelected();
		    	} else if(position == 4){
		    		mainActionListener.onMyNetworkSelected();
		    	} 
		    }
		});
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mainActionListener = (OnMainActionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnMainActionListener");
		}
	}
}