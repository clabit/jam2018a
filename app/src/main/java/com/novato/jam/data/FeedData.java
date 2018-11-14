package com.novato.jam.data;

import android.os.Parcel;
import android.text.TextUtils;

import com.google.firebase.database.ServerValue;
import com.novato.jam.facebookad.FBnative;
import com.novato.jam.firebase.Parser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by poshaly on 16. 9. 13..
 */
public class FeedData extends BaseData {

//    public static final Parcelable.Creator<FeedData> CREATOR = new Parcelable.Creator<FeedData>() {
//        @Override
//        public FeedData createFromParcel(Parcel source) {
//            return new FeedData(source);
//        }
//        @Override
//        public FeedData[] newArray(int size) {
//            return new FeedData[size];
//        }
//    };
//    public FeedData(Parcel src){
//        super(src);
//        readFromParcel(src);
//    }

    private String key = "";
    private String userName = "";
    private String uid = "";
    private String pImg = "";

    private String mic = "";
    private String iuid = "";

    private String img = "";

    private String title;
    private String text = "";
    private long time;
    private String color;

    private long like = -1;
    private long uCount = -1;
    private long chatCount = -1;
    private long cate = -1;

    private int open = 1;

    private int uc = 1;//가입시점 이전 채팅 목록 보는 권한


    private ArrayList<RoomUserData> listRoomUserData = new ArrayList<>();
//    private ArrayList<ReportData> listRoomReport = new ArrayList<>();




    private String link_url = "";

    private String videoId;
    private String statusType;

    private AdmobNativeData admobNativeData;
    private FBnative FBnativeData;

    private int rowType;

    public FeedData() {
    }

    public FeedData(String key) {
        this.key = key;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(key);

        dest.writeString(userName);
        dest.writeString(uid);
        dest.writeString(pImg);
        dest.writeString(mic);
        dest.writeString(iuid);
        dest.writeString(img);

        dest.writeString(title);
        dest.writeString(text);
        dest.writeLong(time);
        dest.writeString(color);
        dest.writeLong(like);
        dest.writeLong(uCount);
        dest.writeLong(chatCount);
        dest.writeLong(cate);
        dest.writeInt(open);
        dest.writeInt(uc);

        dest.writeTypedList(listRoomUserData);
//        dest.writeTypedList(listRoomReport);

        dest.writeString(link_url);
        dest.writeString(videoId);
        dest.writeString(statusType);
        dest.writeInt(rowType);


//        dest.writeTypedArray(listRoomUserData);
//        dest.writeParcelableArray(listRoomUserData.toArray(new RoomUserData[listRoomUserData.size()]), flags);

    }


    @Override
    public void readFromParcel(Parcel src) {
        super.readFromParcel(src);
        key = src.readString();

        userName = src.readString();
        uid = src.readString();
        pImg = src.readString();
        mic = src.readString();
        iuid = src.readString();
        img = src.readString();

        title = src.readString();
        text = src.readString();
        time = src.readLong();
        color = src.readString();
        like = src.readLong();
        uCount = src.readLong();
        chatCount = src.readLong();
        cate = src.readLong();

        open = src.readInt();
        uc = src.readInt();

        src.readTypedList(listRoomUserData, CREATOR);
//        src.readTypedList(listRoomReport, CREATOR);

        link_url = src.readString();
        videoId = src.readString();
        statusType = src.readString();
        rowType = src.readInt();


//        listRoomUserData = src.createTypedArrayList(RoomUserData.CREATOR);
//        listRoomUserData = new ArrayList(Arrays.asList(src.readParcelableArray(RoomUserData.class.getClassLoader())));

    }

    public void setHashMap(HashMap<String, Object> json){
        if(json.get("userName")!=null)userName = json.get("userName") + "";
        if(json.get("uid")!=null)uid = json.get("uid") + "";
        if(json.get("pImg")!=null)pImg = json.get("pImg") + "";
        if(json.get("mic")!=null)mic = json.get("mic") + "";
        if(json.get("iuid")!=null)iuid = json.get("iuid") + "";
        if(json.get("title")!=null)title = json.get("title") + "";
        if(json.get("text")!=null)text = json.get("text") + "";
        if(json.get("time")!=null)time = Long.parseLong(json.get("time")+"");
        if(json.get("color")!=null)color = json.get("color")+"";
        if(json.get("like")!=null)like = Long.parseLong(json.get("like")+"");
        if(json.get("chatCount")!=null)chatCount = Long.parseLong(json.get("chatCount")+"");
        if(json.get("uCount")!=null)uCount = Long.parseLong(json.get("uCount")+"");
        if(json.get("cate")!=null)cate = Long.parseLong(json.get("cate")+"");
        if(json.get("open")!=null)open = Integer.parseInt(json.get("open")+"");
        if(json.get("uc")!=null)uc = Integer.parseInt(json.get("uc")+"");

        if(json.get("img")!=null)img = json.get("img") + "";
        if(json.get("link_url")!=null)link_url = json.get("link_url") + "";
        if(json.get("videoId")!=null)videoId = json.get("videoId") + "";

        if(json.get("listRoomUserData")!=null)listRoomUserData = Parser.getRoomUserDataListParse((HashMap<String, Object>)json.get("listRoomUserData"));
//        if(json.get("listRoomReport")!=null)listRoomReport = Parser.getRoomReportDataListParse((HashMap<String, Object>)json.get("listRoomReport"));


        if(json.get("t")!=null)time = Long.parseLong(json.get("t")+"");
        if(json.get("c")!=null)uCount = Long.parseLong(json.get("c")+"");

    }


