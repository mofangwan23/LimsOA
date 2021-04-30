package cn.flyrise.feep.robot.operation.message;

import android.text.TextUtils;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.squirtlez.frouter.FRouter;

/**
 * Created by Administrator on 2017-6-29.
 * 电话会议dudu
 */

public class PhoneMeetingOperation extends BaseOperation {

	@Override
	public void open() {
		openMessage();
	}

	@Override
	public void invita() {
		String username = mOperationModule.username;
		if (TextUtils.isEmpty(username)) {
			openMessage();
			return;
		}

		queryContact(username);
	}

	@Override
	public void handleAddressBook(AddressBook addressBook) {
		if (addressBook == null || TextUtils.isEmpty(addressBook.userId)) {
			return;
		}
		FRouter.build(mContext, "/x5/browser")
				.withString("extra", addressBook.userId)
				.withInt("moduleId", Func.Dudu)
				.go();
	}

}
