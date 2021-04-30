/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-8
 */
package cn.flyrise.feep.collaboration.view.workflow;

import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.view.Avatar;
import cn.flyrise.feep.collaboration.view.workflow.WorkFlowNode.NodeType;
import cn.flyrise.feep.core.common.X.NodeState;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.PixelUtil;


/**
 * <b>类功能描述：</b><div style="margin-left:40px;margin-top:-10px"> 处理流程图的计算与绘制 </div>
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
class WorkFlowDrawer {

	private final static int cellWidth, cellHeight;
	private final static int paddingTop, paddingLeft;

	static {// 为什么要这些~因为View没画出来不会计算这些宽高啊~~%>_<%
		cellWidth = PixelUtil.dipToPx(54);
		cellHeight = PixelUtil.dipToPx(60);
		paddingTop = PixelUtil.dipToPx(10);
		paddingLeft = PixelUtil.dipToPx(10);
	}

	static int spaceLeft = PixelUtil.dipToPx(10);

	/*--此工具类无需实例化--*/
	private WorkFlowDrawer() {
	}

	/*--绘制流程图--*/
	static void drawTree(final WorkFlowView view) {
		lastLeafNodeY = 0;
		calculateXY(WorkFlowView.rootNode, 1);// 第一次递归调用参数1,代表第一层,参数0代表叶节点高度
		view.content_Rl.removeAllViews();
		view.content_Rl.addView(view.nodeSelected_Iv);
		view.content_Rl.addView(view.nodeDelete_Iv);
		view.content_Rl.addView(view.spaceRight);
		view.spaceRightLp.leftMargin = 0;
		for (final WorkFlowNode node : WorkFlowView.allNode) {
			if (node == null || node.getNodeId() == null) {
				throw new IllegalArgumentException("参数有误");
			}
			/*--加节点图标--*/
			if (node.getParent() == null && node != WorkFlowView.rootNode) {
				continue;// 跳过无效节点
			}
			final Avatar avatar = new Avatar(view.getContext());
			avatar.setMinimumWidth(PixelUtil.dipToPx(53));
			avatar.setNeedInterceptName();
			avatar.setAvatarFace(node.getType(), node.getNodeId(), node.getNodeName(), node.getImageHref());
			avatar.setName(node.getNodeName());
			avatar.setGravity(Gravity.CENTER_HORIZONTAL);
			node.setAvatar(avatar);
			final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			lp.setMargins(node.getNodeX() - cellWidth / 2, (int) (node.getNodeY() - cellHeight / 2 + cellHeight * 0.2), 0,
					0);// 下移0.5个神位让头像在中间
			avatar.setOnClickListener(v -> {
				if ((node.getNodeType() == NodeType.existed || node.getNodeType() == NodeType.locked) && view.lock) {
					FEToast.showMessage(view.getContext().getResources().getString(R.string.flow_nodeunmodify));
					return;
				}
				if (node != WorkFlowView.currentNode) {
					WorkFlowView.currentNode = node;
					view.changeHint(node.getNodeX(), node.getNodeY());
				}
				view.showBubbleWindow(avatar);
			});
			avatar.setOnLongClickListener(v -> {
				if ((node.getNodeType() == NodeType.existed || node.getNodeType() == NodeType.locked) && view.lock) {
					FEToast.showMessage(view.getContext().getResources().getString(R.string.flow_nodeunmodify));
					return true;
				}
				if (node.getNodeType() == NodeType.existed && WorkFlowView.nodeCount == 1 || node.getNodeType() == NodeType.user) {
					return true;
				}
				if (!view.seleted) {
					return true;
				}
				WorkFlowView.currentNode = node;
				view.changeHint(WorkFlowView.currentNode.getNodeX(), WorkFlowView.currentNode.getNodeY());
				final RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) view.nodeDelete_Iv.getLayoutParams();
				lp1.setMargins(node.getNodeX(), node.getNodeY() - cellHeight / 3 * 2, 0, 0);
				view.nodeDelete_Iv.setLayoutParams(lp1);
				view.nodeDelete_Iv.setVisibility(View.VISIBLE);
				view.nodeDelete_Iv.bringToFront();
				return true;
			});
			view.content_Rl.addView(avatar, lp);
			if (node.getNodeX() > view.spaceRightLp.leftMargin) {
				view.spaceRightLp.leftMargin = node.getNodeX();
			}
			if (node.getStatus() == NodeState.Checked) {
				avatar.setReaded(true);
			}
			else {
				avatar.setReaded(false);
			}
			/*--End--*/
			/*--处理节点连线--*/
			if (node.getParent() != null) {
				addLigature(view, (node.getNodeX() + node.getParent().getNodeX()) / 2, node.getNodeX() - cellWidth / 2, node.getNodeY(),
						node.getNodeY());
				if (node == node.getParent().getChildNodes().get(0) && node.getParent().getChildNodes().size() > 1) {
					addLigature(view, (node.getNodeX() + node.getParent().getNodeX()) / 2,
							(node.getNodeX() + node.getParent().getNodeX()) / 2, node.getNodeY(),
							node.getParent().getChildNodes().get(node.getParent().getChildNodes().size() - 1).getNodeY());
				}
			}
			if (node.getChildNodes() != null && node.getChildNodes().size() != 0) {
				addLigature(view, node.getNodeX() + cellWidth / 2, (node.getNodeX() + node.getChildNodes().get(0).getNodeX()) / 2,
						node.getNodeY(), node.getNodeY());
			}
			/*--End--*/
		}
		view.changeHint(WorkFlowView.currentNode.getNodeX(), WorkFlowView.currentNode.getNodeY());
	}

	// 记录上一次绘制叶节点的高度(几个Y)
	private static int lastLeafNodeY;

	/*--数据结构转化为图形化位置计算(节点发生变化都需要调用,递归,先序遍历)--*/
	private static void calculateXY(WorkFlowNode wfn, int currentTreeDepth) {
		if (wfn.getChildNodes().isEmpty()) {
			// 叶节点的坐标依照树深及根节点顺序计算
			wfn.setNodeX((int) ((currentTreeDepth - 1) * (1 + 0.6) * cellWidth + paddingLeft + cellWidth / 2 + spaceLeft));// 连线占0.6个头像宽
			wfn.setNodeY((int) (lastLeafNodeY * cellHeight * 1.25 + paddingTop + cellHeight / 2));// 上下节点间隔0.1个自身高度
			// 递归树绘制节点先绘制其子节点，故叶节点位置最先绘制，记录用以判断下一个叶节点位置
			lastLeafNodeY += 1;// 一个yeah节点一个Y
		}
		else {
			// 非叶节点依照树深及两端叶节点居中计算
			for (final WorkFlowNode t_Node : wfn.getChildNodes()) {
				calculateXY(t_Node, currentTreeDepth + 1);
			}
			wfn.setNodeX((int) ((currentTreeDepth - 1) * (1 + 0.6) * cellWidth + paddingLeft + cellWidth / 2 + spaceLeft));
			wfn.setNodeY((wfn.getChildNodes().get(0).getNodeY() + wfn.getChildNodes().get(wfn.getChildNodes().size() - 1).getNodeY()) / 2);
		}
	}

	/*--添加节点间连接线--*/
	private static void addLigature(WorkFlowView view, int fromX, int toX, int fromY, int toY) {
		RelativeLayout.LayoutParams lp;
		if (fromX == toX) {
			lp = new RelativeLayout.LayoutParams(PixelUtil.dipToPx(1), toY - fromY);
		}
		else {
			lp = new RelativeLayout.LayoutParams(toX - fromX, PixelUtil.dipToPx(1));
		}
		lp.setMargins(fromX, fromY, 0, 0);
		final ImageView ligature_Iv = new ImageView(view.getContext());
		ligature_Iv.setBackgroundColor(0xffc9c9c9);
		view.content_Rl.addView(ligature_Iv, lp);
	}
}
