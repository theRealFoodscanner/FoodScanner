package org.foodscanner.foodscanner.menu;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.foodscanner.foodscanner.R;

/**
 * Activity for displaying licenses of used libraries.
 */

public class LicensesActivity extends MenuActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
    }
}
