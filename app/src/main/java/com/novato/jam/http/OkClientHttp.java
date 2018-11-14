package com.novato.jam.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by poshaly on 2017. 2. 20..
 */

public class OkClientHttp {
    static private OkHttpClient mOkHttpClient;



    static public OkHttpClient getOkClient(){
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }
    static public OkHttpClient setInstance(SSLSocketFactory f){

        synchronized (OkClientHttp.class) {
            if (mOkHttpClient == null) {
                mOkHttpClient = new OkClientHttp().CertificateBuild(f);
            }
        }

        return mOkHttpClient;
    }

    static public OkHttpClient getInstance(){

        synchronized (OkClientHttp.class) {
            if (mOkHttpClient == null) {
                mOkHttpClient = new OkClientHttp().CertificateBuild(null);
            }
        }

        return mOkHttpClient;
    }

    static public OkHttpClient setFileInstance(SSLSocketFactory f){

        synchronized (OkClientHttp.class) {
            if (mOkHttpClient == null) {
                mOkHttpClient = new OkClientHttp().CertificateFileBuild(f);
            }
        }

        return mOkHttpClient;
    }

    static public OkHttpClient getFileInstance(){

        synchronized (OkClientHttp.class) {
            if (mOkHttpClient == null) {
                mOkHttpClient = new OkClientHttp().CertificateFileBuild(null);
            }
        }

        return mOkHttpClient;
    }


    public OkHttpClient CertificateBuild(SSLSocketFactory f){


        if(f== null){
            return Build();
        }


        X509TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        OkHttpClient.Builder o = new OkHttpClient().newBuilder();
        o.sslSocketFactory(f, tm);
        return o.connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
//                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }
    public OkHttpClient Build(){
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
//                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }


    public OkHttpClient CertificateFileBuild(SSLSocketFactory f){


        if(f== null){
            return fileBuild();
        }


        X509TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        OkHttpClient.Builder o = new OkHttpClient().newBuilder();
        o.sslSocketFactory(f, tm);
        return o.connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }
    public OkHttpClient fileBuild(){
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }
}
