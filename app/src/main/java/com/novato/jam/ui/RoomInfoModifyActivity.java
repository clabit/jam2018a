package com.novato.jam.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.novato.jam.R;
import com.novato.jam.common.FragmentAppCompatManager;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.data.FeedData;
import com.novato.jam.firebase.Fire;
import com.novato.jam.ui.fragment.RoomInfoModifyFragment;

import java.util.ArrayList;

public class RoomInfoModifyActivity extends BaseActivity {

    FragmentAppCompatManager mFragmentAppCompatManager;
    FeedData mFeedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_comment);


        mFeedData = getIntent().getParcelableExtra("data");


        mFragmentAppCompatManager = new FragmentAppCompatManager(this, R.id.fragment);

        {
            Fire.loadServerTime();

            RoomInfoModifyFragment d = new RoomInfoModifyFragment();
            d.setFeedData(mFeedData);
            mFragmentAppCompatManager.replaceFragment(d, false);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LoggerManager.e("mun", "onRequestPermissionsResult : " + requestCode);

        try {
            if (mFragmentAppCompatManager != null) {
                ArrayList<Fragment> list = mFragmentAppCompatManager.childFragments();
                if (list != null && list.size() > 0) {
                    LoggerManager.e("mun", "onRequestPermissionsResult : " + list.get(list.size() - 1).getClass().getName());
                    list.get(list.size() - 1).onRequestPermissionsResult(requestCode,permissions, grantResults);
                }
            }
        }catch (Exception e){}
    }
}


