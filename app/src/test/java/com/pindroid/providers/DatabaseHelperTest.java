package com.pindroid.providers;

import com.pindroid.BuildConfig;
import com.pindroid.providers.BookmarkContentProvider.DatabaseHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants=BuildConfig.class, sdk=23)
public class DatabaseHelperTest {

    private DatabaseHelper helper;

    @Before
    public void beforeEachTest() {
        helper = new DatabaseHelper(RuntimeEnvironment.application);
    }

    @Test
    public void helper_createsDatabase() {
        helper.getWritableDatabase(); // at least should not crash
    }
}