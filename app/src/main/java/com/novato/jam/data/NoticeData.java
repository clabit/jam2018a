package com.novato.jam.data;

import android.os.Parcel;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.novato.jam.common.LoggerManager;

import java.util.HashMap;

/**
 * Created by poshaly on 16. 9. 13..
 */
public class NoticeData extends BaseData {
    final static public int TYPE_ONE = 1;
    final static public int TYPE_RECYCLE = 2;


    private String key = "";
    private String userName = "";
    private String desc = "";
    private long time;

    private int type;

    public NoticeData() {
    }

    public NoticeData(String key) {
        this.key = key;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(key);

        dest.writeString(userName);
        dest.writeString(desc);
        dest.writeLong(time);
        dest.writeInt(type);
    }
    @Override
    public void readFromParcel(Parcel src) {
        super.readFromParcel(src);
        key = src.readString();

        userName = src.readString();
        desc = src.readString();
        time = src.readLong();
        type = src.readInt();
    }

    public void setHashMap(HashMap<String, Object> json){
        if(json.get("userName")!=null)userName = json.get("userName") + "";
        if(json.get("desc")!=null)desc = json.get("desc") + "";
        if(json.get("time")!=null)time = Long.parseLong(json.get("time")+"");
        if(json.get("type")!=null)type = Integer.parseInt(json.get("type") + "");

    }


    public HashMap<String, Object> getHashMap(){
        HashMap<String, Object> json = new HashMap<String, Object>();

        try {
            json.put("userName", userName);
            json.put("desc", desc);
            json.put("time", time);
            json.put("type", type);

        }catch (Exception e){}
        return json;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
