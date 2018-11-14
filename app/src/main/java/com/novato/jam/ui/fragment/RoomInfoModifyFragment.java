package com.novato.jam.ui.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.glide.CropCircleTransformation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.lib.hashtag.views.HashTagEditText;
import com.novato.jam.BuildConfig;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.PermissionOk;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.dialog.CustomToast;
import com.novato.jam.firebase.Fire;
import com.novato.jam.http.KakaoImageFilter;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.MakePictureCropActivity;
import com.novato.jam.ui.RoomInfoActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomInfoModifyFragment extends BaseRoomFragment implements View.OnClickListener{


    View mRootView;

    EditText et_title;
    HashTagEditText et_text;
    TextView tv_title_count, tv_text_count, tv_join_msg01, tv_join_msg02, tv_uc_msg01, tv_uc_msg02;

    Spinner spinner;

    ImageView btn_lock, iv_img, btn_uc;

    Handler mHandler = new Handler();

    private boolean isPrivate = false;
    private boolean isUcPrivate = false;

    private int catePosition = 1;

    private List<String> originTags;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_roomcreate, container, false);

        mProgressDialog = new ProgressDialog(getActivity());


        mRootView.findViewById(R.id.lay_bottom).setVisibility(View.GONE);
        ((TextView)mRootView.findViewById(R.id.tv_feed_write)).setText(R.string.feed_modify);

        et_title = mRootView.findViewById(R.id.et_title);
        et_text = mRootView.findViewById(R.id.et_text);
        tv_title_count = mRootView.findViewById(R.id.tv_title_count);
        tv_text_count = mRootView.findViewById(R.id.tv_text_count);


        iv_img = mRootView.findViewById(R.id.iv_img);
