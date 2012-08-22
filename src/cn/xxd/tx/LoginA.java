package cn.xxd.tx;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import q.frame.QActivity;
import q.util.QLog;
import q.util.a.view.QListView;
import q.util.a.view.QLayoutOauth;
import cn.xxd.tx.R.id;
import cn.xxd.tx.adapter.TokenAdapter;
import cn.xxd.tx.bean.TokenDB;
import cn.xxd.tx.util.QConfig;
import cn.xxd.tx.util.QSqlite;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;

public class LoginA extends QActivity implements OnClickListener {
	
	List<QLayoutOauth.Token> listToken;
	TokenAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		//
		ListView lv = (ListView)findViewById(R.id.login_token);
		QListView.init(this, lv);
		findViewById(R.id.login_add).setOnClickListener(this);
		//
		TokenDB db = new TokenDB(this);
		db.open(false);
		listToken = db.queryAll();
		db.close();
		//
		listToken.addAll(listToken);
		listToken.addAll(listToken);
		listToken.addAll(listToken);
		listToken.addAll(listToken);
		//
		adapter = new TokenAdapter(this, listToken, lv);
		lv.setAdapter(adapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu sub = menu.addSubMenu("绑定帐号");
		MenuItem item = sub.getItem();
		item.setIcon(R.drawable.a_social_add_person);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		//
		sub.add("新浪微博").setIcon(R.drawable.logo_sinaweibo).setIntent(new Intent(this, OauthA.class).putExtra(QLayoutOauth.EXTRA_TYPE, QLayoutOauth.TYPE_SINA_WEIBO));
		sub.add("腾讯微博").setIcon(R.drawable.logo_qqweibo);
		sub.add("QQ空间").setIcon(R.drawable.logo_qqzone);
		sub.add("人人网").setIcon(R.drawable.logo_renren);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		QLog.log(item.getTitle().toString() + item.getItemId() + item.getGroupId());
		return super.onOptionsItemSelected(item);
	}
	
	private void refreshListToken(){
		QLog.log("refreshListToken");
		listToken.clear();
		TokenDB db = new TokenDB(this);
		db.open(false);
		listToken.addAll(db.queryAll());
		db.close();
		//
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case R.layout.login_open:
			if(resultCode == RESULT_OK){
				onResultOauth();
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.login_add:
			onClickAdd();
			break;
		case R.id.login_open_sina:
		case R.id.login_open_qqweibo:
		case R.id.login_open_qzone:
		case R.id.login_open_renren:
			onClickOauth(v.getId());
			break;
		}
	}
	
	private void onClickAdd(){
		if(dialogOpen == null){
			dialogOpen = dialogOpen();
		}
		dialogOpen.show();
	}
	
	private void onClickOauth(int type){
		/*switch(type){
		case R.id.login_open_sina: type = OauthA.TYPE_SINA_WEIBO; break;
		case R.id.login_open_qqweibo: type = OauthA.TYPE_QQ_WEIBO; break;
		case R.id.login_open_qzone: type = OauthA.TYPE_QZONE; break;
		case R.id.login_open_renren: type = OauthA.TYPE_RENREN; break;
		}
		startActivityForResult(new Intent(this, OauthA.class).putExtra(OauthA.EXTRA_TYPE, type), R.layout.login_open);
		dialogOpen.dismiss();*/
	}
	
	private void onResultOauth(){
		refreshListToken();
	}
	
	
	private Dialog dialogOpen;
	private Dialog dialogOpen(){
		View v = getLayoutInflater().inflate(R.layout.login_open, null);
		v.findViewById(R.id.login_open_sina).setOnClickListener(this);
		v.findViewById(R.id.login_open_qqweibo).setOnClickListener(this);
		v.findViewById(R.id.login_open_qzone).setOnClickListener(this);
		v.findViewById(R.id.login_open_renren).setOnClickListener(this);
		Dialog d = new Dialog(this);
		d.getWindow().setBackgroundDrawableResource(R.color.transparent);
		d.setContentView(v);
		return d;
	}

	
}
