package com.novato.jam.ui.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.glide.CropCircleBlurTransformation;
import com.glide.CropCircleTransformation;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.PermissionOk;
import com.novato.jam.common.Utils;
import com.novato.jam.data.AdmobNativeData;
import com.novato.jam.data.FeedData;
import com.novato.jam.dialog.BottomAudioDialog;
import com.novato.jam.dialog.CustomAlertDialog;
import com.novato.jam.firebase.Fire;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.fragment.BaseRoomFragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;


/**
 * Created by poshaly on 2017. 1. 16..
 */

public class ChatListReAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final static public int TYPE_ADMOB = 777;
    final static public int TYPE_ME = 601;
    final static public int TYPE_EMPTY = 701;

    final static public int TYPE_CATE_ROOMREMOVE = 801;
    final static public int TYPE_CATE_HEART = 802;
    final static public int TYPE_CATE_BOOM = 803;
    final static public int TYPE_CATE_USERIN = 702;
    final static public int TYPE_CATE_USEROUT = 703;
    final static public int TYPE_CATE_BAN = -2;

    RecyclerView.LayoutManager layoutManager;
    private RecyclerView mListView;
    List<FeedData> mItems;
    Callback mCallback;

    Fragment mFragment;

    public void setCallback(Callback mCallback){
        this.mCallback = mCallback;
    }

    public ChatListReAdapter(Fragment activity, List<FeedData> versionModels, RecyclerView.LayoutManager layoutManager, RecyclerView mListView) {
        this.mFragment = activity;
        this.mListView = mListView;
        this.mItems = versionModels;
        this.layoutManager = layoutManager;
    }


    @Override
    public int getItemViewType(int position) {
//        if(mItems.get(position).getType() == PokemonListData.TYPE_ME){
//            return PokemonListData.TYPE_ME;
//        }


        if(mItems.size() > position && mItems.get(position).getRowType() == TYPE_ADMOB){
            return TYPE_ADMOB;
        }
        else if(mItems.size() > position && mItems.get(position).getRowType() == TYPE_EMPTY){
            return TYPE_EMPTY;
        }
        else if(mItems.size() > position
                && (mItems.get(position).getCate() == TYPE_CATE_USERIN
                || mItems.get(position).getCate() == TYPE_CATE_USEROUT
                || mItems.get(position).getCate() == TYPE_CATE_BAN
                || mItems.get(position).getCate() == TYPE_CATE_HEART
                || mItems.get(position).getCate() == TYPE_CATE_BOOM
        )){
            return (int)mItems.get(position).getCate();
        }
        else if(MainActivity.mUserData != null
                && MainActivity.mUserData .getUid() != null
                && mItems.size() > position
                && mItems.get(position).getUid().equals(MainActivity.mUserData .getUid())
                ){
            return TYPE_ME;
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

        if(viewType == ChatListReAdapter.TYPE_ADMOB){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_admob_native_chat, parent, false);
            return new AdmobHolder(view, viewType);
        }
        else if(viewType == ChatListReAdapter.TYPE_ME){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_my, parent, false);
            return new FeedMyViewHolder(view, viewType);
        }
        else if(viewType == ChatListReAdapter.TYPE_EMPTY){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_empty, parent, false);
            return new EmptyViewHolder(view, viewType);
        }
        else if(viewType == TYPE_CATE_USERIN || viewType == TYPE_CATE_USEROUT || viewType == TYPE_CATE_BAN|| viewType == TYPE_CATE_HEART || viewType == TYPE_CATE_BOOM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user_inout, parent, false);
            return new UserInOutViewHolder(view, viewType);
        }


        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat, parent, false);
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

        if(holder instanceof AdmobHolder) {
            onBindViewHoldersAdmob((AdmobHolder) holder, position);
        }
        else if(holder instanceof FeedMyViewHolder) {
            onBindViewHolders((FeedViewHolder) holder, position);
        }
        else if(holder instanceof EmptyViewHolder) {
            onBindViewHoldersEmpty((EmptyViewHolder) holder, position);
        }
        else if(holder instanceof UserInOutViewHolder) {
            onBindViewHoldersUserInOut((UserInOutViewHolder) holder, position);
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
    private void onBindViewHoldersEmpty(final EmptyViewHolder holder, final int position){

    }
    private void onBindViewHoldersUserInOut(final UserInOutViewHolder holder, final int position){
        final FeedData item = mItems.get(position);

        if(item==null){
            return;
        }

        if(item.getCate() == TYPE_CATE_USERIN) {
            holder.tv_text.setText(String.format(GlobalApplication.getAppResources().getString(R.string.noti_in), item.getUserName()));
            holder.tv_text.setTextColor(ContextCompat.getColor(mListView.getContext(), R.color.blue));
        }
        else if(item.getCate() == TYPE_CATE_USEROUT) {
            holder.tv_text.setText(String.format(GlobalApplication.getAppResources().getString(R.string.noti_out), item.getUserName()));
            holder.tv_text.setTextColor(ContextCompat.getColor(mListView.getContext(), R.color.pink));
        }
        else if(item.getCate() == TYPE_CATE_BAN) {
            holder.tv_text.setText(String.format(GlobalApplication.getAppResources().getString(R.string.noti_ban), item.getUserName()));
            holder.tv_text.setTextColor(ContextCompat.getColor(mListView.getContext(), R.color.pink));
        }
        else if(item.getCate() == TYPE_CATE_HEART || item.getCate() == TYPE_CATE_BOOM) {
            holder.tv_text.setText(item.getText());
            holder.tv_text.setTextColor(ContextCompat.getColor(mListView.getContext(), R.color.pink));
        }

    }


    private com.novato.jam.customview.HtmlTextView lastView;
    private void onBindViewHolders(final FeedViewHolder holder, final int position){

        final FeedData item = mItems.get(position);

        if(item==null){
            return;
        }

//        holder.iv_icon


//        holder.iv_icon.setBackgroundDrawable(getRandomDrawbleColor());
//        holder.iv_profile.setBackgroundDrawable(getRandomDrawbleColor());

        ColorDrawable mColorDrawable;
        if(!TextUtils.isEmpty(item.getColor())){
            mColorDrawable = new ColorDrawable(Color.parseColor("#"+item.getColor()));
        }
        else {
            mColorDrawable = Utils.getRandomDrawbleColor();
        }
        holder.iv_img.setImageDrawable(mColorDrawable);
//
//        Glide.with(mListView.getContext())
//                .load(item.getpImg()+"")
//                .bitmapTransform(new CropCircleTransformation(mListView.getContext()))
////                .placeholder(R.drawable.icon_progress)
////                        .placeholder(null)
//                .skipMemoryCache(true)
////                .error(R.drawable.none_img)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(holder.iv_img);

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
                                    holder.tv_in.setText(item.getUserName() + "");
                                    holder.tv_in.setText(holder.tv_in.getText().toString().trim());
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

            try {
                holder.tv_in.setText("");
            }catch (Exception e){}

        }
        else{
            try {
                holder.tv_in.setText(item.getUserName()+"");
                holder.tv_in.setText(holder.tv_in.getText().toString().trim());
            }catch (Exception e){}
        }


        holder.btn_image.setVisibility(View.GONE);

        holder.tv_title.setText(item.getUserName()+"");
//        holder.tv_msg.setText(item.getText()+"");

        if(!TextUtils.isEmpty(item.getMic())){
            holder.tv_msg.setHtmlText("");

            holder.tv_msg.setVisibility(View.GONE);
            holder.btn_voice.setVisibility(View.VISIBLE);
        }
        else if(!TextUtils.isEmpty(item.getImg())){

            Glide.with(mListView.getContext())
                .load("https://docs.google.com/uc?export=download&id=" + item.getImg())
//                .placeholder(R.drawable.icon_progress)
//                        .placeholder(null)
//                .error(R.drawable.none_img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.iv_image);


            holder.btn_image.setVisibility(View.VISIBLE);


            holder.tv_msg.setHtmlText("");
            holder.tv_msg.setVisibility(View.GONE);
            holder.btn_voice.setVisibility(View.GONE);

            holder.iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(mCallback!=null)mCallback.onRemoveClick(position, item);
                    }catch (Exception e){}
                }
            });
        }
        else{
            holder.tv_msg.setHtmlText(item.getText()+"");

            holder.tv_msg.setVisibility(View.VISIBLE);
            holder.btn_voice.setVisibility(View.GONE);
        }

        holder.tv_time.setText(Utils.getDateTimeString2(item.getTime()));

