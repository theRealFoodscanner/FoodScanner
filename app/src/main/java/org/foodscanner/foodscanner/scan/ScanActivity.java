package org.foodscanner.foodscanner.scan;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.foodscanner.foodscanner.Constants;
import org.foodscanner.foodscanner.R;
import org.foodscanner.foodscanner.helpers.SharedPreferencesHelper;
import org.foodscanner.foodscanner.menu.AboutActivity;
import org.foodscanner.foodscanner.menu.LicensesActivity;
import org.foodscanner.foodscanner.menu.SettingsActivity;
import org.foodscanner.foodscanner.helpers.ActivityHelper;

public class ScanActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private ScanPresenter mScanPresenter;
    private static final String SCAN_FRAGMENT_TAG = "SCAN_FRAGMENT_TAG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Constants.DEBUG_MODE = new SharedPreferencesHelper(this).getBool(Constants.SHARED_KEY_DEBUG_MODE);
        ScanFragment scanFragment = (ScanFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);

        if (scanFragment == null) {
            scanFragment = ScanFragment.newInstance();

            ActivityHelper.addFragmentToActivity(getSupportFragmentManager(), scanFragment, R.id.main_fragment_container, SCAN_FRAGMENT_TAG);
        }

        if (mScanPresenter == null) {
            mScanPresenter = new ScanPresenter(scanFragment);
        }

        requestStoragePermission();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mScanPresenter.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setScanNavigationItemSelected();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (mScanPresenter != null) {
            return(mScanPresenter);
        }
        return super.onRetainCustomNonConfigurationInstance();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        ScanFragment scanFragment = (ScanFragment) getSupportFragmentManager().findFragmentByTag(SCAN_FRAGMENT_TAG);
        if (scanFragment != null && scanFragment.getIsWordView()) {
            scanFragment.showResult();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (id) {
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
//            case R.id.nav_help:
//                startActivity(new Intent(this, HelpActivity.class));
//                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.nav_licenses:
                startActivity(new Intent(this, LicensesActivity.class));
                break;
        }

        return true;
    }

    private void setScanNavigationItemSelected() {
        // Set Scan menu item as selected whenever returning to main Activity
        NavigationView navigationView = findViewById(R.id.nav_view);

        if (navigationView == null)
            return;

        MenuItem scan = navigationView.getMenu().getItem(0);

        if (scan == null)
            return;

        scan.setChecked(true);
    }

    private void requestStoragePermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.TAG, "Missing READ_EXTERNAL_STORAGE");
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 999);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.TAG, "Missing WRITE_EXTERNAL_STORAGE");
            requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 999);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 999:
                if (grantResults.length < 1) {
                    break;
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(this);
                    adb.setTitle(R.string.permission_dialog_denied_title);
                    adb.setMessage(R.string.permission_dialog_denied_content_storage)
                        .setCancelable(false)
                        .setPositiveButton(R.string.permission_dialog_denied_settings, (DialogInterface dialog, int id) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, 1000);
                        });

                    AlertDialog alertDialog = adb.create();
                    alertDialog.show();
                }
                break;
        }
    }
}
