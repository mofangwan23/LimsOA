/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-8
 */
package cn.flyrise.feep.collaboration.view.workflow;

import android.text.TextUtils;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.android.protocol.model.FlowNode;
import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.android.protocol.model.FormNodeItem.FromNodeType;
import cn.flyrise.feep.collaboration.view.workflow.WorkFlowNode.NodeType;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.common.X.NodePermission;
import cn.flyrise.feep.core.common.X.NodeState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <b>类功能描述：</b><div style="margin-left:40px;margin-top:-10px"> 流程图数据转换工具 </div>
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class WorkFlowTranslater {

	// 所有节点用list保存返回,尽可能少递归遍历
	private static final List<WorkFlowNode> list = new ArrayList<>();
	// 显示的一串简单流程
	private static final StringBuilder name = new StringBuilder();
	private static String currentFlowNodeGUID = "";

	/*--此工具类无需实例化--*/
	private WorkFlowTranslater() {
	}

	/*--解析数据用以图形化展示--*/
	public static List<WorkFlowNode> translateProcessItem2Node(Flow flow, WorkFlowNode rootNode, String currentFlowNodeGUID) {
		list.clear();
		list.add(rootNode);
		if (flow == null) {
			return list;
		}
		WorkFlowTranslater.currentFlowNodeGUID = currentFlowNodeGUID;
		if (flow.getNodes() != null && flow.getNodes().size() != 0) {
			FlowNode rootNodeItem = flow.getNodes().get(0);
			if (rootNodeItem == null) {
				return list;
			}
			rootNode.setNodeId(rootNodeItem.getValue());
			rootNode.setParent(null);
			rootNode.setType(AddressBookType.Staff);
			rootNode.setStatus(rootNodeItem.getStatus());
			rootNode.setWflag(rootNodeItem.getWflag());
			rootNode.setGgId(rootNodeItem.getGUID());
			rootNode.setNodeType(NodeType.existed);
			rootNode.setEndorse(rootNodeItem.isEndorse());
			rootNode.setEndorseby(rootNodeItem.getEndorseby());
			rootNode.setNewnode(rootNodeItem.isNewnode());
			if (rootNodeItem.getGUID().equals(currentFlowNodeGUID)) {
				rootNode.setNodeType(NodeType.user);
			}
			rootNode.setNodeName(rootNodeItem.getName());
			rootNode.setCarryInfo(rootNodeItem);
			if (rootNodeItem.getSubnode() != null && rootNodeItem.getSubnode().size() != 0) {
				copyNodeItem2Node(rootNodeItem, rootNode);
			}
		}
		return list;
	}

	/*--递归复制结构--*/
	private static void copyNodeItem2Node(FlowNode nodeItem, WorkFlowNode parentNode) {
		if (nodeItem.getSubnode() == null || nodeItem.getSubnode().size() == 0) {
			final WorkFlowNode t_Node = new WorkFlowNode();
			list.add(t_Node);
			copyData2Node(nodeItem, t_Node);
			t_Node.setParent(parentNode);
			parentNode.getChildNodes().add(t_Node);
		}
		else {
			for (final FlowNode t_Item : nodeItem.getSubnode()) {
				final WorkFlowNode t_Node = new WorkFlowNode();
				list.add(t_Node);
				copyData2Node(t_Item, t_Node);
				t_Node.setParent(parentNode);
				parentNode.getChildNodes().add(t_Node);
				if (t_Item.getSubnode() != null && t_Item.getSubnode().size() != 0) {
					copyNodeItem2Node(t_Item, t_Node);
				}
			}
		}
	}

	/*--复制对应节点里面的数据--*/
	private static void copyData2Node(FlowNode item, WorkFlowNode node) {
		node.setNodeType(NodeType.existed);
		if (item.getGUID().equals(currentFlowNodeGUID)) {
			node.setNodeType(NodeType.user);
		}
		node.setNodeId(item.getValue());
		node.setNodeName(item.getName());
		node.setStatus(item.getStatus());
		node.setType(item.getType());
		node.setCarryInfo(item);
		node.setEndorse(item.isEndorse());
		node.setEndorseby(item.getEndorseby());
		node.setNewnode(item.isNewnode());
		node.setWflag(item.getWflag());
		node.setGgId(item.getGUID());
	}

	/*--将流程图形结果保存--*/
	public static void translateNode2ProcessItem(WorkFlowNode rootNode, Flow flow) {
		// flow.setNodeCollection(new NodeCollection());
		flow.setNodes(new ArrayList<>());
		final FlowNode t_Item = new FlowNode();
		copyData2NodeItem(rootNode, t_Item);
		flow.getNodes().add(t_Item);
		if (!rootNode.getChildNodes().isEmpty()) {
			t_Item.setSubnode(new ArrayList<>());
			copyNode2ProcessItem(rootNode, t_Item);
		}
		flow.setName("");
		flow.setName(name.toString().substring(0, name.length() - 1));
		name.delete(0, name.length());
	}

	/*--递归复制结构--*/
	private static void copyNode2ProcessItem(WorkFlowNode node, FlowNode parentProcessItemList) {
		if (node.getChildNodes().isEmpty()) {
			final FlowNode t_Item = new FlowNode();
			copyData2NodeItem(node, t_Item);
			parentProcessItemList.getSubnode().add(t_Item);
		}
		else {
			for (final WorkFlowNode t_Node : node.getChildNodes()) {
				final FlowNode t_Item = new FlowNode();
				copyData2NodeItem(t_Node, t_Item);
				parentProcessItemList.getSubnode().add(t_Item);
				if (!t_Node.getChildNodes().isEmpty()) {
					t_Item.setSubnode(new ArrayList<>());
					copyNode2ProcessItem(t_Node, t_Item);
				}
			}
		}
	}

	/*--复制对应节点里面的数据--*/
	private static void copyData2NodeItem(WorkFlowNode t_Node, FlowNode t_Item) {
		name.append(t_Node.getNodeName()).append(",");
		// 这个if不好
		if (t_Node.getCarryInfo() == null || t_Node.getCarryInfo() instanceof AddressBookItem) {
			t_Item.setGUID(UUID.randomUUID().toString());
		}
		else {
			t_Item.setGUID(((FlowNode) t_Node.getCarryInfo()).getGUID());
		}
		t_Item.setName(t_Node.getNodeName());
		t_Item.setStatus(t_Node.getStatus() == NodeState.Uncheck ? NodeState.Uncheck : t_Node.getStatus());
		t_Item.setType(t_Node.getType() == AddressBookType.Company && !TextUtils.equals(t_Node.getNodeName(), "并发")
				? AddressBookType.Department
				: t_Node.getType());
		t_Item.setValue(t_Node.getNodeId());
		t_Item.setPopudom(NodePermission.Rollback);
		t_Item.setEndorse(t_Node.isEndorse());
		t_Item.setEndorseby(t_Node.getEndorseby());
		t_Item.setNewnode(t_Node.isNewnode());
	}

	/*--复制表单加签人员数据--*/
	public static void translateNode2FormSendDoItem(List<AddressBookItem> allPerson, List<FormNodeItem> nodeItems, String GUID) {
		if (nodeItems == null) {
			return;
		}
		final FormNodeItem t_FormNodeItem = new FormNodeItem();
		t_FormNodeItem.setGUID(GUID);
		t_FormNodeItem.setType(FromNodeType.FromNodeTypeMultiNode);
		final StringBuilder sb = new StringBuilder();
		final StringBuilder name = new StringBuilder();
		for (final AddressBookItem person : allPerson) {
			name.append(person.getName()).append(",");
			sb.append(person.getType() == AddressBookType.Staff ? ("X" + person.getId()) : ("Y" + person.getId()));
			sb.append(",");
		}
		if (allPerson.size() > 0) {
			t_FormNodeItem.setValue(sb.toString().substring(0, sb.length() - 1));
			t_FormNodeItem.setName(name.toString().substring(0, name.length() - 1));
		}
		nodeItems.add(t_FormNodeItem);
	}
}
