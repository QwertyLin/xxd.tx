package q.util.a.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.xxd.tx.bean.Friend;

import q.manager.QWindow;
import q.util.a.QPinyin;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class QListViewPinyin extends RelativeLayout implements OnTouchListener, OnGestureListener {
	
	GestureDetector mygesture;
	ListView listView;
	Adapter<QPinyin<Friend>.Pinyin> myAdapter;
	Context ctx;
	
	LinearLayout layoutLetter;

	public QListViewPinyin(Context ctx, QListViewPinyin.Adapter<QPinyin<Friend>.Pinyin> adapter) {
		super(ctx);
		this.ctx = ctx;
		this.myAdapter = adapter;
		//ListView
		listView = new ListView(ctx);
		QListView.init(ctx, listView);
		listView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
		listView.setAdapter(adapter);
		this.addView(listView);
		//字母表
		mygesture = new GestureDetector(this);// 构建手势探测器
		layoutLetter = new LinearLayout(ctx);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(new QWindow(ctx).getWidth()/10, RelativeLayout.LayoutParams.FILL_PARENT);
		rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutLetter.setLayoutParams(rlp);
		layoutLetter.setClickable(true);
		layoutLetter.setOrientation(LinearLayout.VERTICAL);
		layoutLetter.setGravity(Gravity.CENTER_HORIZONTAL);
		layoutLetter.setOnTouchListener(this);
		this.addView(layoutLetter);
	}
	
	public ListView getListView(){
		return listView;
	}
	
	public void setOnItemClickListener(OnItemClickListener listener){
		listView.setOnItemClickListener(listener);
	}
	
	public void refreshLetter(){
		myAdapter.initSections();
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1);
		layoutLetter.removeAllViews();
		TextView tv;
		for(String section : myAdapter.sections){
			tv = new TextView(ctx);
			tv.setGravity(Gravity.CENTER);
			tv.setText(section);
			tv.setLayoutParams(llp);
			layoutLetter.addView(tv);
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Log.v("tag", ">>>>>>>>onDown>>");
		util(e);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		util(e2);
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		Log.v("tag", ">>>>>>>>onLongPress>>");
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.v("tag", ">>>>>>>>onFling>>");
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			v.setBackgroundColor(0x00000000);
			return super.onTouchEvent(event);
		default:
			v.setBackgroundColor(0x88DDDDDD);
			return mygesture.onTouchEvent(event);
		}
		
	}
	
	public void util(MotionEvent e2) {
		int i = (int) ((e2.getY() - this.getTop()) / ((float)this.getHeight() / myAdapter.sections.length));
		System.out.println("----------" + i);
		listView.setSelectionFromTop(myAdapter.getPositionForSection(i), 0);
	}
	
	public abstract static class Adapter<T extends QPinyin.Pinyin> extends QBaseAdapter<T> implements SectionIndexer {
		
		public HashMap<String, Integer> alphaIndexer;
		public String[] sections = new String[0];

		public Adapter(Context ctx, List<T> data) {
			super(ctx, data);
		}
		
		@Override
		public boolean isEnabled(int position) {
			return !datas.get(position).isTag();
		}
		
		@Override
		public int getPositionForSection(int section) {
			if(section >= sections.length){
				section = sections.length - 1;
			}else if(section < 0){
				section = 0;
			}
			String letter = sections[section];
			return alphaIndexer.get(letter);
		}

		@Override
		public int getSectionForPosition(int position) {
			Log.v("tag", ">>>>>>>>>>>>>" + sections.length + ">>>"
					+ sections[0]);

			int prevIndex = 0;
			for (int i = 0; i < sections.length; i++) {
				if (getPositionForSection(i) > position
						&& prevIndex <= position) {
					prevIndex = i;
					break;
				}
				prevIndex = i;
			}
			return prevIndex;
		}

		@Override
		public Object[] getSections() {
			Log.v("tag", ">>>>>>>>>>>>>" + sections.length + ">>>"
					+ sections[0]);
			return sections;
		}
		
		public void initSections() {
			alphaIndexer = new HashMap<String, Integer>();
			T t;
			for (int i = datas.size() - 1; i >= 0; i--) {
				//String element = items.get(i).getInitial();
				//String firstChar = element.substring(0, 1).toUpperCase();
				//String firstChar = String.valueOf(datas.get(i).getInitial());//TODO
				t = datas.get(i);
				if(t.isTag()){
					alphaIndexer.put(t.getTag(), i);
				}
				/*if (firstChar > 'Z' || firstChar < 'A')
					firstChar = '#';*/
				
			}
			Set<String> keys = alphaIndexer.keySet();
			Iterator<String> it = keys.iterator();
			ArrayList<String> keyList = new ArrayList<String>();
			while (it.hasNext())
				keyList.add(it.next());
			Collections.sort(keyList);
			sections = new String[keyList.size()];
			keyList.toArray(sections);
			//
			/*for(String str : sections){
				System.out.println("---------" + str);
			}*/
			//
			
		}
		
	}

}
