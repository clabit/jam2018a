package com.novato.jam.ui.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.lib.hashtag.views.HashTagEditText;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.admob.Adinit;
import com.novato.jam.analytics.FirebaseAnalyticsLog;
import com.novato.jam.common.EndlessRecyclerOnScrollListener;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.PermissionOk;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.PushData;
import com.novato.jam.data.ReportData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.data.UserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.dialog.BottomProfileListDialog;
import com.novato.jam.dialog.BottomUserInfoDialog;
import com.novato.jam.dialog.CustomAlertDialog;
import com.novato.jam.dialog.CustomToast;
import com.novato.jam.dialog.ImageToast;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.http.GoogleDriveUploadAsyncTask;
import com.novato.jam.http.GoogleDriveUploadAudioAsyncTask;
import com.novato.jam.push.SendPushFCM;
import com.novato.jam.push.SendPushFCMNewChat;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.MakePictureCropActivity;
import com.novato.jam.ui.RoomInfoActivity;
import com.novato.jam.ui.RoomInfoModifyActivity;
import com.novato.jam.ui.ShowImageActivity;
import com.novato.jam.ui.adapter.ChatListReAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class RoomChatFragment extends BaseRoomFragment implements View.OnClickListener{

    final int REQUEST_RECORDING = 4001;

    final private String childkey = "chat";

    final static public String Action_RoomChatFragmentPrivate = "Action_RoomChatFragmentPrivate";


//    private String uid = "";
//    private String feedKey = "";
//    private String mTitle = "";

    private View mRootView;


    private EditText et_comment;
    private TextView btn_send;


    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout swipelayout;
    private RecyclerView mListView;
    private ArrayList<FeedData> mListData = new ArrayList<>();
    private ChatListReAdapter mAdapter;

    private EndlessRecyclerOnScrollListener mOnScrollListener;
    private boolean isGetData = false;
    private boolean isEndList = false;
    private long searchTime = -1;
    final private int LimitCount = 15;

    private long mLastTime = -1;
    private long mAddLastTime = -1;
    private long mUserLastTime = -1;
    private long mCreateTime = -1;
    private long mUserTime = -1;
    HashMap <String, Object> aa;//수정수정


    private Object dataListAddObject = new Object();

    private boolean isJoin;


//    private HashMap<String, UserData> mListUserData = new HashMap<>();


    private Handler mHandler = new Handler();

    private ArrayList<String> removeAudio = new ArrayList<>();
    private ArrayList<String> removeAudioMsg = new ArrayList<>();


    public boolean isJoin() {
        return isJoin;
    }

    public void setJoin(boolean join) {
        isJoin = join;
    }

    @Override
    public void setFeedDataChange(){
        try{
            ((TextView)mRootView.findViewById(R.id.tv_action_title)).setText(mFeedData.getTitle());
        }catch (Exception e){}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_chat, container, false);

        if(mFeedData.getCate()==11) { // admin만 공지 작성가능
            mRootView.findViewById(R.id.btn_more).setVisibility(View.GONE); // 공지사항의 ... 지우기

            if (MainActivity.isAdmin) {

            } else {
                mRootView.findViewById(R.id.chattext).setVisibility(View.GONE);
            }
        }

        mProgressDialog = new ProgressDialog(getActivity());
        mCreateTime = Fire.getServerTimestamp();


        try {
            if(mFeedData.getUc() == 0) {
                for (RoomUserData u : mFeedData.getListRoomUserData()) {
                    if (MainActivity.mUserData.getUid().equals(u.getUid())) {
                        mUserTime = u.getTime();
                        break;
                    }
                }
            }
            if(MainActivity.isAdminChat){
                mUserTime = -1;
            }
        }catch (Exception e){}

//        feedKey = mFeedData.getKey();
//        uid = mFeedData.getUid();
//        mTitle = mFeedData.getTitle();


        ((TextView)mRootView.findViewById(R.id.tv_action_title)).setText(mFeedData.getTitle());
        mRootView.findViewById(R.id.btn_more).setOnClickListener(this);

        swipelayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipelayout);
        swipelayout.setEnabled(false);
        swipelayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark));
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoggerManager.e("mun", "---------- onRefresh:");
                swipelayout.setRefreshing(false);
            }
        });

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);


        mListView = (RecyclerView) mRootView.findViewById(R.id.list);
        mListView.setLayoutManager(layoutManager);
