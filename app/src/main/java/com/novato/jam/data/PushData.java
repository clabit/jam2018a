package com.novato.jam.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;
import com.novato.jam.common.LoggerManager;

import java.net.URLDecoder;
import java.util.HashMap;

/**
 * Created by poshaly on 16. 9. 13..
 */
public class PushData extends BaseData {

//    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
//        @Override
//        public PushData createFromParcel(Parcel source) {
//            return new PushData(source);
//        }
//        @Override
//        public PushData[] newArray(int size) {
//            return new PushData[size];
//        }
//    };
//    public PushData(Parcel src){
//        readFromParcel(src);
//    }

    String title = "";//isPrivate 일때는 이름이 들어감
    String msg = "";
    String from_uid = "";
    String from_push = "";
    String room;
    String roomName;
    boolean isPrivate;
    long time;
    UserData from;

    boolean story;
    int type;


    public PushData() {
    }


    public PushData(RemoteMessage remoteMessage){

        try {
            if (remoteMessage.getData().containsKey("title")) {
                try{
                    title = URLDecoder.decode(remoteMessage.getData().get("title") + "","UTF-8");
                }catch (Exception e){
                    title = remoteMessage.getData().get("title") + "";
                }
            }
            if (remoteMessage.getData().containsKey("msg")) {
                try{
                    msg = URLDecoder.decode(remoteMessage.getData().get("msg") + "","UTF-8");
                }catch (Exception e){
                    msg = remoteMessage.getData().get("msg") + "";
                }
            }

            if (remoteMessage.getData().containsKey("from_uid")) {
                try{
                    from_uid = URLDecoder.decode(remoteMessage.getData().get("from_uid") + "","UTF-8");
                }catch (Exception e){
                    from_uid = remoteMessage.getData().get("from_uid") + "";
                }
            }

            if (remoteMessage.getData().containsKey("from_push")) {
                try{
                    from_push = URLDecoder.decode(remoteMessage.getData().get("from_push") + "","UTF-8");
                }catch (Exception e){
                    from_push = remoteMessage.getData().get("from_push") + "";
                }
            }

            if (remoteMessage.getData().containsKey("room")) {
                try{
                    room = URLDecoder.decode(remoteMessage.getData().get("room") + "","UTF-8");
                }catch (Exception e){
                    room = remoteMessage.getData().get("room") + "";
                }
            }

            if (remoteMessage.getData().containsKey("roomName")) {
                try{
                    roomName = URLDecoder.decode(remoteMessage.getData().get("roomName") + "","UTF-8");
                }catch (Exception e){
                    roomName = remoteMessage.getData().get("roomName") + "";
                }
            }


            if (remoteMessage.getData().containsKey("time")) {
                try{
                    time = Long.parseLong(remoteMessage.getData().get("time") + "");
                }catch (Exception e){
                }
            }

            if (remoteMessage.getData().containsKey("isPrivate")) {
                try{
                    isPrivate = Boolean.parseBoolean(remoteMessage.getData().get("isPrivate") + "");
                }catch (Exception e){
                }
            }

            if (remoteMessage.getData().containsKey("story")) {
                try{
                    story = Boolean.parseBoolean(remoteMessage.getData().get("story") + "");
                }catch (Exception e){
                }
            }
            if (remoteMessage.getData().containsKey("type")) {
                try{
                    type = Integer.parseInt(remoteMessage.getData().get("type") + "");
                }catch (Exception e){
                }
            }


        }catch (Exception e){}
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(title);
        dest.writeString(msg);
        dest.writeString(from_uid);
        dest.writeString(from_push);
        dest.writeString(room);
        dest.writeString(roomName);
        dest.writeInt(isPrivate?1:0);
        dest.writeLong(time);
        dest.writeParcelable(this.from, flags);

        dest.writeInt(story?1:0);
        dest.writeInt(type);
    }
    @Override
    public void readFromParcel(Parcel src) {
        super.readFromParcel(src);

        title = src.readString();
        msg = src.readString();
        from_uid = src.readString();
        from_push = src.readString();
        room = src.readString();
        roomName = src.readString();
        isPrivate = src.readInt() == 1;
        time =src.readLong();

        this.from = src.readParcelable(UserData.class.getClassLoader());

        story = src.readInt() == 1;
        type = src.readInt();
    }

    public void setHashMap(HashMap<String, Object> json){
        if(json.get("title")!=null)title = json.get("title") + "";
        if(json.get("msg")!=null)msg = json.get("msg") + "";
        if(json.get("from_uid")!=null)from_uid = json.get("from_uid") + "";
        if(json.get("from_push")!=null)from_push = json.get("from_push") + "";
        if(json.get("room")!=null)room = json.get("room") + "";
        if(json.get("roomName")!=null)roomName = json.get("roomName") + "";
        try {
            if (json.get("isPrivate") != null) isPrivate = Boolean.getBoolean(json.get("isPrivate")+"");
        }catch (Exception e){}
        try {
            if (json.get("time") != null) time = Long.parseLong(json.get("time") + "");
        }catch (Exception e){}
        try {
            if (json.get("from") != null) from = (UserData) json.get("from");
        }catch (Exception e){}

        try {
            if (json.get("story") != null) story = Boolean.getBoolean(json.get("story")+"");
        }catch (Exception e){}
        try {
            if (json.get("type") != null) type = Integer.parseInt(json.get("type") + "");
        }catch (Exception e){}
    }


    public HashMap<String, Object> getHashMap(){
        HashMap<String, Object> json = new HashMap<String, Object>();

        try {
            json.put("title", title);
            json.put("msg", msg);
            json.put("from_uid", from_uid);
            json.put("from_push", from_push);
            json.put("room", room);
            json.put("roomName", roomName);
            json.put("isPrivate", isPrivate);
            json.put("time", time);
            json.put("from", from);

            json.put("story", story);
            json.put("type", type);

        }catch (Exception e){}
        return json;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFrom_uid() {
        return from_uid;
    }

    public void setFrom_uid(String from_uid) {
        this.from_uid = from_uid;
    }

    public String getFrom_push() {
        return from_push;
    }

    public void setFrom_push(String from_push) {
        this.from_push = from_push;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public UserData getFrom() {
        return from;
    }

    public void setFrom(UserData from) {
        this.from = from;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isStory() {
        return story;
    }

    public void setStory(boolean story) {
        this.story = story;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
