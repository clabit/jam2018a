package com.novato.jam.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.glide.CropCircleTransformation;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.http.GoogleDriveImgListAsyncTask;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.adapter.ProfileListReAdapter;

import java.util.ArrayList;

/**
 * Created by poshaly on 2018. 2. 22..
 */

public class BottomProfileListDialog {

    Context context;
    ArrayList<String> item = new ArrayList<String>();
    private com.google.api.services.drive.Drive mDrive = null;
    boolean chatType;
    BottomSheetDialog mBottomSheetDialog;
    View mRoot;
    Callback mCallback;
    Handler mHandler = new Handler();

    public BottomProfileListDialog(Context context, com.google.api.services.drive.Drive mDrive){
        this.context = context;
        this.mDrive = mDrive;
        setUi();
    }

    public BottomProfileListDialog(Context context, com.google.api.services.drive.Drive mDrive, boolean chatType){
        this.context = context;
        this.mDrive = mDrive;
        this.chatType = chatType;
        setUi();
    }

    public void setCallback(Callback mCallback){
        this.mCallback = mCallback;
    }

    public void show(){
        if(mBottomSheetDialog!=null)mBottomSheetDialog.show();

    }
    public void dismiss(){
        if(mBottomSheetDialog!=null)mBottomSheetDialog.dismiss();
    }

    private void setUi(){

        mRoot = View.inflate(context, R.layout.dialog_bottom_profilelist, null);

        mBottomSheetDialog = new BottomSheetDialog(context);
        mBottomSheetDialog.setContentView(mRoot);

        mRoot.findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(mCallback!=null){
                    mCallback.onClickAdd();
                }
            }
        });




        LinearLayoutManager layoutManager
                = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);

        final RecyclerView myList = (RecyclerView) mRoot.findViewById(R.id.list);
        myList.setLayoutManager(layoutManager);


        final View loading = mRoot.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);


        try {
            //db에서 가져와서 item 채워 넣자....
            if(chatType){
                item = DBManager.createInstnace(context).getDriveChatImg(MainActivity.mUserData.getUid());
            }
            else {
                item = DBManager.createInstnace(context).getDriveImg(MainActivity.mUserData.getUid());
            }

            ProfileListReAdapter mProfileListReAdapter = new ProfileListReAdapter(item, myList.getLayoutManager(), myList);
            mProfileListReAdapter.setCallback(new ProfileListReAdapter.Callback() {
                @Override
                public void onClick(int position, String item) {
                    dismiss();
                    if (mCallback != null) {
                        mCallback.onClickItem(item);
                    }
                }

                @Override
                public void onLongClick(int position, String item) {
                    dismiss();
                }
            });
            myList.setAdapter(mProfileListReAdapter);
        }catch (Exception e){}



//        new GoogleDriveImgListAsyncTask(context, mDrive, new GoogleDriveImgListAsyncTask.Callback() {
//            @Override
//            public void result(ArrayList<String> result) {
//                try {
//                    loading.setVisibility(View.GONE);
//                }catch (Exception e){}
//                try {
//                    if (result != null) {
//                        item = result;
//
//                        ProfileListReAdapter mProfileListReAdapter = new ProfileListReAdapter(item, myList.getLayoutManager(), myList);
//                        mProfileListReAdapter.setCallback(new ProfileListReAdapter.Callback() {
//                            @Override
//                            public void onClick(int position, String item) {
//                                dismiss();
//                                if (mCallback != null) {
//                                    mCallback.onClickItem(item);
//                                }
//                            }
//
//                            @Override
//                            public void onLongClick(int position, String item) {
//                                dismiss();
//                            }
//                        });
//                        myList.setAdapter(mProfileListReAdapter);
//
//
//                    }
//                    else{
//                        myList.setVisibility(View.GONE);
//                        LoggerManager.e("mun", "BottomProfileListDialog  null");
//                    }
//                }catch (Exception e){
//                    LoggerManager.e("mun", "BottomProfileListDialog : "+ e.toString() );
//                }
//            }
//        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    public interface Callback{
        public void onClickAdd();
        public void onClickItem(String id);
    }
}
