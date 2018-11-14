package com.novato.jam;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

//import com.kakao.auth.ApprovalType;
//import com.kakao.auth.AuthType;
//import com.kakao.auth.IApplicationConfig;
//import com.kakao.auth.ISessionConfig;
//import com.kakao.auth.KakaoAdapter;
//import com.kakao.auth.KakaoSDK;
import com.novato.jam.common.AndroidUtil;
import com.novato.jam.common.CrashHandler;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by poshaly on 2017. 9. 12..
 */

public class GlobalApplication extends MultiDexApplication {
    public static volatile GlobalApplication application = null;
    private static PackageInfo mPackageInfo = null;

    private static CrashHandler mCrashHandler;

    private final int maxThreads = Math.max(AndroidUtil.isJellyBeanMR1OrLater ? Runtime.getRuntime().availableProcessors() : 2, 1);
    public static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            final Thread thread = new Thread(runnable);
            thread.setPriority(Process.THREAD_PRIORITY_DEFAULT+Process.THREAD_PRIORITY_LESS_FAVORABLE);
            return thread;
        }
    };
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(Math.min(2, maxThreads), maxThreads, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), THREAD_FACTORY);
    private Handler mHandler = new Handler(Looper.getMainLooper());



    @Override
    public void onCreate() {
        super.onCreate();
        application = this;


        try {
            mPackageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
        }catch (Exception e){}



//        FirebaseApp.initializeApp(this);
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        KakaoSDK.init(new KakaoSDKAdapter());


        if(mCrashHandler == null){
            mCrashHandler = new CrashHandler();
            Thread.setDefaultUncaughtExceptionHandler(mCrashHandler);
        }



    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }



    public static Context getAppContext() {
        return application;
    }

    public static Resources getAppResources()
    {
        return application.getResources();
    }

    public static String getApplicationVersion() {
        if (mPackageInfo == null) {
            return "0.0.0";
        }
        return mPackageInfo.versionName;//"v" + mPackageInfo.versionName;
    }

    public static int getApplicationVersionCode() {
        if (mPackageInfo == null) {
            return 0;
        }
        return mPackageInfo.versionCode;
    }


    public static void runBackground(Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper())
            runnable.run();
        else
            application.mThreadPool.execute(runnable);
    }

    public static void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper())
            runnable.run();
        else
            application.mHandler.post(runnable);
    }

    public static boolean removeTask(Runnable runnable) {
        return application.mThreadPool.remove(runnable);
    }



    public static File getProfileFile(Context cxt){
        File f = new File(cxt.getExternalFilesDir(null).getAbsolutePath() + "/jam");
        if (!f.exists()) {
            f.mkdirs();
        }

        return f;
    }





//    private static class KakaoSDKAdapter extends KakaoAdapter {
//        /**
//         * Session Config에 대해서는 default값들이 존재한다.
//         * 필요한 상황에서만 override해서 사용하면 됨.
//         * @return Session의 설정값.
//         */
//        @Override
//        public ISessionConfig getSessionConfig() {
//            return new ISessionConfig() {
//                @Override
//                public AuthType[] getAuthTypes() {
//                    return new AuthType[] {AuthType.KAKAO_LOGIN_ALL};
//                }
//
//                @Override
//                public boolean isUsingWebviewTimer() {
//                    return false;
//                }
//
//                @Override
//                public boolean isSecureMode() {
//                    return false;
//                }
//
//                @Override
//                public ApprovalType getApprovalType() {
//                    return ApprovalType.INDIVIDUAL;
//                }
//
//                @Override
//                public boolean isSaveFormData() {
//                    return true;
//                }
//            };
//        }
//
//        @Override
//        public IApplicationConfig getApplicationConfig() {
//            return new IApplicationConfig() {
//                @Override
//                public Context getApplicationContext() {
//                    return GlobalApplication.getAppContext();
//                }
//            };
//        }
//    }

}
