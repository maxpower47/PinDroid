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
}
