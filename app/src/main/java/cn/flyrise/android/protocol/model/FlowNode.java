/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-19 下午2:46:11
 */
package cn.flyrise.android.protocol.model;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.util.ArrayList;
import java.util.List;


/**
 * 类功能描述：</br>
 * @author zms
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class FlowNode implements Cloneable {

	private String GUID;
	private String type;
	private String name;
	private String value;
	private String status;
	private String popudom;
	private String endorse = "0";
	private String endorseby = "";
	private String newnode = "0";
	private String wflag;

	private List<FlowNode> subnode = new ArrayList<>();

	public String getWflag() {
		return wflag;
	}

	public void setWflag(String wflag) {
		this.wflag = wflag;
	}

	public String getGUID() {
		return GUID;
	}

	public void setGUID(String gUID) {
		GUID = gUID;
	}

	public int getType() {
		return CommonUtil.parseInt(type);
	}

	public void setType(int type) {
		this.type = type + "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getStatus() {
		return CommonUtil.parseInt(status);
	}

	public void setStatus(int status) {
		this.status = status + "";
	}

	public int getPopudom() {
		return CommonUtil.parseInt(popudom);
	}

	public void setPopudom(int popudom) {
		this.popudom = popudom + "";
	}

	public List<FlowNode> getSubnode() {
		return subnode;
	}

	public void setSubnode(List<FlowNode> subnode) {
		this.subnode = subnode;
	}

	public boolean isEndorse() {
		return "1".equals(endorse) ? true : false;
	}

	public void setEndorse(boolean isendorse) {
		this.endorse = isendorse ? "1" : "0";
	}

	public String getEndorseby() {
		return endorseby;
	}

	public void setEndorseby(String endorseby) {
		this.endorseby = endorseby;
	}

	public boolean isNewnode() {
		return "1".equals(newnode) ? true : false;
	}

	public void setNewnode(boolean isnewnode) {
		this.newnode = isnewnode ? "1" : "0";
	}

	@Override
	public FlowNode clone() {
		try {
			FlowNode cloneFlowNode = (FlowNode) super.clone();
			cloneFlowNode.subnode = new ArrayList<>();
			for (FlowNode flowNode : subnode) {
				cloneFlowNode.subnode.add(flowNode.clone());
			}
			return cloneFlowNode;
		} catch (Exception e) {
			return null;
		}
	}
}
