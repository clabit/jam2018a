package com.novato.jam.admob;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.novato.jam.BuildConfig;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.novato.jam.GlobalApplication;
import com.novato.jam.common.LoggerManager;

import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Created by poshaly on 2018. 2. 9..
 */

public class AdmobInterstial {

    private static AdmobInterstial mAdinit;


    static public AdmobInterstial getInstance(){
        synchronized (AdmobInterstial.class) {
            if (mAdinit == null) {
                mAdinit = new AdmobInterstial();
            }
        }

        return mAdinit;
    }



    public AdmobInterstial(){
//        MobileAds.setAppVolume(0.5f);
//        MobileAds.initialize(GlobalApplication.getAppContext(), "ca-app-pub-7837921866822480~1757891699");
//        MobileAds.setAppMuted(false);
    }


    private com.google.android.gms.ads.InterstitialAd mADMOBInterstitialAd;

    public void setLoad(Context context){
        try {
            if (mADMOBInterstitialAd == null) {
                LoggerManager.e("mun", "AdmobInterstial setLoad 1");
                setting(context);
            } else {
                if (mADMOBInterstitialAd.isLoaded() || mADMOBInterstitialAd.isLoading()) {
                    LoggerManager.e("mun", "AdmobInterstial setLoad 2");
                } else {
                    LoggerManager.e("mun", "AdmobInterstial setLoad 3");
//                    setting(context);
                    mADMOBInterstitialAd.loadAd(builder());

                }
            }
        }catch (Exception e){}
    }


    public boolean setShow(){
        if(mADMOBInterstitialAd!=null && mADMOBInterstitialAd.isLoaded()){
            mADMOBInterstitialAd.show();
            return true;
        }
        else{
            return false;
        }
    }


    private void setting(Context context){
        mADMOBInterstitialAd = new com.google.android.gms.ads.InterstitialAd(context);
        mADMOBInterstitialAd.setAdUnitId(Adinit.getInstance().getFrontId());
        mADMOBInterstitialAd.setAdListener(mAdListener);
        mADMOBInterstitialAd.loadAd(builder());
    }
    private AdRequest builder(){
        AdRequest.Builder mAdRequest = new AdRequest.Builder();
//        mAdRequest.setGender(AdRequest.GENDER_FEMALE);
        Random r = new Random();
        int rr1 = r.nextInt(10) + 1;
        int rr2 = r.nextInt(15) + 1;

        int rr0 = r.nextInt(15) + 1980;

        LoggerManager.e("munx","AdmobInterstial : " + rr0 + " , " + rr1 + " , " + rr2);

        mAdRequest.setBirthday(new GregorianCalendar(rr0, rr1, rr2).getTime());
        return mAdRequest.build();
    }

    AdListener mAdListener = new AdListener() {

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            LoggerManager.e("mun", "front banner onAdLoaded");
        }

        @Override
        public void onAdFailedToLoad(int i) {
            super.onAdFailedToLoad(i);

            if (AdRequest.ERROR_CODE_NO_FILL == i) {
            }
            LoggerManager.e("mun", "front banner onAdFailedToLoad " + i);
        }
    };
}
