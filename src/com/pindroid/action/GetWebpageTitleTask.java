package com.pindroid.action;

import android.os.AsyncTask;


import com.pindroid.client.NetworkUtilities;

public class GetWebpageTitleTask extends AsyncTask<String, Integer, String>{
	private String url;
	
	@Override
	protected String doInBackground(String... args) {
		
		if(args.length > 0 && args[0] != null && args[0] != "") {
    		url = args[0];
	
    		return NetworkUtilities.getWebpageTitle(url);
		} else return "";
		
	}
}
