package com.yabinc.networkusage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SettingFragment settingFragment = (SettingFragment) getFragmentManager().findFragmentById(R.id.setting_fragment);
        settingFragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SettingFragment settingFragment = (SettingFragment) getFragmentManager().findFragmentById(R.id.setting_fragment);
        settingFragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Toast.makeText(this, "Shared Preference changed", Toast.LENGTH_SHORT).show();
    }
}
