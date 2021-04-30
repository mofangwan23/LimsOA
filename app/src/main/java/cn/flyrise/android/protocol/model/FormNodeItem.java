//
// NodeItem.java
// feep
//
// Created by LuTH on 2011-11-26.
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.android.protocol.model;

import android.text.TextUtils;

public class FormNodeItem {

	// 表单节点类型：0普通节点、1多审批节点、2逻辑节点、3并节点、4结束节点。（显示节点0,1要选人；3、4、6不需要选人直接送给服务器；2节点不显示）
	public enum FromNodeType {
		FromNodeTypeNormal(0), // 普通节点
		FromNodeTypeMultiNode(1), // 多审批节点
		FromNodeTypeLogic(2), // 逻辑节点
		FromNodeTypeUnion(3), // 并节点
		FromNodeTypeOrion(6), // 或节点
		FromNodeTypeEnd(4); // 结束节点

		private final int value;

		FromNodeType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public FromNodeType FromNodeTypeEnum(int number) {
		FromNodeType e = null;
		for (final FromNodeType item : FromNodeType.values()) {
			if (number == item.value) {
				e = item;
				break;
			}
		}
		return e;
	}

	private String GUID;
	private String id;
	private String type;
	private String name;
	private String value;
	private String figureID;     // 办理人ID
	private String figureName;   // 办理人名称
	private String figureType;   // 办理人类型（1:人，3:岗位）
	private String isDefaultNode; // 是否是默认退回节点
	private String isSendPost;    // 是否默认只送办到岗位
	private boolean todo;         //7.0新增，表单节点可以直接送办

	public boolean isTodo() {
		return todo;
	}

	public void setTodo(boolean todo) {
		this.todo = todo;
	}

	public String getGUID() {
		return GUID;
	}

	public void setGUID(String gUID) {
		GUID = gUID;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public FromNodeType getType() {
		try {
			return FromNodeTypeEnum(Integer.valueOf(type));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setType(FromNodeType type) {
		this.type = type == null ? null : type.getValue() + "";
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

	public String getFigureID() {
		return figureID;
	}

	public void setFigureID(String figureID) {
		this.figureID = figureID;
	}

	public String getFigureName() {
		return figureName;
	}

	public void setFigureName(String figureName) {
		this.figureName = figureName;
	}

	public String getFigureType() {
		return figureType;
	}

	public void setFigureType(String figureType) {
		this.figureType = figureType;
	}

	public boolean isDefaultNode() {
		try {
			final Integer value = Integer.valueOf(isDefaultNode);
			return value != 0;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setDefaultNode(boolean isDefaultNode) {
		this.isDefaultNode = isDefaultNode ? "1" : "0";
	}

	public boolean isSendPost() {
		if (TextUtils.isEmpty(isSendPost)){
			return false;
		}
		try {
			final Integer value = Integer.valueOf(isSendPost);
			return value != 0;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setIsSendPost(boolean isSendPost) {
		this.isSendPost = isSendPost ? "1" : "0";
	}

}
