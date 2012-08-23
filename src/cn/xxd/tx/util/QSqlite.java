package cn.xxd.tx.util;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class QSqlite extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "db.db";	//数据库名称
	private static final int DB_VERSION = 1; 			//数据库版本 
	   	
	//调用getWritableDatabase()或 getReadableDatabase()方法时创建数据库
	public QSqlite(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	// 函数在数据库第一次建立时被调用， 一般用来用来创建数据库中的表，并做适当的初始化工作
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("QSqlite", "onCreate");
		//可插入多个
		//db.execSQL(Demo.DB_CREATE);
	}

	//数据库版本号DB_VERSION发生变化时调用
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("QSqlite", "onUpgrade");
		//插入多个数据表的变化
		//db.execSQL("DROP TABLE IF EXISTS " + Table1.DB_TABLE);
		onCreate(db);
	}

	public static class Demo  {
				
		public Demo(Context ctx){
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
		
		// 创建表
		public static final String DB_CREATE = "CREATE TABLE tableName ("
				+ "id INTEGER PRIMARY KEY," 
				+ "name TEXT" 
				+ ")";
		
		/**
		 * 构建ContentValues
		 */
		private ContentValues buildContentValues(Entity e){
			ContentValues cv = new ContentValues();
			cv.put("id", e.id);
			cv.put("name", e.name);
			return cv;
		}
		
		/**
		 * 构建实体
		 */
		private Entity buildEntity(Cursor cs){
			Entity e = new Entity();
			e.id = cs.getInt(0);
			e.name = cs.getString(1);
			return e;
		}
		
		public long insert(Entity e) {
			return db.insert("tableName", "id", buildContentValues(e));
			//db.execSQL("INSERT INTO "+DB_TABLE+"()
		}
		
		public boolean update(Entity e) {
			return db.update("tableName", buildContentValues(e), "id=" + e.id, null) > 0;
			//db.execSQL("UPDATE "+DB_TABLE+" SET "+KEY_DATA+" = ? WHERE "+KEY_ID+" = ?", new Object[]{e.data, Integer.valueOf(e.id)})
		}

		public boolean delete(int id) {
			return db.delete("tableName", "id=" + id, null) > 0;
			//db.execSQL("DELETE FROM "+DB_TABLE+" WHERE "+KEY_ID+" = ?", new Object[]{Integer.valueOf(id)});
		}

		public Entity[] queryAll() {
			//Cursor cs = db.query(DB_TABLE, new String[] { KEY_ID, KEY_DATA }, null, null, null, null, null);
			Cursor cs = db.rawQuery("SELECT * FROM tableName", null);
			Entity[] es = new Entity[cs.getCount()];
			int i = 0;
			while(cs.moveToNext()){
				es[i++] = buildEntity(cs);
			}
			return es;
		}

		public Entity queryOne(int id) throws SQLException {
			//Cursor cs = db.query(true, DB_TABLE, new String[] { KEY_ID, KEY_DATA }, KEY_ID + "=" + id, null, null, null,null, null);
			Cursor cs = db.rawQuery("SELECT * FROM tableName WHERE id= ?", new String[]{String.valueOf(id)});
			if(cs.moveToNext()){
				return buildEntity(cs);
			}else{
				return null;
			}
		}

		private SQLiteDatabase db; // 执行open（）打开数据库时，保存返回的数据库对象
		private SQLiteOpenHelper dbHelper;// 由SQLiteOpenHelper继承过来
		
		/**
		 * 假设的实体
		 */
		public class Entity{
			public long id;
			public String name;
		}
	}

}
