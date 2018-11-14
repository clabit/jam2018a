package com.novato.jam.ui.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.glide.CropCircleTransformation;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.PermissionOk;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.dialog.BottomProfileListDialog;
import com.novato.jam.dialog.CustomToast;
import com.novato.jam.firebase.Fire;
import com.novato.jam.http.GoogleDriveImgListAsyncTask;
import com.novato.jam.http.GoogleDriveTokenAsyncTask;
import com.novato.jam.http.GoogleDriveUploadAsyncTask;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.MakePictureCropActivity;
import com.novato.jam.ui.RoomInfoActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class RoomModifyFragment extends BaseRoomFragment implements View.OnClickListener{

    private View mRootView;

    private EditText et_name, et_desc;

    private TextView tv_title_count, tv_text_count;

    private ImageView iv_img;

    private Handler mHandler = new Handler();

    private boolean isLoad = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_roomjoin, container, false);

        mProgressDialog = new ProgressDialog(getActivity());


        mRootView.findViewById(R.id.layout_back).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_ok).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_ok).setVisibility(View.VISIBLE);

        ((TextView)mRootView.findViewById(R.id.btn_ok)).setText(R.string.btn_join_create);

        ((TextView)mRootView.findViewById(R.id.tv_action_title)).setText(mFeedData.getTitle());

        et_name = mRootView.findViewById(R.id.et_name);
        et_desc = mRootView.findViewById(R.id.et_desc);

        tv_title_count = mRootView.findViewById(R.id.tv_title_count);
        tv_text_count = mRootView.findViewById(R.id.tv_text_count);

        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                int count = s.toString().length();
                tv_title_count.setText(count+"/13");
            }
        });
        et_desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                int count = s.toString().length();
                tv_text_count.setText(count+"/200");
            }
        });

        try {
            for (RoomUserData u : mFeedData.getListRoomUserData()) {
                if (u.getUid().equals(MainActivity.mUserData.getUid())) {
                    et_name.setText(u.getUserName()+"");
                    et_desc.setText(u.getDesc()+"");
                    mpImg = u.getpImg();
                    LoggerManager.e("mun", "pimg : "+ mpImg);
                    break;
                }
            }
        }catch (Exception e){}


        iv_img = mRootView.findViewById(R.id.iv_img);
