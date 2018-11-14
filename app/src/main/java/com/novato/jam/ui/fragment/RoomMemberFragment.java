package com.novato.jam.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.novato.jam.R;
import com.novato.jam.admob.Adinit;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.dialog.BottomUserInfoDialog;
import com.novato.jam.dialog.CustomToast;
import com.novato.jam.firebase.Fire;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.RoomInfoActivity;
import com.novato.jam.ui.adapter.RoomUserListReAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class RoomMemberFragment extends BaseRoomFragment implements View.OnClickListener{

    private View mRootView;

//    UserData userData;


    private RoomUserListReAdapter mAdapter01, mAdapter02;


    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_roominfo, container, false);

        mProgressDialog = new ProgressDialog(getActivity());


        mRootView.findViewById(R.id.layout_back).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_ok).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_ok).setVisibility(View.GONE);

        mRootView.findViewById(R.id.lay_profile).setVisibility(View.GONE);
        mRootView.findViewById(R.id.layout_close).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.layout_close).setOnClickListener(this);

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
                getActivity().onBackPressed();
                break;
            }
            case R.id.layout_close:{
                getActivity().onBackPressed();
                break;
            }
        }
    }


    private void setUi(){
//        if(isLoad)
//            return;
//
//        isLoad = true;
        try {

            if(mFeedData.getListRoomUserData() == null){
                mFeedData.setListRoomUserData(new ArrayList<RoomUserData>());
            }



            TextView tv_action_title = mRootView.findViewById(R.id.tv_action_title);
            tv_action_title.setText(R.string.title_join_member);//(mFeedData.getTitle());


//            ImageView iv_img = mRootView.findViewById(R.id.iv_img);
//            Glide.with(getActivity())
//                    .load(user.getpImg() + "")
//                    .bitmapTransform(new CropCircleTransformation(getActivity()))
////                .placeholder(R.drawable.icon_progress)
////                        .placeholder(null)
//                    .skipMemoryCache(true)
////                .error(R.drawable.none_img)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .into(iv_img);

//            TextView tv_title = mRootView.findViewById(R.id.tv_title);
//            tv_title.setText(user.getUserName());

            TextView tv_text = mRootView.findViewById(R.id.tv_text);
            tv_text.setText(mFeedData.getText());




            mRootView.findViewById(R.id.btn_ok).setVisibility(View.VISIBLE);
//            final ScrollView scrollview = mRootView.findViewById(R.id.scrollview);



            ArrayList<RoomUserData> ready = new ArrayList<>();
            ArrayList<RoomUserData> ok = new ArrayList<>();
            for(RoomUserData u :mFeedData.getListRoomUserData()){
                if(u.getRowType() != RoomUserListReAdapter.ROWTYPE_TITLE) {
                    if (u.getOpen() == 1) {
                        ok.add(u);
                    } else if (u.getOpen() == 0) {
                        ready.add(u);
                    }
                }
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
                } else {
                    mRootView.findViewById(R.id.lay_ready).setVisibility(View.GONE);
                }
            }catch (Exception e){}


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
                                    mBottomSheetDialog.dismiss();
                                    Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(item.getUid()).child("open").setValue(1);
                                    Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(item.getUid()).child("time").setValue(Fire.getServerTimestamp());
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
