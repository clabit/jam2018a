package com.novato.jam.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.novato.jam.common.LoggerManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * Created by poshaly on 16. 9. 13..
 */
public class RoomUserData extends BaseData {
//    public static final Parcelable.Creator<RoomUserData> CREATOR = new Parcelable.Creator<RoomUserData>() {
//        @Override
//        public RoomUserData createFromParcel(Parcel source) {
//            return new RoomUserData(source);
//        }
//        @Override
//        public RoomUserData[] newArray(int size) {
//            return new RoomUserData[size];
//        }
//    };
//    public RoomUserData(Parcel src){
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
    private long time;
    private long backuptime;//수정수정
    private String color;


    private int open;

    private int rowType;

    public RoomUserData() {
    }

    public RoomUserData(String key) {
        this.key = key;
    }

    public RoomUserData(FirebaseUser user){
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

    public void clearData(){
        key = "";
        userName = "";
        desc = "";
        uid = "";
        pImg = "";
        mail = "";
        push = "";
        time = -1;
        backuptime = -1;//수정수정
        color = null;

        open = -1;
        rowType = -1;

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
        dest.writeLong(backuptime);//수정수정
        dest.writeString(color);

        dest.writeInt(rowType);
        dest.writeString(push);
        dest.writeInt(open);
    }
    @Override
    public void readFromParcel(Parcel src) {
        super.readFromParcel(src);
        key = src.readString();

        userName = src.readString();
        desc = src.readString();
        uid = src.readString();
        pImg = src.readString();
        mail = src.readString();
        time = src.readLong();
        backuptime = src.readLong();//수정수정
        color = src.readString();

        rowType = src.readInt();
        push = src.readString();
        open = src.readInt();
    }

    public void setHashMap(HashMap<String, Object> json){
        if(json.get("userName")!=null)userName = json.get("userName") + "";
        if(json.get("desc")!=null)desc = json.get("desc") + "";
        if(json.get("uid")!=null)uid = json.get("uid") + "";
        if(json.get("pImg")!=null)pImg = json.get("pImg") + "";
        if(json.get("time")!=null)time = Long.parseLong(json.get("time")+"");
        if(json.get("backuptime")!=null)backuptime= Long.parseLong(json.get("backuptime")+"");//수정수정
        if(json.get("color")!=null)color = json.get("color") + "";
        if(json.get("mail")!=null)mail = json.get("mail") + "";
        if(json.get("push")!=null)push = json.get("push") + "";
        if(json.get("open")!=null)open = Integer.parseInt(json.get("open")+"");

    }


    public HashMap<String, Object> getHashMap(){
        HashMap<String, Object> json = new HashMap<String, Object>();

        try {
            json.put("userName", userName);
            json.put("desc", desc);
            json.put("uid", uid);
            json.put("pImg", pImg);
            json.put("time", time);
            json.put("backuptime", backuptime);//수정수정
            json.put("color", color);
            json.put("mail", mail);
            json.put("push", push);
            json.put("open", open);

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

    public long getBackuptime() {   return backuptime;    }//수정수정

    public void setBackuptime(long time) {
        this.backuptime = time;
    }//수정수정

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

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }
}
