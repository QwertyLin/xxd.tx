package cn.xxd.tx.util;

import q.frame.layout.QLayoutOauth;

public class QApp extends q.util.QApp {

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	private QLayoutOauth.Token token; //操作中的Token

	public QLayoutOauth.Token getToken() {
		return token;
	}

	public void setToken(QLayoutOauth.Token token) {
		this.token = token;
	}
	
	private int cacheExpirePhoto = 60 * 24 * 30 * 3;  //头像图片缓存时间 3个月

	public int getCacheExpirePhoto() {
		return cacheExpirePhoto;
	}
	
	
}
