/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
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
import com.deliciousdroid.providers.TagContent.Tag;

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
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.tag_view, null);
            }
            Tag o = tags.get(position);
            if (o != null) {
            	TextView tn = (TextView) v.findViewById(R.id.tag_name);
            	TextView tc = (TextView) v.findViewById(R.id.tag_count);
                if (tn != null) {
                  	tn.setText(o.getTagName());                            
                }
                if (tc != null) {
                  	tc.setText(Integer.toString(o.getCount()));                            
                }
            }
            return v;
    }
}
