/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * DeliciousDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeliciousDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.listadapter;

import java.util.ArrayList;

import com.deliciousdroid.R;
import com.deliciousdroid.listadapter.ViewHolder.BookmarkListViewHolder;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BookmarkListAdapter extends ArrayAdapter<Bookmark> {
	
	private ArrayList<Bookmark> bookmarks;
	
    public BookmarkListAdapter(Context context, int textViewResourceId, ArrayList<Bookmark> bookmarks) {
        super(context, textViewResourceId, bookmarks);
        this.bookmarks = bookmarks;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	BookmarkListViewHolder holder;
    	
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.bookmark_view, null);
            
            holder = new BookmarkListViewHolder();
            holder.description = (TextView) convertView.findViewById(R.id.bookmark_description);
            holder.tags = (TextView) convertView.findViewById(R.id.bookmark_tags);
            
            convertView.setTag(holder);
        } else {
        	holder = (BookmarkListViewHolder) convertView.getTag();
        }
        
        Bookmark o = bookmarks.get(position);
        if (o != null) {
            holder.description.setText(o.getDescription());                            
            holder.tags.setText(o.getTags());                            
        }
        return convertView;
    }
}
