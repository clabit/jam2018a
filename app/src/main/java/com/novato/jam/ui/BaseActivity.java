package com.novato.jam.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;

//import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity {

    final String BaseActivityfinishAction = "app.finish.action";
    final String BaseActivityAllfinishAction = "app.allfinish.action";

    protected Toolbar mToolbar;
    protected ActionBar actionBar;
    protected String mActivityKey = "";


    public Activity getActivity(){
        return this;
    }


//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();


        if(TextUtils.isEmpty(mActivityKey))mActivityKey = getTaskId() + "_"+System.currentTimeMillis();
        IntentFilter intent_filter = new IntentFilter();
        intent_filter.addAction(BaseActivityfinishAction);
        intent_filter.addAction(BaseActivityAllfinishAction);
        registerReceiver(finishBroadcastReceiver, intent_filter);

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(finishBroadcastReceiver);
        }catch (Exception e){}
    }

    protected ActionBar initActionBar(Toolbar toolbar) {
        if (toolbar == null)
            return null;

        mToolbar = toolbar;
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        return actionBar;
    }

    public void setToolbarExpandHideShow(boolean isShow){
        AppBarLayout appBarLayout = (AppBarLayout)findViewById(R.id.appbar);
        if(isShow){
            appBarLayout.setExpanded(true, true);
        }
        else{
            appBarLayout.setExpanded(false, true);
        }
    }

    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }


    Typeface fontNanum;
    public void setSuctomFont(TextView textView){
        if(fontNanum == null)fontNanum = Typeface.createFromAsset(this.getAssets(), "fonts/NotoSerifCJKkr_Regular.otf");
        textView.setTypeface(fontNanum);
    }
    public void setSuctomFont(EditText textView){
        if(fontNanum == null)fontNanum = Typeface.createFromAsset(this.getAssets(), "fonts/NotoSerifCJKkr_Regular.otf");
        textView.setTypeface(fontNanum);
    }




    protected void sendFinishBroadcast(){
        Intent i = new Intent(BaseActivityfinishAction);
        i.putExtra("uid", mActivityKey);
        i.putExtra("className", getClass().getName());
        sendBroadcast(i);
    }

    BroadcastReceiver finishBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            if(arg1.getAction().equals(BaseActivityfinishAction)){
                String uid = "";
                String className = "";
                try{
                    uid = arg1.getStringExtra("uid");
                }catch (Exception e){}
                try{
                    className = arg1.getStringExtra("className");
                }catch (Exception e){}

                try {
                    LoggerManager.e("mun", "=---------=");
                    LoggerManager.e("mun", uid + " , " + className);
                    LoggerManager.e("mun", mActivityKey + " , " + getActivity().getClass().getName());
                }catch (Exception e){}

                try {
                    if (!TextUtils.isEmpty(className) && !TextUtils.isEmpty(uid)) {
                        if (className.equals(getActivity().getClass().getName()) && !mActivityKey.equals(uid)) {
                            finish();
                        }
                    }
                }catch (Exception e){}
            }
            else if(arg1.getAction().equals(BaseActivityAllfinishAction)){
                finish();
            }



        }
    };


}


