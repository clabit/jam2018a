package com.novato.jam.push;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.PushData;
import com.novato.jam.data.UserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.firebase.Fire;
import com.novato.jam.ui.MainActivity;
import com.novato.jam.ui.fragment.RoomChatFragment;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    public MyFirebaseMessagingService getContexts(){
        return this;
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ


        String data= "";
        String from = "";
//        PushData mPushData = null;
//
        from = remoteMessage.getFrom();
//
        LoggerManager.e(TAG, "From: " + from);
//
//        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            LoggerManager.e(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            LoggerManager.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification((int)(Fire.getServerTimestamp() % 100000), remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
        // Check if message contains a data payload.
        else if (remoteMessage.getData().size() > 0) {
            LoggerManager.e(TAG, "Message data payload: " + remoteMessage.getData());


            final PushData mPushData = new PushData(remoteMessage);

            try {
                LoggerManager.e(TAG, "title: " + mPushData.getTitle());
                LoggerManager.e(TAG, "msg: " + mPushData.getMsg());
                LoggerManager.e(TAG, "from_uid: " + mPushData.getFrom_uid());
                LoggerManager.e(TAG, "from_push: " + mPushData.getFrom_push());
                LoggerManager.e(TAG, "roomName: " + mPushData.getRoomName());
            }catch (Exception e){}


            try {
                if (!TextUtils.isEmpty(mPushData.getFrom_push())
                        && !TextUtils.isEmpty(mPushData.getFrom_uid())) {
                    try {
                        UserData u = DBManager.createInstnace(getContexts()).getUserPushEDate(mPushData.getFrom_uid());
                        if (u == null || u.getTime() < mPushData.getTime()) {
                            LoggerManager.e(TAG, "db add save:" + mPushData.getFrom_uid());
                            DBManager.createInstnace(getContexts()).addUserPush(mPushData.getFrom_uid(), mPushData.getFrom_push());
                        }
                    } catch (Exception e) {
                        LoggerManager.e(TAG, "db add exception 1:" + e.toString());
                    }
                }
            }catch (Exception e){}

            //채팅 푸시
            if (remoteMessage.getData().containsKey("chatKey")) {
                String key = null;
                try{
                    key = remoteMessage.getData().get("chatKey") + "";
                }catch (Exception e){
                }
                LoggerManager.e(TAG, "chatKey: "+key);


                int join = -1;
                try{
                    join = Integer.parseInt(remoteMessage.getData().get("join")+"");
                }catch (Exception e){
                }


                if(!TextUtils.isEmpty(key)){
                    final String key_f = key;

                    String nnn = mPushData.getRoomName();
                    if(TextUtils.isEmpty(nnn))nnn = ""; else nnn+="\n";
                    final String roomName = nnn;

                    if(join == SendPushFCMReadyOk.TYPE_JOIN){
                        GlobalApplication.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sendNotification(2, getString(R.string.app_name), roomName + getString(R.string.push_join_ok));
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                    else if(join == SendPushFCMReadyOk.TYPE_SIGNUP){
                        GlobalApplication.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sendNotification(3, getString(R.string.app_name), roomName + getString(R.string.push_join_signup));
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                    else {

                        LoggerManager.e(TAG, "badge: gogo");

                        try {
                            DBManager.createInstnace(this).addLauncherBadge(key, 1);
                            long t = DBManager.createInstnace(this).getLauncherBadgeToal();
                            LoggerManager.e(TAG, "badge: "+t);
                            Utils.setBadgeCount(this, t);
                            if(t > 0){

                                if (MyPreferences.getSettingPush(getContexts())) {
                                    GlobalApplication.runOnMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (MainActivity.isLife()) {
                                                    if(TextUtils.isEmpty(MainActivity.mCurrentRoomId) || !key_f.equals(MainActivity.mCurrentRoomId))
                                                    {
                                                        Toast.makeText(getContexts(), roomName + getString(R.string.push_noti), Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    sendNotification(0, getString(R.string.app_name), roomName + getString(R.string.push_noti));
                                                }
                                            } catch (Exception e) {
                                            }
                                        }
                                    });
                                }

                            }
                        }catch (Exception e){
                            LoggerManager.e(TAG, "badge: "+e.toString());
                        }
                    }

                    return;
                }
            }
            else if (remoteMessage.getData().containsKey("story") && mPushData.isStory()) {
                if (MyPreferences.getSettingPush(getContexts())) {

                    String nnn = mPushData.getRoomName();
                    if(TextUtils.isEmpty(nnn))nnn = ""; else nnn+="\n";
                    final String roomName = nnn;

                    if (mPushData.getType() == SendPushFCMStory.STORY_MSG_TYPE_STORY) {
                        GlobalApplication.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sendNotification(4, getString(R.string.app_name), roomName + getString(R.string.push_story));
                                } catch (Exception e) {
                                }
                            }
                        });
                    } else if (mPushData.getType() == SendPushFCMStory.STORY_MSG_TYPE_COMMENT) {
                        GlobalApplication.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sendNotification(5, getString(R.string.app_name), roomName + getString(R.string.push_story_comment));
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                }
            }




            if(mPushData.isPrivate() && Fire.getServerTimestamp() - 30000 < mPushData.getTime()){
                Intent i = new Intent(RoomChatFragment.Action_RoomChatFragmentPrivate);
                i.putExtra("private", mPushData);
                sendBroadcast(i);

                if(!TextUtils.isEmpty(mPushData.getTitle())
                        && !TextUtils.isEmpty(mPushData.getFrom_push())
                        && !TextUtils.isEmpty(mPushData.getFrom_uid())
                        && !TextUtils.isEmpty(mPushData.getRoom())
                        && !TextUtils.isEmpty(mPushData.getRoomName())
                        && !TextUtils.isEmpty(mPushData.getMsg())
                        ){

                    LoggerManager.e(TAG, "db add message");

                    try {
                       // DBManager.createInstnace(getContexts()).updateMessage(mPushData.getRoom(), mPushData.getRoomName(), mPushData.getFrom_uid(), mPushData.getTitle(), mPushData.getMsg());
                        DBManager.createInstnace(getContexts()).addMessage(mPushData.getRoom(), mPushData.getRoomName(), mPushData.getFrom_uid(), mPushData.getTitle(), mPushData.getMsg());//비밀메세지 계속 쌓이게 추가
                    }catch (Exception e){
                        LoggerManager.e(TAG, "db add exception 2:" + e.toString());
                    }
                }
            }


            //보낸사람이 실제 있는 사람인지 확인 하여 유저 정보를 가져옴s
