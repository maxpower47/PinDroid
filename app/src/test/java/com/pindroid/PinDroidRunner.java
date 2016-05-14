package com.pindroid;

import android.support.annotation.NonNull;

import com.pindroid.application.PindroidApplication;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Properties;

/**
 * Custom Robolectric test runner, sp we won't have to specify @Config for every test
 */
public class PinDroidRunner extends RobolectricGradleTestRunner {

    public PinDroidRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Properties getConfigProperties() {
        final Properties properties = new Properties();
        properties.setProperty("constants", BuildConfig.class.getName());
        properties.setProperty("sdk", "21");
        return properties;
    }

    @NonNull
    public static PindroidApplication app() {
        return (PindroidApplication) RuntimeEnvironment.application;
    }
}
