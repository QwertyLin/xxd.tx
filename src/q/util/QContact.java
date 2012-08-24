package q.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * @author Q
 * 
 * 
 */
public final class QContact {
	
	/**
	 * 获取通讯录中的联系人，数据库：data/data/com.android.providers.contacts/databases/contacts2.db
	 * <br/>需要权限<uses-permission android:name="android.permission.READ_CONTACTS" />
	 * 
	 * @param ctx
	 * @return
	 */
	public static final List<Contact> findAllContacts(Context ctx){
		final String[] PROJECTION = new String[]{
				ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.PHOTO_ID,
		};
		List<Contact> list = new ArrayList<QContact.Contact>();
		//得到ContentResolver对象
		ContentResolver cr = ctx.getContentResolver();
		//取得电话本中开始一项的光标
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, null, null, null);
		if(cursor != null) {
			while(cursor.moveToNext()){
				//联系人模型
				Contact item = new Contact();
				// 取得联系人ID
				long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				item.id = contactId;
				// 取得联系人名字
				item.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				// 取得头像ID
				long photoId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
				if(photoId > 0 ){
					item.photo = BitmapFactory.decodeStream(ContactsContract.Contacts.openContactPhotoInputStream(cr, ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)));
				}
				//Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+contactId, null, null);
				//取得电话号码，可能存在多个
				/*List<String> phones = new ArrayList<String>();
				while(phone.moveToNext()){
					phones.add(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				}*/
				//entity.phones = phones;
				//释放连接
				//phone.close();
				list.add(item);
			}
			//释放连接
			cursor.close();
		}
		//验证
		/*for(Contact e : list){
			Log.e("Q", e.getName());
			for(String str : e.getPhones()){
				Log.e("Q", str);
			}
		}*/
		return list;
	}
	
	public static class Contact{
		public long id;
		public String name; //姓名
		public Bitmap photo; //头像
	}
	
	/**
	 * 更新头像
	 * 
	 * @param contactId 联系人id
	 * @param bitmapData 图片
	 */
	public static final void updatePhoto(Context ctx, long contactId, byte[] bitmapData) {
		ContentResolver cr = ctx.getContentResolver();
		ContentValues values = new ContentValues();  
        Uri u = Uri.parse("content://com.android.contacts/data");  
        int photoRow = -1;  
        String where ="raw_contact_id = " + contactId + " AND mimetype ='vnd.android.cursor.item/photo'";  
        Cursor cursor = cr.query(u, null, where, null, null);  
        int idIdx = cursor.getColumnIndexOrThrow("_id");  
        if (cursor.moveToFirst()) {  
            photoRow = cursor.getInt(idIdx);  
        }  
        cursor.close();  
        values.put("raw_contact_id", contactId);  
        values.put("is_super_primary", 1);  
        values.put("data15", bitmapData);  
        values.put("mimetype","vnd.android.cursor.item/photo");  
        if (photoRow >= 0) {  
        	cr.update(u, values, " _id= " + photoRow, null);  
        } else {  
        	cr.insert(u, values);  
        }  
	}

}


