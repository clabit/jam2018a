package com.novato.jam.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
//import com.google.android.gms.internal.zzdym;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
//import com.kakao.auth.ApiResponseCallback;
//import com.kakao.auth.AuthService;
//import com.kakao.auth.AuthType;
//import com.kakao.auth.ISessionCallback;
//import com.kakao.auth.Session;
//import com.kakao.auth.network.response.AccessTokenInfoResponse;
//import com.kakao.network.ErrorResult;
//import com.kakao.usermgmt.UserManagement;
//import com.kakao.usermgmt.callback.LogoutResponseCallback;
//import com.kakao.usermgmt.callback.MeResponseCallback;
//import com.kakao.usermgmt.response.model.UserProfile;
//import com.kakao.util.exception.KakaoException;
import com.novato.jam.BuildConfig;
import com.novato.jam.GlobalApplication;
import com.novato.jam.R;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.common.Utils;
import com.novato.jam.data.UserData;
import com.novato.jam.db.DBManager;
import com.novato.jam.db.MyPreferences;
import com.novato.jam.dialog.CustomToast;
import com.novato.jam.firebase.Fire;
import com.novato.jam.firebase.Parser;
import com.novato.jam.push.SendPushFCM;

import org.json.JSONObject;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class IntroActivity extends BaseActivity implements View.OnClickListener{
    final int RC_SIGN_IN = 333;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    private Handler mHandler = new Handler();

    private ProgressDialog mProgressDialog;

    public static void printHashKey(Context pContext) {
        //facebook get hashkey
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                LoggerManager.e("mun", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (Exception e) {
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        mProgressDialog = new ProgressDialog(getActivity());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        final FirebaseUser currentUser = mAuth.getCurrentUser();


        if(currentUser != null && !TextUtils.isEmpty(currentUser.getUid())){

            findViewById(R.id.lay_intro).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_login).setVisibility(View.GONE);
            if(mHandler!=null)mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {

                        UserData mUserData = new UserData(currentUser);

                        updateUI(mUserData);
                    }catch (Exception e){}
                }
            },400);

        }
        else {
            findViewById(R.id.lay_intro).setVisibility(View.GONE);
            findViewById(R.id.iv_login).setVisibility(View.VISIBLE);

            setFacebookBtn();
            setGoogleBtn();
//            setKakaoBtn();
        }

        findViewById(R.id.btn_google).setOnClickListener(this);
        findViewById(R.id.btn_facebook).setOnClickListener(this);
//        findViewById(R.id.btn_kakao).setOnClickListener(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if(callback!=null)Session.getCurrentSession().removeCallback(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        Fire.getReference().setValue("tttt", new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                if(databaseError != null){
//                    Log.e("munx","" + databaseError.getMessage());
//                }
//            }
//        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_facebook:{
                startCustomPopup(1);

                break;
            }
            case R.id.btn_google:{
                startCustomPopup(2);

                break;
            }
