package com.novato.jam.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.InstreamVideoAdView;
import com.google.android.gms.ads.AdSize;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.admob.Adinit;
import com.novato.jam.admob.AdmobInterstial;
import com.novato.jam.analytics.FirebaseAnalyticsLog;
import com.novato.jam.common.FragmentAppCompatManager;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.NoticeData;
import com.novato.jam.data.UserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.dialog.CustomToast;
import com.novato.jam.facebookad.FBInstreamAd;
import com.novato.jam.facebookad.FBInterstitial;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.http.GetTvBoxVideo;
import com.novato.jam.ui.fragment.MainPagerFeedFragment;
import com.novato.jam.ui.fragment.MyFeedFragment;
import com.novato.jam.ui.fragment.PrivateChatFragment;
import com.novato.jam.ui.fragment.RoomCreateFragment;
import com.novato.jam.ui.fragment.SettingFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
//import uk.co.chrisjenx.calligraphy.CalligraphyUtils;
//import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class MainActivity extends BaseActivity implements View.OnClickListener{
    final int RESULT_INTRO = 123;
    final public static int RESULT_ROOM = 211;


//    ViewPager mViewPager;
//    PagerAdapter mPagerAdapter;
//    View iv_left_menu;

    android.support.v4.widget.DrawerLayout drawer_layout;

    FragmentAppCompatManager mFragmentAppCompatManager;

