package com.novato.jam.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.novato.jam.BuildConfig;
import com.novato.jam.common.LoggerManager;
import com.novato.jam.data.PushData;
import com.novato.jam.data.UserData;
import com.novato.jam.firebase.Fire;

import java.util.ArrayList;


public class DBManager
{
	private static final String TAG = "DBManager";
	// __LINE__
	// new Throwable().getStackTrace()[0].getLineNumber();

	// __FUNCTION__
	// new Throwable().getStackTrace()[0].getMethodName();

	private static class prof
	{
		private String msg_;
		private long time_;

		private prof(String msg)
		{
			msg_ = msg;
			time_ = (new java.util.Date()).getTime();
			// Logger.error(GlobalConfig.LogTag.DBMgr,""+time_+" >>> "+msg_);
			LoggerManager.d(TAG," >>> "+msg_);
		}

		private void close()
		{
			long endtime = (new java.util.Date()).getTime();
			// Logger.error(GlobalConfig.LogTag.DBMgr,""+endtime+" <<< "+msg_+" ("+(endtime-time_)+" elapsed)");
			LoggerManager.d(TAG," <<< "+msg_+" ("+(endtime-time_)+") elapsed");
		}

		public static prof enter()
		{
			if(BuildConfig.DEBUG)
			{
				StackTraceElement[] t = new Throwable().getStackTrace();

				StringBuffer name = new StringBuffer();
				for(int i = Math.min(3, t.length - 1); i > 0; i--)
				{
					String[] token = t[i].getClassName().split("\\.");
					String className = token[token.length - 1];

					name.append(className + ":" + t[i].getMethodName());
					if(i > 1) name.append("->");
				}
				return new prof(name.toString());
			}
			else
			{
				return null;
			}
		}

		public static prof enter(String msg)
		{
			if(BuildConfig.DEBUG)
			{
				Throwable t = new Throwable();
				String name1 = t.getStackTrace()[1].getMethodName();
				String name2 = t.getStackTrace()[2].getMethodName();
				String name3 = t.getStackTrace()[3].getMethodName();
				String name = name3 + "->" + name2 + "->" + name1;
				return new prof(name + " " + msg);
			}
			else
			{
				return null;
			}
		}

		public static void exit(prof o)
		{
			if(BuildConfig.DEBUG)
			{
				if(o != null) o.close();
			}
		}

		public static void exception(Throwable e)
		{
			LoggerManager.e(TAG,"Exception: "+e);
		}
	}

	// SQLiteDatabase mDatabase;
	/**/// SQLiteDatabase oldDB = null;

	private MyDatabaseOpenHelper mHelper = null;

	Context mContext;
	private static DBManager mdbmgr = null;

	public static final String DB_NAME = "jam.db";
	public static final String DB_VERSION = "1.0.5";

	public static final int DB_VERSION_INT = 10005;

	/******************************
	 * DB TABLE
	 ******************************/
	public static final String DB_TABLE_SEEK = "DB_TABLE_SEEK";
	public static final String DB_TABLE_LAUNCHER = "DB_TABLE_LAUNCHER";
	public static final String DB_TABLE_USER = "DB_TABLE_USER";
	public static final String DB_TABLE_MESSAGE = "DB_TABLE_MESSAGE";
	public static final String DB_TABLE_DRIVEID = "DB_TABLE_DRIVEID";
	public static final String DB_TABLE_DRIVECHATID = "DB_TABLE_DRIVECHATID";

	/******************************
	 * DB RECORD
	 ******************************/

	public static final String DB_RECORD_BNO = "bno";
	public static final String DB_DATEON = "dateon";
	public static final String DB_ROOM = "room";
	public static final String DB_COUNT = "count";


	public static final String DB_UID = "uid";
	public static final String DB_PUSH = "push";
	public static final String DB_UNAME = "uname";
	public static final String DB_MSG = "msg";
	public static final String DB_ROOM_NAME = "roomName";

	private class MyDatabaseOpenHelper extends SQLiteOpenHelper
	{

		public MyDatabaseOpenHelper(Context context)
		{
			super(context, DB_NAME, null, DB_VERSION_INT);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			LoggerManager.d(TAG,"DBManager onCreate()");
			createTables(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			LoggerManager.d(TAG,"DBManager onUpgrade()");
			int v1 = oldVersion / 10000;
			int v2 = oldVersion / 100 % 100;
			int v3 = oldVersion % 100;
			String version = String.format("%d.%d.%d", v1, v2, v3);
			DBUpdate(db, mContext, version);
		}
	};

