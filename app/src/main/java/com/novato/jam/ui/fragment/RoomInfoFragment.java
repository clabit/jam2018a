package com.novato.jam.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InstreamVideoAdView;
import com.glide.CropCircleTransformation;
import com.google.android.gms.ads.AdSize;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.novato.jam.BuildConfig;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.admob.Adinit;
import com.novato.jam.common.EndlessRecyclerOnScrollListener;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.ReportData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.data.UserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.dialog.BottomUserInfoDialog;
import com.novato.jam.dialog.CustomAlertDialog;
import com.novato.jam.dialog.CustomToast;
import com.novato.jam.facebookad.FBInstreamAd;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.http.GetTvBoxVideo;
import com.novato.jam.push.SendPushFCM;
import com.novato.jam.push.SendPushFCMNewChat;
import com.novato.jam.push.SendPushFCMReadyOk;
import com.novato.jam.ui.FeedWriteActivity;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.RoomInfoActivity;
import com.novato.jam.ui.SearchActivity;
import com.novato.jam.ui.adapter.ChatListReAdapter;
import com.novato.jam.ui.adapter.FeedListReAdapter;
import com.novato.jam.ui.adapter.RoomUserListReAdapter;
import com.novato.jam.ui.fragment.RoomCreateFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class RoomInfoFragment extends BaseRoomFragment implements View.OnClickListener{

//    private String uid = "";
//    private String roomKey = "";

    private View mRootView;

//    UserData userData;


    private RoomUserListReAdapter mAdapter01, mAdapter02;


    private Link link;
    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;

    private boolean isLoad = false;

//    public void setUid(String uid){
//        this.uid = uid;
//    }
//
//    public void setRoomKey(String roomKey){
//        this.roomKey = roomKey;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_roominfo, container, false);

        mProgressDialog = new ProgressDialog(getActivity());


        mRootView.findViewById(R.id.layout_back).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_ok).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_ok).setVisibility(View.GONE);

        mRootView.findViewById(R.id.btn_story).setOnClickListener(this);


        link = new Link(Pattern.compile("#\\w{1,15}"))
                .setTextColor(getResources().getColor(R.color.pink))                  // optional, defaults to holo blue
                .setTextColorOfHighlightedLink(Color.parseColor("#ffffff")) // optional, defaults to holo blue
                .setHighlightAlpha(.1f)                                     // optional, defaults to .15f
                .setUnderlined(false)                                       // optional, defaults to true
                .setBold(true)                                              // optional, defaults to false
                .setOnLongClickListener(new Link.OnLongClickListener() {
                    @Override
                    public void onLongClick(String clickedText) {
                        // long clicked
                    }
                })
                .setOnClickListener(new Link.OnClickListener() {
                    @Override
                    public void onClick(String clickedText) {
                        // single clicked

                        if (TextUtils.isEmpty(clickedText)) {
                            return;
                        }

                        try {
                            if (clickedText.startsWith("#")) {
                                clickedText = clickedText.substring(1, clickedText.length());
                            }
                        } catch (Exception e) {
                        }

                        Intent i = new Intent(getActivity(), SearchActivity.class);
                        i.putExtra("data", clickedText);
                        startActivity(i);

                    }
                });


        setFeedDataChange();


        //admob
        if(android.os.Build.VERSION.SDK_INT >= 9){
            FrameLayout lay02 = (FrameLayout)mRootView.findViewById(R.id.lay_ad);
            com.google.android.gms.ads.AdView adView22 = new com.google.android.gms.ads.AdView(getActivity());
            adView22.setAdSize(AdSize.SMART_BANNER);
            adView22.setAdUnitId(Adinit.getInstance().getBannerId());
            lay02.addView(adView22, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
            adView22.loadAd(adRequest);
        }

        return mRootView;
    }

    @Override
    public void setFeedDataChange(){
        super.setFeedDataChange();
        setUi();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layout_back:{
                getActivity().finish();
                break;
            }
            case R.id.btn_ok:{

                if(mFeedData == null || MainActivity.mUserData == null || TextUtils.isEmpty(MainActivity.mUserData.getUid())) {
                    CustomToast.showToast(getActivity(), R.string.err_room_info,Toast.LENGTH_SHORT);
                    return;
                }



                if(MainActivity.isAdmin && BuildConfig.DEBUG){

                    CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getContext(), "Noti","어드민 모드로 들어 가시겠습니까?", new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                        @Override
                        public void onClickOk() {
                            MainActivity.isAdminChat = true;
                            if (getActivity() instanceof RoomInfoActivity) {
                                ((RoomInfoActivity) getActivity()).setChatOpen(false);
                            }

                        }

                        @Override
                        public void onClickCancel() {
                            MainActivity.isAdminChat = false;
                            setInChatRoom();
                        }


                    });
                    mCustomAlertDialog.show();

                    return;
                }
                else if(MainActivity.isAdmin == false && mFeedData.getCate()==11){

//                  MainActivity.isAdminChat = true;
                    if (getActivity() instanceof RoomInfoActivity) {
                        ((RoomInfoActivity) getActivity()).setChatOpen(false);
                    }
                    return;
                }
                setInChatRoom();

                break;
            }
            case R.id.btn_story:{
                if(getActivity() instanceof RoomInfoActivity){
                    StoryListFragment mStoryListFragment = new StoryListFragment();
                    mStoryListFragment.setFeedData(mFeedData);
                    ((RoomInfoActivity)getActivity()).setChangeFragment(mStoryListFragment);
                }
                break;
            }
        }
    }

    private void setInChatRoom(){
        boolean isJoining = false;

        HashMap<String, Object> d = new HashMap<>();//MainActivity.mUserData.getHashMap();
        d.put("color", MainActivity.mUserData.getColor());
        d.put("desc", MainActivity.mUserData.getDesc());
//            d.put("mail", MainActivity.mUserData.getMail());
//            d.put("pImg", MainActivity.mUserData.getpImg());
//            d.put("push", MainActivity.mUserData.getPush());
//            d.put("time", MainActivity.mUserData.getTime());
        d.put("uid", MainActivity.mUserData.getUid());
        d.put("userName", MainActivity.mUserData.getUserName());


        int currentType = -1;
//                if(mFeedData.getUid().equals(MainActivity.mUserData.getUid()))
//                {
//                    isJoining = true;
//                    d.put("open", 1);
//                    currentType = 1;
//
//                    for(RoomUserData u : mFeedData.getListRoomUserData()) {
//                        if (u.getUid().equals(MainActivity.mUserData.getUid())) {
//                            try {
//                                d.put("userName", u.getUserName());
//                                d.put("desc", u.getDesc());
//                                d.put("color", u.getColor());
//                            }catch (Exception e){}
//                            break;
//                        }
//                    }
//
//                }
//                else
        {
            for(RoomUserData u : mFeedData.getListRoomUserData()) {
                if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                    isJoining = true;
                    currentType = u.getOpen();

                    try {
                        d.put("userName", u.getUserName());
                        d.put("desc", u.getDesc());
                        d.put("color", u.getColor());
                    }catch (Exception e){}

                    break;
                }
            }
        }


        if(currentType == -2) {
            CustomToast.showToast(getActivity(), R.string.join_ban ,Toast.LENGTH_SHORT);
            return;
        }

        if(isJoining && currentType == 0) {

            CustomToast.showToast(getActivity(), R.string.join_ready ,Toast.LENGTH_SHORT);
            CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getContext(), R.string.jam_out_title, R.string.jam_out_desc, new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                @Override
                public void onClickOk() {


                    if(mFeedData.getUid().equals(MainActivity.mUserData.getUid()) && mFeedData.getListRoomUserData() !=null && mFeedData.getListRoomUserData().size() > 1){
                        CustomToast.showToast(getActivity(), R.string.jam_out_err,Toast.LENGTH_SHORT);
                    }
                    else {
                        Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).removeValue();
                        Fire.getReference().child(Fire.KEY_MY_ROOM).child(MainActivity.mUserData.getUid()).child(mFeedData.getKey()).removeValue();
                        removeAudioList(MainActivity.mUserData.getUid());

                        try {
                            getActivity().setResult(getActivity().RESULT_OK);
                        } catch (Exception e) {
                        }

                        getActivity().finish();
                    }
                }

                @Override
                public void onClickCancel() {
                }
            });
            mCustomAlertDialog.show();


            return;
        }

        if(isJoining && currentType == 1) {
            d.put("open", 1);
//                    d.put("time",Fire.getServerTimestamp());

            Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).updateChildren(d);
