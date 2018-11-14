package com.novato.jam.firebase;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.ui.MainActivity;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class Fire {


    static public DatabaseReference getReference(){
        return FirebaseDatabase.getInstance().getReference().child("jam");
    }

    static public void setOffline(){
//        FirebaseDatabase.getInstance().goOffline();
//        FirebaseDatabase.getInstance().getReference().
    }

    static public void setOnline(){
    }


    static public FirebaseStorage getStorage(){
        return FirebaseStorage.getInstance("gs://jam2018-c7297.appspot.com");
    }


    public static long getServerTimestamp()
    {
        long time = System.currentTimeMillis();
        try{
            time = time + MainActivity.mServerTimeOffset;
        }catch (Exception e){}

        return time;
    }
    static public void loadServerTime(){
        loadServerTime(null);
    }
    static public void loadServerTime(final TimeCallback mTimeCallback){
        try {
            DatabaseReference mServerTimeRef = FirebaseDatabase.getInstance().getReference("/.info/serverTimeOffset");

            mServerTimeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long offset = 0;
                    try {
                        offset = dataSnapshot.getValue(Long.class);
                        MainActivity.mServerTimeOffset = offset;
                        LoggerManager.e("servertime", "offset = " + offset);
                    } catch (Exception e) {
                        LoggerManager.e("servertime", "offset = " + e.toString());
                    }

                    if(mTimeCallback!=null)
                        mTimeCallback.timeOffet(offset);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if(mTimeCallback!=null)
                        mTimeCallback.timeOffet(-1);
                }
            });
        }catch (Exception e){}
    }

    public interface TimeCallback{
        void timeOffet(long offset);
    }

    static public String KEY_ADMIN = "admin";
    static public String KEY_USER = "user";
    static public String KEY_CHAT_ROOM = "chat_room";
    static public String KEY_CHAT_COUNT = "chat_count";
    static public String KEY_ROOM_USERLIST = "listRoomUserData";
    static public String KEY_ROOM_USERCOUNT = "listRoomUserCount";
    static public String KEY_MY_ROOM = "my_room";
    static public String KEY_CHAT_USER = "chat_user";
    static public String KEY_CHAT = "chat";
    static public String KEY_NOTICE = "notice";
    static public String KEY_BLOCK_USER = "block_user";
    static public String KEY_BLOCK_VERSION = "block_version";
    static public String KEY_STORY = "story";
    static public String KEY_STORY_COMMENT = "story_c";
    static public String KEY_TAGS = "tags";

    static public String KEY_EVENT = "event";

    static public String KEY_REPORT_ROOM = "listRoomReport";
    static public String KEY_REPORTCount_ROOM = "listRoomReportCount";
    static public String KEY_ROOM_AUDIO = "listRoomAudio";
    static public String KEY_ROOM_IMAGE = "listRoomImage";

    static public int KEY_VALUSE_DESTROY_ROOM = -66;
}
