package com.novato.jam.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.analytics.FirebaseAnalyticsLog;
import com.novato.jam.common.FragmentAppCompatManager;
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
import com.novato.jam.http.GoogleDriveTokenAsyncTask;
import com.novato.jam.http.GoogleDriveUploadAsyncTask;
import com.novato.jam.http.GoogleDriveUploadAudioAsyncTask;
import com.novato.jam.push.SendPushFCMStory;
import com.novato.jam.ui.fragment.MainFeedFragment;
import com.novato.jam.ui.fragment.RoomChatFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class FeedWriteActivity extends BaseActivity implements View.OnClickListener{


    final int REQUEST_RECORDING = 4001;


    EditText et_text;

    FeedData mFeedData, mModifyData;

    ImageView iv_img;

    TextView tv_feed_all, tv_feed_fam;

    private boolean isOpen = true;

    private boolean isModify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_write);



        try {
            mFeedData = getIntent().getParcelableExtra("data");
        }catch (Exception e){}


        try {
            mModifyData = getIntent().getParcelableExtra("modify");
        }catch (Exception e){}

        iv_img = (ImageView)findViewById(R.id.iv_img);


        if(mFeedData == null || TextUtils.isEmpty(mFeedData.getKey())){
            Toast.makeText(getActivity(), R.string.err_room_info, Toast.LENGTH_SHORT).show();
            finish();
        }

        mProgressDialog = new ProgressDialog(getActivity());


        et_text = findViewById(R.id.et_text);

        tv_feed_all = findViewById(R.id.tv_feed_all);
        tv_feed_fam = findViewById(R.id.tv_feed_fam);
        findViewById(R.id.tv_feed_all).setOnClickListener(this);
        findViewById(R.id.tv_feed_fam).setOnClickListener(this);

        findViewById(R.id.tv_feed_write).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);

        setOpen(true);

        setModify();
    }

    private void setModify(){
        try {
            if (mModifyData != null) {
                et_text.setText(mModifyData.getText());
                setOpen(mModifyData.getOpen() == 1);

                if(!TextUtils.isEmpty(mModifyData.getImg())) {
                    mpImg = mModifyData.getImg();
                    final String url = "https://docs.google.com/uc?export=download&id=" + mpImg;
                    GlobalApplication.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            setImage(url, mpImg);
                        }
                    });
                }

                isModify = true;
            }
        }catch (Exception e){}
    }


    private void setOpen(boolean open){
        isOpen = open;
        if(open) {
            tv_feed_all.setBackgroundResource(R.drawable.feed_write_select_bg);
            tv_feed_fam.setBackgroundResource(R.drawable.feed_write_unselect_bg2);

            tv_feed_all.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            tv_feed_fam.setTextColor(ContextCompat.getColor(getActivity(), R.color.pink));
        }
        else{
            tv_feed_all.setBackgroundResource(R.drawable.feed_write_unselect_bg);
            tv_feed_fam.setBackgroundResource(R.drawable.feed_write_select_bg2);

            tv_feed_all.setTextColor(ContextCompat.getColor(getActivity(), R.color.pink));
            tv_feed_fam.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_feed_write:{

                writeFeed();

                break;
            }
            case R.id.btn_add:{

                if (PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.GET_ACCOUNTS)
                        && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.RECORD_AUDIO)
                        ) {
                    checkGoogleAccountGeller();
                } else {
                    PermissionOk.checkPermission2(getActivity(), MY_PERMISSIONS_REQUEST_READ_CONTACTS, new PermissionOk.Callback() {
                        @Override
                        public void OnFail(final Runnable run) {
//                            new android.support.v7.app.AlertDialog.Builder(getActivity())
//                                    .setTitle(R.string.notice)
//                                    .setMessage(R.string.permission_file_err)
//                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
//
//                                            run.run();
//                                        }
//                                    })
//                                    .create().show();

                            run.run();
                        }

                        @Override
                        public void OnOk() {
                            checkGoogleAccountGeller();
                        }
                    });
                }


//                AdapterSpinner1 apdapter = new AdapterSpinner1(getActivity(), getResources().getStringArray(R.array.chat_room_add_file));
//                final ListPopupWindow popup = new ListPopupWindow(getActivity());
//                popup.setAnchorView(findViewById(R.id.btn_add));
//                popup.setModal(true);
//                popup.setWidth(apdapter.measureContentWidth());
//                popup.setAdapter(apdapter);
//                popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_white_background));
//                popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        popup.dismiss();
//
//                        if(position == 0){
//                            if (PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                                    && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.GET_ACCOUNTS)
//                                    && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.RECORD_AUDIO)
//                                    ) {
//                                File f = GlobalApplication.getProfileFile(getActivity());
//
//                                String filepath = f.getAbsolutePath() + "/jam_audio.wav";
//
//                                f = new File(filepath);
//                                try {
//                                    if (f.exists()) {
//                                        f.delete();
//                                    }
//                                } catch (Exception e) {
//                                }
//
//                                int color = getResources().getColor(R.color.colorPrimaryDark);
//
//                                mMyAudioPath = "";
//                                AndroidAudioRecorder.with(getActivity())
//                                        // Required
//                                        .setFilePath(f.getAbsolutePath())
//                                        .setColor(color)
//                                        .setRequestCode(REQUEST_RECORDING)
//                                        // Optional
//                                        .setSource(AudioSource.MIC)
//                                        .setChannel(AudioChannel.STEREO)
//                                        .setSampleRate(AudioSampleRate.HZ_48000)
//                                        .setAutoStart(false)
//                                        .setKeepDisplayOn(true)
//                                        .setMaxSeconded(15)
//                                        .recordFromFragment();
//                            } else {
//                                PermissionOk.checkPermission2(getActivity(), MY_PERMISSIONS_REQUEST_READ_CONTACTS, new PermissionOk.Callback() {
//                                    @Override
//                                    public void OnFail(final Runnable run) {
//                                        run.run();
//                                    }
//
//                                    @Override
//                                    public void OnOk() {
//                                    }
//                                });
//                            }
//                        }
//                        else if(position == 1){
//                            if (PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                                    && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.GET_ACCOUNTS)
//                                    && PermissionOk.checkPermissionOne(getActivity(), Manifest.permission.RECORD_AUDIO)
//                                    ) {
//                                checkGoogleAccountGeller();
//                            } else {
//                                PermissionOk.checkPermission2(getActivity(), MY_PERMISSIONS_REQUEST_READ_CONTACTS, new PermissionOk.Callback() {
//                                    @Override
//                                    public void OnFail(final Runnable run) {
////                            new android.support.v7.app.AlertDialog.Builder(getActivity())
////                                    .setTitle(R.string.notice)
////                                    .setMessage(R.string.permission_file_err)
////                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
////                                        public void onClick(DialogInterface dialog, int which) {
////                                            dialog.dismiss();
////
////                                            run.run();
////                                        }
////                                    })
////                                    .create().show();
//
//                                        run.run();
//                                    }
//
//                                    @Override
//                                    public void OnOk() {
//                                        checkGoogleAccountGeller();
//                                    }
//                                });
//                            }
//                        }
//
//                    }
//                });
//
//                popup.show();

                break;
            }
            case R.id.tv_feed_all:{
                setOpen(true);
                break;
            }
            case R.id.tv_feed_fam:{
                setOpen(false);
                break;
            }
        }
    }


    boolean isWriteClick = false;
    synchronized private void writeFeed(){
        if(isWriteClick)
            return;

        String s = et_text.getText().toString();

        {
            final FeedData d = new FeedData();
            String uid = MainActivity.mUserData.getUid();
            String pimg = "";
            String name = "";
            String color = "";

            for(RoomUserData u :mFeedData.getListRoomUserData()){
                if(u.getUid().equals(MainActivity.mUserData.getUid())){
                    pimg = u.getpImg();
                    name = u.getUserName();
                    color = u.getColor();
                    break;
                }
            }


            try {
                String xx = Utils.isBlockWord(s, getActivity().getResources().getStringArray(R.array.block_text));
                if (!TextUtils.isEmpty(xx)) {
                    CustomToast.showToast(getActivity(), String.format(getActivity().getString(R.string.block_text_err), xx), Toast.LENGTH_SHORT);
                    return;
                }
            }catch (Exception e){}

            if(TextUtils.isEmpty(name)){
                Toast.makeText(getActivity(), R.string.err_room_info, Toast.LENGTH_SHORT).show();
                finish();

                return;
            }


            d.setUserName(name);
            d.setUid(uid);
            if(!TextUtils.isEmpty(pimg))d.setpImg(pimg);
            if(!TextUtils.isEmpty(color))d.setColor(color);
            if(!TextUtils.isEmpty(mpImg))d.setImg(mpImg);

            d.setOpen(isOpen?1:0);
            d.setText(s);
            d.setTime(Fire.getServerTimestamp());



            if(TextUtils.isEmpty(mpImg) && (TextUtils.isEmpty(s) || s.length() < 1)){
                CustomToast.showToast(getActivity(), R.string.name_size_err, Toast.LENGTH_SHORT);
                return;
            }



            if(isModify && mModifyData != null){
                d.setTime(mModifyData.getTime());

                isWriteClick = true;
                Fire.getReference().child(Fire.KEY_STORY).child(mFeedData.getKey()).child(mModifyData.getKey()).updateChildren(d.getHashMap(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        isWriteClick = false;
                        if (databaseError != null && !TextUtils.isEmpty(databaseError.getMessage())) {
                        } else if (!TextUtils.isEmpty(databaseReference.getKey())) {
                            d.setKey(databaseReference.getKey());

                            Intent u = new Intent();
                            u.putExtra("data", d);
                            setResult(RESULT_OK, u);

                            FirebaseAnalyticsLog.setChatSend(getActivity(), "story_modify");
                        }

                        finish();
                    }
                });
            }
            else {
                isWriteClick = true;
                Fire.getReference().child(Fire.KEY_STORY).child(mFeedData.getKey()).push().setValue(d.getHashMap(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        isWriteClick = false;
                        if (databaseError != null && !TextUtils.isEmpty(databaseError.getMessage())) {
                        } else if (!TextUtils.isEmpty(databaseReference.getKey())) {
                            d.setKey(databaseReference.getKey());

                            Intent u = new Intent();
                            u.putExtra("data", d);
                            setResult(RESULT_OK, u);

                            FirebaseAnalyticsLog.setChatSend(getActivity(), "story");
                        }

                        finish();

                    }
                });

                try {
                    GlobalApplication.runBackground(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ArrayList<String> list = new ArrayList<String>();
                                for (RoomUserData uu : mFeedData.getListRoomUserData()) {
                                    if (uu.getOpen() == 1 && !uu.getUid().equals(MainActivity.mUserData.getUid())) {
                                        String pu = DBManager.createInstnace(getActivity()).getUserPush(uu.getUid());
                                        if (!TextUtils.isEmpty(pu)) {
                                            list.add(pu);

                                            LoggerManager.e("mun", "chat push : " + uu.getUid() + " / " + pu);
                                        } else {
                                            LoggerManager.e("mun", "chat push : " + uu.getUid() + " / no push");
                                        }
                                    }
                                }

                                new SendPushFCMStory(getActivity(), list, SendPushFCMStory.STORY_MSG_TYPE_STORY, mFeedData.getKey(), mFeedData.getTitle()).start();
                            } catch (Exception e) {
                            }
                        }
                    });
                } catch (Exception e) {
                }
            }


        }
    }











    public class AdapterSpinner1 extends BaseAdapter {

        public int measureContentWidth(){
            return measureContentWidth(this);
        }

        private int measureContentWidth(ListAdapter listAdapter) {
            ViewGroup mMeasureParent = null;
            int maxWidth = 0;
            View itemView = null;
            int itemType = 0;

            final ListAdapter adapter = listAdapter;
            final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                final int positionType = adapter.getItemViewType(i);
                if (positionType != itemType) {
                    itemType = positionType;
                    itemView = null;
                }

                if (mMeasureParent == null) {
                    mMeasureParent = new FrameLayout(getActivity());
                }

                itemView = adapter.getView(i, itemView, mMeasureParent);
                itemView.measure(widthMeasureSpec, heightMeasureSpec);

                final int itemWidth = itemView.getMeasuredWidth();

                if (itemWidth > maxWidth) {
                    maxWidth = itemWidth;
                }
            }

            return maxWidth;
        }



        Context context;
        List<String> data;
        LayoutInflater inflater;

        class ViewHolder {
            TextView tvTitle;
            FrameLayout btn_option;

            ViewHolder(View view) {
                tvTitle = view.findViewById(R.id.tv_text);
                btn_option = view.findViewById(R.id.btn_option);
            }
        }


        public AdapterSpinner1(Context context, String [] data){
            this.context = context;
            for(String s:data) {
                if(this.data == null){
                    this.data = new ArrayList<>();
                }
                this.data.add(s);
            }
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            if(data!=null) return data.size();
            else return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            AdapterSpinner1.ViewHolder holder;
            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.popupwindow_menu_url, parent, false);
                convertView = inflater.inflate(R.layout.popupwindow_menu, null);
                holder = new AdapterSpinner1.ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (AdapterSpinner1.ViewHolder) convertView.getTag();
            }

            if(data!=null && data.size() > position) {
                //데이터세팅
                String text = data.get(position);
                holder.tvTitle.setText(text);


                setPadding(holder, position);
            }

            return convertView;
        }

        @Override
        public View getDropDownView(final int position, View convertView, ViewGroup parent) {
            AdapterSpinner1.ViewHolder holder;
            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.popupwindow_menu_url, parent, false);
                convertView = inflater.inflate(R.layout.popupwindow_menu, null);
                holder = new AdapterSpinner1.ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (AdapterSpinner1.ViewHolder) convertView.getTag();
            }


            if(data!=null && data.size() > position) {
                //데이터세팅
                String text = data.get(position);
                holder.tvTitle.setText(text);

                setPadding(holder, position);
            }



            return convertView;
        }

        private void setPadding(AdapterSpinner1.ViewHolder holder, final int position){
            if(position == 0) {
                holder.btn_option.setPadding(Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 8), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0));
            }
            else if(data.size() == position + 1){
                holder.btn_option.setPadding(Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 8));
            }
            else{
                holder.btn_option.setPadding(Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0), Utils.getPixSize(getActivity(), 0));
            }
        }
        //머냐 슈발

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


    }






    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ACCOUNT_PICKER) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                mStrGoogleAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (!TextUtils.isEmpty(mStrGoogleAccountName)) {
                    loadGoogleDrive();
                }
            }
        }
        else if(requestCode == RECOVERABLE_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK) {
                if (!TextUtils.isEmpty(mStrGoogleAccountName)) {
                    loadGoogleDrive();
                }
            }
        }
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
                    intent.putExtra(MakePictureCropActivity.MODE_SQUARE, true);
                    startActivityForResult(intent, RESULT_CROP);


                }catch(Exception e){

                    try {
                        final String filepath = data.getData().getPath();

                        Intent intent = new Intent(getActivity(), MakePictureCropActivity.class);
                        intent.putExtra(MakePictureCropActivity.PATH, filepath);
                        intent.putExtra(MakePictureCropActivity.MODE_SQUARE, true);
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
                            new android.support.v7.app.AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.notice)
                                    .setMessage(R.string.permission_account)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();


                                            checkGoogleAccountImageAdd();

                                        }
                                    })
                                    .create().show();
                        }
                        else{
                            checkGoogleAccountImageAdd();
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
        else if (requestCode == REQUEST_RECORDING) {
            if (resultCode == Activity.RESULT_OK) {
                // Great! User has recorded and saved the audio file

                File f = GlobalApplication.getProfileFile(getActivity());

                String filepath = f.getAbsolutePath() + "/jam_audio.wav";

                f = new File(filepath);
                try {
                    if (f.exists()) {
                        mMyAudioPath = f.getAbsolutePath();

                        checkGoogleAccountAudio();


                    }
                }catch (Exception e){}

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Oops! User has canceled the recording
            }
        }

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

    
    
    
    
    



    final int RESULT_GALL_RETURN = 2212;
    final int RESULT_CROP = 2213;
    final public int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2214;



    public String mMyImagePath;
    public String mpImg;
    public String mAudio;

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


        GoogleDriveTokenAsyncTask mGoogleDriveTokenAsyncTask = new GoogleDriveTokenAsyncTask(getActivity(),null, getGoogleAccountCredential() , new GoogleDriveTokenAsyncTask.Callback() {

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

                        //ui초기화
                        mAudio = "";
                        mpImg = "";

                        Glide.with(getActivity())
                                .load(mpImg)
//                                .bitmapTransform(new CropCircleTransformation(getActivity()))
//                                .skipMemoryCache(true)
//                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .into(iv_img);


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


    /*****
     * drive
     */

    protected void setImage(final String url, String driveId) {
        //이미지처리
        Glide.with(getActivity())
                .load(url)
//                                .bitmapTransform(new CropCircleTransformation(getActivity()))
//                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(iv_img);

        LoggerManager.e("mun","image : "+ url);


//        Glide.with(getActivity()).load(url).downloadOnly(new SimpleTarget<File>() {
//            @Override
//            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
//
//                LoggerManager.e("mun","image : "+ resource.getAbsolutePath());
//
//                Glide.with(getActivity())
//                        .load(resource.getAbsolutePath())
////                                .bitmapTransform(new CropCircleTransformation(getActivity()))
////                                .skipMemoryCache(true)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(iv_img);
//
//            }
//        });

    }

    public void checkGoogleAccountAudio () {
        check_type = 3;
        checkGoogleAccountStep2();
    }

    public void checkGoogleAccountGeller () {
        check_type = 2;
        checkGoogleAccountStep2();
    }

    public void checkGoogleAccountImageAdd () {
        check_type = 1;
        checkGoogleAccountStep2();
    }

    private String mMyAudioPath;


    public void setDriveAudioUpload(){

        if(mProgressDialog!=null)mProgressDialog.show();

        GoogleDriveUploadAudioAsyncTask mGoogleDriveUploadAudioAsyncTask = new GoogleDriveUploadAudioAsyncTask(getActivity(), mMyAudioPath, mDrive, new GoogleDriveUploadAudioAsyncTask.Callback() {
            @Override
            public void result(String result, final boolean reLogin) {
                if(mProgressDialog!=null)mProgressDialog.dismiss();
                if(!TextUtils.isEmpty(result)) {
                    mAudio = result;
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
            mGoogleDriveUploadAudioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mGoogleDriveUploadAudioAsyncTask.execute();
        }
    }


    public void setDriveUpload(){
        try {
            LoggerManager.e("mun", "chat setDriveUpload : " + getClass().getName());
        }catch (Exception e){}
        if(mProgressDialog!=null)mProgressDialog.show();


        GlobalApplication.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                GoogleDriveUploadAsyncTask mGoogleDriveUploadAsyncTask = new GoogleDriveUploadAsyncTask(getActivity(), false, mMyImagePath, mDrive, new GoogleDriveUploadAsyncTask.Callback() {
                    @Override
                    public void result(String result, final boolean reLogin) {
                        if (mProgressDialog != null) mProgressDialog.dismiss();
                        if (!TextUtils.isEmpty(result)) {
                            mpImg = result;

                            DBManager.createInstnace(getActivity()).addDriveChatImg(MainActivity.mUserData.getUid(), result);

                            final String url = "https://docs.google.com/uc?export=download&id=" + result;
                            GlobalApplication.runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    setImage(url, mpImg);
                                }
                            });
                        } else {
                            GlobalApplication.runOnMainThread(new Runnable() {
                                @Override
                                public void run() {

                                    MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_TOKEN, "");
                                    MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_NAME, "");

                                    if (reLogin) {
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
        });


    }


    public void setListDialog(){
        Toast.makeText(getActivity(), R.string.jam_out_removeaudio, Toast.LENGTH_SHORT).show();
        BottomProfileListDialog mBottomProfileListDialog = new BottomProfileListDialog(getActivity(), mDrive, true);
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

}


