package com.novato.jam.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.novato.jam.R;
import com.novato.jam.common.EndlessRecyclerOnScrollListener;
import com.novato.jam.common.FragmentAppCompatManager;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.PushData;
import com.novato.jam.db.DBManager;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.RoomInfoActivity;
import com.novato.jam.ui.adapter.MessageListReAdapter;

import java.util.ArrayList;

// (update2)        비밀 쪽지 수정
public class PrivateChatFragment extends android.support.v4.app.Fragment implements View.OnClickListener {
    android.support.v4.widget.DrawerLayout drawer_layout;


    FragmentAppCompatManager mFragmentAppCompatManager;
    private View mRootView;
    SwipeRefreshLayout left_swipelayout;
    MessageListReAdapter mAdapter;
    RecyclerView left_list;
    EndlessRecyclerOnScrollListener mOnScrollListener;
    FeedData mfeeddata = new FeedData();
    private ArrayList<FeedData> mListData = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.left_menu, container, false);
        mRootView.findViewById(R.id.left_close).setVisibility(View.VISIBLE);

        mRootView.findViewById(R.id.left_close).setOnClickListener(new View.OnClickListener() {  // 비메 전부제거
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                alert_confirm.setTitle(R.string.info_pchatdel)
                        .setMessage(R.string.text_pchatdelareement)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok_pchatdel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DBManager.createInstnace(getActivity()).deleteAllDB();
                                        addDataList(true);
                                    }

                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        });

        left_swipelayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.left_swipelayout); //새로 고침하는부분
        left_swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() { // 아래로 스크롤해 새로고침하는것
            @Override
            public void onRefresh() {
                LoggerManager.e("mun", "---------- onRefresh:");
                left_swipelayout.setRefreshing(false);
                addDataList(true);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        }
        left_list = (RecyclerView) mRootView.findViewById(R.id.left_list);
        left_list.setLayoutManager(layoutManager);
//        mListView.setHasFixedSize(true);
//        mListView.getItemAnimator().setAddDuration(300);
//        mListView.getItemAnimator().setRemoveDuration(100);

        mOnScrollListener = new EndlessRecyclerOnScrollListener(left_list.getLayoutManager()) {
            @Override
            public void onLoadMore(int current_page) {
                LoggerManager.e("munx", "onLoadMore :" + current_page);
            }
        };

        left_list.addOnScrollListener(mOnScrollListener);

        mAdapter = new MessageListReAdapter(mListData, left_list.getLayoutManager(), left_list);
        mAdapter.setNew(true);
        mAdapter.setCallback(new MessageListReAdapter.Callback() {

            @Override

            public void onClick(int position) { // 비밀 메시지 클릭하면 방 들어가는 화면으로 이동 부분

                if (mListData != null && mListData.size() > position) {

                    FeedData mFeedData = mListData.get(position);

                    Intent i = new Intent(getActivity(), RoomInfoActivity.class);
                    i.putExtra("data", mFeedData);
                    getActivity().startActivityForResult(i, MainActivity.RESULT_ROOM);
                }
            }

            @Override
            public void onLongClick(final int position) {

            }
        });

        left_list.setAdapter(mAdapter);
        addDataList(true);
        return mRootView;


    }

    public void addDataList(boolean replace) { // 비밀 메시지 받은 데이터 배열 부분
        ArrayList<PushData> list = DBManager.createInstnace(getActivity()).getMessageAll();
        LoggerManager.e("mun", "size : " + list.size());
        mListData.clear();

        for (PushData p : list) {
            FeedData mFeedData = new FeedData();
            mFeedData.setTitle(p.getRoomName());
            mFeedData.setKey(p.getRoom());
            mFeedData.setText(p.getTitle() + " : " + p.getMsg());
            mFeedData.setTime(p.getTime());
            mListData.add(mFeedData);
        }

        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {

    }
}