package com.novato.jam.push;

import android.content.Context;

import com.novato.jam.common.LoggerManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.firebase.Fire;
import com.novato.jam.http.OkClientHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by poshaly on 2017. 2. 23..
 */

public class SendPushFCMNewChat {

    final String call_url = "https://fcm.googleapis.com/fcm/send";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    Context context;
    ArrayList<String> fcmKey;
    String chatKey;

    String from_uid;
    String from_push;

    HashMap headers;

    public SendPushFCMNewChat(Context context, ArrayList<String> fcmKey, String key) {
        this.context = context;
        this.fcmKey = fcmKey;
        this.chatKey = key;

        from_uid = MyPreferences.getString(context, MyPreferences.KEY_uid);
        from_push = MyPreferences.getString(context, MyPreferences.KEY_push);

        headers = new HashMap();
        headers.put("Authorization","key=AAAAVCzz0fw:APA91bE4wQvaD8ozFQSEp66sUAMBVe9258K8ut3E6RdgBcAB-Tev66lZqhbzWQLf2mXsYnmCjD3ngxwW5Zodm9SLzjXzjeyN2LgmEziBvtLM_-TRJHjwnxW4adKj2dHUyGtet2j7X3wd");//파이어베이스 사이트에서 설정=>클라우스메시징에 서버키 있음..
        headers.put("Content-Type","application/json");
//        headers.put("Referer","http://pandora.tv");
//        headers.put("Host","pandora.tv");
    }


    public String start(){

        String requestString = "";

        try {
            JSONObject json = new JSONObject();

//            json.put("to", fcmKey);

            JSONArray aa = new JSONArray();
            if(fcmKey!=null){
                for(String ket:fcmKey){
                    aa.put(ket);
                }
            }
            json.put("registration_ids", aa);

            json.put("priority", "high");

//            {
//                JSONObject o = new JSONObject();
//                o.put("body", msg);
//                o.put("title", title);
//                json.put("notification", o);
//            }
            {
                JSONObject o = new JSONObject();
                o.put("chatKey", chatKey);
                o.put("time", Fire.getServerTimestamp());
                o.put("from_uid", from_uid);
                o.put("from_push", from_push);

                json.put("data", o);
            }



            String call = json.toString();

            RequestBody body = RequestBody.create(JSON, call);


            Request.Builder mBuilder = new Request.Builder();
            mBuilder.url(call_url);
            mBuilder.post(body);

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
            } catch (Exception e) {
            }
        }catch (Exception e){}

        return requestString;
    }


}
