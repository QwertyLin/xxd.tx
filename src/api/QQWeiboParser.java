package api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import q.util.QLog;
import q.util.a.QPinyin;
import cn.xxd.tx.bean.Friend;

public class QQWeiboParser {
	
	public static final int usersShowFriendsCount(String jsonStr) throws JSONException{
		QLog.log(jsonStr);
		return new JSONObject(jsonStr).getJSONObject("data").getJSONArray("info").getJSONObject(0).getInt("idolnum");
	}

	public static final List<Friend> friendshipsFriends(String jsonStr) throws JSONException{
		List<Friend> data = new ArrayList<Friend>();
		JSONArray ja = new JSONObject(jsonStr).getJSONObject("data").getJSONArray("info");
		Friend f;
		JSONObject j;
		for(int i = 0, size = ja.length(); i < size; i++){
			j = ja.getJSONObject(i);
			f = new Friend();
			f.setId(j.getString("openid"));
			f.setName(j.getString("nick"));
			f.setPhoto(j.getString("head") + "/50");
			f.setPhotoBig(j.getString("head") + "/180");
			data.add(f);
		}
		return data;
	}
	
}
