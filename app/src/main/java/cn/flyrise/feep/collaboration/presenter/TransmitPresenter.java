package cn.flyrise.feep.collaboration.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import cn.flyrise.android.protocol.entity.CollaborationSendDoRequest;
import cn.flyrise.android.protocol.entity.CollaborationTransmitDetailsResponse;
import cn.flyrise.android.protocol.entity.CollaborationTransmitRequest;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.collaboration.utility.CollaborationDetailHelper;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.CollaborationType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.media.common.AttachmentBeanConverter;
import cn.flyrise.feep.utils.Patches;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by klc on 2017/5/3.
 */

public class TransmitPresenter extends NewCollaborationPresenter {

	private String mOriginId;
	private List<Matter> addBodyMatters = new ArrayList<>();
	private List<Matter> deleteMatters;

	public TransmitPresenter(NewCollaborationView mView, boolean isNewImport) {
		super(mView, isNewImport);
		deleteMatters = new ArrayList<>();
	}

	public void loadData(Intent intent) {
		loadDataForTransmit(intent);
	}

	public void transmitCollaboration(Context context) {
		mCollaboration.flow.getNodes().get(0).setStatus(X.NodeState.Checked);// 所有流程发出去了,根节点都被处理,很奇怪干嘛这个处理不在服务器做
		final CollaborationSendDoRequest requestContent = new CollaborationSendDoRequest();
		requestContent.setRequestType(CollaborationType.Forwarding);
		requestContent.setOriginalId(mOriginId);
		requestContent.setIdea(mCollaboration.option);
		requestContent.setChangeIdea(mCollaboration.isChangeIdea);
		requestContent.setDeleteRelationItem(getDeleteRelationItem());

		//移除补充正文的关联事项去上传：
		if (!CommonUtil.isEmptyList(mSelectedMatter) && !CommonUtil.isEmptyList(addBodyMatters)) {
			mSelectedMatter = new ArrayList<>(mSelectedMatter); //List 不支持 remove 操作，在这里我们转化一下....
			mSelectedMatter.removeAll(addBodyMatters);
		}
		checkSend(context, requestContent);
	}

	@Override
	void checkSend(Context context, CollaborationSendDoRequest requestContent) {
		if (FunctionManager.hasPatch(Patches.PATCH_RELATED_MATTERS)) {
			uploadAssociation(context, requestContent);
		}
		else {
			sendContent(context, requestContent);
		}
	}

	private String getDeleteRelationItem() {
		StringBuilder deleteBuilder = new StringBuilder();
		for (Matter deleteMatter : deleteMatters) {
			deleteBuilder.append(deleteMatter.masterKey).append(",");
		}
		if (deleteBuilder.length() > 0) deleteBuilder.deleteCharAt(deleteBuilder.length() - 1);
		return deleteBuilder.toString();
	}

	private void loadDataForTransmit(Intent intent) {
		mView.showLoading();
		mOriginId = intent.getStringExtra("collaborationId");
		CollaborationTransmitRequest request = new CollaborationTransmitRequest(mOriginId, "", true); //APi修改,后面两个参数已经没用了。

		FEHttpClient.getInstance().post(request, new ResponseCallback<CollaborationTransmitDetailsResponse>() {
			@Override
			public void onCompleted(CollaborationTransmitDetailsResponse response) {
				mView.hideLoading();
				mCollaboration = response.result;
				mNetworkAttachments = AttachmentBeanConverter.convert(mCollaboration.getAttachmentList());
				mSelectedMatter = CollaborationDetailHelper.relationItemToMatter(mCollaboration.getRelationList());
				addBodyMatters = CollaborationDetailHelper.relationItemToMatter(mCollaboration.getSupplyContentRelationList());
				mView.displayView(mCollaboration);
				mView.setFileTextCount(getAttachmentCount());
				mView.setHasFlow(hasFlow());
				mView.setAssociationCount(mSelectedMatter == null ? 0 : mSelectedMatter.size());
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				mView.hideLoading();
			}
		});
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_ASSOCAIATION_REQUEST_CODE) {
			if (data != null) {
				Parcelable[] parcelables = data.getParcelableArrayExtra("selectedAssociation");
				Matter[] associations = Arrays.copyOf(parcelables, parcelables.length, Matter[].class);
				mSelectedMatter = Arrays.asList(associations);
				mView.setAssociationCount(mSelectedMatter.size());
				deleteAddBodyMatter();
			}
			return true;
		}
		return super.onActivityResult(requestCode, resultCode, data);
	}

	private void deleteAddBodyMatter() {
		for (Matter matter : addBodyMatters) {
			if (!mSelectedMatter.contains(matter)) {
				deleteMatters.add(matter);
			}
		}
		addBodyMatters.removeAll(deleteMatters);
	}
}
