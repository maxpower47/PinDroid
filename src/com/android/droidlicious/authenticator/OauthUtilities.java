package com.android.droidlicious.authenticator;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Random;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.methods.HttpGet;

import com.android.droidlicious.util.Base64;
import android.util.Log;

import com.android.droidlicious.Constants;

public class OauthUtilities {
	public static HttpGet signRequest(HttpGet request, TreeMap<String, String> params, String authtoken,
			String tokenSecret){
		
        Random r = new Random();
        String nonce = Long.toString(Math.abs(r.nextLong()), 36);
        
        Date d = new Date();
        String timestamp = Long.toString(d.getTime() / 1000);
        
        params.put(Constants.OAUTH_COMSUMER_KEY_PROPERTY, Constants.OAUTH_CONSUMER_KEY);
        params.put(Constants.OAUTH_NONCE_PROPERTY, nonce);
        params.put(Constants.OAUTH_SIGNATURE_METHOD_PROPERTY, Constants.OAUTH_SIGNATURE_METHOD_HMAC);
        params.put(Constants.OAUTH_TIMESTAMP_PROPERTY, timestamp);
        params.put(Constants.OAUTH_TOKEN_PROPERTY, authtoken);
        params.put(Constants.OAUTH_VERSION_PROPERTY, Constants.OAUTH_VERSION);
        
        URI u = request.getURI();
        String url = u.getScheme() + "://" + u.getAuthority() + u.getPath();

		StringBuilder sb = new StringBuilder();
		sb.append("GET");
		sb.append("&" + URLEncoder.encode(url));

		for(String key : params.keySet()){
			if(params.firstKey() == key){
				sb.append("&");
			} else {
				sb.append("%26");
			}
			sb.append(URLEncoder.encode(key) + "%3D");
			sb.append(URLEncoder.encode(params.get(key)));
		}
		
		Log.d("base string", sb.toString());
		
		String keystring = Constants.OAUTH_SHARED_SECRET + "&" + tokenSecret;
		SecretKeySpec sha1key = new SecretKeySpec(keystring.getBytes(), "HmacSHA1");
		String signature = null;
		
		try{
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(sha1key);
			
			byte[] sigBytes = mac.doFinal(sb.toString().getBytes());
			//signature = Base64.encodeToString(sigBytes, Base64.NO_WRAP);
			signature = Base64.encodeBytes(sigBytes);
		}
		catch(Exception e){
			Log.e("Oauth Sign Request", "Hash Error");
		}
		
		StringBuilder authHeader = new StringBuilder();
		authHeader.append("OAuth realm=\"yahooapis.com\"");
		authHeader.append("," + Constants.OAUTH_COMSUMER_KEY_PROPERTY + "=");
		authHeader.append("\"" + Constants.OAUTH_CONSUMER_KEY + "\"");
		authHeader.append("," + Constants.OAUTH_NONCE_PROPERTY + "=");
		authHeader.append("\"" + nonce + "\"");
		authHeader.append("," + Constants.OAUTH_SIGNATURE_PROPERTY + "=");
		authHeader.append("\"" + signature + "\"");
		authHeader.append("," + Constants.OAUTH_SIGNATURE_METHOD_PROPERTY + "=");
		authHeader.append("\"" + Constants.OAUTH_SIGNATURE_METHOD_HMAC + "\"");
		authHeader.append("," + Constants.OAUTH_TIMESTAMP_PROPERTY + "=");
		authHeader.append("\"" + timestamp + "\"");
		authHeader.append("," + Constants.OAUTH_TOKEN_PROPERTY + "=");
		authHeader.append("\"" + authtoken + "\"");
		authHeader.append("," + Constants.OAUTH_VERSION_PROPERTY + "=");
		authHeader.append("\"" + Constants.OAUTH_VERSION + "\"");
		
		request.setHeader("Authorization", authHeader.toString());
			
		return request;
	}
}
