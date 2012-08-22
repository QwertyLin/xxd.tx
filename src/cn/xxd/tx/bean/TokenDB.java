package cn.xxd.tx.bean;

import java.util.ArrayList;
import java.util.List;

import cn.xxd.tx.util.QSqlite;

import q.util.a.view.QLayoutOauth;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TokenDB  {
		
	// 创建表
	public static final String DB_CREATE = "CREATE TABLE token ("
		+ "type INTEGER," 
		+ "id TEXT,"
		+ "token TEXT," 
		+ "expire LONG," 
		+ "name TEXT," 
		+ "pic TEXT" 
		+ ")";
	
	/**
	* 构建ContentValues
	*/
	private ContentValues buildContentValues(QLayoutOauth.Token e){
		ContentValues cv = new ContentValues();
		cv.put("type", e.getType());
		cv.put("id", e.getId());
		cv.put("token", e.getToken());
		cv.put("expire", e.getExpireTime());
		cv.put("name", e.getName());
		cv.put("pic", e.getPhoto());
		return cv;
	}
	
	/**
	* 构建实体
	*/
	private QLayoutOauth.Token buildEntity(Cursor cs){
		QLayoutOauth.Token e = new QLayoutOauth.Token();
		e.setType(cs.getString(0));
		e.setId(cs.getString(1));
		e.setToken(cs.getString(2));
		e.setExpireTime(cs.getLong(3));
		e.setName(cs.getString(4));
		e.setPhoto(cs.getString(5));
		return e;
	}
	
	public TokenDB(Context ctx){
	dbHelper = new QSqlite(ctx);
	}
	
	public void open(boolean writable) throws SQLException {
	if(writable){
		db = dbHelper.getWritableDatabase();
	}else{
		db = dbHelper.getReadableDatabase();
	}
	}
	
	public void close(){
	dbHelper.close();
	}
	
	public void insert(QLayoutOauth.Token e) {
	if(queryOne(e) == null){
		db.insert("token", null, buildContentValues(e));
	}else{
		update(e);
	}
	//db.execSQL("INSERT INTO "+DB_TABLE+"()
	}
	
	public boolean update(QLayoutOauth.Token e) {
	return db.update("token", buildContentValues(e), "type=" + e.getType() + " AND id=" + e.getId(), null) > 0;
	//db.execSQL("UPDATE "+DB_TABLE+" SET "+KEY_DATA+" = ? WHERE "+KEY_ID+" = ?", new Object[]{e.data, Integer.valueOf(e.id)})
	}
	
	public boolean delete(QLayoutOauth.Token e) {
	return db.delete("token", "type=" + e.getType() + " AND id=" + e.getId(), null) > 0;
	//db.execSQL("DELETE FROM "+DB_TABLE+" WHERE "+KEY_ID+" = ?", new Object[]{Integer.valueOf(id)});
	}
	
	public List<QLayoutOauth.Token> queryAll() {
	//Cursor cs = db.query(DB_TABLE, new String[] { KEY_ID, KEY_DATA }, null, null, null, null, null);
	Cursor cs = db.rawQuery("SELECT * FROM token", null);
	List<QLayoutOauth.Token> es = new ArrayList<QLayoutOauth.Token>(cs.getCount());
	int i = 0;
	while(cs.moveToNext()){
		es.add(buildEntity(cs));
	}
	return es;
	}
	
	public QLayoutOauth.Token queryOne(QLayoutOauth.Token e) throws SQLException {
	//Cursor cs = db.query(true, DB_TABLE, new String[] { KEY_ID, KEY_DATA }, KEY_ID + "=" + id, null, null, null,null, null);
	Cursor cs = db.rawQuery("SELECT * FROM token WHERE type = ? AND id = ?", new String[]{String.valueOf(e.getType()), e.getId()});
	if(cs.moveToNext()){
		return buildEntity(cs);
	}else{
		return null;
	}
	}
	
	private SQLiteDatabase db; // 执行open（）打开数据库时，保存返回的数据库对象
	private SQLiteOpenHelper dbHelper;// 由SQLiteOpenHelper继承过来
	
}