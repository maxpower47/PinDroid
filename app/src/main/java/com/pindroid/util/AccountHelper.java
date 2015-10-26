package com.pindroid.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.pindroid.Constants;
import com.pindroid.client.AuthenticationException;

import java.util.ArrayList;
import java.util.List;

public class AccountHelper {

    public static Account getAccount(String username, Context context){
    	for(Account account : AccountManager.get(context).getAccountsByType(Constants.ACCOUNT_TYPE))
    		if (account.name.equals(username))
    			return account;
    	
    	return null;		   
    }
    
    public static Account getFirstAccount(Context context){
    	if(getAccountCount(context) > 0){
    		return AccountManager.get(context).getAccountsByType(Constants.ACCOUNT_TYPE)[0];
    	} 
    	return null;
    }
    
    public static int getAccountCount(Context context){
    	return AccountManager.get(context).getAccountsByType(Constants.ACCOUNT_TYPE).length;
    }

	public static String getAuthToken(Context context, Account account) throws AuthenticationException {
		final AccountManager am = AccountManager.get(context);

		if(account == null) {
			throw new AuthenticationException();
		}

		final String username = account.name;
		String authtoken = "00000000000000000000";  // need to provide a sane default value, since a token that is too short causes a 500 error instead of 401

		try {
			String tempAuthtoken = am.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
			if(tempAuthtoken != null)
				authtoken = tempAuthtoken;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException();
		}

		return username + ":" + authtoken;
	}

    public static List<String> getAccountNames(Context context){

        List<String> accountNames = new ArrayList<>();

        for(Account account : AccountManager.get(context).getAccountsByType(Constants.ACCOUNT_TYPE)) {
            accountNames.add(account.name);
        }

        return accountNames;
    }

    public static boolean isSingleAccount(Context context) {
        return AccountManager.get(context).getAccountsByType(Constants.ACCOUNT_TYPE).length == 1;
    }
}
