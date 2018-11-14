package com.novato.jam.admob;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.novato.jam.BuildConfig;
import com.novato.jam.GlobalApplication;
import com.novato.jam.common.LoggerManager;

/**
 * Created by poshaly on 2018. 2. 9..
 */

public class Adinit {

    private static Adinit mAdinit;


    static public Adinit getInstance(){
        synchronized (Adinit.class) {
            if (mAdinit == null) {
                mAdinit = new Adinit();
            }
        }

        return mAdinit;
    }



    Adinit(){
        MobileAds.initialize(GlobalApplication.getAppContext(), "ca-app-pub-7837921866822480~1757891699");
    }



    public String getStoryBannerId(){
        return "ca-app-pub-7837921866822480/9855820941";
    }

    public String getTopBannerId(){
        return "ca-app-pub-7837921866822480/5424998254";
    }

    public String getBannerId(){
        return "ca-app-pub-7837921866822480/5549861650";
    }

    public String getNativeId(){
        return "ca-app-pub-7837921866822480/8131728357";
    }

    public String getFrontId(){
        return "ca-app-pub-7837921866822480/8425603718";
    }


    public String getRewardId(){
        return "ca-app-pub-7837921866822480/2768347694";
    }


    private RewardedVideoAd mRewardedVideoAd;
    public void setDestroyReward(){
        try {
            if (mRewardedVideoAd != null) mRewardedVideoAd.destroy(GlobalApplication.getAppContext());
        }catch (Exception e){}
        mRewardedVideoAd = null;
    }
    public void setRewardedVideoAd(){
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(GlobalApplication.getAppContext());
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                LoggerManager.e("mun","admob ads onRewardedVideoAdLoaded");
                if(mRewardedVideoAd!=null)mRewardedVideoAd.show();
            }

            @Override
            public void onRewardedVideoAdOpened() {
                LoggerManager.e("mun","admob ads onRewardedVideoAdOpened");
            }

            @Override
            public void onRewardedVideoStarted() {
                LoggerManager.e("mun","admob ads onRewardedVideoStarted");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                LoggerManager.e("mun","admob ads onRewardedVideoAdClosed");
                setDestroyReward();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                LoggerManager.e("mun","admob ads RewardItem : "+rewardItem.getAmount() +" / "+ rewardItem.getType());

            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                LoggerManager.e("mun","admob ads onRewardedVideoAdLeftApplication");
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                LoggerManager.e("mun","admob ads Fail : "+i);

//                AdRequest.ERROR_CODE_INTERNAL_ERROR // 0
                setDestroyReward();
            }

            @Override
            public void onRewardedVideoCompleted() {
                LoggerManager.e("mun","admob ads complete");
            }

        });

        loadRewardedVideoAd();
    }
    private void loadRewardedVideoAd() {
        if (mRewardedVideoAd !=null && !mRewardedVideoAd.isLoaded()) {
            AdRequest.Builder b = new AdRequest.Builder();
            if (BuildConfig.DEBUG) {
                try {
//                    b.addTestDevice("A16A235AF75BAC2293E38AD8DFFC8F4E");//s6
                } catch (Exception e) {
                }
            }

            mRewardedVideoAd.loadAd(getRewardId(), b.build());
        }
    }

}
