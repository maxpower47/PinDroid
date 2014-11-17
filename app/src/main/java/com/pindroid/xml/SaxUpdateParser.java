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

import org.xml.sax.Attributes;

import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import com.pindroid.client.Update;
import com.pindroid.util.DateParser;

public class SaxUpdateParser {

	private InputStream is;
	
    public SaxUpdateParser(InputStream stream) {
    	is = stream;
    }

    public Update parse() throws ParseException {
        final Update update = new Update();
        final RootElement root = new RootElement("update");

        root.setStartElementListener(new StartElementListener(){
            public void start(Attributes attributes) {
            	final String time = attributes.getValue("time");
            	long updateTime;
				try {
					updateTime = DateParser.parse(time).getTime();
					update.setLastUpdate(updateTime);
				} catch (ParseException e) {
					update.setLastUpdate(0);
					e.printStackTrace();
				}	
            }
        });

        try {
            Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
        return update;
    }
}