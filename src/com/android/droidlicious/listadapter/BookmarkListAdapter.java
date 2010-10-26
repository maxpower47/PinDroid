package com.android.droidlicious.listadapter;

import java.util.ArrayList;

import com.android.droidlicious.R;
import com.android.droidlicious.providers.BookmarkContent.Bookmark;

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
    	View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.bookmark_view, null);
        }
        Bookmark o = bookmarks.get(position);
        if (o != null) {
         	TextView td = (TextView) v.findViewById(R.id.bookmark_description);
         	TextView tu = (TextView) v.findViewById(R.id.bookmark_tags);
            if (td != null) {
               	td.setText(o.getDescription());                            
            }
            if (tu != null) {
               	tu.setText(o.getTags());                            
            }

        }
        return v;
    }
}
