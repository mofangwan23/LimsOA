package cn.flyrise.feep.robot.operation.message;

import android.text.TextUtils;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.Robot;
import cn.flyrise.feep.robot.util.RobotSearchMessageDataUtil;
import cn.squirtlez.frouter.FRouter;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017-6-29.
 * 计划
 */

public class WorkPlanOperation extends BaseOperation {

	@Override
	public void open() {
		openMessage();
	}

	@Override
	public void search() {
		String username = mOperationModule.username;
		if (!TextUtils.isEmpty(username)) {
			queryContact(username);
			return;
		}

		if (mOperationModule.grammarResultListener != null) {
			mOperationModule.grammarResultListener.onError();
		}
	}

	@Override
	public void create() {
		String username = mOperationModule.username;
		if (!TextUtils.isEmpty(username)) {
			queryContact(username);
			return;
		}

		createWorkPlan("");
	}

	public void createWorkPlan(String userId) {
		if (TextUtils.isEmpty(userId)) {
			FRouter.build(mContext, "/plan/create").go();
			return;
		}

		ArrayList<String> userIds = new ArrayList<>();
		userIds.add(userId);

		FRouter.build(mContext, "/plan/create")
				.withStringArray("userIds", userIds)
				.go();
	}

	@Override
	public void handleAddressBook(AddressBook addressBook) {
		if (addressBook == null || TextUtils.isEmpty(addressBook.userId)) {
			return;
		}

		String operationType = mOperationModule.operationType;
		if (TextUtils.equals(Robot.operation.searchType, operationType)) {
			int messageId = mOperationModule.getMessageId();
			RobotSearchMessageDataUtil.getInstance()
					.setContext(mContext, messageId)
					.setMessageId(messageId)
					.setAddressBook(addressBook)
					.setListener(mOperationModule.grammarResultListener)
					.searchMessageText(addressBook.userId);
		}
		else if (TextUtils.equals(Robot.operation.createType, operationType)) {
			createWorkPlan(addressBook.userId);
		}
	}
}
