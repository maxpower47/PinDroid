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

import com.pindroid.client.PinboardApiResult;
import com.pindroid.xml.SaxResultParser;

import android.test.AndroidTestCase;

public class SaxResultParserTest extends AndroidTestCase  {
	
	private String doneTest = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><result code=\"done\" />";
	private String errorTest = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><result code=\"something went wrong\" />";

	public SaxResultParserTest(){
		super();
	}
	
	public void testResultDoneParsing() throws ParseException{

		InputStream is = new ByteArrayInputStream( doneTest.getBytes() );
		
		SaxResultParser parser = new SaxResultParser(is);
		
		PinboardApiResult r = parser.parse();
			
		assertEquals("done", r.getCode());
		
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testResultErrorParsing() throws ParseException{

		InputStream is = new ByteArrayInputStream( errorTest.getBytes() );
		
		SaxResultParser parser = new SaxResultParser(is);
		
		PinboardApiResult r = parser.parse();
			
		assertEquals("something went wrong", r.getCode());
		
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
