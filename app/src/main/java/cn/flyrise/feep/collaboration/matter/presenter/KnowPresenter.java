package cn.flyrise.feep.collaboration.matter.presenter;

import android.content.Context;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.core.CoreZygote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.flyrise.android.protocol.entity.MatterFolderTreeRequest;
import cn.flyrise.android.protocol.entity.MatterFolderTreeResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.model.MatterPageInfo;
import cn.flyrise.feep.collaboration.matter.model.DirectoryNode;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by klc on 2017/5/18.
 */

public class KnowPresenter {

	private KnowView mView;

	//DATA
	private List<DirectoryNode> mLeftTreeNodes;
	private List<DirectoryNode> mTopTreeNodes;
	private Map<DirectoryNode, MatterPageInfo> cacheFileList;
	private DirectoryNode mCurrentNode;

	public KnowPresenter(KnowView knowView) {
		this.mView = knowView;
		mLeftTreeNodes = new ArrayList<>();
		mTopTreeNodes = new ArrayList<>();
		cacheFileList = new HashMap<>();
	}

	public void loadFolderTree() {
		FEHttpClient.getInstance().post(new MatterFolderTreeRequest(), new ResponseCallback<MatterFolderTreeResponse>() {
			@Override
			public void onCompleted(MatterFolderTreeResponse response) {
				DirectoryNode rootNode = new DirectoryNode();
				rootNode.id = "-1";
				rootNode.name = CommonUtil.getString(R.string.flow_root);
				rootNode.childNode = new ArrayList<>();

				if (((FEApplication) CoreZygote.getContext()).isGroupVersion()) {
					DirectoryNode groupNode = new DirectoryNode();
					groupNode.id = KnowKeyValue.GROUPROOTFOLDERID;
					groupNode.name = CommonUtil.getString(R.string.know_group_folder);
					groupNode.childNode = response.result.groupFolderTree;
					groupNode.fatherNode = rootNode;
					rootNode.childNode.add(groupNode);
					mLeftTreeNodes.add(groupNode);
				}

				DirectoryNode unitNode = new DirectoryNode();
				unitNode.id = KnowKeyValue.UNITROOTFOLDERID;
				unitNode.name = CommonUtil.getString(R.string.know_unit_folder);
				unitNode.childNode = response.result.unitFolderTree;
				unitNode.fatherNode = rootNode;
				rootNode.childNode.add(unitNode);
				mLeftTreeNodes.add(unitNode);

				DirectoryNode personNode = new DirectoryNode();
				personNode.id = KnowKeyValue.PERSONROOTFOLDERID;
				personNode.name = CommonUtil.getString(R.string.know_person_folder);
				personNode.childNode = response.result.personalFolderTree;
				personNode.fatherNode = rootNode;
				rootNode.childNode.add(personNode);
				mLeftTreeNodes.add(personNode);

				MatterPageInfo pageInfo = new MatterPageInfo();
				pageInfo.currentPage = 1;
				cacheFileList.put(rootNode, pageInfo);
				mTopTreeNodes.add(rootNode);
				mCurrentNode = rootNode;
				mView.displayTopListData(mTopTreeNodes);
				mView.displayLeftListData(mLeftTreeNodes);
				for (DirectoryNode directoryNode : mLeftTreeNodes) {
					setChildNode(directoryNode);
				}
			}
		});
	}

	private void setChildNode(DirectoryNode node) {
		if (!CommonUtil.isEmptyList(node.childNode)) {
			for (DirectoryNode childNode : node.childNode) {
				childNode.fatherNode = node;
				setChildNode(childNode);
			}
		}
	}

	public void leftFolderClick(DirectoryNode node) {
		mCurrentNode = node;
		MatterPageInfo pageInfo;
		if (cacheFileList.containsKey(node)) {
			pageInfo = cacheFileList.get(node);
		}
		else {
			pageInfo = new MatterPageInfo();
			cacheFileList.put(node, pageInfo);
		}
		mView.showLeftHeadView(true);
		mView.displayRightListData(node, pageInfo);
		mView.displayLeftListData(node.childNode);
		mTopTreeNodes.add(node);
		mView.displayTopListData(mTopTreeNodes);
	}

	public void leftHeadClick() {
		mCurrentNode = mCurrentNode.fatherNode;
		mTopTreeNodes.remove(mTopTreeNodes.size() - 1);
		mView.displayTopListData(mTopTreeNodes);
		mView.displayLeftListData(mCurrentNode.childNode);
		if ("-1".equals(mCurrentNode.id)) {
			mView.showLeftHeadView(false);
		}
		MatterPageInfo pageInfo = cacheFileList.get(mCurrentNode);
		mView.displayRightListData(mCurrentNode, pageInfo);
	}

	public void topItemClick(DirectoryNode node) {
		mView.displayLeftListData(node.childNode);
		mCurrentNode = node;
		mTopTreeNodes = mTopTreeNodes.subList(0, mTopTreeNodes.indexOf(node) + 1);
		mView.displayTopListData(mTopTreeNodes);
		if ("-1".equals(mCurrentNode.id)) {
			mView.showLeftHeadView(false);
		}
		MatterPageInfo pageInfo = cacheFileList.get(mCurrentNode);
		mView.displayRightListData(mCurrentNode, pageInfo);
	}

	public DirectoryNode getmCurrentNode() {
		return mCurrentNode;
	}
}
