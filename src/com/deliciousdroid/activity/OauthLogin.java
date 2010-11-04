/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * DeliciousDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeliciousDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.activity;

import com.deliciousdroid.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;

public class OauthLogin extends Activity{
	
	WebView mWebView;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.oauth_login);

	    mWebView = (WebView) findViewById(R.id.oauth_webview);   
	    
	    mWebView.setWebViewClient(new WebViewClient() {      
		    @Override
		    public void onPageStarted(WebView view, String url, Bitmap favicon){
		    	if(url.startsWith("http://deliciousdroid")){
		    		int start = url.indexOf("oauth_verifier=") + 1;
		    		String verification = url.substring(start + 14);
		    		
	            	Intent resultIntent = new Intent();
	            	resultIntent.putExtra("oauth_verifier", verification);
	            	setResult(Activity.RESULT_OK, resultIntent);
	            	finish();
		    	}
		    }
	    });  
  
	    mWebView.getSettings().setJavaScriptEnabled(true);
	    mWebView.getSettings().setUseWideViewPort(true);
	    mWebView.loadUrl(getIntent().getExtras().getString("oauth_url"));
	}
}