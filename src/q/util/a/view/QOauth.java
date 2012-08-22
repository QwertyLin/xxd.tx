package q.util.a.view;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import q.util.QCode;
import q.util.QHttp;
import q.util.a.QLog;
import q.util.view.QLayout;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public abstract class QOauth extends Activity {
	
	/**
	 * 未授权
	 */
	public static class UnAuthException extends Exception{
		private static final long serialVersionUID = 1L;
	}
	
	public static final String EXTRA_TYPE = "type";
	public static final int 
		TYPE_QQ_WEIBO = 1, //http://dev.t.qq.com/
		TYPE_QZONE = 2, //http://opensns.qq.com/
		TYPE_RENREN = 3,
		TYPE_SINA_WEIBO = 4; //http://open.weibo.com/
	
	protected int type;
	
	public static final String SP_NAME = "oauth2_";
	private static final String 
		SP_KEY_EXPIRE = "expire",
		SP_KEY_ACCESS_TOKEN = "access_token";
		
	/**
	 * 获得令牌过期时间
	 */
	public static final long getExpireTime(Context ctx, int type){
		return ctx.getSharedPreferences(SP_NAME + type, Context.MODE_PRIVATE).getLong(SP_KEY_EXPIRE, 0);
	}
	
	/**
	 * 检查令牌是否过期
	 */
	private static final boolean checkExpire(long expire) {
		long timeRemain = expire - new Date().getTime();
		QLog.log("timeRemain:" + timeRemain + " expire:" + expire);
		if(timeRemain > 0){ //未过期
			return true;
		}else{ //已过期
			return false;
		}
	}
	
	/**
	 * 获得令牌
	 * return {accessToken, openId}
	 */
	public static final String getToken(Context ctx, int type) {
		SharedPreferences sp = ctx.getSharedPreferences(SP_NAME + type, Context.MODE_PRIVATE);
		if(checkExpire(sp.getLong(SP_KEY_EXPIRE, 0))){
			return sp.getString(SP_KEY_ACCESS_TOKEN, null);
		}else{ //如果令牌已过期，删除旧令牌
			removeToken(ctx, type);
			return null;
		}
	}

	/**
	 * 清除令牌
	 */
	public static final void removeToken(Context ctx, int type) {
		ctx.getSharedPreferences(SP_NAME + type, Context.MODE_PRIVATE)
			.edit()
			.clear()
			.commit();
	}
	
	private WebView webView;
	private QLayout.Loading layoutLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		this.type = getIntent().getIntExtra(EXTRA_TYPE, 0);
		//
		RelativeLayout layout = new RelativeLayout(this);
		setContentView(layout);
		//
		webView = new WebView(this);
		onInitWebView(webView);
		layout.addView(webView);
		//
		layoutLoading = new QLayout.Loading(this);
		layout.addView(layoutLoading, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
		//
		onGetToken();
	}
	
	private void onInitWebView(WebView webView){
		WebSettings set = webView.getSettings();
		set.setJavaScriptEnabled(true);
		set.setSupportZoom(true);
		set.setBuiltInZoomControls(true);
		set.setCacheMode(WebSettings.LOAD_NO_CACHE);
		//
		webView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress > 30 && (layoutLoading.getVisibility()==View.VISIBLE) ) {
					layoutLoading.setVisibility(View.GONE);
				}
			}
		});
		//获得授权码
		WebViewClient wvc = new WebViewClient() {
			int index = 0;
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				QLog.log("url:" + url);
				Pattern p = Pattern.compile(onInitCompileUrl());
				Matcher m = p.matcher(url);
				if (m.find() && index == 0) {
					layoutLoading.setText("授权中");
					layoutLoading.setVisibility(View.VISIBLE);
					index++;
					CookieManager.getInstance().removeAllCookie();//清除cookie
					onGetTokenSuccess(url, m);
				}
			}
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				QLog.log("shouldOverrideUrlLoading url=" + url);
				if(url.contains("error_uri")  //新浪
					|| url.contains("checkType=error") //QQ微博
					|| url.contains("error=login_denied") //人人
				){
					return true;
				}
				return super.shouldOverrideUrlLoading(view, url);
			}
		};
		webView.setWebViewClient(wvc);
	}
	
	private void onGetToken(){
		String authUrl = onInitAuthUrl();
		QLog.log("load url:" + authUrl);
		webView.loadUrl(authUrl);
	}
	
	private void onGetTokenSuccess(String url, Matcher m){
		webView.setVisibility(View.GONE);
		try {
			QLog.log("url" + url);
			onGetToken(m);
		} catch (Exception e) {
			e.printStackTrace();
			onGetTokenError();
		}
	}
	
	private boolean onSaveToken(SharedPreferences.Editor editor, long expire, String accessToken){
		if(expire == 0 || accessToken == null || "".equals(accessToken)){
			QLog.log("token null");
			editor.clear().commit();
			return false;
		}
		editor.putLong(SP_KEY_EXPIRE, new Date().getTime() + expire * 1000)
			.putString(SP_KEY_ACCESS_TOKEN, accessToken)
			.commit();
		//onSaveTokenSuccess();
		return true;
	}
	
	private boolean onCheckToken(Token token){
		if(token.getExpireTime() == 0 || token.getToken() == null || token.getToken().equals("")){
			QLog.log("onCheckToken error");
			return false;
		}
		return true;
	}
	
	/*需要个性化处理的*/
	protected abstract void onGetTokenError();
	protected abstract void onGetTokenSuccess(Token token);
	
	public static class Token implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private int type;
		private String token;
		private long expireTime;
		private String id;//id号
		private String name;//昵称
		private String pic; //头像
		
		/**
		 * @return true表已过期，false表未过期
		 */
		public boolean isExpire(){
			long timeRemain = expireTime - new Date().getTime();
			QLog.log("timeRemain:" + timeRemain + " expire:" + expireTime);
			if(timeRemain > 0){ //未过期
				return false;
			}else{ //已过期
				return true;
			}
		}
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		public long getExpireTime() {
			return expireTime;
		}
		public void setExpireTime(long expireTime) {
			this.expireTime = expireTime;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPic() {
			return pic;
		}
		public void setPic(String pic) {
			this.pic = pic;
		}
	}
	
	private String onInitCompileUrl(){
		switch(type){
		case TYPE_QQ_WEIBO: return ".+access_token=(.+)&expires_in=(.+)&openid=(.+)&openkey";
		case TYPE_QZONE: return ".+access_token=(.+)&expires_in=(.+)";
		case TYPE_RENREN: return ".+access_token=(.+)&expires_in=(.+)&scope";
		case TYPE_SINA_WEIBO: return ".+access_token=(.+)&.+expires_in=(.+)&uid=(.+)";
		}
		return null;
	}
	
	private String onInitAuthUrl(){
		switch(type){
		case TYPE_QQ_WEIBO: 
			return "https://open.t.qq.com/cgi-bin/oauth2/authorize?" 
					+ "client_id=" + QQ_WEIBO_CLIENT_ID 
					+ "&redirect_uri=" + QQ_WEIBO_CALLBACK_URL
					+ "&response_type=token";
		case TYPE_QZONE: 
			return "https://graph.qq.com/oauth2.0/authorize?" 
					+ "client_id=" + QZONE_CLIENT_ID 
					+ "&redirect_uri=tencentauth:%2F%2Fauth.qq.com"
					+ "&response_type=token"
					+ "&display=mobile"
					+ "&scope=get_user_info,get_user_profile,add_share,add_topic,list_album,upload_pic,add_album";
		case TYPE_RENREN: 
			return "http://graph.renren.com/oauth/authorize?" 
					+ "client_id=" + RENREN_CLIENT_ID 
					+ "&redirect_uri=http:%2F%2Fgraph.renren.com%2Foauth%2Flogin_success.html"
					+ "&response_type=token"
					+ "&display=touch"
					+ "&scope=status_update+photo_upload";
		case TYPE_SINA_WEIBO: 
			return "https://api.weibo.com/oauth2/authorize?"
					+ "client_id=" + SINA_WEIBO_CLIENT_ID 
					+ "&redirect_uri=" + SINA_WEIBO_CALLBACK_URL 
					+ "&response_type=token"
					+ "&display=mobile";
		}
		return null;
	}
	
	private void onGetToken(Matcher m) throws Exception{
		SharedPreferences.Editor editor = this.getSharedPreferences(SP_NAME + type, Context.MODE_PRIVATE).edit();
		switch(this.type){
		case TYPE_QQ_WEIBO: onQQWeiboCheckToken(m); break;
		case TYPE_QZONE: onQzoneGetToken(m, editor); break;
		case TYPE_RENREN: onRenrenGetToken(m, editor); break;
		case TYPE_SINA_WEIBO: onSinaWeiboCheckToken(m); break;
		}
	}
	
	protected void Q(){}
	
	private static final String 
		QQ_WEIBO_CLIENT_ID = "801140374", 
		QQ_WEIBO_CALLBACK_URL = "http:%2F%2Fwww.xxd.cn";
	
	/*private void onQQWeiboGetToken(Matcher m, SharedPreferences.Editor editor) throws Exception{
		String accessToken = m.group(1);
		long expire = Long.parseLong(m.group(2));
		String openId = m.group(3);
		QLog.log("access_token" + accessToken);
		QLog.log("expires_in" + expire);
		QLog.log("openId" + openId);
		if(!onQQWeiboSaveToken(editor, expire, accessToken, openId)){
			throw new Exception();
		}
	}
	
	private boolean onQQWeiboSaveToken(SharedPreferences.Editor editor, long expire, String accessToken, String openId){
		if(openId == null || "".equals(openId) ){
			QLog.log("openId null");
			return false;
		}
		editor.putString("openId", openId);
		return onSaveToken(editor, expire,  accessToken);
	}*/
	
	private void onQQWeiboCheckToken(Matcher m) throws Exception{
		final Token token = new Token();
		token.setType(this.type);
		token.setToken(m.group(1));
		token.setExpireTime(new Date().getTime() + Long.parseLong(m.group(2)) * 1000 );
		token.setId(m.group(3));
		QLog.log("access_token=" + token.getToken() + " expires_in=" + token.getExpireTime() + " openid=" + token.getId());
		if(!onCheckToken(token)){
			throw new Exception();
		}
		if(token.getId() == null || token.getId().equals("")){
			throw new Exception();
		}
		new Thread(){
			public void run() {
				try {
					JSONObject json = new JSONObject(QHttp.staticGet(qQQWeiboUserInfo(token)));
					json = json.getJSONObject("data");
					token.setName(json.getString("nick"));
					token.setPic(json.getString("head") + "/50");
					if(token.getName() == null || token.getName().equals("") 
							|| token.getPic() == null || token.getPic().equals("")){
						throw new Exception();
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onGetTokenSuccess(token);
							setResult(RESULT_OK);
							finish();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onGetTokenError();
						}
					});
				} 
			};
		}.start();
	}
	
	public static String qQQWeiboUserInfo(Token token) throws IOException, UnAuthException{
		return "https://open.t.qq.com/api/user/info?"
				+ "oauth_consumer_key=" + QQ_WEIBO_CLIENT_ID
				+ "&access_token=" + token.getToken()
				+ "&openid=" + token.getId()
				+ "&clientip=127.0.0.1&oauth_version=2.a&format=json"
				;
	}
	
	public static String qQQWeiboFriends(Token token) throws IOException, UnAuthException{
		return "https://open.t.qq.com/api/friends/idollist?"
				+ "oauth_consumer_key=" + QQ_WEIBO_CLIENT_ID
				+ "&access_token=" + token.getToken()
				+ "&openid=" + token.getId()
				+ "&clientip=127.0.0.1&oauth_version=2.a&format=json"
				+ "&reqnum=30"
				;
	}
	
	/*public static String qQQWeiboPostText(Context ctx, String text) throws UnAuthException, IOException {
		String token = getToken(ctx, TYPE_QQ_WEIBO);
		if(token == null){
			throw new UnAuthException();
		}
		String param = 
				"oauth_consumer_key=" + QQ_WEIBO_CLIENT_ID 
				+ "&access_token=" + token 
				+ "&openid=" + qQQWeiboOpenId(ctx)
				+ "&clientip=127.0.0.1"
				+ "&oauth_version=2.a"
				+ "&format=json"
				+ "&content=" + URLEncoder.encode(text, "utf-8");
		return QHttp.staticPost("https://open.t.qq.com/api/t/add", param);
	}
	
	public static String qQQWeiboPostPic(Context ctx, String text, String pic) throws IOException, UnAuthException{
		String token = getToken(ctx, TYPE_QQ_WEIBO);
		if(token == null){
			throw new UnAuthException();
		}
		String boundary = "-----114975832116442893661388290519";
		StringBuffer params = new StringBuffer();
		boundary = "\r\n" + "--" + boundary + "\r\n";
		//
		params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "oauth_consumer_key" + "\"\r\n\r\n");
        params.append(QQ_WEIBO_CLIENT_ID);
		//
		params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "access_token" + "\"\r\n\r\n");
        params.append(token);
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "openid" + "\"\r\n\r\n");
        params.append(qQQWeiboOpenId(ctx));
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "oauth_version" + "\"\r\n\r\n");
        params.append("2.a");
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "format" + "\"\r\n\r\n");
        params.append("json");
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "content" + "\"\r\n\r\n");
        params.append(text);
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "clientip" + "\"\r\n\r\n");
        params.append("127.0.0.1");
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "pic" + "\"; filename=\"" + "pic.png" + "\"\r\n");
        params.append("Content-Type: " + "application/octet-stream" + "\r\n\r\n");
        //
		return QHttp.staticPost("https://open.t.qq.com/api/t/add_pic", params.toString(), "-----114975832116442893661388290519", pic);
	}*/
	
	private static final String QZONE_CLIENT_ID = "100246308";
	
	private void onQzoneGetToken(Matcher m, SharedPreferences.Editor editor) throws Exception{
		String accessToken = m.group(1);
		long expire = Long.parseLong(m.group(2));
		QLog.log("access_token" + accessToken);
		QLog.log("expires_in" + expire);
		onQzoneGetOpenId(editor, accessToken, expire);
	}
	
	private void onQzoneGetOpenId(final SharedPreferences.Editor editor, final String accessToken, final long expire){
		try {
			//String data = QHttpSuper.get("https://graph.qq.com/oauth2.0/me?access_token=" + accessToken);
			String data = null; //TODO;
			if(data != null){
				String sep = "openid\":\"";
				int startIndex = data.indexOf(sep) + sep.length(); 
				int endIndex = data.indexOf("\"", startIndex);
				String openId = data.substring(startIndex, endIndex);
				QLog.log("openId" + openId);
				if(!onQzoneSaveToken(editor, expire, accessToken, openId)){
					throw new Exception();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			onGetTokenError();
		}
	}
	
	private boolean onQzoneSaveToken(SharedPreferences.Editor editor, long expire, String accessToken, String openId){
		if(openId == null || "".equals(openId) ){
			QLog.log("openId null");
			return false;
		}
		editor.putString("openId", openId);
		return onSaveToken(editor, expire, accessToken);
	}
	
	public static final String qQzoneOpenId(Context ctx) {
		return ctx.getSharedPreferences(SP_NAME + TYPE_QZONE, Context.MODE_PRIVATE).getString("openId", null);
	}
	
	public static String qQzoneUserInfo(Context ctx) throws IOException, UnAuthException{
		String token = getToken(ctx, TYPE_QZONE);
		if(token == null){
			throw new UnAuthException();
		}
		String url = 
				"https://graph.qq.com/user/get_user_info?"
				+ "oauth_consumer_key=" + QZONE_CLIENT_ID
				+ "&access_token=" + token
				+ "&openid=" + qQzoneOpenId(ctx);
		//return QHttpSuper.get(url);
		return null; //TODO
	}
	
	public static String qQzonePostText(Context ctx, String text) throws UnAuthException, IOException {
		String token = getToken(ctx, TYPE_QZONE);
		if(token == null){
			throw new UnAuthException();
		}
		String param = 
				"oauth_consumer_key=" + QZONE_CLIENT_ID
				+ "&access_token=" + token
				+ "&openid=" + qQzoneOpenId(ctx)
				+ "&con=" + URLEncoder.encode(text, "utf-8")
				+ "&third_source=1"
				;
		return QHttp.staticPost("https://graph.qq.com/shuoshuo/add_topic", param);
	}
	
	public static String qQzonePostPic(Context ctx, String text, String pic) throws IOException, UnAuthException{
		String token = getToken(ctx, TYPE_QZONE);
		if(token == null){
			throw new UnAuthException();
		}
		String boundary = "-----114975832116442893661388290519";
		StringBuffer params = new StringBuffer();
		boundary = "\r\n" + "--" + boundary + "\r\n";
		//
		params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "access_token" + "\"\r\n\r\n");
        params.append(token);
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "oauth_consumer_key" + "\"\r\n\r\n");
        params.append(QZONE_CLIENT_ID);
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "openid" + "\"\r\n\r\n");
        params.append(qQzoneOpenId(ctx));
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "photodesc" + "\"\r\n\r\n");
        params.append(text);
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "title" + "\"\r\n\r\n");
        params.append(System.currentTimeMillis() + ".png");
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "picture" + "\"; filename=\"" + "pic.png" + "\"\r\n");
        params.append("Content-Type: " + "image/x-png" + "\r\n\r\n");
        //
		return QHttp.staticPost("https://graph.qq.com/photo/upload_pic", params.toString(), "-----114975832116442893661388290519", pic);
    }
	
	private static final String RENREN_CLIENT_ID = "ce90074fea9c4650b6c860aaf149c758", RENREN_CLIENT_SECRET = "1edb76174931407f80c25984385f176a";

	private void onRenrenGetToken(Matcher m, SharedPreferences.Editor editor) throws Exception{
		String accessToken = m.group(1);
		//accessToken 人人网特殊处理
		if(accessToken.contains("%7C")){
			accessToken = accessToken.replace("%7C", "|");
		}
		long expire = Long.parseLong(m.group(2));
		QLog.log("access_token" + accessToken);
		QLog.log("expires_in" + expire);
		if(!onSaveToken(editor, expire, accessToken)){
			throw new Exception();
		}
	}
	
	public static String qRenrenUserInfo(Context ctx) throws IOException, UnAuthException{
		String token = getToken(ctx, TYPE_RENREN);
		if(token == null){
			throw new UnAuthException();
		}
		String md5 = QCode.md5(
				"access_token=" + token
				+ "format=JSON"
				+ "method=users.getInfo"
				+ "v=1.0"
				+ RENREN_CLIENT_SECRET);
		String param = 
				"&access_token=" + token 
				+ "&format=JSON"
				+ "&method=users.getInfo"
				+ "&v=1.0"
				+ "&sig="+md5;
		return QHttp.staticPost("http://api.renren.com/restserver.do", param);
	}
	
	public static String qRenrenFriends(Context ctx) throws IOException, UnAuthException{
		String token = getToken(ctx, TYPE_RENREN);
		if(token == null){
			throw new UnAuthException();
		}
		String md5 = QCode.md5(
				"access_token=" + token
				+ "format=JSON"
				+ "method=" + "friends.getFriends"
				+ "v=1.0"
				+ RENREN_CLIENT_SECRET);
		String param = 
				"&access_token=" + token 
				+ "&format=JSON"
				+ "&method=" + "friends.getFriends"
				+ "&v=1.0"
				+ "&sig="+md5;
		return QHttp.staticPost("http://api.renren.com/restserver.do", param);
	}
	
	public static String qRenrenPostText(Context ctx, String text) throws UnAuthException, IOException {
		String token = getToken(ctx, TYPE_RENREN);
		if(token == null){
			throw new UnAuthException();
		}
		String md5 = QCode.md5(
				"access_token="+token
				+ "format=JSON"
				+ "method=status.set"
				+ "status="+text
				+ "v=1.0"
				+ RENREN_CLIENT_SECRET);
		String param = "&access_token=" + token 
				+ "&format=JSON"
				+ "&method=status.set"
				+ "&status="+URLEncoder.encode(text, "utf-8")
				+ "&v=1.0"
				+ "&sig="+md5;
		return QHttp.staticPost("http://api.renren.com/restserver.do", param);
	}
	
	public static String qRenrenPostPic(Context ctx, String text, String pic) throws IOException, UnAuthException{
		String token = getToken(ctx, TYPE_RENREN);
		if(token == null){
			throw new UnAuthException();
		}
		String md5 = QCode.md5(
				"access_token=" + token
				+ "api_key="+ RENREN_CLIENT_ID
				+ "caption=" + text
				+ "format=JSON"
				+ "method=photos.upload"
				+ "v=1.0"
				+ RENREN_CLIENT_SECRET);					
		//
		String boundary = "-----------------------------114975832116442893661388290519";
		StringBuffer params = new StringBuffer();
		boundary = "\r\n" + "--" + boundary + "\r\n";
		//
		params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "method" + "\"\r\n\r\n");
        params.append("photos.upload");
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "api_key" + "\"\r\n\r\n");
        params.append(RENREN_CLIENT_ID);
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "access_token" + "\"\r\n\r\n");
        params.append(token);
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "sig" + "\"\r\n\r\n");
        params.append(md5);
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "v" + "\"\r\n\r\n");
        params.append("1.0");
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "format" + "\"\r\n\r\n");
        params.append("JSON");
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "caption" + "\"\r\n\r\n");
        params.append(text);
        //
        params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "upload" + "\"; filename=\"" + "pic.png" + "\"\r\n");
        params.append("Content-Type: " + "image/png" + "\r\n\r\n");
        //
		return QHttp.staticPost("http://api.renren.com/restserver.do", params.toString(), "-----------------------------114975832116442893661388290519", pic);
    }
	
	private static final String 
		SINA_WEIBO_CLIENT_ID = "43767983", 
		SINA_WEIBO_CALLBACK_URL = "http://www.xxd.cn";
	
	private void onSinaWeiboCheckToken(Matcher m) throws Exception{
		final Token token = new Token();
		token.setType(this.type);
		token.setToken(m.group(1));
		token.setExpireTime(new Date().getTime() + Long.parseLong(m.group(2)) * 1000 );
		token.setId(m.group(3));
		QLog.log("access_token=" + token.getToken() + " expires_in=" + token.getExpireTime() + " uid=" + token.getId());
		if(!onCheckToken(token)){
			throw new Exception();
		}
		if(token.getId() == null || token.getId().equals("")){
			throw new Exception();
		}
		new Thread(){
			public void run() {
				try {
					JSONObject json = new JSONObject(QHttp.staticGet(qSinaWeiboUsersShow(token)));
					token.setName(json.getString("screen_name"));
					token.setPic(json.getString("profile_image_url"));
					if(token.getName() == null || token.getName().equals("") 
							|| token.getPic() == null || token.getPic().equals("")){
						throw new Exception();
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onGetTokenSuccess(token);
							setResult(RESULT_OK);
							finish();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onGetTokenError();
						}
					});
				} 
			};
		}.start();
	}
	
	public static String qSinaWeiboUsersShow(Token token) {
		return "https://api.weibo.com/2/users/show.json?"
					+ "access_token=" + token.getToken()
					+ "&uid=" + token.getId();
	}
	
	public static String qSinaWeiboFriends(Token token, int count, int cursor) throws IOException, UnAuthException{
		return "https://api.weibo.com/2/friendships/friends.json?"
					+ "access_token=" + token.getToken()
					+ "&uid=" + token.getId()
					+ "&count=" + count
					+ "&cursor=" + cursor;
	}
	
	public static String qSinaWeiboPostText(Token token, String text, String lat, String lng) throws UnAuthException, IOException {
		if(token == null || token.isExpire()){
			throw new UnAuthException();
		}
		//加入空格，否则无法重复发送
		int space = new Random().nextInt(50);
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < space; i++){
			sb.append(" ");
		}
		String param = "access_token=" + token.getToken() + "&status=" + URLEncoder.encode(text + sb, "utf-8");
		//
		if(lat != null && lng != null){
			param += "&lat=" + lat + "&long=" + lng;
		}
		//
		return QHttp.staticPost("https://api.weibo.com/2/statuses/update.json", param);
	}
	
	public static String qSinaWeiboPostPic(Token token, String text, String pic, String lat, String lng) throws IOException, UnAuthException{
		if(token == null || token.isExpire()){
			throw new UnAuthException();
		}
		String boundary = "-----114975832116442893661388290519";
		StringBuffer params = new StringBuffer();
		boundary = "\r\n" + "--" + boundary + "\r\n";
		//
		params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "access_token" + "\"\r\n\r\n");
	    params.append(token.getToken());
	    //
	    params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "status" + "\"\r\n\r\n");
		int space = new Random().nextInt(50); //加入空格，否则无法重复发送
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < space; i++){
			sb.append(" ");
		}
	    params.append(text + sb);
	    //
	    if(lat != null && lng != null){
	    	params.append(boundary);
	 		params.append("Content-Disposition: form-data; name=\"" + "lat" + "\"\r\n\r\n");
	 	    params.append(lat);
	 	    //
	 	    params.append(boundary);
	 		params.append("Content-Disposition: form-data; name=\"" + "long" + "\"\r\n\r\n");
	 	    params.append(lng);
	    }
	    //
	    params.append(boundary);
		params.append("Content-Disposition: form-data; name=\"" + "pic" + "\"; filename=\"" + new File(pic).getName() + "\"\r\n");
	    params.append("Content-Type: " + "image/x-png" + "\r\n\r\n");
	    //
		return QHttp.staticPost("https://upload.api.weibo.com/2/statuses/upload.json", params.toString(), "-----114975832116442893661388290519", pic);
	}
}
