package cn.flyrise.feep.particular.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;

/**
 * @author ZYP
 * @since 2016-10-28 16:26
 */
public class AnimationUtils {

    private static final Interpolator sInterpolator = new FastOutSlowInInterpolator();
    private static final int sDuration = 500;

    public static void executeHideAnimation(final View view, int translateY) {
        if (view == null) {
            return;
        }
        ViewPropertyAnimator animator = view.animate()
                .translationY(translateY)
                .setInterpolator(sInterpolator)
                .setDuration(sDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
        animator.start();
    }

    public static void executeDisplayAnimation(final View view) {
        if (view == null) {
            return;
        }
        ViewPropertyAnimator animator = view.animate()
                .translationY(0)
                .setInterpolator(sInterpolator)
                .setDuration(sDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }
                });
        animator.start();
    }
}
