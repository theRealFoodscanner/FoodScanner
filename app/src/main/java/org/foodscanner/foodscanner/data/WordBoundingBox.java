package org.foodscanner.foodscanner.data;

import android.graphics.Rect;

/**
 * Object for holding a {@code Word}'s bounding box.
 */
public class WordBoundingBox {

    private Point mTopLeftCoordinate;
    private Point mBottomRightCoordinate;

    public WordBoundingBox() {
        mTopLeftCoordinate = new Point();
        mBottomRightCoordinate = new Point();
    }

    public WordBoundingBox(Point topLeft, Point bottomRight) {
        mTopLeftCoordinate = topLeft;
        mBottomRightCoordinate = bottomRight;
    }

    /**
     * Returns the width of the bounding box.
     *
     * @return the width of the bounding box
     */
    public int getWidth() {
        return mBottomRightCoordinate.getXCoordinate() - mTopLeftCoordinate.getXCoordinate();
    }

    /**
     * Returns the height of the bounding box.
     *
     * @return the height of the bounding box
     */
    public int getHeight() {
        return mBottomRightCoordinate.getYCoordinate() - mTopLeftCoordinate.getYCoordinate();
    }

    /**
     * Returns a rectangle matching the size of the bounding box.
     *
     * @return rectangle matching the bounding box size
     */
    public Rect getRectangle() {
        return new Rect(mTopLeftCoordinate.getXCoordinate(), mTopLeftCoordinate.getYCoordinate(), mBottomRightCoordinate.getXCoordinate(), mBottomRightCoordinate.getYCoordinate());
    }

    public Point getTopLeftCoordinate() {
        return mTopLeftCoordinate;
    }

    public Point getBottomRightCoordinate() {
        return mBottomRightCoordinate;
    }

    public void setTopLeftCoordinate(Point topLeftCoordinate) {
        mTopLeftCoordinate = topLeftCoordinate;
    }

    public void setBottomRightCoordinate(Point bottomRightCoordinate) {
        mBottomRightCoordinate = bottomRightCoordinate;
    }

    public void setBoundingBox(Point topLeft, Point bottomRight) {
        mTopLeftCoordinate = topLeft;
        mBottomRightCoordinate = bottomRight;
    }

    public boolean areCoordinatesInsideBox(float x, float y) {
        // Get x coordinates
        int upperLeftX = mTopLeftCoordinate.getXCoordinate();
        int bottomRightX = mBottomRightCoordinate.getXCoordinate();

        // Check if touched x is OUTSIDE the boundaries
        if ( x < upperLeftX || x > bottomRightX ){
            return false;
        }

        // x is INSIDE the boundaries at this point

        // Get y coordinates
        int upperLeftY = mTopLeftCoordinate.getYCoordinate();
        int bottomRightY = mBottomRightCoordinate.getYCoordinate();

        // Check if touched y is INSIDE the boundaries
        return y > upperLeftY && y < bottomRightY;
    }
}