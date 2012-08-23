package cn.xxd.tx;

import android.os.Bundle;
import q.frame.QActivity;
import q.frame.QDialog;
import q.util.QLog;
import q.frame.layout.QLayoutOauth;
import q.frame.layout.QLayoutOauth.Token;

public class OauthA extends QActivity implements QLayoutOauth.Callback {
	
	public static final String EXTRA_TYPE = "type";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int type = getIntent().getIntExtra(EXTRA_TYPE, 0);
		QLayoutOauth.Handle handle = null;
		switch(type){
		case QLayoutOauth.TYPE_SINA_WEIBO: 
			setTitle("新浪微博授权");
			handle = new QLayoutOauth.HandleSinaWeibo();
			break;
		case QLayoutOauth.TYPE_QQ_WEIBO: 
			setTitle("腾讯微博授权");
			handle = new QLayoutOauth.HandleQQWeibo();
			break;
		case QLayoutOauth.TYPE_QQ_ZONE: 
			setTitle("QQ空间授权");
			handle = new QLayoutOauth.HandleQQZone();
			break;
		case QLayoutOauth.TYPE_RENREN: 
			setTitle("人人网授权");
			handle = new QLayoutOauth.HandleRenren();
			break;
		}
		//验证
		if(handle == null){
			return;
		}
		//
		setContentView(new QLayoutOauth(this, handle, this));
	}

	@Override
	public void onSuccess(Token token) {
		QLayoutOauth.TokenDB db = new QLayoutOauth.TokenDB(this);
		db.open(true);
		db.insert(token);
		db.close();
		finish();
	}

	@Override
	public void onError() {
		QLog.error(OauthA.this, "授权出错" + getIntent().getIntExtra(EXTRA_TYPE, 0));
		QDialog.Simple dialog = new QDialog.Simple(this, "授权出错，请稍后再试!");
		dialog.addBtnGotIt(true);
		dialog.show();
	}
	

}
