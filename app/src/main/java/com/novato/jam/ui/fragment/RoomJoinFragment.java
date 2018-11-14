package com.novato.jam.ui.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.novato.jam.BuildConfig;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.PermissionOk;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.data.UserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.dialog.BottomProfileListDialog;
import com.novato.jam.dialog.CustomToast;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.http.GoogleDriveTokenAsyncTask;
import com.novato.jam.http.GoogleDriveUploadAsyncTask;
import com.novato.jam.push.SendPushFCMReadyOk;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.MakePictureCropActivity;
import com.novato.jam.ui.RoomInfoActivity;
import com.novato.jam.ui.adapter.RoomUserListReAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class RoomJoinFragment extends BaseRoomFragment implements View.OnClickListener{



    private View mRootView;

    HashMap<String, Object> userData;


    private EditText et_name, et_desc;

    private TextView tv_title_count, tv_text_count;
    private ImageView iv_img;

    private boolean isCreate = false;

    private Handler mHandler = new Handler();

    private ArrayList<String> tags;



    private boolean isLoad = false;

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void setUserData(HashMap<String, Object> userData) {
        this.userData = userData;
    }

    public void setCreate(boolean is){
        isCreate = is;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_roomjoin, container, false);

        mProgressDialog = new ProgressDialog(getActivity());


        mRootView.findViewById(R.id.layout_back).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_ok).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_ok).setVisibility(View.VISIBLE);

        if(isCreate){
            ((TextView)mRootView.findViewById(R.id.btn_ok)).setText(R.string.btn_join_create);
        }

        ((TextView)mRootView.findViewById(R.id.tv_action_title)).setText(mFeedData.getTitle());

        et_name = mRootView.findViewById(R.id.et_name);
        et_desc = mRootView.findViewById(R.id.et_desc);

        tv_title_count = mRootView.findViewById(R.id.tv_title_count);
        tv_text_count = mRootView.findViewById(R.id.tv_text_count);

        et_name.addTextChangedListener(new TextWatcher() { //이름 바꿀는부분
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

        return mRootView;
    }


    private void createRoom(String imgUrl){
        if(mProgressDialog!=null)mProgressDialog.dismiss();

//        if(!TextUtils.isEmpty(imgUrl)){
//            mFeedData.setpImg(imgUrl);
//        }
//        else{
//            mFeedData.setpImg(MainActivity.mUserData.getpImg());
//        }

        mFeedData.setColor(Utils.getRandomeColor());

        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).setValue(mFeedData.getHashMap());

        if (getActivity() instanceof RoomInfoActivity) {
            ((RoomInfoActivity) getActivity()).setFireListener(mFeedData);
        }


        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child(mFeedData.getCate() + "").child(mFeedData.getKey()).setValue(mFeedData.getHashMap());


        HashMap<String, Object> d = userData;
//                                HashMap<String, Object> d = MainActivity.mUserData.getHashMap();
        d.put("open", 1);
//        Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).child("listRoomUserData").child(MainActivity.mUserData.getUid()).setValue(d);
        Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).setValue(d);
        try {
            Fire.getReference().child(Fire.KEY_MY_ROOM).child(MainActivity.mUserData.getUid()).child(mFeedData.getKey()).setValue(mFeedData.getHashMap());
        } catch (Exception e) {
        }



        try {
            HashMap hh = new HashMap();
            hh.put("time", mFeedData.getTime());
            if (tags != null && tags.size() > 0) {
                for(String t :tags) {
                    if(!TextUtils.isEmpty(t))Fire.getReference().child(Fire.KEY_TAGS).child(t).child(mFeedData.getKey()).updateChildren(hh);
                }
            }
        }catch (Exception e){}


        if (getActivity() instanceof RoomInfoActivity) {
            ((RoomInfoActivity) getActivity()).setChatOpen(true);
        }
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
                if(!MainActivity.isAdmin) {
                    try {
                        String xx = Utils.isBlockWord(name, getActivity().getResources().getStringArray(R.array.block_name));
                        if (!TextUtils.isEmpty(xx)) {
                            CustomToast.showToast(getActivity(), String.format(getActivity().getString(R.string.block_text_err), xx), Toast.LENGTH_SHORT);
                            return;
                        }
                    } catch (Exception e) {
                    }
                    try {
                        String xx = Utils.isBlockWord(desc, getActivity().getResources().getStringArray(R.array.block_text));
                        if (!TextUtils.isEmpty(xx)) {
                            CustomToast.showToast(getActivity(), String.format(getActivity().getString(R.string.block_text_err), xx), Toast.LENGTH_SHORT);
                            return;
                        }
                    } catch (Exception e) {
                    }
                }

                userData.put("userName", name);
                userData.put("desc", desc);
                userData.put("color", Utils.getRandomeColor());
                userData.put("time", Fire.getServerTimestamp());

                try {
                    getActivity().setResult(getActivity().RESULT_OK);
                }catch (Exception e){}


                if(userData!=null && !TextUtils.isEmpty(mpImg))userData.put("pImg",mpImg);

                if(isCreate) {
                    if(mProgressDialog!=null){
                        mProgressDialog.setMessage(getString(R.string.create_loading));
                        mProgressDialog.show();
                    }
                    Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").push().setValue(mFeedData.getHashMap(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null && !TextUtils.isEmpty(databaseReference.getKey())) {

                                mFeedData.setKey(databaseReference.getKey());


                                LoggerManager.e("mun", "create room : " + mFeedData.getKey());


                                createRoom(mFeedData.getpImg());

//                                try {
//                                    //이미지파일 로컬로 선택했을때..
//                                    File f = new File(mFeedData.getpImg());
//                                    if (f != null && f.exists()) {
//
//
//
//                                        StorageReference storageRef = Fire.getStorage().getReference();
//
//                                        Uri file = Uri.fromFile(f);
//                                        StorageReference riversRef = storageRef.child(mFeedData.getKey() + "/" + mFeedData.getKey() +".jpg");
//                                        UploadTask uploadTask = riversRef.putFile(file);
//                                        uploadTask.addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception exception) {
//                                                createRoom(null);
//                                            }
//                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                            @Override
//                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                                String url = null;
//                                                try {
//                                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                                                    url = downloadUrl.toString();
//                                                    LoggerManager.e("munx", "room custom img => " + url);
//                                                }catch (Exception e){
//                                                }
//                                                finally {
//                                                    createRoom(url);
//                                                }
//                                            }
//                                        });
//
//                                    }
//                                    else{
//                                        createRoom(mFeedData.getpImg());
//                                    }
//                                }catch (Exception e){
//                                    createRoom(null);
//                                }

                            } else {
                                if(mProgressDialog!=null)mProgressDialog.dismiss();
                                getActivity().finish();
                            }

                        }
                    });
                }
                else{

                    if(mFeedData.getUid().equals(MainActivity.mUserData.getUid())){
                        userData.put("open", 1);
                    }
//                    Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("0").child(mFeedData.getKey()).child("listRoomUserData")
                    Fire.getReference().child(Fire.KEY_ROOM_USERLIST).child(mFeedData.getKey()).child(MainActivity.mUserData.getUid()).updateChildren(userData, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            try {
                                final FeedData myFeed = new FeedData();
                                myFeed.setUserName(mFeedData.getUserName());
                                myFeed.setUid(mFeedData.getUid());
                                myFeed.setTitle(mFeedData.getTitle());
                                myFeed.setText(mFeedData.getText());
                                myFeed.setColor(mFeedData.getColor());
                                myFeed.setChatCount(mFeedData.getChatCount());
                                myFeed.setuCount(mFeedData.getuCount());
                                myFeed.setCate(mFeedData.getCate());
                                myFeed.setOpen(mFeedData.getOpen());
                                if(!TextUtils.isEmpty(mFeedData.getpImg()))myFeed.setpImg(mFeedData.getpImg());

                                myFeed.setTime(Fire.getServerTimestamp());

                                Fire.getReference().child(Fire.KEY_MY_ROOM).child(MainActivity.mUserData.getUid()).child(mFeedData.getKey()).updateChildren(myFeed.getHashMap(), new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        try {
                                            if ("0".equals(userData.get("open") + "")) {
                                                try {
                                                    for (final RoomUserData p : mFeedData.getListRoomUserData()) {
                                                        LoggerManager.e("mun", "send push " + p.getUid() + " //// " + mFeedData.getUid());

                                                        if (p.getUid().equals(mFeedData.getUid())) {
                                                            //push
                                                            final String uid = p.getUid();
                                                            Fire.getReference().child(Fire.KEY_USER).child(uid).child("push").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    try {
                                                                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                                                            final String push2 = (String) dataSnapshot.getValue(true);
                                                                            LoggerManager.e("mun", "send push " + push2);
                                                                            GlobalApplication.runBackground(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    try {
                                                                                        if (!TextUtils.isEmpty(push2)) {
                                                                                            DBManager.createInstnace(getActivity()).addUserPush(uid, push2);
                                                                                            ArrayList<String> list = new ArrayList<String>();
                                                                                            list.add(push2);
                                                                                            String re = new SendPushFCMReadyOk(getActivity(), list, mFeedData.getKey(), SendPushFCMReadyOk.TYPE_SIGNUP).start();
                                                                                        }
                                                                                    } catch (Exception e) {
                                                                                        LoggerManager.e("mun", "send push " + e.toString());
                                                                                    }
                                                                                }
                                                                            });

                                                                        }
                                                                    } catch (Exception e) {
                                                                        LoggerManager.e("mun", "send push " + e.toString());
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {
                                                                    LoggerManager.e("mun", "send push onCancelled ");
                                                                }
                                                            });
                                                            break;
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    LoggerManager.e("mun", "send push " + e.toString());
                                                }

                                                CustomToast.showToast(getActivity(), R.string.join_ready, Toast.LENGTH_SHORT);
                                                getActivity().finish();
                                                return;
                                            }

                                            if (getActivity() instanceof RoomInfoActivity) {
                                                ((RoomInfoActivity) getActivity()).setChatOpenUserJoinAfter();
                                            }
                                        }catch (Exception e){}
                                    }
                                });
                            }catch (Exception e){}

                        }
                    });

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
