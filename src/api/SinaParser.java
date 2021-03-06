package api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import q.util.a.QPinyin;
import cn.xxd.tx.bean.Friend;

public class SinaParser {
	
	public static final int usersShowFriendsCount(String jsonStr) throws JSONException{
		return new JSONObject(jsonStr).getInt("friends_count");
	}

	public static final List<Friend> friendshipsFriends(String jsonStr, boolean isSwitchNameAndRemark) throws JSONException{
		List<Friend> data = new ArrayList<Friend>();
		JSONObject jo = new JSONObject(jsonStr);
		JSONArray ja = jo.getJSONArray("users");
		Friend f;
		JSONObject j;
		for(int i = 0, size = ja.length(); i < size; i++){
			j = ja.getJSONObject(i);
			f = new Friend();
			f.setId(j.getString("id"));
			if(isSwitchNameAndRemark && !"".equals(j.getString("remark"))){
				f.setName(j.getString("remark"));
				f.setRemark(j.getString("screen_name"));
			}else{
				f.setName(j.getString("screen_name"));
				f.setRemark(j.getString("remark"));
			}
			f.setPhoto("http://tp1.sinaimg.cn/" + f.getId() + "/50/0/1");
			f.setPhotoBig("http://tp1.sinaimg.cn/" + f.getId() + "/180/0/1");
			data.add(f);
		}
		return data;
	}
	
}
