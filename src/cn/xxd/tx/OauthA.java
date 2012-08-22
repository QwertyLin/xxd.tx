package cn.xxd.tx;

import cn.xxd.tx.bean.TokenDB;
import cn.xxd.tx.util.QSqlite;
import q.util.a.QLog;
import q.util.a.view.QOauth;

public class OauthA extends QOauth {

	@Override
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
	}

}
