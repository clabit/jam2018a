package com.novato.jam.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

public class MyPreferences {

	static public String KEY_push = "KEY_push";

	static public String KEY_uid = "KEY_uid";
	static public String KEY_name = "KEY_name";
	static public String KEY_mail = "KEY_mail";
	static public String KEY_img = "KEY_img";


	static public String KEY_NOTICE = "KEY_NOTICE";
	static public String KEY_SETTING_PUSH = "KEY_SETTING_PUSH";

	static public String KEY_DRIVE_NAME = "KEY_DRIVE_NAME";
	static public String KEY_DRIVE_TOKEN = "KEY_DRIVE_TOKEN";

	static public String KEY_ROOMINFI_MODIFY = "KEY_ROOMINFI_MODIFY";
	static public String KEY_ROOM_HEART = "KEY_ROOM_HEART";


	private static SharedPreferences get(Context cxt)
	{
		int mode = Context.MODE_PRIVATE;
		if(Build.VERSION.SDK_INT >= 11) mode |= 4;
		return cxt.getSharedPreferences("com.novato.jam", mode);
	}
	
	
	public static String getString(Context cxt, String name){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		return prefv.getString(name, "");
	}
	public static int getInt(Context cxt, String name){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		return prefv.getInt(name, -1);
	}
	public static long getLong(Context cxt, String name){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		return prefv.getLong(name, -1);
	}
	public static boolean getBoolean(Context cxt, String name){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		return prefv.getBoolean(name, false);
	}
	
	
	public static void set(Context cxt, String name, String value){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		Editor edit =  prefv.edit();
		edit.putString(name, value);
		edit.commit();
	}
	public static void set(Context cxt, String name, boolean value){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		Editor edit =  prefv.edit();
		edit.putBoolean(name, value);
		edit.commit();
	}
	public static void set(Context cxt, String name, long value){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		Editor edit =  prefv.edit();
		edit.putLong(name, value);
		edit.commit();
	}
	public static void set(Context cxt, String name, int value){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		Editor edit =  prefv.edit();
		edit.putInt(name, value);
		edit.commit();
	}




	public static boolean getSettingPush(Context cxt){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		return prefv.getBoolean(MyPreferences.KEY_SETTING_PUSH, true);
	}
	public static void setSettingPush(Context cxt, boolean value){
		SharedPreferences prefv =  MyPreferences.get(cxt);
		Editor edit =  prefv.edit();
		edit.putBoolean(MyPreferences.KEY_SETTING_PUSH, value);
		edit.commit();
	}


	static public void setLogOutData(Context con){
		MyPreferences.set(con, MyPreferences.KEY_uid, "");
		MyPreferences.set(con, MyPreferences.KEY_name, "");
		MyPreferences.set(con, MyPreferences.KEY_img, "");
		MyPreferences.set(con, MyPreferences.KEY_mail, "");
		MyPreferences.set(con, MyPreferences.KEY_push, "");

		MyPreferences.set(con, MyPreferences.KEY_DRIVE_NAME, "");
		MyPreferences.set(con, MyPreferences.KEY_DRIVE_TOKEN, "");

		MyPreferences.set(con, MyPreferences.KEY_SETTING_PUSH, true);
	}
	
}