//            Fire.getReference().child(Fire.KEY_USER).child(mPushData.getFrom_uid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if(dataSnapshot!=null){
//
//                        HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);
//                        if(data!=null) {
//                            UserData m = Parser.getUserDataParse(mPushData.getFrom_uid(), data);
//
//
//                            if(m!=null) {
//                                mPushData.setFrom(m);
//                                LoggerManager.e(TAG, "from username : " + m.getUserName() + "");
//
//
//                                if(!TextUtils.isEmpty(MyPreferences.getString(getContexts(), MyPreferences.KEY_push))
//                                        && !TextUtils.isEmpty(MyPreferences.getString(getContexts(), MyPreferences.KEY_uid))
//                                        )
//                                {
//                                    sendNotification(mPushData.getTitle(), mPushData.getMsg());
//                                }
//
//                            }
//                        }
//                    }
//                }
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                }
//            });



//
//
//            try {
//
//                mPushData = new PushData(remoteMessage.getData());
//
//                if(BuildConfig.DEBUG) {
////                    Set<String> keys = map.keySet();
////                    Iterator<String> iter = keys.iterator();
////
////                    while (iter.hasNext()) {
////                        String key = iter.next();
////                        String dd = map.get(key);
////
////                        data += "\n" + key + "=>" + dd;
////                    }
//                }
//            }catch (Exception e){
//                if(BuildConfig.DEBUG) {
//                    Log.d(TAG, "err: " + e.toString());
//                }
//            }
        }
//
//
//
//
//        Intent mIntent = new Intent(MomoBroadcaster.ACTION_MOMO_BROADCAST_PUSH);
//        mIntent.putExtra(MomoBroadcaster.ACTION_MOMO_BROADCAST_PUSH, mPushData);
//        sendBroadcast(mIntent);


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(int id, String title, String messageBody) {
        Intent intent = new Intent(getContexts(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContexts(), 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getContexts())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if(id!=0)notificationBuilder.setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        int id= 0;
//        id = (int)(Fire.getServerTimestamp() % 100000);
        notificationManager.notify(id /* ID of notification */, notificationBuilder.build());
    }


}
