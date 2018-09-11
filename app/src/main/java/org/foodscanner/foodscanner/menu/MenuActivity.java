package org.foodscanner.foodscanner.menu;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public abstract class MenuActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
