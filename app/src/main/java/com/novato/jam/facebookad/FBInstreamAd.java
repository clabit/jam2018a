package com.novato.jam.facebookad;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.InstreamVideoAdListener;
import com.facebook.ads.InstreamVideoAdView;

/**
 * Created by poshaly on 2017. 12. 5..
 */


public class FBInstreamAd {
    static private FBInstreamAd mFBInstreamAd;


    static public FBInstreamAd init(Context context){
        if(mFBInstreamAd == null){
            mFBInstreamAd = new FBInstreamAd(context);
        }

        return mFBInstreamAd;
    }


    private String TAG = "FBADs";

    Context context;
    InstreamVideoAdView adView;
    Ad mCurAd;
    ViewGroup adContainer;
    boolean isLoad = false;
    Callback mCallback;
    private long time;

    public FBInstreamAd(Context context){
        this.context = context;
        isLoad = false;
        mCallback = null;
    }

    public void setCallback(Callback c){
        mCallback = c;
    }
    synchronized public boolean setPlay(ViewGroup adContainer){
        return setPlay (adContainer, true);
    }
    synchronized public boolean setPlay(ViewGroup adContainer, boolean destroy){
        Log.e(TAG, "FBonEvent setPlay");

        boolean is = false;

        if(!isLoad || adView == null){
            return false;
        }

        if(destroy && adContainer != null && adContainer.getChildCount() > 0){
            setDestroy();
            return false;
        }

//        mCallback = null;


        this.adContainer = adContainer;



        is = true;

        this.adContainer.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.adContainer.addView(adView, p);
        adView.show();
        isLoad = false;



        return is;
    }

    public void setDestroy(){
        Log.e(TAG, "FBonEvent setDestroy");
        mCallback = null;
        try {
            if(adContainer!=null)adContainer.removeAllViews();
        }catch (Exception e){}
        try {
            if (adView != null) {
                adView.destroy();
                adView = null;
            }
        }catch (Exception e){}
        try{
            if(mCurAd!=null){
                mCurAd.destroy();
                mCurAd = null;
            }
        }catch (Exception e){}
    }


