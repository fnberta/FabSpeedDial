package ch.berta.fabio.fabspeeddial;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import java.util.List;

/**
 * Provides an implementation of the FAB quick dial pattern as described by Google in its Material
 * Design Guidelines. Uses standard {@link FloatingActionButton} from the design support lib to
 * draw the speed dial options.
 */
@CoordinatorLayout.DefaultBehavior(FabMenu.Behavior.class)
public class FabMenu extends ViewGroup {

    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    static final long FAB_FADE_ANIMATION_DURATION = 200l;
    private static final long ROTATE_ANIMATION_DURATION = 300l;
    private static final String STATE_SUPER = "state_super";
    private static final String STATE_MENU_IS_OPENED = "state_menu_is_opened";
    private static final String LOG_TAG = FabMenu.class.getSimpleName();
    private final Rect mShadowPadding = new Rect();
    private ObjectAnimator mOpenAnimator;
    private ObjectAnimator mCloseAnimator;
    private int mButtonsCount;
    private int mMaxButtonWidth;
    private boolean mMenuIsOpened;
    private Drawable mMenuFabIcon;
    private FloatingActionButton mMenuFab;
    private int mButtonSpacing;
    private int mLabelsMargin;
    private CharSequence[] mLabelNames;
    private RotateDrawable mRotateDrawable;
    private int mContentPadding;

    public FabMenu(Context context) {
        this(context, null);
    }

