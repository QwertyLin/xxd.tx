package cn.xxd.tx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


import cn.xxd.tx.adapter.FriendAdapter;
import cn.xxd.tx.bean.Friend;
import cn.xxd.tx.util.QActivity;
import cn.xxd.tx.util.QApp;

import q.frame.QDialog;
import q.frame.QLayout;
import q.util.QFile;
import q.util.QHttp;
import q.util.QLog;
import q.util.a.QPinyin;
import q.util.a.QPinyin.IPinyin;
import q.util.a.QPinyin.Pinyin;
import q.frame.layout.QLayoutOauth;
import q.frame.layout.QLayoutOauth.Token;
import q.frame.layout.QLayoutOauth.UnAuthException;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import api.QQWeiboParser;
import api.SinaParser;

//先比较本地版本，

public class FriendA extends QActivity implements OnClickListener, OnItemClickListener, OnCheckedChangeListener {
				
	private int friendPageCount, friendPageCountTemp, friendCount, friendCountTemp;
	private QPinyin<Friend> qPinyin;
	private QHttp qhttp, qhttp2;
	private QLayoutOauth.Token token;
	private boolean isByName = true; //true为按name排序，false为按remark排序

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		if(getQApp().isFromOut()){
			getQApp().getQActivityCache().put(this);
		}
		//
		getSupportActionBar().setTitle("好友列表");
		//
		//
		token = getQApp().getToken();
		//
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		//
		RadioGroup rg = (RadioGroup)getLayoutInflater().inflate(R.layout.friend_order, null);
		rg.setOnCheckedChangeListener(this);
		//
		getSupportActionBar().setCustomView(rg);
		//
		setContentView(R.layout.friend);
		//
		pb = (ProgressBar)findViewById(R.id.friend_pb);
		((RelativeLayout)findViewById(R.id.layout)).addView(new QLayout.Loading(this, "加载中"), 0);
		
