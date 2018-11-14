package com.novato.jam.customview.reveal;

/**
 * Created by poshaly on 2017. 1. 10..
 */

//import android.view.ViewGroup;

/**
// * Indicator for internal API that {@link ViewGroup} support
 * Circular Reveal animation
 */
public interface RevealViewGroup {

    /**
     * @return Bridge between view and circular reveal animation
     */
    ViewRevealManager getViewRevealManager();
}
