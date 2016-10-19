package com.pindroid.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.pindroid.BuildConfig;
import com.pindroid.Constants;
import com.pindroid.application.PindroidApplication;
import com.pindroid.authenticator.AuthenticatorActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAccountManager;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ActivityController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants=BuildConfig.class, sdk=23)
public class FragmentBaseActivityTest {

    private ActivityController<TestActivity> controller;
    private ShadowApplication shadowApp;

    @Before
    public void beforeEachTest() {
        controller = buildActivity(TestActivity.class);
        shadowApp = shadowOf(RuntimeEnvironment.application);
    }

    @Test
    public void activity_startsAuthenticationOnNoAccount() {
        controller.create().start().resume();

        final Intent intent = shadowApp.getNextStartedActivity();
        assertThat(intent.getComponent().getClassName()).isEqualTo(AuthenticatorActivity.class.getName());
    }

    @Test
    public void activity_setsSubtitleOnMultipleAccounts() {
        controller.create();
        addPinboardAccount("test");
        addPinboardAccount("test_2");

        controller.start().resume();

        final CharSequence subtitle = controller.get().getSupportActionBar().getSubtitle();
        assertThat(subtitle).isEqualTo("test_2");
    }

    private void addPinboardAccount(@NonNull String name) {
        final AccountManager am = ShadowAccountManager.get(RuntimeEnvironment.application);
        final Account account = new Account(name, Constants.ACCOUNT_TYPE);
        am.addAccountExplicitly(account, "password", null);
        ((PindroidApplication) RuntimeEnvironment.application).setUsername(name);
    }

    public static class TestActivity extends FragmentBaseActivity {

        @Override
        protected void startSearch(String query) {
            throw new IllegalStateException("Shouldn't happen in this test");
        }

        @Override
        protected void changeAccount() {
            // no-op in test
        }
    }
}