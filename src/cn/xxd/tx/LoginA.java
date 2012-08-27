package cn.xxd.tx;

import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import q.util.QLog;
import q.util.a.view.QListView;
import q.frame.layout.QLayoutOauth;
import cn.xxd.tx.adapter.LoginAdapter;
import cn.xxd.tx.util.QActivity;
import cn.xxd.tx.util.QApp;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ListView;

public class LoginA extends QActivity implements OnClickListener {
	
	List<QLayoutOauth.Token> listToken;
	LoginAdapter adapter;
	ListView lv;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		if(getQApp().isFromOut()){
			getQApp().getQActivityCache().put(this);
		}
		//
		setContentView(R.layout.login);
		//
		lv = (ListView)findViewById(R.id.login_token);
		QListView.init(this, lv);
		
		//
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		QLayoutOauth.TokenDB db = new QLayoutOauth.TokenDB(this);
		db.open(false);
		if(listToken == null){
			listToken = db.queryAll();
			adapter = new LoginAdapter(this, listToken, lv);
			lv.setAdapter(adapter);
		}else{
			listToken.clear();
			listToken.addAll(db.queryAll());
			adapter.notifyDataSetChanged();
		}
		db.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu sub = menu.addSubMenu("绑定帐号");
		MenuItem item = sub.getItem();
		item.setIcon(R.drawable.a_social_add_person);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		//
		sub.add("新浪微博").setIcon(R.drawable.logo_sinaweibo).setIntent(new Intent(this, OauthA.class).putExtra(OauthA.EXTRA_TYPE, QLayoutOauth.TYPE_SINA_WEIBO));
		sub.add("腾讯微博").setIcon(R.drawable.logo_qqweibo).setIntent(new Intent(this, OauthA.class).putExtra(OauthA.EXTRA_TYPE, QLayoutOauth.TYPE_QQ_WEIBO));
		sub.add("QQ空间").setIcon(R.drawable.logo_qqzone).setIntent(new Intent(this, OauthA.class).putExtra(OauthA.EXTRA_TYPE, QLayoutOauth.TYPE_QQ_ZONE));
		sub.add("人人网").setIcon(R.drawable.logo_renren).setIntent(new Intent(this, OauthA.class).putExtra(OauthA.EXTRA_TYPE, QLayoutOauth.TYPE_RENREN));
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		QLog.log(item.getTitle().toString() + item.getItemId() + item.getGroupId());
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		}
	}
	
}
