package com.novato.jam.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.Toast;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;

/**
 * Created by poshaly on 2018. 2. 22..
 */

public class BottomAudioDialog {

    Context context;
    private MediaPlayer mediaPlayer;
    String url;
    BottomSheetDialog mBottomSheetDialog;
    View mRoot;

    Callback mCallback;

    public BottomAudioDialog(Context context,  String url, Callback mCallback){
        this.context = context;
        this.mCallback = mCallback;
        this.url = url;

        setUi();
    }

    public void show(){



        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(context, Uri.parse(url));
        }catch (Exception e){}

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                LoggerManager.e("mun", "onError");

                mRoot.findViewById(R.id.btn_progress).setVisibility(View.GONE);
                mRoot.findViewById(R.id.btn_mic).setVisibility(View.VISIBLE);

                CustomToast.showToast(context, R.string.audio_err, Toast.LENGTH_SHORT);

                dismiss();


                return false;
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                LoggerManager.e("mun", "onPrepared");

                mRoot.findViewById(R.id.btn_progress).setVisibility(View.GONE);
                mRoot.findViewById(R.id.btn_mic).setVisibility(View.VISIBLE);

                if(mediaPlayer!=null)mediaPlayer.start();

                if(mCallback!=null)
                    mCallback.onPrepared();

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                LoggerManager.e("mun", "onCompletion");

                dismiss();
            }
        });
        mediaPlayer.prepareAsync();



        if(mBottomSheetDialog!=null)mBottomSheetDialog.show();

        ((GLAudioVisualizationView)mRoot.findViewById(R.id.layout_wave)).onResume();

    }
    public void dismiss(){
        if(mBottomSheetDialog!=null)mBottomSheetDialog.dismiss();
    }

    private void setUi(){

        mRoot = View.inflate(context, R.layout.dialog_audio_alert, null);

        mBottomSheetDialog = new BottomSheetDialog(context);
        mBottomSheetDialog.setContentView(mRoot);

//        mBottomSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//
//            }
//        });
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                LoggerManager.e("mun", "onDismiss");

                ((GLAudioVisualizationView)mRoot.findViewById(R.id.layout_wave)).release();

                stop();

                if(mCallback!=null)
                    mCallback.onDismiss();
            }
        });



        mRoot.findViewById(R.id.btn_progress).setVisibility(View.VISIBLE);
        mRoot.findViewById(R.id.btn_mic).setVisibility(View.GONE);

        mediaPlayer = new MediaPlayer();

        ((GLAudioVisualizationView)mRoot.findViewById(R.id.layout_wave)).linkTo(DbmHandler.Factory.newVisualizerHandler(context, mediaPlayer));


//        GLAudioVisualizationView visualizerView = new GLAudioVisualizationView.Builder(context)
//                .setLayersCount(1)
//                .setWavesCount(6)
//                .setWavesHeight(cafe.adriel.androidaudiorecorder.R.dimen.aar_wave_height)
//                .setWavesFooterHeight(cafe.adriel.androidaudiorecorder.R.dimen.aar_footer_height)
//                .setBubblesPerLayer(20)
//                .setBubblesSize(cafe.adriel.androidaudiorecorder.R.dimen.aar_bubble_size)
//                .setBubblesRandomizeSize(true)
////                .setBackgroundColor(Util.getDarkerColor(color))
////                .setLayerColors(new int[]{color})
//                .build();
//
//        ((FrameLayout)mRoot.findViewById(R.id.layout_wave)).addView(visualizerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }


    private void stop(){
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        }catch (Exception e){}
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        }catch (Exception e){}
        mediaPlayer = null;
    }



    public interface Callback{
        public void onPrepared();
        public void onError();
        public void onDismiss();
    }
}
