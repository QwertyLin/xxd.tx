package cn.xxd.tx.bean;

import q.util.a.QPinyin.IPinyin;

public class Friend implements IPinyin {

	private String 
		id,
		name, //
		remark, //备注
		photo, //头像
	    photoBig; //大头像
	
	@Override
	public String getText() {
		return name;
	}
	
	public Friend clone() {
		Friend item = new Friend();
		item.id = this.id;
		item.name = this.name;
		item.remark = this.remark;
		item.photo = this.photo;
		item.photoBig = this.photoBig;
		return item;
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

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getPhotoBig() {
		return photoBig;
	}

	public void setPhotoBig(String photoBig) {
		this.photoBig = photoBig;
	}

}
