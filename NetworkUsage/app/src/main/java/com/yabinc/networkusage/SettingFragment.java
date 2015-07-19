package com.yabinc.networkusage;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by yabinc on 7/18/15.
 */
public class SettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
