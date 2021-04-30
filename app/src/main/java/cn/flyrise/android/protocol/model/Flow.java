/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-19 下午2:46:11
 */
package cn.flyrise.android.protocol.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述：</br>
 * @author zms
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class Flow implements Cloneable  {

	private String name;
	private String GUID;
	private List<FlowNode> nodes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGUID() {
		return GUID;
	}

	public void setGUID(String gUID) {
		GUID = gUID;
	}

	public List<FlowNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<FlowNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public Flow clone() {
		try {
			Flow cloneFlow = (Flow) super.clone();
			cloneFlow.nodes = new ArrayList<>();
			for (FlowNode flowNode : nodes) {
				cloneFlow.nodes.add(flowNode.clone());
			}
			return cloneFlow;
		}catch (Exception e){
			return null;
		}


	}
}