//    MainPagerFeedFragment mMainPagerFeedFragment;


    private int mMenuBtn[] = {R.id.btn_list, R.id.btn_my, R.id.btn_create, R.id.btn_setting};


    static public UserData mUserData;
    static public boolean isAdmin;
    static public boolean isAdminChat;
    static public String admin_push;
    static public String mCurrentRoomId;
    static public long mServerTimeOffset;
    static public long isInstream;

    static public int frontAdMaxCount = 3;
    static public int frontAdCount = 0;

    private static boolean isLife = false;

    static public boolean isLife(){
        return isLife;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLife = false;
        mServerTimeOffset = -1;

        Fire.setOffline();

        setInstreamDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fire.setOnline();
        Fire.loadServerTime();


        isLife = true;

        frontAdCount = 0;

        FirebaseAnalyticsLog.setStartActivity(getActivity());

        startActivityForResult(new Intent(this, IntroActivity.class), RESULT_INTRO);

        drawer_layout = findViewById(R.id.drawer_layout);
        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            boolean isFirst = false;
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

                if(!isFirst) {
                    isFirst = true;
                    try {
                        setLeftFragment(true);
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

//        mMainPagerFeedFragment = new MainPagerFeedFragment();

//        setPager();



//        mFragmentAppCompatManager = new FragmentAppCompatManager(this, R.id.fragment);
//
//        for(int f : mMenuBtn){
//            findViewById(f).setOnClickListener(this);
//        }
//
//        long t = 0;
//        try {
//            t = DBManager.createInstnace(this).getLauncherBadgeToal();
//        }catch (Exception e){}
//        if(t > 0) {
////            setMeFragment(false);//인트로 땜에 부르면 안됨
//        }
//        else {
//            mFragmentAppCompatManager.replaceFragment(new MainPagerFeedFragment());
//        }






//        ((AppBarLayout)findViewById(R.id.appbar)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//
//
//                LoggerManager.e("off", appBarLayout.getHeight() +" / "+verticalOffset);
//
//                int max = appBarLayout.getHeight() - Utils.getPixSize(getActivity(), 38);
//                int bottom = max +verticalOffset;
//
//                if(mMainPagerFeedFragment!=null)mMainPagerFeedFragment.setWritePosition(bottom);
//
//            }
//        });


//        setFBads();
        setAdmob();


    }


    private void destroyBanner(){
        try {
            if (mFBadView != null) {
                mFBadView.destroy();
            }
        }catch (Exception e){}
        try{
            ((ViewManager)mFBadView.getParent()).removeView(mFBadView);
        }catch (Exception e){}

        try{
            mADMOBadView.destroy();
        }catch (Exception e){}
        try{
            ((ViewManager)mADMOBadView.getParent()).removeView(mADMOBadView);
        }catch (Exception e){}
    }
    com.facebook.ads.AdView mFBadView;
    private void setFBads(){
        destroyBanner();
        //admob
        if(android.os.Build.VERSION.SDK_INT >= 9){
            FrameLayout lay02 = (FrameLayout)findViewById(R.id.lay_ad);

            try{
                lay02.removeAllViews();
            }catch (Exception e){}
            mFBadView = new com.facebook.ads.AdView(getActivity(), "1897697773876301_1925909627721782", com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            mFBadView.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    setAdmob();
                }
                @Override
                public void onAdLoaded(Ad ad) {
                }
                @Override
                public void onAdClicked(Ad ad) {
                    setFBads();
                }
                @Override
                public void onLoggingImpression(Ad ad) {
                }
            });
            lay02.addView(mFBadView);
            mFBadView.loadAd();
        }
    }

    com.google.android.gms.ads.AdView mADMOBadView;
    private void setAdmob(){
        destroyBanner();
        if(android.os.Build.VERSION.SDK_INT >= 9){
            FrameLayout lay02 = (FrameLayout)findViewById(R.id.lay_ad);
            try{
                lay02.removeAllViews();
            }catch (Exception e){}

            mADMOBadView = new com.google.android.gms.ads.AdView(this);
            mADMOBadView.setAdSize(AdSize.SMART_BANNER);
            mADMOBadView.setAdUnitId(Adinit.getInstance().getBannerId());
            lay02.addView(mADMOBadView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_INTRO){
            if(resultCode == RESULT_OK){

                setProfile();


                long t = 0;
                try {
                    t = DBManager.createInstnace(this).getLauncherBadgeToal();
                }catch (Exception e){}
                if(t > 0) {
                    setMeFragment(false);
                }
            }
            else{
                finish();
            }
        }
        else if(RESULT_ROOM == requestCode && resultCode == RESULT_OK){
            try {
                for (Fragment f : mFragmentAppCompatManager.childFragments()) {
                    if(f instanceof MainPagerFeedFragment){
//                        ((MainPagerFeedFragment)f).setReplace();
                    }
                    else if(f instanceof MyFeedFragment){
                        try {
                            ((MyFeedFragment) f).addDataList(true);
                        }catch (Exception e){}
                    }
                }
            }catch (Exception e){}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        FBInterstitial.getInstance().setLoad(getActivity(), fbCallback);
    }

    FBInterstitial.Callback fbCallback = new FBInterstitial.Callback() {
        @Override
        public void onLoad() {
        }
        @Override
        public void onErr() {
            AdmobInterstial.getInstance().setLoad(getActivity());
        }
    };



    long exitTime = -1;
    @Override
    public void onBackPressed() {

        if(drawer_layout!=null && drawer_layout.isDrawerOpen(Gravity.END)){
            drawer_layout.closeDrawer(Gravity.END);
            return;
        }

        if(mFragmentAppCompatManager!=null && mFragmentAppCompatManager.popFragment(true)){

        }
        else if(exitTime < Fire.getServerTimestamp() - 1000){
            CustomToast.showToast(getActivity(), R.string.app_exit , Toast.LENGTH_SHORT);
            exitTime = Fire.getServerTimestamp();
        }
        else{
            finish();//super.onBackPressed();
        }
    }







    private void setProfile(){

//        Glide.with(getActivity())
//                .load(mUserData.getpImg())
////                .bitmapTransform(new CropCircleTransformation(mContext))
////                .placeholder(R.drawable.icon_progress)
////                        .placeholder(null)
//                .skipMemoryCache(true)
////                .error(R.drawable.profle)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .bitmapTransform(new CropCircleTransformation(getActivity()))
//                .into((ImageView) findViewById(R.id.iv_profile));
//
//
//
//        Glide.with(getActivity())
//                .load(mUserData.getpImg())
////                .bitmapTransform(new CropCircleTransformation(mContext))
////                .placeholder(R.drawable.icon_progress)
////                        .placeholder(null)
//                .skipMemoryCache(true)
////                .error(R.drawable.profle)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .bitmapTransform(new BlurTransformation(getActivity(), 10))
//                .into((ImageView) findViewById(R.id.iv_background));
//
//
//        ((TextView)findViewById(R.id.tv_name)).setText(mUserData.getUserName());
//        setSuctomFont(((TextView)findViewById(R.id.tv_name)));
//
//        findViewById(R.id.iv_profile).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                IntroActivity.setLogOut(getActivity());
//                finish();
//                startActivity(new Intent(getActivity(), MainActivity.class));
//            }
//        });

        //ver check
        Fire.getReference().child(Fire.KEY_BLOCK_VERSION).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot != null && dataSnapshot.getValue() !=null) {

                    HashMap data = (HashMap)dataSnapshot.getValue(true);
                    if(data!=null) {
                        boolean isBlock = false;
                        boolean isUpdate = false;
                        try {
                            long block = (Long) data.get("block");
                            if(GlobalApplication.getApplicationVersionCode() < block){
                                isBlock = true;
                            }
                        }catch (Exception e){}
                        try {
                            long update = (Long) data.get("update");
                            if(GlobalApplication.getApplicationVersionCode() < update){
                                isUpdate = true;
                            }
                        }catch (Exception e){}

                        try{
                            admin_push = data.get("admin")+"";
                        }catch (Exception e){}

                        try{
                            int fad = Integer.parseInt(data.get("fad")+"");
                            if(fad > 0) {
                                frontAdMaxCount = fad;
                            }
                        }catch (Exception e){
                            LoggerManager.e("munx", e.toString());
                        }


                        try {
                            long ad = (Long) data.get("ad");
                            isInstream = ad;
                            {
                                setFacebookInstream();
                            }
                        }catch (Exception e){}


                        if(isBlock){
                            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                            alert_confirm.setTitle(R.string.notice);
                            alert_confirm.setMessage(R.string.block_version);
                            alert_confirm.setCancelable(false);
                            alert_confirm.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent u = new Intent(Intent.ACTION_VIEW);
                                    u.setData(Uri.parse("https://play.google.com/store/apps/details?id="+ getPackageName()));
                                    startActivity(u);

                                    finish();
                                }
                            });
                            alert_confirm.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    finish();
                                }
                            });
                            alert_confirm.show();
                        }
                        else if(isUpdate){
                            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                            alert_confirm.setTitle(R.string.notice);
                            alert_confirm.setMessage(R.string.block_version);
                            alert_confirm.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent u = new Intent(Intent.ACTION_VIEW);
                                    u.setData(Uri.parse("https://play.google.com/store/apps/details?id="+ getPackageName()));
                                    startActivity(u);
                                }
                            });
                            alert_confirm.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                }
                            });
                            alert_confirm.show();
                        }

                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //notice

        Fire.getReference().child(Fire.KEY_NOTICE).orderByChild("time").endAt(Fire.getServerTimestamp()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<NoticeData> list = Parser.getNoticeDataListParse(dataSnapshot);
                if(list!=null && list.size() > 0) {

                    try {
                        final NoticeData data = list.get(0);

                        if(data.getType() == 99){
                            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                            alert_confirm.setTitle(R.string.notice);
                            alert_confirm.setMessage(data.getDesc());
                            alert_confirm.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            alert_confirm.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    finish();
                                }
                            });
                            alert_confirm.show();
                        }
                        else {
                            if (data.getTime() > MyPreferences.getLong(getActivity(), MyPreferences.KEY_NOTICE)) {
                                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                                alert_confirm.setTitle(R.string.notice);
                                alert_confirm.setMessage(data.getDesc());
                                LayoutInflater chec = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                final View checlay = chec.inflate(R.layout.notshowagain, null);
                                alert_confirm.setView(checlay);
                                alert_confirm.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CheckBox dontaskagain = (CheckBox)checlay.findViewById(R.id.skip);

                                        if (data.getType() != NoticeData.TYPE_RECYCLE) {
                                            MyPreferences.set(getActivity(), MyPreferences.KEY_NOTICE, data.getTime());
                                        }else{
                                            if(dontaskagain.isChecked()){
                                                MyPreferences.set(getActivity(), MyPreferences.KEY_NOTICE, data.getTime());
                                            }
                                        }
                                    }
                                });
                                alert_confirm.show();


                            }

                        }


                    }catch (Exception e){}


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //block check
        Fire.getReference().child(Fire.KEY_BLOCK_USER).child(MainActivity.mUserData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot != null && dataSnapshot.getValue() !=null) {
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                    alert_confirm.setTitle(R.string.notice);
                    alert_confirm.setMessage(R.string.block_user_msg);
                    alert_confirm.setCancelable(false);
                    alert_confirm.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    alert_confirm.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });
                    alert_confirm.show();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        mFragmentAppCompatManager = new FragmentAppCompatManager(this, R.id.fragment);

        for(int f : mMenuBtn){
            findViewById(f).setOnClickListener(this);
        }
        findViewById(R.id.btn_left_menu).setOnClickListener(this); // 아이콘 클릭시 이벤트 발생(비밀 메시지)
        long t = 0;
        try {
            t = DBManager.createInstnace(this).getLauncherBadgeToal();
        }catch (Exception e){}
        if(t > 0) {
//            setMeFragment(false);//인트로 땜에 부르면 안됨
        }
        else {
            mFragmentAppCompatManager.replaceFragment(new MainPagerFeedFragment());
        }






        try {
            if (getIntent() != null) {
                Uri uri = getIntent().getData();
                if (uri != null) {

                    if(getString(R.string.kakao_scheme).equals(uri.getScheme()) && getString(R.string.kakaolink_host).equals(uri.getHost())) {
                        String app_host_parameter = uri.getQueryParameter(getString(R.string.app_host_parameter));
                        if(!TextUtils.isEmpty(app_host_parameter)) {
                            app_host_parameter = Utils.getBase64Decoding(app_host_parameter);
                            LoggerManager.e("mun", "방초대 들어가기 : " + app_host_parameter);


                            FeedData mFeedData = new FeedData();
                            mFeedData.setKey(app_host_parameter);

                            Intent i = new Intent(getActivity(), RoomInfoActivity.class);
                            i.putExtra("data", mFeedData);
                            getActivity().startActivityForResult(i, MainActivity.RESULT_ROOM);
                        }

                    }
                }
            }
        }catch (Exception e){}

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_list:{
                setListFragment(true);

                break;
            }
            case R.id.btn_my:{
                setMeFragment(true);

                break;
            }
            case R.id.btn_create:{
                setCreateFragment(true);

                break;
            }
            case R.id.btn_setting:{
                setSettingFragment(true);

                break;
            }
            case R.id.btn_left_menu: {  // 클릭된것이 아이콘이면 setLeftFragment 실행함.   비밀 쪽지 수정
                setLeftFragment(true);

                break;
            }
        }
    }

    public void setListFragment(boolean ani) {
        for(int f : mMenuBtn){
            ((TextView)findViewById(f)).setTextColor(ContextCompat.getColor(getActivity(),R.color.btn_back));
        }
        ((TextView)findViewById(R.id.btn_list)).setTextColor(ContextCompat.getColor(getActivity(),R.color.pink));

        mFragmentAppCompatManager.replaceFragment(new MainPagerFeedFragment(), ani);
    }
    public void setMeFragment(boolean ani) {
        for(int f : mMenuBtn){
            ((TextView)findViewById(f)).setTextColor(ContextCompat.getColor(getActivity(),R.color.btn_back));
        }
        ((TextView)findViewById(R.id.btn_my)).setTextColor(ContextCompat.getColor(getActivity(),R.color.pink));

        mFragmentAppCompatManager.replaceFragment(new MyFeedFragment(), ani);
    }
    public void setCreateFragment(boolean ani) {
        if(mUserData == null || TextUtils.isEmpty(mUserData.getUid())){
            CustomToast.showToast(getActivity(), R.string.err_room_info, Toast.LENGTH_SHORT);
            return;
        }

        for(int f : mMenuBtn){
            ((TextView)findViewById(f)).setTextColor(ContextCompat.getColor(getActivity(),R.color.btn_back));
        }
        ((TextView)findViewById(R.id.btn_create)).setTextColor(ContextCompat.getColor(getActivity(),R.color.pink));

        mFragmentAppCompatManager.replaceFragment(new RoomCreateFragment(), ani);
    }
    public void setSettingFragment(boolean ani) {
        if(mUserData == null || TextUtils.isEmpty(mUserData.getUid())){
            CustomToast.showToast(getActivity(), R.string.err_room_info, Toast.LENGTH_SHORT);
            return;
        }

        for(int f : mMenuBtn){
            ((TextView)findViewById(f)).setTextColor(ContextCompat.getColor(getActivity(),R.color.btn_back));
        }
        ((TextView)findViewById(R.id.btn_setting)).setTextColor(ContextCompat.getColor(getActivity(),R.color.pink));


        mFragmentAppCompatManager.replaceFragment(new SettingFragment(), ani);
    }

    public void setLeftFragment(boolean ani){ // (  비밀 쪽지 수정
        for(int f : mMenuBtn){ // 아이콘 클릭됬으면 다른부분 색상 검정.
            ((TextView)findViewById(f)).setTextColor(ContextCompat.getColor(getActivity(),R.color.btn_back));
        }

        mFragmentAppCompatManager.replaceFragment(new PrivateChatFragment(), ani); // true 값이면 PrivateChatFragment 부분 실행.
    }

