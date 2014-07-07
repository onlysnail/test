package com.example.sqlitedemo;

import com.example.sqlitedemo.MainActivity.StudentModel;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class StudentProvider extends ContentProvider {

	private static final String DATABASE_NAME = "tb_students.db";
	private static final int DATABASE_VERSION = 1;

	static final String AUTHORITY = "com.example.sqlitedemodebug.settings";

	static final String PARAMETER_NOTIFY = "notify";
	static final String TABLE_STUDENTS = "students";

	private DatabaseHelper mOpenHelper;

	/**
	 * 该方法在ContentProvider创建后被调用,当其他应用程序第一次访问ContentProvider时，
	 * 该ContentProvider会被创建出来，并立即回调该onCreate访法。
	 */
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mOpenHelper = new DatabaseHelper(getContext());
		return true;// 返回false 和返回true的区别
	}

	/**
	 * 根据Uri查询出select条件所匹配的全部记录，其中projection就是一个列名列表，表明只选择出指定的数据列.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(args.table);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor result = qb.query(db, projection, args.where, args.args, null,
				null, sortOrder);
		result.setNotificationUri(getContext().getContentResolver(), uri);

		return result;
	}

	/**
	 * 该方法用于返回当前Uri所代表的数据库MIME类型。如果该Uri对应数据可能包括多条记录,那么MIME类型字符串应该以vnd.android.
	 * cursor
	 * .dir/开头:如果该Uri对应的数据只包含一条记录，那么返回MIME类型字符串应该以vnd.android.cursor.item/开头
	 */
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		SqlArguments args = new SqlArguments(uri, null, null);
		if (TextUtils.isEmpty(args.where)) {
			return "vnd.android.cursor.dir/" + args.table;
		} else {
			return "vnd.android.cursor.item/" + args.table;
		}
	}

	/**
	 * 增 根据该Uri插入values对应的数据
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		SqlArguments args = new SqlArguments(uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final long rowId = dbInsertAndCheck(mOpenHelper, db, args.table, null,
				values);
		if (rowId <= 0)
			return null;

		uri = ContentUris.withAppendedId(uri, rowId);
		sendNotify(uri);

		return uri;
	}

	private static long dbInsertAndCheck(DatabaseHelper helper,
			SQLiteDatabase db, String table, String nullColumnHack,
			ContentValues values) {
		// if (!values.containsKey(StudentSettings.Students._ID)) {
		// throw new RuntimeException(
		// "Error: attempting to add item without specifying an id");
		// }
		return db.insert(table, nullColumnHack, values);
	}

	/**
	 * 删 > 根据Uri删除select条件所匹配的全部记录
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = db.delete(args.table, args.where, args.args);
		if (count > 0)
			sendNotify(uri);

		return count;
	}

	/**
	 * 改 > 根据Uri修改select条件锁匹配的全部记录
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = db.update(args.table, values, args.where, args.args);
		if (count > 0)
			sendNotify(uri);
		return count;
	}

	/**
	 * 发送通知
	 * 
	 * @param uri
	 */
	private void sendNotify(Uri uri) {
		String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
		if (notify == null || "true".equals(notify)) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}

	/**
	 * 批量插入
	 */
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		// TODO Auto-generated method stub
		SqlArguments args = new SqlArguments(uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			int numValues = values.length;
			for (int i = 0; i < numValues; i++) {
				if (dbInsertAndCheck(mOpenHelper, db, args.table, null,
						values[i]) < 0) {
					return 0;
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		sendNotify(uri);
		return values.length;
	}

	/**
	 * 
	 * @author 李晓强 数据库
	 */
	class DatabaseHelper extends SQLiteOpenHelper {

		private long mMaxId = -1;

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
			// In the case where neither onCreate nor onUpgrade gets called, we
			// read the maxId from
			// the DB here
			if (mMaxId == -1) {
				mMaxId = initializeMaxId(getWritableDatabase());
			}
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			mMaxId = 1;
			db.execSQL("CREATE TABLE students(_id INTEGER PRIMARY KEY,name TEXT,age INTEGER);");
			Log.d(StudentModel.TAG, "创建数据库");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			/**
			 * 以下代码拷贝的Launcher4.03部分代码，紧供学习之用。与项目无关。
			 */
			/*
			 * // 旧版本 int version = oldVersion; if (version < 9) { // The max id
			 * is not yet set at this point (onUpgrade is // triggered in the
			 * ctor // before it gets a change to get set, so we need to read it
			 * // here when we use it) if (mMaxId == -1) { mMaxId =
			 * initializeMaxId(db); }
			 * 
			 * // Add default hotseat icons version = 9; }
			 * 
			 * if (version != DATABASE_VERSION) { // "Destroying all old data;
			 * db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
			 * onCreate(db); }
			 */
		}

		// Generates a new ID to use for an object in your database. This method
		// should be only
		// called from the main UI thread. As an exception, we do call it when
		// we call the
		// constructor from the worker thread; however, this doesn't extend
		// until after the
		// constructor is called, and we only pass a reference to
		// LauncherProvider to LauncherApp
		// after that point
		public long generateNewId() {
			if (mMaxId < 0) {
				throw new RuntimeException("Error: max id was not initialized");
			}
			mMaxId += 1;
			return mMaxId;
		}

		// 查询最大的Id
		private long initializeMaxId(SQLiteDatabase db) {
			Cursor c = db.rawQuery("SELECT MAX(_id) FROM students", null);

			// get the result
			final int maxIdIndex = 0;
			long id = -1;
			if (c != null && c.moveToNext()) {
				id = c.getLong(maxIdIndex);
			}
			if (c != null) {
				c.close();
			}

			if (id == -1) {
				throw new RuntimeException("Error: could not query max id");
			}

			return id;
		}
	}

	static class SqlArguments {
		public final String table;
		public final String where;
		public final String[] args;

		SqlArguments(Uri url, String where, String[] args) {
			// 各自打印的值
			// content://com.example.sqlitedemodebug.settings/students?notify=true
			// System.out.println(url);
			
			// students
			// System.out.println(url.getPath());
			
			// com.example.sqlitedemodebug.settings
			// System.out.println(url.getAuthority());
			
			// [students]
			// System.out.println(url.getPathSegments());
			
			// 1
			// System.out.println(url.getPathSegments().size());
			
			// students
			// System.out.println(url.getPathSegments().get(0));
			
			if (url.getPathSegments().size() == 1) {
				this.table = url.getPathSegments().get(0);
				this.where = where;
				this.args = args;
			} else if (url.getPathSegments().size() != 2) {
				throw new IllegalArgumentException("Invalid URI: " + url);
			} else if (!TextUtils.isEmpty(where)) {
				throw new UnsupportedOperationException(
						"WHERE clause not supported: " + url);
			} else {
				this.table = url.getPathSegments().get(0);
				this.where = "_id=" + ContentUris.parseId(url);
				this.args = null;
			}
		}

		SqlArguments(Uri url) {
			if (url.getPathSegments().size() == 1) {
				table = url.getPathSegments().get(0);
				where = null;
				args = null;
			} else {
				throw new IllegalArgumentException("Invalid URI: " + url);
			}
		}
	}
}
