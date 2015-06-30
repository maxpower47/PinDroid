package com.pindroid.model;

import java.util.ArrayList;
import java.util.List;

public class TagSuggestions {
	private List<String> popular;
	private List<String> recommended;

	public TagSuggestions() {
		popular = new ArrayList<>();
		recommended = new ArrayList<>();
	}

	public List<String> getPopular() {
		return popular;
	}
    
	public List<String> getRecommended() {
		return recommended;
	}
}
