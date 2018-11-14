package com.novato.jam.facebookad;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.ui.adapter.FeedListReAdapter;
import com.novato.jam.ui.fragment.MainPagerFeedFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by poshaly on 2018. 3. 22..
 */

public class FBnative {
    Context context;

    public FBnative(Context context){
        this.context = context;
    }

    private com.facebook.ads.NativeAd nativeAd;
    private long facebookLoadTime;
    private boolean isLoadingFBads = false;
    private Object object = new Object();


    public void setDestroy(){
        try {
            try{
                if (nativeAd != null) {
                    nativeAd.unregisterView();
                }
            }catch (Exception e){}
            if (nativeAd != null) {
                nativeAd.destroy();
                nativeAd = null;
            }
        }catch (Exception e){}
    }
    synchronized public boolean initFacebook(final Callback mCallback){
        boolean replce = false;
        try {

            if(nativeAd == null){// || facebookLoadTime < System.currentTimeMillis() - (1000 * 60 * 1) ) {
                facebookLoadTime = System.currentTimeMillis();
                isLoadingFBads = false;
                try {
                    try{
                        if (nativeAd != null) {
                            nativeAd.unregisterView();
                        }
                    }catch (Exception e){}
                    if (nativeAd != null) {
                        nativeAd.destroy();
                        nativeAd = null;
                    }
                }catch (Exception e){}
                nativeAd = new com.facebook.ads.NativeAd(context, "1897697773876301_1920116094967802");
                nativeAd.setAdListener(new com.facebook.ads.AdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        try {
                            try {
                                if (nativeAd != null) {
                                    nativeAd.unregisterView();
                                }
                            }catch (Exception e){}
                        }catch (Exception e){}

                        if(mCallback!=null){
                            mCallback.onFail();
                        }
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
//                        if (ad != nativeAd) {
//                            return;
//                        }

                        isLoadingFBads = true;

//                        notifyDataSetChanged();

                        if(mCallback!=null){
                            mCallback.onLoad();
                        }
                    }

                    @Override
                    public void onAdClicked(Ad ad) {

                    }

                    @Override
                    public void onLoggingImpression(Ad ad) {

                    }
                });
                nativeAd.loadAd(NativeAd.MediaCacheFlag.ALL);
                replce = true;
            }
        }catch (Exception e){}

        return replce;
    }



    static public void showNativeAd(Context context, FBnative mFBnative, final FeedListReAdapter.FaceBookHolder holder, final int position){
        if(mFBnative == null || context == null || holder == null)
            return;


//        synchronized (object)
        {
            try {
                holder.lay_root.setVisibility(View.GONE);
            }catch (Exception e){
                LoggerManager.e("munx", "showNativeAd setVisibility : " + e.toString());
            }


//            initFacebook();

            if(mFBnative.nativeAd == null)
                return;

            if(!mFBnative.isLoadingFBads)
                return;

            try {
                if (mFBnative.nativeAd != null) {
                    mFBnative.nativeAd.unregisterView();
                }
            } catch (Exception e) {
            }


            try {
                try {
                    try {
                        holder.tv_title.setText(mFBnative.nativeAd.getAdTitle());
                    } catch (Exception e) {
                    }
                    try {
                        holder.tv_store.setText(mFBnative.nativeAd.getAdSocialContext());
                    } catch (Exception e) {
                    }
                    try {
                        String m = mFBnative.nativeAd.getAdBody();
                        if(!TextUtils.isEmpty(m)) {
                            holder.tv_message.setText(m);
                            holder.tv_message.setVisibility(View.VISIBLE);
                            holder.tv_title.setTextSize(10);
                        }
                        else{
                            holder.tv_message.setVisibility(View.GONE);
                            holder.tv_title.setTextSize(14);
                        }
                    } catch (Exception e) {
                    }
                    try {
                        holder.native_ad_call_to_action.setText(mFBnative.nativeAd.getAdCallToAction());
                    } catch (Exception e) {
                    }

                    try {
                        // Download and display the ad icon.
                        NativeAd.Image adIcon = mFBnative.nativeAd.getAdIcon();
                        NativeAd.downloadAndDisplayImage(adIcon, holder.native_ad_icon);
                    } catch (Exception e) {
                    }

//                        try {
//                            // Download and display the cover image.
//                            holder.iv_cover.setNativeAd(nativeAd);
////            NativeAd.downloadAndDisplayImage(nativeAd.getAdCoverImage(), holder.iv_pokemon);
//                        } catch (Exception e) {
//                        }

                    try {
                        // Add the AdChoices icon
                        holder.ad_choices_container.removeAllViews();
                        AdChoicesView adChoicesView = new AdChoicesView(context, mFBnative.nativeAd, true);
                        holder.ad_choices_container.addView(adChoicesView);
                    } catch (Exception e) {
                    }


                    // Register the Title and CTA button to listen for clicks.
                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(holder.tv_title);
                    clickableViews.add(holder.native_ad_call_to_action);
                    clickableViews.add(holder.tv_message);
                    clickableViews.add(holder.native_ad_icon);
                    try {
                        clickableViews.add(holder.iv_cover);
                    } catch (Exception e) {
                    }


                    holder.lay_root.setVisibility(View.VISIBLE);

                    mFBnative.nativeAd.registerViewForInteraction(holder.lay_root, clickableViews);
                } catch (Exception e) {
                    LoggerManager.e("munx", "showNativeAd : " + e.toString());
                }
            } catch (Exception e) {
            }
        }

    }



    static public void showNativeAd(Context context, FBnative mFBnative, final MainPagerFeedFragment.FaceBookHolder holder){
        if(mFBnative == null || context == null || holder == null)
            return;


//        synchronized (object)
        {
            try {
                holder.lay_root.setVisibility(View.GONE);
            }catch (Exception e){
                LoggerManager.e("munx", "showNativeAd setVisibility : " + e.toString());
            }


//            initFacebook();

            if(mFBnative.nativeAd == null)
                return;

            if(!mFBnative.isLoadingFBads)
                return;

            try {
                if (mFBnative.nativeAd != null) {
                    mFBnative.nativeAd.unregisterView();
                }
            } catch (Exception e) {
            }


            try {
                try {
                    try {
                        holder.tv_title.setText(mFBnative.nativeAd.getAdTitle());
                    } catch (Exception e) {
                    }
                    try {
                        holder.tv_store.setText(mFBnative.nativeAd.getAdSocialContext());
                    } catch (Exception e) {
                    }
                    try {
                        String m = mFBnative.nativeAd.getAdBody();
                        if(!TextUtils.isEmpty(m)) {
                            holder.tv_message.setText(m);
                            holder.tv_message.setVisibility(View.VISIBLE);
                            holder.tv_title.setTextSize(10);
                        }
                        else{
                            holder.tv_message.setVisibility(View.GONE);
                            holder.tv_title.setTextSize(14);
                        }
                    } catch (Exception e) {
                    }
                    try {
                        holder.native_ad_call_to_action.setText(mFBnative.nativeAd.getAdCallToAction());
                    } catch (Exception e) {
                    }

                    try {
                        // Download and display the ad icon.
                        NativeAd.Image adIcon = mFBnative.nativeAd.getAdIcon();
                        NativeAd.downloadAndDisplayImage(adIcon, holder.native_ad_icon);
                    } catch (Exception e) {
                    }

//                        try {
//                            // Download and display the cover image.
//                            holder.iv_cover.setNativeAd(nativeAd);
////            NativeAd.downloadAndDisplayImage(nativeAd.getAdCoverImage(), holder.iv_pokemon);
//                        } catch (Exception e) {
//                        }

                    try {
                        // Add the AdChoices icon
                        holder.ad_choices_container.removeAllViews();
                        AdChoicesView adChoicesView = new AdChoicesView(context, mFBnative.nativeAd, true);
                        holder.ad_choices_container.addView(adChoicesView);
                    } catch (Exception e) {
                    }


                    // Register the Title and CTA button to listen for clicks.
                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(holder.tv_title);
                    clickableViews.add(holder.native_ad_call_to_action);
                    clickableViews.add(holder.tv_message);
                    clickableViews.add(holder.native_ad_icon);
                    try {
                        clickableViews.add(holder.iv_cover);
                    } catch (Exception e) {
                    }


                    holder.lay_root.setVisibility(View.VISIBLE);

                    mFBnative.nativeAd.registerViewForInteraction(holder.lay_root, clickableViews);
                } catch (Exception e) {
                    LoggerManager.e("munx", "showNativeAd : " + e.toString());
                }
            } catch (Exception e) {
            }
        }

    }



    public interface Callback{
        public void onLoad();
        public void onFail();
    }

}
