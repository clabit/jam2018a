package com.novato.jam.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.kekstudio.dachshundtablayout.DachshundTabLayout;
import com.kekstudio.dachshundtablayout.indicators.DachshundIndicator;
import com.novato.jam.R;
import com.novato.jam.admob.Adinit;
import com.novato.jam.ui.SearchActivity;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class MainPagerFeedFragment extends android.support.v4.app.Fragment implements View.OnClickListener{

    private View mRootView;

    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;

    ViewPager mViewPager;
    PagerAdapter mPagerAdapter;

    private FrameLayout lay_topad;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_mainpager, container, false);

        mProgressDialog = new ProgressDialog(getActivity());

        lay_topad = mRootView.findViewById(R.id.lay_topad);

        setPager();


        try {
            //if(isAdmin)
            {
                mRootView.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), SearchActivity.class));
                    }
                });
                mRootView.findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
            }
        }catch (Exception e){}
        return mRootView;
    }


    private void setPager(){
        if(mViewPager == null) {
            mViewPager = (ViewPager) mRootView.findViewById(R.id.pager);
            mViewPager.setOffscreenPageLimit(100);
            mPagerAdapter = new PagerAdapter(getActivity().getSupportFragmentManager());
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }
                @Override
                public void onPageSelected(int position) {
                    try {
                        if (mPagerAdapter != null) {
                            try {
                                Fragment f = mPagerAdapter.getItem(position);
                                if (f instanceof MainFeedFragment) {
                                    ((MainFeedFragment) f).setPagerCurrent();
                                }
                            }catch (Exception e){}
                        }
                    }catch (Exception e){}
                }
                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });


            DachshundTabLayout sliding_tabs = (DachshundTabLayout) mRootView.findViewById(R.id.sliding_tabs);
            sliding_tabs.setAnimatedIndicator(new DachshundIndicator(sliding_tabs));
            sliding_tabs.setupWithViewPager(mViewPager);

            mViewPager.setCurrentItem(1);


            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mViewPager.setCurrentItem(0,true);
                    }catch (Exception e){}
                }
            },1000);

        }
        else{
            //replace
        }
    }

    @Override
    public void onClick(View v) {

    }

    public void setReplace(){
        try {
            if (mPagerAdapter != null) {
                for(int i=0; i<mPagerAdapter.getCount(); i++){
                    try {
                        Fragment f = mPagerAdapter.getItem(i);
                        if (f instanceof MainFeedFragment) {
                            ((MainFeedFragment) f).addDataList(true);
                        }
                    }catch (Exception e){}
                }

            }
        }catch (Exception e){}
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        String[] Items = getResources().getStringArray(R.array.catelist);

        MainFeedFragment[] fragments = new MainFeedFragment[Items.length];

        public PagerAdapter(FragmentManager fm) {
            super(fm);


        }

        @Override
        public Fragment getItem(int position) {

            MainFeedFragment mMainFeedFragment;

            if(fragments[position] == null) {

                mMainFeedFragment = new MainFeedFragment();
                if(position == 0){
                    mMainFeedFragment.setChildCate("-99");
                }
                else if(position<10)
                {
                    mMainFeedFragment.setChildCate(position + "");

                }
                else if(position==10){
                    mMainFeedFragment.setChildCate("11"); // 공지 11번고정
                }


                fragments[position] = mMainFeedFragment;
            }
            else{
                mMainFeedFragment = fragments[position];
            }

            return mMainFeedFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);

            try{
                fragments[position].onDestroyView();
            }catch (Exception e){}
            try{
                fragments[position].onDestroy();
            }catch (Exception e){}
            try {
                fragments[position] = null;
            }catch (Exception e){}
        }

        @Override
        public int getCount() {
            if(Items==null){
                return 0;
            }
            return Items.length;  // 총 5개의 page를 보여줍니다.
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(Items!=null && Items.length > position){
                return Items[position];
            }
            return null;
        }


    }


    @Override
    public void onResume() {
        super.onResume();

//        setFbAd();


//        setAdmob();

    }



    com.google.android.gms.ads.AdView mADMOBadView;
    private void setAdmob(){
        destroyBanner();
        if(android.os.Build.VERSION.SDK_INT >= 9){
            try{
                lay_topad.removeAllViews();
            }catch (Exception e){}

            mADMOBadView = new com.google.android.gms.ads.AdView(getActivity());
            mADMOBadView.setAdSize(AdSize.SMART_BANNER);
            mADMOBadView.setAdUnitId(Adinit.getInstance().getTopBannerId());
            lay_topad.addView(mADMOBadView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mADMOBadView.setAdListener(new com.google.android.gms.ads.AdListener(){
                @Override
                public void onAdClosed() {
//                    setFBads();
                }
                @Override
                public void onAdFailedToLoad(int var1) {
//                    setFBads();
                }
                @Override
                public void onAdLeftApplication() {
                }
                @Override
                public void onAdOpened() {
                }
                @Override
                public void onAdLoaded() {
                }
                @Override
                public void onAdClicked() {
//                    setFBads();
                }
                @Override
                public void onAdImpression() {
                }
            });
            com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
            mADMOBadView.loadAd(adRequest);
        }
    }
    private void destroyBanner(){
//        try {
//            if (mFBadView != null) {
//                mFBadView.destroy();
//            }
//        }catch (Exception e){}
//        try{
//            ((ViewManager)mFBadView.getParent()).removeView(mFBadView);
//        }catch (Exception e){}

        try{
            mADMOBadView.destroy();
        }catch (Exception e){}
        try{
            ((ViewManager)mADMOBadView.getParent()).removeView(mADMOBadView);
        }catch (Exception e){}
    }




//    private void onBindViewHoldersAdmob() {
//        try {
//            lay_topad.removeAllViews();
//        }catch (Exception e){}
//        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.row_admob_native_small, null);
//
//        final AdmobHolder holder = new AdmobHolder(view);
//        holder.adView.setVisibility(View.GONE);
//
//        AdLoader adLoader = AdmobNativeData.getNewAd(
//                new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
//                    @Override
//                    public void onAppInstallAdLoaded(NativeAppInstallAd nativeAppInstallAd) {
//                        LoggerManager.e("mun","onAppInstallAdLoaded");
//
//                        lay_topad.addView(view);
//                        holder.setView(holder, nativeAppInstallAd);
//
//                    }
//                }
//                ,new AdListener(){
//                    @Override
//                    public void onAdFailedToLoad(int errorCode) {
//                        LoggerManager.e("mun","onAdFailedToLoad "+errorCode);
//
//                        try {
//                            ((ViewManager)view.getParent()).removeView(view);
//                        }catch (Exception e){}
//                    }
//                }
//        );
//        new AdmobNativeData(adLoader);
//    }
//
//
//
//    class AdmobHolder {
//        FrameLayout layout_root;
//        NativeAppInstallAdView adView;
//
//        public AdmobHolder(View v) {
//            layout_root = (FrameLayout) v.findViewById(R.id.layout_root);
////            admob = (com.google.android.gms.ads.AdView) v.findViewById(R.id.admob);
////            admob.setAdSize(new AdSize(-1,-2));
//
//            adView = v.findViewById(R.id.lay_adview);
//        }
//
//
//        private void setView(AdmobHolder holder, NativeAppInstallAd nativeAppInstallAd) {
//
//            NativeAppInstallAdView adView = holder.adView;
//
//            TextView headlineView = adView.findViewById(R.id.tv_title);
//            headlineView.setText(nativeAppInstallAd.getHeadline());
//            adView.setHeadlineView(headlineView);
//
//
//            TextView tv_message = adView.findViewById(R.id.tv_message);
//            tv_message.setText(nativeAppInstallAd.getBody());
//            adView.setBodyView(tv_message);
//
//
////            MediaView mediaView = adView.findViewById(R.id.iv_cover);
////            try {
////                if (nativeAppInstallAd.getVideoController().hasVideoContent()) {
////                    mediaView.setVisibility(View.VISIBLE);
////                    adView.setMediaView(mediaView);
////                } else {
////                    mediaView.setVisibility(View.GONE);
////                }
////            } catch (Exception e) {
////            }
//
////                            ImageView iv_main = adView.findViewById(R.id.iv_main);
////                            iv_main.setVisibility(View.VISIBLE);
////                            List<com.google.android.gms.ads.formats.NativeAd.Image> images = contentAd.getImages();
////                            if(images!=null && images.size() > 0)iv_main.setImageDrawable(images.get(0).getDrawable());
////                            adView.setImageView(iv_main);
//
//            ImageView icon = adView.findViewById(R.id.iv_icon);
//            try {
//                icon.setImageDrawable(nativeAppInstallAd.getIcon().getDrawable());
//            } catch (Exception e) {
//            }
//            adView.setIconView(icon);
//
//            com.google.android.gms.ads.formats.AdChoicesView ad_choices_container = adView.findViewById(R.id.ad_choices_container);
//            adView.setAdChoicesView(ad_choices_container);
//
//            Button btn = adView.findViewById(R.id.native_ad_call_to_action);
//            btn.setText(nativeAppInstallAd.getCallToAction());
//            adView.setCallToActionView(btn);
//
//
//            adView.setNativeAd(nativeAppInstallAd);
//            adView.setVisibility(View.VISIBLE);
//        }
//
//        private void setView(AdmobHolder holder, NativeContentAd nativeAppInstallAd) {
//
//            NativeAppInstallAdView adView = holder.adView;
//
//            TextView headlineView = adView.findViewById(R.id.tv_title);
//            headlineView.setText(nativeAppInstallAd.getHeadline());
//            adView.setHeadlineView(headlineView);
//
//
//            TextView tv_message = adView.findViewById(R.id.tv_message);
//            tv_message.setText(nativeAppInstallAd.getBody());
//            adView.setBodyView(tv_message);
//
//
////            MediaView mediaView = adView.findViewById(R.id.iv_cover);
////            try {
////                if (nativeAppInstallAd.getVideoController().hasVideoContent()) {
////                    mediaView.setVisibility(View.VISIBLE);
////                    adView.setMediaView(mediaView);
////                } else {
////                    mediaView.setVisibility(View.GONE);
////                }
////            } catch (Exception e) {
////            }
//
////                            ImageView iv_main = adView.findViewById(R.id.iv_main);
////                            iv_main.setVisibility(View.VISIBLE);
////                            List<com.google.android.gms.ads.formats.NativeAd.Image> images = contentAd.getImages();
////                            if(images!=null && images.size() > 0)iv_main.setImageDrawable(images.get(0).getDrawable());
////                            adView.setImageView(iv_main);
//
//            ImageView icon = adView.findViewById(R.id.iv_icon);
//            try {
//                icon.setImageDrawable(nativeAppInstallAd.getLogo().getDrawable());
//            } catch (Exception e) {
//            }
//            adView.setIconView(icon);
//
//            com.google.android.gms.ads.formats.AdChoicesView ad_choices_container = adView.findViewById(R.id.ad_choices_container);
//            adView.setAdChoicesView(ad_choices_container);
//
//            Button btn = adView.findViewById(R.id.native_ad_call_to_action);
//            btn.setText(nativeAppInstallAd.getCallToAction());
//            adView.setCallToActionView(btn);
//
//
//            adView.setNativeAd(nativeAppInstallAd);
//            adView.setVisibility(View.VISIBLE);
//        }
//    }
//    private void setFbAd(){
//        try {
//            lay_topad.removeAllViews();
//        }catch (Exception e){}
//
//        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.row_facebook_ad, null);
//        final FaceBookHolder holder = new FaceBookHolder(view);
//
//        final FBnative mFBnative = new FBnative(getActivity());
//        mFBnative.initFacebook(new FBnative.Callback() {
//            @Override
//            public void onLoad() {
//                lay_topad.addView(view);
//                mFBnative.showNativeAd(getActivity(), mFBnative, holder);
//
//            }
//
//            @Override
//            public void onFail() {
//                try{
//                    mFBnative.setDestroy();
//                }catch (Exception e){}
//                try {
//                    ((ViewManager)view.getParent()).removeView(view);
//                }catch (Exception e){}
//
//                onBindViewHoldersAdmob();
//            }
//        });
//    }
    public class FaceBookHolder {
        private Object object = new Object();


        public View origin_root;
        public View lay_root;
        public TextView tv_title,tv_message,tv_store;
        public ImageView native_ad_icon;
        public Button native_ad_call_to_action;
        public com.facebook.ads.MediaView iv_cover;
        public LinearLayout ad_choices_container;

        public FaceBookHolder(View v){
            origin_root = v;

            lay_root = (View)v.findViewById(R.id.lay_root);
            tv_message = (TextView) v.findViewById(R.id.tv_message);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_store = (TextView) v.findViewById(R.id.tv_store);
            native_ad_icon = (ImageView) v.findViewById(R.id.native_ad_icon);
            native_ad_call_to_action = (Button) v.findViewById(R.id.native_ad_call_to_action);
            iv_cover = (com.facebook.ads.MediaView) v.findViewById(R.id.iv_cover);
            ad_choices_container = (LinearLayout) v.findViewById(R.id.ad_choices_container);

        }


    }
}
