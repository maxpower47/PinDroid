package com.pindroid.util;

import android.util.Base64;

public class NetworkUtil {
    public static String encodeCredentialsForBasicAuthorization(String username, String password) {
        final String userAndPassword = username + ":" + password;
        return "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
    }
}
