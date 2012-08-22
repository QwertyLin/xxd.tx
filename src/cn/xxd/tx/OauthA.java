package cn.xxd.tx;

import android.os.Bundle;
import cn.xxd.tx.bean.TokenDB;
import cn.xxd.tx.util.QSqlite;
import q.frame.QActivity;
import q.util.QLog;
import q.util.a.view.QLayoutOauth;
import q.util.a.view.QLayoutOauth.Token;

public class OauthA extends QActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(new QLayoutOauth(this, new QLayoutOauth.HandleRenren(), new QLayoutOauth.Callback() {
			
			@Override
			public void onSuccess(Token token) {
				
			}
			
			@Override
			public void onError() {
				QLog.log("error");
			}
		}));
		
	}
	
	/*@Override
	protected void onGetTokenError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onGetTokenSuccess(Token token) {
		setResult(RESULT_OK);
		//
		TokenDB db = new TokenDB(this);
		db.open(true);
		db.insert(token);
		db.close();
		//
	}*/

}
