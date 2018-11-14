package com.novato.jam.http;

import android.content.Context;
import android.text.TextUtils;

import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by poshaly on 2017. 2. 23..
 */

public class KakaoImageFilter {

    final String call_url = "https://kapi.kakao.com/v1/vision/adult/detect";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    String key = "";
    Context context;

    HashMap headers;

    File mFile;
    String path;

    public KakaoImageFilter(Context context, String url) {
        key = context.getString(R.string.kakao_app_key);
        path = url;

        this.context = context;

        headers = new HashMap();
        headers.put("Authorization","KakaoAK "+key);
    }

    public KakaoImageFilter(Context context, File f) {
        key = context.getString(R.string.kakao_app_key);
        mFile = f;

        this.context = context;

        headers = new HashMap();
        headers.put("Authorization","KakaoAK "+key);
    }


    public boolean start(){
        boolean isAdult = false;
        String requestString = "";

        try {
            RequestBody requestBody = null;



            if(mFile!=null && mFile.exists()){
                requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", mFile.getName(), RequestBody.create(MultipartBody.FORM, mFile))
                        .build();
            }
            else if(!TextUtils.isEmpty(path)){
                requestBody = new FormBody.Builder()
                        .add("image_url", path)
                        .build();
            }




            if(requestBody == null){
                JSONObject g = new JSONObject();
                g.put("msg", "null param");

                LoggerManager.e("mun", "requestString:" + g.toString());
                return false;//g.toString();
            }



            Request.Builder mBuilder = new Request.Builder();
            mBuilder.url(call_url);
            mBuilder.post(requestBody);

            try {
                if (headers != null) {
                    HashMap map = headers;
                    Set<String> keys = map.keySet();
                    Iterator<String> iter = keys.iterator();

                    while (iter.hasNext()) {
                        try {
                            String key = iter.next();
                            mBuilder.header(key, map.get(key) + "");
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e) {
                LoggerManager.e("mun", "KakaoImageFilter:" + e.toString());
            }


            Request request = mBuilder.build();

            try {
                Response response = OkClientHttp.getOkClient().newCall(request).execute();
                requestString = response.body().string();
                LoggerManager.e("mun", "requestString:" + requestString);


                JSONObject j = new JSONObject(requestString);
                double result = j.getJSONObject("result").getDouble("adult");

                if(result > 0.8){
                    isAdult = true;
                }

                //{"result":{"normal":0.042,"soft":0.803,"adult":0.156}}

            } catch (Exception e) {
                LoggerManager.e("mun", "KakaoImageFilter:" + e.toString());
            }
        }catch (Exception e){
            LoggerManager.e("mun", "KakaoImageFilter:" + e.toString());
        }

        return isAdult;
    }


}
