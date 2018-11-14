package com.novato.jam.dialog;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.novato.jam.GlobalApplication;
import com.novato.jam.R;

/**
 * Created by poshaly on 2018. 2. 23..
 */

public class CustomToast {
    private static Toast toast = null;
    static {
        toast = new Toast(GlobalApplication.getAppContext());
        View view = View.inflate(GlobalApplication.getAppContext(), R.layout.custom_toast_layout, null);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.show();
    }



    public static void showToast(Context ctx, String message, int duration) {
        try {
            if (null == toast) {
                return;
            }

            View view = toast.getView();
            TextView textView = (TextView) view.findViewById(R.id.text_toast);
            textView.setText(message);
            toast.setDuration(duration);
            toast.show();
        } catch (Exception e) {
        }
    }


    public static void showToast(Context ctx, @StringRes int message, int duration) {
        try {
            if (null == toast) {
                return;
            }

            View view = toast.getView();
            TextView textView = (TextView) view.findViewById(R.id.text_toast);
            textView.setText(message);
            toast.setDuration(duration);
            toast.show();
        } catch (Exception e) {
        }
    }

}
