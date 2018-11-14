package com.novato.jam.data;

import android.content.Context;
import android.os.Parcel;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.firebase.auth.FirebaseUser;
import com.novato.jam.BuildConfig;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.admob.Adinit;
import com.novato.jam.common.LoggerManager;

import java.util.HashMap;

/**
 * Created by poshaly on 16. 9. 13..
 */
public class AdmobNativeData {


    AdLoader adLoader;
    NativeContentAd contentAd;
    NativeAppInstallAd nativeAppInstallAd;

    public AdmobNativeData(AdLoader ad) {
        adLoader = ad;
    }

    public void setNativeAppInstallAd(NativeAppInstallAd ad){
        try {
            if (contentAd != null) contentAd.destroy();
            contentAd = null;
        }catch (Exception e){}
        try {
            if (nativeAppInstallAd != null) nativeAppInstallAd.destroy();
            nativeAppInstallAd = null;
        }catch (Exception e){}

        nativeAppInstallAd = ad;

    }

    public void setNativeContentAd(NativeContentAd ad){
        try {
            if (contentAd != null) contentAd.destroy();
            contentAd = null;
        }catch (Exception e){}
        try {
            if (nativeAppInstallAd != null) nativeAppInstallAd.destroy();
            nativeAppInstallAd = null;
        }catch (Exception e){}

        contentAd = ad;
    }


    public AdLoader getAdLoader() {
        return adLoader;
    }

    public void setAdLoader(AdLoader adLoader) {
        this.adLoader = adLoader;
    }

    public NativeContentAd getContentAd() {
        return contentAd;
    }

    public NativeAppInstallAd getNativeAppInstallAd() {
        return nativeAppInstallAd;
    }

    static private AdRequest.Builder getAdRequest(){
        AdRequest.Builder b = new AdRequest.Builder();
        if (BuildConfig.DEBUG) {
            try {
//                b.addTestDevice("A16A235AF75BAC2293E38AD8DFFC8F4E");//s6
            } catch (Exception e) {
            }
        }
        return b;
    }

    static public AdLoader getNewAd(NativeAppInstallAd.OnAppInstallAdLoadedListener listener, AdListener errListener){
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setImageOrientation(NativeAdOptions.ORIENTATION_PORTRAIT)
                .setVideoOptions(videoOptions)
                .build();
        AdLoader adLoader = new AdLoader.Builder(GlobalApplication.getAppContext(), Adinit.getInstance().getNativeId())
                .forAppInstallAd(listener)
                .withAdListener(errListener)
                .withNativeAdOptions(adOptions)
                .build();
        adLoader.loadAd(getAdRequest().build());
        return adLoader;
    }

    static public AdLoader getNewAd(NativeContentAd.OnContentAdLoadedListener listener, AdListener errListener){
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setImageOrientation(NativeAdOptions.ORIENTATION_PORTRAIT)
                .setVideoOptions(videoOptions)
                .build();
        AdLoader adLoader = new AdLoader.Builder(GlobalApplication.getAppContext(), Adinit.getInstance().getNativeId())
                .forContentAd(listener)
                .withAdListener(errListener)
                .withNativeAdOptions(adOptions)
                .build();
        adLoader.loadAd(getAdRequest().build());
        return adLoader;
    }

    static public boolean isLoadedAd(AdmobNativeData ad){
        if(ad!=null
                && ad.getAdLoader() !=null
                && !ad.getAdLoader().isLoading()
                && (ad.getNativeAppInstallAd() !=null || ad.getContentAd() !=null)){
            return true;
        }

        return false;
    }

}
