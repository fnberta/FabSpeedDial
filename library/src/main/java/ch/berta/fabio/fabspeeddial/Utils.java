package ch.berta.fabio.fabspeeddial;

import android.content.Context;
import android.os.Build;

/**
 * Provides useful static utility methods.
 */
public class Utils {

    private Utils() {
        // class cannot be instantiated
    }

    /**
     * Returns whether the device is running Android Lollipop or higher.
     *
     * @return whether the device is running Android Lollipop or higher
     */
    public static boolean isRunningLollipopAndHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Returns the dp value in pixels, calculated for the device's display density.
     *
     * @param context the context to get the display metrics
     * @param dp      the dp value to convert to pixels
     * @return the dp value in pixels, calculated for the device's display density
     */
    public static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }
}
