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

import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import com.pindroid.providers.NoteContent.Note;
import com.pindroid.util.DateParser;

public class SaxNoteListParser {

	private InputStream is;
	
    public SaxNoteListParser(InputStream stream) {
    	is = stream;
    }

    public ArrayList<Note> parse() throws ParseException {
        final Note currentNote = new Note();
        final RootElement root = new RootElement("notes");
        final ArrayList<Note> notes = new ArrayList<Note>();

        root.getChild("note").setStartElementListener(new StartElementListener(){
            public void start(Attributes attributes) {
            	final String id = attributes.getValue("id");
            	
            	currentNote.setPid(id);
            }
        });
        root.getChild("note").getChild("title").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentNote.setTitle(body);
            }
        });
        root.getChild("note").getChild("hash").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentNote.setHash(body);
            }
        });
        root.getChild("note").getChild("created_at").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentNote.setAdded(DateParser.parseTime(body));
            }
        });
        root.getChild("note").getChild("updated_at").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	currentNote.setUpdated(DateParser.parseTime(body));
            }
        });
        root.getChild("note").setEndElementListener(new EndElementListener(){
            public void end() {
            	notes.add(currentNote.copy());
            	currentNote.clear();
            }
        });
        try {
            Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
        return notes;
    }
}
