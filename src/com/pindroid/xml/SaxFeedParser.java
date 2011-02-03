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

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.util.DateParser;

public class SaxFeedParser {

	private InputStream is;
	private String nsRdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private String nsDc = "http://purl.org/dc/elements/1.1/";
	private String ns = "http://purl.org/rss/1.0/";
	
    public SaxFeedParser(InputStream stream) {
    	is = stream;
    }

    public ArrayList<Bookmark> parse() throws ParseException {
        final Bookmark currentBookmark = new Bookmark();
        final RootElement root = new RootElement(nsRdf, "RDF");
        final Element item = root.getChild(ns, "item");
        final ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
        
        item.setEndElementListener(new EndElementListener(){
            public void end() {
                bookmarks.add(currentBookmark.copy());
                currentBookmark.clear();
            }
        });
        item.getChild(ns, "title").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentBookmark.setDescription(body);
            }
        });
        item.getChild(nsDc, "date").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	try {
					currentBookmark.setTime(DateParser.parseTime(body.trim()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
        item.getChild(ns, "link").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentBookmark.setUrl(body);
            }
        });
        item.getChild(nsDc, "creator").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentBookmark.setAccount(body);
            }
        });
        item.getChild(ns, "description").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentBookmark.setNotes(body.trim());
            }
        });
        item.getChild(nsDc, "subject").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentBookmark.setTagString(body.trim());
            }
        });
        item.getChild(nsDc, "source").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentBookmark.setSource(body.trim());
            }
        });

        try {
            Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
        return bookmarks;
    }
}