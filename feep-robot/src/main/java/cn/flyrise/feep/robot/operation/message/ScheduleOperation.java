package cn.flyrise.feep.robot.operation.message;

/**
 * 新建：陈冕;
 * 日期： 2017-6-29.
 */

public class ScheduleOperation extends BaseOperation {

	@Override
	public void open() {
		if (mOperationModule.grammarResultListener != null) {
			mOperationModule.grammarResultListener.onGrammarMessage(
					mOperationModule.getMessageId(), mOperationModule.operationType);
		}
	}

	@Override
	public void search() {
		if (mOperationModule.grammarResultListener != null) {
			mOperationModule.grammarResultListener.onGrammarMessage(
					mOperationModule.getMessageId(), mOperationModule.operationType);
		}
	}
}
