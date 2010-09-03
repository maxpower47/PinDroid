package com.android.droidlicious.listadapter;

import java.util.ArrayList;

import com.android.droidlicious.R;
import com.android.droidlicious.client.User;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BookmarkListAdapter extends ArrayAdapter<User.Bookmark> {
	
	private ArrayList<User.Bookmark> bookmarks;
	
    public BookmarkListAdapter(Context context, int textViewResourceId, ArrayList<User.Bookmark> bookmarks) {
        super(context, textViewResourceId, bookmarks);
        this.bookmarks = bookmarks;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.bookmark_view, null);
        }
        User.Bookmark o = bookmarks.get(position);
        if (o != null) {
         	TextView td = (TextView) v.findViewById(R.id.bookmark_description);
         	TextView tu = (TextView) v.findViewById(R.id.bookmark_url);
            if (td != null) {
               	td.setText(o.getDescription());                            
            }
            if (tu != null) {
               	tu.setText(o.getUrl());                            
            }

        }
        return v;
    }
}
