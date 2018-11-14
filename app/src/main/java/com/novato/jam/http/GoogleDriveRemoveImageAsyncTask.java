package com.novato.jam.http;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.novato.jam.GlobalApplication;
import com.novato.jam.common.LoggerManager;

import java.util.ArrayList;

/**
 * Created by birdgang on 2015. 11. 3..
 */
public class GoogleDriveRemoveImageAsyncTask extends AsyncTask<Void, Void, String> {

    private final String TAG = "GoogleDriveTokenAsyncTask";

    private Context mContext;
    private Callback mCallback;
    private com.google.api.services.drive.Drive mDrive = null;
    private ArrayList<String> list;

    public GoogleDriveRemoveImageAsyncTask(Context context, com.google.api.services.drive.Drive mDrive , Callback listener) {
        this.mDrive = mDrive;
        this.mCallback = listener;
        this.mContext = context;
    }

    public GoogleDriveRemoveImageAsyncTask(Context context, com.google.api.services.drive.Drive mDrive , ArrayList<String> list, Callback listener) {
        this.mDrive = mDrive;
        this.mCallback = listener;
        this.mContext = context;
        this.list = list;
    }



    @Override
    protected String doInBackground(Void... params) {
        String resultEntry = null;
        try {

            if(list == null) {

                String folderid = "";
                {
                    com.google.api.services.drive.model.FileList list = mDrive.files().list().setQ("mimeType = \"application/vnd.google-apps.folder\" and name = \"" + GlobalApplication.getAppContext().getPackageName()+"_image" + "\"  and trashed=false ").execute();
                    for (com.google.api.services.drive.model.File ff : list.getFiles()) {
                        if (!TextUtils.isEmpty(ff.getId())) {
                            folderid = ff.getId();
                            LoggerManager.e("mun", "folders: " + folderid);
                            break;
                        }
                    }
                }


                try {
                    com.google.api.services.drive.model.FileList list = mDrive.files().list().setQ("mimeType = \"image/png\" and name=\"jam_photo.png\" and parents in \"" + folderid + "\" ").execute();
                    for (com.google.api.services.drive.model.File ff : list.getFiles()) {
                        if (!TextUtils.isEmpty(ff.getId())) {
                            LoggerManager.e("mun", "image remove: " + ff.getId() + " / " + ff.getName() + " / " + ff.getMimeType());
                            mDrive.files().delete(ff.getId()).execute();
                        }
                    }
                } catch (Exception e) {
                    LoggerManager.e("mun", "image remove: " + e.toString());
                }
            }
            else{
                for(String s:list){
                    if(!TextUtils.isEmpty(s))mDrive.files().delete(s).execute();
                }
            }


            GlobalApplication.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if(mCallback!=null){
                        mCallback.result(null, true);
                    }
                }
            });

        } catch (IllegalArgumentException e) {
            LoggerManager.e("mun", "File ID + IllegalArgumentException : " + e.toString());
            GlobalApplication.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if(mCallback!=null){
                        mCallback.result(null, true);
                    }
                }
            });
        } catch (Exception e) {
            LoggerManager.e("mun", "File ID: " + e.toString());
            GlobalApplication.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if(mCallback!=null){
                        mCallback.result(null, false);
                    }
                }
            });
        }



        return resultEntry;
    }

    @Override
    protected void onPostExecute(String resultEntry) {
        super.onPostExecute(resultEntry);
    }


    public interface Callback{
        public void result(String result, boolean reLogin);
    }
}