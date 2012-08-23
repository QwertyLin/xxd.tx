package q.util.a;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import q.util.QLog;

import android.os.SystemClock;

import net.sourceforge.pinyin4j.PinyinHelper;


public class QPinyin<T extends QPinyin.IPinyin> {
	
	private boolean isWorking = false;
	
	private TreeSet<Pinyin> dataSet = new TreeSet<Pinyin>(new Comparator<Pinyin>() {
		@Override
		public int compare(Pinyin o1, Pinyin o2) {
			int compare = 0;
			if(o1.isCn() && o2.isCn()){
				compare = o1.getPinyin().compareTo(o2.getPinyin());
			}else if(o1.isCn() && !o2.isCn()){
				compare = o1.getPinyin().compareToIgnoreCase(o2.getObj().getText());
			}else if( !o1.isCn() && o2.isCn()){
				compare = o1.getObj().getText().compareToIgnoreCase(o2.getPinyin());
			}else{
				compare = o1.getObj().getText().compareToIgnoreCase(o2.getObj().getText());
			}
			return compare == 0 ? -1 : compare;
		}
	});
	
	public class Pinyin {
		
		private String pinyin;
		private String tag;//首字母
		private T obj;
    	
    	/**
		 * 是否中文
		 */
		public boolean isCn(){ //若pinyin为空，则为英语
    		return pinyin != null;
    	}
		
		public boolean isTag(){ //若obj为空，则为标签
    		return obj == null;
    	}
    	
		public String getPinyin() {
			return pinyin;
		}
		public void setPinyin(String pinyin) {
			this.pinyin = pinyin;
		}
		public String getTag() {
			return tag;
		}
		public void setTag(String tag) {
			this.tag = tag;
		}

		public T getObj() {
			return obj;
		}

		public void setObj(T obj) {
			this.obj = obj;
		}
		
	}
	
	public interface IPinyin {
		public String getText();
	}
	
	public synchronized void add(List<T> listTemp){
		while(isWorking){
			SystemClock.sleep(200);
		}
		isWorking = true;
		//System.out.println("listTemp " + listTemp.size());
		List<Pinyin> list = new ArrayList<Pinyin>();
		Pinyin item = null;
		for(T t : listTemp){
			item = new Pinyin();
			item.setObj(t);
			list.add(item);
		}
		for(Pinyin t : list){
			String[] cs = PinyinHelper.toHanyuPinyinStringArray(t.getObj().getText().charAt(0));
    		if(cs != null){
    			t.setPinyin(PinyinHelper.toHanyuPinyinStringArray(t.getObj().getText().charAt(0))[0]);
    			//System.out.println(t.getPinyin());
    			t.setTag(String.valueOf(Character.toUpperCase(t.getPinyin().charAt(0))));
    		}else{
    			if(Character.isLetter(t.getObj().getText().charAt(0))){ //区别字母还是数字或其他
    				t.setTag(String.valueOf(Character.toUpperCase(t.getObj().getText().charAt(0))));
    			}else{
    				t.setTag("#");
    			}
    		}
    		dataSet.add(t);
		}
		isWorking = false;
	}
	
	public synchronized List<Pinyin> order(){
		while(isWorking){
			SystemClock.sleep(200);
		}
		isWorking = true;
		//System.out.println("dataset " + dataSet.size());
		Set<String> tagSet = new HashSet<String>();
		Pinyin tag;
		List<Pinyin> list = new ArrayList<Pinyin>();
    	for(Pinyin p : dataSet){
    		if(tagSet.add(p.getTag())){
    			tag = new Pinyin();
    			tag.setTag(p.getTag());
    			list.add(tag);
    		}
    		list.add(p);
    	}
    	isWorking = false;
		return list;
		
	}

}
