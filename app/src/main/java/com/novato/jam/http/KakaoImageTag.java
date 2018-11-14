package com.novato.jam.http;

import android.content.Context;
import android.text.TextUtils;

import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;

import org.json.JSONArray;
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

public class KakaoImageTag {

    final String call_url = //"https://kapi.kakao.com/v1/vision/multitag/generate";//tag
    "https://kapi.kakao.com/v1/vision/face/detect";//face age
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    String key = "";
    Context context;

    HashMap headers;

    File mFile;
    String path;

    public KakaoImageTag(Context context, String url) {
        key = context.getString(R.string.kakao_app_key);
        path = url;

        this.context = context;

        headers = new HashMap();
        headers.put("Authorization","KakaoAK "+key);
    }

    public KakaoImageTag(Context context, File f) {
        key = context.getString(R.string.kakao_app_key);
        mFile = f;

        this.context = context;

        headers = new HashMap();
        headers.put("Authorization","KakaoAK "+key);
    }


    public String start(){

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
                return g.toString();
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
            }


            Request request = mBuilder.build();

            try {
                Response response = OkClientHttp.getOkClient().newCall(request).execute();
                requestString = response.body().string();
                LoggerManager.e("mun", "requestString:" + requestString);

                JSONObject jj = new JSONObject(requestString);
                JSONArray d = jj.getJSONObject("result").getJSONArray("faces");
                for(int i=0; i<d.length(); i++){
                    JSONObject facial_attributes = d.getJSONObject(i).getJSONObject("facial_attributes");
                    LoggerManager.e("mun", "gender:" + facial_attributes.get("gender").toString());
                    LoggerManager.e("mun", "age:" + facial_attributes.get("age").toString());

                    LoggerManager.e("mun", "x:" + d.getJSONObject(i).get("x").toString());
                    LoggerManager.e("mun", "y:" + d.getJSONObject(i).get("y").toString());
                }

            } catch (Exception e) {
            }
        }catch (Exception e){}

        return requestString;
    }


}