//        setSuctomBoldFont(holder.tv_title);

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mCallback!=null){
//                    mCallback.onClick(position);
//                }
//            }
//        });

        holder.iv_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback!=null){
                    mCallback.onClick(position);
                }
            }
        });

        holder.btn_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play("https://docs.google.com/uc?export=download&id=" + item.getMic());
            }
        });





    }

    private long audioTime;
    synchronized private void play(String url) {

        if (mFragment!=null && PermissionOk.checkPermissionOne(mFragment.getActivity(), Manifest.permission.RECORD_AUDIO)) {
            if (audioTime > System.currentTimeMillis() - 1000 * 1) {
                return;
            }
            audioTime = System.currentTimeMillis();
            new BottomAudioDialog(mListView.getContext(), url, null).show();
        }
        else{
            if(mFragment instanceof BaseRoomFragment) {
                PermissionOk.checkPermission2(mFragment.getActivity(), ((BaseRoomFragment)mFragment).MY_PERMISSIONS_REQUEST_READ_CONTACTS, new PermissionOk.Callback() {
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
    }

    private void onBindViewHoldersAdmob(final AdmobHolder holder, final int position) {
        boolean isStagger = false;
        try {
            if (layoutManager instanceof StaggeredGridLayoutManager) {
                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                if (params == null) {
                    params = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
                    isStagger = true;
                } else {
                    if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                        StaggeredGridLayoutManager.LayoutParams staggeredParams = (StaggeredGridLayoutManager.LayoutParams) params;
                        staggeredParams.setFullSpan(true);
                        isStagger = true;
                    }
                }
                holder.itemView.setLayoutParams(params);
            }
        }catch (Exception e){}


//        holder.setLoadingStart(holder);

        holder.adView.setVisibility(View.GONE);
        final FeedData item = mItems.get(position);

        if( AdmobNativeData.isLoadedAd(item.getAdmobNativeData()) ){
            if(item.getAdmobNativeData().getNativeAppInstallAd() != null) {
                holder.setView(holder, item.getAdmobNativeData().getNativeAppInstallAd());
            }
            else if(item.getAdmobNativeData().getContentAd() != null){
                holder.setView(holder, item.getAdmobNativeData().getContentAd());
            }
            holder.layout_root.setPadding(0, Utils.getPixSize(mListView.getContext(), 16), 0, 0);
        }
        else{
            if(item.getAdmobNativeData()==null) {
                LoggerManager.e("mun","load go");


                AdListener errListener = new AdListener(){
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        LoggerManager.e("mun","onAdFailedToLoad "+errorCode);
                        if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION)
                        {
                            int p = holder.getAdapterPosition();
                            LoggerManager.e("mun","replce err position "+p);
                            FeedData itemx = mItems.get(p);

                            if(itemx.getRowType() == TYPE_ADMOB) {
                                try {
                                    itemx.getAdmobNativeData().getNativeAppInstallAd().destroy();
                                } catch (Exception e) {
                                }
                                try {
                                    itemx.getAdmobNativeData().setNativeAppInstallAd(null);
                                } catch (Exception e) {
                                }
                                try {
                                    itemx.setAdmobNativeData(null);
                                } catch (Exception e) {
                                }
//                                notifyItemChanged(p);
//                                notifyDataSetChanged();
                                try {
                                    int i = 0;
                                    for (FeedData m : mItems) {
                                        if (m.getRowType() == TYPE_ADMOB) {
                                            notifyItemChanged(i);
                                        }
                                        i++;
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                        else{
                            LoggerManager.e("mun","onAdFailedToLoad none position");
                        }
                    }
                };


                AdLoader adLoader =  null;

                Random r = new Random();
                int z = r.nextInt(2);
                LoggerManager.e("mun", "ads :" + z);
                if(z == 0){
                    NativeAppInstallAd.OnAppInstallAdLoadedListener listener = new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
                        @Override
                        public void onAppInstallAdLoaded(NativeAppInstallAd nativeAppInstallAd) {
                            LoggerManager.e("mun","onAppInstallAdLoaded");
                            if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION)
                            {
                                int p = holder.getAdapterPosition();
                                LoggerManager.e("mun","replce position "+p);

                                if(mItems.get(p).getRowType() == TYPE_ADMOB) {
                                    mItems.get(p).getAdmobNativeData().setNativeAppInstallAd(nativeAppInstallAd);
//                                notifyDataSetChanged();
//                                notifyItemChanged(p);
                                    try {
                                        int i = 0;
                                        for (FeedData m : mItems) {
                                            if (m.getRowType() == TYPE_ADMOB) {
                                                notifyItemChanged(i);
                                            }
                                            i++;
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            else{
                                LoggerManager.e("mun","onAppInstallAdLoaded none position");
                            }
                        }
                    };
                    adLoader = AdmobNativeData.getNewAd(listener , errListener);
                }
                else{
                    NativeContentAd.OnContentAdLoadedListener listener = new NativeContentAd.OnContentAdLoadedListener(){

                        @Override
                        public void onContentAdLoaded(NativeContentAd nativeContentAd) {
                            LoggerManager.e("mun","onAppInstallAdLoaded");
                            if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION)
                            {
                                int p = holder.getAdapterPosition();
                                LoggerManager.e("mun","replce position "+p);

                                if(mItems.get(p).getRowType() == TYPE_ADMOB) {
                                    mItems.get(p).getAdmobNativeData().setNativeContentAd(nativeContentAd);
                                    //                                notifyDataSetChanged();
                                    //                                notifyItemChanged(p);
                                    try {
                                        int i = 0;
                                        for (FeedData m : mItems) {
                                            if (m.getRowType() == TYPE_ADMOB) {
                                                notifyItemChanged(i);
                                            }
                                            i++;
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            else{
                                LoggerManager.e("mun","onAppInstallAdLoaded none position");
                            }
                        }
                    };
                    adLoader = AdmobNativeData.getNewAd(listener , errListener);
                }






                item.setAdmobNativeData(new AdmobNativeData(adLoader));
            }
        }





//        holder.layout_root.removeAllViews();
//
//        com.google.android.gms.ads.AdView adView22 = new com.google.android.gms.ads.AdView(mListView.getContext());
////        if(isStagger){
////            adView22.setAdSize(AdSize.LARGE_BANNER);
////        }
////        else {
////            adView22.setAdSize(new AdSize(180, 278));
////        }
//        adView22.setAdSize(AdSize.SMART_BANNER);
//        adView22.setAdUnitId("");
//        holder.layout_root.addView(adView22, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
//        adView22.loadAd(adRequest);

    }


    /***********
     * holder
     */

    class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView iv_img, iv_image;
        View btn_voice, btn_image;
        TextView tv_title,tv_time, tv_in;
        com.novato.jam.customview.HtmlTextView tv_msg;

        public FeedViewHolder(View v, int viewType) {
            super(v);

            iv_img = (ImageView) v.findViewById(R.id.iv_img);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_msg = (com.novato.jam.customview.HtmlTextView) v.findViewById(R.id.tv_msg);
            tv_time = (TextView) v.findViewById(R.id.tv_time);
            tv_in = v.findViewById(R.id.tv_in);
            btn_voice = v.findViewById(R.id.btn_voice);
            btn_image = v.findViewById(R.id.btn_image);
            iv_image = v.findViewById(R.id.iv_image);
        }

        @Override
        public void onClick(View v) {

        }
    }
    class FeedMyViewHolder extends FeedViewHolder implements View.OnClickListener {

//        ImageView iv_img;
//        TextView tv_title,tv_msg,tv_time;

        public FeedMyViewHolder(View v, int viewType) {
            super(v, viewType);

//            iv_img = (ImageView) v.findViewById(R.id.iv_img);
//            tv_title = (TextView) v.findViewById(R.id.tv_title);
//            tv_msg = (TextView) v.findViewById(R.id.tv_msg);
//            tv_time = (TextView) v.findViewById(R.id.tv_time);
        }

        @Override
        public void onClick(View v) {

        }
    }


    class EmptyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public EmptyViewHolder(View itemView, int viewType) {
            super(itemView);
        }
        @Override
        public void onClick(View v) {

        }
    }

    class UserInOutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_text;
        public UserInOutViewHolder(View itemView, int viewType) {
            super(itemView);

            tv_text = itemView.findViewById(R.id.tv_text);
        }
        @Override
        public void onClick(View v) {

        }
    }


    class AdmobHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        FrameLayout layout_root;
        NativeAppInstallAdView adView;
        public AdmobHolder(View v, int viewType) {
            super(v);

            layout_root = (FrameLayout) v.findViewById(R.id.layout_root);
//            admob = (com.google.android.gms.ads.AdView) v.findViewById(R.id.admob);
//            admob.setAdSize(new AdSize(-1,-2));

            adView = v.findViewById(R.id.lay_adview);

            LoggerManager.e("munx", "AdmobHolder create " + mListView.getContext().getClass().getName());

        }
        @Override
        public void onClick(View v) {

        }

        private void setView(AdmobHolder holder, NativeAppInstallAd nativeAppInstallAd){

            NativeAppInstallAdView adView = holder.adView;

            TextView headlineView = adView.findViewById(R.id.tv_title);
            headlineView.setText(nativeAppInstallAd.getHeadline());
            adView.setHeadlineView(headlineView);


            TextView tv_message = adView.findViewById(R.id.tv_message);
            tv_message.setText(nativeAppInstallAd.getBody());
            adView.setBodyView(tv_message);


//            MediaView mediaView = adView.findViewById(R.id.iv_cover);
//            try {
//                if (nativeAppInstallAd.getVideoController().hasVideoContent()) {
//                    mediaView.setVisibility(View.VISIBLE);
//                    adView.setMediaView(mediaView);
//                } else {
//                    mediaView.setVisibility(View.GONE);
//                }
//            } catch (Exception e) {
//            }

//                            ImageView iv_main = adView.findViewById(R.id.iv_main);
//                            iv_main.setVisibility(View.VISIBLE);
//                            List<com.google.android.gms.ads.formats.NativeAd.Image> images = contentAd.getImages();
//                            if(images!=null && images.size() > 0)iv_main.setImageDrawable(images.get(0).getDrawable());
//                            adView.setImageView(iv_main);

            ImageView icon = adView.findViewById(R.id.iv_icon);
            try {
                icon.setImageDrawable(nativeAppInstallAd.getIcon().getDrawable());
            } catch (Exception e) {
            }
            adView.setIconView(icon);

            com.google.android.gms.ads.formats.AdChoicesView ad_choices_container = adView.findViewById(R.id.ad_choices_container);
            adView.setAdChoicesView(ad_choices_container);

            Button btn = adView.findViewById(R.id.native_ad_call_to_action);
            btn.setText(nativeAppInstallAd.getCallToAction());
            adView.setCallToActionView(btn);


            adView.setNativeAd(nativeAppInstallAd);
            adView.setVisibility(View.VISIBLE);
        }
        private void setView(AdmobHolder holder, NativeContentAd nativeAppInstallAd){

            NativeAppInstallAdView adView = holder.adView;

            TextView headlineView = adView.findViewById(R.id.tv_title);
            headlineView.setText(nativeAppInstallAd.getHeadline());
            adView.setHeadlineView(headlineView);


            TextView tv_message = adView.findViewById(R.id.tv_message);
            tv_message.setText(nativeAppInstallAd.getBody());
            adView.setBodyView(tv_message);


//            MediaView mediaView = adView.findViewById(R.id.iv_cover);
//            try {
//                if (nativeAppInstallAd.getVideoController().hasVideoContent()) {
//                    mediaView.setVisibility(View.VISIBLE);
//                    adView.setMediaView(mediaView);
//                } else {
//                    mediaView.setVisibility(View.GONE);
//                }
//            } catch (Exception e) {
//            }

//                            ImageView iv_main = adView.findViewById(R.id.iv_main);
//                            iv_main.setVisibility(View.VISIBLE);
//                            List<com.google.android.gms.ads.formats.NativeAd.Image> images = contentAd.getImages();
//                            if(images!=null && images.size() > 0)iv_main.setImageDrawable(images.get(0).getDrawable());
//                            adView.setImageView(iv_main);

            ImageView icon = adView.findViewById(R.id.iv_icon);
            try {
                icon.setImageDrawable(nativeAppInstallAd.getLogo().getDrawable());
            } catch (Exception e) {
            }
            adView.setIconView(icon);

            com.google.android.gms.ads.formats.AdChoicesView ad_choices_container = adView.findViewById(R.id.ad_choices_container);
            adView.setAdChoicesView(ad_choices_container);

            Button btn = adView.findViewById(R.id.native_ad_call_to_action);
            btn.setText(nativeAppInstallAd.getCallToAction());
            adView.setCallToActionView(btn);


            adView.setNativeAd(nativeAppInstallAd);
            adView.setVisibility(View.VISIBLE);
        }


//        AdLoader adLoader;
//        NativeContentAd contentAd;
//        NativeAppInstallAd mNativeAppInstallAd;
//        long time = 0;
//        synchronized public void setLoadingStart(final AdmobHolder holder){
//            VideoOptions videoOptions = new VideoOptions.Builder()
//                    .setStartMuted(true)
//                    .build();
//
//
//            NativeAdOptions adOptions = new NativeAdOptions.Builder()
//                    .setImageOrientation(NativeAdOptions.ORIENTATION_PORTRAIT)
//                    .setVideoOptions(videoOptions)
//                    .build();
//
//            if(adLoader != null && adLoader.isLoading()){
//                return;
//            }
////            if(contentAd!=null){
////                return;
////            }
////            if(mNativeAppInstallAd!=null){
////                return;
////            }
//            if(Fire.getServerTimestamp() - (1000 * 60) < time){
//                return;
//            }
//
//            time = Fire.getServerTimestamp();
//            adLoader = new AdLoader.Builder(GlobalApplication.getAppContext(), Adinit.getInstance().getNativeId())
//                    .forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
//                        @Override
//                        public void onAppInstallAdLoaded(NativeAppInstallAd appInstallAd) {
//                            try {
//                                if (contentAd != null) contentAd.destroy();
//                                contentAd = null;
//                            }catch (Exception e){}
//                            try {
//                                if (mNativeAppInstallAd != null) mNativeAppInstallAd.destroy();
//                                mNativeAppInstallAd = null;
//                            }catch (Exception e){}
//                            try {
//                                holder.layout_root.removeAllViews();
//                            }catch (Exception e){}
//
//                            mNativeAppInstallAd = appInstallAd;
//
//                            LayoutInflater inflater = (LayoutInflater) holder.layout_root.getContext()
//                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                            NativeAppInstallAdView adView = (NativeAppInstallAdView) inflater
//                                    .inflate(R.layout.row_admob_native_small, null);
//
//
//
//                            TextView headlineView = adView.findViewById(R.id.tv_title);
//                            headlineView.setText(mNativeAppInstallAd.getHeadline());
//                            adView.setHeadlineView(headlineView);
//
//
//                            TextView tv_message = adView.findViewById(R.id.tv_message);
//                            tv_message.setText(mNativeAppInstallAd.getBody());
//                            adView.setBodyView(tv_message);
//
//
//                            MediaView mediaView = adView.findViewById(R.id.iv_cover);
//                            try {
//                                if (mNativeAppInstallAd.getVideoController().hasVideoContent()) {
//                                    mediaView.setVisibility(View.VISIBLE);
//                                    adView.setMediaView(mediaView);
//                                } else {
//                                    mediaView.setVisibility(View.GONE);
//                                }
//                            }catch (Exception e){}
//
////                            ImageView iv_main = adView.findViewById(R.id.iv_main);
////                            iv_main.setVisibility(View.VISIBLE);
////                            List<com.google.android.gms.ads.formats.NativeAd.Image> images = contentAd.getImages();
////                            if(images!=null && images.size() > 0)iv_main.setImageDrawable(images.get(0).getDrawable());
////                            adView.setImageView(iv_main);
//
//                            ImageView icon = adView.findViewById(R.id.iv_icon);
//                            try {
//                                icon.setImageDrawable(mNativeAppInstallAd.getIcon().getDrawable());
//                            }catch (Exception e){}
//                            adView.setIconView(icon);
//
//                            com.google.android.gms.ads.formats.AdChoicesView ad_choices_container = adView.findViewById(R.id.ad_choices_container);
//                            adView.setAdChoicesView(ad_choices_container);
//
//                            Button btn = adView.findViewById(R.id.native_ad_call_to_action);
//                            btn.setText(mNativeAppInstallAd.getCallToAction());
//                            adView.setCallToActionView(btn);
//
//
//                            adView.setNativeAd(mNativeAppInstallAd);
//                            holder.layout_root.addView(adView);
//
//                        }
//                    })
////                    .forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
////                        @Override
////                        public void onContentAdLoaded(NativeContentAd contentAds) {
////                            try {
////                                if (contentAd != null) contentAd.destroy();
////                                contentAd = null;
////                            }catch (Exception e){}
////                            try {
////                                if (mNativeAppInstallAd != null) mNativeAppInstallAd.destroy();
////                                mNativeAppInstallAd = null;
////                            }catch (Exception e){}
////                            try {
////                                holder.layout_root.removeAllViews();
////                            }catch (Exception e){}
////
////                            contentAd = contentAds;
////                            // Show the content ad.
////
////
////                            LayoutInflater inflater = (LayoutInflater) holder.layout_root.getContext()
////                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
////                            NativeAppInstallAdView adView = (NativeAppInstallAdView) inflater
////                                    .inflate(R.layout.row_admob_native_small, null);
////
////
////
////                            TextView headlineView = adView.findViewById(R.id.tv_title);
////                            headlineView.setText(contentAd.getHeadline());
////                            adView.setHeadlineView(headlineView);
////
////
////                            TextView tv_message = adView.findViewById(R.id.tv_message);
////                            tv_message.setText(contentAd.getBody());
////                            adView.setBodyView(tv_message);
////
////
//////                            MediaView mediaView = adView.findViewById(R.id.iv_cover);
//////                            if(contentAd.getVideoController().hasVideoContent()) {
//////                                mediaView.setVisibility(View.VISIBLE);
//////                                adView.setMediaView(mediaView);
//////                            }
//////                            else{
//////                                mediaView.setVisibility(View.GONE);
//////                            }
//////                            ImageView iv_main = adView.findViewById(R.id.iv_main);
//////                            iv_main.setVisibility(View.VISIBLE);
//////                            List<com.google.android.gms.ads.formats.NativeAd.Image> images = contentAd.getImages();
//////                            if(images!=null && images.size() > 0)iv_main.setImageDrawable(images.get(0).getDrawable());
//////                            adView.setImageView(iv_main);
////
////                            com.google.android.gms.ads.formats.AdChoicesView ad_choices_container = adView.findViewById(R.id.ad_choices_container);
////                            adView.setAdChoicesView(ad_choices_container);
////
////                            ImageView icon = adView.findViewById(R.id.iv_icon);
////                            try {
////                                icon.setImageDrawable(contentAd.getLogo().getDrawable());
////                            }catch (Exception e){}
////                            adView.setIconView(icon);
////
////                            Button btn = adView.findViewById(R.id.native_ad_call_to_action);
////                            btn.setText(contentAd.getCallToAction());
////                            adView.setCallToActionView(btn);
////
////
////
////                            adView.setNativeAd(contentAd);
////                            holder.layout_root.addView(adView);
////
////                        }
////                    })
//                    .withAdListener(new AdListener() {
//                        @Override
//                        public void onAdFailedToLoad(int errorCode) {
//                        }
//                    })
//                    .withNativeAdOptions(adOptions)
//                    .build();
//            adLoader.loadAd(new AdRequest.Builder().build());
//        }


    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);




        if(holder instanceof FeedViewHolder){
            LoggerManager.e("mun", "onViewDetachedFromWindow clear");
            FeedViewHolder h = (FeedViewHolder)holder;
            h.tv_msg.clear();
        }

    }


    public interface Callback{
        void onClick(final int position);
        void onLongClick(final int position);
        void onRemoveClick(final int position, FeedData item);
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
