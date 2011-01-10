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

package com.pindroid.listadapter;

import java.util.ArrayList;

import com.pindroid.R;
import com.pindroid.listadapter.ViewHolder.TagListViewHolder;
import com.pindroid.providers.TagContent.Tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TagListAdapter extends ArrayAdapter<Tag> {
	
	private ArrayList<Tag> tags;
	
    public TagListAdapter(Context context, int textViewResourceId, ArrayList<Tag> tags) {
        super(context, textViewResourceId, tags);
        this.tags = tags;
    }
    
    public void update(ArrayList<Tag> t) {
    	tags = t;
    }
    
    @Override
    public int getCount() {
    	return tags.size();
    }
    
    @Override
    public Tag getItem(int position) {
		return tags.get(position);   	
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	TagListViewHolder holder;
    	
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.tag_view, null);
            
            holder = new TagListViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tag_name);
            holder.count = (TextView) convertView.findViewById(R.id.tag_count);
            convertView.setTag(holder);
        } else {
        	holder = (TagListViewHolder) convertView.getTag();
        }
        
        if(tags.size() > 0) {
	        Tag o = tags.get(position);
	        if (o != null) {
	          	holder.name.setText(o.getTagName());                            
	          	holder.count.setText(Integer.toString(o.getCount()));                            
	        }
        } else {
        	return new View(this.getContext());
        }
        return convertView;
    }
}
