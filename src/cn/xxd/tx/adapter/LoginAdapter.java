package cn.xxd.tx.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.xxd.tx.FriendA;
import cn.xxd.tx.R;
import cn.xxd.tx.util.QApp;
import q.frame.QDialog;
import q.util.QFile;
import q.util.QHttp;
import q.util.a.QBitmapCache;
import q.util.view.QBaseAdapter;
import q.frame.layout.QLayoutOauth;

public class LoginAdapter extends QBaseAdapter<QLayoutOauth.Token> implements OnClickListener {
	
	QBitmapCache cache = new QBitmapCache();
	QHttp qHttp;

	public LoginAdapter(Context ctx, List<QLayoutOauth.Token> data, final ListView lv) {
		super(ctx, data);
		qHttp = new QHttp(10, QFile.get("login"), ((QApp)ctx.getApplicationContext()).getCacheExpirePhoto(), new QHttp.CallbackBitmapList() {
			
			@Override
			public void onError(IOException e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted(Bitmap bm, String tag) {
				cache.put(tag, bm);
				View v = lv.findViewWithTag(tag);
				if(v != null){
					((ImageView)v).setImageBitmap(bm);
				}
			}
		});
	}

	@Override
	protected int getLayoutId() {
		return R.layout.login_item;
	}

	@Override
	protected Object getViewHolder(View v) {
		Holder h = new Holder();
		h.layout = (LinearLayout)v.findViewById(R.id.login_item);
		h.ivPic = (ImageView)v.findViewById(R.id.login_pic);
		h.ivLogo = v.findViewById(R.id.login_logo);
		h.tvName = (TextView)v.findViewById(R.id.login_token_name);
		h.btnDelete = v.findViewById(R.id.login_delete);
		return h;
	}

	@Override
	protected void onInitItem(int position, final QLayoutOauth.Token data, Object viewHolder) {
		Holder h = (Holder)viewHolder;
		//
		h.ivPic.setTag(data.getId());
		//
		/*switch(data.getType()){
		case QLayoutOauth.TYPE_SINA_WEIBO: h.ivLogo.setBackgroundResource(R.drawable.logo_sinaweibo); break;
		case QLayoutOauth.TYPE_QQ_WEIBO: h.ivLogo.setBackgroundResource(R.drawable.logo_qqweibo); break;
		case QLayoutOauth.TYPE_QZONE: h.ivLogo.setBackgroundResource(R.drawable.logo_qqzone); break;
		case QLayoutOauth.TYPE_RENREN: h.ivLogo.setBackgroundResource(R.drawable.logo_renren); break;
		}*/
		//
		h.tvName.setText(data.getName());
		//
		h.layout.setTag(data);
		h.layout.setOnClickListener(this);
		//
		h.btnDelete.setTag(data);
		h.btnDelete.setOnClickListener(this);
		
		Bitmap bm = cache.get(data.getId());
		if(bm != null){
			h.ivPic.setImageBitmap(bm);
		}else{
			h.ivPic.setImageBitmap(null);
			qHttp.get(data.getPhoto(), data.getId());
		}
	}
	
	class Holder {
		LinearLayout layout;
		ImageView ivPic;
		View ivLogo;
		TextView tvName;
		View btnDelete;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.login_item:
			onClickItem((QLayoutOauth.Token)v.getTag());
			break;
		case R.id.login_delete:
			onClickDelete((QLayoutOauth.Token)v.getTag());
			break;
		}
	}
	
	private void onClickItem(QLayoutOauth.Token data){
		((QApp)ctx.getApplicationContext()).setToken(data);
		ctx.startActivity(new Intent(ctx, FriendA.class));
	}
	
	private void onClickDelete(QLayoutOauth.Token data){
		final QDialog.Simple dialogDelete = new QDialog.Simple(ctx, "确定要删除<" + data.getName() + ">吗？");
		dialogDelete.addBtn("确定", new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialogDelete.cancel();
			}
		});
		dialogDelete.show();
	}
	
	

}
