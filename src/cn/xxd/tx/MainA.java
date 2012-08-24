package cn.xxd.tx;

import cn.xxd.tx.util.QApp;
import q.frame.QActivity;
import q.util.QLog;
import android.os.Bundle;
import android.content.Intent;

public class MainA extends QActivity {
	
	public static final String EXTRA_FORM_OUT = "from_out";

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	QLog.log("onCreate");
        super.onCreate(savedInstanceState);
        QApp qApp = (QApp)getApplicationContext();
        setContentView(R.layout.main);
        
        if("android.intent.action.GET_CONTENT".equals(getIntent().getAction())){
        	QLog.log("From 相册");
        	qApp.initQActivityCache();
        	qApp.getQActivityCache().put(this);
        	 startActivity(new Intent(this, LoginA.class).putExtra(EXTRA_FORM_OUT, true));
        }else{
	       	startActivity(new Intent(this, LoginA.class));
	       	finish();
        }
        
    }
    
    
}
