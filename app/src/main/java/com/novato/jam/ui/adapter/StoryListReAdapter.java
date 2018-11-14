package com.novato.jam.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.glide.CropCircleTransformation;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.lib.hashtag.views.HashTagEditText;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.customview.CircleImageView;
import com.novato.jam.data.AdmobNativeData;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.dialog.CustomAlertDialog;
import com.novato.jam.facebookad.FBnative;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.fragment.EventOneFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by poshaly on 2017. 1. 16..
 */

public class StoryListReAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final static public int TYPE_EVENT = 779;

    RecyclerView.LayoutManager layoutManager;
    private RecyclerView mListView;
    List<FeedData> mItems;
    Callback mCallback;
    boolean isNew = false;
    boolean isSearch;
    boolean isMember;

    FeedData mFeedData;

    private FragmentManager mFragmentManager;

    public void setFragmentManager(FragmentManager mFragmentManager) {
        this.mFragmentManager = mFragmentManager;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public void setSearchType(boolean isSearch) {
        this.isSearch = isSearch;
    }

    public void setCallback(Callback mCallback){
        this.mCallback = mCallback;
    }

    public StoryListReAdapter(List<FeedData> versionModels, RecyclerView.LayoutManager layoutManager, RecyclerView mListView) {
        this.mListView = mListView;
        this.mItems = versionModels;
        this.layoutManager = layoutManager;
    }

    public void setMember(boolean isMember){
        this.isMember = isMember;
    }

    public void setFeedData(FeedData mFeedData){
        this.mFeedData = mFeedData;
    }


    @Override
    public int getItemViewType(int position) {
//        if(mItems.get(position).getType() == PokemonListData.TYPE_ME){
//            return PokemonListData.TYPE_ME;
//        }

        if(mItems.size() > position && mItems.get(position).getRowType() == TYPE_EVENT){
            return TYPE_EVENT;
        }


        return 1;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if(viewType == PokemonListData.TYPE_ME){
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_memory_row, parent, false);
//            return new MeMemoryViewHolder(view, viewType);
//        }
//        else{
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_memory_row, parent, false);
//            return new OthereMemoryViewHolder(view, viewType);
//        }

        if(viewType == StoryListReAdapter.TYPE_EVENT){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_event, parent, false);
            return new EventHolder(view, viewType);
        }


        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_story, parent, false);
        return new FeedViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        if(holder instanceof MeMemoryViewHolder){
//            onBindViewHolders((MeMemoryViewHolder)holder, position);
//        }
//        else if(holder instanceof OthereMemoryViewHolder){
//            onBindViewHolders((OthereMemoryViewHolder)holder, position);
//        }

//        holder.setIsRecyclable(false);

        if(holder instanceof EventHolder) {
            onBindViewHoldersEvent((EventHolder) holder, position);
        }
        else{
            onBindViewHolders((FeedViewHolder) holder, position);
        }

    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }


    /***********
     * dataSet
     */
    private void onBindViewHolders(final FeedViewHolder holder, final int position){

        final FeedData item = mItems.get(position);

        if(item==null){
            return;
        }


        ColorDrawable mColorDrawable = null;
        if(!TextUtils.isEmpty(item.getColor())){
            mColorDrawable = new ColorDrawable(Color.parseColor("#"+item.getColor()));

        }
        else {
            mColorDrawable = Utils.getRandomDrawbleColor();
        }
        if(mColorDrawable!=null)holder.iv_img.setImageDrawable(mColorDrawable);
//        holder.iv_profile.setBackgroundDrawable(getRandomDrawbleColor());
//
//
//        Glide.with(mListView.getContext())
//                .load(item.getImg()+"")
////                .bitmapTransform(new CropCircleTransformation(mContext))
////                .placeholder(R.drawable.icon_progress)
////                        .placeholder(null)
//                .skipMemoryCache(true)
////                .error(R.drawable.none_img)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(holder.iv_icon);

        try {
            holder.tv_in.setText("");
        }catch (Exception e){}



        holder.iv_image.setImageBitmap(null);

        if(!isMember && item.getOpen() != 1){
            holder.iv_image.setVisibility(View.GONE);
        }
        else if(!TextUtils.isEmpty(item.getImg())) {
            Glide.with(mListView.getContext())
                    .load("https://docs.google.com/uc?export=download&id=" + item.getImg() + "")
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.iv_image);
            holder.iv_image.setVisibility(View.VISIBLE);
        }
        else{
            holder.iv_image.setVisibility(View.GONE);
        }


        if(!TextUtils.isEmpty(item.getpImg())) {
            String url = "https://docs.google.com/uc?export=download&id=" + item.getpImg();
            Glide.with(mListView.getContext())
                    .load(url + "")
//                .load(getRandomDrawbleColor())//(item.getpImg()+"")
//                .bitmapTransform(new CropCircleTransformation(mListView.getContext()))
//                .placeholder(R.drawable.icon_progress)
                        .placeholder(mColorDrawable)
//                .skipMemoryCache(true)
//                .error(getRandomDrawbleColor())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .bitmapTransform(new CropCircleTransformation(mListView.getContext()))
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                            try {
                                if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION && holder.getAdapterPosition() == position) {
                                    if(!TextUtils.isEmpty(item.getTitle())) {
                                        holder.tv_in.setText(item.getTitle() + "");
                                        holder.tv_in.setText(holder.tv_in.getText().toString().trim());
                                    }
                                }

                            }catch (Exception x){}
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                            try {
                                if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION && holder.getAdapterPosition() == position)
                                    holder.iv_img.setImageDrawable(glideDrawable);
                            }catch (Exception e){}
                            return false;
                        }
                    })
                    .into(holder.iv_img);

        }
        else{
            try {
                if(!TextUtils.isEmpty(item.getUserName())) {
                    holder.tv_in.setText(item.getUserName() + "");
                    holder.tv_in.setText(holder.tv_in.getText().toString().trim());
                }
            }catch (Exception e){}
        }

        holder.tv_time.setText(Utils.getDateTimeString2(item.getTime()));


        if(!TextUtils.isEmpty(item.getUserName())) {
            holder.tv_title.setText(item.getUserName() + "");
        }
        else{
            holder.tv_title.setText("");
        }


        holder.lay_blind.setVisibility(View.GONE);
        if(!isMember && item.getOpen() != 1){
            holder.tv_msg.setVisibility(View.GONE);

            holder.lay_bottom.setVisibility(View.GONE);

            holder.lay_blind.setVisibility(View.VISIBLE);
        }
        else if(!TextUtils.isEmpty(item.getText())) {
            holder.tv_msg.setText(item.getText() + "");
            holder.tv_msg.setVisibility(View.VISIBLE);

            holder.lay_bottom.setVisibility(View.VISIBLE);
        }
        else {
            holder.tv_msg.setText("");
            holder.tv_msg.setVisibility(View.GONE);

            holder.lay_bottom.setVisibility(View.VISIBLE);
        }

        if(item.getOpen() != 1){
            holder.v_lock.setVisibility(View.VISIBLE);
        }
        else{
            holder.v_lock.setVisibility(View.GONE);
        }

        holder.btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback !=null){
                    mCallback.onClick(position, item);
                }
            }
        });


        if(item.getChatCount() > 0){
            holder.btn_comment.setText(mListView.getContext().getString(R.string.story_comment) + " " +item.getChatCount());
        }
        else{
            holder.btn_comment.setText(R.string.story_comment);
        }



        if(isSearch){
            holder.tv_msg.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(MainActivity.isAdmin) {
                    final FeedData mFeedData = item;
                }

                return false;
            }
        });

        holder.btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback!=null)mCallback.onMoreClick(v, position, item);
            }
        });

        holder.btn_more.setVisibility(View.GONE);
        try {
            if (item.getUid().equals(MainActivity.mUserData.getUid())
                    || mFeedData.getUid().equals(MainActivity.mUserData.getUid())
                    ) {
                holder.btn_more.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){}

    }


    synchronized private void onBindViewHoldersEvent(final EventHolder holder, final int position){
        if(mItems!=null && mItems.size() > position) {

            final FeedData item = mItems.get(position);
            if(item.getTime() <= 0){
                item.setTime(System.currentTimeMillis());


                Fire.getReference().child(Fire.KEY_EVENT).orderByChild("time").limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        try {
                            ArrayList<RoomUserData> list = Parser.getRoomUserDataListParse(dataSnapshot);
                            if (list != null) {
                                item.setListRoomUserData(list);
                                notifyDataSetChanged();
                            }
                        }catch (Exception e){}

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
            else{
                ArrayList<RoomUserData> list = item.getListRoomUserData();
                if(list!=null){
                    //pager setting

                    holder.setPage(mFragmentManager, list);
                }
            }

        }
    }



    /***********
     * holder
     */

    class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView iv_img;
        ImageView iv_image;
        TextView tv_title, tv_msg, tv_in, tv_time, btn_comment;


        View v_lock, lay_bottom, btn_more, lay_blind;

        public FeedViewHolder(View v, int viewType) {
            super(v);

            iv_img = v.findViewById(R.id.iv_img);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_msg = (TextView) v.findViewById(R.id.tv_msg);
            v_lock = v.findViewById(R.id.v_lock);
            tv_in = v.findViewById(R.id.tv_in);
            iv_image = v.findViewById(R.id.iv_image);
            tv_time = v.findViewById(R.id.tv_time);
            btn_comment = v.findViewById(R.id.btn_comment);
            lay_bottom = v.findViewById(R.id.lay_bottom);
            btn_more = v.findViewById(R.id.btn_more);
            lay_blind = v.findViewById(R.id.lay_blind);
        }


        @Override
        public void onClick(View v) {

        }
    }



    public class EventHolder extends RecyclerView.ViewHolder {
        private Handler mHandler = new Handler();
        private ArrayList<Fragment> fragments = new ArrayList<>();
        ViewPager mViewPager;
        MainPagerAdapter mPagerAdapter;


        public View origin_root, lay_root;

        public EventHolder(View v, int viewType){
            super(v);
            origin_root = v;
            mViewPager = v.findViewById(R.id.pager);
            lay_root = v.findViewById(R.id.lay_root);
        }

        private void setPage(FragmentManager manager, ArrayList<RoomUserData> f){
            this.fragments.clear();
            for(RoomUserData r :f){
                if(r.getOpen() == 1) {
                    EventOneFragment mEventOneFragment = new EventOneFragment();
                    mEventOneFragment.setmRoomUserData(r);
                    fragments.add(mEventOneFragment);
                }
            }
            if(fragments.size() > 0) {
                lay_root.setVisibility(View.VISIBLE);
                mViewPager.setOffscreenPageLimit(100);
                mPagerAdapter = new MainPagerAdapter(manager);
                mViewPager.setAdapter(mPagerAdapter);
                mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {
                        try {
                            if (mPagerAdapter != null) {
                                try {

                                } catch (Exception e) {
                                }
                            }
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                        if (ViewPager.SCROLL_STATE_DRAGGING == state) {
                            try {
                                mHandler.removeCallbacks(runAutopage);
                            } catch (Exception e) {
                            }
                        }
                    }
                });

//            com.rd.PageIndicatorView pageIndicatorView = mRootView.findViewById(R.id.pageIndicatorView);
//            pageIndicatorView.setViewPager(mViewPager);


                try {
                    mHandler.postDelayed(runAutopage, 3000);
                } catch (Exception e) {
                }
            }
        }

        int page = 0;
        Runnable runAutopage = new Runnable() {
            @Override
            public void run() {
                page++;

                try {
                    if (fragments.size() <= page) {
                        page = 0;
                    }
                    mViewPager.setCurrentItem(page);

                    mHandler.postDelayed(this, 3000);

                }catch (Exception e){}
            }
        };


        private class MainPagerAdapter extends FragmentStatePagerAdapter {

            public MainPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {

                return fragments.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);

                try{
                    fragments.get(position).onDestroyView();
                }catch (Exception e){}
                try{
                    fragments.get(position).onDestroy();
                }catch (Exception e){}
            }

            @Override
            public int getCount() {
                if(fragments==null){
                    return 0;
                }
                return fragments.size();  // 총 5개의 page를 보여줍니다.
            }

//        @Override
//        public CharSequence getPageTitle(int position) {
//            return "　 ";
////            return null;
//        }


        }


    }



    public interface Callback{
        void onClick(final int position, FeedData item);
        void onLongClick(final int position);
        void onMoreClick(View v , final int position, FeedData item);
    }



    Typeface fontNanum;
    public void setSuctomBoldFont(TextView textView){
        if(fontNanum == null)fontNanum = Typeface.createFromAsset(GlobalApplication.getAppContext().getAssets(), "fonts/NotoSerifCJKkr_Bold.otf");
        textView.setTypeface(fontNanum);
    }
    public void setSuctomBoldFont(EditText textView){
        if(fontNanum == null)fontNanum = Typeface.createFromAsset(GlobalApplication.getAppContext().getAssets(), "fonts/NotoSerifCJKkr_Bold.otf");
        textView.setTypeface(fontNanum);
    }


    private Transaction.Handler setCountTransaction(){
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












}
