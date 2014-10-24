package com.pindroid.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.pindroid.Constants;

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
}
