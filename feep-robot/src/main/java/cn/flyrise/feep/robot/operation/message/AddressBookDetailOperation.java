package cn.flyrise.feep.robot.operation.message;

import android.text.TextUtils;

import cn.flyrise.feep.robot.R;

/**
 * Created by Administrator on 2017-6-29.
 * 打开人员详情
 */

public class AddressBookDetailOperation extends BaseOperation {

	@Override
	public void open() {
		String username = mOperationModule.username;
		if (!TextUtils.isEmpty(username)) {
			queryContact(username);
			return;
		}

		if (mOperationModule.grammarResultListener != null) {
			mOperationModule.grammarResultListener.onGrammarText(mContext.getString(R.string.robot_address_detail));
		}
	}
}
