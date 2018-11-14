package com.novato.jam.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.EndlessRecyclerOnScrollListener;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.db.DBManager;
import com.novato.jam.dialog.CustomAlertDialog;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.RoomInfoActivity;
import com.novato.jam.ui.adapter.FeedListReAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class MyFeedFragment extends android.support.v4.app.Fragment implements View.OnClickListener{

    final private String childkey = Fire.KEY_MY_ROOM;


    private View mRootView;

    private SwipeRefreshLayout swipelayout;
    private RecyclerView mListView;
    private ArrayList<FeedData> mListData = new ArrayList<>();
    private FeedListReAdapter mAdapter;

    private EndlessRecyclerOnScrollListener mOnScrollListener;
    private boolean isGetData = false;
    private boolean isEndList = false;
    private long searchTime = -1;
    final private int LimitCount = 15;


    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;

    private boolean isUserOk = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_mainfeed, container, false);

        mProgressDialog = new ProgressDialog(getActivity());

        mRootView.findViewById(R.id.btn_write).setOnClickListener(this);
//        mRootView.findViewById(R.id.btn_write).setVisibility(View.VISIBLE);

        swipelayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipelayout);


//        swipelayout.setRefreshView(new PullToRefreshLoadingView(swipelayout));
//        swipelayout.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                addDataList(true);
//            }
//        });
//        swipelayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark));
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoggerManager.e("mun", "---------- onRefresh:");
                //swipeRefreshLayout.setRefreshing(false);
                if(swipelayout!=null)swipelayout.setRefreshing(false);
                addDataList(true);

            }
        });



