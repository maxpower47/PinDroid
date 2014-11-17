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

import com.pindroid.client.Update;
import com.pindroid.xml.SaxUpdateParser;

import android.test.AndroidTestCase;

public class SaxUpdateParserTest extends AndroidTestCase  {
	
	private String updateTest = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><update time=\"2011-03-24T19:02:07Z\" />";

	public SaxUpdateParserTest(){
		super();
	}
	
	public void testUpdateParsing() throws ParseException{

		InputStream is = new ByteArrayInputStream(updateTest.getBytes());
		
		SaxUpdateParser parser = new SaxUpdateParser(is);
		
		Update r = parser.parse();
			
		assertEquals(1300993327000l, r.getLastUpdate());
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}