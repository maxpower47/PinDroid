package com.android.droidlicious.activity;

import com.android.droidlicious.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OauthLogin extends Activity{
	
	WebView mWebView;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.oauth_login);

	    mWebView = (WebView) findViewById(R.id.oauth_webview);   
	    
	    mWebView.setWebViewClient(new WebViewClient() {  
		    @Override  
		    public boolean shouldOverrideUrlLoading(WebView view, String url)  
		    {  
		    	if(url.startsWith("dandroid:")){
		    		int start = url.indexOf("oauth_verifier=") + 1;
		    		String verification = url.substring(start + 14);
		    		
	            	Intent resultIntent = new Intent();
	            	resultIntent.putExtra("oauth_verifier", verification);
	            	setResult(Activity.RESULT_OK, resultIntent);
	            	finish();
		    	} else {
		    		view.loadUrl(url);
		    		return false;
		    	}
			    return true;  
		    }  
	    });  
  
	    mWebView.getSettings().setJavaScriptEnabled(true);
	    mWebView.getSettings().setUseWideViewPort(true);
	    mWebView.loadUrl(getIntent().getExtras().getString("oauth_url"));
	}
}