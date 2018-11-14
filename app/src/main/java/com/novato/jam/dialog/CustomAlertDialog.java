package com.novato.jam.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.novato.jam.R;
import com.novato.jam.common.Utils;

/**
 * Created by birdgang on 2016. 4. 19..
 */
public class CustomAlertDialog extends Dialog implements View.OnClickListener {

    static final float DIMENSIONS_DIFF_PORTRAIT = 70;



    private final String TAG = "OptionMenuDialog";

    private Context mContext = null;

    public onCustomAlertDialogItemClickListener mOptionDialogItemClickListener = null;

    String title;
    String desc;
    boolean cancelBtn = true;

    public interface onCustomAlertDialogItemClickListener {
        public void onClickOk();
        public void onClickCancel();
    }


    public CustomAlertDialog(Context context, int title , int desc , onCustomAlertDialogItemClickListener listener) {
        super(context);
        this.mContext = context;
        this.title = context.getString(title);
        this.desc = context.getString(desc);
        this.mOptionDialogItemClickListener = listener;
    }
    public CustomAlertDialog(Context context, String title , String desc , onCustomAlertDialogItemClickListener listener) {
        super(context);
        this.mContext = context;
        this.title = title;
        this.desc = desc;
        this.mOptionDialogItemClickListener = listener;
    }

    public CustomAlertDialog(Context context, boolean cancelBtn, int title , int desc , onCustomAlertDialogItemClickListener listener) {
        super(context);
        this.mContext = context;
        this.title = context.getString(title);
        this.desc = context.getString(desc);
        this.cancelBtn = cancelBtn;
        this.mOptionDialogItemClickListener = listener;
    }

    public CustomAlertDialog(Context context, boolean cancelBtn, String title , String desc , onCustomAlertDialogItemClickListener listener) {
        super(context);
        this.mContext = context;
        this.title = title;
        this.desc = desc;
        this.cancelBtn = cancelBtn;
        this.mOptionDialogItemClickListener = listener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



////        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
////        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        getWindow().setGravity(Gravity.CENTER);
////        getWindow().getAttributes().windowAnimations = R.style.OptionMenuDialogAnimation;
//
//        setCanceledOnTouchOutside(true);
//        setCancelable(true);
//
//        setContentView(R.layout.dialog_custom_alert);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.dialog_custom_alert, null);


        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);

        if(!cancelBtn){
            view.findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        }


        if(TextUtils.isEmpty(title)){
            title = "";
        }
        ((TextView)view.findViewById(R.id.tv_title)).setText(title);


        if(TextUtils.isEmpty(desc)){
            desc = "";
        }
        ((TextView)view.findViewById(R.id.tv_desc)).setText(desc);


        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(mOptionDialogItemClickListener!=null)mOptionDialogItemClickListener.onClickCancel();
            }
        });



        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        float dimensions = DIMENSIONS_DIFF_PORTRAIT;

//		addContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));




        if(Utils.getScreenWidth(mContext) > Utils.getScreenHeight(mContext)){
            addContentView(view, new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        else {

            addContentView(view, new LinearLayout.LayoutParams(
                    display.getWidth() - ((int) (dimensions * scale + 0.5f)),
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }


    }



    @Override
    public void onClick(View v) {
        int itemId = v.getId();
        switch (itemId) {
            case R.id.btn_ok:{
                if(mOptionDialogItemClickListener!=null)mOptionDialogItemClickListener.onClickOk();
                break;
            }
            case R.id.btn_cancel:{
                if(mOptionDialogItemClickListener!=null)mOptionDialogItemClickListener.onClickCancel();
                break;
            }
        }
        dismiss();
    }

}
