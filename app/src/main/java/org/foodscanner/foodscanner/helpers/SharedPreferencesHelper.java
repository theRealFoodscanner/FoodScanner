package org.foodscanner.foodscanner.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import org.foodscanner.foodscanner.Constants;

public class SharedPreferencesHelper {

    private Context mContext;

    public SharedPreferencesHelper(Context context){
        mContext = context;
    }

    public void putBool(String key, boolean value) {
       getSharedPrefsEditor().putBoolean(formatKey(key), value).commit();
    }

    public boolean getBool(String key) {
        return getBool(key, false);
    }

    public boolean getBool(String key, boolean defaultValue) {
        return getSharedPrefs().getBoolean(formatKey(key), defaultValue);
    }

    private SharedPreferences getSharedPrefs() {
        return  mContext.getSharedPreferences(formatKey(Constants.SHARED_PREFERENCES_KEY), Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getSharedPrefsEditor() {
        return getSharedPrefs().edit();
    }

    private String formatKey(String key) {
        return String.format("%1$s.%2$s", mContext.getApplicationContext().getPackageName() , key);
    }
}
