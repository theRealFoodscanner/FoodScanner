package org.foodscanner.foodscanner.menu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import org.foodscanner.foodscanner.Constants;
import org.foodscanner.foodscanner.R;
import org.foodscanner.foodscanner.helpers.SharedPreferencesHelper;

/**
 * Settings Activity
 */

public class SettingsActivity extends MenuActivity {

    private SharedPreferencesHelper mSharedPrefs;

    // Preferences
    private SwitchCompat mDebugMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mSharedPrefs = new SharedPreferencesHelper(this);
        initializeViews();
        setDefaults();
        preferenceChangeListener();
    }

    private void initializeViews() {
        mDebugMode = findViewById(R.id.settings_enable_debug);
    }

    private void setDefaults() {
        mDebugMode.setChecked(mSharedPrefs.getBool(Constants.SHARED_KEY_DEBUG_MODE, false));
    }

    private void preferenceChangeListener() {
        mDebugMode.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b) -> {
            mSharedPrefs.putBool(Constants.SHARED_KEY_DEBUG_MODE, b);
            Constants.DEBUG_MODE = b;
        });
    }

}
