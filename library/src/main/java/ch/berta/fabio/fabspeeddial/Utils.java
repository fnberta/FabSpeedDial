package ch.berta.fabio.fabspeeddial;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by fabio on 18.08.15.
 */
public class Utils {

    private Utils() {
        // class cannot be instantiated
    }

    public static boolean isRunningLollipopAndHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }
}
