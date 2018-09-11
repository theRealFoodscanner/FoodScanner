package org.foodscanner.foodscanner.data;

/**
 * Enum for danger levels
 */
public enum DangerLevel {

    UNDEFINED(-1), SAFE(0), DANGEROUS(1), EXTREMELY_DANGEROUS(2), SUSPICIOUS(3), UNKNOWN(4);
    private int mDangerLevel;

    DangerLevel(int value) {
        mDangerLevel = value;
    }

    public int getDangerLevel() {
        return mDangerLevel;
    }
}

