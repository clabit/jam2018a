package com.novato.jam.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.novato.jam.http.OkClientHttp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2016-01-19.
 */
public class ImageDownTask extends AsyncTask<String, Void, File> {

    ImageDownTaskListner mListener;
    Context context;

    public ImageDownTask(Context context, ImageDownTaskListner listener)
    {
        this.context = context;
        mListener = listener;
    }

    @Override
    protected File doInBackground(String... params) {
        Bitmap imgBitmap = null;
        String sdcardFile = null;
        try {
            String strImageURL = params[0];
            sdcardFile = params[1];


//            Request request = new Request.Builder()
//                    .url(strImageURL)
//                    .build();
//
//            Response response = OkClientHttp.getOkClient().newCall(request).execute();
//            InputStream inputStream = response.body().byteStream();
//            imgBitmap = BitmapFactory.decodeStream(inputStream);
//
//            inputStream.close();
//            response.close();

            URL url = new URL(strImageURL);
            URLConnection conn = url.openConnection();
            conn.connect();

            int nSize = conn.getContentLength();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), nSize);
            imgBitmap = BitmapFactory.decodeStream(bis);
            bis.close();


        } catch (Exception ex) {
            LoggerManager.e("mun",ex.toString());
        }

        try{
            if(context!=null){
                imgBitmap = Utils.addWaterMark(imgBitmap, context, false);
            }
        }catch (Exception e){}
        File file = new File(sdcardFile);

        try {
            FileOutputStream filestream = new FileOutputStream(file);
            imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, filestream);

            imgBitmap.recycle();
            imgBitmap = null;
        } catch (Throwable e) {
            LoggerManager.e("mun",e.toString());
        }

        return file;
    }

    @Override
    protected void onPostExecute(File bm) {
        super.onPostExecute(bm);

        if(mListener!=null){
            if(bm !=null && bm.exists() && bm.length() > 0){
                mListener.onSuccess(bm);
            }
            else{
                mListener.onFail();
            }
        }
    }

    public interface ImageDownTaskListner {
        void onSuccess(File f);
        void onFail();
    }
}
