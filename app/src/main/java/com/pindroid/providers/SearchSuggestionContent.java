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

package com.pindroid.providers;

import java.util.Comparator;

public class SearchSuggestionContent {

	private String text1;
	private String text2;
	private String text2Url;
	private int icon2;
	private String intentData;
	private String intentAction;
	
	public String getText1() {
		return text1;
	}
	
	public String getText2() {
		return text2;
	}
	
	public String getText2Url() {
		return text2Url;
	}
	
	public int getIcon2() {
		return icon2;
	}
	
	public String getIntentData() {
		return intentData;
	}
	
	public String getIntentAction() {
		return intentAction;
	}
	
	public SearchSuggestionContent(String t1, String t2, int i2, String data, String action) {
		text1 = t1;
		text2 = t2;
		icon2 = i2;
		intentData = data;
		intentAction = action;
	}
	
	public SearchSuggestionContent(String t1, String t2, String t2u, int i2, String data, String action) {
		text1 = t1;
		text2 = t2;
		text2Url = t2u;
		icon2 = i2;
		intentData = data;
		intentAction = action;
	}
	
	public static class Comparer implements Comparator<SearchSuggestionContent> {

		public int compare(SearchSuggestionContent a, SearchSuggestionContent b) {
			return a.text1.compareToIgnoreCase(b.text1);
		}
	}
}