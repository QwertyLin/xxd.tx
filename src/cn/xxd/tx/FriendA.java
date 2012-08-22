package cn.xxd.tx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;


import cn.xxd.tx.adapter.FriendAdapter;
import cn.xxd.tx.bean.Friend;
import cn.xxd.tx.util.QConfig;

import q.frame.QDialog;
import q.util.QFile;
import q.util.QHttp;
import q.util.QLog;
import q.util.a.QPinyin;
import q.util.a.QPinyin.IPinyin;
import q.util.a.QPinyin.Pinyin;
import q.util.a.view.QLayoutOauth;
import q.util.a.view.QLayoutOauth.Token;
import q.util.a.view.QLayoutOauth.UnAuthException;
import q.util.a.view.QListViewPinyin;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import api.SinaParser;

//先比较本地版本，

public class FriendA extends Activity implements OnClickListener, OnItemClickListener {
	
	private int friendPageCount, friendPageCountTemp, friendCount, friendCountTemp;
	private QPinyin<Friend> qPinyin = new QPinyin<Friend>();
	private QHttp qhttp, qhttp2;
	private String cacheDir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend);
		cacheDir = QFile.get(QConfig.TOKEN.getType() + "_" + QConfig.TOKEN.getId());
		//
		QLog.log(QConfig.TOKEN.getToken() + " " + QConfig.TOKEN.getId());
		qhttp = new QHttp(0, cacheDir, 60 * 24 * 30, new QHttp.CallbackString() {
			
			@Override
			public void onError(IOException e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted(String obj) {
				try {
					onGetCountSuccess(SinaParser.usersShowFriendsCount(obj));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		qhttp2 = new QHttp(5, cacheDir, 60 * 24 * 30, new QHttp.CallbackString() {
			
			@Override
			public void onError(IOException e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted(String obj) {
				List<Friend> data;
				try {
					data = SinaParser.friendshipsFriends((String)obj);
					onGetIndexSuccess(data);
					dialogLoading.cancel();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		//
		findViewById(R.id.friend_down).setOnClickListener(this);
		pb = (ProgressBar)findViewById(R.id.friend_pb);
		pbTv = (TextView)findViewById(R.id.friend_pb_text);
		//
		dialogLoading = new QDialog.Loading(this);
		dialogLoading.show();
		new Thread(){
			public void run() {
				SystemClock.sleep(200);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						onClickDown();
					}
				});
			};
		}.start();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Dialog d = new Dialog(this);
		d.setContentView(R.layout.photo);
		/*qhttp.getBitmap(datas.get(position).getObj().getPic().replace("/50/", "/180/"), new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case 0:
					((ImageView)d.findViewById(R.id.photo_pic)).setImageBitmap((Bitmap)msg.obj);
					break;
				}
				super.handleMessage(msg);
			}
		});*/
		d.show();
		//startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
        	final Uri uriRet = data.getData(); 
            if(uriRet != null) 
            { 
              try 
              { 
                /* 必须要有android.permission.READ_CONTACTS权限 */ 
                Cursor c = managedQuery(uriRet, null, null, null, null); 
                /*将Cursor移到资料最前端*/ 
                c.moveToFirst(); 
                /*取得联络人的姓名*/ 
                String strName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)); 
                /*将姓名写入EditText01中*/ 
               ///mEditText01.setText(strName); 
                /*取得联络人的电话*/ 
                int contactId = c.getInt(c.getColumnIndex(ContactsContract.Contacts._ID)); 
                Cursor phones = getContentResolver().query ( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null ); 
                StringBuffer sb = new StringBuffer(); 
                int typePhone, resType; 
                String numPhone; 
                if (phones.getCount() > 0) 
                { 
                  phones.moveToFirst(); 
                  /* 2.0可以允许User设定多组电话号码，但本范例只捞一组电话号码作示范 */ 
                  typePhone = phones.getInt ( phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE) ); 
                  numPhone = phones.getString ( phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) ); 
                  resType = ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(typePhone); 
                  sb.append(getString(resType) +": "+ numPhone +"/n"); 
                  /*将电话写入EditText02中*/ 
                 // mEditText02.setText(numPhone); 
                  } 
                else 
                { 
                  sb.append("no Phone number found"); 
                  } 
                /*Toast是否读取到完整的电话种类与电话号码*/ 
                Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show(); 
                } 
              catch(Exception e) 
              { 
                /*将错误信息在TextView中显示*/ 
               // mTextView01.setText(e.toString()); 
                e.printStackTrace(); 
                } 
              } 
        }
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.friend_down:
			onClickDown();
			break;
		}
	}
	
	private void onClickDown(){
		friendPageCountTemp = 0;
		onGetCount();
	}
	
	private void onGetCount(){
		QLog.log("onGetCount");
		qhttp.get(QLayoutOauth.HandleSinaWeibo.urlUsersShow(QConfig.TOKEN));
	}
	
	private void onGetCountSuccess(int friendsCount){
		QLog.log("onGetCountSuccess");
		friendPageCount = friendsCount / 50;;
		if(friendsCount % 50 != 0){
			friendPageCount++;
		}
		onGetIndex();
	}
	
	private void onGetCountError(){
		
	}
	
	private void onGetIndex(){
		QLog.log("onGetIndex 线程需求：" + friendPageCount);
		for(int i = 0; i < friendPageCount; i++){
			try {
				qhttp2.get(QLayoutOauth.HandleSinaWeibo.urlFriends(QConfig.TOKEN, 50, i * 50));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void onGetIndexSuccess(List<Friend> data){
		friendPageCountTemp++;
		int percent = friendPageCountTemp * 100 / friendPageCount;
		pb.setProgress(percent);
		pbTv.setText("正在下载好友列表 " + percent + "%");
		//
		qPinyin.add(data);
		datas.clear();
		datas.addAll(qPinyin.order());
		if(adapter == null){
			System.out.println("======" + datas.size());
			adapter = new FriendAdapter(this, datas, handler, cacheDir);
			qList = new QListViewPinyin(this, adapter);
			adapter.setListView(qList.getListView());
			qList.setOnItemClickListener(this);
			((FrameLayout)findViewById(R.id.friend_layout_lv)).addView(qList);
		}
		qList.refreshLetter();
		adapter.notifyDataSetChanged();
		//
		if(friendPageCount == friendPageCountTemp){
		}
	}
	List<QPinyin<Friend>.Pinyin> datas = new ArrayList<QPinyin<Friend>.Pinyin>();
	
	
	private final int
		MSG_GET_COUNT_ERROR = 2;
	public static final int 
		MSG_REFRESH_LIST = 5;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_REFRESH_LIST:
				adapter.notifyDataSetChanged();
				break;
			}
		};
	};
	
	private Dialog d;
	private Dialog dialogLoading;
	private ProgressBar pb;
	private TextView pbTv;
	private QListViewPinyin qList;
	private FriendAdapter adapter;

	
	
}
