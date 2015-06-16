package com.pindroid.event;

public class AccountChangedEvent {
    private String newAccount;

    public AccountChangedEvent(String newAccount) {
        this.newAccount = newAccount;
    }

    public String getNewAccount() {
        return newAccount;
    }
}
