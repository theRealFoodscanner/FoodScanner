package org.foodscanner.foodscanner.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data structure for storing pairs of coordinates.
 */
public class Point implements Parcelable {

    private int mXCoordinate;
    private int mYCoordinate;

    public Point() {
        mXCoordinate = -1;
        mYCoordinate = -1;
    }

    public Point(int x, int y) {
        mXCoordinate = x;
        mYCoordinate = y;
    }

    public int getXCoordinate() {
        return mXCoordinate;
    }

    public int getYCoordinate() {
        return mYCoordinate;
    }

    @Override
    public String toString() {
        return "X: " + mXCoordinate + " Y: " + mYCoordinate;
    }

    /* Parcelable components */

    public Point(Parcel in) {
        mXCoordinate = in.readInt();
        mYCoordinate = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mXCoordinate);
        parcel.writeInt(mYCoordinate);
    }

    public static final Parcelable.Creator<Point> CREATOR = new Parcelable.Creator<Point>() {

        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        public Point[] newArray(int size) {
            return new Point[size];
        }

    };
}
