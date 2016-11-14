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
package com.pindroid.ui;


import com.pindroid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NsMenuAdapter extends ArrayAdapter<NsMenuItemModel> {

	public NsMenuAdapter(Context context) {
		super(context, 0);
	}

	public void addHeader(int title) {
		add(new NsMenuItemModel(title, -1, true));
	}

	public void addItem(int title, int icon) {
		add(new NsMenuItemModel(title, icon, false));
	}

	public void addItem(int title) {
		add(new NsMenuItemModel(title));
	}

    public void addItem(String title) {
        add(new NsMenuItemModel(title));
    }
	
	public void addItem(NsMenuItemModel itemModel) {
		add(itemModel);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).isHeader ? 0 : 1;
	}

	@Override
	public boolean isEnabled(int position) {
		return !getItem(position).isHeader;
	}

	public static class ViewHolder {
		public final TextView textHolder;
		public final ImageView imageHolder;
		public final TextView textCounterHolder;

		public ViewHolder(TextView text1, ImageView image1, TextView textcounter1) {
			this.textHolder = text1;
			this.imageHolder = image1;
			this.textCounterHolder=textcounter1;
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		NsMenuItemModel item = getItem(position);
		ViewHolder holder = null;
		View view = convertView;

		if (view == null) {
			int layout = R.layout.main_view;
			if (item.isHeader) {
                layout = R.layout.menu_header;
            }

			view = LayoutInflater.from(getContext()).inflate(layout, null);

			TextView text1 = (TextView) view.findViewById(R.id.menurow_title);
			ImageView image1 = (ImageView) view.findViewById(R.id.menurow_icon);
			TextView textcounter1 = (TextView) view.findViewById(R.id.menurow_counter);
			view.setTag(new ViewHolder(text1, image1,textcounter1));
		}

		if (holder == null && view != null) {
			Object tag = view.getTag();
			if (tag instanceof ViewHolder) {
				holder = (ViewHolder) tag;
			}
		}
		
		
	    if(item != null && holder != null)
	    {
	    	if (holder.textHolder != null)
                if(item.stringTitle != null) {
                    holder.textHolder.setText(item.stringTitle);
                } else {
                    holder.textHolder.setText(item.title);
                }
	    	
	    	if (holder.textCounterHolder != null){
                holder.textCounterHolder.setText(item.counter > 0 ? String.valueOf(item.counter) : "");
			}
	    	
	        if (holder.imageHolder != null) {
				if (item.iconRes > 0) {
					holder.imageHolder.setVisibility(View.VISIBLE);
					holder.imageHolder.setImageResource(item.iconRes);
				} else {
					holder.imageHolder.setVisibility(View.GONE);
				}
			}
	    }
	    
	    return view;		
	}

}