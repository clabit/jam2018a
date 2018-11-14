package com.novato.jam.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.db.MyPreferences;

import java.util.HashMap;

/**
 * Created by poshaly on 16. 9. 13..
 */
public class UserData extends BaseData {
//    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
//        @Override
//        public UserData createFromParcel(Parcel source) {
//            return new UserData(source);
//        }
//        @Override
//        public UserData[] newArray(int size) {
//            return new UserData[size];
//        }
//    };
//    public UserData(Parcel src){
//        super(src);
//        readFromParcel(src);
//    }

    private String key = "";
    private String userName = "";
    private String desc = "";
    private String uid = "";
    private String pImg = "";
    private String mail = "";
    private String push = "";
    private long time = -1;
    private String color;

    private int rowType;

    public UserData() {
    }

    public UserData(String key) {
        this.key = key;
    }

    public UserData(FirebaseUser user){

        try {
            setUid(user.getUid());

            if (!TextUtils.isEmpty(user.getDisplayName()))
                setUserName(user.getDisplayName());
            if (user.getPhotoUrl() != null && !TextUtils.isEmpty(user.getPhotoUrl().toString()))
                setpImg(user.getPhotoUrl().toString());
            if (!TextUtils.isEmpty(user.getEmail()))
                setMail(user.getEmail());

            LoggerManager.e("mun", "Uid : " + user.getUid());
            LoggerManager.e("mun", "mail : " + user.getEmail());
            LoggerManager.e("mun", "DisplayName : " + user.getDisplayName());
            LoggerManager.e("mun", "PhotoUrl : " + user.getPhotoUrl());
        }catch (Exception e){}
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(key);

        dest.writeString(userName);
        dest.writeString(desc);
        dest.writeString(uid);
        dest.writeString(pImg);
        dest.writeString(mail);
        dest.writeLong(time);
        dest.writeString(color);

        dest.writeInt(rowType);
        dest.writeString(push);
    }
    @Override
    public void readFromParcel(Parcel src) {
        super.readFromParcel(src);
        key = src.readString();

        userName = src.readString();
        desc = src.readString();
        uid = src.readString();
        pImg = src.readString();
        time = src.readLong();
        color = src.readString();
        mail = src.readString();

        rowType = src.readInt();
        push = src.readString();
    }

    public void setHashMap(HashMap<String, Object> json){
        if(json.get("userName")!=null)userName = json.get("userName") + "";
        if(json.get("desc")!=null)desc = json.get("desc") + "";
        if(json.get("uid")!=null)uid = json.get("uid") + "";
        if(json.get("pImg")!=null)pImg = json.get("pImg") + "";
        if(json.get("time")!=null)time = Long.parseLong(json.get("time")+"");
        if(json.get("color")!=null)color = json.get("color")+"";
        if(json.get("mail")!=null)mail = json.get("mail") + "";
        if(json.get("push")!=null)push = json.get("push") + "";

    }


    public HashMap<String, Object> getHashMap(){
        HashMap<String, Object> json = new HashMap<String, Object>();

        try {
            if(!TextUtils.isEmpty(userName))json.put("userName", userName);
            if(!TextUtils.isEmpty(desc))json.put("desc", desc);
            if(!TextUtils.isEmpty(uid))json.put("uid", uid);
            if(!TextUtils.isEmpty(pImg))json.put("pImg", pImg);
            if(time > -1)json.put("time", time);
            if(!TextUtils.isEmpty(color))json.put("color", color);
            if(!TextUtils.isEmpty(mail))json.put("mail", mail);
            if(!TextUtils.isEmpty(push))json.put("push", push);

        }catch (Exception e){}
        return json;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getpImg() {
        return pImg;
    }

    public void setpImg(String pImg) {
        this.pImg = pImg;
    }

    public int getRowType() {
        return rowType;
    }

    public void setRowType(int rowType) {
        this.rowType = rowType;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPush() {
        return push;
    }

    public void setPush(String push) {
        this.push = push;
    }
}
