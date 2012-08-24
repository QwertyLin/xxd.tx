package cn.xxd.tx.util;

import q.frame.layout.QLayoutOauth;

public class QApp extends q.util.QApp {

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
	
	
	
	
}
