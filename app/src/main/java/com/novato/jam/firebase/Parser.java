package com.novato.jam.firebase;

import android.text.TextUtils;
import com.google.firebase.database.DataSnapshot;
import com.novato.jam.data.FeedData;
import com.novato.jam.data.NoticeData;
import com.novato.jam.data.ReportData;
import com.novato.jam.data.RoomUserData;
import com.novato.jam.data.UserData;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by poshaly on 2017. 8. 2..
 */

public class Parser {




    static public ArrayList<FeedData> getFeedDataListParse(DataSnapshot dataSnapshot){
        if(dataSnapshot == null || dataSnapshot.getValue() == null){
            return  null;
        }

        ArrayList<FeedData> list = new ArrayList<>();

        HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
        if (data != null) {
            for (String key : data.keySet()) {
                HashMap<String, Object> itemData = (HashMap<String, Object>) data.get(key);

                FeedData mArticleData = getFeedDataParse(key, itemData);

                if(mArticleData!=null){
                    list.add(mArticleData);
                }
            }
        }

        return list;

    }
    static public FeedData getFeedDataParse(String key, HashMap<String, Object> itemData){
        if(itemData == null || TextUtils.isEmpty(key)){
            return  null;
        }

        FeedData mArticleData = new FeedData(key);
        mArticleData.setHashMap(itemData);

        return mArticleData;
    }




    static public ArrayList<UserData> getUserDataListParse(DataSnapshot dataSnapshot){
        if(dataSnapshot == null || dataSnapshot.getValue() == null){
            return  null;
        }

        ArrayList<UserData> list = new ArrayList<>();

        HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
        if (data != null) {
            for (String key : data.keySet()) {
                HashMap<String, Object> itemData = (HashMap<String, Object>) data.get(key);

                UserData mArticleData = getUserDataParse(key, itemData);

                if(mArticleData!=null){
                    list.add(mArticleData);
                }
            }
        }

        return list;

    }
    static public UserData getUserDataParse(String key, HashMap<String, Object> itemData){
        if(itemData == null || TextUtils.isEmpty(key)){
            return  null;
        }

        UserData mArticleData = new UserData(key);
        mArticleData.setHashMap(itemData);

        return mArticleData;
    }



    static public ArrayList<RoomUserData> getRoomUserDataListParse(DataSnapshot dataSnapshot){
        if(dataSnapshot == null || dataSnapshot.getValue() == null){
            return  null;
        }

        ArrayList<RoomUserData> list = new ArrayList<>();

        HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
        if (data != null) {
            for (String key : data.keySet()) {
                HashMap<String, Object> itemData = (HashMap<String, Object>) data.get(key);

                RoomUserData mArticleData = getRoomUserDataParse(key, itemData);

                if(mArticleData!=null){
                    list.add(mArticleData);
                }
            }
        }

        return list;

    }
    static public ArrayList<RoomUserData> getRoomUserDataListParse(HashMap<String, Object> data){
        if(data == null){
            return  null;
        }

        ArrayList<RoomUserData> list = new ArrayList<>();

        if (data != null) {
            for (String key : data.keySet()) {
                HashMap<String, Object> itemData = (HashMap<String, Object>) data.get(key);

                RoomUserData mArticleData = getRoomUserDataParse(key, itemData);

                if(mArticleData!=null){
                    list.add(mArticleData);
                }
            }
        }

        return list;

    }
    static public RoomUserData getRoomUserDataParse(String key, HashMap<String, Object> itemData){
        if(itemData == null || TextUtils.isEmpty(key)){
            return  null;
        }

        RoomUserData mArticleData = new RoomUserData(key);
        mArticleData.setHashMap(itemData);

        return mArticleData;
    }



    static public ArrayList<ReportData> getRoomReportDataListParse(HashMap<String, Object> data){
        if(data == null){
            return  null;
        }

        ArrayList<ReportData> list = new ArrayList<>();

        if (data != null) {
            for (String key : data.keySet()) {
                HashMap<String, Object> itemData = (HashMap<String, Object>) data.get(key);

                ReportData mArticleData = getRoomReportDataParse(key, itemData);

                if(mArticleData!=null){
                    list.add(mArticleData);
                }
            }
        }

        return list;

    }
    static public ReportData getRoomReportDataParse(String key, HashMap<String, Object> itemData){
        if(itemData == null || TextUtils.isEmpty(key)){
            return  null;
        }

        ReportData mArticleData = new ReportData(key);
        mArticleData.setHashMap(itemData);

        return mArticleData;
    }


    static public ArrayList<NoticeData> getNoticeDataListParse(DataSnapshot dataSnapshot){
        if(dataSnapshot == null || dataSnapshot.getValue() == null){
            return  null;
        }

        ArrayList<NoticeData> list = new ArrayList<>();

        HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
        if (data != null) {
            for (String key : data.keySet()) {
                HashMap<String, Object> itemData = (HashMap<String, Object>) data.get(key);

                NoticeData mArticleData = getNoticeDataParse(key, itemData);

                if(mArticleData!=null){
                    list.add(mArticleData);
                }
            }
        }

        return list;

    }
    static public NoticeData getNoticeDataParse(String key, HashMap<String, Object> itemData){
        if(itemData == null || TextUtils.isEmpty(key)){
            return  null;
        }

        NoticeData mArticleData = new NoticeData(key);
        mArticleData.setHashMap(itemData);

        return mArticleData;
    }




    static public ArrayList<HashMap> getAudioListParse(DataSnapshot dataSnapshot){
        if(dataSnapshot == null || dataSnapshot.getValue() == null){
            return  null;
        }

        ArrayList<HashMap> list = new ArrayList<>();

        HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
        if (data != null) {
            for (String key : data.keySet()) {
                HashMap<String, Object> itemData = (HashMap<String, Object>) data.get(key);

                HashMap h = new HashMap();
                h.put("key", key);
                h.put("id", itemData.get("d")+"");
                list.add(h);
            }
        }

        return list;

    }

}