    public HashMap<String, Object> getHashMap(){
        HashMap<String, Object> json = new HashMap<String, Object>();

        try {
            if(!TextUtils.isEmpty(userName))json.put("userName", userName);
            if(!TextUtils.isEmpty(uid))json.put("uid", uid);
            if(!TextUtils.isEmpty(pImg))json.put("pImg", pImg);
            if(!TextUtils.isEmpty(mic))json.put("mic", mic);
            if(!TextUtils.isEmpty(iuid))json.put("iuid", iuid);
            if(!TextUtils.isEmpty(title))json.put("title", title);
            if(!TextUtils.isEmpty(text))json.put("text", text);
            if(time > -1)json.put("time", ServerValue.TIMESTAMP);
            if(!TextUtils.isEmpty(color))json.put("color", color);
            if(like > -1)json.put("like", like);
            if(chatCount > -1)json.put("chatCount", chatCount);
            if(uCount > -1)json.put("uCount", uCount);
            json.put("cate", cate);
            json.put("open", open);
            json.put("uc", uc);

            if(!TextUtils.isEmpty(img))json.put("img", img);
            if(!TextUtils.isEmpty(link_url))json.put("link_url", link_url);
            if(!TextUtils.isEmpty(videoId))json.put("videoId", videoId);

            if(listRoomUserData!=null && listRoomUserData.size() > 0)json.put("listRoomUserData", listRoomUserData);
//            if(listRoomReport!=null && listRoomReport.size() > 0)json.put("listRoomReport", listRoomReport);

        }catch (Exception e){}
        return json;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public long getLike() {
        return like;
    }

    public void setLike(long like) {
        this.like = like;
    }

    public long getChatCount() {
        return chatCount;
    }

    public void setChatCount(long chatCount) {
        this.chatCount = chatCount;
    }

    public long getuCount() {
        return uCount;
    }

    public void setuCount(long uCount) {
        this.uCount = uCount;
    }

    public long getCate() {
        return cate;
    }

    public void setCate(long cate) {
        this.cate = cate;
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

    public String getMic() {
        return mic;
    }

    public void setMic(String mic) {
        this.mic = mic;
    }

    public String getIuid() {
        return iuid;
    }

    public void setIuid(String iuid) {
        this.iuid = iuid;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getLink_url() {
        return link_url;
    }

    public void setLink_url(String link_url) {
        this.link_url = link_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getUc() {
        return uc;
    }

    public void setUc(int uc) {
        this.uc = uc;
    }

    public int getRowType() {
        return rowType;
    }

    public void setRowType(int rowType) {
        this.rowType = rowType;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public AdmobNativeData getAdmobNativeData() {
        return admobNativeData;
    }

    public void setAdmobNativeData(AdmobNativeData admobNativeData) {
        this.admobNativeData = admobNativeData;
    }

    public FBnative getFBnativeData() {
        return FBnativeData;
    }

    public void setFBnativeData(FBnative FBnativeData) {
        this.FBnativeData = FBnativeData;
    }

    public ArrayList<RoomUserData> getListRoomUserData() {
        return listRoomUserData;
    }

    public void setListRoomUserData(ArrayList<RoomUserData> listRoomUserData) {
        this.listRoomUserData = listRoomUserData;
    }

//    public ArrayList<ReportData> getListRoomReport() {
//        return listRoomRep ArrayList<ReportData> getListRoomReport() {
//        return listRoomReport;
//    }
//
//    public void setListRoomReport(ArrayList<ReportData> listRoomReport) {
//        this.listRoomReport = listRoomReport;
//    }ort;
//    }
//
//    public void setListRoomReport(ArrayList<ReportData> listRoomReport) {
//        this.listRoomReport = listRoomReport;
//    }
}