//                    try {
//                        Fire.getReference().child(Fire.KEY_MY_ROOM).child(MainActivity.mUserData.getUid()).child(mFeedData.getKey()).child("chatCount").setValue(mFeedData.getChatCount());
//                    }catch (Exception e){}

            if (getActivity() instanceof RoomInfoActivity) {
                ((RoomInfoActivity) getActivity()).setChatOpen(false);
            }
            return;
        }




        if(mFeedData.getOpen() == 1){
            d.put("open", 1);
        }
        else if(mFeedData.getOpen() == 0){
            d.put("open", 0);
        }
        RoomJoinFragment mRoomJoinFragment = new RoomJoinFragment();
        mRoomJoinFragment.setUserData(d);
        mRoomJoinFragment.setFeedData(mFeedData);
        if (getActivity() instanceof RoomInfoActivity) {
            ((RoomInfoActivity) getActivity()).setChangeFragment(mRoomJoinFragment);
        }
    }


    synchronized private void setUi(){
//        if(isLoad)
//            return;
//
//        isLoad = true;

        try {

            if(mFeedData.getListRoomUserData() == null){
                mFeedData.setListRoomUserData(new ArrayList<RoomUserData>());
            }



            TextView tv_action_title = mRootView.findViewById(R.id.tv_action_title);
            tv_action_title.setText(mFeedData.getTitle());

            ImageView iv_img = mRootView.findViewById(R.id.iv_img);
            try {
                if(!TextUtils.isEmpty(mFeedData.getpImg())) {
                    Glide.with(getActivity())
                            .load("https://docs.google.com/uc?export=download&id=" +mFeedData.getpImg() + "")
//                    .bitmapTransform(new CropCircleTransformation(getActivity()))
//                .placeholder(R.drawable.icon_progress)
//                        .placeholder(null)
//                    .skipMemoryCache(true)
//                .error(R.drawable.none_img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(iv_img);
                    iv_img.setVisibility(View.VISIBLE);
                }
            }catch (Exception e){}


//            TextView tv_title = mRootView.findViewById(R.id.tv_title);
//            tv_title.setText(user.getUserName());

            TextView tv_text = mRootView.findViewById(R.id.tv_text);
            tv_text.setText(mFeedData.getText());


// create the link builder object add the link rule
            LinkBuilder.on(tv_text)
                    .addLink(link)
                    .build(); // create the clickable links



            mRootView.findViewById(R.id.btn_ok).setVisibility(View.VISIBLE);
//            final ScrollView scrollview = mRootView.findViewById(R.id.scrollview);


            boolean isReady = false;

            ArrayList<RoomUserData> ready = new ArrayList<>();
            ArrayList<RoomUserData> ok = new ArrayList<>();
            for(RoomUserData u :mFeedData.getListRoomUserData()){
                if(u.getRowType() != RoomUserListReAdapter.ROWTYPE_TITLE) {
                    if (u.getOpen() == 1) {
                        ok.add(u);
                    } else if (u.getOpen() == 0) {
                        ready.add(u);

                        try {
                            if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                                isReady = true;
                            }
                        }catch (Exception e){}
                    }
                    else if(MainActivity.isAdmin){
                        ready.add(u);
                    }
                }
            }

            if(isReady){
                ((TextView)mRootView.findViewById(R.id.btn_ok)).setText(R.string.feed_readying);
            }
            else{
                ((TextView)mRootView.findViewById(R.id.btn_ok)).setText(R.string.feed_join);
            }



            TextView tv_ready = mRootView.findViewById(R.id.tv_ready);
            tv_ready.setText(String.format(getString(R.string.ready_count), ready.size() + ""));

            TextView tv_member = mRootView.findViewById(R.id.tv_member);
            tv_member.setText(String.format(getString(R.string.member_count), ok.size() + ""));

            {
                RecyclerView mListView = (RecyclerView) mRootView.findViewById(R.id.list01);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                mListView.setLayoutManager(layoutManager);
//                mListView.setOnTouchListener( new View.OnTouchListener( )
//                {
//                    @Override
//                    public boolean onTouch( View v, MotionEvent event )
//                    {
//                        scrollview.requestDisallowInterceptTouchEvent(true);
//                        return false;
//                    }
//                } );
                mAdapter01 = new RoomUserListReAdapter(mFeedData.getUid(), ready, mListView.getLayoutManager(), mListView);
                mAdapter01.setCallback(callback);
                mListView.setAdapter(mAdapter01);
                mAdapter01.notifyDataSetChanged();
            }
            {
                RecyclerView mListView = (RecyclerView) mRootView.findViewById(R.id.list02);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                mListView.setLayoutManager(layoutManager);
//                mListView.setOnTouchListener( new View.OnTouchListener( )
//                {
//                    @Override
//                    public boolean onTouch( View v, MotionEvent event )
//                    {
//                        scrollview.requestDisallowInterceptTouchEvent(true);
//                        return false;
//                    }
//                } );
                mAdapter02 = new RoomUserListReAdapter(mFeedData.getUid(), ok, mListView.getLayoutManager(), mListView);
                mAdapter02.setCallback(callback);
                mListView.setAdapter(mAdapter02);
                mAdapter02.notifyDataSetChanged();
            }


            try {
                if (MainActivity.mUserData != null && !TextUtils.isEmpty(mFeedData.getUid()) && mFeedData.getUid().equals(MainActivity.mUserData.getUid())) {
                    mRootView.findViewById(R.id.lay_ready).setVisibility(View.VISIBLE);
                }
                else if(MainActivity.isAdmin){
                    mRootView.findViewById(R.id.lay_ready).setVisibility(View.VISIBLE);
                }
                else {
                    mRootView.findViewById(R.id.lay_ready).setVisibility(View.GONE);
                }
            }catch (Exception e){}




            if(mFeedData.getCate()<11) { //공지사항탭 스토리 지우기
                mRootView.findViewById(R.id.btn_story).setVisibility(View.VISIBLE);
            }
        }catch (Exception e){}
    }

    RoomUserListReAdapter.Callback callback = new RoomUserListReAdapter.Callback() {
        @Override
        public void onClick(int position, final RoomUserData item) {
            try {
                if (item != null) {
                }
            } catch (Exception e) {
            }
        }

        @Override
        public void onLongClick(int position, final RoomUserData item) {
            try {
                if (item != null) {
                }
            } catch (Exception e) {
            }
        }

        @Override
        public void onMoreClick(int position, final RoomUserData item) {
            try {
                if (item != null) {

                    //방관리자 이면서 본인이 아닌 프로필 이였을때s
                    if(MainActivity.mUserData!=null && !TextUtils.isEmpty(mFeedData.getUid()) && mFeedData.getUid().equals(MainActivity.mUserData.getUid()) && !mFeedData.getUid().equals(item.getUid())) {
                        if (item.getOpen() == 1) {

                            final BottomUserInfoDialog mBottomSheetDialog = new BottomUserInfoDialog(getActivity(), item ,BottomUserInfoDialog.TYPE_ADMIN_BAN);
                            mBottomSheetDialog.setOnClickBan(new View.OnClickListener() {
                                int count = 0;
                                @Override
                                public void onClick(View v) {
                                    if(count == 0){
                                        CustomToast.showToast(getActivity(), String.format(getString(R.string.member_ban_msg01), item.getUserName()), Toast.LENGTH_SHORT);
                                        count++;
                                        return;
                                    }

                                    CustomToast.showToast(getActivity(), String.format(getString(R.string.member_ban_msg02), item.getUserName()), Toast.LENGTH_SHORT);

                                    mBottomSheetDialog.dismiss();
                                    Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(item.getUid()).child("open").setValue(-2);
                                    Fire.getReference().child(Fire.KEY_MY_ROOM).child(item.getUid()).child(mFeedData.getKey()).removeValue();

                                    removeAudioList(item.getUid());
                                }
                            });
                            mBottomSheetDialog.setOnClickMaster(new View.OnClickListener() {
                                int count = 0;
                                @Override
                                public void onClick(View v) {

                                    if(count == 0){
                                        CustomToast.showToast(getActivity(), String.format(getString(R.string.member_master_msg01), item.getUserName()), Toast.LENGTH_SHORT);
                                        count++;
                                        return;
                                    }

                                    CustomToast.showToast(getActivity(), String.format(getString(R.string.member_master_msg02), item.getUserName()), Toast.LENGTH_SHORT);


                                    Fire.getReference().child(Fire.KEY_MY_ROOM).child(mFeedData.getUid()).child(mFeedData.getKey()).child("uid").setValue(item.getUid());
                                    Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).child("uid").setValue(item.getUid());
                                    Fire.getReference().child(Fire.KEY_CHAT_ROOM).child(mFeedData.getCate()+"").child(mFeedData.getKey()).child("uid").setValue(item.getUid());
                                    mBottomSheetDialog.dismiss();
                                }
                            });
                            mBottomSheetDialog.show();

                        } else {
                            //승인처리
                            final BottomUserInfoDialog mBottomSheetDialog = new BottomUserInfoDialog(getActivity(), item, BottomUserInfoDialog.TYPE_ADMIN_JOIN);
                            mBottomSheetDialog.setOnClickJoinOk(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(item.getUid()).child("open").setValue(1);
                                    Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(item.getUid()).child("time").setValue(Fire.getServerTimestamp());

                                    try {
                                        final String uid = item.getUid();
                                        Fire.getReference().child(Fire.KEY_USER).child(uid).child("push").addListenerForSingleValueEvent(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(DataSnapshot dataSnapshot) {
                                               try {

                                                   if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                                                       final String push2 = (String) dataSnapshot.getValue(true);


                                                       LoggerManager.e("mun", "send push "+push2);

                                                       GlobalApplication.runBackground(new Runnable() {
                                                           @Override
                                                           public void run() {
                                                               try{
                                                                   if(!TextUtils.isEmpty(push2)) {
                                                                       DBManager.createInstnace(getActivity()).addUserPush(uid, push2);
                                                                       ArrayList<String> list = new ArrayList<String>();
                                                                       list.add(push2);
                                                                       String re = new SendPushFCMReadyOk(getActivity(), list, mFeedData.getKey()).start();
                                                                   }
                                                               }catch (Exception e){
                                                                   LoggerManager.e("mun", "send push "+e.toString());
                                                               }
                                                           }
                                                       });

                                                   }
                                               } catch (Exception e) {
                                                   LoggerManager.e("mun", "send push "+e.toString());
                                               }
                                           }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                LoggerManager.e("mun", "send push onCancelled ");
                                            }
                                        });

                                    }catch (Exception e){
                                        LoggerManager.e("mun", "send push "+e.toString());
                                    }

                                    mBottomSheetDialog.dismiss();

                                }
                            });
                            mBottomSheetDialog.setOnClickRemoveUser(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mBottomSheetDialog.dismiss();
                                    Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(item.getUid()).removeValue();
                                    Fire.getReference().child(Fire.KEY_MY_ROOM).child(item.getUid()).child(mFeedData.getKey()).removeValue();
                                }
                            });
                            mBottomSheetDialog.show();
                        }
                    }
                    else{
                        BottomUserInfoDialog mBottomSheetDialog = new BottomUserInfoDialog(getActivity(), item);
                        mBottomSheetDialog.show();
                    }
                }
            } catch (Exception e) {
            }
        }
    };
}
