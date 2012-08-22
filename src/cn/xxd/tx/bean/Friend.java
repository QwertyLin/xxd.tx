package cn.xxd.tx.bean;

import q.util.a.QPinyin.IPinyin;

public class Friend implements IPinyin {

	private String 
		id,
		name, //screen_name
		remark, //remark 备注
		pic; //avatar_large 头像
	
	@Override
	public String getText() {
		return name;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

}
