package cn.flyrise.feep.robot.operation.message;

import android.content.Context;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.robot.bean.RobotAdapterListData;
import cn.flyrise.feep.robot.entity.RobotResultData;
import cn.flyrise.feep.robot.module.OperationModule;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-12-07 17:40
 */
public class OperationBuilder {

	private Context context;
	private String messageType;
	private OperationModule operationModule;
	private RobotResultData identifyResults;
	private List<RobotAdapterListData> processData;

	public OperationBuilder(Context context) {
		this.context = context;
	}

	public OperationBuilder messaegeType(String messageType) {
		this.messageType = messageType;
		return this;
	}

	public OperationBuilder operationModule(OperationModule module) {
		this.operationModule = module;
		return this;
	}

	public OperationBuilder identifyResults(RobotResultData result) {
		this.identifyResults = result;
		return this;
	}

	public OperationBuilder processData(List<RobotAdapterListData> datas) {
		this.processData = datas;
		return this;
	}

	public BaseOperation build() {
		BaseOperation operation = null;
		if (("0".equals(messageType)
				|| "1".equals(messageType)
				|| "4".equals(messageType)
				|| "10".equals(messageType)
				|| "44".equals(messageType))
				&& isModuleExist("44")) {
			operation = new CollaborationOperation();
		}
		else if (("13".equals(messageType) && isModuleExist("13"))      // 报表
				|| ("36".equals(messageType) && isModuleExist("36"))   // 问卷
				|| ("38".equals(messageType) && isModuleExist("38"))    // 活动
				|| ("48".equals(messageType) && isModuleExist("48"))) {  // 工资
			operation = new SingleOpenOperation();
		}
		else if ("14".equals(messageType) && isModuleExist("14")) {    // 计划
			operation = new WorkPlanOperation();
		}
		else if (("5".equals(messageType) && isModuleExist("5"))
				|| ("6".equals(messageType) && isModuleExist("6"))) {   // 新闻公告
			operation = new NewAnnouncementOperation();
		}
		else if ("9".equals(messageType) && isModuleExist("9")) {     // 会议
			operation = new MeetingOperation();
		}
		else if ("21".equals(messageType) && isModuleExist("21")) {    // 签到
			operation = new LocationSignOperation();
		}
		else if ("35".equals(messageType) && isModuleExist("35")) {    // 文档
			operation = new KnowledgeOperation();
		}
		else if ("37".equals(messageType) && isModuleExist("37")) {    // 日程
			operation = new ScheduleOperation();
		}
		else if ("45".equals(messageType) && isModuleExist("45")) {    // 嘟嘟
			operation = new PhoneMeetingOperation();
		}
		else if ("46".equals(messageType) && isModuleExist("46")) {    // 邮件
			operation = new EmailOperation();
		}
		else if ("661".equals(messageType)) {    // 聊天
			operation = new ChatIMOperation();
		}
		else if ("662".equals(messageType)       // 打电话
				|| "663".equals(messageType)) {  // 发短信
			operation = new TelOrSmsOperation();
		}
		else if ("664".equals(messageType)) {    // 打开用户详情
			operation = new AddressBookDetailOperation();
		}

		if (operation != null) {
			operation.setContext(context);
			operation.setOperationModule(operationModule);
		}

		return operation;
	}

	private boolean isModuleExist(String moduleId) {
		return FunctionManager.hasModule(Integer.valueOf(moduleId));
	}


}
