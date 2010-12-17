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

package com.pindroid.listadapter;

import java.util.ArrayList;

import com.deliciousdroid.R;
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
        
        Tag o = tags.get(position);
        if (o != null) {
          	holder.name.setText(o.getTagName());                            
          	holder.count.setText(Integer.toString(o.getCount()));                            
        }
        return convertView;
    }
}