//            case R.id.btn_kakao:{
//                startCustomPopup(3);
//
//                break;
//            }
        }
    }

    private void startCustomPopup(final int type){
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
        alert_confirm.setTitle(R.string.info_agreement)
                .setMessage(R.string.text_agreement01)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_agreement,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (type){
                                    case 1:{

                                        setLogOut(getActivity());
                                        LoginManager.getInstance().logOut();
                                        LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("email","public_profile"));
                                        break;
                                    }
                                    case 2:{
                                        setLogOut(getActivity());
                                        signIn();

                                        break;
                                    }
                                    case 3:{
                                        setLogOut(getActivity());
//                                        Session.getCurrentSession().close();
//                                        Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, getActivity());

                                        break;
                                    }
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

        AlertDialog alert = alert_confirm.create();
        alert.show();
    }




    private void updateUI(UserData user){
        updateUI(user, false);
    }
    private void updateUI(final UserData user, boolean isNewLogin){
        if(user == null || TextUtils.isEmpty(user.getUid())) {

            setLogOut(getActivity());

            findViewById(R.id.lay_intro).setVisibility(View.GONE);
            findViewById(R.id.iv_login).setVisibility(View.VISIBLE);

            setFacebookBtn();
            setGoogleBtn();
//            setKakaoBtn();


            return;
        }

        if(!TextUtils.isEmpty(user.getUid())) {
            MyPreferences.set(getActivity(), MyPreferences.KEY_uid, user.getUid());


            try {
                if (!TextUtils.isEmpty(user.getUserName()))
                    MyPreferences.set(getActivity(), MyPreferences.KEY_name, user.getUserName());
                if (user.getpImg() != null && !TextUtils.isEmpty(user.getpImg()))
                    MyPreferences.set(getActivity(), MyPreferences.KEY_img, user.getpImg());
                if (!TextUtils.isEmpty(user.getMail()))
                    MyPreferences.set(getActivity(), MyPreferences.KEY_mail, user.getMail());

                LoggerManager.e("mun", "Uid : " + user.getUid());
                LoggerManager.e("mun", "mail : " + user.getMail());
                LoggerManager.e("mun", "DisplayName : " + user.getUserName());
                LoggerManager.e("mun", "PhotoUrl : " + user.getpImg());
            }catch (Exception e){}


//            final UserData mUserData = new UserData();
//            mUserData.setUid(user.getUid());
//            mUserData.setMail(user.getEmail());
//            mUserData.setUserName(user.getDisplayName());
//            mUserData.setTime(Fire.getServerTimestamp());
//            mUserData.setpImg(user.getpImg());


            final String push_token = FirebaseInstanceId.getInstance().getToken();

            if(!TextUtils.isEmpty(push_token)){
                MyPreferences.set(getActivity(), MyPreferences.KEY_push, push_token);
            }


            if(isNewLogin) {
                user.setPush(push_token);
                setAlertNickName(user);
            }
            else{


                Fire.getReference().child(Fire.KEY_USER).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot!=null && dataSnapshot.getValue() !=null){


                            HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue(true);

                            final UserData m = Parser.getUserDataParse(user.getUid() , data);

                            if(!TextUtils.isEmpty(user.getpImg())) {//프로필 이미지가 변경됬을 수도 있으니 엎어침
                                m.setpImg(user.getpImg());
                                Fire.getReference().child(Fire.KEY_USER).child(user.getUid()).child("pImg").setValue(user.getpImg());
                            }

                            if(!TextUtils.isEmpty(push_token)){
                                Fire.getReference().child(Fire.KEY_USER).child(user.getUid()).child("push").setValue(push_token);
                                m.setPush(push_token);


                                Fire.getReference().child(Fire.KEY_ADMIN).child(user.getUid()).child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        try {
                                            if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                                long admin = (Long) dataSnapshot.getValue(true);
                                                if(admin > 0){
                                                    MainActivity.isAdmin = true;
                                                }
                                            }
                                        }catch (Exception e){}
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                if(BuildConfig.DEBUG){
                                    GlobalApplication.runBackground(new Runnable() {
                                        @Override
                                        public void run() {
                                            ArrayList<String> list = new ArrayList<String>();
                                            list.add(push_token);
                                            String re = new SendPushFCM(getActivity(), list, "디버깅 모드 입니다. 안녕하세요. " + m.getUserName() + " 님 ^^","디버깅모드 푸시 테스트 입니다.").start();
                                        }
                                    });
                                }
                            }


                            MainActivity.mUserData = m;

                            setResult(RESULT_OK);
                            finish();
                        }
                        else{
                            setLogOut(getActivity());

                            findViewById(R.id.lay_intro).setVisibility(View.GONE);
                            findViewById(R.id.iv_login).setVisibility(View.VISIBLE);

                            setFacebookBtn();
                            setGoogleBtn();
//                            setKakaoBtn();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
//                Fire.getReference().child(Fire.KEY_USER).child(user.getUid()).setValue(mUserData.getHashMap());
//                setResult(RESULT_OK);
//                finish();
            }




        }

    }

    private void setAlertNickName(final UserData mUserData){

        Fire.getReference().child(Fire.KEY_USER).child(mUserData.getUid()).setValue(mUserData.getHashMap());
        MainActivity.mUserData = mUserData;
        setResult(RESULT_OK);
        finish();


//        final EditText et = new EditText(getActivity());
//        et.setText(mUserData.getUserName());
//        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
//        alert_confirm.setTitle(R.string.name_setting)
//                .setView(et)
//                .setCancelable(false)
//                .setPositiveButton(android.R.string.ok,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                                String name = et.getText().toString();
//                                if(!TextUtils.isEmpty(name) && name.length() >= 2) {
//                                    mUserData.setUserName(name);
//
//                                    Fire.getReference().child(Fire.KEY_USER).child(mUserData.getUid()).setValue(mUserData.getHashMap());
//
//
//                                    MainActivity.mUserData = mUserData;
//
//                                    setResult(RESULT_OK);
//                                    finish();
//                                }
//                                else{
//                                    Toast.makeText(getActivity(), R.string.name_size_err,Toast.LENGTH_SHORT).show();
//                                    setAlertNickName(mUserData);
//                                }
//                            }
//                        });
//        AlertDialog alert = alert_confirm.create();
//        alert.show();
    }


    /*********************
     * google login
     */

    private void setGoogleBtn(){
        findViewById(R.id.btn_google).setVisibility(View.VISIBLE);
    }

    private void signIn() {
        /*
        https://console.developers.google.com/apis 에서 OAuth 등록 필요. 플업의 파이어베이스를 사용하고 있기 때문에 플럽콘솔(ptv.app)에서 설정해야함.
         */

//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        mGoogleSignInClient.signOut();

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        if(mProgressDialog!=null)mProgressDialog.show();
        try {
            if (!TextUtils.isEmpty(acct.getDisplayName()))
                MyPreferences.set(getActivity(), MyPreferences.KEY_name, acct.getDisplayName());
            if (acct.getPhotoUrl() != null && !TextUtils.isEmpty(acct.getPhotoUrl().toString()))
                MyPreferences.set(getActivity(), MyPreferences.KEY_img, acct.getPhotoUrl().toString());
            if (!TextUtils.isEmpty(acct.getEmail())) {
                MyPreferences.set(getActivity(), MyPreferences.KEY_mail, acct.getEmail());
                MyPreferences.set(getActivity(), MyPreferences.KEY_DRIVE_NAME, acct.getEmail());
            }

            LoggerManager.e("mun", "firebaseAuthWithGoogle id:" + acct.getId());
            LoggerManager.e("mun", "firebaseAuthWithGoogle Email:" + acct.getEmail());
            LoggerManager.e("mun", "firebaseAuthWithGoogle name:" + acct.getDisplayName());
            LoggerManager.e("mun", "firebaseAuthWithGoogle url:" + acct.getPhotoUrl());

        }catch (Exception e){}


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(mProgressDialog!=null)mProgressDialog.dismiss();

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            LoggerManager.e("mun", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(new UserData(user), true);
                        } else {
                            // If sign in fails, display a message to the user.
                            LoggerManager.e("mun", "signInWithCredential:failure : " + task.getException().getMessage());
                            CustomToast.showToast(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT);
                            updateUI(null);
                        }
                    }
                });
    }





    /*******************
     * facebook
     */

    CallbackManager mCallbackManager;
    private void setFacebookBtn(){
        // Initialize Facebook Login button

        findViewById(R.id.btn_facebook).setVisibility(View.VISIBLE);
        mCallbackManager = CallbackManager.Factory.create();
        {
            boolean loggedIn = AccessToken.getCurrentAccessToken() == null;
            LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException error) {

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                LoggerManager.e("mun", "signInWithCredential:failure : " + result.getStatus().toString());

                CustomToast.showToast(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT);
                updateUI(null);
            }
        }
