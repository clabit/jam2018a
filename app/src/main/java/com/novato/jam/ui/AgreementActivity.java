package com.novato.jam.ui;

import android.os.Bundle;
import android.view.View;
import com.novato.jam.R;
import com.novato.jam.common.FragmentAppCompatManager;
import com.novato.jam.ui.fragment.AgreementFragment;

public class AgreementActivity extends BaseActivity implements View.OnClickListener{

    FragmentAppCompatManager mFragmentAppCompatManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree);


        mFragmentAppCompatManager = new FragmentAppCompatManager(this, R.id.fragment);


        setFragment(getIntent().getIntExtra("type", 1));
    }


    @Override
    public void onClick(View v) {

    }

    private void setFragment(int type){
        AgreementFragment mAgreementFragment = new AgreementFragment();
        mAgreementFragment.setType(type);
        mFragmentAppCompatManager.replaceFragment(mAgreementFragment);

    }
}


