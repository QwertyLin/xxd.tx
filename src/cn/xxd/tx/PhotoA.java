package cn.xxd.tx;

import java.io.File;
import java.io.IOException;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cn.xxd.tx.util.QActivity;
import cn.xxd.tx.util.QApp;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import q.frame.QLayout;
import q.util.QBitmap;
import q.util.QContact;
import q.util.QHttp;
import q.util.QLog;

public class PhotoA extends QActivity {
	
	public static String EXTRA_IMG = "img";
	private String filePath;
	private Bitmap bm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		//
		filePath = getIntent().getStringExtra(EXTRA_IMG);
		//
		setContentView(R.layout.friend_photo);
		 ((RelativeLayout)findViewById(R.id.layout)).addView(new QLayout.Loading(this, "加载中"), 0);
		
		new QHttp(this, 0, getQApp().getCacheExpirePhoto(), new QHttp.CallbackBitmap() {
			
			@Override
			public void onError(IOException e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted(Bitmap bm) {
				((ImageView)findViewById(R.id.friend_photo)).setImageBitmap(bm);
				PhotoA.this.bm = bm;
			}
		}).get(filePath);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(getQApp().isFromOut()){
			menu.add(0, 1, 0, "确定")
	    	.setIcon(R.drawable.a_content_save)
	        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}else{
			menu.add(0, 1, 0, "设置为通讯录头像")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case 1:
			onSelectSave();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void onSelectSave(){
		String file = new QHttp(this, 0, 0, null).getFilePath(filePath);
		QLog.log(file);
		if(getQApp().isFromOut()){
			finish();
			QLog.log(getQApp().getQActivityCache().get(0).getClass().getName());
			getQApp().getQActivityCache().get(0).setResult(RESULT_OK, new Intent().setData(Uri.parse("file://" + file)));
			getQApp().getQActivityCache().clear();
			getQApp().setFromOut(false);//只要点击了设置，就还原，以免中途退出时影响下次使用
		}else{
			Intent intent = new Intent();
	        intent.setAction(Intent.ACTION_PICK);
	        intent.setData(ContactsContract.Contacts.CONTENT_URI);
	        startActivityForResult(intent, 0);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }    
            
           	//
           	View v = getLayoutInflater().inflate(R.layout.photo_save, null);
           	((ImageView)v.findViewById(R.id.photo_save_new)).setImageBitmap(bm);
           	((ImageView)v.findViewById(R.id.photo_save_old)).setImageBitmap(bm);
           	new AlertDialog.Builder(this)
           	.setView(v)
           	.setMessage("确定设置为<>的头像吗？")
           	.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Uri result = data.getData();
		            String contactId = result.getLastPathSegment();
		            String contactName = getPhoneContacts(contactId);
		           	QLog.log(contactId + " " + contactName);
		           	QContact.updatePhoto(PhotoA.this, Long.parseLong(contactId), QBitmap.toByte(bm));
		           	finish();
				}
           	})
           	.setNegativeButton(R.string.dialog_cancel, null)
           	.show();
        }
	}
	
	private String getPhoneContacts(String contactId) {
        Cursor cursor = null;
        String name = "";
        try {
            Uri uri = People.CONTENT_URI;
            cursor = getContentResolver().query(uri, null, People._ID + "=?",new String[] { contactId },null);
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(People.NAME));
                //Toast.makeText(this, name, Toast.LENGTH_LONG).show();
            } else {
                 Toast.makeText(this, "No contact found.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
               name = "";
               e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return name;
    }

	
}
