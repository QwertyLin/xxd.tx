package cn.xxd.tx.util;

import q.frame.layout.QLayoutOauth;
import q.util.QAppSp;

public class QApp extends QAppSp {

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	//操作中的Token
	
	private QLayoutOauth.Token token; 

	public QLayoutOauth.Token getToken() {
		return token;
	}

	public void setToken(QLayoutOauth.Token token) {
		this.token = token;
	}
	
	//头像图片缓存时间 3个月
	
	private int cacheExpirePhoto = 60 * 24 * 30 * 3;  

	public int getCacheExpirePhoto() {
		return cacheExpirePhoto;
	}
	
	//是否从相册等其他地方进入
	
	private boolean isFromOut;

	public boolean isFromOut() {
		return isFromOut;
	}

	public void setFromOut(boolean isFromOut) {
		this.isFromOut = isFromOut;
	}
	
	
	
	
}
