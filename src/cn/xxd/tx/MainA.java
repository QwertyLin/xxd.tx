package cn.xxd.tx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.xxd.tx.bean.Friend;

import q.util.QFile;
import q.util.QHttp;
import q.util.a.QPinyin;
import q.util.a.QPinyin.Pinyin;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;

public class MainA extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //
        //new QDialog.Loading(this).show();
        
        //
        QFile.init(this);
        //
        startActivity(new Intent(this, LoginA.class));
        finish();
    }
    
    
}