//        if(BuildConfig.DEBUG)
        if(!TextUtils.isEmpty(mpImg)){
            try {
                String url = "https://docs.google.com/uc?export=download&id=" + mpImg;
                Glide.with(getActivity())
                        .load(url)
                        .bitmapTransform(new CropCircleTransformation(getActivity()))
//                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(iv_img);

            } catch (Exception e) {
            }
        }
        iv_img.setOnClickListener(this);
        mRootView.findViewById(R.id.lay_img).setVisibility(View.VISIBLE);


        return mRootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layout_back:{
                getActivity().finish();
                break;
            }
            case R.id.btn_ok:{

                if(mFeedData == null || MainActivity.mUserData == null || TextUtils.isEmpty(MainActivity.mUserData.getUid())) {
                    CustomToast.showToast(getActivity(), R.string.err_room_info,Toast.LENGTH_SHORT);
                    return;
                }

                String name = et_name.getText().toString().trim();
                String desc = et_desc.getText().toString().trim();


                if(TextUtils.isEmpty(name) || name.length() < 1){
                    CustomToast.showToast(getActivity(), R.string.name_size_err,Toast.LENGTH_SHORT);
                    return;
                }
//                if(TextUtils.isEmpty(desc) || desc.length() < 1){
//                    CustomToast.showToast(getActivity(), R.string.name_size_err,Toast.LENGTH_SHORT);
//                    return;
//                }

                try {
                    String xx = Utils.isBlockWord(name, getActivity().getResources().getStringArray(R.array.block_name));
                    if(!TextUtils.isEmpty( xx )){
                        CustomToast.showToast(getActivity(), String.format(getActivity().getString(R.string.block_text_err), xx), Toast.LENGTH_SHORT);
                        return;
                    }
                }catch (Exception e){}
                try {
                    String xx = Utils.isBlockWord(desc, getActivity().getResources().getStringArray(R.array.block_text));
                    if(!TextUtils.isEmpty( xx )){
                        CustomToast.showToast(getActivity(), String.format(getActivity().getString(R.string.block_text_err), xx), Toast.LENGTH_SHORT);
                        return;
                    }
                }catch (Exception e){}


                HashMap<String, Object> userData = new HashMap<>();

                userData.put("userName", name);
                userData.put("desc", desc);
                if(!TextUtils.isEmpty(mpImg))userData.put("pImg", mpImg);


                {
                    Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).updateChildren(userData);

                    if (getActivity() instanceof RoomInfoActivity) {
                        ((RoomInfoActivity) getActivity()).removeFragment();
                    }
                }






                break;
            }
            case R.id.iv_img:{

                if(PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.GET_ACCOUNTS)
                        ) {
                    setGaller();
                }
                else {
                    PermissionOk.checkPermission(getActivity(), MY_PERMISSIONS_REQUEST_READ_CONTACTS, new PermissionOk.Callback() {
                        @Override
                        public void OnFail(final Runnable run) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.notice)
                                    .setMessage(R.string.permission_file_err)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                            run.run();
                                        }
                                    })
                                    .create().show();
                        }

                        @Override
                        public void OnOk() {
                            setGaller();
                        }
                    });
                }

                break;
            }
        }
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == RESULT_GALL_RETURN) {
            if (resultCode == getActivity().RESULT_OK) {
                try {
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().managedQuery(data.getData(), projection, null, null, null);
                    int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String uploadFile = cursor.getString(column_index_data);

                    File f = GlobalApplication.getProfileFile(getActivity());

                    String filepath = f.getAbsolutePath() + "/" + Fire.getServerTimestamp() + ".jpg";
                    copy(new File(uploadFile), new File(filepath));

                    Intent intent = new Intent(getActivity(), MakePictureCropActivity.class);
                    intent.putExtra(MakePictureCropActivity.PATH, filepath);
                    startActivityForResult(intent, RESULT_CROP);


                }catch(Exception e){

                    try {
                        final String filepath = data.getData().getPath();

                        Intent intent = new Intent(getActivity(), MakePictureCropActivity.class);
                        intent.putExtra(MakePictureCropActivity.PATH, filepath);
                        startActivityForResult(intent, RESULT_CROP);
                    }catch (Exception e1){}
                }
            }
        }
        else if(requestCode == RESULT_CROP){
            if (resultCode == getActivity().RESULT_OK) {
                if(data != null){
                    final String path = data.getStringExtra(MakePictureCropActivity.PATH);
                    if(!TextUtils.isEmpty(path)){
                        LoggerManager.e("mun","path : " + path);
                        //파일 업로드드
                        mMyImagePath = path;

//                        signIn();


                        if(TextUtils.isEmpty(MyPreferences.getString(getActivity(), MyPreferences.KEY_DRIVE_NAME))) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.notice)
                                    .setMessage(R.string.permission_account)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();


                                            checkGoogleAccount();

                                        }
                                    })
                                    .create().show();
                        }
                        else{
                            checkGoogleAccount();
                        }



//                        LoggerManager.e("mun", mMyImagePath);
//                        Glide.with(getActivity())
//                                .load(mMyImagePath)
//                                .bitmapTransform(new CropCircleTransformation(getActivity()))
//                                .skipMemoryCache(true)
//                                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                                .into(iv_img);
                    }
                }
            }
        }
        else if(MY_PERMISSIONS_REQUEST_READ_CONTACTS == requestCode){

            if(PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.GET_ACCOUNTS)
                    ){
                setGaller();
            }
        }
        else if(requestCode == REQUEST_ACCOUNT_PICKER) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                mStrGoogleAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (!TextUtils.isEmpty(mStrGoogleAccountName)) {
                    loadGoogleDrive();
                }
            }
        }
        else if(requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == Activity.RESULT_OK) {
            } else {
                startActivityForResult(getGoogleAccountCredential().newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        }
        else if(requestCode == RECOVERABLE_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK) {
                if (!TextUtils.isEmpty(mStrGoogleAccountName)) {
//                    PreferenceUtil.INSTANCE.setGoogleAccount(mStrGoogleAccountName);
                    loadGoogleDrive();
                }
            }
        }

    }


    public void setGaller(){
        check_type = 2;
        checkGoogleAccountStep2();
    }

    public void checkGoogleAccount () {
        check_type = 1;
        checkGoogleAccountStep2();
    }

    @Override
    protected void setImage(String url, String driveId) {
        super.setImage(url, driveId);

        Glide.with(getActivity())
                .load(url)
                .bitmapTransform(new CropCircleTransformation(getActivity()))
//                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(iv_img);

    }
}
