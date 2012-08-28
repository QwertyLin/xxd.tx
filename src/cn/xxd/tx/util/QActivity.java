package cn.xxd.tx.util;

import com.actionbarsherlock.app.ActionBar;

import android.os.Bundle;
import q.frame.QActivitySp;

public class QActivity extends QActivitySp {

	private QApp qApp;
	
	public QApp getQApp(){
		if(qApp == null){
			qApp = (QApp)this.getApplicationContext();
		}
		return qApp;
	}
}
