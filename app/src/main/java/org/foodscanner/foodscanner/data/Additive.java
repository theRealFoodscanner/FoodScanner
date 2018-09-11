package org.foodscanner.foodscanner.data;

import android.util.Log;

/**
 * Data structure for storing additives.
 */
public class Additive {

    //TODO: Rename this
    public static final int VALUE_LARGER_THAN_ENUMBERS = 3000;
    public static final String TAG = "FoodScanner-Additive";

    private int mId;
    private String mName;
    private String mENumber;
    private String mDescription;
    private DangerLevel mDangerLevel;

    /**
     * Creates instance of class Additive with the specified values.
     *
     * @param name Additive name
     * @param eNumber Additive's E-number
     * @param description Additive's description
     * @param dangerLevel Additive's danger level
     */
    public Additive(String name, String eNumber, String description, int dangerLevel) {
        mId = parseAdditiveIdFromENumber(eNumber);
        mName = name;
        mENumber = eNumber;
        mDescription = description;
        mDangerLevel = parseDangerLevelFromInt(dangerLevel);
    }

    public static int parseAdditiveIdFromENumber(String eNumber) {
        String number = eNumber.substring(1);   // Remove leading E
        int calculatedNumber = -1;
        if (!Character.isDigit(number.charAt(number.length()-1))) {
            calculatedNumber = Integer.parseInt(number.substring(0, number.length()-1));
            switch (number.charAt(number.length()-1)) {
                case 'a': calculatedNumber += VALUE_LARGER_THAN_ENUMBERS;
                    break;
                case 'b': calculatedNumber += VALUE_LARGER_THAN_ENUMBERS+1;
                    break;
                case 'c': calculatedNumber += VALUE_LARGER_THAN_ENUMBERS+2;
                    break;
                case 'd': calculatedNumber += VALUE_LARGER_THAN_ENUMBERS+3;
                    break;
                case 'e': calculatedNumber += VALUE_LARGER_THAN_ENUMBERS+4;
                    break;
                default:
                    calculatedNumber += VALUE_LARGER_THAN_ENUMBERS+5;
                    Log.e(TAG, "Unexpected E-Number format: " + eNumber);
            }
        } else {
            calculatedNumber = Integer.parseInt(number);
        }
        return calculatedNumber;
    }

    /* Getters and setters */

    public int getId() {
        return mId;
    }

    public void setENumber(String eNumber) {
        mENumber = eNumber;
    }

    public String getENumber() {
        return mENumber;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDangerLevel(DangerLevel dangerLevel) {
        mDangerLevel = dangerLevel;
    }

    public DangerLevel getDangerLevel() {
        return mDangerLevel;
    }

    public String getName() {
        return mName;
    }

    private DangerLevel parseDangerLevelFromInt(int dangerLevel) {
        switch (dangerLevel) {
            case 0:
                return DangerLevel.SAFE;
            case 1:
                return DangerLevel.DANGEROUS;
            case 2:
                return DangerLevel.EXTREMELY_DANGEROUS;
            case 3:
                return DangerLevel.SUSPICIOUS;
            case 4:
                return DangerLevel.UNKNOWN;
        }
        return DangerLevel.UNDEFINED;
    }
}
