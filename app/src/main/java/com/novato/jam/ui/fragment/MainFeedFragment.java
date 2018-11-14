package com.novato.jam.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.BuildConfig;
import com.novato.jam.R;
import com.novato.jam.common.EndlessRecyclerOnScrollListener;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.RoomInfoActivity;
import com.novato.jam.ui.adapter.FeedListReAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class MainFeedFragment extends android.support.v4.app.Fragment implements View.OnClickListener{

    final private String childkey = Fire.KEY_CHAT_ROOM;
    private String childCate = "0";


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

    public void setChildCate(String childCate) {
        this.childCate = childCate;
    }

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
//        swipelayout.setColorSchemeColors(getResources().getColor(R.color.pink));
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            private long time = Fire.getServerTimestamp();
            @Override
            public void onRefresh() {
                LoggerManager.e("mun", "---------- onRefresh:");
                if(swipelayout!=null)swipelayout.setRefreshing(false);

                if(!childCate.equals("-99")){
                    addDataList(true);
                }
                else if(time < Fire.getServerTimestamp() - (1000 * 60)){
                    time = Fire.getServerTimestamp();
                    addDataList(true);
                }

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

                if(!childCate.equals("-99"))addDataList(false);
            }
        };

        mListView.addOnScrollListener(mOnScrollListener);

        mAdapter = new FeedListReAdapter(mListData, mListView.getLayoutManager(), mListView);
        mAdapter.setCallback(new FeedListReAdapter.Callback() {
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
        mListView.setAdapter(mAdapter);





        if(isUserOk){
            mRootView.findViewById(R.id.btn_write).setVisibility(View.VISIBLE);
        }
        else{
            mRootView.findViewById(R.id.btn_write).setVisibility(View.GONE);
        }
        mRootView.findViewById(R.id.btn_write).setOnClickListener(this);


//        addDataList(true);

        return mRootView;
    }

    boolean isFirst = false;
    public void setFirstPosition(){
        if(!isFirst){
            isFirst = true;
            addDataList(true);
        }
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


    synchronized public void setPagerCurrent(){

        if(mListData == null || mListData.size() <= 0)
            addDataList(true);
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
            mQuery = Fire.getReference().child(childkey).child(childCate).orderByChild("time").limitToLast(LimitCount);
        }
        else{
            mQuery = Fire.getReference().child(childkey).child(childCate).orderByChild("time").endAt(searchTime).limitToLast(LimitCount);
        }


        if(mQuery!=null) {
            isGetData = true;

            if(replace) {
                isEndList = false;
                searchTime = -1;
                if (mOnScrollListener != null) mOnScrollListener.setInit();
            }
            swipelayout.setRefreshing(true);
            LoggerManager.e("mun", "firelistget mainfeed");
            mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (replace) {
                            mListData.clear();
                            LoggerManager.e("mun", "feed data replace ----");
                        }

                        swipelayout.setRefreshing(false);
                        isGetData = false;


                        if (dataSnapshot != null) {


                            ArrayList<FeedData> lists = Parser.getFeedDataListParse(dataSnapshot);
                            ArrayList<FeedData> list = new ArrayList<>();

                            if (lists == null) {
                                isEndList = true;
                            } else {
                                LoggerManager.e("mun", "firelistget mainfeed" + lists.size());
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
//                                    long totalsize = 0;
//                                    try{
//                                        totalsize = mListData.size();
//                                    }catch (Exception e){}
//
//
//                                    int s = list.size();
//
//                                    int i = 0;
//                                    int counts = 0;
//                                    while (i < s) {
//                                        try {
//                                            FeedData mFeedData = new FeedData();
//                                            if (MainActivity.isInstream == 1 && (totalsize+counts) % 2 == 0) {
//                                                mFeedData.setRowType(FeedListReAdapter.TYPE_FACEBOOKAD);
//                                            }
//                                            else
//                                                {
//                                                mFeedData.setRowType(FeedListReAdapter.TYPE_ADMOB);
//                                            }
//                                            list.add(i, mFeedData);
//
//                                            i += 10;
//                                        } catch (Exception e) {
//                                        }
//                                        counts++;
//                                    }
//                                } catch (Exception e) {
//                                }


                                if(replace && childCate.equals("-99")){
                                    mAdapter.setFragmentManager(getChildFragmentManager());
                                    FeedData mFeedData = new FeedData();
                                    mFeedData.setRowType(FeedListReAdapter.TYPE_EVENT);
                                    list.add(0, mFeedData);
                                }

                                mListData.addAll(list);
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
