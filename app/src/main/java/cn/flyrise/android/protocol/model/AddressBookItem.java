/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-26 ����9:31:33
 */
package cn.flyrise.android.protocol.model;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.io.Serializable;

public class AddressBookItem implements Serializable {

	private static final long serialVersionUID = 3362940236532561519L;
	private String id;
	private String name;
	private String nums;
	private String nodeNums;
	private String type;
	private String departmentName;
	private String imageHref;
	private String position;
	private String tel;
	private String phone;
	private String email;
	private String address;
	private String imid;

	public String getImid() {
		return imid;
	}

	public void setImid(String imid) {
		this.imid = imid;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public String getNums() {
		return nums;
	}

	public void setNums(String nums) {
		this.nums = nums;
	}

	public String getNodeNums() {
		return nodeNums;
	}

	public void setNodeNums(String nodeNums) {
		this.nodeNums = nodeNums;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getType() {
		return CommonUtil.parseInt(type);
	}

	public void setDataSourceType(int type) {
		this.type = type + "";
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getImageHref() {
		return imageHref;
	}

	public void setImageHref(String imageHref) {
		this.imageHref = imageHref;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "AddressBookItem{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", nums='" + nums + '\'' +
				", nodeNums='" + nodeNums + '\'' +
				", type='" + type + '\'' +
				", departmentName='" + departmentName + '\'' +
				", imageHref='" + imageHref + '\'' +
				", commonGroup='" + position + '\'' +
				", tel='" + tel + '\'' +
				", phone='" + phone + '\'' +
				", email='" + email + '\'' +
				", address='" + address + '\'' +
				", imid='" + imid + '\'' +
				'}';
	}
}
