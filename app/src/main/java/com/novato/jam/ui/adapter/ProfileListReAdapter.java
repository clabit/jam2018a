package com.novato.jam.ui.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.glide.CropCircleTransformation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.dialog.CustomAlertDialog;
import com.novato.jam.firebase.Fire;
import com.novato.jam.ui.MainActivity;

import java.util.List;


/**
 * Created by poshaly on 2017. 1. 16..
 */

public class ProfileListReAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    RecyclerView.LayoutManager layoutManager;
    private RecyclerView mListView;
    List<String> mItems;
    Callback mCallback;

    public void setCallback(Callback mCallback){
        this.mCallback = mCallback;
    }

    public ProfileListReAdapter(List<String> versionModels, RecyclerView.LayoutManager layoutManager, RecyclerView mListView) {
        this.mListView = mListView;
        this.mItems = versionModels;
        this.layoutManager = layoutManager;
    }


    @Override
    public int getItemViewType(int position) {
        return 1;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_profile_img, parent, false);
        return new FeedViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        onBindViewHolders((FeedViewHolder) holder, position);

    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }


    /***********
     * dataSet
     */
    private void onBindViewHolders(final FeedViewHolder holder, final int position){

        LoggerManager.e("mun", "sunal ...");
        if(mItems!=null && mItems.size() > position) {
            final String item = mItems.get(position);
            LoggerManager.e("mun", "sunal "+ item);
            if (!TextUtils.isEmpty(item)) {
                String url = "https://docs.google.com/uc?export=download&id=" + item;
                Glide.with(mListView.getContext())
                        .load(url + "")
                        .placeholder(new ColorDrawable(Color.parseColor("#e7361e")))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .bitmapTransform(new CropCircleTransformation(mListView.getContext()))
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                                try {
                                    if (holder.getAdapterPosition() != RecyclerView.NO_POSITION && holder.getAdapterPosition() == position)
                                        holder.iv_img.setImageDrawable(glideDrawable);
                                } catch (Exception e) {
                                }
                                return false;
                            }
                        })
                        .into(holder.iv_img);


                holder.iv_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mCallback!=null){
                            mCallback.onClick(position, item);
                        }
                    }
                });


            }
        }

    }

    /***********
     * holder
     */

    class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView iv_img;

        public FeedViewHolder(View v, int viewType) {
            super(v);

            iv_img = (ImageView) v.findViewById(R.id.iv_img);
        }



        @Override
        public void onClick(View v) {

        }
    }


    public interface Callback {
        void onClick(final int position, final String item);

        void onLongClick(final int position, final String item);
    }

}
