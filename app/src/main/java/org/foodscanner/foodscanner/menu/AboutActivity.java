package org.foodscanner.foodscanner.menu;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.foodscanner.foodscanner.R;

/**
 * Activity to display information about the application / project.
 */

public class AboutActivity extends MenuActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
