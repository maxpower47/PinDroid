package com.pindroid.providers;

import com.pindroid.PinDroidRunner;
import com.pindroid.providers.BookmarkContentProvider.DatabaseHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(PinDroidRunner.class)
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