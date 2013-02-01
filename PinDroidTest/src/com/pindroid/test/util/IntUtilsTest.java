package com.pindroid.test.util;

import com.pindroid.util.IntUtils;

import android.test.AndroidTestCase;

public class IntUtilsTest extends AndroidTestCase  {

	public IntUtilsTest(){
		super();
	}
	
	public void testIntParsing(){
		assertEquals(1, IntUtils.parseUInt("1"));
		assertEquals(1, IntUtils.parseUInt("01"));
		assertEquals(2165, IntUtils.parseUInt("2165"));
	}
}