//    public void setAddFragment(Fragment f, boolean ani){
//        mFragmentAppCompatManager.replaceFragment(f, ani);
//    }


    /*******************************************************
     * left menu
     */
   /* SwipeRefreshLayout left_swipelayout;
    MessageListReAdapter mAdapter;
    RecyclerView left_list;
    EndlessRecyclerOnScrollListener mOnScrollListener;
    private ArrayList<FeedData> mListData = new ArrayList<>();

    private void setLeftFragment(){
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.addToBackStack(null);
//        transaction.add(R.id.left_fragment, new MyFeedFragment());
//        transaction.commitAllowingStateLoss();


        findViewById(R.id.left_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawer_layout!=null){
                    drawer_layout.closeDrawer(Gravity.END);
                }
            }
        });


        left_swipelayout = (SwipeRefreshLayout) findViewById(R.id.left_swipelayout);
        left_swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoggerManager.e("mun", "---------- onRefresh:");
                left_swipelayout.setRefreshing(false);
                addDataList(true);

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        }
        left_list = (RecyclerView) findViewById(R.id.left_list);
        left_list.setLayoutManager(layoutManager);
//        mListView.setHasFixedSize(true);
//        mListView.getItemAnimator().setAddDuration(300);
//        mListView.getItemAnimator().setRemoveDuration(100);
        mOnScrollListener = new EndlessRecyclerOnScrollListener(left_list.getLayoutManager()) {
            @Override
            public void onLoadMore(int current_page) {
                LoggerManager.e("munx", "onLoadMore :" + current_page);

//                addDataList(false);
            }
        };

        left_list.addOnScrollListener(mOnScrollListener);

        mAdapter = new MessageListReAdapter(mListData, left_list.getLayoutManager(), left_list);
        mAdapter.setNew(true);
        mAdapter.setCallback(new MessageListReAdapter.Callback() {
            @Override
            public void onClick(int position) {
                if(mListData!=null && mListData.size() > position){
                    FeedData mFeedData = mListData.get(position);

                    Intent i = new Intent(getActivity(), RoomInfoActivity.class);
                    i.putExtra("data",mFeedData);
                    getActivity().startActivityForResult(i, MainActivity.RESULT_ROOM);
                }
            }
            @Override
            public void onLongClick(int position) {
            }
        });
        left_list.setAdapter(mAdapter);

        addDataList(true);
    }

    private void addDataList(boolean replace){
        ArrayList<PushData> list = DBManager.createInstnace(getActivity()).getMessageAll();

        LoggerManager.e("mun", "size : " + list.size());

        mListData.clear();

        for(PushData p :list){
            FeedData mFeedData = new FeedData();
            mFeedData.setTitle(p.getRoomName());
            mFeedData.setKey(p.getRoom());
            mFeedData.setText(p.getTitle() + " : " + p.getMsg());
            mFeedData.setTime(p.getTime());

            mListData.add(mFeedData);
        }
        mAdapter.notifyDataSetChanged();
    }
*/
    /**************************************
     * instream
     */
    MediaPlayer mediaPlayer;
    Ad adInstrem;
    SurfaceView surfaceView;
    FrameLayout lay_instream;
    View btn_instream;
    ImageView iv_instream;
    private void setInstreamDestroy(){

        try{
            if(mediaPlayer!=null){
                mediaPlayer.release();
            }
        }catch (Exception e){}
        mediaPlayer = null;

        try{
            adInstrem.destroy();
        }catch (Exception e){}
        adInstrem = null;

        try{
            ViewGroup.LayoutParams p = surfaceView.getLayoutParams();
            p.height = 1;
            surfaceView.setLayoutParams(p);
        }catch (Exception e){}

        try{
            btn_instream.setVisibility(View.GONE);
        }catch (Exception e){}

    }
    private void setFacebookInstream(){
        if(MainActivity.isInstream < 2)
            return;

        lay_instream = findViewById(R.id.lay_instream);
        lay_instream.setVisibility(View.VISIBLE);
        btn_instream = findViewById(R.id.btn_instream);
        iv_instream = findViewById(R.id.iv_instream);
        surfaceView = findViewById(R.id.surface);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LoggerManager.e("fbads", ""+"surfaceCreated");

                loadTvBoxUrl();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                try {
                    mediaPlayer.setDisplay(null);
                }catch (Exception e){}

                setInstreamDestroy();
            }
        });



    }

    private void loadTvBoxUrl(){
        if(MainActivity.isInstream < 2)
            return;

        GlobalApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> result = new GetTvBoxVideo(getActivity()).start();
                if(result !=null && result.get("url") != null) {
                    final String mVideoUrl = result.get("url") + "";
                    String thum = "";
                    if(result.get("thum")!=null){
                        thum = result.get("thum") + "";
                    }

                    runStream(mVideoUrl, thum);

                }

            }
        });
    }

    private void runStream(final String mVideoUrl, final String thum){
        try{
            if(mediaPlayer!=null){
                mediaPlayer.release();
            }
        }catch (Exception e){}
        mediaPlayer = null;

        try{
            adInstrem.destroy();
        }catch (Exception e){}
        adInstrem = null;


        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(mVideoUrl);
            mediaPlayer.setDisplay(surfaceView.getHolder());
//                                            mediaPlayer.setVolume(0, 0);
        }catch (Exception e){
            LoggerManager.e("fbads", "3 : "+e.toString());
        }

        try {
            LoggerManager.e("fbads", "run 2");



//                                mediaPlayer.prepare();
            mediaPlayer.prepare();

            LoggerManager.e("fbads", "run 3");

            GlobalApplication.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int videoWidth = mediaPlayer.getVideoWidth();
                        int videoHeight = mediaPlayer.getVideoHeight();

                        LoggerManager.e("fbads", videoWidth + " = "+videoHeight);


                        final int screen = Utils.getScreenWidth(getActivity());

                        float heights = (((float) videoHeight / (float) videoWidth) * (float)screen );

                        float width = screen;

                        float s120 = (float)screen / 16.0f * 9.0f;
                        if(heights > s120){
                            float z =  s120 / heights;
                            heights = s120;
                            width = width * z;
                        }

                        final float height = heights;

                        {
                            ViewGroup.LayoutParams p = surfaceView.getLayoutParams();
                            p.height = (int) height;
                            p.width = (int) width;
                            surfaceView.setLayoutParams(p);
                        }

                        {
                            ViewGroup.LayoutParams p = iv_instream.getLayoutParams();
                            p.height = (int) height;
                            iv_instream.setLayoutParams(p);
                        }

                        Glide.with(getActivity())
                        .load(thum)
                        .into(iv_instream);


                        btn_instream.setVisibility(View.VISIBLE);
                        findViewById(R.id.btn_streamplay).setOnClickListener(new View.OnClickListener() {
                            boolean is = false;
                            @Override
                            public void onClick(View v) {

                                if(is){
                                   return;
                                }
                                is = true;

                                FBInstreamAd.setLoad(getActivity(), screen, (int)height,new FBInstreamAd.Callback2() {

                                    @Override
                                    public void onAdVideoComplete(Ad ad, int w, int h) {
                                        try{
                                            ad.destroy();
                                        }catch (Exception e){}
                                        try{
                                            adInstrem.destroy();
                                        }catch (Exception e){}

                                        try {
                                            btn_instream.setVisibility(View.GONE);
                                        }catch (Exception e){}

                                        try {
                                            mediaPlayer.start();
                                        }catch (Exception e){}

                                    }

                                    @Override
                                    public void onAdLoaded(Ad ad, InstreamVideoAdView adView) {
                                        adInstrem = ad;
                                        lay_instream.addView(adView);
                                        adView.show();
                                    }

                                    @Override
                                    public void onError(Ad ad, AdError adError) {
                                        try {
                                            btn_instream.setVisibility(View.GONE);
                                        }catch (Exception e){}

                                        try {
                                            mediaPlayer.start();
                                        }catch (Exception e){}

                                    }
                                });
                            }
                        });


                        LoggerManager.e("fbads", mVideoUrl);



                    }catch (Exception e){
                        LoggerManager.e("fbads", "3 : "+e.toString());
                    }
                }
            });
        }catch (Exception e){
            LoggerManager.e("fbads", "1 : "+e.toString());
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LoggerManager.e("mun", "onRequestPermissionsResult : " + requestCode);

        try {
            if (mFragmentAppCompatManager != null) {
                ArrayList<Fragment> list = mFragmentAppCompatManager.childFragments();
                if (list != null && list.size() > 0) {
                    LoggerManager.e("mun", "onRequestPermissionsResult : " + list.get(list.size() - 1).getClass().getName());
                    list.get(list.size() - 1).onRequestPermissionsResult(requestCode,permissions, grantResults);
                }
            }
        }catch (Exception e){}
    }

}


