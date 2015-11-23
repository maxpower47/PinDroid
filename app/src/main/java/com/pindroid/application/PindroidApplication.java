package com.pindroid.application;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import com.pindroid.Constants;
import com.pindroid.event.AuthenticationEvent;
import com.pindroid.util.AccountHelper;

import de.greenrobot.event.EventBus;

public class PindroidApplication extends Application {

	@Override
	public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
	}

	public void onEvent(AuthenticationEvent authenticationEvent) {
		final AccountManager am = AccountManager.get(this);

		Account account = AccountHelper.getAccount(authenticationEvent.getAccount(), this);

		am.invalidateAuthToken(Constants.AUTHTOKEN_TYPE, authenticationEvent.getAuthToken());

		try {
			am.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
