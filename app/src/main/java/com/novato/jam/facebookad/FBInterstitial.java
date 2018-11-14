package com.novato.jam.facebookad;

import android.content.Context;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.novato.jam.admob.AdmobInterstial;
import com.novato.jam.common.LoggerManager;

/**
 * Created by poshaly on 2018. 3. 30..
 */

public class FBInterstitial {

    private static FBInterstitial mAdinit;


    static public FBInterstitial getInstance(){
        synchronized (FBInterstitial.class) {
            if (mAdinit == null) {
                mAdinit = new FBInterstitial();
            }
        }

        return mAdinit;
    }



    public FBInterstitial(){

    }


    InterstitialAd interstitialAd;
    boolean isLoading = false;
    Callback mCallback;

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoad(Context context, Callback mCallback){
        this.mCallback = mCallback;
        try {
            if (interstitialAd == null) {
                LoggerManager.e("mun", "FBInterstial setLoad 1");
                setting(context);
            } else {
                if (interstitialAd.isAdLoaded() || isLoading()) {
                    LoggerManager.e("mun", "FBInterstial setLoad 2");
                } else {
                    LoggerManager.e("mun", "FBInterstial setLoad 3");
//                    setting(context);
                    interstitialAd.loadAd();
                    isLoading = true;

                }
            }
        }catch (Exception e){}
    }


    public boolean setShow(){
        if(interstitialAd!=null && interstitialAd.isAdLoaded()){
            interstitialAd.show();
            return true;
        }
        else{
            return false;
        }
    }


    private void setting(Context context){
        try {
            if(interstitialAd!=null){
                interstitialAd.destroy();
            }
        }catch (Exception e){}

        if(interstitialAd==null){
            interstitialAd = new InterstitialAd(context, "1897697773876301_1923960637916681");
        }
        interstitialAd.setAdListener(mInterstitialAdListener);
        interstitialAd.loadAd();
        isLoading = true;
    }


    InterstitialAdListener mInterstitialAdListener = new InterstitialAdListener() {
        @Override
        public void onInterstitialDisplayed(Ad ad) {
            // Interstitial displayed callback
        }

        @Override
        public void onInterstitialDismissed(Ad ad) {
            // Interstitial dismissed callback
        }

        @Override
        public void onError(Ad ad, AdError adError) {
            // Ad error callback
            isLoading = false;

            try {
                if (mCallback != null) {
                    mCallback.onErr();
                }
            }catch (Exception e){}
        }

        @Override
        public void onAdLoaded(Ad ad) {
            // Show the ad when it's done loading.
            isLoading = false;
            try {
                if (mCallback != null) {
                    mCallback.onLoad();
                }
            }catch (Exception e){}
        }

        @Override
        public void onAdClicked(Ad ad) {
            // Ad clicked callback
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            // Ad impression logged callback
        }
    };

    public interface Callback{
        public void onLoad();
        public void onErr();
    }
}
