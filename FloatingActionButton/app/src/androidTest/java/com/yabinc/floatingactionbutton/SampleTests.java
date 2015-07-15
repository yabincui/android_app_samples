package com.yabinc.floatingactionbutton;

import android.test.ActivityInstrumentationTestCase2;

import com.yabinc.logger.LogFragment;

/**
 * Created by yabinc on 7/11/15.
 */
public class SampleTests extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mTestActivity;
    private LogFragment mTestFragment;

    public SampleTests() { super(MainActivity.class); }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTestActivity = getActivity();
        mTestFragment = (LogFragment) mTestActivity.getFragmentManager().findFragmentById(R.id.log_fragment);
    }

    public void testPreconditions() {
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mTestFragment is null", mTestFragment);
    }
}