//        if(BuildConfig.DEBUG)
        {
            try {
                //이미지 파일 선택할수 있게 했을때
//                Glide.with(getActivity())
//                        .load(mMyImagePath)
////                        .error(R.drawable.btn_edit)
//                        .bitmapTransform(new CropCircleTransformation(getActivity()))
//                        .skipMemoryCache(true)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .into(iv_img);
                mRootView.findViewById(R.id.lay_img).setVisibility(View.VISIBLE);
            } catch (Exception e) {
            }
            iv_img.setOnClickListener(this);
        }


        tv_join_msg01 = mRootView.findViewById(R.id.tv_join_msg01);
        tv_join_msg02 = mRootView.findViewById(R.id.tv_join_msg02);

        et_title.addTextChangedListener(new TextWatcher() {
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
        et_text.addTextChangedListener(new TextWatcher() {
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

        btn_lock = mRootView.findViewById(R.id.btn_lock);
        btn_lock.setOnClickListener(this);

        btn_lock.setImageResource(R.drawable.btn_unlock);
        tv_join_msg01.setText(R.string.create_open);
        tv_join_msg02.setText(R.string.create_open_desc);
        isPrivate = false;


        btn_uc = mRootView.findViewById(R.id.btn_uc);
        btn_uc.setOnClickListener(this);
        tv_uc_msg01 = mRootView.findViewById(R.id.tv_uc_msg01);
        tv_uc_msg02 = mRootView.findViewById(R.id.tv_uc_msg02);


        spinner = (Spinner) mRootView.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                catePosition = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        mRootView.findViewById(R.id.tv_feed_write).setOnClickListener(this);


        setDataText();

        return mRootView;
    }



    private void setDataText(){

        if(mFeedData == null)
            return;

        if(!TextUtils.isEmpty(mFeedData.getTitle()))et_title.setText(mFeedData.getTitle());
        if(!TextUtils.isEmpty(mFeedData.getText()))et_text.setText(mFeedData.getText());


        originTags = et_text.getHashTags();



        if(!TextUtils.isEmpty(mFeedData.getpImg())) {
            final String url = "https://docs.google.com/uc?export=download&id=" + mFeedData.getpImg();
            Glide.with(getActivity())
                    .load(url)
                    .bitmapTransform(new CropCircleTransformation(getActivity()))
//                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iv_img);
        }



        if(mFeedData.getUc() == 0) {
            isUcPrivate = true;
            btn_uc.setImageResource(R.drawable.btn_lock);
            tv_uc_msg01.setText(R.string.create_uc02);
            tv_uc_msg02.setText(R.string.create_uc_desc02);
        }
        else{
            isUcPrivate = false;
            btn_uc.setImageResource(R.drawable.btn_unlock);
            tv_uc_msg01.setText(R.string.create_uc01);
            tv_uc_msg02.setText(R.string.create_uc_desc01);
        }



    }


    boolean isWrite = false;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_feed_write:{
                if(isWrite)
                    return;

                isWrite = true;
                String s = et_title.getText().toString().trim();
                String text = et_text.getText().toString().trim();

                if(TextUtils.isEmpty(s) || s.length() < 1){
                    CustomToast.showToast(getActivity(), R.string.title_size_err, Toast.LENGTH_SHORT);
                    isWrite = false;
                }
                else if(TextUtils.isEmpty(text) || text.length() < 1){
                    CustomToast.showToast(getActivity(), R.string.text_size_err, Toast.LENGTH_SHORT);
                    isWrite = false;
                }
                else{
                    try {
                        if(!BuildConfig.DEBUG) {
                            long fff = MyPreferences.getLong(getActivity(), MyPreferences.KEY_ROOMINFI_MODIFY);
                            if (fff > Fire.getServerTimestamp() - (1000 * 60 * 5)) {
                                String t = String.format(getActivity().getString(R.string.jam_promotion_err), ((fff - (Fire.getServerTimestamp() - (1000 * 60 * 5))) / 1000) + "");
                                CustomToast.showToast(getActivity(), t, Toast.LENGTH_SHORT);

                                isWrite = false;
                                return;
                            }
                        }
                    }catch (Exception e){}

                    try{
                        if(et_text.getHashTags().size() > 3){
                            CustomToast.showToast(getActivity(), R.string.create_tag_size, Toast.LENGTH_SHORT);
                            isWrite = false;
                            return;
                        }
                    }catch (Exception e){}
                    try{
                        for(String tags : et_text.getHashTags()){
                            if(tags.length() > 10) {
                                CustomToast.showToast(getActivity(), R.string.create_tag_leng, Toast.LENGTH_SHORT);
                                isWrite = false;
                                return;
                            }
                        }
                    }catch (Exception e){}

                    try {
                        String xx = Utils.isBlockWord(s, getActivity().getResources().getStringArray(R.array.block_text));
                        if(!TextUtils.isEmpty( xx )){
                            CustomToast.showToast(getActivity(), String.format(getActivity().getString(R.string.block_text_err), xx), Toast.LENGTH_SHORT);
                            isWrite = false;
                            return;
                        }
                    }catch (Exception e){}
                    try {
                        String xx = Utils.isBlockWord(text, getActivity().getResources().getStringArray(R.array.block_text));
                        if(!TextUtils.isEmpty( xx )){
                            CustomToast.showToast(getActivity(), String.format(getActivity().getString(R.string.block_text_err), xx), Toast.LENGTH_SHORT);
                            isWrite = false;
                            return;
                        }
                    }catch (Exception e){}


//                    final FeedData mFeedData = new FeedData();
////                    mFeedData.setUserName(MainActivity.mUserData.getUserName());
//                    if(!TextUtils.isEmpty(mpImg))mFeedData.setpImg(mpImg);
////                    mFeedData.setUid(MainActivity.mUserData.getUid());
//
////                    mFeedData.setOpen(isPrivate?0:1);
//                    mFeedData.setTitle(s);
//                    mFeedData.setText(text);
//                    mFeedData.setTime(Fire.getServerTimestamp());

//                    mFeedData.setCate(catePosition);

                    {

                        final HashMap pp = new HashMap();
                        pp.put("title", s);
                        pp.put("text", text);
                        pp.put("uc", isUcPrivate?0:1);
                        if(!TextUtils.isEmpty(mpImg))pp.put("pImg", mpImg);

                        final ArrayList<String> tags = new ArrayList<>();
                        try {
                            for (String tag : et_text.getHashTags()) {
                                if (!TextUtils.isEmpty(tag)) {
                                    tags.add(tag);
                                }
                            }
                        }catch (Exception e){}



                        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("-99").child(mFeedData.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot != null && dataSnapshot.getValue() !=null){
                                    try{
                                        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("-99").child(mFeedData.getKey()).updateChildren(pp);
                                    }catch (Exception e){}
                                }
                                try {
                                    Fire.getReference().child(Fire.KEY_CHAT_ROOM).child(mFeedData.getCate() + "").child(mFeedData.getKey()).updateChildren(pp);
                                }catch (Exception e){}
                                try {
                                    Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).updateChildren(pp);
                                }catch (Exception e){}
                                try {
                                    for (RoomUserData d : mFeedData.getListRoomUserData()) {
                                        try {
                                            if(d.getOpen() == 1 || d.getOpen() == 0) {
                                                if (!TextUtils.isEmpty(d.getUid()))
                                                    Fire.getReference().child(Fire.KEY_MY_ROOM).child(d.getUid()).child(mFeedData.getKey()).updateChildren(pp);
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                }catch (Exception e){}


                                try {
                                    long time = Fire.getServerTimestamp();
                                    HashMap hh = new HashMap();
                                    hh.put("time", time);
                                    if (tags != null && tags.size() > 0) {
                                        for(String t :tags) {
                                            if(!TextUtils.isEmpty(t))Fire.getReference().child(Fire.KEY_TAGS).child(t).child(mFeedData.getKey()).updateChildren(hh);
                                        }
                                    }
                                }catch (Exception e){}

                                isWrite = false;

                                try {
                                    MyPreferences.set(getActivity(), MyPreferences.KEY_ROOMINFI_MODIFY, Fire.getServerTimestamp());
                                }catch (Exception e){}

                                getActivity().finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                isWrite = false;
                                getActivity().finish();
                            }
                        });



                        try{
                            if(originTags!=null){
                                for(String t :originTags){
                                    if(!TextUtils.isEmpty(t))Fire.getReference().child(Fire.KEY_TAGS).child(t).child(mFeedData.getKey()).removeValue();
                                }
                            }
                        }catch (Exception e){}


                    }

                }


                break;
            }
            case R.id.btn_lock:{
                if(isPrivate) {
                    btn_lock.setImageResource(R.drawable.btn_unlock);
                    tv_join_msg01.setText(R.string.create_open);
                    tv_join_msg02.setText(R.string.create_open_desc);
                    isPrivate = false;
                }
                else{
                    btn_lock.setImageResource(R.drawable.btn_lock);
                    tv_join_msg01.setText(R.string.create_lock);
                    tv_join_msg02.setText(R.string.create_lock_desc);
                    isPrivate = true;
                }

                break;
            }
            case R.id.btn_uc:{
                if(isUcPrivate) {
                    btn_uc.setImageResource(R.drawable.btn_unlock);
                    tv_uc_msg01.setText(R.string.create_uc01);
                    tv_uc_msg02.setText(R.string.create_uc_desc01);
                    isUcPrivate = false;
                }
                else{
                    btn_uc.setImageResource(R.drawable.btn_lock);
                    tv_uc_msg01.setText(R.string.create_uc02);
                    tv_uc_msg02.setText(R.string.create_uc_desc02);
                    isUcPrivate = true;
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
    public void setDriveUpload(){
        super.setDriveUpload();
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


