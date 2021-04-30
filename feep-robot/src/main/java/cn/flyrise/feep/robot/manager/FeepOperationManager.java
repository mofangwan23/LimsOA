package cn.flyrise.feep.robot.manager;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.Robot;
import cn.flyrise.feep.robot.bean.RobotAdapterListData;
import cn.flyrise.feep.robot.entity.FeepOperationEntry;
import cn.flyrise.feep.robot.entity.RobotResultData;
import cn.flyrise.feep.robot.module.OperationModule;
import cn.flyrise.feep.robot.module.RobotModuleItem;
import cn.flyrise.feep.robot.operation.RobotOperation;
import cn.flyrise.feep.robot.operation.message.OperationBuilder;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-6-29.
 * oa操作
 */
public class FeepOperationManager {

	protected Context mContext;
	private OnMessageGrammarResultListener mListener;
	private List<RobotAdapterListData> mProcess;
	private RobotResultData mRobotResultData;

	public void setContext(Context context) {
		this.mContext = context;
	}

	void setUnderstanderData(RobotResultData robotResultData) {
		this.mRobotResultData = robotResultData;
	}

	//当前操作的过程（例如新建日程到哪一步了）
	public void setProcess(List<RobotAdapterListData> process) {
		this.mProcess = process;
	}

	public void setListener(OnMessageGrammarResultListener listener) {
		this.mListener = listener;
	}

	RobotOperation startOperation() {
		if (mRobotResultData == null || mRobotResultData.operationEntry == null) return null;

		FeepOperationEntry operationEntity = mRobotResultData.operationEntry;

		OperationModule module = new OperationModule.Builder()
				.operationType(operationEntity.operationType)
				.messageType(operationEntity.messageType)
				.username(operationEntity.userName)
				.dateTime(operationEntity.dateTime)
				.wildcard(operationEntity.wildcard)
				.grammarResultListener(mListener)
				.build();

		RobotOperation robotOperation = new OperationBuilder(mContext)
				.identifyResults(mRobotResultData)
				.messaegeType(operationEntity.messageType)
				.processData(mProcess)
				.operationModule(module)
				.build();

		if (robotOperation == null) {
			if (mListener != null) {
				mListener.onGrammarText(CommonUtil.getString(R.string.robot_grammar_error));
			}
			return null;
		}else{
			if(mListener!=null)mListener.onShowOALeftHint();
		}

		if (TextUtils.equals(Robot.operation.openType, operationEntity.operationType)
				|| TextUtils.isEmpty(operationEntity.operationType)) {
			robotOperation.open();
		}
		else if (TextUtils.equals(Robot.operation.searchType, operationEntity.operationType)) {
			robotOperation.search();
		}
		else if (TextUtils.equals(Robot.operation.createType, operationEntity.operationType)) {
			robotOperation.create();
		}
		else if (TextUtils.equals(Robot.operation.invitaType, operationEntity.operationType)) {
			robotOperation.invita();
		}
		return robotOperation;
	}

	public interface OnMessageGrammarResultListener {

		void onGrammarResultItems(List<RobotModuleItem> robotModuleItems);

		void onGrammarMessage(int messageId, String operation);

		void onGrammarText(String text);

		void onGrammarModule(RobotModuleItem robotModuleItem);

		void onError();

		void onShowOALeftHint();
	}

}
