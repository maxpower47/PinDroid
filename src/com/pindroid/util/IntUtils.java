package com.pindroid.util;

public class IntUtils {	
	public static int parseUInt(final String s)
	{
	    // Check for a sign.
	    int num  = 0;
	    final int len  = s.length();
	    final char ch  = s.charAt(0);

	    num = '0' - ch;

	    // Build the number.
	    int i = 1;
	    while ( i < len )
	        num = num * 10 + '0' - s.charAt(i++);

	    return -1 * num;
	} 
}