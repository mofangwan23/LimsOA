/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-8-17 下午2:10:31
 */

package cn.flyrise.feep.form.util;

import android.text.TextUtils;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.form.been.FormSubNodeInfo;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</   br> 修改时间：2012-8-17</br> 修改备注：</br>
 */
public class ChooseSubNodeUtil {

	private final HashMap<Integer, ArrayList<FormSubNodeInfo>> checkedSubNodes;

	public ChooseSubNodeUtil() {
		checkedSubNodes = new HashMap<>();
	}

	public String deleteAllNode(int position) {
		final ArrayList<FormSubNodeInfo> subNodes = getCheckedSubNodeList(position);
		if (subNodes != null) {
			subNodes.clear();
		}
		return "";
	}

	/**
	 * 选择节点（会进行相关操作：已经选中的数据集中删除，未选中的就添加）
	 * @param position 出口到节点的index
	 * @param subNode 人员节点
	 * @param isMulChoose 是否多选
	 * @return boolean 标志点击之前是否已经选中，true选中，false未选
	 */
	public boolean chooseNode(int position, FormSubNodeInfo subNode, boolean isMulChoose) {
		final ArrayList<FormSubNodeInfo> subNodes = getCheckedSubNodeList(position);
		boolean isContains = false;
		if (subNodes == null) {
			return false;
		}
		if (subNode != null) {
			final int index = getContainsIndex(subNodes, subNode);
			if (index != -1) {
				subNodes.remove(index);
				isContains = true;
			}
			else {
				if (!isMulChoose) {// 非多选状态时，先清空所有节点
					subNodes.clear();
				}
				subNodes.add(subNode);
				isContains = false;
			}
		}
		return isContains;
	}

	/**
	 * 判断一个集合中是否包含某个元素，并获取它的索引位置
	 * @return 索引位置 -1不包含，其他包含
	 */
	public int getContainsIndex(int position, FormSubNodeInfo subNode) {
		final ArrayList<FormSubNodeInfo> subNodeInfos = getCheckedSubNodeList(position);
		return getContainsIndex(subNodeInfos, subNode);
	}

	/**
	 * 判断一个集合中是否包含某个元素，并获取它的索引位置
	 * @return 索引位置 -1不包含，其他包含
	 */
	private int getContainsIndex(ArrayList<FormSubNodeInfo> subNodeInfos, FormSubNodeInfo subNode) {
		if (subNodeInfos == null || subNode == null) {
			return -1;
		}
		final ReferenceItem referenceItem1 = subNode.getReferenceItem();
		if (referenceItem1 == null) {
			return -1;
		}
		final String key1 = referenceItem1.getKey();
		final String value1 = referenceItem1.getValue();
		for (int i = 0; i < subNodeInfos.size(); i++) {
			final ReferenceItem referenceItem2 = subNodeInfos.get(i).getReferenceItem();
			if (referenceItem2 != null) {
				final String key2 = referenceItem2.getKey();
				final String value2 = referenceItem2.getValue();
				if (key1 != null && key1.equals(key2) && value1 != null && value1.equals(value2)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 获取对应索引位置的节点集合
	 */
	private ArrayList<FormSubNodeInfo> getCheckedSubNodeList(int position) {
		ArrayList<FormSubNodeInfo> subNodes;
		if (checkedSubNodes.containsKey(position)) {
			subNodes = checkedSubNodes.get(position);
		}
		else {
			subNodes = new ArrayList<>();
			checkedSubNodes.put(position, subNodes);
		}
		return subNodes;
	}

	/**
	 * 生成节点字符串
	 */
	public String getNodesString(int position) {
		final ArrayList<FormSubNodeInfo> subNodes = getCheckedSubNodeList(position);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < subNodes.size(); i++) {
			final ReferenceItem referenceItem = subNodes.get(i).getReferenceItem();
			if (TextUtils.isEmpty(referenceItem.getValue())) continue;
			if (i == subNodes.size() - 1) {
				sb.append(referenceItem.getValue());
			}
			else {
				sb.append(referenceItem.getValue()).append("，");
			}
		}
		return sb.toString();
	}

	/**
	 * 清空选中的节点
	 */
	public void clearCheckSubNodes() {
		checkedSubNodes.clear();
	}

	/**
	 * 清空某一节点下选中的节点
	 */
	public void clearCheckSubNodesWithIndex(int index) {
		final ArrayList<FormSubNodeInfo> subNodes = getCheckedSubNodeList(index);
		if (subNodes != null) {
			subNodes.clear();
		}
	}

	/**
	 * @return the checkedSubNodes
	 */
	public HashMap<Integer, ArrayList<FormSubNodeInfo>> getCheckedSubNodes() {
		return checkedSubNodes;
	}
}
