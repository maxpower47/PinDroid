package com.android.droidlicious.listadapter;

import java.util.ArrayList;

import com.android.droidlicious.R;
import com.android.droidlicious.client.User;
import com.android.droidlicious.providers.TagContent.Tag;

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