//        mListView.setHasFixedSize(true);
//        mListView.getItemAnimator().setAddDuration(300);
//        mListView.getItemAnimator().setRemoveDuration(100);
        mOnScrollListener = new EndlessRecyclerOnScrollListener(mListView.getLayoutManager()) {
            @Override
            public void onLoadMore(int current_page) {
                LoggerManager.e("munx", "onLoadMore :" + current_page);

                addDataList(false);
            }
        };

        mListView.addOnScrollListener(mOnScrollListener);

        mAdapter = new ChatListReAdapter(this, mListData, mListView.getLayoutManager(), mListView);
        mAdapter.setCallback(new ChatListReAdapter.Callback() {
            @Override
            public void onClick(int position) {
                if(mListData!=null && mListData.size() > position){
                    try {
                        final FeedData item = mListData.get(position);

                        if (item != null && !TextUtils.isEmpty(item.getUid())) {

                            for (final RoomUserData u : mFeedData.getListRoomUserData()) {
                                if (u.getUid().equals(item.getUid())) {


                                    AdapterSpinner1 apdapter = new AdapterSpinner1(getActivity(), getResources().getStringArray(R.array.chat_room_send_msg_type));//폭탄보내기
                                    AdapterSpinner1 apdapter2 = new AdapterSpinner1(getActivity(), getResources().getStringArray(R.array.chat_room_send_msg_normaltype));//(update3)

                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());

                                    if(mFeedData.getUc() == 0) { //(update3) 폭탄방에서만 폭탄,하트 보내기 기능 표시
                                        alertBuilder.setAdapter(apdapter, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which == 0) {
                                                    BottomUserInfoDialog mBottomSheetDialog = new BottomUserInfoDialog(getActivity(), u);
                                                    mBottomSheetDialog.show();
                                                }
                                                if (which == 1) {
                                                    sendPrivateMessage(item);
                                                } else if (which == 2) {

                                                    try {
                                                        long fff = MyPreferences.getLong(getActivity(), MyPreferences.KEY_ROOM_HEART);
                                                        if (fff > Fire.getServerTimestamp() - (1000 * 60 * 15)) {
                                                            String t = String.format(getActivity().getString(R.string.jam_promotion_err), ((fff - (Fire.getServerTimestamp() - (1000 * 60 * 15))) / 1000) + "");
                                                            CustomToast.showToast(getActivity(), t, Toast.LENGTH_SHORT);
                                                            return;
                                                        }
                                                    } catch (Exception e) {
                                                    }


                                                    sendHeart(item.getUid(), u.getUserName(), ChatListReAdapter.TYPE_CATE_HEART);
                                                } else if (which == 3) {

                                                    try {
                                                        long fff = MyPreferences.getLong(getActivity(), MyPreferences.KEY_ROOM_HEART);
                                                        if (fff > Fire.getServerTimestamp() - (1000 * 60 * 15)) {
                                                            String t = String.format(getActivity().getString(R.string.jam_promotion_err), ((fff - (Fire.getServerTimestamp() - (1000 * 60 *15))) / 1000) + "");
                                                            CustomToast.showToast(getActivity(), t, Toast.LENGTH_SHORT);
                                                            return;
                                                        }
                                                    } catch (Exception e) {
                                                    }

                                                    sendHeart(item.getUid(), u.getUserName(), ChatListReAdapter.TYPE_CATE_BOOM);
                                                }
                                            }
                                        });
                                        alertBuilder.show();
                                    }else{ //(update3) 일반방에서 보이는 부분 프로필과 비밀쪽지 보내기만 보이게 하기         정식 폭탄 출시시 삭제해야함.
                                        alertBuilder.setAdapter(apdapter2, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which == 0) {
                                                    BottomUserInfoDialog mBottomSheetDialog = new BottomUserInfoDialog(getActivity(), u);
                                                    mBottomSheetDialog.show();
                                                }
                                                if (which == 1) {
                                                    sendPrivateMessage(item);
                                                }

                                            }
                                        });
                                        alertBuilder.show();
                                    }
                                    break;
                                }
                            }
                        }
                    }catch (Exception e){}
                }
            }
            @Override
            public void onLongClick(int position) {
                if(mListData!=null && mListData.size() > position) {
                    final FeedData item = mListData.get(position);

                    sendPrivateMessage(item);
                }

            }

            @Override
            public void onRemoveClick(int position, final FeedData item) {
                try {
//                    if (item.getUid().equals(MainActivity.mUserData.getUid())) {
//                        CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(mListView.getContext(), mListView.getContext().getString(R.string.notice), mListView.getContext().getString(R.string.chat_remove_desc), new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
//                            @Override
//                            public void onClickOk() {
//                                try {
//                                    if (!TextUtils.isEmpty(mFeedData.getKey()) && !TextUtils.isEmpty(item.getKey())) {
//                                        Fire.getReference().child(childkey).child(mFeedData.getKey()).child(item.getKey()).removeValue();
//
//                                        synchronized (dataListAddObject) {
//                                            boolean is = false;
//                                            try {
//                                                for (FeedData info : mListData) {
//                                                    if (item.getKey().equals(info.getKey())) {
//                                                        is = true;
//                                                        mListData.remove(info);
//                                                        break;
//                                                    }
//                                                }
//                                            }catch (Exception e){
//                                            }
//                                            try {
//
//                                                if (is) if (mAdapter != null) mAdapter.notifyDataSetChanged();
//                                            }catch (Exception e){}
//
//                                        }
//
//                                    }
//                                }catch (Exception e){}
//                            }
//
//                            @Override
//                            public void onClickCancel() {
//
//                            }
//                        });
//                        mCustomAlertDialog.show();
//                    }
//                    else
                    {
                        Intent ss = new Intent(getActivity(), ShowImageActivity.class);
                        ss.putExtra("url", item.getImg());
                        startActivity(ss);
                    }
                }catch (Exception e){}
            }
        });
        mListView.setAdapter(mAdapter);




        et_comment = (EditText) mRootView.findViewById(R.id.et_comment);
        et_comment.requestFocus();
        et_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                int count = s.toString().trim().length();
                if (count > 0) {
                    if(btn_send!=null)btn_send.setEnabled(true);
                } else {
                    if(btn_send!=null)btn_send.setEnabled(false);
                }
            }
        });
  /*      et_comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                }
                return true;
            }
        });*/

        btn_send = (TextView) mRootView.findViewById(R.id.btn_send);
        btn_send.setEnabled(false);
        btn_send.setOnClickListener(this);

        addDataList(true);

        mRootView.findViewById(R.id.btn_add).setOnClickListener(this);

        mRootView.findViewById(R.id.btn_send).setEnabled(false);
        mRootView.findViewById(R.id.btn_add).setEnabled(false);

        Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).child("userName").addListenerForSingleValueEvent(mNameValueEventListener);
        Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).child("open").addValueEventListener(mOpenValueEventListener);


        MainActivity.mCurrentRoomId = mFeedData.getKey();


        //admob
        if(android.os.Build.VERSION.SDK_INT >= 9){
            FrameLayout lay02 = (FrameLayout)mRootView.findViewById(R.id.lay_ad);
            com.google.android.gms.ads.AdView adView22 = new com.google.android.gms.ads.AdView(getActivity());
            adView22.setAdSize(AdSize.SMART_BANNER);
            adView22.setAdUnitId(Adinit.getInstance().getTopBannerId());
            lay02.addView(adView22, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
            adView22.loadAd(adRequest);
        }



        try {
            DBManager.createInstnace(getActivity()).removeLauncherBadge(mFeedData.getKey());
            long t = DBManager.createInstnace(getActivity()).getLauncherBadgeToal();
            Utils.setBadgeCount(getActivity(), t);
        }catch (Exception e){}



        IntentFilter intent_filter = new IntentFilter();
        intent_filter.addAction(Action_RoomChatFragmentPrivate);
        getActivity().registerReceiver(privateBroadcastReceiver, intent_filter);


        if(MainActivity.isAdmin && MainActivity.isAdminChat){
            mRootView.findViewById(R.id.lay_edit).setVisibility(View.GONE);
            mRootView.findViewById(R.id.btn_more).setVisibility(View.GONE);
            mRootView.findViewById(R.id.btn_kakao).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.btn_kakao).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mProgressDialog!=null)mProgressDialog.show();
                    Utils.sendKakaoLink(getActivity(), mFeedData, new ResponseCallback<KakaoLinkResponse>() {
                        @Override
                        public void onFailure(ErrorResult errorResult) {
//                Logger.e(errorResult.toString());
                            if(mProgressDialog!=null)mProgressDialog.dismiss();
                        }

                        @Override
                        public void onSuccess(KakaoLinkResponse result) {
                            if(mProgressDialog!=null)mProgressDialog.dismiss();
                        }
                    });
                }
            });


        }
        return mRootView;
    }

    BroadcastReceiver privateBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if(arg1!=null && arg1.getParcelableExtra("private") !=null){
                PushData privateData = arg1.getParcelableExtra("private");
                if(privateData!=null && !TextUtils.isEmpty(privateData.getRoom()) && mFeedData!=null && privateData.getRoom().equals(mFeedData.getKey())){

                    CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getContext(), false, privateData.getTitle(), privateData.getMsg(), new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                        @Override
                        public void onClickOk() {
                        }
                        @Override
                        public void onClickCancel() {
                        }
                    });
                    mCustomAlertDialog.show();


                }
            }
        }
    };

    ValueEventListener mNameValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot!=null && dataSnapshot.getValue() !=null){
//                myName = dataSnapshot.getValue() +"";
//
//                try {
//                    for (RoomUserData u : mFeedData.getListRoomUserData()) {
//                        if (u.getUid().equals(MainActivity.mUserData.getUid())) {
//                            u.setUserName(myName);
//                            break;
//                        }
//                    }
//                }catch (Exception e){}
                mRootView.findViewById(R.id.btn_send).setEnabled(true);
                mRootView.findViewById(R.id.btn_add).setEnabled(true);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    ValueEventListener mOpenValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot!=null && dataSnapshot.getValue() !=null){
                LoggerManager.e("mun", "추가" + dataSnapshot.getKey());

                if("-2".equals(dataSnapshot.getValue() + "")){
                    //강퇴처리
                    mRootView.findViewById(R.id.btn_send).setEnabled(false);
                    mRootView.findViewById(R.id.btn_add).setEnabled(false);
                    reqWriteBan();

                    CustomToast.showToast(getActivity(), R.string.join_ban, Toast.LENGTH_SHORT);

                    if(!MainActivity.isAdmin)
                        getActivity().finish();
                }
            }
            else{
                LoggerManager.e("mun", "제거됨");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    private void sendMessage(){ //줄바꿈 기능 추가
        String message = et_comment.getText().toString().trim().replaceAll("\\n", "<br />");
        if (TextUtils.isEmpty(message)) {
            return;
        }

        try {
            if(mFeedData.getOpen() == 1) {
                String xx = Utils.isBlockWord(message, getActivity().getResources().getStringArray(R.array.block_text));
                if (!TextUtils.isEmpty(xx)) {
                    CustomToast.showToast(getActivity(), String.format(getActivity().getString(R.string.block_text_err), xx), Toast.LENGTH_SHORT);
                    return;
                }
            }
        }catch (Exception e){}





        reqWriteComment(message);

        et_comment.setText("");
//                View view = this.getCurrentFocus();
//                if (view != null) {
//                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }

        sendChatPushBadge();
    }

    private void sendHeart(String uid, String youName, int type) {
        String color = null;
        String myName = "";
        try {
            for (RoomUserData u : mFeedData.getListRoomUserData()) {
                LoggerManager.e("munx", u.getUid() + " / " + u.getUserName());
                if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                    myName = u.getUserName();
                    color = u.getColor();
                    break;
                }
            }
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(myName)) {
            CustomToast.showToast(getActivity(), "User Name err", Toast.LENGTH_SHORT);
            return;
        }

        final FeedData f = new FeedData();
        f.setCate(type);
        f.setUserName(myName);
        f.setIuid(uid);
//        f.setpImg(MainActivity.mUserData.getpImg());
        f.setUid(MainActivity.mUserData.getUid());

        if (type == ChatListReAdapter.TYPE_CATE_HEART) {
            f.setText(myName + "님께서 " + youName + "님에게 하트를 보냈습니다.");

        } else if(type == ChatListReAdapter.TYPE_CATE_BOOM) {
            f.setText(youName + "님이 폭탄을 받으셨습니다.");//맞춤법

        }
        f.setTime(Fire.getServerTimestamp());

        if(!TextUtils.isEmpty(color))f.setColor(color) ;

            Fire.getReference().child(childkey).child(mFeedData.getKey()).push().setValue(f.getHashMap());


            Fire.getReference().child(Fire.KEY_CHAT_COUNT).child(mFeedData.getKey()).child("chatCount").runTransaction(setCountTransactionStep1());


            FirebaseAnalyticsLog.setChatSend(getActivity(), "heart");


        try {
            MyPreferences.set(getActivity(), MyPreferences.KEY_ROOM_HEART, Fire.getServerTimestamp());
        }catch (Exception e){}

        sendChatPushBadge();
    }

    private void sendAudio(final String drive_id){
        {
            String color = null;
            String myName = "";
            try {
                for (RoomUserData u : mFeedData.getListRoomUserData()) {
                    LoggerManager.e("munx", u.getUid() + " / "+ u.getUserName());
                    if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                        myName = u.getUserName();
                        color = u.getColor();
                        break;
                    }
                }
            }catch (Exception e){}
            if(TextUtils.isEmpty(myName)){
                CustomToast.showToast(getActivity(), "User Name err",Toast.LENGTH_SHORT);
                return;
            }

            final FeedData f = new FeedData();
            f.setUserName(myName);
//        f.setpImg(MainActivity.mUserData.getpImg());
            f.setUid(MainActivity.mUserData.getUid());

            f.setText("");
            f.setTime(Fire.getServerTimestamp());
            f.setMic(drive_id);

            if(!TextUtils.isEmpty(color))f.setColor(color);

            Fire.getReference().child(childkey).child(mFeedData.getKey()).push().setValue(f.getHashMap(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError == null && !TextUtils.isEmpty(databaseReference.getKey())){
                        String key = databaseReference.getKey();
                        HashMap p = new HashMap();
                        p.put("d", drive_id);
                        Fire.getReference().child(Fire.KEY_ROOM_AUDIO).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).child(key).setValue(p);
                    }
                }
            });

