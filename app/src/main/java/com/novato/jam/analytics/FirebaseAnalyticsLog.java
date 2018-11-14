package com.novato.jam.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by poshaly on 2017. 11. 2..
 */

public class FirebaseAnalyticsLog {

    static public void setStartActivity(Activity activity){

        try {
            FirebaseAnalytics.getInstance(activity).setCurrentScreen(activity, activity.getClass().getSimpleName(), "onCreate");
        }catch (Exception e){}
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "startActivity");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "startActivity");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, activity.getClass().getSimpleName());
//            FirebaseAnalytics.getInstance(activity).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            FirebaseAnalytics.getInstance(activity).logEvent("startActivity", bundle);
        }catch (Exception e){}
    }

    static public void setScreen(Context context, String screenName){
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "activitys");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "activitys");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, screenName);
//            FirebaseAnalytics.getInstance(activity).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            FirebaseAnalytics.getInstance(context).logEvent("screen", bundle);
        }catch (Exception e){}
    }


    static public void setChatSend(Context context, String type){
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "chatsend");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "chatsend");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
//            FirebaseAnalytics.getInstance(activity).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            FirebaseAnalytics.getInstance(context).logEvent("chat", bundle);
        }catch (Exception e){}
    }

}
