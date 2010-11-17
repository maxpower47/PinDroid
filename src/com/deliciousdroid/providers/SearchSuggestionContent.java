package com.deliciousdroid.providers;

import java.util.Comparator;

public class SearchSuggestionContent {

	private String text1;
	private String text2;
	private int icon1;
	private int icon2;
	private String intentData;
	
	public String getText1() {
		return text1;
	}
	
	public String getText2() {
		return text2;
	}
	
	public int getIcon1() {
		return icon1;
	}
	
	public int getIcon2() {
		return icon2;
	}
	
	public String getIntentData() {
		return intentData;
	}
	
	public SearchSuggestionContent(String t1, String t2, int i1, int i2, String data) {
		text1 = t1;
		text2 = t2;
		icon1 = i1;
		icon2 = i2;
		intentData = data;
	}
	
	public static class Comparer implements Comparator<SearchSuggestionContent> {

		public int compare(SearchSuggestionContent a, SearchSuggestionContent b) {
			return a.text1.compareToIgnoreCase(b.text1);
		}
		
	}
}