//
            Fire.getReference().child(Fire.KEY_CHAT_COUNT).child(mFeedData.getKey()).child("chatCount").runTransaction(setCountTransactionStep1());


            FirebaseAnalyticsLog.setChatSend(getActivity(), "audio");

        }
        sendChatPushBadge();
    }

    private void sendImage(final String img){

        if(TextUtils.isEmpty(img)){
            return;
        }

        {
            String color = null;
            String myName = "";
            try {
                for (RoomUserData u : mFeedData.getListRoomUserData()) {
                    LoggerManager.e("munx", u.getUid() + " / "+ u.getUserName());
                    if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                        myName = u.getUserName();
                        color = u.getColor();
                        break;
                    }
                }
            }catch (Exception e){}
            if(TextUtils.isEmpty(myName)){
                CustomToast.showToast(getActivity(), "User Name err",Toast.LENGTH_SHORT);
                return;
            }

            final FeedData f = new FeedData();
            f.setUserName(myName);
//        f.setpImg(MainActivity.mUserData.getpImg());
            f.setUid(MainActivity.mUserData.getUid());

            f.setText("");
            f.setTime(Fire.getServerTimestamp());
            f.setImg(img);

            if(!TextUtils.isEmpty(color))f.setColor(color);

            Fire.getReference().child(childkey).child(mFeedData.getKey()).push().setValue(f.getHashMap(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError == null && !TextUtils.isEmpty(databaseReference.getKey())){
                        String key = databaseReference.getKey();
                        HashMap p = new HashMap();
                        p.put("d", img);
                        Fire.getReference().child(Fire.KEY_ROOM_IMAGE).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).child(key).setValue(p);
                    }
                }
            });


            Fire.getReference().child(Fire.KEY_CHAT_COUNT).child(mFeedData.getKey()).child("chatCount").runTransaction(setCountTransactionStep1());


            FirebaseAnalyticsLog.setChatSend(getActivity(), "image");

        }
        sendChatPushBadge();
    }

    private void sendChatPushBadge(){
        try{
            GlobalApplication.runBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        ArrayList<String> list = new ArrayList<String>();

                        for(RoomUserData u:mFeedData.getListRoomUserData()){
                            if(u.getOpen() == 1) {

                                String pu = DBManager.createInstnace(getActivity()).getUserPush(u.getUid());
//                                if(mListUserData!=null && mListUserData.get(u.getUid()) !=null && !TextUtils.isEmpty(mListUserData.get(u.getUid()).getPush()) ){
//                                    pu = mListUserData.get(u.getUid()).getPush();
//                                }

                                if(!TextUtils.isEmpty(pu)){
                                    list.add(pu);

                                    LoggerManager.e("mun","chat push : " + u.getUid() + " / " + pu);
                                }
                                else{
                                    LoggerManager.e("mun","chat push : " + u.getUid() + " / no push");
                                }
                            }
                        }

                        String re = new SendPushFCMNewChat(getActivity(), list, mFeedData.getKey()).start();
                    }catch (Exception e){}
                }
            });
        }catch (Exception e){}
    }


    private void sendPrivateMessage(final FeedData item){
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.dialog_private_msg, null);

        final TextView cu = view.findViewById(R.id.et_desc);


        android.support.v7.app.AlertDialog.Builder alert_confirm = new android.support.v7.app.AlertDialog.Builder(getActivity());
        if(mFeedData.getCate()==11){

        }else {
        alert_confirm.setTitle(R.string.private_msg_title)
                .setView(view)
                .setPositiveButton(R.string.btn_send,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                try {
                                    final String msg = cu.getText().toString();
                                    if (!TextUtils.isEmpty(msg)) {
                                        GlobalApplication.runBackground(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {

                                                    String myName = "";
                                                    try {
                                                        for (RoomUserData u : mFeedData.getListRoomUserData()) {
                                                            LoggerManager.e("munx", u.getUid() + " / " + u.getUserName());
                                                            if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                                                                myName = u.getUserName();

                                                                if(!TextUtils.isEmpty(myName))
                                                                    break;
                                                            }

                                                        }
                                                    } catch (Exception e) {
                                                    }

                                                    String push = null;

                                                    try {
                                                        push = DBManager.createInstnace(getActivity()).getUserPush(item.getUid());
//                                                        UserData u = mListUserData.get(item.getUid());
//                                                        if (!TextUtils.isEmpty(u.getPush())) {
//                                                            push = u.getPush();
//                                                        }
                                                    }catch (Exception e){}


                                                    if (TextUtils.isEmpty(myName)) {
                                                        return;
                                                    }

                                                    if(TextUtils.isEmpty(push)){
                                                        LoggerManager.e("mun", "message to "+item.getUid()+" / getFirebase push......");
                                                        final String myName2 = myName;
                                                        Fire.getReference().child(Fire.KEY_USER).child(item.getUid()).child("push").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                try{

                                                                    if(dataSnapshot!=null && dataSnapshot.getValue() != null) {

                                                                        final String push2 = (String)dataSnapshot.getValue(true);

                                                                        GlobalApplication.runBackground(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                try{
                                                                                    UserData y = new UserData();
                                                                                    y.setUid(item.getUid());
                                                                                    y.setPush(push2);
//                                                                                    mListUserData.put(item.getUid(), y);
                                                                                }catch (Exception e){}

                                                                                if(!TextUtils.isEmpty(push2)) {
                                                                                    DBManager.createInstnace(getActivity()).addUserPush(item.getUid(), push2);


                                                                                    ArrayList<String> list = new ArrayList<String>();
                                                                                    list.add(push2);
                                                                                    String re = new SendPushFCM(getActivity(), list, myName2, msg, mFeedData.getKey(), mFeedData.getTitle()).start();
                                                                                }

                                                                            }
                                                                        });


                                                                    }


                                                                }catch (Exception e){}
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                            }
                                                        });
                                                    }
                                                    else {
                                                        LoggerManager.e("mun", "message to "+item.getUid()+" / "+ push);

                                                        ArrayList<String> list = new ArrayList<String>();
                                                        list.add(push);
                                                        String re = new SendPushFCM(getActivity(), list, myName, msg, mFeedData.getKey(), mFeedData.getTitle()).start();
                                                    }
                                                } catch (Exception e) {
                                                }
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

        android.support.v7.app.AlertDialog alert = alert_confirm.create();
        alert.show();}
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()){
            case R.id.btn_send:{
                sendMessage();
                break;
            }
            case R.id.btn_add:{


                LoggerManager.e("mun", "btn_add");

                AdapterSpinner1 apdapter = new AdapterSpinner1(getActivity(), getResources().getStringArray(R.array.chat_room_add_file));
                final ListPopupWindow popup = new ListPopupWindow(getActivity());
                popup.setAnchorView(mRootView.findViewById(R.id.btn_add));
                popup.setModal(true);
                popup.setWidth(apdapter.measureContentWidth());
                popup.setAdapter(apdapter);
                popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_white_background));
                popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        popup.dismiss();

                        if(position == 0){
                            if (PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.GET_ACCOUNTS)
                                    && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.RECORD_AUDIO)
                                    ) {
                                File f = GlobalApplication.getProfileFile(getActivity());

                                String filepath = f.getAbsolutePath() + "/jam_audio.wav";

                                f = new File(filepath);
                                try {
                                    if (f.exists()) {
                                        f.delete();
                                    }
                                } catch (Exception e) {
                                }

                                int color = getResources().getColor(R.color.colorPrimaryDark);

                                mMyAudioPath = "";
                                AndroidAudioRecorder.with(RoomChatFragment.this)
                                        // Required
                                        .setFilePath(f.getAbsolutePath())
                                        .setColor(color)
                                        .setRequestCode(REQUEST_RECORDING)
                                        // Optional
                                        .setSource(AudioSource.MIC)
                                        .setChannel(AudioChannel.STEREO)
                                        .setSampleRate(AudioSampleRate.HZ_48000)
                                        .setAutoStart(false)
                                        .setKeepDisplayOn(true)
                                        .setMaxSeconded(15)
                                        .recordFromFragment();
                            } else {
                                PermissionOk.checkPermission2(getActivity(), MY_PERMISSIONS_REQUEST_READ_CONTACTS, new PermissionOk.Callback() {
                                    @Override
                                    public void OnFail(final Runnable run) {
                                        run.run();
                                    }

                                    @Override
                                    public void OnOk() {
                                    }
                                });
                            }
                        }
                        else if(position == 1){
                            if (PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.GET_ACCOUNTS)
                                    && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.RECORD_AUDIO)
                                    ) {
                                checkGoogleAccountGeller();
                            } else {
                                PermissionOk.checkPermission2(getActivity(), MY_PERMISSIONS_REQUEST_READ_CONTACTS, new PermissionOk.Callback() {
                                    @Override
                                    public void OnFail(final Runnable run) {
//                            new android.support.v7.app.AlertDialog.Builder(getActivity())
//                                    .setTitle(R.string.notice)
//                                    .setMessage(R.string.permission_file_err)
//                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
//
//                                            run.run();
//                                        }
//                                    })
//                                    .create().show();

                                        run.run();
                                    }

                                    @Override
                                    public void OnOk() {
                                        checkGoogleAccountGeller();
                                    }
                                });
                            }
                        }

                    }
                });

                popup.show();

                break;
            }
            case R.id.btn_more:{
                int reArray;
                if(MainActivity.mUserData!=null && mFeedData.getUid()!=null && mFeedData.getUid().equals(MainActivity.mUserData.getUid())){
                    reArray = R.array.chat_room_option_admin;
                }
                else {
                    reArray = R.array.chat_room_option;
                }

                AdapterSpinner1 apdapter = null;

                apdapter = new AdapterSpinner1(getActivity(), getResources().getStringArray(reArray));
                final ListPopupWindow popup = new ListPopupWindow(getActivity());
                popup.setAnchorView(mRootView.findViewById(R.id.btn_more));
                popup.setModal(true);
                popup.setWidth(apdapter.measureContentWidth());
                popup.setAdapter(apdapter);
                popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_white_background));
                popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        switch (i){
                            case 0:{
                                if(mProgressDialog!=null)mProgressDialog.show();
                                Utils.sendKakaoLink(getActivity(), mFeedData, new ResponseCallback<KakaoLinkResponse>() {
                                    @Override
                                    public void onFailure(ErrorResult errorResult) {
//                Logger.e(errorResult.toString());
                                        if(mProgressDialog!=null)mProgressDialog.dismiss();
                                    }

                                    @Override
                                    public void onSuccess(KakaoLinkResponse result) {
                                        if(mProgressDialog!=null)mProgressDialog.dismiss();
                                    }
                                });
                                break;
                            }
                            case 1:{
                                if(getActivity() instanceof RoomInfoActivity){
                                    RoomModifyFragment mRoomModifyFragment = new RoomModifyFragment();
                                    mRoomModifyFragment.setFeedData(mFeedData);
                                    ((RoomInfoActivity)getActivity()).setChangeFragment(mRoomModifyFragment);
                                }
                                break;
                            }
                            case 2:{
                                if(getActivity() instanceof RoomInfoActivity){
                                    RoomMemberFragment mRoomMemberFragment = new RoomMemberFragment();
                                    mRoomMemberFragment.setFeedData(mFeedData);
                                    ((RoomInfoActivity)getActivity()).setChangeFragment(mRoomMemberFragment);
                                }

                                break;
                            }
                            case 3:{

                                if(mFeedData.getUid().equals(MainActivity.mUserData.getUid()) && mFeedData.getListRoomUserData() !=null && mFeedData.getListRoomUserData().size() > 1){
                                    //방장일때 나가는거 처리를 해야하나???
                                    CustomToast.showToast(getActivity(), R.string.jam_out_err,Toast.LENGTH_SHORT);
                                }
                                else {
                                    //유저일때 방나감
                                    CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getContext(), R.string.jam_out_title, R.string.jam_out_desc, new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                                        @Override
                                        public void onClickOk() {

                                            Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).removeValue();
                                            Fire.getReference().child(Fire.KEY_MY_ROOM).child(MainActivity.mUserData.getUid()).child(mFeedData.getKey()).removeValue();


                                            try {
                                                getActivity().setResult(getActivity().RESULT_OK);
                                            }catch (Exception e){}

                                            removeAudioList(MainActivity.mUserData.getUid());

                                            reqWriteUserOut();


                                            if(mFeedData.getUid().equals(MainActivity.mUserData.getUid())) {
                                                //폐쇄처리
                                                Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).removeValue();
                                                Fire.getReference().child(Fire.KEY_CHAT_ROOM).child(mFeedData.getCate() + "").child(mFeedData.getKey()).removeValue();
                                                Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("-99").child(mFeedData.getKey()).removeValue();


                                                try {
                                                    Fire.getReference().child(Fire.KEY_ROOM_USERCOUNT).child(mFeedData.getKey()).removeValue();
                                                } catch (Exception e) {
                                                }

                                                try {
                                                    Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).removeValue();
                                                } catch (Exception e) {
                                                }
                                                try {
                                                    Fire.getReference().child(Fire.KEY_ROOM_AUDIO).child(mFeedData.getKey()).removeValue();
                                                }catch (Exception e) {
                                                }
                                                try {
                                                    Fire.getReference().child(Fire.KEY_ROOM_IMAGE).child(mFeedData.getKey()).removeValue();
                                                }catch (Exception e) {
                                                }
                                                try {
                                                    Fire.getReference().child(Fire.KEY_STORY).child(mFeedData.getKey()).removeValue();
                                                }catch (Exception e) {
                                                }
                                                try {
                                                    Fire.getReference().child(Fire.KEY_STORY_COMMENT).child(mFeedData.getKey()).removeValue();
                                                }catch (Exception e) {
                                                }

                                                try{
                                                    HashTagEditText m = new HashTagEditText(getActivity());
                                                    m.setText(mFeedData.getText());
                                                    List<String> originTags = m.getHashTags();
                                                    if(originTags!=null){
                                                        for(String t :originTags){
                                                            if(!TextUtils.isEmpty(t))Fire.getReference().child(Fire.KEY_TAGS).child(t).child(mFeedData.getKey()).removeValue();
                                                        }
                                                    }
                                                }catch (Exception e){}


                                            }

                                            getActivity().finish();
                                        }

                                        @Override
                                        public void onClickCancel() {
                                        }
                                    });
                                    mCustomAlertDialog.show();
                                }


                                break;
                            }
                            case 4:{
                                if(MainActivity.mUserData!=null && mFeedData.getUid()!=null && mFeedData.getUid().equals(MainActivity.mUserData.getUid())) {
                                    CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getContext(), R.string.jam_remove_title, R.string.jam_remove_desc, new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                                        @Override
                                        public void onClickOk() {

                                            try {
                                                for (RoomUserData u : mFeedData.getListRoomUserData()) {
//                                                    Fire.getReference().child(Fire.KEY_MY_ROOM).child(u.getUid()).child(mFeedData.getKey()).removeValue();
                                                    Fire.getReference().child(Fire.KEY_MY_ROOM).child(u.getUid()).child(mFeedData.getKey()).child("open").setValue(Fire.KEY_VALUSE_DESTROY_ROOM);
                                                }
                                            }catch (Exception e){}
                                            try {
                                                Fire.getReference().child(Fire.KEY_CHAT_ROOM).child(mFeedData.getCate() + "").child(mFeedData.getKey()).removeValue();
                                            }catch (Exception e){}

                                            reqRoomRemove();
                                            removeAudioList(MainActivity.mUserData.getUid());
                                            try {
                                                Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).removeValue();
                                            }catch (Exception e){}

                                            try{
                                                Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("-99").child(mFeedData.getKey()).removeValue();
                                            }catch (Exception e){}


                                            try {
                                                Fire.getReference().child(Fire.KEY_ROOM_USERCOUNT).child(mFeedData.getKey()).removeValue();
                                            } catch (Exception e) {
                                            }

                                            try {
                                                Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).removeValue();
                                            } catch (Exception e) {
                                            }

                                            try {
                                                Fire.getReference().child(Fire.KEY_ROOM_AUDIO).child(mFeedData.getKey()).removeValue();
                                            }catch (Exception e) {
                                            }

                                            try {
                                                Fire.getReference().child(Fire.KEY_ROOM_IMAGE).child(mFeedData.getKey()).removeValue();
                                            }catch (Exception e) {
                                            }

                                            try {
                                                Fire.getReference().child(Fire.KEY_STORY).child(mFeedData.getKey()).removeValue();
                                            }catch (Exception e) {
                                            }
                                            try {
                                                Fire.getReference().child(Fire.KEY_STORY_COMMENT).child(mFeedData.getKey()).removeValue();
                                            }catch (Exception e) {
                                            }


                                            try{
                                                HashTagEditText m = new HashTagEditText(getActivity());
                                                m.setText(mFeedData.getText());
                                                List<String> originTags = m.getHashTags();
                                                if(originTags!=null){
                                                    for(String t :originTags){
                                                        if(!TextUtils.isEmpty(t))Fire.getReference().child(Fire.KEY_TAGS).child(t).child(mFeedData.getKey()).removeValue();
                                                    }
                                                }
                                            }catch (Exception e){}



//                                            try {
//                                                Fire.getStorage().getReference().child(mFeedData.getKey()+"/"+mFeedData.getKey()+".jpg").delete();
//                                            }catch (Exception e){}

                                            try {
                                                getActivity().setResult(getActivity().RESULT_OK);
                                            } catch (Exception e) {
                                            }

                                            getActivity().finish();
                                        }

                                        @Override
                                        public void onClickCancel() {
                                        }
                                    });
                                    mCustomAlertDialog.show();
                                }
                                else{
                                    CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getContext(), R.string.jam_report_title, R.string.jam_report_desc, new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                                        @Override
                                        public void onClickOk() {
                                            try {
                                                Fire.getReference().child(Fire.KEY_REPORT_ROOM).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {


                                                        if(dataSnapshot == null || dataSnapshot.getValue() == null) {
                                                            ReportData mReportData = new ReportData();
                                                            mReportData.setTime(Fire.getServerTimestamp());

                                                            Fire.getReference().child(Fire.KEY_REPORT_ROOM).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).setValue(mReportData.getHashMap());
                                                            Fire.getReference().child(Fire.KEY_REPORTCount_ROOM).child(mFeedData.getKey()).runTransaction(setCountTransactionStep1());
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            }catch (Exception e){}

                                            try{
                                                GlobalApplication.runBackground(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            ArrayList<String> list = new ArrayList<String>();
                                                            list.add(MainActivity.admin_push);
                                                            String re = new SendPushFCM(getActivity(), list, "신고]"+mFeedData.getTitle(), mFeedData.getKey()).start();
                                                        }catch (Exception e){}
                                                    }
                                                });
                                            }catch (Exception e){}
                                        }

                                        @Override
                                        public void onClickCancel() {
                                        }
                                    });
                                    mCustomAlertDialog.show();
                                }

                                break;
                            }
                            case 5: {
                                if (MainActivity.mUserData != null && mFeedData.getUid() != null && mFeedData.getUid().equals(MainActivity.mUserData.getUid())) {
                                    CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getContext(), R.string.jam_promotion_title, R.string.jam_promotion_desc, new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                                        @Override
                                        public void onClickOk() {
                                            try {


                                                Fire.loadServerTime(new Fire.TimeCallback() {
                                                    @Override
                                                    public void timeOffet(long offset) {


                                                        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("-99").child(mFeedData.getKey()).child("time").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                FeedData d = new FeedData();
                                                                d.setUserName(mFeedData.getUserName());
                                                                d.setUid(mFeedData.getUid());
                                                                d.setpImg(mFeedData.getpImg());
                                                                d.setText(mFeedData.getText());
                                                                d.setTitle(mFeedData.getTitle());
                                                                d.setUserName(mFeedData.getUserName());
                                                                d.setTime(Fire.getServerTimestamp());
                                                                d.setColor(mFeedData.getColor());
                                                                d.setuCount(mFeedData.getuCount());
                                                                d.setCate(mFeedData.getCate());
                                                                d.setOpen(mFeedData.getOpen());


                                                                if(dataSnapshot == null || dataSnapshot.getValue() == null){
                                                                    Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("-99").child(mFeedData.getKey()).setValue(d.getHashMap());
                                                                }
                                                                else{
                                                                    try {
                                                                        long fff = Long.parseLong(dataSnapshot.getValue(true) + "");
                                                                        if (fff > d.getTime() - (1000 * 60 * 5)) {


                                                                            String t = String.format(getActivity().getString(R.string.jam_promotion_err), ((fff - (d.getTime() - (1000 * 60 * 5))) / 1000) + "");
                                                                            Toast.makeText(getActivity(), t, Toast.LENGTH_SHORT).show();
                                                                            return;
                                                                        }
                                                                        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("-99").child(mFeedData.getKey()).setValue(d.getHashMap());
                                                                    }catch (Exception e){}
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                                    }
                                                });






                                            }catch (Exception e){}
                                        }

                                        @Override
                                        public void onClickCancel() {
                                        }
                                    });
                                    mCustomAlertDialog.show();
                                }
                                break;
                            }
                            case 6: {
                                if (MainActivity.mUserData != null && mFeedData.getUid() != null && mFeedData.getUid().equals(MainActivity.mUserData.getUid())) {
                                    Intent zz = new Intent(getActivity(), RoomInfoModifyActivity.class);
                                    zz.putExtra("data",mFeedData);
                                    startActivity(zz);
                                }
                                break;
                            }
                        }
                        popup.dismiss();
                    }
                });
                popup.show();
                break;
            }
        }
    }


    private void reqWriteComment(final String comment) {
        String color = null;
        String myName = "";
        try {
            for (RoomUserData u : mFeedData.getListRoomUserData()) {
                LoggerManager.e("munx", u.getUid() + " / "+ u.getUserName());
                if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                    myName = u.getUserName();
                    color = u.getColor();
                    break;
                }
            }
        }catch (Exception e){}
        if(TextUtils.isEmpty(myName)){
            CustomToast.showToast(getActivity(), "User Name err",Toast.LENGTH_SHORT);
            return;
        }

        final FeedData f = new FeedData();
        f.setUserName(myName);
