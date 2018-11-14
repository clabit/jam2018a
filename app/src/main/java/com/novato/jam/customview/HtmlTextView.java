package com.novato.jam.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.ui.adapter.ChatListReAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by poshaly on 2018. 3. 2..
 */

@SuppressLint("AppCompatCustomView")
public class HtmlTextView extends TextView implements Html.ImageGetter{


    private final List<GlideDrawable> gifs = new ArrayList<>();
    private final Set<ImageGetterViewTarget> mTargets = new HashSet<>();



    public HtmlTextView(Context context) {
        super(context, null);
    }

    public HtmlTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public HtmlTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void clear() {
//        setText(null);
        stopGifs(gifs);
        clearTarget(mTargets);
//        setTag(null);
    }

    private static void stopGifs(List<GlideDrawable> gifs) {
        if (null == gifs) return;
        for (GlideDrawable glideDrawable : gifs) {
            if (null != glideDrawable) {
                glideDrawable.stop();
                if (null != glideDrawable) glideDrawable.setCallback(null);
            }
        }
        gifs.clear();
    }


    public static void clearGifInsideView(HtmlTextView currentGlideImage) {
        currentGlideImage.setText(null);
        if (currentGlideImage == null) return;

        stopGifs(currentGlideImage.gifs);
        clearTarget(currentGlideImage.mTargets);
        currentGlideImage.setTag(null);
    }
    private static void clearTarget(Set<ImageGetterViewTarget> mTargets) {
        if (null == mTargets) return;

        for (ImageGetterViewTarget target : mTargets) {
            Glide.clear(target);
        }
    }



    public void setHtmlText(String source){
        Spanned spanned = Html.fromHtml(source, this, null);
        setText(spanned);
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        invalidate();
    }

    @Override
    public Drawable getDrawable(String source) {
        LoggerManager.e("mun", "html text src = " + source);


//        final LevelListDrawable dd = new LevelListDrawable();
//        Drawable empty = GlobalApplication.getAppContext().getResources().getDrawable(R.mipmap.ic_launcher_round);
//        dd.addLevel(0, 0, empty);
//        dd.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
//
//        Glide.with(GlobalApplication.getAppContext())
//                .load(source)
//                .asBitmap()
////                .override(600,200)
////                .thumbnail(0.1f)
//                .skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(new SimpleTarget<Bitmap>(150,150) {
//                    @Override
//                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
//                        if (bitmap != null) {
//                            BitmapDrawable d = new BitmapDrawable(bitmap);
//                            dd.addLevel(1, 1, d);
//                            dd.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
//                            dd.setLevel(1);
//                            // i don't know yet a better way to refresh TextView
//                            // mTv.invalidate() doesn't work as expected
//                            CharSequence t = getText();
//                            setText(t);
//                        }
//                    }
//                });

//        new LoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, source, dd);




        UrlDrawable dd = null;

        try {
            dd = new UrlDrawable();

            ImageGetterViewTarget mImageGetterViewTarget = new ImageGetterViewTarget(this, dd);

            Glide.with(GlobalApplication.getAppContext())
                    .load(source)
                .skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mImageGetterViewTarget);


            mTargets.add(mImageGetterViewTarget);
        }catch (Exception e){}

        return dd;
    }

