package cn.flyrise.feep.robot.operation.message;

import android.text.TextUtils;

import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * Created by Administrator on 2017-6-29.
 * 打电话、发短信
 */

public class TelOrSmsOperation extends BaseOperation {

	@Override
	public void open() {
		String username = mOperationModule.username;
		if (!TextUtils.isEmpty(username)) {
			queryContact(username);
			return;
		}

		if (mOperationModule.grammarResultListener != null) {
			String messageType = mOperationModule.messageType;
			String text = TextUtils.equals(messageType, "662")
					? TextUtils.equals(messageType, "663")
					? CommonUtil.getString(R.string.robot_sms_hint)
					: CommonUtil.getString(R.string.robot_tel_hint)
					: CommonUtil.getString(R.string.robot_grammar_error);
			mOperationModule.grammarResultListener.onGrammarText(text);
		}
	}
}
