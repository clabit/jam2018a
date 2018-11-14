package com.novato.jam.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.BuildConfig;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.FeedData;
import com.novato.jam.db.DBManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.dialog.CustomAlertDialog;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.http.GoogleDriveRemoveAudioAsyncTask;
import com.novato.jam.http.GoogleDriveRemoveImageAsyncTask;
import com.novato.jam.ui.AdminStarListActivity;
import com.novato.jam.ui.AgreementActivity;
import com.novato.jam.ui.IntroActivity;
import com.novato.jam.ui.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class SettingFragment extends android.support.v4.app.Fragment implements View.OnClickListener{

    private View mRootView;
    private CheckBox cb_push;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_setting, container, false);


        mRootView.findViewById(R.id.btn_logout).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_agree01).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_agree02).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_agree03).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_agree04).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_removeaudioimage).setOnClickListener(this);

        if(MainActivity.isAdmin){
            mRootView.findViewById(R.id.btn_admin_starlist).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.btn_admin_starlist).setOnClickListener(this);

            mRootView.findViewById(R.id.btn_admin_removelist).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.btn_admin_removelist).setOnClickListener(this);

        }

        ((TextView)mRootView.findViewById(R.id.btn_version)).setText(String.format(getString(R.string.setting_version), GlobalApplication.getApplicationVersion()));


        cb_push = mRootView.findViewById(R.id.cb_push);
        cb_push.setChecked(MyPreferences.getSettingPush(getActivity()));
        cb_push.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyPreferences.setSettingPush(getActivity(), isChecked);
            }
        });

        return mRootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_removeaudioimage:{
                CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getActivity(), true, R.string.setting_removeaudio, R.string.setting_removeaudio_msg, new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                    @Override
                    public void onClickOk() {
                        try {

                            checkGoogleAccountStep2();
                        }catch (Exception e){}
                    }

                    @Override
                    public void onClickCancel() {

                    }
                });
                mCustomAlertDialog.show();

                break;
            }
            case R.id.btn_logout:{

                CustomAlertDialog mCustomAlertDialog = new CustomAlertDialog(getActivity(), true, R.string.logout_title, R.string.logout_msg, new CustomAlertDialog.onCustomAlertDialogItemClickListener() {
                    @Override
                    public void onClickOk() {
                        IntroActivity.setLogOut(getActivity());
                        getActivity().finish();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                    }

                    @Override
                    public void onClickCancel() {

                    }
                });
                mCustomAlertDialog.show();

                break;
            }
            case R.id.btn_agree01:{
                Intent i = new Intent(getActivity(), AgreementActivity.class);
                i.putExtra("type", 1);
                startActivity(i);

                break;
            }
            case R.id.btn_agree02:{
                Intent i = new Intent(getActivity(), AgreementActivity.class);
                i.putExtra("type", 2);
                startActivity(i);

                break;
            }
            case R.id.btn_agree03:{
                Intent i = new Intent(getActivity(), AgreementActivity.class);
                i.putExtra("type", 3);
                startActivity(i);

                break;
            }
            case R.id.btn_agree04:{

                Intent i = new Intent(getActivity(), AgreementActivity.class);
                i.putExtra("type", 4);
                startActivity(i);

                break;
            }
            case R.id.btn_admin_starlist:{

                if(!MainActivity.isAdmin)
                    return;

                Intent i = new Intent(getActivity(), AdminStarListActivity.class);
                startActivity(i);

                break;
            }
            case R.id.btn_admin_removelist:{
                if(!MainActivity.isAdmin)
                    return;

                {
                    Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("-99").orderByChild("time").endAt(Fire.getServerTimestamp()- 1000 * 60 * 10).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final ArrayList<FeedData> lists = Parser.getFeedDataListParse(dataSnapshot);
                            Collections.sort(lists, mComparatorNumber);


                            if(lists!=null && lists.size() > 0) {
                                FeedData data = lists.get(0);

                                android.support.v7.app.AlertDialog.Builder alert_confirm = new android.support.v7.app.AlertDialog.Builder(getActivity());
                                alert_confirm.setTitle(R.string.notice);
                                alert_confirm.setMessage( "총"+lists.size() +"개\n"+data.getTitle() + " / " + Utils.getDateTimeString(data.getTime()) +"\n밑의 글들 모두 삭제???"
                                );
                                alert_confirm.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        for (FeedData d : lists) {
                                            LoggerManager.e("mun", d.getTitle() + " / " + Utils.getDateTimeString(d.getTime()));

                                            if(!TextUtils.isEmpty(d.getKey()))Fire.getReference().child(Fire.KEY_CHAT_ROOM).child("-99").child(d.getKey()).removeValue();
                                        }


                                    }
                                });
                                alert_confirm.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                    }
                                });
                                alert_confirm.show();



                            }
                            else{
                                Toast.makeText(getActivity(), "size  0", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                break;
            }
        }
    }


    public Comparator mComparatorNumber = new Comparator<FeedData>() {
        @Override
        public int compare(FeedData s1, FeedData s2) {
            int mReturn = -1;

            if(s1.getTime() < s2.getTime()){
                mReturn = 1;
            }
            else if(s1.getTime() > s2.getTime()){
                mReturn = -1;
            }
            else{
                mReturn = 0;
            }
            //return s11 < s22 ? -1 : s11 > s22 ? 1:0;

            return mReturn;
        }
    };






    public GoogleAccountCredential mGoogleAccountCredential = null;
    public com.google.api.services.drive.Drive mDrive = null;

    public void checkGoogleAccountStep2() {
        String mStrGoogleAccountName = MyPreferences.getString(getActivity(), MyPreferences.KEY_DRIVE_NAME);
        String mStrGoogleDriveAccessToken = MyPreferences.getString(getActivity(), MyPreferences.KEY_DRIVE_TOKEN);


        if (!TextUtils.isEmpty(mStrGoogleAccountName)) {
            loadGoogleDrive(mStrGoogleAccountName);
        }
    }

    public GoogleAccountCredential getGoogleAccountCredential() {
        if (mGoogleAccountCredential == null) {
            mGoogleAccountCredential = GoogleAccountCredential.usingOAuth2(GlobalApplication.getAppContext(), Arrays.asList(DriveScopes.DRIVE)).setBackOff(new ExponentialBackOff());
        }
        return mGoogleAccountCredential;
    }

    private com.google.api.services.drive.Drive getDriveService(GoogleAccountCredential credential) {
//        return new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new CustomJsonFactory(), credential).build();
        return new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), credential).build();
    }

    public void loadGoogleDrive (String mStrGoogleAccountName) {
        if (TextUtils.isEmpty(mStrGoogleAccountName)) {
            return;
        }

        getGoogleAccountCredential().setSelectedAccountName(mStrGoogleAccountName);
        mDrive = getDriveService(getGoogleAccountCredential());


        {
            GoogleDriveRemoveAudioAsyncTask mGoogleDriveRemoveAudioAsyncTask = new GoogleDriveRemoveAudioAsyncTask(getActivity(), mDrive, new GoogleDriveRemoveAudioAsyncTask.Callback() {
                @Override
                public void result(String result, boolean reLogin) {

                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mGoogleDriveRemoveAudioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mGoogleDriveRemoveAudioAsyncTask.execute();
            }
        }

        {
            try {
                DBManager.createInstnace(getActivity()).removeDriveChatImgsAll();
            }catch (Exception e){}

            GoogleDriveRemoveImageAsyncTask mGoogleDriveRemoveImageAsyncTask = new GoogleDriveRemoveImageAsyncTask(getActivity(), mDrive, new GoogleDriveRemoveImageAsyncTask.Callback() {
                @Override
                public void result(String result, boolean reLogin) {

                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mGoogleDriveRemoveImageAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mGoogleDriveRemoveImageAsyncTask.execute();
            }
        }

    }
}
