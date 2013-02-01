package com.pindroid.test.util;

import java.util.Date;

import com.pindroid.util.DateParser;

import android.test.AndroidTestCase;

public class DateUtilsTest extends AndroidTestCase  {

	public DateUtilsTest(){
		super();
	}
	
	public void testDateParsing(){

		try {
			Date d = DateParser.parse("2005-11-28T05:26:09Z");
			
			assertEquals(2005, d.getYear() + 1900);
			assertEquals(11, d.getMonth() + 1);
			assertEquals(28, d.getDate());
			assertEquals(5, d.getHours() + (d.getTimezoneOffset() / 60));
			assertEquals(26, d.getMinutes());
			assertEquals(9, d.getSeconds());

		} catch (Exception e) {
		}
		
		try {
			Date d = DateParser.parse("2005-11-28T05:26:09");	
			fail("Expected date parser to fail on invalid format.");

		} catch (Exception e) {
		}
	}
	
	public void testDateTimeParsing(){

		try {
			long time = DateParser.parseTime("2005-11-28T05:26:09Z");
			
			assertEquals(1133155569000l, time);

		} catch (Exception e) {
		}
		
		try {
			Date d = DateParser.parse("2005-11-28T05:26:09");	
			fail("Expected date time parser to fail on invalid format.");

		} catch (Exception e) {
		}
	}
	
	public void testToString(){		
		Date d = new Date(1133155569000l);

		assertEquals("2005-11-28T05:26:09Z", DateParser.toString(d));
	}
}