		//
		QLog.log(token.getType() + "_" + token.getId());
		qhttp = new QHttp(this, 0, 60 * 24 * 30, new QHttp.CallbackString() {
			
			@Override
			public void onError(IOException e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted(String obj) {
				try {
					switch(token.getType()){
					case QLayoutOauth.TYPE_SINA_WEIBO:
						onGetCountSuccess(SinaParser.usersShowFriendsCount(obj));
						break;
					case QLayoutOauth.TYPE_QQ_WEIBO:
						onGetCountSuccess(QQWeiboParser.usersShowFriendsCount(obj));
						break;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		qhttp2 = new QHttp(this, 5, 60 * 24 * 30, new QHttp.CallbackString() {
			
			@Override
			public void onError(IOException e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted(String obj) {
				List<Friend> data = null;
				try {
					switch(token.getType()){
					case QLayoutOauth.TYPE_SINA_WEIBO:
						data = SinaParser.friendshipsFriends((String)obj, !isByName);
						break;
					case QLayoutOauth.TYPE_QQ_WEIBO:
						data = QQWeiboParser.friendshipsFriends((String)obj);
						break;
					}
					onGetIndexSuccess(data);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		//
		/*findViewById(R.id.friend_down).setOnClickListener(this);
		pb = (ProgressBar)findViewById(R.id.friend_pb);
		pbTv = (TextView)findViewById(R.id.friend_pb_text);*/
		
		//
		datas = new ArrayList<QPinyin<Friend>.Pinyin>();
		adapter = new FriendAdapter(this, datas);
		qList = new QListViewPinyin(this, adapter);
		qList.setBackgroundColor(0xFFFFFFFF);
		adapter.setListView(qList.getListView());
		qList.setOnItemClickListener(this);
		
		((FrameLayout)findViewById(R.id.friend_layout_lv)).addView(qList);
		//
		new Thread(){
			public void run() {
				SystemClock.sleep(200);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						onClickRefresh();
					}
				});
			};
		}.start();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menuRefresh = menu.add("刷新");
		menuRefresh.setIcon(R.drawable.a_navigation_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item == menuRefresh){
			onClickRefresh();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		startActivity(new Intent(this, PhotoA.class).putExtra(PhotoA.EXTRA_IMG, datas.get(position).getObj().getPhotoBig()));
	}
	
	@Override
	public void onCheckedChanged(RadioGroup rg, int checkedId) {
		boolean isSelectNameTemp = false;
		switch(checkedId){
		case R.id.friend_order_name:
			isSelectNameTemp = true;
			break;
		case R.id.friend_order_remark:
			isSelectNameTemp = false;
			break;
		}
		final boolean isSelectName = isSelectNameTemp;
		if( (isSelectName && datasByName != null) || (!isSelectName && datasByRemark != null)){
			datas.clear();
			if(isSelectName){
				datas.addAll(datasByName);
			}else{
				datas.addAll(datasByRemark);
			}
			qList.refreshLetter();
			qList.getListView().scrollTo(0, 0);
			adapter.notifyDataSetChanged();
		} else {
			qList.setVisibility(View.INVISIBLE);
			new Thread(){
				public void run() {
					QPinyin<Friend> qPinyin = new QPinyin<Friend>();
					Friend oldFriend;
					Friend newFriend;
					List<Friend> newDatas = new ArrayList<Friend>();
					for(QPinyin<Friend>.Pinyin item : datas){
						oldFriend = item.getObj();
						if(oldFriend != null){
							newFriend = oldFriend.clone();
							if(oldFriend.getRemark() != null && !oldFriend.getRemark().equals("")){
								newFriend.setName(oldFriend.getRemark());
								newFriend.setRemark(oldFriend.getName());
							}
							newDatas.add(newFriend);
						}
					}
					qPinyin.add(newDatas);
					if(isSelectName){
						datasByName = qPinyin.order();
					}else{
						datasByRemark = qPinyin.order();
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							datas.clear();
							if(isSelectName){
								datas.addAll(datasByName);
							}else{
								datas.addAll(datasByRemark);
							}
							qList.setVisibility(View.VISIBLE);
							qList.refreshLetter();
							qList.getListView().scrollTo(0, 0);
							adapter.notifyDataSetChanged();
						}
					});
				};
			}.start();
		}
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
		}
	}
	
	private void onClickRefresh(){
		qList.setVisibility(View.INVISIBLE);
		qPinyin = new QPinyin<Friend>();
		datas.clear();
		datasByName = null;
		datasByRemark = null;
		adapter.notifyDataSetChanged();
		friendPageCountTemp = 0;
		onGetCount();
	}
	
	private void onGetCount(){
		QLog.log("onGetCount");
		switch(token.getType()){
		case QLayoutOauth.TYPE_SINA_WEIBO:
			qhttp.get(QLayoutOauth.HandleSinaWeibo.urlUsersShow(token));
			break;
		case QLayoutOauth.TYPE_QQ_WEIBO:
			qhttp.get(QLayoutOauth.HandleQQWeibo.urlUserInfoSimple(token, token.getId()));
			break;
		}
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
				switch(token.getType()){
				case QLayoutOauth.TYPE_SINA_WEIBO:
					qhttp2.get(QLayoutOauth.HandleSinaWeibo.urlFriends(token, 50, i * 50));
					break;
				case QLayoutOauth.TYPE_QQ_WEIBO:
					qhttp2.get(QLayoutOauth.HandleQQWeibo.urlFriendsIdolistSimple(token, 50, i * 50));
					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void onGetIndexSuccess(final List<Friend> data){		
		new Thread(){
			public void run() {
				qPinyin.add(data);
				final List<QPinyin<Friend>.Pinyin> list = qPinyin.order();
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						datas.clear();
						datas.addAll(list);
						friendPageCountTemp++;
						pb.setProgress(friendPageCountTemp * 100 / friendPageCount);
						adapter.notifyDataSetChanged();
						pb.setVisibility(View.VISIBLE);
						qList.setVisibility(View.VISIBLE);
						qList.refreshLetter();
						if(friendPageCountTemp == friendPageCount){
							pb.setProgress(0);
							pb.setVisibility(View.GONE);
							if(qhttp.getCacheExpire() != 0L || qhttp2.getCacheExpire() != 0){
								qhttp.setCacheExpire(0L);
								qhttp2.setCacheExpire(0L);
							}
							//
							if(isByName){
								datasByName = new ArrayList<QPinyin<Friend>.Pinyin>();
								datasByName.addAll(datas);
							}else{
								datasByRemark = new ArrayList<QPinyin<Friend>.Pinyin>();
								datasByRemark.addAll(datas);
							}
						}
					}
				});
			};
		}.start();
		//
		
	}
		
	private ProgressBar pb;
	private List<QPinyin<Friend>.Pinyin> datas; //当其中一个按name排序时，另一个就按remark排序
	private List<QPinyin<Friend>.Pinyin> datasByName, datasByRemark;
	private QListViewPinyin qList;
	private FriendAdapter adapter;
	private MenuItem menuRefresh;

	
	
	
	
}
