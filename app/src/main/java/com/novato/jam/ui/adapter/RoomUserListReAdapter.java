package com.novato.jam.ui.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.AdmobNativeData;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.dialog.CustomAlertDialog;
import com.novato.jam.firebase.Fire;
import com.novato.jam.ui.MainActivity;

import java.util.List;
import java.util.Random;


/**
 * Created by poshaly on 2017. 1. 16..
 */

public class RoomUserListReAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final static public int ROWTYPE_TITLE = 112;

    private String roomUid;
    RecyclerView.LayoutManager layoutManager;
    private RecyclerView mListView;
    List<RoomUserData> mItems;
    Callback mCallback;

    public void setCallback(Callback mCallback){
        this.mCallback = mCallback;
    }

    public RoomUserListReAdapter(String roomUid, List<RoomUserData> versionModels, RecyclerView.LayoutManager layoutManager, RecyclerView mListView) {
        this.roomUid = roomUid;
        this.mListView = mListView;
        this.mItems = versionModels;
        this.layoutManager = layoutManager;
    }


    @Override
    public int getItemViewType(int position) {
        if(mItems.get(position).getRowType() == ROWTYPE_TITLE){
            return ROWTYPE_TITLE;
        }

        return 1;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ROWTYPE_TITLE){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_title, parent, false);
            return new TitleViewHolder(view, viewType);
        }
//        else{
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_memory_row, parent, false);
//            return new OthereMemoryViewHolder(view, viewType);
//        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_roomuser, parent, false);
        return new FeedViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof TitleViewHolder){
            onBindViewHolders((TitleViewHolder)holder, position);
        }
//        else if(holder instanceof OthereMemoryViewHolder){
//            onBindViewHolders((OthereMemoryViewHolder)holder, position);
//        }

//        holder.setIsRecyclable(false);

        else
            onBindViewHolders((FeedViewHolder) holder, position);

    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }


    /***********
     * dataSet
     */
    private void onBindViewHolders(final TitleViewHolder holder, final int position){
        final RoomUserData item = mItems.get(position);

        if(item==null){
            return;
        }

        holder.tv_title.setText(item.getUserName());
    }
    private void onBindViewHolders(final FeedViewHolder holder, final int position){

        final RoomUserData item = mItems.get(position);

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

//        Glide.with(mListView.getContext())
//                .load(item.getpImg()+"")
//                .bitmapTransform(new CropCircleTransformation(mListView.getContext()))
////                .placeholder(R.drawable.icon_progress)
////                        .placeholder(null)
//                .skipMemoryCache(true)
////                .error(R.drawable.none_img)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(holder.iv_img);
//        Glide.with(mListView.getContext())
//                .load(item.getpImg()+"")
////                .bitmapTransform(new CropCircleTransformation(mContext))
////                .placeholder(R.drawable.icon_progress)
////                        .placeholder(null)
//                .skipMemoryCache(true)
////                .error(R.drawable.profle)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .bitmapTransform(new CropCircleTransformation(mListView.getContext()))
//                .into(holder.iv_profile);


        holder.tv_title.setText(item.getUserName()+"");
        holder.tv_msg.setText(item.getDesc()+"");

//        try {
//            holder.tv_in.setText(item.getUserName());
//        }catch (Exception e){}
//        setSuctomBoldFont(holder.tv_title);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback!=null){
                    mCallback.onClick(position, item);
                }
            }
        });


        if(roomUid.equals(item.getUid())){
            holder.iv_admin.setVisibility(View.VISIBLE);
        }
        else{
            holder.iv_admin.setVisibility(View.GONE);
        }

        holder.btn_more.setVisibility(View.VISIBLE);
        holder.btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback!=null){
                    mCallback.onMoreClick(position, item);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(MainActivity.isAdmin) {
                    CustomAlertDialog mCustomAlertDialog2 = new CustomAlertDialog(mListView.getContext(), "영구정지 처리", item.getUserName() + "\n영구정지 처리 하시겠습니까??", new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                        @Override
                        public void onClickOk() {


                            if (!TextUtils.isEmpty(item.getUid())) {
                                Fire.getReference().child(Fire.KEY_BLOCK_USER).child(item.getUid()).setValue(Utils.getDateTimeString(Fire.getServerTimestamp()));
                            }

                        }

                        @Override
                        public void onClickCancel() {
                        }
                    });
                    mCustomAlertDialog2.show();
                }

                return false;
            }
        });

//        if(!TextUtils.isEmpty(roomUid) && MainActivity.mUserData!=null && !TextUtils.isEmpty(MainActivity.mUserData.getUid()) && roomUid.equals(MainActivity.mUserData.getUid())
//                && !roomUid.equals(item.getUid())
//                ){
//            holder.btn_more.setVisibility(View.VISIBLE);
//            holder.btn_more.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(mCallback!=null){
//                        mCallback.onMoreClick(position, item);
//                    }
//                }
//            });
//        }
//        else{
//            holder.btn_more.setVisibility(View.INVISIBLE);
//            holder.btn_more.setOnClickListener(null);
//        }


    }

    /***********
     * holder
     */

    class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView iv_img;
        TextView tv_title, tv_msg, tv_in;

        View btn_more, iv_admin;

        public FeedViewHolder(View v, int viewType) {
            super(v);

            iv_img = (ImageView) v.findViewById(R.id.iv_img);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_msg = (TextView) v.findViewById(R.id.tv_msg);

            btn_more = v.findViewById(R.id.btn_more);
            iv_admin = v.findViewById(R.id.iv_admin);

            tv_in = v.findViewById(R.id.tv_in);
        }



        @Override
        public void onClick(View v) {

        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_title;

        public TitleViewHolder(View v, int viewType) {
            super(v);

            tv_title = (TextView) v.findViewById(R.id.tv_title);
        }



        @Override
        public void onClick(View v) {

        }
    }


    public interface Callback{
        void onClick(final int position, final RoomUserData item);
        void onLongClick(final int position, final RoomUserData item);
        void onMoreClick(final int position, final RoomUserData item);
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