    public FabMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FabMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (Utils.isRunningLollipopAndHigher()) {
            final int normalSpace = getResources().getDimensionPixelSize(R.dimen.normal_space);
            mLabelsMargin = normalSpace;
            mButtonSpacing = normalSpace;
        } else {
            int maxContentSize = (int) getResources().getDimension(R.dimen.fab_content_size);
            mContentPadding = (getSizeDimension() - maxContentSize) / 2;
            // shadow drawable will take up a lot of space, account for it
            mLabelsMargin = Utils.dpToPx(context, 0f);
            mButtonSpacing = Utils.dpToPx(context, -24f);
        }

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FabMenu, defStyleAttr, 0);
        try {
            mMenuFabIcon = attr.getDrawable(R.styleable.FabMenu_menu_icon);
            mLabelNames = attr.getTextArray(R.styleable.FabMenu_labels);
        } finally {
            attr.recycle();
        }

        createMenuFab();
        setRotateAnimators();
    }

    /**
     * Returns the size of the fab menu.
     * <p/>
     * TODO: support mini size as well.
     *
     * @return the size of the fab
     */
    private int getSizeDimension() {
        return getResources().getDimensionPixelSize(R.dimen.fab_size_normal);
    }

    private void createMenuFab() {
        mMenuFab = new FloatingActionButton(getContext());
        mRotateDrawable = (RotateDrawable) ContextCompat.getDrawable(getContext(),
                R.drawable.rotate);
        if (mMenuFabIcon != null && Utils.isRunningLollipopAndHigher()) {
            setRotateDrawable(mRotateDrawable);
        }

        mMenuFab.setImageDrawable(mRotateDrawable);
        mMenuFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        addView(mMenuFab, generateDefaultLayoutParams());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setRotateDrawable(RotateDrawable rotateDrawable) {
        rotateDrawable.setDrawable(mMenuFabIcon);
    }

    private void setRotateAnimators() {
        RotateDrawable rotateDrawable = (RotateDrawable) mMenuFab.getDrawable();

        mOpenAnimator = getRotateAnimator(rotateDrawable, true);
        mCloseAnimator = getRotateAnimator(rotateDrawable, false);
    }

    private ObjectAnimator getRotateAnimator(Drawable target, boolean open) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(target, "level", open ? 1000 : 0);
        objectAnimator.setDuration(ROTATE_ANIMATION_DURATION);
        objectAnimator.setInterpolator(new OvershootInterpolator());

        return objectAnimator;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mButtonsCount = getChildCount();
        if (mLabelNames != null) {
            createLabels();
        }
    }

    private void createLabels() {
        int labelTitleCount = 0;
        for (int i = 0; i < mButtonsCount; i++) {
            final FloatingActionButton fab = (FloatingActionButton) getChildAt(i);
            if (fab == mMenuFab) {
                continue;
            }

            CharSequence text;
            try {
                text = mLabelNames[labelTitleCount];
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }

            final Label label = new Label(getContext());
            if (Utils.isRunningLollipopAndHigher()) {
                setLabelElevation(label, fab);
            }
            label.setText(text);
            labelTitleCount++;

            addView(label);
            fab.setTag(R.id.fab_label, label);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setLabelElevation(Label label, FloatingActionButton fab) {
        label.setElevation(fab.getElevation());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int height = 0;
        int width;
        mMaxButtonWidth = 0;
        int maxLabelWidth = 0;

        for (int i = 0; i < mButtonsCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
            height += child.getMeasuredHeight();

            Label label = (Label) child.getTag(R.id.fab_label);
            if (label != null) {
                maxLabelWidth = Math.max(maxLabelWidth, label.getMeasuredWidth());
            }
        }

        width = mMaxButtonWidth + (maxLabelWidth > 0 ? maxLabelWidth + mLabelsMargin : 0) +
                getPaddingLeft() + getPaddingRight();
        height += mButtonSpacing * (mButtonsCount - 1) + getPaddingTop() + getPaddingBottom();

        if (getLayoutParams().width == LayoutParams.MATCH_PARENT) {
            width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        }

        if (getLayoutParams().height == LayoutParams.MATCH_PARENT) {
            height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int buttonsHorizontalCenter = r - l - mMaxButtonWidth / 2 - getPaddingRight();
        int menuButtonTop = b - t - mMenuFab.getMeasuredHeight() - getPaddingBottom();
        int menuButtonLeft = buttonsHorizontalCenter - mMenuFab.getMeasuredWidth() / 2;

        // layout menu fab
        mMenuFab.layout(menuButtonLeft, menuButtonTop, menuButtonLeft + mMenuFab.getMeasuredWidth(),
                menuButtonTop + mMenuFab.getMeasuredHeight());

        if (!Utils.isRunningLollipopAndHigher()) {
            setFakeShadowOffset();
        }

        // layout child fabs
        int nextY = menuButtonTop - mButtonSpacing;
        for (int i = 0; i < mButtonsCount; i++) {
            FloatingActionButton fab = (FloatingActionButton) getChildAt(i);
            if (fab == mMenuFab || fab.getVisibility() == GONE) {
                continue;
            }

            int childX = buttonsHorizontalCenter - fab.getMeasuredWidth() / 2;
            int childY = nextY - fab.getMeasuredHeight();
            fab.layout(childX, childY, childX + fab.getMeasuredWidth(),
                    childY + fab.getMeasuredHeight());

            if (!mMenuIsOpened) {
                fab.setScaleX(0f);
                fab.setScaleY(0f);
                fab.setAlpha(0f);
                fab.setVisibility(View.GONE);
            }

            Label label = (Label) fab.getTag(R.id.fab_label);
            if (label != null) {
                int labelsOffset = fab.getMeasuredWidth() / 2 + mLabelsMargin;
                int labelXNearButton = buttonsHorizontalCenter - labelsOffset;

                int labelLeft = labelXNearButton - label.getMeasuredWidth();
                int labelTop = childY + (fab.getMeasuredHeight()
                        - label.getMeasuredHeight()) / 2;

                label.layout(labelLeft, labelTop, labelXNearButton, labelTop + label.getMeasuredHeight());

                if (!mMenuIsOpened) {
                    label.hide(false);
                }
            }

            nextY = childY - mButtonSpacing;
        }
    }

    private void setFakeShadowOffset() {
        final int startEnd = mMenuFab.getPaddingStart() - mContentPadding;
        final int topBottom = mMenuFab.getPaddingTop() - mContentPadding;
        mShadowPadding.set(startEnd, topBottom, startEnd, topBottom);
    }

    /**
     * Hides the menu button.
     *
     * @param animate whether to animate the transition
     */
    public void hideMenuButton(boolean animate) {
        if (animate) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_out);
            anim.setDuration(FAB_FADE_ANIMATION_DURATION);
            anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mMenuFab.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mMenuFab.startAnimation(anim);
        } else {
            mMenuFab.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the menu button.
     *
     * @param animate whether to animate the transition
     */
    public void showMenuButton(boolean animate) {
        if (animate) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_in);
            anim.setDuration(FAB_FADE_ANIMATION_DURATION);
            anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            mMenuFab.setVisibility(View.VISIBLE);
            mMenuFab.startAnimation(anim);
        } else {
            mMenuFab.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Toggles the visibility of the speed dial options.
     */
    public void toggle() {
        if (mMenuIsOpened) {
            close();
        } else {
            open();
        }
    }

    /**
     * Closes the speed dial options.
     */
    public void close() {
        if (!mMenuIsOpened) {
            return;
        }

        mCloseAnimator.start();

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child instanceof FloatingActionButton) || child == mMenuFab) {
                continue;
            }

            FloatingActionButton fab = (FloatingActionButton) child;
            fab.hide();

            Label label = (Label) fab.getTag(R.id.fab_label);
            if (label != null) {
                label.hide(true);
            }
        }

        mMenuIsOpened = false;
    }

    /**
     * Opens the speed dial options.
     */
    public void open() {
        if (mMenuIsOpened) {
            return;
        }

        mOpenAnimator.start();

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child instanceof FloatingActionButton) || child == mMenuFab) {
                continue;
            }

            final FloatingActionButton fab = (FloatingActionButton) child;
            fab.show();

            Label label = (Label) fab.getTag(R.id.fab_label);
            if (label != null) {
                label.show(true);
            }
        }

        mMenuIsOpened = true;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        bundle.putBoolean(STATE_MENU_IS_OPENED, mMenuIsOpened);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            mMenuIsOpened = bundle.getBoolean(STATE_MENU_IS_OPENED);
            if (mRotateDrawable != null) {
                mRotateDrawable.setLevel(mMenuIsOpened ? 1000 : 0);
            }
            state = bundle.getParcelable(STATE_SUPER);
        }

        super.onRestoreInstanceState(state);
    }

    /**
     * Provides a {@link CoordinatorLayout.Behavior} that mimics the standard
     * {@link FloatingActionButton} behaviour, i.e. moving out of the way for a {@link Snackbar}.
     * <p/>
     * Code copy pasted from the Google {@link FloatingActionButton} implementation and adapted
     * slightly.
     */
    public static class Behavior extends CoordinatorLayout.Behavior<FabMenu> {

        private static final boolean SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
        private float mTranslationY;

        public Behavior() {
        }

        public boolean layoutDependsOn(CoordinatorLayout parent, FabMenu child, View dependency) {
            return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
        }

        public boolean onDependentViewChanged(CoordinatorLayout parent, FabMenu child, View dependency) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                updateFabTranslationForSnackbar(parent, child, dependency);
            }

            return false;
        }

        public void onDependentViewRemoved(CoordinatorLayout parent, FabMenu child, View dependency) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                ViewCompat
                        .animate(child)
                        .translationY(0.0F)
                        .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(null);
            }
        }

        private void updateFabTranslationForSnackbar(CoordinatorLayout parent, FabMenu fab,
                                                     View snackbar) {
            if (fab.getVisibility() == VISIBLE) {
                float translationY = getFabTranslationYForSnackbar(parent, fab);
                if (translationY != mTranslationY) {
                    ViewCompat.animate(fab).cancel();
                    ViewCompat.setTranslationY(fab, translationY);
                    mTranslationY = translationY;
                }

            }
        }

        private float getFabTranslationYForSnackbar(CoordinatorLayout parent, FabMenu fab) {
            float minOffset = 0.0F;
            List dependencies = parent.getDependencies(fab);

            for (int i = 0, z = dependencies.size(); i < z; ++i) {
                View view = (View) dependencies.get(i);
                if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                    minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) -
                            (float) view.getHeight());
                }
            }

            return minOffset;
        }

        public boolean onLayoutChild(CoordinatorLayout parent, FabMenu child, int layoutDirection) {
            parent.onLayoutChild(child, layoutDirection);
            offsetIfNeeded(parent, child);
            return true;
        }

        private void offsetIfNeeded(CoordinatorLayout parent, FabMenu fab) {
            Rect padding = fab.mShadowPadding;
            if (padding != null && padding.centerX() > 0 && padding.centerY() > 0) {
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                int offsetTB = 0;
                int offsetLR = 0;
                if (fab.getRight() >= parent.getWidth() - lp.rightMargin) {
                    offsetLR = padding.right;
                } else if (fab.getLeft() <= lp.leftMargin) {
                    offsetLR = -padding.left;
                }

                if (fab.getBottom() >= parent.getBottom() - lp.bottomMargin) {
                    offsetTB = padding.bottom;
                } else if (fab.getTop() <= lp.topMargin) {
                    offsetTB = -padding.top;
                }

                fab.offsetTopAndBottom(offsetTB);
                fab.offsetLeftAndRight(offsetLR);
            }
        }
    }
}
