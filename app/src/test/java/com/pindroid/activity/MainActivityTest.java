package com.pindroid.activity;

import com.pindroid.PinDroidRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.util.ActivityController;

import static org.robolectric.Robolectric.buildActivity;

@RunWith(PinDroidRunner.class)
public class MainActivityTest {

    private ActivityController<Main> controller;

    @Before
    public void beforeEachTest() {
        controller = buildActivity(Main.class);
    }

    @Test
    public void activity_starts() { // just basic test checking that activity doesn't crash
        controller.create().start().resume();
    }
}