//        else if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
//            return;
//        }
        else {
            // Pass the activity result back to the Facebook SDK
            if (mCallbackManager != null)
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }



    private void handleFacebookAccessToken(AccessToken token) {

        if(mProgressDialog!=null)mProgressDialog.show();


        LoggerManager.e("mun","AccessToken UserId : " + token.getUserId());
        LoggerManager.e("mun","AccessToken Token : " + token.getToken());
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());


        //기존 플럽에서 사용하고 있어서 페이스북 로그인은 쓸수 없음....
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(mProgressDialog!=null)mProgressDialog.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(new UserData(user), true);
                        } else {
                            LoggerManager.e("mun","AccessToken getException : " + task.getException().getMessage());

                            CustomToast.showToast(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT);
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }


    /*****************
     *
     * kakao
     */

//    private SessionCallback callback;
//    private void setKakaoBtn(){
//
//        Session.getCurrentSession().close();
//
////        UserManagement.requestLogout(new LogoutResponseCallback() {
////            @Override
////            public void onCompleteLogout() {
////            }
////        });
//
//        callback = new SessionCallback();
//        Session.getCurrentSession().addCallback(callback);
//        Session.getCurrentSession().checkAndImplicitOpen();
//        findViewById(R.id.btn_kakao).setVisibility(View.VISIBLE);
//    }
//    private class SessionCallback implements ISessionCallback {
//
//        @Override
//        public void onSessionOpened() {
//            requestMe();
//        }
//
//        @Override
//        public void onSessionOpenFailed(KakaoException exception) {
//            if(exception != null) {
//                LoggerManager.e("mun",exception.toString());
//            }
//        }
//    }
//    private void requestMe() {
//        if(mProgressDialog!=null)mProgressDialog.show();
//
//        List<String> propertyKeys = new ArrayList<String>();
//        propertyKeys.add("kaccount_email");
//        propertyKeys.add("nickname");
//        propertyKeys.add("profile_image");
//        propertyKeys.add("thumbnail_image");
//
//        UserManagement.requestMe(new MeResponseCallback() {
//            @Override
//            public void onFailure(ErrorResult errorResult) {
//                if(mProgressDialog!=null)mProgressDialog.dismiss();
//
//                String message = "failed to get user info. msg=" + errorResult;
//                LoggerManager.d("mun", message);
//            }
//
//            @Override
//            public void onSessionClosed(ErrorResult errorResult) {
//                if(mProgressDialog!=null)mProgressDialog.dismiss();
//            }
//
//            @Override
//            public void onSuccess(UserProfile userProfile) {
//                if(mProgressDialog!=null)mProgressDialog.dismiss();
//                if(userProfile == null)
//                    return;
//
//                LoggerManager.e("mun", "kakao UserProfile : " + userProfile.getId());
//                LoggerManager.e("mun", "kakao UserProfile : " + userProfile.getUUID());
//                LoggerManager.e("mun", "kakao UserProfile : " + userProfile.toString());
//
//
//                handleKakaoAccessProfile(userProfile);
//            }
//
//            @Override
//            public void onNotSignedUp() {
//                if(mProgressDialog!=null)mProgressDialog.dismiss();
//            }
//        }, propertyKeys, false);
//    }
//
//    protected void handleKakaoAccessProfile(UserProfile userProfile) {
//
//        //되지 않음.... 서버에서 파이어베이스 어드민 sdk로 설정 하는 방법밖엔 없음.. 보안문제로 클라에선 불가능s
//        //https://firebase.google.com/docs/auth/admin/create-custom-tokens
////        KeyPair myKey = RsaProvider.generateKeyPair();
////        Date current_date = new Date();
////        Calendar calendar = Calendar.getInstance();
////        calendar.add(Calendar.SECOND, 3000);
////        Date exp_time = calendar.getTime();
////        String compactJws = Jwts.builder()
////                .setSubject("firebase-adminsdk-eizh6@jam2018-c7297.iam.gserviceaccount.com")//(ACCOUNT_EMAIL)
////                .setIssuer("firebase-adminsdk-eizh6@jam2018-c7297.iam.gserviceaccount.com")//(ACCOUNT_EMAIL)
////                .setAudience("https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit")
//////                .setIssuedAt(current_date)
//////                .setExpiration(exp_time)
//////                .setId("kakao:"+userProfile.getId())//(linkedinId)
//////                .claim("displayName", userProfile.getNickname())
//////                .claim("provider", "kakao")
//////                .claim("uid", "kakao:"+userProfile.getId())
//////                .claim("url", userProfile.getProfileImagePath())
////                .claim("iat",current_date.getTime())
////                .claim("uid","kakao:"+userProfile.getId())
////                .claim("exp",exp_time.getTime()/1000)
////                .signWith(SignatureAlgorithm.RS256, myKey.getPrivate())
////                .compact();
////        signInWithCustomToken
//
//
//        UserData mUser = new UserData();
//        mUser.setUid(userProfile.getId()+"");
//        mUser.setMail(userProfile.getEmail());
//        mUser.setpImg(userProfile.getProfileImagePath());
//        mUser.setUserName(userProfile.getNickname());
//
//        checkCustomEmail(mUser);
//    }

    private void checkCustomEmail(final UserData userProfile){
        String mail = userProfile.getUid() + "@kakao.jam";
        if(mAuth!=null)mAuth.signInWithEmailAndPassword(mail,mail).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(mProgressDialog!=null)mProgressDialog.dismiss();
                if (task.isSuccessful() && mAuth!=null) {

                    LoggerManager.e("mun","task : " + task.toString());


                    FirebaseUser user = mAuth.getCurrentUser();


                    userProfile.setUid(user.getUid());

                    if (!TextUtils.isEmpty(user.getDisplayName()))
                        userProfile.setUserName(user.getDisplayName());
                    if (user.getPhotoUrl() != null && !TextUtils.isEmpty(user.getPhotoUrl().toString()))
                        userProfile.setpImg(user.getPhotoUrl().toString());
                    if (!TextUtils.isEmpty(user.getEmail()))
                        userProfile.setMail(user.getEmail());

                    updateUI(userProfile, true);
                }
                else{
                    LoggerManager.e("mun","task : " + task.getException().getMessage());
                    joinCustomEmail(userProfile);
                }
            }
        });
    }

    private void joinCustomEmail(final UserData userProfile){
        String mail = userProfile.getUid() + "@kakao.jam";

        if(mAuth!=null)mAuth.createUserWithEmailAndPassword(mail,mail).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(mProgressDialog!=null)mProgressDialog.dismiss();
                if (task.isSuccessful() && mAuth!=null) {

                    LoggerManager.e("mun","task : " + task.toString());


                    FirebaseUser user = mAuth.getCurrentUser();

                    userProfile.setUid(user.getUid());

                    if (!TextUtils.isEmpty(user.getDisplayName()))
                        userProfile.setUserName(user.getDisplayName());
                    if (user.getPhotoUrl() != null && !TextUtils.isEmpty(user.getPhotoUrl().toString()))
                        userProfile.setpImg(user.getPhotoUrl().toString());
                    if (!TextUtils.isEmpty(user.getEmail()))
                        userProfile.setMail(user.getEmail());

                    updateUI(userProfile, true);
                }
                else{
                    LoggerManager.e("mun","task : " + task.getException().getMessage());
                    CustomToast.showToast(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT);
                    updateUI(null);
                }
            }
        });
    }










    static public void setLogOut(Context con){

        FirebaseAuth.getInstance().signOut();
        MyPreferences.setLogOutData(con);
        MainActivity.mUserData = null;
        MainActivity.isAdmin = false;

        try {
            DBManager.createInstnace(con).deleteAllDB();
        }catch (Exception e){}

        try {
            Utils.setBadgeCount(con, 0);
        }catch (Exception e){}

    }


}


