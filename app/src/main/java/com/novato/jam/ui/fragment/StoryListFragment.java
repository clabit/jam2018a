package com.novato.jam.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.admob.Adinit;
import com.novato.jam.analytics.FirebaseAnalyticsLog;
import com.novato.jam.common.AnimUtils;
import com.novato.jam.common.EndlessRecyclerOnScrollListener;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.PermissionOk;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.ui.FeedWriteActivity;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.RoomInfoActivity;
import com.novato.jam.ui.StoryCommentActivity;
import com.novato.jam.ui.adapter.StoryListReAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class StoryListFragment extends BaseRoomFragment implements View.OnClickListener{

    final int RESULT_WRITE = 112;
    final int RESULT_COMMENT = 113;
    final int RESULT_MODIFY = 114;

    final private String childkey = Fire.KEY_STORY;


    private View mRootView;

    private View btn_write;

    private SwipeRefreshLayout swipelayout;
    private RecyclerView mListView;
    private ArrayList<FeedData> mListData = new ArrayList<>();
    private StoryListReAdapter mAdapter;

    private EndlessRecyclerOnScrollListener mOnScrollListener;
    private boolean isGetData = false;
    private boolean isEndList = false;
    private long searchTime = -1;

    final private int LimitCount = 15;


    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;

    private boolean isMember = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_storylist, container, false);

        mProgressDialog = new ProgressDialog(getActivity());

        btn_write = mRootView.findViewById(R.id.btn_write);
        btn_write.setOnClickListener(this);

        for(RoomUserData u :mFeedData.getListRoomUserData()){
            if(u.getUid().equals(MainActivity.mUserData.getUid()) && u.getOpen() == 1){
                mRootView.findViewById(R.id.btn_write).setVisibility(View.VISIBLE);
                AnimUtils.transBottomIn(btn_write, true);
                isMember = true;
                break;
            }
        }

        try{
            LoggerManager.e("mun", "getKey : "+ mFeedData.getKey());
        }catch (Exception e){}

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

            int currentPosition = 0;
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                int position = mListView.getChildAdapterPosition(recyclerView.getChildAt(0));
                if(currentPosition > position){
                    try {
                        setShowLastPlayBtn(true);
                    }catch (Exception e){}
                }
                else if(currentPosition < position){
                    try {
                        setShowLastPlayBtn(false);
                    }catch (Exception e){}
                }
                currentPosition = position;

            }

            private void setShowLastPlayBtn(boolean isShow){
                if(!isMember)
                    return;

                if(isShow){
                    if(btn_write!=null && btn_write.getVisibility() != View.VISIBLE)
                        AnimUtils.transBottomIn(btn_write, true);
                }
                else{
                    if(btn_write!=null && btn_write.getVisibility() == View.VISIBLE)AnimUtils.transBottomOut(btn_write, true);
                }
            }

        };

        mListView.addOnScrollListener(mOnScrollListener);

        mAdapter = new StoryListReAdapter(mListData, mListView.getLayoutManager(), mListView);
        mAdapter.setMember(isMember);
        mAdapter.setFeedData(mFeedData);
        mAdapter.setCallback(new StoryListReAdapter.Callback() {
            @Override
            public void onClick(int position, FeedData item) {
                if(mListData!=null && mListData.size() > position){

//                    if(getActivity() instanceof RoomInfoActivity){
//                        StoryCommentListFragment mStoryListFragment = new StoryCommentListFragment();
//                        mStoryListFragment.setFeedData(mFeedData);
//                        mStoryListFragment.setChild(mFeedData.getKey() , item.getKey(), item);
//                        ((RoomInfoActivity)getActivity()).setChangeFragmentTopAnimation(mStoryListFragment);
//                    }
                    Intent u = new Intent(getActivity(), StoryCommentActivity.class);
                    u.putExtra("data", mFeedData);
                    u.putExtra("item", item);
                    u.putExtra("position", position);
                    startActivityForResult(u , RESULT_COMMENT);
                    try {
                        getActivity().overridePendingTransition(0, 0);
                    }catch (Exception e){}
                }
            }
            @Override
            public void onLongClick(int position) {
            }

            @Override
            public void onMoreClick(View v, int position, final FeedData item) {
                try {
                    if (item.getUid().equals(MainActivity.mUserData.getUid())
                            || mFeedData.getUid().equals(MainActivity.mUserData.getUid())
                            ) {
                        String[] list = new String[]{GlobalApplication.getAppContext().getString(R.string.story_remove)};

                        if(item.getUid().equals(MainActivity.mUserData.getUid())){
                            list = new String[]{GlobalApplication.getAppContext().getString(R.string.story_remove), GlobalApplication.getAppContext().getString(R.string.story_modify)};
                        }

                        AdapterSpinner1 apdapter = new AdapterSpinner1(GlobalApplication.getAppContext(), list);
                        final ListPopupWindow popup = new ListPopupWindow(mListView.getContext());
                        popup.setAnchorView(v);
                        popup.setModal(true);
                        popup.setWidth(apdapter.measureContentWidth());
                        popup.setAdapter(apdapter);
                        popup.setBackgroundDrawable(GlobalApplication.getAppResources().getDrawable(R.drawable.round_white_background));
                        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                popup.dismiss();

                                try {
                                    if (position == 0) {
                                        android.app.AlertDialog.Builder alert_confirm = new android.app.AlertDialog.Builder(getActivity());
                                        alert_confirm
                                                .setMessage(R.string.story_remove_desc)
                                                .setPositiveButton(android.R.string.ok,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                try{
                                                                    if (!TextUtils.isEmpty(item.getKey())) {
                                                                        Fire.getReference().child(childkey).child(mFeedData.getKey()).child(item.getKey()).removeValue();
                                                                        Fire.getReference().child(Fire.KEY_STORY_COMMENT).child(mFeedData.getKey()).child(item.getKey()).removeValue();
                                                                        mListData.remove(item);
                                                                        mAdapter.notifyDataSetChanged();


                                                                        FirebaseAnalyticsLog.setChatSend(getActivity(), "story_remove");
                                                                    }
                                                                }catch (Exception e){}
                                                            }
                                                        })
                                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
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
                                    else if(position == 1){
                                        Intent i = new Intent(getActivity(), FeedWriteActivity.class);
                                        i.putExtra("data", mFeedData);
                                        i.putExtra("modify", item);
                                        startActivityForResult(i, RESULT_MODIFY);

                                    }
                                }catch (Exception e){}

                            }
                        });
                        popup.show();
                    }
                }catch (Exception e){}
            }
        });
        mListView.setAdapter(mAdapter);


        addDataList(true);


        //admob
        if(android.os.Build.VERSION.SDK_INT >= 9){
            FrameLayout lay02 = (FrameLayout)mRootView.findViewById(R.id.lay_ad);
            com.google.android.gms.ads.AdView adView22 = new com.google.android.gms.ads.AdView(getActivity());
            adView22.setAdSize(AdSize.SMART_BANNER);
            adView22.setAdUnitId(Adinit.getInstance().getStoryBannerId());
            lay02.addView(adView22, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
            adView22.loadAd(adRequest);
        }

        return mRootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_write:{

                Intent i = new Intent(getActivity(), FeedWriteActivity.class);
                i.putExtra("data", mFeedData);
                startActivityForResult(i, RESULT_WRITE);
                break;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == RESULT_WRITE && resultCode == Activity.RESULT_OK){
            if(data!=null && data.getParcelableExtra("data") != null){
                FeedData d = data.getParcelableExtra("data");

                try {
                    mListData.add(0, d);

                    mAdapter.notifyDataSetChanged();
                }catch (Exception e){}
            }
        }
        else if(requestCode == RESULT_MODIFY && resultCode == Activity.RESULT_OK){
            if(data!=null && data.getParcelableExtra("data") != null){
                FeedData d = data.getParcelableExtra("data");

                try {
                    if(!TextUtils.isEmpty(d.getKey())) {
                        int position = 0;
                        for(FeedData f :mListData){
                            if(d.getKey().equals(f.getKey())){
                                f.setHashMap(d.getHashMap());
                                f.setKey(d.getKey());
                                break;
                            }
                            position++;
                        }
                        mAdapter.notifyItemChanged(position);
                    }
                }catch (Exception e){}
            }
        }
        else if(requestCode == RESULT_COMMENT && resultCode == Activity.RESULT_OK){
            try{
                long count = data.getLongExtra("count",-1);
                int position = data.getIntExtra("position", -1);

                if(position >= 0 && count >= 0) {
                    mListData.get(position).setChatCount(count);
                }
            }catch (Exception e){}
            if(mAdapter!=null)mAdapter.notifyDataSetChanged();
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

                                        try {
                                            for (RoomUserData u : mFeedData.getListRoomUserData()) {
                                                if (u.getUid().equals(m.getUid())) {
                                                    m.setpImg(u.getpImg());
                                                    m.setUserName(u.getUserName());
                                                    m.setColor(u.getColor());
                                                    break;
                                                }
                                            }
                                        }catch (Exception e){}


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
                    mMeasureParent = new FrameLayout(mListView.getContext());
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

            AdapterSpinner1.ViewHolder holder;
            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.popupwindow_menu_url, parent, false);
                convertView = inflater.inflate(R.layout.popupwindow_menu, null);
                holder = new AdapterSpinner1.ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (AdapterSpinner1.ViewHolder) convertView.getTag();
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
            AdapterSpinner1.ViewHolder holder;
            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.popupwindow_menu_url, parent, false);
                convertView = inflater.inflate(R.layout.popupwindow_menu, null);
                holder = new AdapterSpinner1.ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (AdapterSpinner1.ViewHolder) convertView.getTag();
            }


            if(data!=null && data.size() > position) {
                //데이터세팅
                String text = data.get(position);
                holder.tvTitle.setText(text);

                setPadding(holder, position);
            }



            return convertView;
        }

        private void setPadding(AdapterSpinner1.ViewHolder holder, final int position){
            if(position == 0) {
                holder.btn_option.setPadding(Utils.getPixSize(mListView.getContext(), 0), Utils.getPixSize(mListView.getContext(), 8), Utils.getPixSize(mListView.getContext(), 0), Utils.getPixSize(mListView.getContext(), 0));
            }
            else if(data.size() == position + 1){
                holder.btn_option.setPadding(Utils.getPixSize(mListView.getContext(), 0), Utils.getPixSize(mListView.getContext(), 0), Utils.getPixSize(mListView.getContext(), 0), Utils.getPixSize(mListView.getContext(), 8));
            }
            else{
                holder.btn_option.setPadding(Utils.getPixSize(mListView.getContext(), 0), Utils.getPixSize(mListView.getContext(), 0), Utils.getPixSize(mListView.getContext(), 0), Utils.getPixSize(mListView.getContext(), 0));
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

}
