package de.ytvwld.andbacktothe;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class de.ytvwld.andbacktothe.mainTest \
 * de.ytvwld.andbacktothe.tests/android.test.InstrumentationTestRunner
 */
public class mainTest extends ActivityInstrumentationTestCase2<main> {

    public mainTest() {
        super("de.ytvwld.andbacktothe", main.class);
    }

}
