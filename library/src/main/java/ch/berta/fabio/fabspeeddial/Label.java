package ch.berta.fabio.fabspeeddial;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Created by fabio on 18.08.15.
 */
public class Label extends TextView {

    public Label(Context context) {
        super(context);

        setTextAppearance(context, R.style.TextAppearance_AppCompat_Button);
        setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_corners));
    }

    /**
     * Shows label with same animation as design support lib FAB
     */
    public void show(boolean animate) {
        if (animate) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_in);
            anim.setDuration(FabMenu.FAB_FADE_ANIMATION_DURATION);
            anim.setInterpolator(FabMenu.FAST_OUT_SLOW_IN_INTERPOLATOR);
            setVisibility(View.VISIBLE);
            startAnimation(anim);
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hides label either with same animation as design support lib or immediately
     * @param animate
     */
    public void hide(boolean animate) {
        if (animate) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_out);
            anim.setDuration(FabMenu.FAB_FADE_ANIMATION_DURATION);
            anim.setInterpolator(FabMenu.FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            startAnimation(anim);
        } else {
            setVisibility(View.GONE);
        }
    }
}