	// readonly database
	public synchronized SQLiteDatabase ro()
	{
		// getReadableDatabase() cache in mHelper
		return mHelper.getReadableDatabase();
	}

	// read/write database
	public synchronized SQLiteDatabase rw()
	{
		// getWritableDatabase() cache in mHelper
		return mHelper.getWritableDatabase();
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	public static DBManager createInstnace(Context cxt)
	{
		//prof p = prof.enter();
		if(mdbmgr != null)
		{
			return mdbmgr;
		}
		else
		{
			mdbmgr = new DBManager(cxt);
			//prof.exit(p);
			return mdbmgr;
		}
	}

	// by MG2 - 서비스에서 사용하기 위해서
	public DBManager(Context c)
	{
		mContext = c;
		mHelper = new MyDatabaseOpenHelper(c);
	}

	public synchronized void deleteAllDB()
	{
		prof p = prof.enter();

		deleteTable(DB_TABLE_SEEK);
		deleteTable(DB_TABLE_LAUNCHER);
		deleteTable(DB_TABLE_MESSAGE);
		deleteTable(DB_TABLE_DRIVEID);
		deleteTable(DB_TABLE_DRIVECHATID);


		prof.exit(p);
	}

	public synchronized void deleteTable(String tablename)
	{
		prof p = prof.enter("tablename=" + tablename);

		try
		{
			rw().delete(tablename, null, null);
		}
		catch(Exception e)
		{
			prof.exception(e);
		}
		prof.exit(p);
	}

	public synchronized void closeDBManager()
	{
	}

	public synchronized  void deleteRecord(long time){ // 비밀메세지 목록 하나씩 제거하기
		prof p = prof.enter();

		try{

			String sql = "DELETE FROM "+ DB_TABLE_MESSAGE + " WHERE " + DB_DATEON + " = " + time + "";
			rw().execSQL(sql);


		}catch (SQLiteException e){
			prof.exception(e);
		}
		prof.exit(p);
	}


	private synchronized void createTables(SQLiteDatabase sqlitedb)
	{
		prof p = prof.enter();

		sqlitedb.beginTransaction();




		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_SEEK + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_ROOM + " TEXT, " +
				DB_COUNT + " INTEGER, " +
				DB_DATEON + " INTEGER);");

		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_LAUNCHER + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_ROOM + " TEXT, " +
				DB_COUNT + " INTEGER, " +
				DB_DATEON + " INTEGER);");

		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_USER + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_PUSH + " TEXT, " +
				DB_UID + " TEXT, " +
				DB_DATEON + " INTEGER);");

		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_MESSAGE + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_ROOM + " TEXT, " +
				DB_ROOM_NAME + " TEXT, " +
				DB_UID + " TEXT, " +
				DB_UNAME + " TEXT, " +
				DB_MSG + " TEXT, " +
				DB_DATEON + " INTEGER);");

		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_DRIVEID + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_MSG + " TEXT, " +
				DB_UID + " TEXT, " +
				DB_DATEON + " INTEGER);");

		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_DRIVECHATID + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_MSG + " TEXT, " +
				DB_UID + " TEXT, " +
				DB_DATEON + " INTEGER);");

		sqlitedb.setTransactionSuccessful();
		sqlitedb.endTransaction();

		prof.exit(p);
	}

	/******************************
	 * DB exist or not
	 ******************************/
	public synchronized boolean isNotExistDB()
	{
		for(String b : mContext.databaseList())
		{
			if(b.equals(DBManager.DB_NAME)) return false;
		}
		return true;
	}

	/******************************
	 * db update
	 ******************************/
	private synchronized void DBUpdate(SQLiteDatabase sqlitedb, Context context, String ver)
	{
		prof p = prof.enter(ver);

		sqlitedb.beginTransaction();





		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_SEEK + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_ROOM + " TEXT, " +
				DB_COUNT + " INTEGER, " +
				DB_DATEON + " INTEGER);");


		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_LAUNCHER + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_ROOM + " TEXT, " +
				DB_COUNT + " INTEGER, " +
				DB_DATEON + " INTEGER);");

		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_USER + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_PUSH + " TEXT, " +
				DB_UID + " TEXT, " +
				DB_DATEON + " INTEGER);");

		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_MESSAGE + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_ROOM + " TEXT, " +
				DB_ROOM_NAME + " TEXT, " +
				DB_UID + " TEXT, " +
				DB_UNAME + " TEXT, " +
				DB_MSG + " TEXT, " +
				DB_DATEON + " INTEGER);");


		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_DRIVEID + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_MSG + " TEXT, " +
				DB_UID + " TEXT, " +
				DB_DATEON + " INTEGER);");

		sqlitedb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_DRIVECHATID + " (" +
				DB_RECORD_BNO + " INTEGER PRIMARY KEY, " +
				DB_MSG + " TEXT, " +
				DB_UID + " TEXT, " +
				DB_DATEON + " INTEGER);");


		sqlitedb.setTransactionSuccessful();
		sqlitedb.endTransaction();

		prof.exit(p);
	}


	/************************
	 *
	 * room badge
	 */

	public synchronized boolean addRoomBadge(String room, long count){
		rw().beginTransaction();
		updateRoomBadge(room, count);
		rw().setTransactionSuccessful();
		rw().endTransaction();
		return true;
	}

	private synchronized boolean updateRoomBadge(String room, long count){
		ContentValues values = new ContentValues();
		values.put(DB_ROOM, room);
		values.put(DB_COUNT, count);
		values.put(DB_DATEON, Fire.getServerTimestamp());

		Cursor c = null;
		try{
			c = ro().query(
					DB_TABLE_SEEK,
					new String[] {DB_ROOM},
					DB_ROOM + "=? ",
					new String[] { room},
					null,
					null,
					null);

			if(c.moveToNext()){
				String sql = DB_ROOM + "='" + room + "' ";
				rw().update(DB_TABLE_SEEK, values, sql, null);
			}else{
				InsertHelper helper = new InsertHelper(rw(), DB_TABLE_SEEK);
				helper.replace(values);
				if(helper != null)
					helper.close();
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();



		return true;
	}



	public synchronized long getRoomBadge(String room){
		long re = 0;
		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_SEEK, null, DB_ROOM + "=?", new String[] {room}, null, null, null);
			if(c.moveToFirst()){
				do{
					long d = c.getLong(c.getColumnIndex(DB_COUNT));
					if(d > 0){
						re = d;
						break;
					}
				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return re;
	}


	public synchronized long getRoomBadgeToal(){

		long re = 0;

		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_SEEK, null, null, null, null, null, null);
			if(c.moveToFirst()){
				do{
					long d = c.getLong(c.getColumnIndex(DB_COUNT));
					if(d > 0){
						re += d;
					}
				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return re;
	}

	public synchronized boolean removeRoomBadge(String room){
		try{

			String sql = " "+DB_ROOM +" = '"+room+"' ";
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_COMMENTNO + "=?", new String[] {phone});
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_USERNICK + "=? " + DB_RECORD_FILENAME + "=? ", new String[] {phone,filename});
			rw().delete(DB_TABLE_SEEK, sql, null);

		}catch (SQLiteException e){
			return false;
		}
		return true;
	}

	public synchronized void removeRoomBadgeAll(){
		try{
			rw().delete(DB_TABLE_SEEK, null, null);
		}catch(Exception e){
		}
	}


	/***********
	 * launcher badge
	 */
	public synchronized boolean addLauncherBadge(String room, long count){
		rw().beginTransaction();
		updateLauncherBadge(room, count);
		rw().setTransactionSuccessful();
		rw().endTransaction();
		return true;
	}

	private synchronized boolean updateLauncherBadge(String room, long count){
		ContentValues values = new ContentValues();
		values.put(DB_ROOM, room);
		values.put(DB_COUNT, count);
		values.put(DB_DATEON, Fire.getServerTimestamp());

		Cursor c = null;
		try{
			c = ro().query(
					DB_TABLE_LAUNCHER,
					new String[] {DB_ROOM},
					DB_ROOM + "=? ",
					new String[] { room},
					null,
					null,
					null);

			if(c.moveToNext()){
				String sql = DB_ROOM + "='" + room + "' ";
				rw().update(DB_TABLE_LAUNCHER, values, sql, null);
			}else{
				InsertHelper helper = new InsertHelper(rw(), DB_TABLE_LAUNCHER);
				helper.replace(values);
				if(helper != null)
					helper.close();
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();



		return true;
	}



	public synchronized long getLauncherBadge(String room){
		long re = 0;
		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_LAUNCHER, null, DB_ROOM + "=?", new String[] {room}, null, null, null);
			if(c.moveToFirst()){
				do{
					long d = c.getLong(c.getColumnIndex(DB_COUNT));
					if(d > 0){
						re = d;
						break;
					}
				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return re;
	}


	public synchronized long getLauncherBadgeToal(){

		long re = 0;

		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_LAUNCHER, null, null, null, null, null, null);
			if(c.moveToFirst()){
				do{
					long d = c.getLong(c.getColumnIndex(DB_COUNT));
					if(d > 0){
						re += d;
					}
				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return re;
	}

	public synchronized boolean removeLauncherBadge(String room){
		try{

			String sql = " "+DB_ROOM +" = '"+room+"' ";
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_COMMENTNO + "=?", new String[] {phone});
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_USERNICK + "=? " + DB_RECORD_FILENAME + "=? ", new String[] {phone,filename});
			rw().delete(DB_TABLE_LAUNCHER, sql, null);

		}catch (SQLiteException e){
			return false;
		}
		return true;
	}

	public synchronized void removeLauncherBadgeAll(){
		try{
			rw().delete(DB_TABLE_LAUNCHER, null, null);
		}catch(Exception e){
		}
	}






	/***********
	 * user push
	 */
	public synchronized boolean addUserPush(String uid, String push){
		rw().beginTransaction();
		updateUserPush(uid, push);
		rw().setTransactionSuccessful();
		rw().endTransaction();
		return true;
	}

	private synchronized boolean updateUserPush(String uid, String push){
		ContentValues values = new ContentValues();
		values.put(DB_PUSH, push);
		values.put(DB_UID, uid);
		values.put(DB_DATEON, Fire.getServerTimestamp());

		Cursor c = null;
		try{
			c = ro().query(
					DB_TABLE_USER,
					new String[] {DB_UID},
					DB_UID + "=? ",
					new String[] { uid},
					null,
					null,
					null);

			if(c.moveToNext()){
				String sql = DB_UID + "='" + uid + "' ";
				rw().update(DB_TABLE_USER, values, sql, null);
			}else{
				InsertHelper helper = new InsertHelper(rw(), DB_TABLE_USER);
				helper.replace(values);
				if(helper != null)
					helper.close();
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();



		return true;
	}



	public synchronized String getUserPush(String uid){
		String re = null;
		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_USER, null, DB_UID + "=?", new String[] {uid}, null, null, null);
			if(c.moveToFirst()){
				do{
					String d = c.getString(c.getColumnIndex(DB_PUSH));
					if(!TextUtils.isEmpty(d)){
						re = d;
						break;
					}
				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return re;
	}
	public synchronized UserData getUserPushEDate(String uid){
		UserData re = null;
		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_USER, null, DB_UID + "=?", new String[] {uid}, null, null, null);
			if(c.moveToFirst()){
				do{
					String d = c.getString(c.getColumnIndex(DB_PUSH));
					long t = c.getLong(c.getColumnIndex(DB_DATEON));
					if(!TextUtils.isEmpty(d)){
						re = new UserData();
						re.setUid(uid);
						re.setPush(d);
						re.setTime(t);
						break;
					}
				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return re;
	}

	public synchronized boolean removeUserPush(String uid){
		try{

			String sql = " "+DB_UID +" = '"+uid+"' ";
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_COMMENTNO + "=?", new String[] {phone});
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_USERNICK + "=? " + DB_RECORD_FILENAME + "=? ", new String[] {phone,filename});
			rw().delete(DB_TABLE_USER, sql, null);

		}catch (SQLiteException e){
			return false;
		}
		return true;
	}

	public synchronized void removeUserPushAll(){
		try{
			rw().delete(DB_TABLE_USER, null, null);
		}catch(Exception e){
		}
	}





	/***********
	 * message
	 */
	public synchronized boolean addMessage(String room, String roomName, String uid, String name, String msg){

		rw().beginTransaction();


		ContentValues values = new ContentValues();
		values.put(DB_ROOM, room);
		values.put(DB_ROOM_NAME, roomName);
		values.put(DB_UID, uid);
		values.put(DB_UNAME, name);
		values.put(DB_MSG, msg);
		values.put(DB_DATEON, Fire.getServerTimestamp());

		try{
			InsertHelper helper = new InsertHelper(rw(), DB_TABLE_MESSAGE);
			helper.replace(values);
			if(helper != null)
				helper.close();
		}catch (SQLiteException e){
		}


		rw().setTransactionSuccessful();
		rw().endTransaction();

		return true;
	}
	public synchronized boolean updateMessage(String room, String roomName, String uid, String name, String msg){
		ContentValues values = new ContentValues();
		values.put(DB_ROOM, room);
		values.put(DB_ROOM_NAME, roomName);
		values.put(DB_UID, uid);
		values.put(DB_UNAME, name);
		values.put(DB_MSG, msg);
		values.put(DB_DATEON, Fire.getServerTimestamp());

		Cursor c = null;
		try{
			c = ro().query(
					DB_TABLE_MESSAGE,
					new String[] {DB_ROOM},
					DB_ROOM + "=? ",
					new String[] { room},
					null,
					null,
					null);

			if(c.moveToNext()){
				String sql = DB_ROOM + "='" + room + "' ";
				rw().update(DB_TABLE_MESSAGE, values, sql, null);
			}else{
				InsertHelper helper = new InsertHelper(rw(), DB_TABLE_MESSAGE);
				helper.replace(values);
				if(helper != null)
					helper.close();
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();



		return true;
	}


	public synchronized ArrayList<PushData> getRommMessage(String room){
		ArrayList<PushData> list = new ArrayList<>();

		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_MESSAGE, null, DB_ROOM + "=?", new String[] {room}, null, null, DB_DATEON + " desc");
			if(c.moveToFirst()){
				do{


					PushData d = new PushData();
					d.setTitle(c.getString(c.getColumnIndex(DB_UNAME)));
					d.setMsg(c.getString(c.getColumnIndex(DB_MSG)));
					d.setFrom_uid(c.getString(c.getColumnIndex(DB_UID)));
					d.setRoom(c.getString(c.getColumnIndex(DB_ROOM)));
					d.setRoomName(c.getString(c.getColumnIndex(DB_ROOM_NAME)));
					d.setTime(c.getLong(c.getColumnIndex(DB_DATEON)));
					list.add(d);

				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return list;
	}

	public synchronized ArrayList<PushData> getMessageAll(){
		ArrayList<PushData> list = new ArrayList<>();

		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_MESSAGE, null, null, null, null, null, DB_DATEON + " desc");
			if(c.moveToFirst()){
				do{


					PushData d = new PushData();
					d.setTitle(c.getString(c.getColumnIndex(DB_UNAME)));
					d.setMsg(c.getString(c.getColumnIndex(DB_MSG)));
					d.setFrom_uid(c.getString(c.getColumnIndex(DB_UID)));
					d.setRoom(c.getString(c.getColumnIndex(DB_ROOM)));
					d.setRoomName(c.getString(c.getColumnIndex(DB_ROOM_NAME)));
					d.setTime(c.getLong(c.getColumnIndex(DB_DATEON)));
					list.add(d);

				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return list;
	}


	/****
	 * drive profile img ids
	 */
	public synchronized boolean addDriveImg(String uid, String driveId){

		rw().beginTransaction();


		ContentValues values = new ContentValues();
		values.put(DB_UID, uid);
		values.put(DB_MSG, driveId);
		values.put(DB_DATEON, Fire.getServerTimestamp());

		try{
			InsertHelper helper = new InsertHelper(rw(), DB_TABLE_DRIVEID);
			helper.replace(values);
			if(helper != null)
				helper.close();
		}catch (SQLiteException e){
		}


		rw().setTransactionSuccessful();
		rw().endTransaction();


		removeDriveImg(uid);

		return true;
	}

	private synchronized boolean removeDriveImg(String uid){
		try{

			//8개 이하로 제거거

		String sql = " DELETE FROM "+ DB_TABLE_DRIVEID +
					" WHERE "+DB_DATEON+" in " +
					"(" +
					"  SELECT "+DB_DATEON+" FROM "+DB_TABLE_DRIVEID+" WHERE "+DB_UID+"=\""+uid + "\" order by "+DB_DATEON+" desc LIMIT 10 offset 8" +
					") ";
			rw().execSQL(sql);
//			rw().delete(DB_TABLE_DRIVEID, sql, null);

		}catch (SQLiteException e){
			LoggerManager.e("mun", "removeDriveImg :" + e.toString());
			return false;
		}

		getDriveImg(uid);
		return true;
	}

	public synchronized ArrayList<String> getDriveImg(String uid){
		ArrayList<String> list = new ArrayList<>();

		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_DRIVEID, null, DB_UID + "=?", new String[] {uid}, null, null, DB_DATEON + " desc");
			if(c.moveToFirst()){
				do{

					String driveid = c.getString(c.getColumnIndex(DB_MSG));

					list.add(driveid);

					LoggerManager.e("mun", "getDriveImg :" + driveid + " / " +c.getLong(c.getColumnIndex(DB_DATEON)));
				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return list;
	}


	/****
	 * drive chat img ids
	 */
	public synchronized boolean addDriveChatImg(String uid, String driveId){

		rw().beginTransaction();


		ContentValues values = new ContentValues();
		values.put(DB_UID, uid);
		values.put(DB_MSG, driveId);
		values.put(DB_DATEON, Fire.getServerTimestamp());

		try{
			InsertHelper helper = new InsertHelper(rw(), DB_TABLE_DRIVECHATID);
			helper.replace(values);
			if(helper != null)
				helper.close();
		}catch (SQLiteException e){
		}


		rw().setTransactionSuccessful();
		rw().endTransaction();


		removeDriveChatImg(uid);

		return true;
	}

	private synchronized boolean removeDriveChatImg(String uid){
		try{

			//8개 이하로 제거거

			String sql = " DELETE FROM "+ DB_TABLE_DRIVECHATID +
					" WHERE "+DB_DATEON+" in " +
					"(" +
					"  SELECT "+DB_DATEON+" FROM "+DB_TABLE_DRIVECHATID+" WHERE "+DB_UID+"=\""+uid + "\" order by "+DB_DATEON+" desc LIMIT 10 offset 8" +
					") ";
			rw().execSQL(sql);
//			rw().delete(DB_TABLE_DRIVECHATID, sql, null);

		}catch (SQLiteException e){
			LoggerManager.e("mun", "removeDriveChatImg :" + e.toString());
			return false;
		}

		getDriveImg(uid);
		return true;
	}


	public synchronized boolean removeDriveChatImgs(String driveid){
		try{

			//8개 이하로 제거거


			String sql = " "+DB_MSG +" = '"+driveid+"' ";
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_COMMENTNO + "=?", new String[] {phone});
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_USERNICK + "=? " + DB_RECORD_FILENAME + "=? ", new String[] {phone,filename});
			rw().delete(DB_TABLE_DRIVECHATID, sql, null);

		}catch (SQLiteException e){
			LoggerManager.e("mun", "removeDriveChatImg :" + e.toString());
			return false;
		}
		return true;
	}
	public synchronized boolean removeDriveChatImgsAll(){
		try{
			String sql = "";//" "+DB_UID +" = '"+uid+"' ";
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_COMMENTNO + "=?", new String[] {phone});
			//rw().delete(DB_TABLE_COMMENT, DB_RECORD_USERNICK + "=? " + DB_RECORD_FILENAME + "=? ", new String[] {phone,filename});
			rw().delete(DB_TABLE_DRIVECHATID, sql, null);

		}catch (SQLiteException e){
			LoggerManager.e("mun", "removeDriveChatImg :" + e.toString());
			return false;
		}
		return true;
	}

	public synchronized ArrayList<String> getDriveChatImg(String uid){
		ArrayList<String> list = new ArrayList<>();

		Cursor c = null;
		try{
			c = ro().query(DB_TABLE_DRIVECHATID, null, DB_UID + "=?", new String[] {uid}, null, null, DB_DATEON + " desc");
			if(c.moveToFirst()){
				do{

					String driveid = c.getString(c.getColumnIndex(DB_MSG));

					list.add(driveid);

					LoggerManager.e("mun", "getDriveChatImg :" + driveid + " / " +c.getLong(c.getColumnIndex(DB_DATEON)));
				} while (c.moveToNext());
			}
		}catch (SQLiteException e){
		}

		if(c != null)
			c.close();

		return list;
	}

}
