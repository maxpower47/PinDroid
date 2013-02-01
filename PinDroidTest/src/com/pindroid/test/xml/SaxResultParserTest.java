package com.pindroid.test.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import com.pindroid.client.PinboardApiResult;
import com.pindroid.xml.SaxResultParser;

import android.test.AndroidTestCase;

public class SaxResultParserTest extends AndroidTestCase  {
	
	private String doneTest = "<result code=\"done\" />";
	private String errorTest = "<result code=\"something went wrong\" />";

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
