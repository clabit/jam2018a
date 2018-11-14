package com.novato.jam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by leesangin on 2015. 11. 4..
 */
public class ExternalExecuteActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(BaseActivityAllfinishAction);
        sendBroadcast(i);

        startMainActivity();
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setData(getIntent().getData());
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainIntent);
    }
}
