package com.novato.jam.http;

import android.content.Context;
import android.text.TextUtils;

import com.novato.jam.common.LoggerManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.firebase.Fire;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by poshaly on 2017. 2. 23..
 */

public class GetTvBoxVideo {

    final String call_url = "http://imgcdn.pandora.tv/tvbox/kmp_tvbox_app.json";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    private String strLanguage = "gb";


    Context context;


    HashMap headers;

    public GetTvBoxVideo(Context context) {
        this.context = context;
        try {
            Locale systemLocale = context.getResources().getConfiguration().locale;
            strLanguage = systemLocale.getLanguage().toLowerCase();
        }catch (Exception e){}
    }

    //requestString:{"tvbox":{"kr":[{"set_data":[{"userid":"mobidictv","prg_id":"55922417","runtime":"143210","ch_thumbnail":"http:\/\/imguser2.pandora.tv\/pandora\/_channel_img\/e\/d\/edgerank\/logo_s.gif","title":"%EC%83%81%ED%81%BC+%EB%8F%8B%EB%8A%94+%EB%94%B8%EA%B8%B0%EB%B0%80%ED%91%80%EC%9C%A0%ED%86%A0%EC%8A%A4%ED%8A%B8","data":{"prgid":"55922417","all_class_code":"010000","short_url":"qsdBS6Dr","areoHit":"98","adult_chk":"0","prg_pub":"0","ch_userid":"mobidictv","subject":"\uceec\ub7ec\ud39c\uc2ac\ub85c \uc5b4\ub514\uae4c\uc9c0 \uc368\ubd24\ub2c8? (\ub625\uc190\uc744 \uc704\ud55c) [\uae40\uae30\uc218\uc758 \uc608\uc0b4\uadf8\uc0b4] \uceec\ub7ec\ud39c\uc2ac \ud3b8","status":"30003","scrap_pub":"1","fid":"20180316101612724agjqwt1dhzuvm","parent_prg_id":"46795120","upload_userid":"mobidictv","hit":"98","passing_time":"1\uc2dc\uac04 \uc804","facebook_hit":0,"facebook_count_show":"N","runtime":"143210","time":"00:02:23","day":"1\uc2dc\uac04 \uc804","score":"0.00","likecnt":"0","categ":"010000","cate":"01","cate_code":"01","nickname":"\ubaa8\ube44\ub515 Mobidic","imglogo":"http:\/\/imguser2.pandora.tv\/pandora\/_channel_img\/m\/o\/mobidictv\/logo_s.gif","resol":"6","vod_svr":"169","categ_id":"38739668","flv_scrsize":"*","hd_scrsize":"1080*1080","tag":"\uae40\uae30\uc218,\uc608\uc0b4\uadf8\uc0b4,\uc608\uc058\uac8c\uc0b4\ub798\uadf8\ub0e5\uc0b4\ub798,\ubdf0\ud2f0,\ud654\uc7a5,\ud654\uc7a5\ud488,\uceec\ub7ec\ud39c\uc2ac,\ud39c\uc2ac,\ubaa8\ube44\ub515,sbs,\uc544\uc774\ub77c\uc778,\uc100\ub3c4\uc6b0","reg_date":"2018-03-16","mobile_userid":"mobidictv","mobile_prgid":"55922417","embed_status":"","embed_vod_id":"","poster":"http:\/\/imguser2.pandora.tv\/pandora\/_channel_img_sm_temp\/m\/o\/mobidictv\/17\/vod_thumb_55922417.jpg","thumbnail":"http:\/\/imguser2.pandora.tv\/pandora\/_channel_img_sm_temp\/m\/o\/mobidictv\/17\/vod_thumb_55922417.jpg","prism_use_yn":0,"prism_skip_time":0,"resolArr":["1","2","3","5","6"],"service_url":{"2160":"","1080":"\/m\/o\/mobidictv\/fhd\/20180316101612724agjqwt1dhzuvm.flv?key1=32463344453630323830304331383931363930333431384333364337&key2=E028579026AF566EEC1691D32C59E3&ft=FC&class=normal&country=KR&pcode2=60135","720":"\/m\/o\/mobidictv\/hd\/20180316101612724agjqwt1dhzuvm.flv?key1=32463344453630323830304331383931363930333431384333364337&key2=E028579026AF566EEC1691D32C59E3&ft=FC&class=normal&country=KR&pcode2=68623","480":"\/m\/o\/mobidictv\/sd\/20180316101612724agjqwt1dhzuvm.flv?key1=32463344453630323830304331383931363930333431384333364337&key2=E028579026AF566EEC1691D32C59E3&ft=FC&class=normal&country=KR&pcode2=57197","336":"\/m\/o\/mobidictv\/flv\/20180316101612724agjqwt1dhzuvm.flv?key1=32463344453630323830304331383931363930333431384333364337&key2=E028579026AF566EEC1691D32C59E3&ft=FC&class=normal&country=KR&pcode2=62230","240":"\/m\/o\/mobidictv\/vld\/20180316101612724agjqwt1dhzuvm.flv?key1=32463344453630323830304331383931363930333431384333364337&key2=E028579026AF566EEC1691D32C59E3&ft=FC&class=normal&country=KR&pcode2=2069"},"log_runtime":"3","dtm":"365","gtm":"365","ch_name":"\ubaa8\ube44\ub515 Mobidic","body":"\ubdf0\ud2f0\ud06c\ub9ac\uc5d0\uc774\ud130 \uae40\uae30\uc218\uc758 \ud654\uc7a5 \ud29c\ud1a0\ub9ac\uc5bc \n\uc608\uc0b4\uadf8\uc0b4 \uceec\ub7ec\ud39c\uc2ac \ud3b8\n\n\u25b6\ubaa8\ube44\ub515 \ud398\uc774\uc2a4\ubd81 http:\/\/www.facebook.com\/mobidictv\/ \n\u25b6\ub124\uc774\ubc84 tv \uce90\uc2a4\ud2b8 http:\/\/tv.naver.com\/sbsunnieya \n\u25b6\ubaa8\ube44\ub515 \uacf5\uc2dd \uc0ac\uc774\ud2b8 http:\/\/www.mobidic.com","service_url_kr":{"2160":"","1080":"http:\/\/trans-idx.cdn.pandora.tv\/applecs.pandora.tv\/fhd\/_user\/m\/o\/mobidictv\/17\/20180316101612724agjqwt1dhzuvm.flv?ft=fc&class=normal&country=kr&cms=1&format=fsh&and_device=&and_os=&pcode2=60831","720":"http:\/\/trans-idx.cdn.pandora.tv\/applecs.pandora.tv\/hd\/_user\/m\/o\/mobidictv\/17\/20180316101612724agjqwt1dhzuvm.flv?ft=fc&class=normal&country=kr&cms=1&format=fsh&and_device=&and_os=&pcode2=48983","480":"http:\/\/trans-idx.cdn.pandora.tv\/applecs.pandora.tv\/sd\/_user\/m\/o\/mobidictv\/17\/20180

