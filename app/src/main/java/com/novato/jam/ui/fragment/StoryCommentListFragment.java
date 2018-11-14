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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.admob.Adinit;
import com.novato.jam.analytics.FirebaseAnalyticsLog;
import com.novato.jam.common.AnimUtils;
import com.novato.jam.common.EndlessRecyclerOnScrollListener;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.dialog.CustomToast;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.push.SendPushFCMStory;
import com.novato.jam.ui.FeedWriteActivity;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.adapter.CommentListReAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class StoryCommentListFragment extends BaseRoomFragment implements View.OnClickListener{

    final int RESULT_WRITE = 112;
    final private String childkey = Fire.KEY_STORY_COMMENT;


    private View mRootView;

    private EditText et_comment;
    private TextView btn_send;


    private SwipeRefreshLayout swipelayout;
    private RecyclerView mListView;
    private ArrayList<FeedData> mListData = new ArrayList<>();
    private CommentListReAdapter mAdapter;

    private EndlessRecyclerOnScrollListener mOnScrollListener;
    private boolean isGetData = false;
    private boolean isEndList = false;
    private long searchTime = -1;

    final private int LimitCount = 15;


    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;

    private boolean isMember = false;

    private String commentChild = "";
    private FeedData commentData;
    private int commentPosition;

    public void setChild(String k01, String k02, FeedData f, int position){
        if(TextUtils.isEmpty(k01))
            return;
        if(TextUtils.isEmpty(k02))
            return;

        if(f == null)
            return;

        commentChild = k01 + "/"+k02;
        commentData = f;
        commentPosition = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_commentlist, container, false);

        mProgressDialog = new ProgressDialog(getActivity());

        for(RoomUserData u :mFeedData.getListRoomUserData()){
            if(u.getUid().equals(MainActivity.mUserData.getUid()) && (u.getOpen() == 1 || u.getOpen() == 0)){
                mRootView.findViewById(R.id.lay_edit).setVisibility(View.VISIBLE);
                isMember = true;
                break;
            }
        }

        swipelayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipelayout);
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoggerManager.e("mun", "---------- onRefresh:");
                if(swipelayout!=null)swipelayout.setRefreshing(false);

                addDataList(true);

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mListView = (RecyclerView) mRootView.findViewById(R.id.list);
        mListView.setLayoutManager(layoutManager);
        mOnScrollListener = new EndlessRecyclerOnScrollListener(mListView.getLayoutManager()) {
            @Override
            public void onLoadMore(int current_page) {
                LoggerManager.e("munx", "onLoadMore :" + current_page);

                addDataList(false);
            }

        };

        mListView.addOnScrollListener(mOnScrollListener);

        mAdapter = new CommentListReAdapter(mListData, mListView.getLayoutManager(), mListView);
        mAdapter.setMember(isMember);
        mAdapter.setFeedData(mFeedData);
        mAdapter.setCallback(new CommentListReAdapter.Callback() {
            @Override
            public void onClick(int position) {
                if(mListData!=null && mListData.size() > position){
                    FeedData mFeedData = mListData.get(position);
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

                                                                    if(TextUtils.isEmpty(childkey)) {
                                                                        return;
                                                                    }
                                                                    if(TextUtils.isEmpty(commentChild)) {
                                                                        return;
                                                                    }

                                                                    if (!TextUtils.isEmpty(item.getKey())) {
                                                                        Fire.getReference().child(childkey).child(commentChild).child(item.getKey()).removeValue();
                                                                        Fire.getReference().child(Fire.KEY_STORY).child(commentChild).child("chatCount").runTransaction(setCountTransactionStepMinus());

                                                                        mListData.remove(item);
                                                                        mAdapter.notifyDataSetChanged();
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
                                }catch (Exception e){}

                            }
                        });
                        popup.show();
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
        et_comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                }
                return true;
            }
        });

        btn_send = (TextView) mRootView.findViewById(R.id.btn_send);
        btn_send.setEnabled(false);
        btn_send.setOnClickListener(this);

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


        if(TextUtils.isEmpty(commentChild) || commentChild.length() < 3) {
            getActivity().onBackPressed();
        }


        return mRootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send:{
                sendMessage();
                break;
            }
        }
    }


    boolean isMessage = false;
    synchronized private void sendMessage(){
        if(isMessage)
            return;

        String message = et_comment.getText().toString().trim();
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




        String color = null;
        String myName = "";
        String pImg = "";
        try {
            for (RoomUserData u : mFeedData.getListRoomUserData()) {
                LoggerManager.e("munx", u.getUid() + " / "+ u.getUserName());
                if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                    myName = u.getUserName();
                    color = u.getColor();
                    pImg = u.getpImg();
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
        f.setUid(MainActivity.mUserData.getUid());
        f.setText(message);
        f.setTime(Fire.getServerTimestamp());

        if(!TextUtils.isEmpty(pImg))f.setpImg(pImg);
        if(!TextUtils.isEmpty(color))f.setColor(color);


        isMessage = true;
        Fire.getReference().child(childkey).child(commentChild).push().setValue(f.getHashMap(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                isMessage = false;

                if(databaseError!= null && !TextUtils.isEmpty(databaseError.getMessage())){
                }
                else if(!TextUtils.isEmpty(databaseReference.getKey())){
                    f.setKey(databaseReference.getKey());
                    mListData.add(0, f);
                    mAdapter.notifyDataSetChanged();

                    Fire.getReference().child(Fire.KEY_STORY).child(commentChild).child("chatCount").runTransaction(setCountTransactionStep1());

                    FirebaseAnalyticsLog.setChatSend(getActivity(), "story_comment");

                    mListView.scrollToPosition(0);
                }
            }
        });

        et_comment.setText("");

//        sendChatPushBadge();


        try {
            GlobalApplication.runBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        ArrayList<String> list = new ArrayList<String>();
                        for (RoomUserData uu : mFeedData.getListRoomUserData()) {
                            if (uu.getOpen() == 1 && !uu.getUid().equals(MainActivity.mUserData.getUid())) {
                                String pu = DBManager.createInstnace(getActivity()).getUserPush(uu.getUid());
                                if (!TextUtils.isEmpty(pu)) {
                                    list.add(pu);

                                    LoggerManager.e("mun", "chat push : " + uu.getUid() + " / " + pu);
                                } else {
                                    LoggerManager.e("mun", "chat push : " + uu.getUid() + " / no push");
                                }
                            }
                        }

                        new SendPushFCMStory(getActivity(), list, SendPushFCMStory.STORY_MSG_TYPE_COMMENT, mFeedData.getKey(), mFeedData.getTitle()).start();
                    } catch (Exception e) {
                    }
                }
            });
        }catch (Exception e){}



        try{
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_comment.getWindowToken(), 0);
        }catch (Exception e){}



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

        if(TextUtils.isEmpty(childkey)) {
            return;
        }
        if(TextUtils.isEmpty(commentChild)) {
            return;
        }




        Query mQuery = null;

        if(replace){
            mQuery = Fire.getReference().child(childkey).child(commentChild).orderByChild("time").limitToLast(LimitCount);
        }
        else{
            mQuery = Fire.getReference().child(childkey).child(commentChild).orderByChild("time").endAt(searchTime).limitToLast(LimitCount);
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
                try {
                    if (databaseError != null) {
                        LoggerManager.e("firebase", "type1 : " + databaseError.getCode() + " , " + databaseError.getMessage());
                    } else {
                        long commentCount = (Long) dataSnapshot.getValue();
                        if(commentCount >= 0)commentData.setChatCount(commentCount);
                        LoggerManager.e("firebase", "plus : " + commentCount);

                        Intent u = new Intent();
                        u.putExtra("count", commentCount);
                        u.putExtra("position",commentPosition);
                        getActivity().setResult(getActivity().RESULT_OK, u);
                    }
                }catch (Exception e){}

            }
        };
    }

    private Transaction.Handler setCountTransactionStepMinus(){
        return new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                try {

                    if (mutableData.getValue() == null) {
                        mutableData.setValue(0);
                        return Transaction.success(mutableData);
                    }

                    long p = (Long) mutableData.getValue();

                    if (p < 1) {
                        mutableData.setValue(0);
                        return Transaction.success(mutableData);
                    }
                    mutableData.setValue(p - 1);
                }catch (Exception e){}

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                try {
                    if (databaseError != null) {
                        LoggerManager.e("firebase", "type1 : " + databaseError.getCode() + " , " + databaseError.getMessage());
                    } else {
                        long commentCount = (Long) dataSnapshot.getValue();
                        if(commentCount >= 0)commentData.setChatCount(commentCount);
                        LoggerManager.e("firebase", "minus : " + commentCount);

                        Intent u = new Intent();
                        u.putExtra("count", commentCount);
                        u.putExtra("position",commentPosition);
                        getActivity().setResult(getActivity().RESULT_OK, u);
                    }
                }catch (Exception e){}


            }
        };
    }

}
