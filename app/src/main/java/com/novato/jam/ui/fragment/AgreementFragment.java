package com.novato.jam.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.ui.IntroActivity;
import com.novato.jam.ui.MainActivity;

/**
 * Created by poshaly on 2018. 2. 6..
 */

public class AgreementFragment extends android.support.v4.app.Fragment implements View.OnClickListener{

    private View mRootView;

    private TextView tv_action_title, tv_text;

    private int type = 1;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_agree, container, false);


        mRootView.findViewById(R.id.layout_close).setOnClickListener(this);

        tv_action_title = mRootView.findViewById(R.id.tv_action_title);
        tv_text = mRootView.findViewById(R.id.tv_text);


        if(getType() == 1){
            tv_action_title.setText(R.string.setting_agree01);
            tv_text.setText(R.string.text_agreement01);
        }
        else if(getType() == 2){
            tv_action_title.setText(R.string.setting_agree02);
            tv_text.setText(R.string.text_agreement02);
        }
        else if(getType() == 3){
            tv_action_title.setText(R.string.setting_agree03);
            tv_text.setText(R.string.text_agreement03);
        }
        else if(getType() == 4){
            tv_action_title.setText(R.string.setting_agree04);
            tv_text.setText(R.string.text_agreement04);
        }

        return mRootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layout_close:{
                getActivity().finish();
                break;
            }
        }
    }


}
