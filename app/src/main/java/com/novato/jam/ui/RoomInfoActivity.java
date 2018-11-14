package com.novato.jam.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.R;
import com.novato.jam.admob.AdmobInterstial;
import com.novato.jam.common.FragmentAppCompatManager;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.data.UserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.facebookad.FBInterstitial;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.ui.adapter.MessageListReAdapter;
import com.novato.jam.ui.fragment.BaseRoomFragment;
import com.novato.jam.ui.fragment.RoomChatFragment;
import com.novato.jam.ui.fragment.RoomInfoFragment;
import com.novato.jam.ui.fragment.RoomJoinFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class RoomInfoActivity extends BaseActivity {

    FragmentAppCompatManager mFragmentAppCompatManager;
    FeedData mFeedData;
    boolean isCreate;
    //(update3)
    MessageListReAdapter mAdapter;
    private ArrayList<FeedData> mListData = new ArrayList<>();

//    private com.google.android.gms.ads.InterstitialAd mADMOBInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_comment);

        sendFinishBroadcast();

        mFeedData = getIntent().getParcelableExtra("data");


        mFragmentAppCompatManager = new FragmentAppCompatManager(this, R.id.fragment);



        //RoomChatFragment


        isCreate = getIntent().getBooleanExtra("create", false);

        if(isCreate) {
            final ArrayList<String> tags = getIntent().getStringArrayListExtra("tags");

            Fire.loadServerTime(new Fire.TimeCallback() {
                @Override
                public void timeOffet(long offset) {
                    HashMap<String, Object> d = new HashMap<>();//MainActivity.mUserData.getHashMap();
                    d.put("open", 1);
                    d.put("color", MainActivity.mUserData.getColor());
                    d.put("desc", MainActivity.mUserData.getDesc());
//            d.put("mail", MainActivity.mUserData.getMail());
//            d.put("pImg", MainActivity.mUserData.getpImg());
//            d.put("push", MainActivity.mUserData.getPush());
//            d.put("time", MainActivity.mUserData.getTime());
                    d.put("uid", MainActivity.mUserData.getUid());
                    d.put("userName", MainActivity.mUserData.getUserName());

                    d.put("time", Fire.getServerTimestamp());

                    RoomJoinFragment mRoomJoinFragment = new RoomJoinFragment();
                    mRoomJoinFragment.setUserData(d);
                    mRoomJoinFragment.setFeedData(mFeedData);
                    mRoomJoinFragment.setCreate(true);
                    try {
                        if(tags!=null && tags.size() > 0)mRoomJoinFragment.setTags(tags);
                    }catch (Exception e){}
                    mFragmentAppCompatManager.replaceFragment(mRoomJoinFragment, false);
                }
            });
        }
        else{
            Fire.loadServerTime();

            Fire.getReference().child(Fire.KEY_REPORTCount_ROOM).child(mFeedData.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long z = 0;
                    try {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            z = Long.parseLong(dataSnapshot.getValue() + "");
                        }
                    }catch (Exception e){}

                    LoggerManager.e("mun", Fire.KEY_REPORTCount_ROOM + " //// " + z);
                    if(z > 10){
                        android.support.v7.app.AlertDialog.Builder alert_confirm = new android.support.v7.app.AlertDialog.Builder(getActivity());
                        alert_confirm.setTitle(R.string.notice);
                        alert_confirm.setMessage(R.string.block_jam_msg);
                        alert_confirm.setCancelable(false);
                        alert_confirm.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        });
                        alert_confirm.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                getActivity().finish();
                            }
                        });
                        alert_confirm.show();

                        return;
                    }
                    else{
                        RoomInfoFragment d = new RoomInfoFragment();
                        d.setFeedData(mFeedData);
                        mFragmentAppCompatManager.replaceFragment(d, false);

                        try{
                            Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).removeEventListener(infoListener);
                        }catch (Exception e){}
                        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).addValueEventListener(infoListener);


                        try{
                            Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).removeEventListener(userListListener);
                        }catch (Exception e){}
                        Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).addChildEventListener(userListListener);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




        }




