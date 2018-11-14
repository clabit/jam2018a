package com.novato.jam.ui.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.glide.CropCircleTransformation;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.PermissionOk;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.data.UserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.dialog.BottomProfileListDialog;
import com.novato.jam.dialog.BottomUserInfoDialog;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.http.GoogleDriveRemoveAudioAsyncTask;
import com.novato.jam.http.GoogleDriveTokenAsyncTask;
import com.novato.jam.http.GoogleDriveUploadAsyncTask;
import com.novato.jam.http.GoogleDriveUploadAudioAsyncTask;
import com.novato.jam.ui.MainActivity;
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

public class BaseRoomFragment extends android.support.v4.app.Fragment{

    protected FeedData mFeedData;

    public FeedData getFeedData() {
        return mFeedData;
    }

    public void setFeedData(FeedData mFeedData) {
        this.mFeedData = mFeedData;
    }
    public void setFeedDataChange(){

    }





    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }




    protected void removeAudioList(final String uid){
        Fire.getReference().child(Fire.KEY_ROOM_AUDIO).child(mFeedData.getKey()).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    ArrayList<String> drive = new ArrayList<>();
                    ArrayList<HashMap> list = Parser.getAudioListParse(dataSnapshot);
                    for(HashMap k :list){
                        final String key = k.get("key")+"";
                        final String id = k.get("id")+"";

                        Fire.getReference().child("chat").child(mFeedData.getKey()).child(key).removeValue();
                        drive.add(id);
                    }

                    if(!TextUtils.isEmpty(uid))Fire.getReference().child(Fire.KEY_ROOM_AUDIO).child(mFeedData.getKey()).child(uid).removeValue();

                    try {
                        if(drive != null && drive.size() > 0 && uid.equals(MainActivity.mUserData.getUid())) {

                            if(mDrive== null) {
                                String mAccountName = MyPreferences.getString(getActivity(), MyPreferences.KEY_DRIVE_NAME);
                                getGoogleAccountCredential().setSelectedAccountName(mAccountName);
                                mDrive = getDriveService(getGoogleAccountCredential());
                            }

                            GoogleDriveRemoveAudioAsyncTask mGoogleDriveRemoveAudioAsyncTask = new GoogleDriveRemoveAudioAsyncTask(GlobalApplication.getAppContext(), mDrive, drive, null);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                mGoogleDriveRemoveAudioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                mGoogleDriveRemoveAudioAsyncTask.execute();
                            }
                        }
                    }catch (Exception e){}

                }catch (Exception e){}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        removeImageList(uid);
        removeStory(uid);
    }

    protected void removeImageList(final String uid){
        Fire.getReference().child(Fire.KEY_ROOM_IMAGE).child(mFeedData.getKey()).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    ArrayList<String> drive = new ArrayList<>();
                    ArrayList<HashMap> list = Parser.getAudioListParse(dataSnapshot);
                    for(HashMap k :list){
                        final String key = k.get("key")+"";
                        final String id = k.get("id")+"";

                        Fire.getReference().child("chat").child(mFeedData.getKey()).child(key).removeValue();
                        drive.add(id);
                    }

                    if(!TextUtils.isEmpty(uid))Fire.getReference().child(Fire.KEY_ROOM_IMAGE).child(mFeedData.getKey()).child(uid).removeValue();

                    try {
                        if(drive != null && drive.size() > 0 && uid.equals(MainActivity.mUserData.getUid())) {

                            if(mDrive== null) {
                                String mAccountName = MyPreferences.getString(getActivity(), MyPreferences.KEY_DRIVE_NAME);
                                getGoogleAccountCredential().setSelectedAccountName(mAccountName);
                                mDrive = getDriveService(getGoogleAccountCredential());
                            }

//                            for(String s : drive) {
//                                DBManager.createInstnace(getActivity()).removeDriveChatImgs(s);
//                            }
//
//                            GoogleDriveRemoveAudioAsyncTask mGoogleDriveRemoveAudioAsyncTask = new GoogleDriveRemoveAudioAsyncTask(GlobalApplication.getAppContext(), mDrive, drive, null);
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                                mGoogleDriveRemoveAudioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                            } else {
//                                mGoogleDriveRemoveAudioAsyncTask.execute();
//                            }
                        }
                    }catch (Exception e){}

                }catch (Exception e){}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeStory(final String uid){
        try {
            Fire.getReference().child(Fire.KEY_STORY).child(mFeedData.getKey()).orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {

                        ArrayList<FeedData> liust = Parser.getFeedDataListParse(dataSnapshot);

                        for(FeedData f:liust){
                            Fire.getReference().child(Fire.KEY_STORY).child(mFeedData.getKey()).child(f.getKey()).removeValue();
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (Exception e) {
        }
    }




    final int RESULT_GALL_RETURN = 2212;
    final int RESULT_CROP = 2213;
    final public int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2214;



    public String mMyImagePath;
    public String mpImg;

    public ProgressDialog mProgressDialog;

    /*****
     * drive
     */

    public final int REQUEST_ACCOUNT_PICKER = 331;
    public final int REQUEST_AUTHORIZATION = 332;
    public static final int RECOVERABLE_REQUEST_CODE = 1001;

    public GoogleAccountCredential mGoogleAccountCredential = null;
    public com.google.api.services.drive.Drive mDrive = null;
    public String mStrGoogleAccountName;
    public String mStrGoogleDriveAccessToken;

    protected int check_type = 1;

//    public void setGaller(){
//        check_type = 2;
//        checkGoogleAccountStep2();
//    }
//
//    public void checkGoogleAccount () {
//        check_type = 1;
//        checkGoogleAccountStep2();
//    }
    public void checkGoogleAccountStep2(){
        mStrGoogleAccountName = MyPreferences.getString(getActivity(), MyPreferences.KEY_DRIVE_NAME);
        mStrGoogleDriveAccessToken = MyPreferences.getString(getActivity(), MyPreferences.KEY_DRIVE_TOKEN);



        if(!TextUtils.isEmpty(mStrGoogleAccountName)) {
            loadGoogleDrive();
        }
        else {

            try {
                startActivityForResult(getGoogleAccountCredential().newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            } catch (Exception e) {
            }
        }
    }
    public GoogleAccountCredential getGoogleAccountCredential() {
        if (mGoogleAccountCredential == null) {
            mGoogleAccountCredential = GoogleAccountCredential.usingOAuth2(GlobalApplication.getAppContext(), Arrays.asList(DriveScopes.DRIVE)).setBackOff(new ExponentialBackOff());
        }
        return mGoogleAccountCredential;
    }


    //    class CustomGsonFactory extends GsonFactory {//compile 'com.google.http-client:google-http-client-gson:1.16.0-rc'
//        @Override
//        public JsonParser createJsonParser(String value) {
//            return super.createJsonParser(value);
//        }
//    }
//    class CustomJsonFactory extends com.google.api.client.json.JsonFactory{
//    }
    private com.google.api.services.drive.Drive getDriveService(GoogleAccountCredential credential) {
//        return new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new CustomJsonFactory(), credential).build();
        return new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), credential).build();
    }

    public void loadGoogleDrive () {
        if(TextUtils.isEmpty(mStrGoogleAccountName)){
            return;
        }

        getGoogleAccountCredential().setSelectedAccountName(mStrGoogleAccountName);
        mDrive = getDriveService(getGoogleAccountCredential());


        GoogleDriveTokenAsyncTask mGoogleDriveTokenAsyncTask = new GoogleDriveTokenAsyncTask(getActivity(), getFragment(), getGoogleAccountCredential() , new GoogleDriveTokenAsyncTask.Callback() {

            @Override
            public void result(GoogleDriveTokenAsyncTask.Data resultEntry) {
                if(mProgressDialog!=null)mProgressDialog.dismiss();
                if (null != resultEntry) {
                    if(TextUtils.isEmpty(resultEntry.token)){
                        MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_TOKEN, "");
                        MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_NAME, "");

                        checkGoogleAccountStep2();
                    }
                    else {
                        mStrGoogleDriveAccessToken = resultEntry.token;

                        if (!TextUtils.isEmpty(mStrGoogleDriveAccessToken)) {
                            MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_TOKEN, mStrGoogleDriveAccessToken);

                            if (!TextUtils.isEmpty(mStrGoogleAccountName)) {
                                MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_NAME, mStrGoogleAccountName);
                            }
                        }

                        LoggerManager.e("mun", "dddddddddddd : " + mStrGoogleDriveAccessToken + " //// " + resultEntry.name);

                        if(check_type == 2){
                            setListDialog();
                        }
                        else if(check_type == 3){
                            setDriveAudioUpload();
                        }
                        else{
                            setDriveUpload();
                        }
                    }
                }
                else
                {
                    LoggerManager.e("mun", "dddddddddddd : " + "sibal ");
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mGoogleDriveTokenAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mGoogleDriveTokenAsyncTask.execute();
        }

        if(mProgressDialog!=null)mProgressDialog.show();
    }

    public void setListDialog(){
        BottomProfileListDialog mBottomProfileListDialog = new BottomProfileListDialog(getActivity(), mDrive);
        mBottomProfileListDialog.setCallback(new BottomProfileListDialog.Callback() {
            @Override
            public void onClickAdd() {
                Intent intent = new Intent(Intent.ACTION_PICK).setType("image/*");
                startActivityForResult(intent, RESULT_GALL_RETURN);
            }

            @Override
            public void onClickItem(String id) {
                mpImg = id;

                final String url = "https://docs.google.com/uc?export=download&id=" + id;
                GlobalApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        setImage(url, mpImg);
                    }
                });
            }
        });
        mBottomProfileListDialog.show();
    }

    public void setDriveUpload(){

        try {
            LoggerManager.e("mun", "baseroom setDriveUpload : " + getClass().getName());
        }catch (Exception e){}

        if(mProgressDialog!=null)mProgressDialog.show();

        GoogleDriveUploadAsyncTask mGoogleDriveUploadAsyncTask = new GoogleDriveUploadAsyncTask(getActivity(), mMyImagePath, mDrive, new GoogleDriveUploadAsyncTask.Callback() {
            @Override
            public void result(String result, final boolean reLogin) {
                if(mProgressDialog!=null)mProgressDialog.dismiss();
                if(!TextUtils.isEmpty(result)) {
                    mpImg = result;

                    DBManager.createInstnace(getActivity()).addDriveImg(MainActivity.mUserData.getUid(), result);

                    final String url = "https://docs.google.com/uc?export=download&id=" + result;
                    GlobalApplication.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            setImage(url, mpImg);
                        }
                    });
                }
                else{
                    GlobalApplication.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {

                            MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_TOKEN, "");
                            MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_NAME, "");

                            if(reLogin){
                                mGoogleAccountCredential = null;
                            }
                            checkGoogleAccountStep2();
                        }
                    });
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mGoogleDriveUploadAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mGoogleDriveUploadAsyncTask.execute();
        }
    }

    public Fragment getFragment(){
        return this;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(MY_PERMISSIONS_REQUEST_READ_CONTACTS == requestCode){

            LoggerManager.e("mun", "sdsdsdsdsdsdsd");

            if(!PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || !PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.GET_ACCOUNTS)
                    ) {
//            if(grantResults != null && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
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
            else{
//                setGaller();
            }
        }
    }


    public void setDriveAudioUpload(){
    }




    protected void setImage(String url, String driveId){

    }
}
