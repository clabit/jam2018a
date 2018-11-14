package com.novato.jam.ui;

import android.os.Bundle;
import android.text.TextUtils;

import com.novato.jam.R;
import com.novato.jam.common.FragmentAppCompatManager;
import com.novato.jam.data.FeedData;
import com.novato.jam.ui.fragment.SearchFeedFragment;
import com.novato.jam.ui.fragment.StoryCommentListFragment;

public class StoryCommentActivity extends BaseActivity {

    FragmentAppCompatManager mFragmentAppCompatManager;
    FeedData item;
    FeedData data;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_comment);

        mFragmentAppCompatManager = new FragmentAppCompatManager(this, R.id.fragment);


        StoryCommentListFragment mStoryListFragment = new StoryCommentListFragment();



        try {
            data = getIntent().getParcelableExtra("data");
            item = getIntent().getParcelableExtra("item");
            position = getIntent().getIntExtra("position", -1);
            if (data != null) {
                mStoryListFragment.setFeedData(data);
            }

            if (item != null) {
                mStoryListFragment.setChild(data.getKey() , item.getKey(), item, position);
            }

        }catch (Exception e){}

        mFragmentAppCompatManager.replaceFragmentTopAnimation(mStoryListFragment);

    }
}


