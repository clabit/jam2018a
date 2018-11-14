# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/poshaly/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


#kakao
-keep class com.kakao.** { *; }
-keepattributes Signature
-keepclassmembers class * {
  public static <fields>;
  public *;
}
-dontwarn android.support.v4.**,org.slf4j.**,com.google.android.gms.**



# For Google Play Services
-keep public class com.google.android.gms.ads.**{
   public *;
}

# For old ads classes
-keep public class com.google.ads.**{
   public *;
}

-keep class org.videolan.** { *; }




-keep class com.google.android.youtube.** { *; }
-dontwarn com.google.android.youtube.**

-keep class wseemann.media.** { *; }
-dontwarn wseemann.media.**

-keep class com.squareup.** { *; }
-dontwarn com.squareup.**

-keep class com.bumptech.** { *; }
-dontwarn com.bumptech.**

-keep class org.codehaus.** { *; }
-dontwarn org.codehaus.**

-keep class com.android.okhttp.** { *; }
-dontwarn com.android.okhttp.**

-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**


-keep class com.google.api.** { *; }
-dontwarn com.google.api.**

-keep class com.kailashdabhi.** { *; }
-dontwarn com.kailashdabhi.**

-keep class com.cleveroad.** { *; }
-dontwarn com.cleveroad.**

-keep class com.klinkerapps.** { *; }
-dontwarn com.klinkerapps.**
