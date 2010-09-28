package com.android.droidlicious.activity;

import com.android.droidlicious.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class OauthLogin extends Activity{
	
	WebView mWebView;
	EditText mToken;
	Button mButton;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.oauth_login);
	    

	    mWebView = (WebView) findViewById(R.id.oauth_webview);
	    mToken = (EditText) findViewById(R.id.oauth_token_edit_url);
	    mButton = (Button) findViewById(R.id.oauth_ok_button);
	    
	    
	    mWebView.setWebViewClient(new WebViewClient() {  
		    @Override  
		    public boolean shouldOverrideUrlLoading(WebView view, String url)  
		    {  
				view.loadUrl(url);  
			    return true;  
		    }  
	    });  
	    
	    mButton.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View view){
	        	Log.d("blah", "got to onclick");
	            if (view == mButton) {
	            	Log.d("blah", "got to mbutton");
	            	Intent resultIntent = new Intent();
	            	resultIntent.putExtra("oauth_verifier", mToken.getText().toString());
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
