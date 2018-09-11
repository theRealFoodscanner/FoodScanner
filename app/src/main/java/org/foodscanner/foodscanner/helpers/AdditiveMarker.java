package org.foodscanner.foodscanner.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;

import org.foodscanner.foodscanner.R;
import org.foodscanner.foodscanner.data.DangerLevel;
import org.foodscanner.foodscanner.data.MultilineWord;
import org.foodscanner.foodscanner.data.Result;
import org.foodscanner.foodscanner.data.Word;

import java.util.List;

/**
 * Class for visually marking additives on the overlay image.
 */
public class AdditiveMarker {

    public static final String TAG = "FoodScanner-AM";
    private static final int ADDITIVE_COLOR_ALPHA_LEVEL = 128;
    private List<Word> mWordList;
    private ImageView mOverlayImageView;
    private Bitmap mOverlayBitmap;
    private Context mContext;

    public AdditiveMarker(Context context, ImageView overlayImageView, Bitmap overlayBitmap) {
        mWordList = Result.getInstance().getWordList();
        mOverlayImageView = overlayImageView;
        mOverlayBitmap = overlayBitmap;
        mContext = context;
    }

    public AdditiveMarker(Context context, ImageView overlayImageView, ImageView resultImageView) {
        mWordList = Result.getInstance().getWordList();
        mOverlayImageView = overlayImageView;
        createTransparentBitmap(resultImageView);
        mContext = context;
    }

    /**
     * Iterates over the {@code Word} list and visually marks the found additives on the overlay
     * {@code Bitmap}. When done it also displays said {@code Bitmap} in the overlay {@code
     * ImageView}.
     */
    public void markAdditives() {
        for (Word w : mWordList) {
            DangerLevel dangerLevel = DangerLevel.UNDEFINED;

            if (w.hasAdditive()) {
                // Get danger level from additive
                dangerLevel = w.getAdditive().getDangerLevel();
            }

            // Draw filled rectangle(s) of appropriate color on image
            if (w instanceof MultilineWord) {
                for (Word line : ((MultilineWord) w).getAllLines()) {
                    drawRectangleOnBitmap(dangerLevel, line.getBoundingBox().getRectangle());
                }
            } else {
                drawRectangleOnBitmap(dangerLevel, w.getBoundingBox().getRectangle());
            }
        }

        setOverlayImage();
    }

    /**
     * Creates a transparent (blank) {@code Bitmap} with the same dimensions as the result image
     * and displays it in the overlay {@code ImageView}.
     *
     * @param resultImageView {@code ImageView} containing the result image
     */
    private void createTransparentBitmap(ImageView resultImageView) {
        int width = resultImageView.getDrawable().getIntrinsicWidth();
        int height = resultImageView.getDrawable().getIntrinsicHeight();

        mOverlayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas overlayCanvas = new Canvas(mOverlayBitmap);
        overlayCanvas.drawColor(Color.TRANSPARENT);

        setOverlayImage();
    }

    /**
     * Displays the overlay {@code Bitmap} in the overlay {@code ImageView}.
     */
    private void setOverlayImage() {
        mOverlayImageView.setImageBitmap(mOverlayBitmap);
    }

    /**
     * Draws the specified rectangle, filled with a color dependant on the specified danger level.
     *
     * @param dangerLevel additive's danger level
     * @param rectangle rectangle to be drawn
     */
    private void drawRectangleOnBitmap(DangerLevel dangerLevel, Rect rectangle) {
        Canvas canvas = new Canvas(mOverlayBitmap);
        Paint paint = getPaintForDangerLevel(dangerLevel);

        canvas.drawRect(rectangle, paint);
    }

    /**
     * Sets-up a new {@code Paint} instance with the appropriate color and transparency for the
     * specified danger level. A {@code null} danger level will return a {@code Paint} instance
     * with a transparent color.
     *
     * @param dangerLevel additive's danger level
     * @return {@code Paint} instance with an appropriate color and transparency set-up
     */
    private Paint getPaintForDangerLevel(DangerLevel dangerLevel) {
        Paint dangerPaint = new Paint();

        if (dangerLevel == DangerLevel.UNDEFINED) {
            dangerPaint.setColor(Color.TRANSPARENT);
            return dangerPaint;
        }

        dangerPaint.setColor(mContext.getResources().getColor(getColorForDangerLevel(dangerLevel)));
        dangerPaint.setAlpha(ADDITIVE_COLOR_ALPHA_LEVEL);
        return dangerPaint;
    }

    public static int getColorForDangerLevel(DangerLevel dangerLevel) {
        switch (dangerLevel) {
            case SAFE:
                return R.color.colorAdditiveDangerSafe;
            case DANGEROUS:
                return R.color.colorAdditiveDangerDangerous;
            case EXTREMELY_DANGEROUS:
                return R.color.colorAdditiveDangerExtremelyDangerous;
            case SUSPICIOUS:
                return R.color.colorAdditiveDangerSuspicious;
            case UNKNOWN:
                return R.color.colorAdditiveDangerUnknown;
        }
        return android.R.color.white;
    }
}
