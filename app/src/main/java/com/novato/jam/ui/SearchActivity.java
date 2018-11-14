package com.novato.jam.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.novato.jam.R;
import com.novato.jam.common.FragmentAppCompatManager;
import com.novato.jam.data.FeedData;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.ui.fragment.BaseRoomFragment;
import com.novato.jam.ui.fragment.RoomChatFragment;
import com.novato.jam.ui.fragment.RoomInfoFragment;
import com.novato.jam.ui.fragment.RoomJoinFragment;
import com.novato.jam.ui.fragment.SearchFeedFragment;

import java.util.HashMap;

public class SearchActivity extends BaseActivity {

    FragmentAppCompatManager mFragmentAppCompatManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_comment);

        sendFinishBroadcast();

        mFragmentAppCompatManager = new FragmentAppCompatManager(this, R.id.fragment);
        SearchFeedFragment mSearchFeedFragment = new SearchFeedFragment();

        try {
            String search = getIntent().getStringExtra("data");
            if (!TextUtils.isEmpty(search)) {
                mSearchFeedFragment.setSearch(search);
            }
        }catch (Exception e){}

        mFragmentAppCompatManager.replaceFragment(mSearchFeedFragment, false);

    }
}


