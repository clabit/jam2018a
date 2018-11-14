package com.novato.jam.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
//import android.databinding.repacked.apache.commons.io.FileUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.CommerceDetailObject;
import com.kakao.message.template.CommerceTemplate;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.SocialObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.data.FeedData;
import com.novato.jam.firebase.Fire;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by poshaly on 2017. 9. 12..
 */

public class Utils {

    static public int getPixSize(Context context, float dp){
//        return ((int) (dp * context.getResources().getDisplayMetrics().density) / 2);
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int)px;
    }

    public static int getScreenWidth(Context con)
    {
        return con.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context con)
    {
        return con.getResources().getDisplayMetrics().heightPixels;
    }

    public static float convertPixelsToDp(Context context, float px){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }



//    static public String getFileSize(File file) {
//        try {
//            if (file.isFile()) {
//                return FileUtils.byteCountToDisplaySize(file.length());
//            }
//        } catch (Exception e) {
//        }
//        return "";
//    }


    public static boolean isStringEmpty(String text) {
        if (text != null && text.trim().length() > 0)
            return false;
        return true;
    }


    public static boolean isExternalLcation(String location) {
        try {
            String externalPath = getExtSdcardPath();
            if (externalPath == null || externalPath.isEmpty() || location == null || location.isEmpty()) {
                return false;
            }

            if(location.startsWith(externalPath)){
               return true;
            }

//            location = location.substring("file://".length(), "file://".length()+externalPath.length());
//            location = location.substring(0, externalPath.length());
//            if (externalPath.equals(location)) {
//                return true;
//            }
        } catch (Exception e) {
        }

        return false;
    }
    private static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";

        try {
            final Process process = new ProcessBuilder().command("mount").redirectErrorStream(true).start();
            process.waitFor();

            final InputStream is = process.getInputStream();

            final byte[] buffer = new byte[1024];

            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final String[] lines = s.split("\n");

        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")){
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold")) {
                                out.add(part);
                            }
                    }
                }
            }
        }
        return out;
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getExtSdcardPath2(){
        String extSdcard = "";
        try {
            File[] externalFiles = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                externalFiles = GlobalApplication.getAppContext().getExternalFilesDirs(null);
                if (externalFiles.length > 1 && externalFiles[1] != null) {
                    extSdcard = externalFiles[1].getAbsolutePath();

                    if (!isStringEmpty(extSdcard) && extSdcard.contains("/Android")) {
                        int indexed = extSdcard.indexOf("Android");
                        if (indexed > -1) {
                            extSdcard = extSdcard.substring(0, indexed);
                        }
                    }
                }
            } else {
                HashSet<String> mounts = getExternalMounts();
                for (String mount : mounts) {
                    if (mount.contains("extSdCard") || mount.contains("external_SD") || mount.contains("USBstorage")) {
                        extSdcard = mount;
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        return extSdcard;
    }
    private static String getExtSdcardPath () {
        String extSdcardPath = null;
        if (extSdcardPath == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                extSdcardPath = getExtSdcardPath2();
            } else {
                extSdcardPath = getSdcardPath();
            }
        }
        return extSdcardPath;
    }
    private static String getSdcardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }


    public static String getLanguageStringCode() {
        String s = Locale.getDefault().getLanguage();


        if(s.equals("ja")//일본
                || s.equals("ko")//한국
                || s.equals("es")//스페인
                || s.equals("zh")//중국
                || s.equals("ru")//러시아
                || s.equals("en")//미국
                || s.equals("hi")//india
                || s.equals("bn")//Bangladesh
                || s.equals("si")//Sri Lanka
                || s.equals("fa")//이란어(페르시아어
                || s.equals("ar")//아랍어
                || s.equals("bg")//불가리아어

                ){

            try {
                String x = Locale.getDefault().getCountry();
                if ("en".equals(s) && "IN".equals(x)) {
                    return "hi";
                }
            }catch (Exception e){
            }
            if(s.equals("bn") || s.equals("si")){
                return "hi";
            }

            if(s.equals("ja")){
                return "ko";
            }

            if(s.equals("ar")){
                return "fa";
            }

            if(s.equals("bg")){
                return "ru";
            }

            if(s.equals("es")){
                return "en";
            }




            return s;
        }

        return "en";
    }


    /**파일 확장자 가져오기
     * @param fileStr 경로나 파일이름
     * @return*/
    public static String getExtension(String fileStr){
        if(fileStr == null || fileStr.indexOf(".") == -1)
            return null;
        String fileExtension = fileStr.substring(fileStr.lastIndexOf(".")+1,fileStr.length());
        return TextUtils.isEmpty(fileExtension) ? null : fileExtension;
    }

    /**파일 이름 가져오기
     * @param fileStr 파일 경로
     * @param isExtension 확장자 포함 여부
     * @return */
    public static String getFileName(String fileStr , boolean isExtension){
        String fileName = null;
        if(isExtension)
        {
            if(fileStr == null || fileStr.indexOf(".") == -1){
                return null;
            }
            fileName = fileStr.substring(0,fileStr.lastIndexOf("."));
        }else{
            fileName = fileStr;
        }
        return fileName;
    }



    static public String getDateTimeString(long time) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = df.format(new Date(time));
        return currentDate;
    }


    static public String getDateTimeString2(long time) {
        SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd");
        String today = to.format(new Date(Fire.getServerTimestamp()));

        String d = to.format(new Date(time));


        String currentDate="";

        if(today.equals(d)){
            SimpleDateFormat df = new SimpleDateFormat("aa hh:mm");
            currentDate = df.format(new Date(time));
        }
        else{
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd aa hh:mm");
            currentDate = df.format(new Date(time));
        }

        return currentDate;
    }




    static private ColorDrawable[] vibrantLightColorList =
            {
                    new ColorDrawable(Color.parseColor("#9ACCCD")), new ColorDrawable(Color.parseColor("#8FD8A0")),
                    new ColorDrawable(Color.parseColor("#CBD890")), new ColorDrawable(Color.parseColor("#DACC8F")),
                    new ColorDrawable(Color.parseColor("#D9A790")), new ColorDrawable(Color.parseColor("#D18FD9")),
                    new ColorDrawable(Color.parseColor("#FF6772")), new ColorDrawable(Color.parseColor("#52B1D9")),
                    new ColorDrawable(Color.parseColor("#FFD15C")), new ColorDrawable(Color.parseColor("#59585D"))
            };
    static public ColorDrawable getRandomDrawbleColor() {
        int idx = new Random().nextInt(vibrantLightColorList.length);
        return vibrantLightColorList[idx];
    }


    static private String[] vibrantLightColorList2 =
            {       "9ACCCD","8FD8A0",
                    "CBD890","DACC8F",
                    "D9A790","D18FD9",
                    "FF6772","52B1D9",
                    "FFD15C","59585D"
            };
    static public String getRandomeColor() {
        int idx = new Random().nextInt(vibrantLightColorList2.length);
        return vibrantLightColorList2[idx];
    }




    public static void setBadgeCount(Context context, long count){

        boolean success = ShortcutBadger.applyCount(context, (int)count);


//        String launcherClassName = getLauncherClassName(context);
//        if (launcherClassName == null) {
//            return;
//        }
//
////        LoggerManager.e("setBadgeCount", context.getPackageName() + " / " +launcherClassName);
//
//        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
//        intent.putExtra("badge_count_package_name", context.getPackageName());
//        intent.putExtra("badge_count_class_name", launcherClassName);
//        intent.putExtra("badge_count", (int)count);
//        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }


    static public String isBlockWord(final String message, String[]blockWords){

        try {
            String text = message;

            try {
                //이모티콘제거
                Pattern emoticons = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+");
                Matcher emoticonsMatcher = emoticons.matcher(text);
                text = emoticonsMatcher.replaceAll("");
            }catch (Exception e){}

            text = text.replaceAll(" ", "");
            text = text.replaceAll("　", "");

            String regPice = "[0123456789 \\?+-\\.,!@#$%\\^&\\*\\(\\);\\\\\\/\\|<>\\\"\\']*";
            String subsPice = "\\?";
            text = text.replaceAll(subsPice, "");
            text = text.replaceAll(regPice, "");


            for(String ss : blockWords){
                Pattern p = Pattern.compile(ss, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(text);
                while (m.find())
                {
                    return ss;
                }
            }


        }catch (Exception e){}

        return null;
    }
    private static String maskWord(String str)
    {

        StringBuffer buf = new StringBuffer();
        char[] ch = str.toCharArray();
        for (int i = 0; i < ch.length; i++)

        {

            if (i < 1)

            {

                buf.append(ch[i]);

            } else {

                buf.append("*");

            }

        }
        return buf.toString();

    }





    public static Bitmap addWaterMark(Bitmap src, Context context, boolean recycle) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Drawable drawable = context.getResources().getDrawable(R.mipmap.ic_launcher_round);
        float scale = 1;

        int CAPTURE_IMAGE_MAX_SIZE = 1024;
//        int screen = Utils.getScreenWidth(context);
//        if(CAPTURE_IMAGE_MAX_SIZE > screen){
//            CAPTURE_IMAGE_MAX_SIZE = screen;
//        }

        try {
            if (src.getWidth() > src.getHeight()) {
                if (src.getWidth() < CAPTURE_IMAGE_MAX_SIZE) {
                    scale = (float)CAPTURE_IMAGE_MAX_SIZE / (float)src.getWidth();
                }
            }
            else {
                if (src.getHeight() < CAPTURE_IMAGE_MAX_SIZE)
                    scale = (float)CAPTURE_IMAGE_MAX_SIZE / (float)src.getHeight();
            }
        }catch(Exception e){
        }

        int padding = 15;
        if(result.getWidth() / 20 < 15){
            padding = result.getWidth() / 20;
        }
        float textSize = 34.0f / scale;

        Bitmap water = Bitmap.createBitmap((int)(drawable.getIntrinsicWidth() / scale), (int)(drawable.getIntrinsicHeight() / scale), Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(water);
        drawable.setBounds(0, 0, canvas2.getWidth(), canvas2.getHeight());
        drawable.draw(canvas2);
        canvas.drawBitmap(water, padding, result.getHeight() - water.getHeight()-padding - textSize, null);


        try {
            Paint shadowPaint = new Paint();
            shadowPaint.setAntiAlias(true);
            shadowPaint.setColor(Color.WHITE);
            shadowPaint.setTextSize(textSize);
//		shadowPaint.setStrokeWidth(2.0f);
//		shadowPaint.setStyle(Paint.Style.STROKE);
            shadowPaint.setShadowLayer(4f, 0, 0, Color.BLACK);
            String msg = context.getString(R.string.app_desc);

            canvas.drawText(msg, padding, result.getHeight() - textSize, shadowPaint);
        }catch (Exception e){}

        if(recycle)src.recycle();

        return result;
    }


    public static String getBase64Encoding(String txt){
        String re = txt;
        try {
            byte[] data = txt.getBytes("UTF-8");
            re = Base64.encodeToString(data, Base64.NO_WRAP );
//            re = Base64.encodeToString(data, Base64.DEFAULT );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return re;
    }
    public static String getBase64Decoding(String txt){
        String decodeText = txt;
        try {
            decodeText = new String(Base64.decode(txt, Base64.NO_WRAP));
//            decodeText = new String(Base64.decode(txt, Base64.DEFAULT));
        }catch (Exception e){}

        return decodeText;
    }



    public static void sendKakaoLink(Context context, FeedData mFeedData, ResponseCallback mResponseCallback){
        if(mFeedData == null || context == null)
            return;


        String title = "";
        String desc = "";
        String img = "https://1.bp.blogspot.com/-tbuUYER9XNk/WvQKBK1P3hI/AAAAAAAAAH8/u1OGT4CMY5AlYg6EFv1nZ0AoUxLqj7A_wCLcBGAs/s320/facebook_default.png";

        if(!TextUtils.isEmpty(mFeedData.getTitle())){
            title = mFeedData.getTitle();
        }
        if(!TextUtils.isEmpty(mFeedData.getText())){
            desc = mFeedData.getText();
        }
        if(!TextUtils.isEmpty(mFeedData.getpImg())) {
            img = "https://docs.google.com/uc?export=download&id=" + mFeedData.getpImg();
        }


        FeedTemplate params = FeedTemplate
                .newBuilder(ContentObject.newBuilder(title,
                        img,
                        LinkObject.newBuilder().setAndroidExecutionParams(context.getString(R.string.app_host_parameter) +"=" + getBase64Encoding(mFeedData.getKey())).build()
//                        LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
//                                .setMobileWebUrl("https://developers.kakao.com").build()
                        )
                        .setDescrption(desc)
                        .build())
//                .setSocial(SocialObject.newBuilder().setLikeCount(10).setCommentCount(20).setSharedCount(30).setViewCount(40).build())
//                .addButton(new ButtonObject("웹에서 보기", LinkObject.newBuilder().setWebUrl("'https://developers.kakao.com").setMobileWebUrl("'https://developers.kakao.com").build()))
                .addButton(new ButtonObject(context.getString(R.string.kakao_link_btn01), LinkObject.newBuilder()
//                        .setWebUrl("'https://developers.kakao.com")
//                        .setMobileWebUrl("'https://developers.kakao.com")
//                        .setAndroidExecutionParams("key1=value1")
                        .setAndroidExecutionParams(context.getString(R.string.app_host_parameter) +"=" + getBase64Encoding(mFeedData.getKey()))
//                        .setIosExecutionParams("key1=value1")
                        .build()))
                .build();

        KakaoLinkService.getInstance().sendDefault(context, params, mResponseCallback);


    }

}
