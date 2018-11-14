package com.novato.jam.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.Util;
import com.novato.jam.R;
import com.novato.jam.common.ImageDownTask;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.PermissionOk;
import com.novato.jam.common.Utils;
import com.novato.jam.customview.ViewTouchImageView;
import com.novato.jam.firebase.Fire;

import java.io.File;
import java.util.Calendar;

public class ShowImageActivity extends BaseActivity {
    final public int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2214;

    ProgressDialog mProgressDialog;

//    Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);

        mProgressDialog = new ProgressDialog(this);

        final ViewTouchImageView iv = (ViewTouchImageView)findViewById(R.id.touch_image);
        iv.setScaleType(ImageView.ScaleType.MATRIX);

        if(getIntent()!=null) {
            final String url = getIntent().getStringExtra("url");

            if(!TextUtils.isEmpty(url)) {
                Glide.with(this)
                        .load("https://docs.google.com/uc?export=download&id=" + url)
//                .placeholder(R.drawable.icon_progress)
//                        .placeholder(null)
//                .error(R.drawable.none_img)
//                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(new SimpleTarget<Bitmap>() {
//                            @Override
//                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
//                                iv.setImageBitmap(bitmap);
//                                mBitmap = bitmap;
//                            }
//                        });
                        .into(iv);

                findViewById(R.id.btn_down).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_down).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                            save("https://docs.google.com/uc?export=download&id=" + url);
                        }
                        else{
                            PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_CONTACTS, new PermissionOk.Callback() {
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
                });
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(MY_PERMISSIONS_REQUEST_READ_CONTACTS == requestCode){

            LoggerManager.e("mun", "sdsdsdsdsdsdsd");

            if(!PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ) {
                android.app.AlertDialog.Builder alert_confirm = new android.app.AlertDialog.Builder(getActivity());
                alert_confirm
                        .setMessage(R.string.permission_all_err)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PermissionOk.checkPermissionAllActivity(getActivity(), MY_PERMISSIONS_REQUEST_READ_CONTACTS);
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
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        try{
//            mBitmap.recycle();
//        }catch (Exception e){}
    }

    private void save(final String  bitmap){

        File n = new File(Environment.getExternalStorageDirectory().toString()+"/download");
        if(!n.exists()){
            n.mkdirs();
        }
        String newpath = n.getAbsolutePath() + "/" + DateFormat.format("yyyy-MM-dd_kk:mm:ss", Fire.getServerTimestamp()).toString()+".jpg";

        new ImageDownTask(getActivity(), new ImageDownTask.ImageDownTaskListner() {
            @Override
            public void onSuccess(File f) {
                if(mProgressDialog!=null)mProgressDialog.dismiss();
                try {
                    if (f != null && f.exists() && f.length() > 0) {
                        Toast.makeText(ShowImageActivity.this, getString(R.string.profile_popup_chat_image_downok), Toast.LENGTH_SHORT).show();
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + f.getAbsolutePath())));
                    } else {
                        Toast.makeText(ShowImageActivity.this, getString(R.string.profile_popup_chat_image_downfail), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){}
            }

            @Override
            public void onFail() {
                if(mProgressDialog!=null)mProgressDialog.dismiss();
                try {
                    Toast.makeText(ShowImageActivity.this,getString(R.string.profile_popup_chat_image_downfail),Toast.LENGTH_SHORT).show();
                }catch (Exception e){}
            }
        }).execute(bitmap, newpath);

        mProgressDialog.show();
    }
}


