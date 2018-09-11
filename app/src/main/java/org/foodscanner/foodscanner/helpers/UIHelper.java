package org.foodscanner.foodscanner.helpers;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

/**
 * Helper class for UI elements.
 */

public class UIHelper {

    public static Drawable disableDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        Drawable disabledDrawable = drawable.mutate();
        disabledDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        return  disabledDrawable;
    }
}