//        mADMOBInterstitialAd = new com.google.android.gms.ads.InterstitialAd(getActivity());
//        mADMOBInterstitialAd.setAdUnitId(Adinit.getInstance().getFrontId());
//        AdRequest mAdRequest = new AdRequest.Builder().build();
//        mADMOBInterstitialAd.setAdListener(new AdListener(){
//
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                LoggerManager.e("mun", "front banner onAdLoaded");
//            }
//
//            @Override
//            public void onAdFailedToLoad(int i) {
//                super.onAdFailedToLoad(i);
//
//                if(AdRequest.ERROR_CODE_NO_FILL == i){
//                }
//                LoggerManager.e("mun", "front banner onAdFailedToLoad "+ i);
//            }
//        });
//        mADMOBInterstitialAd.loadAd(mAdRequest);

        if(MainActivity.frontAdCount < 0) {
            MainActivity.frontAdCount = 0;
        }
        if(MainActivity.frontAdCount > MainActivity.frontAdMaxCount){
            MainActivity.frontAdCount = 0;
        }


    }
    ValueEventListener infoListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot!=null && dataSnapshot.getValue()!=null){

                HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
                FeedData d = Parser.getFeedDataParse(mFeedData.getKey(),data);

                if(d == null)
                    return;

                if(mFeedData == null) {
                    mFeedData = d;
                }
                else{
                    mFeedData.setUserName(d.getUserName());
                    mFeedData.setUid(d.getUid());
                    mFeedData.setpImg(d.getpImg());
                    mFeedData.setText(d.getText());
                    mFeedData.setTitle(d.getTitle());
                    mFeedData.setUserName(d.getUserName());
                    mFeedData.setTime(d.getTime());
                    mFeedData.setColor(d.getColor());
                    mFeedData.setuCount(d.getuCount());
                    mFeedData.setCate(d.getCate());
                    mFeedData.setOpen(d.getOpen());
                    mFeedData.setUc(d.getUc());
                }
                try {
                    for (Fragment f : mFragmentAppCompatManager.childFragments()) {
                        try {
                            if (f instanceof BaseRoomFragment) {
                                ((BaseRoomFragment) f).setFeedData(mFeedData);
                                ((BaseRoomFragment) f).setFeedDataChange();
                            }
                        } catch (Exception e) {
                        }
                    }
                }catch (Exception e){}
            }
            else{
                Toast.makeText(getActivity(), R.string.jam_remove_room ,Toast.LENGTH_SHORT).show();

                // (update3) 폐쇄된잼 제거하기
                Fire.getReference().child(Fire.KEY_MY_ROOM).child(MainActivity.mUserData.getUid()).child(mFeedData.getKey()).removeValue();
                try {
                    int count = 0;
                    for(FeedData f :mListData){
                        if(f.getKey().equals(mFeedData.getKey())) {
                            mListData.remove(f);
                            mAdapter.notifyItemRemoved(count);
                            break;
                        }
                        count++;
                    }
                }catch (Exception e){}
                // (update3) 폐쇄된잼 제거하기

                finish();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    ChildEventListener userListListener = new ChildEventListener() {
        Object o = new Object();
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            synchronized (o) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
                    final RoomUserData d = Parser.getRoomUserDataParse(dataSnapshot.getKey(), data);

                    if (d == null || TextUtils.isEmpty(d.getKey()) || TextUtils.isEmpty(d.getUid()))
                        return;

                    if (mFeedData.getListRoomUserData() == null) {
                        mFeedData.setListRoomUserData(new ArrayList<RoomUserData>());
                    }

                    boolean isIn = false;
                    for(RoomUserData u :mFeedData.getListRoomUserData()){
                        if(u.getUid().equals(d.getUid())){
                            u.clearData();
                            u.setHashMap(d.getHashMap());
                            isIn = true;
                            break;
                        }
                    }

                    if(!isIn){
                        mFeedData.getListRoomUserData().add(d);
                    }


                    try {
                        for (Fragment f : mFragmentAppCompatManager.childFragments()) {
                            try {
                                if (f instanceof BaseRoomFragment) {
                                    ((BaseRoomFragment) f).setFeedData(mFeedData);
                                    ((BaseRoomFragment) f).setFeedDataChange();
                                }
                            } catch (Exception e) {
                            }
                        }
                    } catch (Exception e) {
                    }


                    try {
                        LoggerManager.e("mun", "onChildAdded : " + d.getUid() + " / " + d.getUserName());

                        UserData user = DBManager.createInstnace(getActivity()).getUserPushEDate(d.getUid());
                        if (d.getOpen() == 1 && (user == null || TextUtils.isEmpty(user.getUid()) || TextUtils.isEmpty(user.getPush()) || user.getTime() < Fire.getServerTimestamp() - 1000 * 60 * 60 * 12)) {
                            LoggerManager.e("mun", "add DB fire ...");
                            Fire.getReference().child(Fire.KEY_USER).child(d.getUid()).child("push").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    try {
                                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                            final String push2 = (String) dataSnapshot.getValue(true);
                                            if (!TextUtils.isEmpty(push2)) {
                                                LoggerManager.e("mun", "add DB : " + d.getUid() + " / " + push2);
                                                DBManager.createInstnace(getActivity()).addUserPush(d.getUid(), push2);
                                            }
                                        }
                                    } catch (Exception e) {
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }catch (Exception e){}
                }
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            synchronized (o) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
                    RoomUserData d = Parser.getRoomUserDataParse(dataSnapshot.getKey(), data);

                    if (d == null || TextUtils.isEmpty(d.getKey()) || TextUtils.isEmpty(d.getUid()))
                        return;

                    if (mFeedData.getListRoomUserData() == null) {
                        mFeedData.setListRoomUserData(new ArrayList<RoomUserData>());
                    }

                    boolean isIn = false;
                    for(RoomUserData u :mFeedData.getListRoomUserData()){
                        if(u.getUid().equals(d.getUid())){
                            u.clearData();
                            u.setHashMap(d.getHashMap());
                            isIn = true;
                            break;
                        }
                    }

                    if(!isIn){
                        mFeedData.getListRoomUserData().add(d);
                    }


                    try {
                        for (Fragment f : mFragmentAppCompatManager.childFragments()) {
                            try {
                                if (f instanceof BaseRoomFragment) {
                                    ((BaseRoomFragment) f).setFeedData(mFeedData);
                                    ((BaseRoomFragment) f).setFeedDataChange();
                                }
                            } catch (Exception e) {
                            }
                        }
                    } catch (Exception e) {
                    }

                    LoggerManager.e("mun", "onChildChanged : " + d.getUid() + " / " + d.getUserName());
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            synchronized (o) {
                if (dataSnapshot != null && !TextUtils.isEmpty(dataSnapshot.getKey())) {

                    if (mFeedData.getListRoomUserData() != null) {
                        for (RoomUserData u : mFeedData.getListRoomUserData()) {
                            if (u.getKey().equals(dataSnapshot.getKey())) {
                                mFeedData.getListRoomUserData().remove(u);
                                break;
                            }
                        }
                    }


                    try {
                        for (Fragment f : mFragmentAppCompatManager.childFragments()) {
                            try {
                                if (f instanceof BaseRoomFragment) {
                                    ((BaseRoomFragment) f).setFeedData(mFeedData);
                                    ((BaseRoomFragment) f).setFeedDataChange();
                                }
                            } catch (Exception e) {
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };



    public void setFireListener(FeedData mFeedData){
        this.mFeedData = mFeedData;
        try{
            Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).removeEventListener(infoListener);
        }catch (Exception e){}
        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).addValueEventListener(infoListener);

        try{
            Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).removeEventListener(userListListener);
        }catch (Exception e){}
        Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).addChildEventListener(userListListener);
    }

    public void setChangeFragment(android.support.v4.app.Fragment f){
        mFragmentAppCompatManager.addFragment(f, true);
    }
    public void setChangeFragmentTopAnimation(android.support.v4.app.Fragment f){
        mFragmentAppCompatManager.addFragmentTopAnimation(f);
    }

    public void removeFragment(){
        if(mFragmentAppCompatManager!=null)mFragmentAppCompatManager.popFragment(false);
    }
    public void setChatOpen(boolean removePre) {
//        if(removePre)removeFragment();

        RoomChatFragment m = new RoomChatFragment();
        m.setFeedData(mFeedData);
        if(removePre){
            mFragmentAppCompatManager.replaceFragment(m, false);
        }
        else
            mFragmentAppCompatManager.addFragment(m, true);
    }

    public void setChatOpenUserJoinAfter() {
        removeFragment();

        RoomChatFragment m = new RoomChatFragment();
        m.setFeedData(mFeedData);
        m.setJoin(true);
        mFragmentAppCompatManager.addFragment(m, true);
    }

    @Override
    public void onBackPressed() {
        if(mFragmentAppCompatManager!=null && mFragmentAppCompatManager.popFragment(true)){
            ;
        }
        else{

//            if(mADMOBInterstitialAd!=null && mADMOBInterstitialAd.isLoaded()){
//                mADMOBInterstitialAd.show();
//            }
//            else{
//                super.onBackPressed();
//            }

            if(MainActivity.frontAdCount == 0) {
                if (FBInterstitial.getInstance().setShow()) {
                    MainActivity.frontAdCount++;
                } else if (AdmobInterstial.getInstance().setShow()) {
                    MainActivity.frontAdCount++;
                }
            }
            else{
                MainActivity.frontAdCount++;
            }



            super.onBackPressed();


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).removeEventListener(infoListener);

        Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).removeEventListener(userListListener);

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


