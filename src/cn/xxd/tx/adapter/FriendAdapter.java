package cn.xxd.tx.adapter;

import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.xxd.tx.FriendA;
import cn.xxd.tx.R;
import cn.xxd.tx.bean.Friend;
import cn.xxd.tx.util.QApp;
import q.util.QHttp;
import q.util.QLog;
import q.util.a.QBitmapCache;
import q.util.a.QPinyin;
import q.util.a.view.QListViewPinyin;

public class FriendAdapter extends QListViewPinyin.Adapter<QPinyin<Friend>.Pinyin>{
	
	QBitmapCache cache = new QBitmapCache();
	QHttp qhttp;
	ListView lv;

	public FriendAdapter(final Context ctx, List<QPinyin<Friend>.Pinyin> data, String cacheDir) {
		super(ctx, data);
		qhttp = new QHttp(10, cacheDir, ((QApp)ctx.getApplicationContext()).getCacheExpirePhoto(), new QHttp.CallbackBitmapList() {
			
			@Override
			public void onError(IOException e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted(final Bitmap bm, final String tag) {
				cache.put(tag, bm);
				View v = lv.findViewWithTag(tag);
				if(v != null){
					((ImageView)v).setImageBitmap(bm);
				}
			}
		});
		qhttp.setCheckConnContentLength(true);
	}
	
	public void setListView(ListView lv){
		this.lv = lv;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.friend_item;
	}

	@Override
	protected Object getViewHolder(View v) {
		Holder h = new Holder();
		h.layoutItem = v.findViewById(R.id.friend_layout_item);
		h.tvName = (TextView)v.findViewById(R.id.friend_name);
		h.tvRemark = (TextView)v.findViewById(R.id.friend_remark);
		h.ivPic = (ImageView)v.findViewById(R.id.friend_pic);
		h.layoutTag = v.findViewById(R.id.friend_layout_tag);
		h.tvTag = (TextView)v.findViewById(R.id.friend_tag);
		return h;
	}

	@Override
	protected void onInitItem(final int position, QPinyin<Friend>.Pinyin data, Object viewHolder) {
		final Holder h = (Holder)viewHolder;
		final Friend f = data.getObj();
		if(data.isTag()){
			h.layoutItem.setVisibility(View.GONE);
			h.layoutTag.setVisibility(View.VISIBLE);
			h.tvTag.setText(data.getTag());
		}else{
			h.ivPic.setTag(f.getId());
			h.layoutTag.setVisibility(View.GONE);
			h.layoutItem.setVisibility(View.VISIBLE);
			h.tvName.setText(f.getName());
			h.tvRemark.setText(f.getRemark());
			
			Bitmap bm = cache.get(f.getId());
			if(bm != null){
				h.ivPic.setImageBitmap(bm);
			}else{
				h.ivPic.setImageBitmap(null);
				qhttp.get(f.getPic(), f.getId());
			}
			
		}
	}
	
	class Holder{
		View layoutItem;
		TextView tvName;
		TextView tvRemark;
		ImageView ivPic;
		View layoutTag;
		TextView tvTag;
	}


}
