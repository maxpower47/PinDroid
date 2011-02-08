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

package com.pindroid.xml;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

import org.xml.sax.Attributes;

import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

import com.pindroid.providers.TagContent.Tag;
import com.pindroid.util.IntUtils;

public class SaxTagParser {

	private InputStream is;
	
    public SaxTagParser(InputStream stream) {
    	is = stream;
    }

    public ArrayList<Tag> parse() throws ParseException {
        final Tag currentTag = new Tag();
        final RootElement root = new RootElement("tags");
        final ArrayList<Tag> tags = new ArrayList<Tag>();

        root.getChild("tag").setStartElementListener(new StartElementListener(){
            public void start(Attributes attributes) {
            	final String count = attributes.getValue("count");
            	final String tag = attributes.getValue("tag");
            	
            	if(count != null) {
            		currentTag.setCount(IntUtils.parseUInt(count));
            	}
            	if(tag != null) {
            		currentTag.setTagName(tag);
            	}

            	tags.add(currentTag.copy());
            }
        });
        try {
            Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
        return tags;
    }
    
    public ArrayList<Tag> parseSuggested() throws ParseException {
        final Tag currentTag = new Tag();
        final RootElement root = new RootElement("suggested");
        final ArrayList<Tag> tags = new ArrayList<Tag>();

        root.getChild("popular").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentTag.setTagName(body);
            	currentTag.setType("popular");

            	tags.add(currentTag.copy());
            }
        });
        root.getChild("recommended").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentTag.setTagName(body);
            	currentTag.setType("recommended");

            	tags.add(currentTag.copy());
            }
        });
        try {
            Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
        	Log.d("parse error", e.getMessage());
            throw new ParseException(e.getMessage(), 0);
        }
        return tags;
    }
}