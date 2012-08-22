package cn.xxd.tx;

import java.util.ArrayList;
import java.util.List;

import q.util.a.QLog;
import q.util.a.view.QListView;
import q.util.a.view.QOauth;
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

public class LoginA extends Activity implements OnClickListener {
	
	List<QOauth.Token> listToken;
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
		switch(type){
		case R.id.login_open_sina: type = OauthA.TYPE_SINA_WEIBO; break;
		case R.id.login_open_qqweibo: type = OauthA.TYPE_QQ_WEIBO; break;
		case R.id.login_open_qzone: type = OauthA.TYPE_QZONE; break;
		case R.id.login_open_renren: type = OauthA.TYPE_RENREN; break;
		}
		startActivityForResult(new Intent(this, OauthA.class).putExtra(OauthA.EXTRA_TYPE, type), R.layout.login_open);
		dialogOpen.dismiss();
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
