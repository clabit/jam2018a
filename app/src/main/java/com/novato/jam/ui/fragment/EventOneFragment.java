package com.novato.jam.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.novato.jam.R;
import com.novato.jam.data.RoomUserData;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class EventOneFragment extends android.support.v4.app.Fragment implements View.OnClickListener{

    private View mRootView;
    private RoomUserData mRoomUserData;

    public RoomUserData getmRoomUserData() {
        return mRoomUserData;
    }

    public void setmRoomUserData(RoomUserData mRoomUserData) {
        this.mRoomUserData = mRoomUserData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_one_event, container, false);


        try {
            mRootView.findViewById(R.id.root).setBackgroundColor(Color.parseColor("#" + mRoomUserData.getColor()));
        }catch (Exception e){}

        try {
            Glide.with(getActivity())
                    .load(mRoomUserData.getpImg() + "")
//                .bitmapTransform(new CropCircleTransformation(mContext))
//                .placeholder(R.drawable.icon_progress)
//                        .placeholder(null)
//                .skipMemoryCache(true)
//                .error(R.drawable.none_img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) mRootView.findViewById(R.id.iv_img));
        }catch (Exception e){}


        mRootView.findViewById(R.id.frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent u = new Intent(Intent.ACTION_VIEW, Uri.parse(mRoomUserData.getMail()));
                    startActivity(u);
                }catch (Exception e){}
            }
        });


        return mRootView;
    }

    @Override
    public void onClick(View v) {

    }

}
