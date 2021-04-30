package cn.flyrise.feep.robot.operation.message;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.robot.Robot;
import cn.flyrise.feep.robot.bean.RobotEmailNumberRequest;
import cn.flyrise.feep.robot.bean.RobotEmailNumberResponse;
import cn.flyrise.feep.robot.manager.FeepOperationManager.OnMessageGrammarResultListener;
import cn.flyrise.feep.robot.module.RobotModuleItem;
import cn.flyrise.feep.robot.view.RobotUnderstanderActivity;
import cn.squirtlez.frouter.FRouter;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017-6-29.
 * 邮箱
 */
public class EmailOperation extends BaseOperation {

	private List<String> mMailAccountList;
	private String mCurrentMailAccount;

	@Override
	public void open() {
		openMessage();
	}

	@Override
	public void search() {
		requestMailBoxInfo();
	}

	@Override
	public void create() {
		FRouter.build(mContext, "/mail/create").go();
	}

	private void requestMailBoxInfo() {
		if (mCurrentMailAccount == null) {
			mCurrentMailAccount = CoreZygote.getLoginUserServices().getUserName();
		}
		final OnMessageGrammarResultListener grammarResultListener = mOperationModule.grammarResultListener;

		RobotEmailNumberRequest request = new RobotEmailNumberRequest(mCurrentMailAccount);
		FEHttpClient.getInstance().post(request, new ResponseCallback<RobotEmailNumberResponse>(this) {
			@Override
			public void onPreExecute() {
			}

			@Override
			public void onCompleted(RobotEmailNumberResponse response) {
				mMailAccountList = response.mailList;
				if (mCurrentMailAccount == null) {
					mCurrentMailAccount = mMailAccountList.get(0);
				}
				if (mMailAccountList != null && mMailAccountList.size() > 0 && mContext instanceof RobotUnderstanderActivity) {
					List<RobotModuleItem> robotModuleItems = new LinkedList<>();
					robotModuleItems.add(new RobotModuleItem.Builder()
							.setModuleParentType(mOperationModule.getMessageId())
							.setIndexType(Robot.adapter.ROBOT_CONTENT_EMAIL)
							.setTextList(mMailAccountList)
							.setTitle(mOperationModule.username)
							.create());

					if (grammarResultListener != null) {
						grammarResultListener.onGrammarResultItems(robotModuleItems);
					}
				}
				else {
					FRouter.build(mContext, "/mail/search")
							.withString("extra_box_name", "InBox/Inner")
							.withString("mail_search_text", mOperationModule.username)
							.go();
				}
			}

			@Override
			public void onFailure(RepositoryException responseException) {
			}
		});
	}
}