//        if(TabletUtils.isTablet(getActivity())){
//            layoutManager  = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);//new GridLayoutManager(context);
//        }
//        else{
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        }
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

        mAdapter = new FeedListReAdapter(mListData, mListView.getLayoutManager(), mListView);
        mAdapter.setNew(true);
        mAdapter.setCallback(new FeedListReAdapter.Callback() {
            @Override
            public void onClick(int position) {
                if(mListData!=null && mListData.size() > position){
                    final FeedData mFeedData = mListData.get(position);

                    LoggerManager.e("mun","x : "+ MainActivity.mUserData.getUid() + " / " + mFeedData.getKey());

                    if(mFeedData.getOpen() == Fire.KEY_VALUSE_DESTROY_ROOM){
                        CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(mListView.getContext(), mListView.getContext().getString(R.string.notice), mListView.getContext().getString(R.string.jam_remove_room_ok), new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                            @Override
                            public void onClickOk() {
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

                            }
                            @Override
                            public void onClickCancel() {
                            }
                        });
                        mCustomAlertDialog.show();
                        return;
                    }

                    Intent i = new Intent(getActivity(), RoomInfoActivity.class);
                    i.putExtra("data",mFeedData);
                    getActivity().startActivityForResult(i, MainActivity.RESULT_ROOM);
                }
            }
            @Override
            public void onLongClick(int position) {
            }
        });
        mListView.setAdapter(mAdapter);





        if(isUserOk){
            mRootView.findViewById(R.id.btn_write).setVisibility(View.VISIBLE);
        }
        else{
            mRootView.findViewById(R.id.btn_write).setVisibility(View.GONE);
        }
        mRootView.findViewById(R.id.btn_write).setOnClickListener(this);


        addDataList(true);


        try {
            DBManager.createInstnace(getActivity()).removeLauncherBadgeAll();
            Utils.setBadgeCount(getActivity(), 0);
        }catch (Exception e){}

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mAdapter.notifyDataSetChanged();
        }catch (Exception e){}
    }

    public void setUserOk(){
        isUserOk = true;
        try {
            mRootView.findViewById(R.id.btn_write).setVisibility(View.VISIBLE);
        }catch (Exception e){}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_write:{
                break;
            }
        }
    }


    public void setWritePosition(int bottom){
        try {
            View iv_left_menu = mRootView.findViewById(R.id.btn_write);
            ((RelativeLayout.LayoutParams) iv_left_menu.getLayoutParams()).bottomMargin = bottom;
            iv_left_menu.requestLayout();
        }catch (Exception e){}
    }

    synchronized public void addDataList(final boolean replace){

        if(mRootView == null)
            return;

        if(isGetData){//먼저호출해서 불러오는중
            return;
        }

        if(!replace && isEndList){//더보기 할경우 더이상 불러올 데이터가 없다고 판단시 취소
            return;
        }



        Query mQuery = null;

        if(replace){
            mQuery = Fire.getReference().child(childkey).child(MainActivity.mUserData.getUid()).orderByChild("time").limitToLast(LimitCount);
        }
        else{
            mQuery = Fire.getReference().child(childkey).child(MainActivity.mUserData.getUid()).orderByChild("time").endAt(searchTime).limitToLast(LimitCount);
        }


        if(mQuery!=null) {
            isGetData = true;

            if(replace) {
                isEndList = false;
                searchTime = -1;
                if (mOnScrollListener != null) mOnScrollListener.setInit();
            }
            swipelayout.setRefreshing(true);

            LoggerManager.e("mun", "firelistget myFeed");
            mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (replace) {
                            mListData.clear();
                        }

                        swipelayout.setRefreshing(false);
                        isGetData = false;


                        if (dataSnapshot != null) {


                            ArrayList<FeedData> lists = Parser.getFeedDataListParse(dataSnapshot);
                            final ArrayList<FeedData> list = new ArrayList<>();

                            if (lists == null) {
                                isEndList = true;
                            } else {
                                LoggerManager.e("mun", "firelistget myFeed" + lists.size());
                                for (FeedData m : lists) {
                                    if (m.getTime() > 0) {
                                        if (!replace && searchTime > m.getTime()) {
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
                                } else {
                                    isEndList = true;
                                }

//                                try {
//                                    int s = list.size();
//
//                                    int i = 0;
//                                    while (i < s) {
//                                        try {
//                                            FeedData mFeedData = new FeedData();
//                                            if (MainActivity.isInstream == 1) {
//                                                mFeedData.setRowType(FeedListReAdapter.TYPE_FACEBOOKAD);
//                                            }
//                                            else{
//                                                mFeedData.setRowType(FeedListReAdapter.TYPE_ADMOB);
//                                            }
//                                            list.add(i, mFeedData);
//
//                                            i += 10;
//                                        } catch (Exception e) {
//                                        }
//                                    }
//                                } catch (Exception e) {
//                                }

                                mListData.addAll(list);


                                //뉴 표시 위해서 만들어둠...
                                GlobalApplication.runOnMainThread(new Runnable() {
                                    Object object = new Object();
                                    int size = list.size();
                                    @Override
                                    public void run() {
                                        for(final FeedData f:list){
                                            Fire.getReference().child(Fire.KEY_CHAT_COUNT).child(f.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    try {
                                                        if(dataSnapshot!=null && dataSnapshot.getValue()!=null) {
                                                            HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
                                                            long count = Long.parseLong(data.get("chatCount") + "");
                                                            f.setChatCount(count);
                                                        }
                                                    }catch (Exception e){}
                                                    try {
                                                        synchronized (object) {
                                                            size -= 1;
                                                            if (size == 0) {
                                                                try {
                                                                    mAdapter.notifyDataSetChanged();
                                                                } catch (Exception e) {
                                                                }
                                                            }
                                                        }
                                                    }catch (Exception e){}
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    try {
                                                        synchronized (object) {
                                                            size -= 1;
                                                            if (size == 0) {
                                                                try {
                                                                    mAdapter.notifyDataSetChanged();
                                                                } catch (Exception e) {
                                                                }
                                                            }
                                                        }
                                                    }catch (Exception e){}
                                                }
                                            });
                                        }
                                    }
                                });


                            }

                        } else {
                            isEndList = true;
                        }


                        try {
                            if (mListData.size() <= 0) {
                                mRootView.findViewById(R.id.btn_nolist).setVisibility(View.VISIBLE);
                            } else {
                                mRootView.findViewById(R.id.btn_nolist).setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                        }

                    }catch (Exception e){
                        swipelayout.setRefreshing(false);
                        isGetData = false;
                    }


                    try{
                        mAdapter.notifyDataSetChanged();
                    }catch (Exception e){}
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    swipelayout.setRefreshing(false);
                    isGetData = false;
                }
            });
        }

    }



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


}