//        f.setpImg(MainActivity.mUserData.getpImg());
        f.setUid(MainActivity.mUserData.getUid());

        f.setText(comment);
        f.setTime(Fire.getServerTimestamp());

        if(!TextUtils.isEmpty(color))f.setColor(color);

        Fire.getReference().child(childkey).child(mFeedData.getKey()).push().setValue(f.getHashMap());


        Fire.getReference().child(Fire.KEY_CHAT_COUNT).child(mFeedData.getKey()).child("chatCount").runTransaction(setCountTransactionStep1());


        FirebaseAnalyticsLog.setChatSend(getActivity(), "text");
    }

    private void reqWriteBan() {//처리 하지 말자s
//        final FeedData f = new FeedData();
//        f.setUserName(myName);
//        f.setpImg(MainActivity.mUserData.getpImg());
//        f.setUid(MainActivity.mUserData.getUid());
//        f.setTime(Fire.getServerTimestamp());
//
//        f.setCate(ChatListReAdapter.TYPE_CATE_BAN);
//
//        Fire.getReference().child(Fire.KEY_CHAT_USER).child(feedKey).push().setValue(f.getHashMap());
    }

    private void reqRoomRemove(){
        Fire.getReference().child(childkey).child(mFeedData.getKey()).removeValue();
        Fire.getReference().child(Fire.KEY_CHAT_USER).child(mFeedData.getKey()).removeValue();


        final FeedData f = new FeedData();
        f.setUserName("");
//        f.setpImg(MainActivity.mUserData.getpImg());
        f.setUid(MainActivity.mUserData.getUid());
        f.setTime(Fire.getServerTimestamp());

        f.setCate(ChatListReAdapter.TYPE_CATE_ROOMREMOVE);
        Fire.getReference().child(Fire.KEY_CHAT_USER).child(mFeedData.getKey()).push().setValue(f.getHashMap());

    }


    private void reqWriteUserIn(){
        String color = null;
        String myName = "";
        try {
            for (RoomUserData u : mFeedData.getListRoomUserData()) {
                if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                    myName = u.getUserName();
                    color = u.getColor();
                    break;
                }
            }
        }catch (Exception e){}
        if(TextUtils.isEmpty(myName)){
            return;
        }

        final FeedData f = new FeedData();
        f.setUserName(myName);
