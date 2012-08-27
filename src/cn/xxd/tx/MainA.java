package cn.xxd.tx;

import cn.xxd.tx.util.QActivity;
import cn.xxd.tx.util.QApp;
import q.util.QLog;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class MainA extends QActivity {
	
	public static final String EXTRA_FORM_OUT = "from_out";

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	QLog.log("onCreate");
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, LoginA.class));
        
       	if("android.intent.action.GET_CONTENT".equals(getIntent().getAction())){
       		QLog.log("From 相册");
       		getQApp().setFromOut(true);
       		getQApp().removeQActivityCache();
        	 getQApp().getQActivityCache().put(this);
       	}else{
       		getQApp().setFromOut(false);
           	finish();
       	}
    }
    
    
}
