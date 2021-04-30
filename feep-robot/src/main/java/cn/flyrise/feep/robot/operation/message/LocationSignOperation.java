package cn.flyrise.feep.robot.operation.message;

/**
 * Created by Administrator on 2017-6-29.
 * 签到、搜索位置
 */
public class LocationSignOperation extends BaseOperation {

	@Override
	public void open() {
		if (mOperationModule.grammarResultListener != null) {
			mOperationModule.grammarResultListener.onGrammarMessage(mOperationModule.getMessageId(), mOperationModule.operationType);
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
