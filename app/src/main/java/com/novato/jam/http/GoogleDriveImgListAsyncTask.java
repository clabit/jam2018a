package com.novato.jam.http;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.model.Permission;
import com.novato.jam.GlobalApplication;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by birdgang on 2015. 11. 3..
 */
public class GoogleDriveImgListAsyncTask extends AsyncTask<Void, Void, ArrayList<String>> {

    private final String TAG = "GoogleDriveTokenAsyncTask";

    private Context mContext;
    private Callback mCallback;
    private com.google.api.services.drive.Drive mDrive = null;

    public GoogleDriveImgListAsyncTask(Context context, com.google.api.services.drive.Drive mDrive , Callback listener) {
        this.mDrive = mDrive;
        this.mCallback = listener;
        this.mContext = context;
    }



    @Override
    protected ArrayList<String> doInBackground(Void... params) {
        final ArrayList<String> resultEntry = new ArrayList<>();
        try {

            String folderid = "";
            {
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(GlobalApplication.getAppContext().getPackageName());
                fileMetadata.setMimeType("application/vnd.google-apps.folder");
                com.google.api.services.drive.model.FileList list = mDrive.files().list().setQ("mimeType = \"application/vnd.google-apps.folder\" and name = \"" + GlobalApplication.getAppContext().getPackageName() + "\"  and trashed=false ").execute();
                for (com.google.api.services.drive.model.File ff : list.getFiles()) {
                    if (!TextUtils.isEmpty(ff.getId())) {
                        folderid = ff.getId();
                        LoggerManager.e("mun", "folders: " + folderid);
                        break;
                    }
                }
            }


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


            String value = simpleDateFormat.format(new Date(System.currentTimeMillis()));
            LoggerManager.e("mun","sdsdsdsdsdsd : "+value);


            //mimeType = "image/jpeg" and
            com.google.api.services.drive.model.FileList list = mDrive.files().list().setQ("parents=\""+folderid+"\" and name contains \"jam_photo.\" and mimeType=\"image/png\" and createdTime >= \"2018-03-30T09:00:00\" ").execute();
            for(com.google.api.services.drive.model.File ff: list.getFiles()){
                if(!TextUtils.isEmpty(ff.getId())){

                    String id = ff.getId();
                    resultEntry.add(id);
                    LoggerManager.e("mun", "File ID: " + id );
                }
            }

            GlobalApplication.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if(mCallback!=null){
                        mCallback.result(resultEntry);
                    }
                }
            });


        } catch (IllegalArgumentException e) {
            LoggerManager.e("mun", "File ID + IllegalArgumentException : " + e.toString());
            GlobalApplication.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if(mCallback!=null){
                        mCallback.result(null);
                    }
                }
            });
        } catch (Exception e) {
            LoggerManager.e("mun", "File ID: " + e.toString());
            GlobalApplication.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if(mCallback!=null){
                        mCallback.result(null);
                    }
                }
            });
        }



        return resultEntry;
    }

    @Override
    protected void onPostExecute(ArrayList<String> resultEntry) {
        super.onPostExecute(resultEntry);
    }


    public interface Callback{
        public void result(ArrayList<String> result);
    }
}
