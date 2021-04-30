package cn.flyrise.feep.robot.operation.message;

import android.text.TextUtils;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.Robot;
import cn.flyrise.feep.robot.util.RobotSearchMessageDataUtil;
import cn.squirtlez.frouter.FRouter;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017-6-29.
 * 语音识别：协同、审批、表单、待办、已办、已发
 */

public class CollaborationOperation extends BaseOperation {

	@Override
	public void open() {
		int messageId = mOperationModule.getMessageId();
		if (44 == messageId || 10 == messageId) {
			operationCollaborationMessage(0);
		}
		else if (messageId == 0 || messageId == 1 || messageId == 4) {
			operationCollaborationMessage(messageId);
		}
		else {
			openMessage();
		}
	}

	@Override
	public void search() {
		int messageId = mOperationModule.getMessageId();
		if (messageId == 10 || messageId == 44) {
			if (mOperationModule.grammarResultListener != null) {
				mOperationModule.grammarResultListener.onGrammarText(CommonUtil.getString(R.string.robot_error_search_collaboration));
			}
			return;
		}
		if (!isMessageType(messageId)) {
			searchMessage(messageId);
			return;
		}
		if (!TextUtils.isEmpty(mOperationModule.wildcard)) {
			searchCollaborationListItem(mOperationModule.wildcard, null, mOperationModule.operationType);
		}
		else if (!TextUtils.isEmpty(mOperationModule.username)) {
			queryContact(mOperationModule.username);
		}
		else {
			searchMessage(messageId);
		}
	}

	private boolean isMessageType(int messageId) {//是否为待办、已办、已发
		return messageId == 0 || messageId == 1 || messageId == 4;
	}

	@Override
	public void create() {
		String messagetType = mOperationModule.messageType;
		if (TextUtils.equals(messagetType, "10")) {
			createForm();
			return;
		}

		if (TextUtils.equals(messagetType, "44")) {
			String username = mOperationModule.username;
			if (TextUtils.isEmpty(username)) {
				createCollaboration("");
				return;
			}

			queryContact(username);
		}
	}

	@Override
	public void handleAddressBook(AddressBook addressBook) {
		if (addressBook == null || TextUtils.isEmpty(addressBook.userId)) {
			return;
		}
		if (TextUtils.equals(Robot.operation.createType, mOperationModule.operationType)) {
			createCollaboration(addressBook.userId);
			return;
		}
		searchCollaborationListItem(mOperationModule.username, addressBook, mOperationModule.operationType);
	}

	private void searchCollaborationListItem(String searchKey, AddressBook addressBook, String operationType) {
		if (TextUtils.equals(Robot.operation.searchType, operationType)) {
			int messageId = mOperationModule.getMessageId();
			RobotSearchMessageDataUtil.getInstance()
					.setContext(mContext, messageId)
					.setMessageId(messageId)
					.setAddressBook(addressBook)
					.setListener(mOperationModule.grammarResultListener)
					.searchMessageText(searchKey);
		}
	}

	private void createCollaboration(String userId) {
		if (TextUtils.isEmpty(userId)) {
			FRouter.build(mContext, "/collaboration/create").go();
			return;
		}

		ArrayList<String> userIds = new ArrayList<>();
		userIds.add(userId);
		FRouter.build(mContext, "/collaboration/create")
				.withInt("fromType", 100)
				.withStringArray("userIds", userIds).go();
	}

	private void createForm() {
		Module module = FunctionManager.findModule(Func.NewForm);
		if (module != null && !TextUtils.isEmpty(module.url)) {
			FRouter.build(mContext, "/x5/browser")
					.withString("appointURL", module.url)
					.withInt("moduleId", Func.Default)
					.go();
		}
		else {
			FRouter.build(mContext, "/flow/list").go();
		}
	}

	private void operationCollaborationMessage(int messageId) {
		if (Robot.operation.openType.equals(mOperationModule.operationType)) {
			FRouter.build(mContext, "/collaboration/list")
					.withInt("request_type", messageId).go();
		}
	}

}
