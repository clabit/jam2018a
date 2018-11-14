package com.novato.jam.common;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by birdgang on 2016. 4. 11..
 */
public class AnimUtils {
    public static final String TAG = "AnimUtils";

    private static final int ANIMATION_DURATION = 250;
    public static final int DURATION = ANIMATION_DURATION; // + 50;

    public static void transTopIn(View view, boolean overshoot, long duration) {
        view.setVisibility(View.VISIBLE);
        translate(view, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, duration, overshoot);
    }

    public static void transTopIn(View view, boolean overshoot) {
        view.setVisibility(View.VISIBLE);
        translate(view, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, ANIMATION_DURATION, overshoot);
    }

    public static void transTopOut(View view, boolean overshoot) {
        view.setVisibility(View.GONE);
        translate(view, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, ANIMATION_DURATION, overshoot);
    }

    public static void transBottomIn(View view, boolean overshoot) {
        view.setVisibility(View.VISIBLE);
        translate(view, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, ANIMATION_DURATION, overshoot);
    }

    public static void transBottomIn(View view, boolean overshoot, long duration) {
        if (view == null) {
            return;
        }

        view.setVisibility(View.VISIBLE);
        translate(view, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, duration, overshoot);
    }

    public static void transBottomOut(View view, boolean overshoot, long duration) {
        if (view == null) {
            return;
        }

        view.setVisibility(View.GONE);
        translate(view, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, duration, overshoot);
    }

    public static void transBottomOut(View view, boolean overshoot) {
        view.setVisibility(View.GONE);
        translate(view, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, ANIMATION_DURATION, overshoot);
    }

    public static void transLeftIn(View view, boolean overshoot) {
        view.setVisibility(View.VISIBLE);
        AnimUtils.translate(view, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, ANIMATION_DURATION, overshoot);
    }

    public static void transLeftOut(View view, boolean overshoot) {
        view.setVisibility(View.GONE);
        AnimUtils.translate(view, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, ANIMATION_DURATION, overshoot);
    }

    public static void transLeftOut(View view, boolean overshoot, float startFade, float finishFade) {
        view.setVisibility(View.GONE);
        AnimUtils.translate(view, -0.7f, -1.0f, 0.0f, 0.0f, startFade, finishFade, ANIMATION_DURATION, overshoot);
    }

    public static void transRightIn(View view, boolean overshoot) {
        view.setVisibility(View.VISIBLE);
        AnimUtils.translate(view, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, ANIMATION_DURATION, overshoot);
    }

    public static void transRightOut(View view, boolean overshoot) {
        view.setVisibility(View.GONE);
        AnimUtils.translate(view, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, ANIMATION_DURATION, overshoot);
    }

    public static void translate(View view, float fromX, float toX, float fromY, float toY,
                                 float fromAlpha, float toAlpha, long duration, boolean overshoot) {
        if (view == null) {
            return;
        }

        final AnimationSet animSet = new AnimationSet(true);
        if (overshoot) {
            animSet.setInterpolator(AnimationUtils.loadInterpolator(view.getContext(),
                    android.R.anim.overshoot_interpolator));
        } else {
            animSet.setInterpolator(AnimationUtils.loadInterpolator(view.getContext(),
                    android.R.anim.decelerate_interpolator));
        }
        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        Animation anim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, fromX,
                Animation.RELATIVE_TO_SELF, toX,
                Animation.RELATIVE_TO_SELF, fromY,
                Animation.RELATIVE_TO_SELF, toY);
        anim.setDuration(duration);
        animSet.addAnimation(anim);

		/*if (overshoot) {
			anim = new AlphaAnimation(fromAlpha, toAlpha);
			anim.setDuration(duration);
			animSet.addAnimation(anim);
		}*/

        view.startAnimation(animSet);
    }

    public static void scaleIn(View view) {
        scaleIn(view, ANIMATION_DURATION);
    }

    public static void scaleOut(View view) {
        scaleOut(view, ANIMATION_DURATION);
    }

    public static void scaleIn(View view, int duration) {
        view.setVisibility(View.VISIBLE);
        AnimUtils.scale(view, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, android.R.anim.overshoot_interpolator, duration);
    }

    public static void scaleOut(View view, int ANIMATION_DURATION) {
        view.setVisibility(View.GONE);
        AnimUtils.scale(view, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, android.R.anim.overshoot_interpolator, ANIMATION_DURATION);
    }

    public static void scaleIn(View view, int interpolator, int duration) {
        view.setVisibility(View.VISIBLE);
        AnimUtils.scale(view, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, interpolator, duration);
    }

    public static void scaleOut(View view, int interpolator, int duration) {
        view.setVisibility(View.GONE);
        AnimUtils.scale(view, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, interpolator, ANIMATION_DURATION);
    }

    public static void scale(View view, float fromX, float toX, float fromY, float toY,
                             float fromAlpha, float toAlpha, int interpolator, long duration) {
        final AnimationSet animSet = new AnimationSet(true);
        animSet.setFillAfter(true);
        if (interpolator > 0) {
            animSet.setInterpolator(AnimationUtils.loadInterpolator(view.getContext(),
                    interpolator));
        }

        Animation anim = new ScaleAnimation(
                fromX, toX,
                fromY, toY,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(duration);
        animSet.addAnimation(anim);

        anim = new AlphaAnimation(fromAlpha, toAlpha);
        anim.setDuration(duration);
        animSet.addAnimation(anim);

        view.startAnimation(animSet);
    }

    public static void clickX(View view) {
        //click(view, 1.0f, 0.7f, 1.0f, 1.0f);
    }

    public static void clickY(View view) {
        //click(view, 1.0f, 1.0f, 1.0f, 0.7f);
    }

    public static void click(View view) {
        //click(view, 1.0f, 0.95f, 1.0f, 0.95f);
    }

    public static void click(View view, Runnable runnable) {
        //click(view, 1.0f, 0.95f, 1.0f, 0.95f);
        view.post(runnable);
    }


    public static void fadeIn(View view) {
        fadeIn(view, ANIMATION_DURATION);
    }

    public static void fadeOut(View view) {
        fadeOut(view, ANIMATION_DURATION);
    }

    public static void fadeIn(View view, int duration) {
        if (view == null) {
            return;
        }

        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            AnimUtils.fade(view, 0.0f, 1.0f, duration);
        }
    }

    public static void fadeOut(View view, int duration) {
        if (view == null) {
            return;
        }

        if (view.getVisibility() != View.GONE) {
            view.setVisibility(View.GONE);
            AnimUtils.fade(view, 1.0f, 0.0f, duration);
        }
    }

    public static void forceFadeIn(View view) {
        view.setVisibility(View.GONE);
        fadeIn(view, ANIMATION_DURATION);
    }

    public static void fade(View view, float fromAlpha, float toAlpha, long duration) {
        if (view == null) {
            return;
        }

        final Animation anim = new AlphaAnimation(fromAlpha, toAlpha);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }
}