    synchronized public boolean setLoad(int w, int h){
        Log.e(TAG, "FBonEvent setLoad");

        if(time > System.currentTimeMillis() - (1000 * 30)){
            return false;
        }
        time = System.currentTimeMillis();

        try {
            if (adView != null) {
                adView.destroy();
                adView = null;
            }
        }catch (Exception e){}
        try{
            if(mCurAd!=null){
                mCurAd.destroy();
                mCurAd = null;
            }
        }catch (Exception e){}

        mCallback = null;

        isLoad = false;


//        int w = pxToDP(context, Utils.getScreenWidth(context));
//        int h = pxToDP(context, Utils.getScreenHeight(context));
        w = pxToDP(context,w);
        h = pxToDP(context,h);


        if(w < 10 || h < 10){
            w = 100;
            h = 100;
        }
        if(w > 300){
            w = w/2;
            h = h/2;
        }


        Log.e(TAG, "FBonEvent setLoad size " + w + " / " + h);


//        if(BuildConfig.DEBUG) {
//            AdSettings.clearTestDevices();
//            AdSettings.addTestDevice("0fd635a8f576155eddfbe9ee8ad767a1");//s7
//            AdSettings.addTestDevice("28e915d94eeb77f2fcc92674ecd4b749");//s6
//        }
        adView = new InstreamVideoAdView(
                context,
                "1897697773876301_1916288585350553",//YOUR_PLACEMENT_ID,
                new AdSize(w, h)
        );

        adView.setAdListener(new InstreamVideoAdListener() {
            @Override
            public void onAdLoaded(Ad ad) {
                //adView.show();
                try{
                    if(mCurAd!=null){
                        mCurAd.destroy();
                        mCurAd = null;
                    }
                }catch (Exception e){}
                mCurAd = ad;
                isLoad = true;
                Log.e(TAG, "FBonEvent onAdLoaded");

                if(mCallback!=null)
                    mCallback.onAdLoaded(ad);
            }


            @Override
            public void onAdVideoComplete(Ad ad) {
                final int w = adContainer.getMeasuredWidth();
                final int h = adContainer.getMeasuredHeight();
                try {
                    adContainer.removeAllViews();
                }catch (Exception e){}
                try{
                    if(mCurAd!=null){
                        mCurAd.destroy();
                        mCurAd = null;
                    }
                }catch (Exception e){}
                try{
                    ad.destroy();
                }catch (Exception e){}

                isLoad = false;

                Log.e(TAG, "FBonEvent onAdVideoComplete");

                if(mCallback!=null)
                    mCallback.onAdVideoComplete(ad , w, h);
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                try {
                    adContainer.removeAllViews();
                }catch (Exception e){}
                try{
                    if(mCurAd!=null){
                        mCurAd.destroy();
                        mCurAd = null;
                    }
                }catch (Exception e){}
                try{
                    ad.destroy();
                }catch (Exception e){}

                isLoad = false;

                Log.e(TAG, "FBonEvent onError "+adError.getErrorCode() +" / "+ adError.getErrorMessage());

                if(mCallback!=null)
                    mCallback.onError(ad, adError);
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        });
        adView.loadAd();

        return true;
    }

    //    public InstreamVideoAdView setInstream(Context context, final ViewGroup adContainer, final Callback mCallback){
//        final InstreamVideoAdView adView;
//        {
//
//            int w = pxToDP(context, adContainer.getMeasuredWidth());
//            int h = pxToDP(context, adContainer.getMeasuredHeight());
//            LogManager.e("munx", "FBonEvent FBInstreamAd " + w +" / "+h);
//            // Get the Ad Container
//            adView = new InstreamVideoAdView(
//                    context,
//                    "1395562270750546_1438827249757381",//YOUR_PLACEMENT_ID,
//                    new AdSize(
//                            w,
//                            h
//                    )
//            );
//            // set ad listener to handle events
//            adView.setAdListener(new InstreamVideoAdListener() {
//                @Override
//                public void onAdLoaded(Ad ad) {
//                    // we have an ad so let's show it
//                    try {
//                        adContainer.removeAllViews();
//                    }catch (Exception e){}
////
//                    try {
//                        adContainer.addView(adView);
////                        adView.show();
//                    }catch (Exception e){}
//
//                    if(mCallback!=null)
//                        mCallback.onAdLoaded(ad);
//                }
//
//
//                @Override
//                public void onAdVideoComplete(Ad ad) {
//                    try {
//                        adContainer.removeAllViews();
//                    }catch (Exception e){}
//                }
//
//                @Override
//                public void onError(Ad ad, AdError adError) {
//                    if(mCallback!=null)
//                        mCallback.onError(ad, adError);
//                }
//
//                @Override
//                public void onAdClicked(Ad ad) {
//
//                }
//
//                @Override
//                public void onLoggingImpression(Ad ad) {
//
//                }
//            });
////            adView.loadAd();
//            return adView;
//        }
//    }





    static synchronized public boolean setLoad(Context context, int w, int h, final Callback2 mCallback){
//        int w = pxToDP(context, Utils.getScreenWidth(context));
//        int h = pxToDP(context, Utils.getScreenHeight(context));

        try {
            w = pxToDP(context, w);
            h = pxToDP(context, h);


            if (w < 10 || h < 10) {
                w = 100;
                h = 100;
            }
            if (w > 300) {
                w = w / 2;
                h = h / 2;
            }

            final int ww = w;
            final int hh = h;


//        if(BuildConfig.DEBUG) {
//            AdSettings.clearTestDevices();
//            AdSettings.addTestDevice("0fd635a8f576155eddfbe9ee8ad767a1");//s7
//            AdSettings.addTestDevice("28e915d94eeb77f2fcc92674ecd4b749");//s6
//        }
            final InstreamVideoAdView adView = new InstreamVideoAdView(
                    context,
                    "1897697773876301_1925942984385113",//"1897697773876301_1916288585350553",//YOUR_PLACEMENT_ID,
                    new AdSize(w, h)
            );

            adView.setAdListener(new InstreamVideoAdListener() {
                @Override
                public void onAdLoaded(Ad ad) {
                    Log.e("fbads", "FBonEvent onAdLoaded");
                    if (mCallback != null)
                        mCallback.onAdLoaded(ad, adView);
                }


                @Override
                public void onAdVideoComplete(Ad ad) {
                    Log.e("fbads", "FBonEvent onAdVideoComplete");
                    if (mCallback != null)
                        mCallback.onAdVideoComplete(ad, ww, hh);
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.e("fbads", "FBonEvent onError "+adError.getErrorCode() +" / "+ adError.getErrorMessage());
                    try {
                        ad.destroy();
                    } catch (Exception e) {
                    }
                    if (mCallback != null)
                        mCallback.onError(ad, adError);
                }

                @Override
                public void onAdClicked(Ad ad) {
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                }
            });
            adView.loadAd();

            return true;
        }catch (Exception e){
            Log.e("fbads", "FBonEvent "+e.toString());
        }

        return false;
    }




    static private int pxToDP(Context con, int px) {
        return (int)(px / con.getResources().getDisplayMetrics().density);
    }




    public interface Callback{
        void onAdVideoComplete(Ad ad, int w, int h);
        void onAdLoaded(Ad ad);
        void onError(Ad ad, AdError adError);
    }

    public interface Callback2{
        void onAdVideoComplete(Ad ad, int w, int h);
        void onAdLoaded(Ad ad, InstreamVideoAdView adView);
        void onError(Ad ad, AdError adError);
    }
}
