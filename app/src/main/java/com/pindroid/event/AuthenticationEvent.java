package com.pindroid.event;

public class AuthenticationEvent {

	private String account;
	private String authToken;

	public AuthenticationEvent(String accountAuthToken) {
		this.account = accountAuthToken.split(":")[0];
		this.authToken = accountAuthToken.split(":")[1];
	}

	public AuthenticationEvent(String account, String authToken) {
		this.account = account;
		this.authToken = authToken;
	}

	public String getAccount() {
		return account;
	}

	public String getAuthToken() {
		return authToken;
	}
}
