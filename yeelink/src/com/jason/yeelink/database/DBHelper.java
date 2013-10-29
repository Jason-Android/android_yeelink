package com.jason.yeelink.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	// 数据库名称和版本号
	private static final String DATABASE_NAME = "YeeLinkDB";
	private static final int DATABASE_VERSION = 1;
	
	private static final String CREATE_TABLE_USER_SQL=
			"create table Tb_user(user_id INTEGER PRIMARY KEY AUTOINCREMENT,username nvarchar(64) not null unique,password nvarchar(64) not null,apikey nvarchar(32) not null unique)";
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//首次创建数据库
		db.execSQL(CREATE_TABLE_USER_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		//更新数据库
		db.execSQL("DROP TABLE IF EXISTS Tb_user");
		onCreate(db);
	}

}
