package com.novato.jam.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.customview.CircleImageView;
import com.novato.jam.data.AdmobNativeData;
import com.novato.jam.data.FeedData;
import com.novato.jam.db.DBManager;
import com.novato.jam.dialog.CustomAlertDialog;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by poshaly on 2017. 1. 16..
 */

public class MessageListReAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final static public int TYPE_ADMOB = 777;

    RecyclerView.LayoutManager layoutManager;
    private RecyclerView mListView;
    List<FeedData> mItems;
    Callback mCallback;
    boolean isNew = false;

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public void setCallback(Callback mCallback){
        this.mCallback = mCallback;
    }

    public MessageListReAdapter(List<FeedData> versionModels, RecyclerView.LayoutManager layoutManager, RecyclerView mListView) {
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

        if(viewType == MessageListReAdapter.TYPE_ADMOB){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_admob_native, parent, false);
            return new AdmobHolder(view, viewType);
        }


        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message, parent, false);
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
        holder.iv_img.setImageDrawable(mColorDrawable);

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
                                    holder.tv_in.setText(item.getTitle() + "");
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
                holder.tv_in.setText(item.getTitle()+"");
                holder.tv_in.setText(holder.tv_in.getText().toString().trim());
            }catch (Exception e){}
        }

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
//        Glide.with(mListView.getContext())
//                .load(getRandomDrawbleColor())//(item.getpImg()+"")
////                .bitmapTransform(new CropCircleTransformation(mContext))
////                .placeholder(R.drawable.icon_progress)
////                        .placeholder(null)
//                .skipMemoryCache(true)
////                .error(getRandomDrawbleColor())
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .bitmapTransform(new CropCircleTransformation(mListView.getContext()))
//                .into(holder.iv_img);


        //뉴 표시 위해서 만들어둠...
//        LoggerManager.e("mun", DBManager.createInstnace(mListView.getContext()).getRoomBadge(item.getKey()) +" < "+item.getChatCount());
        if(isNew && DBManager.createInstnace(mListView.getContext()).getRoomBadge(item.getKey()) < item.getChatCount()){
            holder.iv_new.setVisibility(View.VISIBLE);
        }
        else {
            holder.iv_new.setVisibility(View.GONE);
        }

        holder.tv_title.setText(item.getTitle() + "");
        holder.tv_msg.setText(item.getText()+"");
        holder.tv_msg.setSingleLine(false);

//        try {
//            holder.tv_in.setText(item.getTitle()+"");
//        }catch (Exception e){}

        if(item.getOpen() == 1){
            holder.iv_lock.setVisibility(View.GONE);
        }
        else{
            holder.iv_lock.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback !=null){
                    mCallback.onClick(position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() { //  비밀 쪽지 수정
            @Override
                                    public boolean onLongClick(View v) { // 롱클릭시 메시지 지우기 띄움

                                        CustomAlertDialog mCustomAlertDialog2 = new CustomAlertDialog(mListView.getContext(), "비밀쪽지 지우기", item.getUserName() + "\n비밀쪽지를 지우시겠습니까??", new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                                            @Override
                                            public void onClickOk() {
                                                try{
                                                    DBManager.createInstnace(mListView.getContext()).deleteRecord(item.getTime());

                                                }catch (Exception e){
                                                }

                                                try { //지우면 바로 사라지게 하는 부분
                                                    int count = 0;
                                                    for(FeedData f :mItems){
                                                        if(f.getTime() == item.getTime()) {
                                                            mItems.remove(f);
                                    notifyItemRemoved(count);
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
                mCustomAlertDialog2.show();

                return true;
            }
        });

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
            holder.setView(holder, item.getAdmobNativeData().getNativeAppInstallAd());
        }
        else{
            if(item.getAdmobNativeData()==null) {
                LoggerManager.e("mun","load go");

                AdLoader adLoader = AdmobNativeData.getNewAd(
                    new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
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
                    }
                    ,new AdListener(){
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
                    }
                );
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

        CircleImageView iv_img;
        TextView tv_title,tv_msg, tv_in;


        View iv_lock, iv_new;

        public FeedViewHolder(View v, int viewType) {
            super(v);

            iv_img = v.findViewById(R.id.iv_img);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_msg = (TextView) v.findViewById(R.id.tv_msg);
            iv_lock = v.findViewById(R.id.iv_lock);
            tv_in = v.findViewById(R.id.tv_in);
            iv_new = v.findViewById(R.id.iv_new);
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


    public interface Callback{
        void onClick(final int position);
        void onLongClick(final int position);
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
