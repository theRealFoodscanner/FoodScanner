package org.foodscanner.foodscanner;

/**
 * Constants class.
 *
 */

public class Constants {
    public static final int IS_ADDITIVE_DATABASE_ENABLED = 0;   // 1 - Enabled; other - Disabled
    public static boolean DEBUG_MODE;  // DEBUG
    public static final String TAG = "FoodScanner"; // Logging

    public static final String E_NUMBER_REGEX = "^(e|E)[1-9]\\d{2,3}[(a-eA-E)]?$";
    public static final int MIN_ENUMBER_LENGTH = 4;
    public static final int MAX_ENUMBER_LENGTH = 7;

    public static final String SHARED_PREFERENCES_KEY = "foodscanner_shared_prefs";
    public static final String SHARED_KEY_DEBUG_MODE = "debug_mode";
}
