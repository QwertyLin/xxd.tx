package cn.xxd.tx.bean;

import java.util.ArrayList;
import java.util.List;

import cn.xxd.tx.util.QSqlite;

import q.util.a.view.QOauth;
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
	private ContentValues buildContentValues(QOauth.Token e){
		ContentValues cv = new ContentValues();
		cv.put("type", e.getType());
		cv.put("id", e.getId());
		cv.put("token", e.getToken());
		cv.put("expire", e.getExpireTime());
		cv.put("name", e.getName());
		cv.put("pic", e.getPic());
		return cv;
	}
	
	/**
	* 构建实体
	*/
	private QOauth.Token buildEntity(Cursor cs){
		QOauth.Token e = new QOauth.Token();
		e.setType(cs.getInt(0));
		e.setId(cs.getString(1));
		e.setToken(cs.getString(2));
		e.setExpireTime(cs.getLong(3));
		e.setName(cs.getString(4));
		e.setPic(cs.getString(5));
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
	
	public void insert(QOauth.Token e) {
	if(queryOne(e) == null){
		db.insert("token", null, buildContentValues(e));
	}else{
		update(e);
	}
	//db.execSQL("INSERT INTO "+DB_TABLE+"()
	}
	
	public boolean update(QOauth.Token e) {
	return db.update("token", buildContentValues(e), "type=" + e.getType() + " AND id=" + e.getId(), null) > 0;
	//db.execSQL("UPDATE "+DB_TABLE+" SET "+KEY_DATA+" = ? WHERE "+KEY_ID+" = ?", new Object[]{e.data, Integer.valueOf(e.id)})
	}
	
	public boolean delete(QOauth.Token e) {
	return db.delete("token", "type=" + e.getType() + " AND id=" + e.getId(), null) > 0;
	//db.execSQL("DELETE FROM "+DB_TABLE+" WHERE "+KEY_ID+" = ?", new Object[]{Integer.valueOf(id)});
	}
	
	public List<QOauth.Token> queryAll() {
	//Cursor cs = db.query(DB_TABLE, new String[] { KEY_ID, KEY_DATA }, null, null, null, null, null);
	Cursor cs = db.rawQuery("SELECT * FROM token", null);
	List<QOauth.Token> es = new ArrayList<QOauth.Token>(cs.getCount());
	int i = 0;
	while(cs.moveToNext()){
		es.add(buildEntity(cs));
	}
	return es;
	}
	
	public QOauth.Token queryOne(QOauth.Token e) throws SQLException {
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