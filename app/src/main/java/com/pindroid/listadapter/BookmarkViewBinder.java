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

import com.pindroid.R;

import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BookmarkViewBinder implements SimpleCursorAdapter.ViewBinder {

	public boolean setViewValue(View v, Cursor c, int columnIndex) {
        switch(v.getId()) {
            case R.id.bookmark_description:
            case R.id.bookmark_feed_description:
            	((TextView)v).setText(c.getString(columnIndex));
            	break;
            case R.id.bookmark_tags:
            case R.id.bookmark_feed_tags:
            	((TextView)v).setText(c.getString(columnIndex));
            	break;
            case R.id.bookmark_unread:
            	if(c.getInt(columnIndex) == 1)
            		v.setVisibility(View.VISIBLE);
            	else v.setVisibility(View.INVISIBLE);
            	break;
            case R.id.bookmark_synced:
            	if(c.getInt(columnIndex) == 0)
            		v.setVisibility(View.INVISIBLE);
            	else v.setVisibility(View.VISIBLE);
            	
            	if(c.getInt(columnIndex) == -1)
            		((ImageView)v).setImageResource(R.drawable.sync_fail);
            	else ((ImageView)v).setImageResource(R.drawable.sync);
            	break;
            case R.id.bookmark_private:
                if(c.getInt(columnIndex) == 0)
                	v.setVisibility(View.VISIBLE);
                	else v.setVisibility(View.INVISIBLE);
            	break;
        }

		return true;
	}
}
