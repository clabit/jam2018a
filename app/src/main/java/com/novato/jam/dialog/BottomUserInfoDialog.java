package com.novato.jam.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.glide.CropCircleTransformation;
import com.novato.jam.R;
import com.novato.jam.common.Utils;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.ui.ShowImageActivity;

/**
 * Created by poshaly on 2018. 2. 22..
 */

public class BottomUserInfoDialog {
    static public int TYPE_ADMIN_BAN = 1;
    static public int TYPE_ADMIN_JOIN = 2;
    static public int TYPE_USER = -1;

    Context context;
    RoomUserData item;
    BottomSheetDialog mBottomSheetDialog;
    View mRoot;
    int type = -1;

    public BottomUserInfoDialog(Context context, RoomUserData item){
        this.context = context;
        this.item = item;
        setUi();
    }

    public BottomUserInfoDialog(Context context, RoomUserData item, int type){
        this.context = context;
        this.item = item;
        this.type = type;

        setUi();
    }

    public void show(){
        if(mBottomSheetDialog!=null)mBottomSheetDialog.show();
    }
    public void dismiss(){
        if(mBottomSheetDialog!=null)mBottomSheetDialog.dismiss();
    }

    public void setOnClickBan(View.OnClickListener listener){
        mRoot.findViewById(R.id.btn_ban).setOnClickListener(listener);
    }
    public void setOnClickMaster(View.OnClickListener listener){
        mRoot.findViewById(R.id.btn_mastar).setOnClickListener(listener);
    }

    public void setOnClickJoinOk(View.OnClickListener listener){
        mRoot.findViewById(R.id.btn_joinok).setOnClickListener(listener);
    }

    public void setOnClickRemoveUser(View.OnClickListener listener){
        mRoot.findViewById(R.id.btn_remove).setOnClickListener(listener);
    }

    private void setUi(){

        mRoot = View.inflate(context, R.layout.dialog_bottom_userinfo, null);

        mBottomSheetDialog = new BottomSheetDialog(context);
        mBottomSheetDialog.setContentView(mRoot);

        try {
            mBottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        }catch (Exception e){}


        if(type == TYPE_ADMIN_BAN){
            mRoot.findViewById(R.id.btn_ban).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.btn_mastar).setVisibility(View.VISIBLE);
        }
        else if(type == TYPE_ADMIN_JOIN){
            mRoot.findViewById(R.id.btn_joinok).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.btn_remove).setVisibility(View.VISIBLE);
        }
        else{

        }



//        Glide.with(context)
//                .load(item.getpImg()+"")
//                .bitmapTransform(new CropCircleTransformation(context))
////                .placeholder(R.drawable.icon_progress)
////                        .placeholder(null)
//                .skipMemoryCache(true)
////                .error(R.drawable.none_img)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into((ImageView) mRoot.findViewById(R.id.iv_img));


        ColorDrawable mColorDrawable;
        if(!TextUtils.isEmpty(item.getColor())){
            mColorDrawable = new ColorDrawable(Color.parseColor("#"+item.getColor()));
        }
        else {
            mColorDrawable = Utils.getRandomDrawbleColor();
        }
        ((ImageView) mRoot.findViewById(R.id.iv_img)).setImageDrawable(mColorDrawable);


        ((TextView)mRoot.findViewById(R.id.tv_name)).setText(item.getUserName()+"");
        ((TextView)mRoot.findViewById(R.id.tv_desc)).setText(item.getDesc()+"");
//        try {
//            ((TextView) mRoot.findViewById(R.id.tv_in)).setText(item.getUserName()+"");
//        }catch (Exception e){}



        if(!TextUtils.isEmpty(item.getpImg())) {
            final String url = "https://docs.google.com/uc?export=download&id=" + item.getpImg();
            Glide.with(context)
                    .load(url + "")
//                .load(getRandomDrawbleColor())//(item.getpImg()+"")
//                .bitmapTransform(new CropCircleTransformation(mListView.getContext()))
//                .placeholder(R.drawable.icon_progress)
                    .placeholder(mColorDrawable)
//                .skipMemoryCache(true)
//                .error(getRandomDrawbleColor())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                            try {
                                ((TextView) mRoot.findViewById(R.id.tv_in)).setText(item.getUserName()+"");
                            }catch (Exception x){}

                            return false;
                        }
                        @Override
                        public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                            try {
                                mRoot.findViewById(R.id.lay_middle).setVisibility(View.GONE);
//                                ((ImageView) mRoot.findViewById(R.id.iv_img)).setImageDrawable(glideDrawable);
                            }catch (Exception e){}

                            try {
                                ((ImageView) mRoot.findViewById(R.id.iv_img_top)).setImageDrawable(glideDrawable);
                            }catch (Exception e){}
                            return false;
                        }
                    })
                    .into(((ImageView) mRoot.findViewById(R.id.iv_img)));

            try {
                ((TextView) mRoot.findViewById(R.id.tv_in)).setText("");
            }catch (Exception e){}

            mRoot.findViewById(R.id.lay_top).setVisibility(View.VISIBLE);


//            mRoot.findViewById(R.id.iv_img).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent ss = new Intent(context, ShowImageActivity.class);
//                    ss.putExtra("url", item.getpImg());
//                    context.startActivity(ss);
//                }
//            });

        }
        else{
            try {
                ((TextView) mRoot.findViewById(R.id.tv_in)).setText(item.getUserName()+"");
            }catch (Exception e){}
        }




    }

}
