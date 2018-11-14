package com.novato.jam.ui;

import android.os.Bundle;
import android.view.View;

import com.novato.jam.R;
import com.novato.jam.common.FragmentAppCompatManager;
import com.novato.jam.ui.fragment.AgreementFragment;
import com.novato.jam.ui.fragment.StarListFragment;

public class AdminStarListActivity extends BaseActivity implements View.OnClickListener{

    FragmentAppCompatManager mFragmentAppCompatManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree);


        mFragmentAppCompatManager = new FragmentAppCompatManager(this, R.id.fragment);

        setFragment();
    }


    @Override
    public void onClick(View v) {

    }

    private void setFragment(){
        mFragmentAppCompatManager.replaceFragment(new StarListFragment());

    }
}