//        f.setpImg(MainActivity.mUserData.getpImg());
        f.setUid(MainActivity.mUserData.getUid());
        f.setTime(Fire.getServerTimestamp());
        if(!TextUtils.isEmpty(color))f.setColor(color);

        f.setCate(ChatListReAdapter.TYPE_CATE_USERIN);

        Fire.getReference().child(Fire.KEY_CHAT_USER).child(mFeedData.getKey()).push().setValue(f.getHashMap());

    }
    private void reqWriteUserOut(){
        String color = null;
        String myName = "";
        try {
            for (RoomUserData u : mFeedData.getListRoomUserData()) {
                if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                    myName = u.getUserName();
                    color = u.getColor();
                    break;
                }
            }
        }catch (Exception e){}
        if(TextUtils.isEmpty(myName)){
            return;
        }

        final FeedData f = new FeedData();
        f.setUserName(myName);
//        f.setpImg(MainActivity.mUserData.getpImg());
        f.setUid(MainActivity.mUserData.getUid());
        f.setTime(Fire.getServerTimestamp());
        if(!TextUtils.isEmpty(color))f.setColor(color);

        f.setCate(ChatListReAdapter.TYPE_CATE_USEROUT);

        Fire.getReference().child(Fire.KEY_CHAT_USER).child(mFeedData.getKey()).push().setValue(f.getHashMap());
    }

    private Transaction.Handler setCountTransactionStep1(){
        return new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                try {

                    if (mutableData.getValue() == null) {
                        mutableData.setValue(1);
                        return Transaction.success(mutableData);
                    }

                    long p = (Long) mutableData.getValue();

                    if (p < 1) {
                        mutableData.setValue(1);
                        return Transaction.success(mutableData);
                    }
                    mutableData.setValue(p + 1);
                }catch (Exception e){}

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    LoggerManager.e("firebase", "type1 : " + databaseError.getCode() + " , " + databaseError.getMessage());
                }

            }
        };
    }


    synchronized public void addDataList(final boolean replace){

        if(TextUtils.isEmpty(mFeedData.getUid())){
            CustomToast.showToast(getActivity(), "User Id Err",Toast.LENGTH_SHORT);
            return;
        }
        if(isGetData){//먼저호출해서 불러오는중
            return;
        }

        if(!replace && isEndList){//더보기 할경우 더이상 불러올 데이터가 없다고 판단시 취소
            return;
        }



        Query mQuery = null;

        if(replace){
            mQuery = Fire.getReference().child(childkey).child(mFeedData.getKey()).orderByChild("time").limitToLast(LimitCount);
        }
        else{
            mQuery = Fire.getReference().child(childkey).child(mFeedData.getKey()).orderByChild("time").endAt(searchTime).limitToLast(LimitCount);
        }


        if(mQuery!=null) {
            isGetData = true;

            if(replace) {
                isEndList = false;
                searchTime = -1;
                if (mOnScrollListener != null) mOnScrollListener.setInit();
            }
            swipelayout.setRefreshing(true);

            mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataListAddObject == null)
                        return;

                    synchronized (dataListAddObject) {
                        if (replace) {
                            mListData.clear();
                            try {
                                Fire.getReference().child(childkey).child(mFeedData.getKey()).orderByChild("time").startAt(mLastTime).removeEventListener(mChildEventListener);
                            } catch (Exception e) {
                            }
                        }

                        swipelayout.setRefreshing(false);
                        isGetData = false;


                        if (dataSnapshot != null) {


                            ArrayList<FeedData> lists = Parser.getFeedDataListParse(dataSnapshot);
                            ArrayList<FeedData> list = new ArrayList<>();

                            if (lists == null) {
                                isEndList = true;
                            } else {

                                for (FeedData m : lists) {
                                    if (m.getTime() > 0) {

                                        try {
                                            for (RoomUserData u : mFeedData.getListRoomUserData()) {
                                                if (u.getUid().equals(m.getUid())) {
                                                    if (!TextUtils.isEmpty(u.getpImg()))
                                                        m.setpImg(u.getpImg());
                                                    break;
                                                }
                                            }
                                        }catch (Exception e){}


                                        if(mUserTime > m.getTime()){
                                            isEndList = true;
                                        }
                                        else if (!replace && searchTime > m.getTime()) {
                                            list.add(m);
                                        } else if (replace) {
                                            list.add(m);
                                        }
                                    }
                                }

                                Collections.sort(list, mComparatorNumber);

                                if (list.size() > 0) {
                                    FeedData d = list.get(list.size() - 1);
                                    searchTime = d.getTime();

                                    LoggerManager.e("mun", list.get(list.size() - 1).getTime() + " / " + list.get(0).getTime());
                                } else {
                                    isEndList = true;
                                }

                                if (replace) {
                                    if(list.size() > 0){
                                        mLastTime = list.get(0).getTime();
                                    }
                                    else{
                                        mLastTime = Fire.getServerTimestamp();
                                    }
                                }


//                                try {
//                                    if (mListData != null && mListData.size() <= 0) {
//                                        FeedData f = new FeedData();
//                                        f.setRowType(ChatListReAdapter.TYPE_ADMOB);
//
//                                        try{
//                                            f.setTime(list.get(0).getTime() + 1);
//                                        }catch (Exception e){}
//                                        list.add(0, f);
//                                    }
//                                } catch (Exception e) {
//                                }

                                if (mListData != null) {
                                    for (FeedData f : mListData) {
                                        if (f.getRowType() == ChatListReAdapter.TYPE_EMPTY) {
                                            mListData.remove(f);
                                            break;
                                        }
                                    }
                                }


                                mListData.addAll(list);

                                FeedData f = new FeedData();
                                f.setRowType(ChatListReAdapter.TYPE_EMPTY);
                                try{
                                    f.setTime(mListData.get(0).getTime() + 1);
                                }catch (Exception e){}
                                mListData.add(0, f);
                            }

                        } else {
                            isEndList = true;
                        }


                        try{
                            mAdapter.notifyDataSetChanged();
                        }catch (Exception e){}

                        if (replace) {
                            mListView.smoothScrollToPosition(0);

                            mAddLastTime = mLastTime;

                            Fire.getReference().child(childkey).child(mFeedData.getKey()).orderByChild("time").startAt(mLastTime).addChildEventListener(mChildEventListener);

                            if(mUserLastTime < 0) {
                                mUserLastTime = Fire.getServerTimestamp();
                                Fire.getReference().child(Fire.KEY_CHAT_USER).child(mFeedData.getKey()).orderByChild("time").startAt(mUserLastTime).addChildEventListener(mChildEventListener);
                            }

                            if(isJoin)reqWriteUserIn();
                        }

                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    swipelayout.setRefreshing(false);
                    isGetData = false;
                }
            });
        }

    }


    private ChildEventListener mChildEventListener = new ChildEventListener() {
        long adCount = 0;
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            LoggerManager.e("munx", "onChildAdded");
            if (dataSnapshot == null)
                return;
            if (dataSnapshot.getValue() == null) {
                return;
            }

            FeedData chat = Parser.getFeedDataParse(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue(true));
            if (chat == null)
                return;

            if (chat.getCate() == 0 && mLastTime >= chat.getTime()) {
                return;
            }

            if (chat.getCate() == ChatListReAdapter.TYPE_CATE_ROOMREMOVE){
                dataListAddObject = null;

                try {
                    Fire.getReference().child(childkey).child(mFeedData.getKey()).orderByChild("time").startAt(mLastTime).removeEventListener(mChildEventListener);
                }catch (Exception e){}
                try{
                    Fire.getReference().child(Fire.KEY_CHAT_USER).child(mFeedData.getKey()).orderByChild("time").startAt(mUserLastTime).removeEventListener(mChildEventListener);
                }catch (Exception e){}


                CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getContext(), R.string.notice, R.string.jam_remove_room, new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                    @Override
                    public void onClickOk() {
                        try {
                            getActivity().setResult(getActivity().RESULT_OK);
                        }catch (Exception e){}

                        getActivity().finish();
                    }

                    @Override
                    public void onClickCancel() {
                        try {
                            getActivity().setResult(getActivity().RESULT_OK);
                        }catch (Exception e){}

                        getActivity().finish();
                    }
                });
                mCustomAlertDialog.setCancelable(false);
                mCustomAlertDialog.show();

                return;
            }


            if (!TextUtils.isEmpty(chat.getIuid()) && chat.getIuid().equals(MainActivity.mUserData.getUid())
                    && chat.getTime() > mCreateTime
                    ) {
                if(chat.getCate() == ChatListReAdapter.TYPE_CATE_HEART) {
                    ImageToast.showToast(getActivity(), chat.getText(), R.raw.heart, Toast.LENGTH_SHORT);
                    //---------------------------------------------------//수정수정--------------------------------------------------------------------------------------------------------------
                    for(RoomUserData a : mFeedData.getListRoomUserData()) {
                        if (MainActivity.mUserData.getUid().equals(a.getUid())) {
                            aa=a.getHashMap();
                            if(a.getBackuptime()==0){
                                aa.put("backuptime", a.getTime());
                                a.setHashMap(aa);
                                Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).updateChildren(aa);
                            }
                            Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).child("time").setValue(a.getBackuptime());
                            break;
                        }
                    }
                    //---------------------------------------------------//수정수정--------------------------------------------------------------------------------------------------------------
                }
                else if(chat.getCate() == ChatListReAdapter.TYPE_CATE_BOOM){
                    ImageToast.showToast(getActivity(), chat.getText(), R.raw.explosion, Toast.LENGTH_SHORT);
                    //---------------------------------------------------//수정수정--------------------------------------------------------------------------------------------------------------
                    for (RoomUserData u : mFeedData.getListRoomUserData()) {
                        if (MainActivity.mUserData.getUid().equals(u.getUid())) {
                            aa=u.getHashMap();
                            if(u.getBackuptime()==0){
                                aa.put("backuptime", u.getTime());
                                u.setHashMap(aa);
                                Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).updateChildren(aa);
                            }
                            mListData.clear();
                            Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).child("time").setValue(Fire.getServerTimestamp());
                            break;
                        }
                    }
                }
            }


            chat.setKey(dataSnapshot.getKey());

            for(int position = 0; position<mListData.size();position++){
                if(position > 6)
                    break;

                if(mListData.size() > position && mListData.get(position).getRowType() == ChatListReAdapter.TYPE_ADMOB){
                }
                else if(mListData.size() > position && mListData.get(position).getRowType() == ChatListReAdapter.TYPE_EMPTY){
                }
                else if(mListData.size() > position
                        && (mListData.get(position).getCate() == ChatListReAdapter.TYPE_CATE_USERIN
                        || mListData.get(position).getCate() == ChatListReAdapter.TYPE_CATE_USEROUT
                        || mListData.get(position).getCate() == ChatListReAdapter.TYPE_CATE_BAN
                )){
                }
                else{
                    if(chat.getKey().equals(mListData.get(position).getKey())){
                        return;
                    }
                }
            }


            if(dataListAddObject == null)
                return;

            synchronized (dataListAddObject) {
                if (mListData != null) {
                    for (FeedData f : mListData) {
                        if (f.getRowType() == ChatListReAdapter.TYPE_EMPTY) {
                            mListData.remove(f);
                            break;
                        }
                    }

//                if (mListData.size() > 0)
//                {
//                    if (mListData.get(0).getIsFeed() == 1)
//                        mListFirstIndex = 1;
//                    else
//                        mListFirstIndex = 0;
//                }


                    try {
                        for (RoomUserData u : mFeedData.getListRoomUserData()) {
                            if (u.getUid().equals(chat.getUid())) {
                                if (!TextUtils.isEmpty(u.getpImg()))
                                    chat.setpImg(u.getpImg());
                                break;
                            }
                        }
                    }catch (Exception e){}

                    mListData.add(0, chat);

                    try {
                        if (chat.getUid().equals(MainActivity.mUserData.getUid()) && !TextUtils.isEmpty(chat.getMic())) {
                            if (removeAudioMsg != null) removeAudioMsg.add(chat.getKey());
                        }
                    }catch (Exception e){}


//                adCount++;
//                if(adCount == 10){
//                    adCount = 0;
//
//                    FeedData f = new FeedData();
//                    f.setRowType(ChatListReAdapter.TYPE_ADMOB);
//                    mListData.add(0, f);
//                }

                    //여러 사용자가 동시에 파이어 베이스에 썻을경우 가장 최근게 늦게 와야하는데 젤먼저옴...
                    Collections.sort(mListData, mComparatorNumber);

                    FeedData f = new FeedData();
                    f.setRowType(ChatListReAdapter.TYPE_EMPTY);
                    f.setTime(chat.getTime() + 1);
                    mListData.add(0, f);


                    if (mAdapter != null)
                        mAdapter.notifyDataSetChanged();//mAdapter.notifyItemInserted(0);

                }

                mAddLastTime = chat.getTime();

//            if (size <= 0 && mListData.size() > 0) {
//                setEmptyViewDelete();
//            }
//            else if (size <= 0 && mListData.size() <= 0) {
//                setEmptyView();
//            }

                if (mListData.size() > 0 && chat.getUid() != null) {
                    if (chat.getUid().equals(MainActivity.mUserData.getUid())) {
                        mListView.smoothScrollToPosition(0);//.scrollToPosition(0);//.smoothScrollToPosition(0);//스무스로하면 다른동작들에 장에를줄수있다
                    } else {
                        if (layoutManager != null && layoutManager.findFirstVisibleItemPosition() < 5) {
                            mListView.smoothScrollToPosition(0);//.scrollToPosition(0);
                        }
                    }
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

//            if (dataSnapshot == null)
//                return;
//            if (dataSnapshot.getValue() == null)
//                return;
//
//            if (dataSnapshot.getKey() == null)
//                return;
//
//            ProfileChatInfo chat = ParseUtils.parseProfileChatInfo(dataSnapshot);
//            if (chat == null)
//                return;
//
//            for(int i=0; i<mListData.size();i++){
//                try {
//                    ProfileChatInfo d =  mListData.get(i);
//                    if (d.getType() == 4 && dataSnapshot.getKey().equals(d.getKey())) {
//                        chat.setKey(dataSnapshot.getKey());
//                        d.setAllData(chat);
//
//                        if (mAdapter != null) mAdapter.notifyDataSetChanged();//mAdapter.notifyItemChanged(i);
//
//                        break;
//                    }
//                }catch (Exception e){}
//            }


        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            LoggerManager.e("munx", "onChildRemoved");
            if (dataSnapshot == null || dataSnapshot.getKey() == null)
                return;

            String key = dataSnapshot.getKey();

            try {
                if(dataListAddObject == null)
                    return;

                LoggerManager.e("munx", "onChildRemoved2");
                synchronized (dataListAddObject) {
                    boolean is = false;
                    try {
                        for (FeedData info : mListData) {
                            if (key.equals(info.getKey())) {
                                is = true;
                                mListData.remove(info);
                                break;
                            }
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (is) if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                    }
                }
            }catch (Exception e){}
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            LoggerManager.e("munx", "onChildMoved");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            LoggerManager.e("munx", "onCancelled");
        }
    };




    public Comparator mComparatorNumber = new Comparator<FeedData>() {
        @Override
        public int compare(FeedData s1, FeedData s2) {
            int mReturn = -1;

            if(s1.getTime() < s2.getTime()){
                mReturn = 1;
            }
            else if(s1.getTime() > s2.getTime()){
                mReturn = -1;
            }
            else{
                mReturn = 0;
            }
            //return s11 < s22 ? -1 : s11 > s22 ? 1:0;

            return mReturn;
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Fire.getReference().child(Fire.KEY_CHAT_COUNT).child(mFeedData.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if(dataSnapshot!=null && dataSnapshot.getValue()!=null) {
                        HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
                        long count = Long.parseLong(data.get("chatCount") + "");

                        DBManager.createInstnace(getActivity()).addRoomBadge(mFeedData.getKey(), count);
                    }
                }catch (Exception e){}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        try {
            DBManager.createInstnace(getActivity()).removeLauncherBadge(mFeedData.getKey());
            long t = DBManager.createInstnace(getActivity()).getLauncherBadgeToal();
            Utils.setBadgeCount(getActivity(), (int)t);
        }catch (Exception e){}


        try {
            getActivity().unregisterReceiver(privateBroadcastReceiver);
        }catch (Exception e){}

//        reqWriteUserOut();



        try {
            Fire.getReference().child(childkey).child(mFeedData.getKey()).orderByChild("time").startAt(mLastTime).removeEventListener(mChildEventListener);
        }catch (Exception e){}
        try{
            Fire.getReference().child(Fire.KEY_CHAT_USER).child(mFeedData.getKey()).orderByChild("time").startAt(mUserLastTime).removeEventListener(mChildEventListener);
        }catch (Exception e){}
        try{
            Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).child("open").removeEventListener(mOpenValueEventListener);
        }catch (Exception e){}
        try {
            Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).child("userName").removeEventListener(mNameValueEventListener);
        }catch (Exception e){}



        try {
            if (mFeedData.getUid().equals(MainActivity.mUserData.getUid()) && mFeedData.getListRoomUserData() != null && mFeedData.getListRoomUserData().size() > 0) {
                Fire.getReference().child(Fire.KEY_ROOM_USERCOUNT).child(mFeedData.getKey()).child("c").setValue(mFeedData.getListRoomUserData().size());
                Fire.getReference().child(Fire.KEY_ROOM_USERCOUNT).child(mFeedData.getKey()).child("t").setValue(Fire.getServerTimestamp());
            }
        }catch (Exception e){}


        MainActivity.mCurrentRoomId = null;


//        try {
//            for (String key : removeAudioMsg) {
//                Fire.getReference().child(childkey).child(mFeedData.getKey()).child(key).removeValue();
//            }
//        }catch (Exception e){}
//
//        try {
//            GoogleDriveRemoveAudioAsyncTask mGoogleDriveRemoveAudioAsyncTask = new GoogleDriveRemoveAudioAsyncTask(getActivity(), mDrive, null);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                mGoogleDriveRemoveAudioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            } else {
//                mGoogleDriveRemoveAudioAsyncTask.execute();
//            }
//        }catch (Exception e){}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }









    public class AdapterSpinner1 extends BaseAdapter {

        public int measureContentWidth(){
            return measureContentWidth(this);
        }

        private int measureContentWidth(ListAdapter listAdapter) {
            ViewGroup mMeasureParent = null;
            int maxWidth = 0;
            View itemView = null;
            int itemType = 0;

            final ListAdapter adapter = listAdapter;
            final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                final int positionType = adapter.getItemViewType(i);
                if (positionType != itemType) {
                    itemType = positionType;
                    itemView = null;
                }

                if (mMeasureParent == null) {
                    mMeasureParent = new FrameLayout(getActivity());
                }

                itemView = adapter.getView(i, itemView, mMeasureParent);
                itemView.measure(widthMeasureSpec, heightMeasureSpec);

                final int itemWidth = itemView.getMeasuredWidth();

                if (itemWidth > maxWidth) {
                    maxWidth = itemWidth;
                }
            }

            return maxWidth;
        }



        Context context;
        List<String> data;
        LayoutInflater inflater;

        class ViewHolder {
            TextView tvTitle;
            FrameLayout btn_option;

            ViewHolder(View view) {
                tvTitle = view.findViewById(R.id.tv_text);
                btn_option = view.findViewById(R.id.btn_option);
            }
        }


        public AdapterSpinner1(Context context, String [] data){
            this.context = context;
            for(String s:data) {
                if(this.data == null){
                    this.data = new ArrayList<>();
                }
                this.data.add(s);
            }
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            if(data!=null) return data.size();
            else return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.popupwindow_menu_url, parent, false);
                convertView = inflater.inflate(R.layout.popupwindow_menu, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if(data!=null && data.size() > position) {
                //데이터세팅
                String text = data.get(position);
                holder.tvTitle.setText(text);


                setPadding(holder, position);
            }

            return convertView;
        }

        @Override
        public View getDropDownView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.popupwindow_menu_url, parent, false);
                convertView = inflater.inflate(R.layout.popupwindow_menu, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            if(data!=null && data.size() > position) {
                //데이터세팅
                String text = data.get(position);
                holder.tvTitle.setText(text);

                setPadding(holder, position);
            }



            return convertView;
        }

        private void setPadding(ViewHolder holder, final int position){
            if(position == 0) {
                holder.btn_option.setPadding(Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 8), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0));
            }
            else if(data.size() == position + 1){
                holder.btn_option.setPadding(Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 8));
            }
            else{
                holder.btn_option.setPadding(Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0));
            }
        }
        //머냐 슈발

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ACCOUNT_PICKER) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                mStrGoogleAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (!TextUtils.isEmpty(mStrGoogleAccountName)) {
                    loadGoogleDrive();
                }
            }
        }
        else if(requestCode == RECOVERABLE_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK) {
                if (!TextUtils.isEmpty(mStrGoogleAccountName)) {
                    loadGoogleDrive();
                }
            }
        }
        if(requestCode == RESULT_GALL_RETURN) {
            if (resultCode == getActivity().RESULT_OK) {
                try {
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().managedQuery(data.getData(), projection, null, null, null);
                    int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String uploadFile = cursor.getString(column_index_data);

                    File f = GlobalApplication.getProfileFile(getActivity());

                    String filepath = f.getAbsolutePath() + "/" + Fire.getServerTimestamp() + ".jpg";
                    copy(new File(uploadFile), new File(filepath));

                    Intent intent = new Intent(getActivity(), MakePictureCropActivity.class);
                    intent.putExtra(MakePictureCropActivity.PATH, filepath);
                    intent.putExtra(MakePictureCropActivity.MODE_SQUARE, true);
                    startActivityForResult(intent, RESULT_CROP);


                }catch(Exception e){

                    try {
                        final String filepath = data.getData().getPath();

                        Intent intent = new Intent(getActivity(), MakePictureCropActivity.class);
                        intent.putExtra(MakePictureCropActivity.PATH, filepath);
                        intent.putExtra(MakePictureCropActivity.MODE_SQUARE, true);
                        startActivityForResult(intent, RESULT_CROP);
                    }catch (Exception e1){}
                }
            }
        }
        else if(requestCode == RESULT_CROP){
            if (resultCode == getActivity().RESULT_OK) {
                if(data != null){
                    final String path = data.getStringExtra(MakePictureCropActivity.PATH);
                    if(!TextUtils.isEmpty(path)){
                        LoggerManager.e("mun","path : " + path);
                        //파일 업로드드
                        mMyImagePath = path;

//                        signIn();

                        if(TextUtils.isEmpty(MyPreferences.getString(getActivity(), MyPreferences.KEY_DRIVE_NAME))) {
                            new android.support.v7.app.AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.notice)
                                    .setMessage(R.string.permission_account)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();


                                            checkGoogleAccountImageAdd();

                                        }
                                    })
                                    .create().show();
                        }
                        else{
                            checkGoogleAccountImageAdd();
                        }



