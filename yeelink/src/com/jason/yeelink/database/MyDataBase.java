package com.jason.yeelink.database;

import java.util.LinkedList;
import java.util.List;

import com.jason.yeelink.base.User;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MyDataBase {
	
	public static final String DB_NAME="Tb_user";
	
	/*
	 * 删除一个用户记录
	 */
	public static void UserDelete(SQLiteDatabase db,User user){
		db.execSQL("delete from " + DB_NAME + " where username='"+user.getUserName()+"'");
	}
	
	/*
	 * 增加一个用户的记录
	 */
	public static void UserAdd(SQLiteDatabase db,User user){
		db.execSQL("insert into "+DB_NAME+" (username, password, apikey) values ('"+user.getUserName()+"','"+user.getPassword()+"','"+user.getApikey()+"')");
	}
	/*
	 * 更新一条用户记录
	 */
	public static void UserUpdate(SQLiteDatabase db,User user) {
		db.execSQL("update "+DB_NAME+" set password='"+user.getPassword()+"', apikey='"+user.getApikey()+"' where username='"+user.getUserName()+"'");
	}
	/*
	 * 查询所有用户信息
	 */
	public static List<User> queryAllUser(SQLiteDatabase db){
		Cursor cursor=db.rawQuery("select username,password,apikey from "+DB_NAME+"", null);
		List<User> list=new LinkedList<User>();
		while(cursor.moveToNext()){
			list.add(new User(cursor.getString(0),cursor.getString(1),cursor.getString(2)));
		}
		return list;
	}
	
	/*
	 * 根据用户名查询用户是否存在表中
	 */
	public static boolean queryUser(SQLiteDatabase db,User user){
		Cursor cursor=db.rawQuery("select * from "+DB_NAME+" where username='"+user.getUserName()+"'", null);
		if(cursor.moveToNext()){
			return true;
		}
		else{
			return false;
		}
	}
	
	/*
	 * 根据用户名获取从数据库中获取一个用户实例
	 */
	public static User getUser(SQLiteDatabase db,String userName){
		User user=null;
		Cursor cursor=db.rawQuery("select * from "+DB_NAME+" where username='"+userName+"'", null);
		if(cursor.moveToNext()){
			user=new User(cursor.getString(1),cursor.getString(2),cursor.getString(3));
		}
		return user;
	}
}
