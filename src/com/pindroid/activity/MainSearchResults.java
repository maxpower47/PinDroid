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

import com.pindroid.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;

public class MainSearchResults extends AppBaseListActivity {

	private Context mContext;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		String[] MENU_ITEMS = new String[] {res.getString(R.string.search_results_bookmark),
				res.getString(R.string.search_results_tag)};
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_view, MENU_ITEMS));
		mContext = this;

		final Intent intent = getIntent();
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	if(position == 0){
		    		
		    		Intent i = new Intent(mContext, BrowseBookmarks.class);
		    		i.setAction(Intent.ACTION_SEARCH);
		    		i.putExtras(intent.getExtras());
		    		
		    		startActivity(i);
		    	} else if(position == 1){
		    		
		    		Intent i = new Intent(mContext, BrowseTags.class);
		    		i.setAction(Intent.ACTION_SEARCH);
		    		i.putExtras(intent.getExtras());
		    		
		    		startActivity(i);
		    	}
		    }
		});
	}
}