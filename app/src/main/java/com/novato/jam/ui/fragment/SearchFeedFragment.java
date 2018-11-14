package com.novato.jam.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.admob.Adinit;
import com.novato.jam.common.EndlessRecyclerOnScrollListener;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.data.FeedData;
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

public class SearchFeedFragment extends android.support.v4.app.Fragment implements View.OnClickListener{

    final private String childkey = Fire.KEY_TAGS;
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
    final private int LimitCount = 20;


    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;

    private boolean isUserOk = false;

    private String search = "";
    private EditText et_comment;

    public void setSearch(String search) {
        this.search = search;
    }

    public void setChildCate(String childCate) {
        this.childCate = childCate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_searchfeed, container, false);

        mProgressDialog = new ProgressDialog(getActivity());

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
            @Override
            public void onRefresh() {
                LoggerManager.e("mun", "---------- onRefresh:");
                swipelayout.setRefreshing(false);

//                addDataList(true);

            }
        });


        et_comment = mRootView.findViewById(R.id.et_comment);
        et_comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    searchSend();
                }
                return true;
            }
        });

        mRootView.findViewById(R.id.btn_send).setOnClickListener(this);


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
        mAdapter.setSearchType(true);
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




//        addDataList(true);


        {
            FrameLayout lay02 = (FrameLayout) mRootView.findViewById(R.id.lay_ad);
            com.google.android.gms.ads.AdView mADMOBadView = new com.google.android.gms.ads.AdView(getActivity());
            mADMOBadView.setAdSize(AdSize.SMART_BANNER);
            mADMOBadView.setAdUnitId(Adinit.getInstance().getBannerId());
            lay02.addView(mADMOBadView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
            mADMOBadView.loadAd(adRequest);
        }



        if(!TextUtils.isEmpty(search)){
            et_comment.setText(search);

            searchSend();
        }


        return mRootView;
    }

    boolean isFirst = false;
    public void setFirstPosition(){
        if(!isFirst){
            isFirst = true;
            addDataList(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send:{
                searchSend();
                break;
            }
        }
    }

    private void searchSend(){
        String text = et_comment.getText().toString();
        if(!TextUtils.isEmpty(text) && text.length() >= 1) {
            search = text.replaceAll("#","");
            addDataList(true);
        }
        else{
            Toast.makeText(getActivity(), R.string.name_size_err, Toast.LENGTH_SHORT).show();
        }
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

        if(TextUtils.isEmpty(search)){
            return;
        }



        //검색 서버 필요함...

        Query mQuery = null;

        if(replace){
            mQuery = Fire.getReference().child(childkey).child(search).orderByChild("time").limitToLast(LimitCount);
        }
        else{
            mQuery = Fire.getReference().child(childkey).child(search).orderByChild("time").endAt(searchTime).limitToLast(LimitCount);
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
//                                            mFeedData.setRowType(FeedListReAdapter.TYPE_ADMOB);
//                                            list.add(i, mFeedData);
//
//                                            i += 10;
//                                        } catch (Exception e) {
//                                        }
//                                    }
//                                } catch (Exception e) {
//                                }
                                mListData.addAll(list);


                                GlobalApplication.runOnMainThread(new Runnable() {
                                    Object object = new Object();
                                    int size = list.size();
                                    @Override
                                    public void run() {
                                        for(final FeedData f:list){

                                            if(!TextUtils.isEmpty(f.getKey())) {
                                                Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(f.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {//.child("title")
                                                        try {
                                                            if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                                                FeedData mda = Parser.getFeedDataParse(dataSnapshot.getKey() ,(HashMap<String, Object>)dataSnapshot.getValue(true));
                                                                f.setHashMap(mda.getHashMap());
//                                                                String ss = (String) dataSnapshot.getValue(true);
//                                                                f.setTitle(ss);

                                                            }
                                                        } catch (Exception e) {
                                                        }
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
                                                        } catch (Exception e) {
                                                        }
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
                                                        } catch (Exception e) {
                                                        }
                                                    }
                                                });
                                            }
                                            else
                                            {
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
                                                } catch (Exception e) {
                                                }
                                            }
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


                        mAdapter.notifyDataSetChanged();

                    }catch (Exception e){
                        swipelayout.setRefreshing(false);
                        isGetData = false;
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