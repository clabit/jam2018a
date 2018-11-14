package com.novato.jam.common;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by poshaly on 16. 3. 31..
 */
public class PermissionOk
{//마시멜로관련




    static public boolean checkPermissionWindow(Activity con, int REQUEST) {
        //ㄷ다다른앱 위에그리기 권한.. 시스템 윈도우
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(con)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + con.getPackageName()));
                con.startActivityForResult(intent, REQUEST);

                return false;
            }

            return true;
        }


        return true;
    }
    static public void checkPermissionAllActivity(Activity con, int REQUEST) {
        //앱 정보->권한에서 해당 권한을 받도록 유도한다.
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + con.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        con.startActivityForResult(i, REQUEST);
    }
    static public boolean checkPermissionOne(Activity con, String permission){
        //permission = Manifest.permission.READ_CONTACTS
        return ContextCompat.checkSelfPermission(con, permission) == PackageManager.PERMISSION_GRANTED;

    }
    static public void checkPermission(final Activity con, final int MY_PERMISSIONS_REQUEST_READ_CONTACTS, Callback mCallback){//마시멜로관련


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(con,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(con,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED
                ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(con, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            else if(ActivityCompat.shouldShowRequestPermissionRationale(con, Manifest.permission.GET_ACCOUNTS)){
                ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            else {
                if(mCallback!=null)
                    mCallback.OnFail(new Runnable() {
                        @Override
                        public void run() {
                            ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//                            PermissionOk.checkPermissionAllActivity(con, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            //ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            //requestPermissions 를 한번더해서 엑티비티의 onRequestPermissionsResult 결과를 리스너 받아 checkPermissionAllActivity를 호출
                        }
                    });
            }
        }
        else{
            if(mCallback!=null)
                mCallback.OnOk();
        }
    }

    static public void checkPermission2(final Activity con, final int MY_PERMISSIONS_REQUEST_READ_CONTACTS, Callback mCallback){//마시멜로관련


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(con,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(con,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(con,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(con, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                LoggerManager.e("mun", "permission WRITE_EXTERNAL_STORAGE");
                ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS, Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            else if(ActivityCompat.shouldShowRequestPermissionRationale(con, Manifest.permission.GET_ACCOUNTS)){
                LoggerManager.e("mun", "permission GET_ACCOUNTS");
                ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS, Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            else if(ActivityCompat.shouldShowRequestPermissionRationale(con, Manifest.permission.RECORD_AUDIO)){
                LoggerManager.e("mun", "permission RECORD_AUDIO");
                ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS, Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            else {
                LoggerManager.e("mun", "permission not all");
                if(mCallback!=null)
                    mCallback.OnFail(new Runnable() {
                        @Override
                        public void run() {
                            ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS, Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);

//                            PermissionOk.checkPermissionAllActivity(con, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            //ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            //requestPermissions 를 한번더해서 엑티비티의 onRequestPermissionsResult 결과를 리스너 받아 checkPermissionAllActivity를 호출
                        }
                    });
            }
        }
        else{
            if(mCallback!=null)
                mCallback.OnOk();
        }
    }

    static public void checkPermissionAccount(final Activity con, final int MY_PERMISSIONS_REQUEST_READ_CONTACTS, Callback mCallback){//마시멜로관련


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(con,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(con, Manifest.permission.GET_ACCOUNTS)) {
                ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                if(mCallback!=null)
                    mCallback.OnFail(new Runnable() {
                        @Override
                        public void run() {
                            ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            //requestPermissions 를 한번더해서 엑티비티의 onRequestPermissionsResult 결과를 리스너 받아 checkPermissionAllActivity를 호출
                        }
                    });
            }
        }
        else{
            if(mCallback!=null)
                mCallback.OnOk();
        }
    }

    static public void checkPermissionOne(final Activity con, final String permission, final int MY_PERMISSIONS_REQUEST_READ_CONTACTS, Callback mCallback){//마시멜로관련


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(con, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(con, permission)) {
                LoggerManager.e("mun", "permission "+permission);
                ActivityCompat.requestPermissions(con, new String[]{permission}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            else {
                LoggerManager.e("mun", "permission not all");
                if(mCallback!=null)
                    mCallback.OnFail(new Runnable() {
                        @Override
                        public void run() {
                            ActivityCompat.requestPermissions(con, new String[]{permission}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    });
            }
        }
        else{
            if(mCallback!=null)
                mCallback.OnOk();
        }
    }



    public interface Callback{
        void OnFail(Runnable run);
        void OnOk();
    }
}