//                        LoggerManager.e("mun", mMyImagePath);
//                        Glide.with(getActivity())
//                                .load(mMyImagePath)
//                                .bitmapTransform(new CropCircleTransformation(getActivity()))
//                                .skipMemoryCache(true)
//                                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                                .into(iv_img);
                    }
                }
            }
        }
        else if (requestCode == REQUEST_RECORDING) {
            if (resultCode == Activity.RESULT_OK) {
                // Great! User has recorded and saved the audio file

                File f = GlobalApplication.getProfileFile(getActivity());

                String filepath = f.getAbsolutePath() + "/jam_audio.wav";

                f = new File(filepath);
                try {
                    if (f.exists()) {
                        mMyAudioPath = f.getAbsolutePath();

                        checkGoogleAccountAudio();


                    }
                }catch (Exception e){}

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Oops! User has canceled the recording
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(MY_PERMISSIONS_REQUEST_READ_CONTACTS == requestCode){

            LoggerManager.e("mun", "sdsdsdsdsdsdsd");

            if(!PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || !PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.GET_ACCOUNTS)
                    || !PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.RECORD_AUDIO)
                    ) {
//            if(grantResults != null && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                android.app.AlertDialog.Builder alert_confirm = new android.app.AlertDialog.Builder(getActivity());
                alert_confirm
                        .setMessage(R.string.permission_all_err)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PermissionOk.checkPermissionAllActivity(getActivity(), MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                                    }
                                })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                            }
                        });
                android.app.AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        }
    }



    /*****
     * drive
     */

    @Override
    protected void setImage(String url, String driveId) {
        super.setImage(url, driveId);
        sendImage(driveId);
    }

    public void checkGoogleAccountAudio () {
        check_type = 3;
        checkGoogleAccountStep2();
    }

    public void checkGoogleAccountGeller () {
        check_type = 2;
        checkGoogleAccountStep2();
    }

    public void checkGoogleAccountImageAdd () {
        check_type = 1;
        checkGoogleAccountStep2();
    }

    private String mMyAudioPath;

    @Override
    public void setDriveAudioUpload(){

        if(mProgressDialog!=null)mProgressDialog.show();

        GoogleDriveUploadAudioAsyncTask mGoogleDriveUploadAudioAsyncTask = new GoogleDriveUploadAudioAsyncTask(getActivity(), mMyAudioPath, mDrive, new GoogleDriveUploadAudioAsyncTask.Callback() {
            @Override
            public void result(String result, final boolean reLogin) {
                if(mProgressDialog!=null)mProgressDialog.dismiss();
                if(!TextUtils.isEmpty(result)) {
//                    mpImg = result;
                    removeAudio.add(result);
                    sendAudio(result);
                }
                else{
                    GlobalApplication.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {

                            MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_TOKEN, "");
                            MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_NAME, "");

                            if(reLogin){
                                mGoogleAccountCredential = null;
                            }
                            checkGoogleAccountStep2();
                        }
                    });
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mGoogleDriveUploadAudioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mGoogleDriveUploadAudioAsyncTask.execute();
        }
    }

    @Override
    public void setDriveUpload(){
        try {
            LoggerManager.e("mun", "chat setDriveUpload : " + getClass().getName());
        }catch (Exception e){}
        if(mProgressDialog!=null)mProgressDialog.show();


        GlobalApplication.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                GoogleDriveUploadAsyncTask mGoogleDriveUploadAsyncTask = new GoogleDriveUploadAsyncTask(getActivity(), false, mMyImagePath, mDrive, new GoogleDriveUploadAsyncTask.Callback() {
                    @Override
                    public void result(String result, final boolean reLogin) {
                        if (mProgressDialog != null) mProgressDialog.dismiss();
                        if (!TextUtils.isEmpty(result)) {
                            mpImg = result;

                            DBManager.createInstnace(getActivity()).addDriveChatImg(MainActivity.mUserData.getUid(), result);

                            final String url = "https://docs.google.com/uc?export=download&id=" + result;
                            GlobalApplication.runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    setImage(url, mpImg);
                                }
                            });
                        } else {
                            GlobalApplication.runOnMainThread(new Runnable() {
                                @Override
                                public void run() {

                                    MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_TOKEN, "");
                                    MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_NAME, "");

                                    if (reLogin) {
                                        mGoogleAccountCredential = null;
                                    }
                                    checkGoogleAccountStep2();
                                }
                            });
                        }
                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mGoogleDriveUploadAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mGoogleDriveUploadAsyncTask.execute();
                }
            }
        });


    }

    @Override
    public void setListDialog(){
        Toast.makeText(getActivity(), R.string.jam_out_removeaudio, Toast.LENGTH_SHORT).show();
        BottomProfileListDialog mBottomProfileListDialog = new BottomProfileListDialog(getActivity(), mDrive, true);
        mBottomProfileListDialog.setCallback(new BottomProfileListDialog.Callback() {
            @Override
            public void onClickAdd() {
                Intent intent = new Intent(Intent.ACTION_PICK).setType("image/*");
                startActivityForResult(intent, RESULT_GALL_RETURN);
            }

            @Override
            public void onClickItem(String id) {
                mpImg = id;

                final String url = "https://docs.google.com/uc?export=download&id=" + id;

                GlobalApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        setImage(url, mpImg);
                    }
                });

            }
        });
        mBottomProfileListDialog.show();
    }

}
