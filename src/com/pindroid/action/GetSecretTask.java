package com.pindroid.action;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.pindroid.Constants;
import com.pindroid.client.PinboardApi;

public class GetSecretTask extends AsyncTask<TaskArgs, Integer, String>{
	private Context context;
	private Account account;
	private String token;
	
	@Override
	protected String doInBackground(TaskArgs... args) {
		context = args[0].getContext();
		account = args[0].getAccount();
			

		try {
			token = PinboardApi.getSecretToken(account, context);
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return token;
	}
	
	@Override
	protected void onPostExecute(String result) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
    	editor.putString(Constants.PREFS_SECRET_TOKEN, result);
    	editor.commit();
	}
}