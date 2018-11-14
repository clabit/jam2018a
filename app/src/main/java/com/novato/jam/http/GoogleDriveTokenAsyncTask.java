package com.novato.jam.http;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.novato.jam.GlobalApplication;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.ui.fragment.RoomCreateFragment;

import java.io.IOException;

/**
 * Created by birdgang on 2015. 11. 3..
 */
public class GoogleDriveTokenAsyncTask extends AsyncTask<Void, Void, GoogleDriveTokenAsyncTask.Data> {

    private final String TAG = "GoogleDriveTokenAsyncTask";

    private GoogleAccountCredential mCredential = null;
    private Context mContext;
    private Fragment mFragment;
    private Callback mCallback;

    public GoogleDriveTokenAsyncTask(Context context, Fragment mFragment, GoogleAccountCredential credential , Callback listener) {
        this.mCredential = credential;
        this.mCallback = listener;
        this.mContext = context;
        this.mFragment = mFragment;
    }



    @Override
    protected Data doInBackground(Void... params) {
        Data resultEntry = null;
        try {
            String scope = mCredential.getScope();
            String name = mCredential.getSelectedAccountName();
            String token = mCredential.getToken();

            resultEntry = new Data();
            resultEntry.setName(name);
            resultEntry.setToken(token);
        } catch (IOException transientEx) {
        } catch (final UserRecoverableAuthException e) {
            LoggerManager.e("mun", "UserRecoverableAuthException : "+e.toString());
            GlobalApplication.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if(mFragment!=null){
                        mFragment.startActivityForResult(e.getIntent(), RoomCreateFragment.RECOVERABLE_REQUEST_CODE);
                    }
                    else if(mContext != null) {
                        ((Activity) mContext).startActivityForResult(e.getIntent(), RoomCreateFragment.RECOVERABLE_REQUEST_CODE);
                    }
                }
            });
        } catch (GoogleAuthException e) {
            LoggerManager.e("mun", "GoogleAuthException : "+e.toString());
        }
        catch (Exception e) {
            LoggerManager.e("mun", "Exception : "+e.toString());
        }
        return resultEntry;
    }

    @Override
    protected void onPostExecute(Data resultEntry) {
        super.onPostExecute(resultEntry);
        if(mCallback!=null){
            mCallback.result(resultEntry);
        }
    }


    public interface Callback{
        public void result(Data result);
    }

    public class Data{
        public String name = "";
        public String token = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
