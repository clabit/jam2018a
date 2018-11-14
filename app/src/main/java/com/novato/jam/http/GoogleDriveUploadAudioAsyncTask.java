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

import java.io.IOException;
import java.util.Collections;

/**
 * Created by birdgang on 2015. 11. 3..
 */
public class GoogleDriveUploadAudioAsyncTask extends AsyncTask<Void, Void, String> {

    private final String TAG = "GoogleDriveTokenAsyncTask";

    private Context mContext;
    private String mMyImagePath;
    private Callback mCallback;
    private com.google.api.services.drive.Drive mDrive = null;

    public GoogleDriveUploadAudioAsyncTask(Context context, String mMyImagePath, com.google.api.services.drive.Drive mDrive , Callback listener) {
        this.mDrive = mDrive;
        this.mCallback = listener;
        this.mContext = context;
        this.mMyImagePath = mMyImagePath;
    }



    @Override
    protected String doInBackground(Void... params) {
        String resultEntry = null;
        try {

            String folderid= "";
            {

                LoggerManager.e("mun","audioPath : "+ mMyImagePath);
                com.google.api.services.drive.model.FileList list = mDrive.files().list().setQ("mimeType = \"application/vnd.google-apps.folder\" and name = \""+ GlobalApplication.getAppContext().getPackageName() +"\"  and trashed=false ").execute();
                for(com.google.api.services.drive.model.File ff: list.getFiles()){
                    if(!TextUtils.isEmpty(ff.getId())){
                        folderid = ff.getId();
                        LoggerManager.e("mun", "folders: "+folderid);
                        break;
                    }
                }

                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(GlobalApplication.getAppContext().getPackageName());
                fileMetadata.setMimeType("application/vnd.google-apps.folder");

                if(TextUtils.isEmpty(folderid)) {
                    com.google.api.services.drive.model.File folder = mDrive.files().create(fileMetadata)
                            .setFields("id")
                            .execute();

                    folderid = folder.getId();

                    LoggerManager.e("mun", "folders create: "+folderid);
                }
            }


//            try{
//                com.google.api.services.drive.model.FileList list = mDrive.files().list().setQ("mimeType = \"audio/wav\" and name=\"jam_audio.wav\" and parents in \"" + folderid + "\" ").execute();
//                        //mDrive.files().list().setQ("mimeType = \"application/vnd.google-apps.audio\" and name=\""+"jam_audio.wav"+"\" and parents in \"" + folderid + "\" ").execute();
//                for (com.google.api.services.drive.model.File ff : list.getFiles()) {
//                    if (!TextUtils.isEmpty(ff.getId())) {
//                        LoggerManager.e("mun", "audio remove: "+ff.getId() + " / "+ ff.getName() + " / "+ ff.getMimeType());
//                        mDrive.files().delete(ff.getId()).execute();
//                    }
//                }
//            }catch (Exception e){
//                LoggerManager.e("mun", "audio remove: "+e.toString());
//            }


            //application/vnd.google-apps.audio

            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName("jam_audio.wav");
            fileMetadata.setParents(Collections.singletonList(folderid));
//            fileMetadata.setParents(Collections.singletonList("appDataFolder"));//퍼미션땜에 안됨.. 왜안되는지 멀겟네.

            java.io.File filePath = new java.io.File(mMyImagePath);

            FileContent mediaContent = new FileContent("audio/wav", filePath);

            com.google.api.services.drive.model.File file = mDrive.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            LoggerManager.e("mun", "file upload ok:" + file.getId());

            final String id = file.getId();

            if (!TextUtils.isEmpty(id)) {

                Permission userPermission = new Permission()
                        .setType("anyone")
                        .setRole("reader");

                BatchRequest batch = mDrive.batch();

                mDrive.permissions().create(id, userPermission)
                        .setFields("id")
                        .queue(batch, new JsonBatchCallback<Permission>() {

                            @Override
                            public void onSuccess(Permission permission, HttpHeaders responseHeaders) throws IOException {
                                LoggerManager.e("mun", "Permission : " + permission.getId());


//                                final String url = "https://docs.google.com/uc?export=download&id=" + id;

                                GlobalApplication.runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(mCallback!=null){
                                            mCallback.result(id, false);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                                LoggerManager.e("mun", "Permission : " + e.toString());

                                GlobalApplication.runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(mCallback!=null){
                                            mCallback.result(null, true);
                                        }
                                    }
                                });

                            }
                        });

                batch.execute();
            }
            else{
                GlobalApplication.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mCallback!=null){
                            mCallback.result(null, false);
                        }
                    }
                });
            }
            LoggerManager.e("mun", "File ID: " + file.getId());

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
