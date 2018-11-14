package com.novato.jam.data;

import android.os.Parcel;

import java.util.HashMap;

/**
 * Created by poshaly on 16. 9. 13..
 */
public class ReportData extends BaseData {

    private String key = "";
    private String desc = "";
    private long time;

    public ReportData() {
    }

    public ReportData(String key) {
        this.key = key;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(key);
        dest.writeString(desc);
        dest.writeLong(time);
    }
    @Override
    public void readFromParcel(Parcel src) {
        super.readFromParcel(src);
        key = src.readString();

        desc = src.readString();
        time = src.readLong();
    }

    public void setHashMap(HashMap<String, Object> json){
        if(json.get("desc")!=null)desc = json.get("desc") + "";
        if(json.get("time")!=null)time = Long.parseLong(json.get("time")+"");

    }


    public HashMap<String, Object> getHashMap(){
        HashMap<String, Object> json = new HashMap<String, Object>();

        try {
            json.put("desc", desc);
            json.put("time", time);

        }catch (Exception e){}
        return json;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
}