    public Map<String, Object> start(){

        Map<String, Object> params = new HashMap<String, Object>();

        String requestString = "";

        try {
            JSONObject json = new JSONObject();
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

                params = parse(requestString);
            } catch (Exception e) {
            }
        }catch (Exception e){}

        return params;
    }


    private Map<String, Object> parse(String response) {
        Map<String, Object> params = new HashMap<String, Object>();


        try {
            response = response.replace("\n", "");
        } catch (Exception e) {
        }


        try {
            String resultContents = response;
            JSONObject jobj = new JSONObject(String.valueOf(resultContents));

            JSONObject tvbox = jobj.getJSONObject("tvbox");

            JSONArray set_data;
            if("ko".equals(strLanguage)){
                JSONArray kr = tvbox.getJSONArray("kr");
                set_data = kr.getJSONObject(0).getJSONArray("set_data");
            }
            else{
                JSONArray gb = tvbox.getJSONArray("gb");
                set_data = gb.getJSONObject(0).getJSONArray("set_data");
            }

            if(set_data.length() > 0) {
                JSONObject service_url;


                Random r = new Random();
                int po = r.nextInt(set_data.length() - 1);
                if(po < 0)
                    po = 0;

                JSONObject item = set_data.getJSONObject(po);
                if("ko".equals(strLanguage)) {
                    service_url = item.getJSONObject("data").getJSONObject("service_url_kr");
                }
                else{
                    service_url = item.getJSONObject("data").getJSONObject("service_url_gb");
                }


                try {
                    String thum = item.getJSONObject("data").optString("thumbnail");
                    params.put("thum", thum);
                }catch (Exception e){}

                int lastSize = 99990;
                if(service_url !=null) {
                    String key;
                    for (Iterator<String> it = service_url.keys(); it.hasNext(); ) {
                        key = it.next();
                        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(service_url.optString(key))) {
//                            params.put(key, service_url.getString(key));
                            try {
                                if (lastSize > Integer.parseInt(key)) {
                                    params.put("url", service_url.getString(key));
                                    lastSize = Integer.parseInt(key);
                                }
                            }catch (Exception e){
                                params.put("url", service_url.getString(key));
                            }

                        }
                    }

                    try {
                        if (service_url.get("336") != null && !TextUtils.isEmpty(service_url.getString("336"))) {
                            params.put("url", service_url.getString("336"));
                            lastSize = Integer.parseInt("336");
                        }
                    }catch (Exception e){}

                    LoggerManager.e("fbads", "tvbox video lastSize : "+lastSize);

                }

            }





        } catch (JSONException e) {
        }



        return params;
    }


}
