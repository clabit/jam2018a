package com.novato.jam.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.Utils;
import com.novato.jam.customview.MTouchImageCropView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;


public class MakePictureCropActivity extends BaseActivity {
    static final public String PATH = "image-path";

    static final public String MODE_SQUARE = "image-mode-square";


    private Handler mHandler = new Handler();
//    private CropImageFreeSizeView painting_image;
    private String path = "";
    private ProgressDialog mProgressDialog;

    private MTouchImageCropView painting_image;
    private Bitmap curBitmap;

    private boolean isSQUARE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop_freesize);


        Intent i = getIntent();
        mProgressDialog = new ProgressDialog(this);
//        mProgressDialog.setMessage(getString(R.string.progress_loading));

//        painting_image = (CropImageFreeSizeView) findViewById(R.id.painting_image);
//        painting_image.setCropMode(CropImageFreeSizeView.CropMode.CIRCLE);
        painting_image = (MTouchImageCropView) findViewById(R.id.touch_image);
        painting_image.setCropMode(MTouchImageCropView.CropMode.CIRCLE);

        if (i != null) {
            String p = i.getStringExtra(PATH);
            if (p != null && !"".equals(p.trim())) {
                path = p;
            }
            if(i.getBooleanExtra(MODE_SQUARE, false)){
                isSQUARE = true;
                painting_image.setCropMode(MTouchImageCropView.CropMode.SQUARE_1_1);
            }
        }

        if ("".equals(path.trim())) {

            Intent i2 = new Intent();
            i2.putExtra(PATH, "");
            setResult(RESULT_CANCELED, i2);
            setFinish();

            return;
        }


        curBitmap = decodeFile(this, path);
        painting_image.setImageBitmap(curBitmap);

        View crop = findViewById(R.id.btn_crop);
//        ButtonAni.set(crop, null);
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                crop();
            }
        });


        View btn_cancel = findViewById(R.id.btn_back);
//        ButtonAni.set(btn_cancel, null);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i2 = new Intent();
                i2.putExtra(PATH, "");
                setResult(RESULT_CANCELED, i2);
                setFinish();
            }
        });

    }

    private Bitmap decodeFile(Context context, String path) {

        Bitmap b = null;
        File f = new File(path);
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
			fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			fis.close();

            int screen = Utils.getScreenWidth(context);
            int IMAGE_MAX_SIZE = 1024;// maximum dimension limit
            if (screen >= 0 && IMAGE_MAX_SIZE > screen) {
                IMAGE_MAX_SIZE = screen;
            }

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        return b;
    }

    public void crop() {

        if(mProgressDialog!=null)mProgressDialog.show();


        GlobalApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;


                if(painting_image!=null) {
                    bitmap = painting_image.getCroppedImage();//getCroppedBitmap();
                }

                if (bitmap != null) {

                    Log.e("munx","bit:"+bitmap.getWidth() + "|"+bitmap.getHeight());

                    int qu = 100;

                    if(!isSQUARE) {
                        if (bitmap.getWidth() > 400 || bitmap.getHeight() > 400) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false);

                        }
                    }

                    File f = GlobalApplication.getProfileFile(MakePictureCropActivity.this);

                    String filepath = f.getAbsolutePath() + "/" + Calendar.getInstance().getTimeInMillis() + ".png";
//                    if (path.contains(f.getAbsolutePath())) {
//                        filepath = path;
//                    }

                    File file = new File(filepath);
                    final FileOutputStream filestream;
                    try {
                        filestream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, qu, filestream);

                        bitmap.recycle();
                        bitmap = null;
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    Intent i = new Intent();
                    i.putExtra(PATH, file.getAbsolutePath());
                    setResult(RESULT_OK, i);
                } else {
                    Intent i = new Intent();
                    i.putExtra(PATH, "");
                    setResult(RESULT_CANCELED, i);
                }


                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mProgressDialog!=null)mProgressDialog.dismiss();
                        setFinish();
                    }
                });


            }
        });






    }



    synchronized private void setImageRecycle(){
//        if (painting_image != null) {
//            Bitmap m = painting_image.getImageBitmap();
//            if (m != null) {
//                m.recycle();
//            }
//
//            painting_image = null;
//        }

        if(curBitmap!=null){
            curBitmap.recycle();
            curBitmap = null;
        }

    }

    private void setFinish() {
        setImageRecycle();
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setFinish();
    }

    @Override
    protected void onDestroy() {
        setImageRecycle();

        super.onDestroy();
    }
}
