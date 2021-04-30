package cn.flyrise.feep.robot.operation.message;

import android.text.TextUtils;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * Created by Administrator on 2017-6-29.
 * 发起聊天
 */

public class ChatIMOperation extends BaseOperation {

	@Override
	public void open() {
		openTelOrSms();
	}

	@Override
	public void create() {
		openTelOrSms();
	}


	private void openTelOrSms() {
		if (!TextUtils.isEmpty(mOperationModule.username)) {
			queryContact(mOperationModule.username);
			return;
		}

		if (mOperationModule.grammarResultListener != null) {
			String text = TextUtils.equals(mOperationModule.messageType, "661")
					? CommonUtil.getString(R.string.robot_send_im_message)
					: CommonUtil.getString(R.string.robot_grammar_error);
			mOperationModule.grammarResultListener.onGrammarText(text);
		}
	}
}
