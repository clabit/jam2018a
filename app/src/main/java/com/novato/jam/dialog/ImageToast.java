package com.novato.jam.dialog;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;

/**
 * Created by poshaly on 2018. 2. 23..
 */

public class ImageToast {
    private static Toast toast = null;
    static {
        toast = new Toast(GlobalApplication.getAppContext());
        View view = View.inflate(GlobalApplication.getAppContext(), R.layout.custom_toast_image_layout, null);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM, 0, 400);
        toast.show();
    }



    public static void showToast(Context ctx, String text, int path, int duration) {
        try {
            if (null == toast) {
                return;
            }

            View view = toast.getView();
            ImageView img = (ImageView) view.findViewById(R.id.img_toast);

//            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(img);
            Glide.with(ctx)
                .load(path)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .asGif()
//                .into(imageViewTarget);
                    .into(img);


            TextView text_toast = (TextView) view.findViewById(R.id.text_toast);
            text_toast.setText(text);


            toast.setDuration(duration);
            toast.show();
        } catch (Exception e) {
        }
    }

}
