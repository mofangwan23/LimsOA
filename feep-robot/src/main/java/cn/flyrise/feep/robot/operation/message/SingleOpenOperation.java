package cn.flyrise.feep.robot.operation.message;

import android.content.Intent;
import cn.flyrise.feep.core.function.FunctionManager;

/**
 * Created by Administrator on 2017-6-29.
 * 问卷、工资、活动、报表
 */
public class SingleOpenOperation extends BaseOperation {

	@Override
	public void open() {
		int messageId = mOperationModule.getMessageId();
		Class activityClass = FunctionManager.findClass(messageId);
		Intent intent = new Intent(mContext, activityClass);
		intent.putExtra("request_type", messageId);
		intent.putExtra("moduleId", messageId);
		mContext.startActivity(intent);
	}
}
