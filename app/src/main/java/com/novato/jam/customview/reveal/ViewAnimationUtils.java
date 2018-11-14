package com.novato.jam.customview.reveal;

/**
 * Created by poshaly on 2017. 1. 10..
 */

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;


public final class ViewAnimationUtils {
    private final static boolean DEBUG = true;

    /**
     * Returns an Animator which can animate a clipping circle.
     * <p>
     * Any shadow cast by the View will respect the circular clip from this animator.
     * <p>
     * Only a single non-rectangular clip can be applied on a View at any time.
     * Views clipped by a circular reveal animation take priority over
     * {@link View#setClipToOutline(boolean) View Outline clipping}.
     * <p>
     * Note that the animation returned here is a one-shot animation. It cannot
     * be re-used, and once started it cannot be paused or resumed.
     *
     * @param view The View will be clipped to the clip circle.
     * @param centerX The x coordinate of the center of the clip circle.
     * @param centerY The y coordinate of the center of the clip circle.
     * @param startRadius The starting radius of the clip circle.
     * @param endRadius The ending radius of the clip circle.
     */


    public static void show(View ClicView, final View revealView, long dur, Animator.AnimatorListener mListener){
        //dur = 350


        revealView.setVisibility(View.VISIBLE);
        // get the center for the clipping circle
//        int cx = (ClicView.getLeft() + ClicView.getRight()) / 2;
//        int cy = (ClicView.getTop() + ClicView.getBottom()) / 2;

        int [] location = new int[2];
        ClicView.getLocationOnScreen(location);

        int cx = (location[0] + (ClicView.getWidth() / 2));
        int cy = location[1];// + (GUIUtils.getStatusBarHeight(this) / 2);


        // get the final radius for the clipping circle
        int dx = Math.max(cx, revealView.getWidth() - cx);
        int dy = Math.max(cy, revealView.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        // Android native animator
        Animator animator =
                ViewAnimationUtils.createCircularReveal(revealView, cx, cy, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(dur);
        if(mListener!=null)animator.addListener(mListener);

        animator.start();
    }

    public static void hide(final View ClicView, final View revealView, final long dur, final Animator.AnimatorListener mListener){

        // get the center for the clipping circle
//        int cx = (ClicView.getLeft() + ClicView.getRight()) / 2;
//        int cy = (ClicView.getTop() + ClicView.getBottom()) / 2;


        revealView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean isFirst = false;
            @Override
            public void onGlobalLayout() {
                if(isFirst)
                {
                    return;
                }
                isFirst = true;

                int [] location = new int[2];
                ClicView.getLocationOnScreen(location);

                int cx = (location[0] + (ClicView.getWidth() / 2));
                int cy = location[1] + (ClicView.getHeight() / 2);// + (GUIUtils.getStatusBarHeight(this) / 2);

                // get the final radius for the clipping circle
                int dx = Math.max(cx, revealView.getWidth() - cx);
                int dy = Math.max(cy, revealView.getHeight() - cy);
                float finalRadius = (float) Math.hypot(dx, dy);
                Log.e("mun",""+cx +"/"+cy);

                if(finalRadius <= 0){
                    finalRadius = 1999;
                }
                // Android native animator
                Animator animator = ViewAnimationUtils.createCircularReveal(revealView, cx, cy, finalRadius, 0);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(dur);
                if(mListener!=null)animator.addListener(mListener);
                else{
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            revealView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }
                animator.start();
            }
        });
        revealView.setVisibility(View.VISIBLE);
        revealView.requestLayout();

    }



    public static Animator createCircularReveal(View view, int centerX, int centerY,
                                                float startRadius, float endRadius) {

        return createCircularReveal(view, centerX, centerY, startRadius, endRadius,
                View.LAYER_TYPE_SOFTWARE);
    }

    /**
     * Returns an Animator which can animate a clipping circle.
     * <p>
     * Any shadow cast by the View will respect the circular clip from this animator.
     * <p>
     * Only a single non-rectangular clip can be applied on a View at any time.
     * Views clipped by a circular reveal animation take priority over
     * {@link View#setClipToOutline(boolean) View Outline clipping}.
     * <p>
     * Note that the animation returned here is a one-shot animation. It cannot
     * be re-used, and once started it cannot be paused or resumed.
     *
     * @param view The View will be clipped to the clip circle.
     * @param centerX The x coordinate of the center of the clip circle.
     * @param centerY The y coordinate of the center of the clip circle.
     * @param startRadius The starting radius of the clip circle.
     * @param endRadius The ending radius of the clip circle.
     * @param layerType View layer type {@link View#LAYER_TYPE_HARDWARE} or {@link
     * View#LAYER_TYPE_SOFTWARE}
     */
    public static Animator createCircularReveal(View view, int centerX, int centerY,
                                                float startRadius, float endRadius, int layerType) {

//        if (!(view.getParent() instanceof RevealViewGroup)) {
//            throw new IllegalArgumentException("Parent must be instance of RevealViewGroup");
//        }

//        RevealViewGroup viewGroup = (RevealViewGroup) view.getParent();
        ViewRevealManager rm = new ViewRevealManager();//viewGroup.getViewRevealManager();

        if (!rm.hasCustomerRevealAnimator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return android.view.ViewAnimationUtils.createCircularReveal(view, centerX, centerY,
                        startRadius, endRadius);
            }
        }

        ViewRevealManager.RevealValues viewData = new ViewRevealManager.RevealValues(view, centerX, centerY, startRadius, endRadius);
        ObjectAnimator animator = rm.createAnimator(viewData);

        if (layerType != view.getLayerType()) {
            animator.addListener(new ViewRevealManager.ChangeViewLayerTypeAdapter(viewData, layerType));
        }
        return animator;
    }
}
