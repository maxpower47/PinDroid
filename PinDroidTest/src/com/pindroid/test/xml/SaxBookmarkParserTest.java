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

package com.pindroid.test.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.xml.SaxBookmarkParser;

import android.test.AndroidTestCase;

public class SaxBookmarkParserTest extends AndroidTestCase  {
	
	private String singleBookmarkTest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
        "<posts user=\"user\">" + 
          "<post href=\"http://www.howtocreate.co.uk/tutorials/texterise.php?dom=1\" " + 
            "description=\"JavaScript DOM reference\" " + 
            "extended=\"dom reference\" " + 
            "hash=\"c0238dc0c44f07daedd9a1fd9bbdeebd\" " + 
            "meta=\"92959a96fd69146c5fe7cbde6e5720f2\" " +
            "tag=\"dom javascript webdev\" time=\"2005-11-28T05:26:09Z\" />" +
        "</posts>";
	
	private String multipleBookmarkTest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
		"<posts user=\"maxpower47\">" + 
		  "<post href=\"http://f-droid.org/wiki/page/com.pindroid\" time=\"2013-01-31T21:30:36Z\" description=\"com.pindroid - F-Droid\" extended=\"\" tag=\"pinboard pindroid\" hash=\"fafff755581363fa720dc8894212e522\" meta=\"f86784d71f229e3b8d061a09b5d60c6f\"    />" +
		  "<post href=\"http://cybernetnews.com/pinboard-android/\" time=\"2013-01-31T21:29:30Z\" description=\"PinDroid: Pinboard for Android\" extended=\"\" tag=\"pindroid pinboard\" hash=\"a84f543a510d8218f16824cf059d33a8\" meta=\"e3f30dc2166dbf73cf1636095fd8bcb6\"    />" +
		"</posts>";

	public SaxBookmarkParserTest(){
		super();
	}
	
	public void testSingleBookmarkParsing() throws ParseException{

		InputStream is = new ByteArrayInputStream(singleBookmarkTest.getBytes());
		
		SaxBookmarkParser parser = new SaxBookmarkParser(is);
		
		ArrayList<Bookmark> r = parser.parse();
			
		assertEquals(1, r.size());
		
		Bookmark b = r.get(0);
		assertEquals("http://www.howtocreate.co.uk/tutorials/texterise.php?dom=1", b.getUrl());
		assertEquals("JavaScript DOM reference", b.getDescription());
		assertEquals("dom reference", b.getNotes());
		assertEquals("c0238dc0c44f07daedd9a1fd9bbdeebd", b.getHash());
		assertEquals("92959a96fd69146c5fe7cbde6e5720f2", b.getMeta());
		assertEquals("dom javascript webdev", b.getTagString());
		assertEquals(1133155569000l, b.getTime());
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testMultipleBookmarkParsing() throws ParseException{

		InputStream is = new ByteArrayInputStream(multipleBookmarkTest.getBytes());
		
		SaxBookmarkParser parser = new SaxBookmarkParser(is);
		
		ArrayList<Bookmark> r = parser.parse();
			
		assertEquals(2, r.size());
		
		Bookmark b1 = r.get(0);
		assertEquals("http://f-droid.org/wiki/page/com.pindroid", b1.getUrl());
		assertEquals("com.pindroid - F-Droid", b1.getDescription());
		assertEquals("", b1.getNotes());
		assertEquals("fafff755581363fa720dc8894212e522", b1.getHash());
		assertEquals("f86784d71f229e3b8d061a09b5d60c6f", b1.getMeta());
		assertEquals("pinboard pindroid", b1.getTagString());
		assertEquals(1359667836000l, b1.getTime());
		
		Bookmark b2 = r.get(1);
		assertEquals("http://cybernetnews.com/pinboard-android/", b2.getUrl());
		assertEquals("PinDroid: Pinboard for Android", b2.getDescription());
		assertEquals("", b2.getNotes());
		assertEquals("a84f543a510d8218f16824cf059d33a8", b2.getHash());
		assertEquals("e3f30dc2166dbf73cf1636095fd8bcb6", b2.getMeta());
		assertEquals("pindroid pinboard", b2.getTagString());
		assertEquals(1359667770000l, b2.getTime());
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}