//    class LoadImage extends AsyncTask<Object, Void, Bitmap> {
//
//        private LevelListDrawable mDrawable;
//
//        @Override
//        protected Bitmap doInBackground(Object... params) {
//            String source = (String) params[0];
//            mDrawable = (LevelListDrawable) params[1];
//            try {
//                InputStream is = new URL(source).openStream();
//                return BitmapFactory.decodeStream(is);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            if (bitmap != null) {
//                BitmapDrawable d = new BitmapDrawable(bitmap);
//                mDrawable.addLevel(1, 1, d);
//                mDrawable.setBounds(0, 0, 100, 100);
//                mDrawable.setLevel(1);
//                // i don't know yet a better way to refresh TextView
//                // mTv.invalidate() doesn't work as expected
//                CharSequence t = getText();
//                setText(t);
//            }
//        }
//    }


    private class ImageGetterViewTarget extends ViewTarget<TextView, GlideDrawable> {

        private final UrlDrawable mDrawable;

        private ImageGetterViewTarget(TextView view, UrlDrawable drawable) {
            super(view);
            this.mDrawable = drawable;
        }

        @Override
        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
            try {
                Rect rect;
//                if (resource.getIntrinsicWidth() > 100) {
//                    float width;
//                    float height;
//                    System.out.println("Image width is " + resource.getIntrinsicWidth());
//                    System.out.println("View width is " + view.getWidth());
//                    if (resource.getIntrinsicWidth() >= getView().getWidth()) {
//                        float downScale = (float) resource.getIntrinsicWidth() / getView().getWidth();
//                        width = (float) resource.getIntrinsicWidth() / (float) downScale;
//                        height = (float) resource.getIntrinsicHeight() / (float) downScale;
//                    } else {
//                        float multiplier = (float) getView().getWidth() / resource.getIntrinsicWidth();
//                        width = (float) resource.getIntrinsicWidth() * (float) multiplier;
//                        height = (float) resource.getIntrinsicHeight() * (float) multiplier;
//                    }
//                    System.out.println("New Image width is " + width);
//
//
//                    rect = new Rect(0, 0, Math.round(width), Math.round(height));
//                } else {
//                    rect = new Rect(0, 0, resource.getIntrinsicWidth() * 2, resource.getIntrinsicHeight() * 2);
//                }

                float downScale = (float) Utils.getScreenWidth(GlobalApplication.getAppContext())/3 / resource.getIntrinsicWidth();
                float width = (float) resource.getIntrinsicWidth() * (float) downScale;
                float height = (float) resource.getIntrinsicHeight() * (float) downScale;
                rect = new Rect(0, 0, Math.round(width), Math.round(height));

                resource.setBounds(rect);

                mDrawable.setBounds(rect);
                mDrawable.setDrawable(resource);


                gifs.add(resource);

                if (resource.isAnimated()) {
                    mDrawable.setCallback(getView());
                    resource.setLoopCount(GlideDrawable.LOOP_FOREVER);
                    resource.start();
                }

                getView().setText(getView().getText());
                getView().invalidate();
            }catch (Exception e){}
        }

        private Request request;
        @Override
        public Request getRequest() {
            return request;
        }

        @Override
        public void setRequest(Request request) {
            this.request = request;
        }
    }


    final class UrlDrawable extends Drawable implements Drawable.Callback {

        private GlideDrawable mDrawable;

        @Override
        public void draw(Canvas canvas) {
            if (mDrawable != null) {
                mDrawable.draw(canvas);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            if (mDrawable != null) {
                mDrawable.setAlpha(alpha);
            }
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            if (mDrawable != null) {
                mDrawable.setColorFilter(cf);
            }
        }

        @Override
        public int getOpacity() {
            if (mDrawable != null) {
                return mDrawable.getOpacity();
            }
            return PixelFormat.UNKNOWN;
        }

        public void setDrawable(GlideDrawable drawable) {
            if (this.mDrawable != null) {
                this.mDrawable.setCallback(null);
            }
            drawable.setCallback(this);
            this.mDrawable = drawable;
        }

        @Override
        public void invalidateDrawable(Drawable who) {
            if (getCallback() != null) {
                getCallback().invalidateDrawable(who);
            }
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            if (getCallback() != null) {
                getCallback().scheduleDrawable(who, what, when);
            }
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            if (getCallback() != null) {
                getCallback().unscheduleDrawable(who, what);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        try {
            clear();
        }catch (Exception e){}

    }

    @Override
    public void destroyDrawingCache() {
        super.destroyDrawingCache();

        try {
            clear();
        }catch (Exception e){}
    }
}
