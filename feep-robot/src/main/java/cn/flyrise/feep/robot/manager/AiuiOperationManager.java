package cn.flyrise.feep.robot.manager;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.Robot;
import cn.flyrise.feep.robot.Robot.adapter;
import cn.flyrise.feep.robot.Robot.input;
import cn.flyrise.feep.robot.analysis.AnalysisBrainTeaser;
import cn.flyrise.feep.robot.bean.RobotAdapterListData;
import cn.flyrise.feep.robot.contract.RobotUnderstanderContract;
import cn.flyrise.feep.robot.entity.RiddleResultItem;
import cn.flyrise.feep.robot.entity.RobotResultData;
import cn.flyrise.feep.robot.entity.SemanticParsenr;
import cn.flyrise.feep.robot.entity.SlotParsenr;
import cn.flyrise.feep.robot.event.EventRobotModule;
import cn.flyrise.feep.robot.module.RobotModuleItem;
import cn.flyrise.feep.robot.util.RobotTextModifyUtil;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * 新建：陈冕;
 * 日期： 2017-10-13-10:04.
 * 理解后的操作
 */

public class AiuiOperationManager {

	private static final String CONTECT_ERROR = "亲，将小飞换成同事姓名";

	private RobotUnderstanderContract.View mView;

	private Context mContext;

	private int index = 0;
	private final String[] errorText = {"我没明白你说的话", "你说的有点难"
			, "我居然没听懂", "万水千山总是情，再说一遍行不行"};

	private RobotResultData robotResultData;

	public AiuiOperationManager(Context context) {
		this.mContext = context;
	}

	void setRobotResultData(RobotResultData robotResultData) {
		this.robotResultData = robotResultData;
	}

	private RobotResultData getRobotData() {
		return robotResultData;
	}

	AiuiOperationManager(Context context, RobotUnderstanderContract.View mView) {
		this.mView = mView;
		this.mContext = context;
	}

	void setServiceModuleItemHint(String title, int type, int process) {
		setServiceModuleItemHint("", title, type, process);
	}

	//讯飞返回输入的内容(系统提示)
	private void setServiceModuleItemHint(String service, String title, int type, int process) {
		notificationData(new RobotModuleItem.Builder()
				.setService(service)
				.setIndexType(type)
				.setTitle(title)
				.setProcess(process)
				.create());
	}

	//理解错误显示
	void grammarError() {
		if (mContext == null || mView == null) {
			return;
		}
		setServiceModuleItemHint(errorText[index % (errorText.length)], Robot.adapter.ROBOT_INPUT_LEFT, Robot.process.content);
		index++;
	}

	//理解错误显示
	boolean grammarUserNameError() {
		if (getRobotData() == null || getRobotData().operationEntry == null
				|| TextUtils.isEmpty(getRobotData().operationEntry.userName)
				|| mContext == null || mView == null) {
			return false;
		}
		if (TextUtils.equals(getRobotData().operationEntry.userName, "小飞")) {
			setServiceModuleItemHint(getRobotData().query, Robot.adapter.ROBOT_INPUT_RIGHT, Robot.process.start);
			setServiceModuleItemHint(CONTECT_ERROR, Robot.adapter.ROBOT_INPUT_LEFT, Robot.process.content);
			return true;
		}
		return false;
	}

	//OA模块的操作
	boolean setFeOAMessageModuleItem() {
		if (TextUtils.isEmpty(getResultText())) {
			grammarError();
			return false;
		}
		setRightModuleItem(getResultText());
		return true;
	}

	private String getResultText() {
		return TextUtils.isEmpty(getRobotData().text) ? TextUtils.isEmpty(getRobotData().query) ?
				"" : getRobotData().query : getRobotData().text;
	}

	private String getUserNmae(RobotResultData resultData) {
		if (resultData == null || resultData.operationEntry == null) {
			return "";
		}
		return resultData.operationEntry.userName;
	}

	//用户自己输入的内容
	private void setRightModuleItem(String text) {
		notificationData(new RobotModuleItem.Builder()
				.setIndexType(Robot.adapter.ROBOT_INPUT_RIGHT)
				.setProcess(Robot.process.start)
				.setTitle(text)
				.create());
	}

	public void getLeftModuleItem() {
		SemanticParsenr parsenr = CommonUtil.isEmptyList(getRobotData().semantic) ? null : getRobotData().semantic.get(0);
		if (parsenr == null || CommonUtil.isEmptyList(parsenr.slots)) return;
		notificationData(new RobotModuleItem.Builder()
				.setIndexType(Robot.adapter.ROBOT_INPUT_LEFT)
				.setProcess(Robot.process.content)
				.setTitle(setFeOAMessageHint(getUserNmae(getRobotData()), parsenr.slots.get(0)))
				.create());
	}

	//用户自己输入的语句
	private void setUserInputHint() {
		setServiceModuleItemHint(getRobotData().service, getRobotData().text, Robot.adapter.ROBOT_INPUT_RIGHT, Robot.process.start);
	}

	//添加提示语
	private String setFeOAMessageHint(String userName, SlotParsenr slotParsenr) {
		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(userName)) {
			sb.append(String.format("已找到%s，", getUserName(userName)));
		}
		sb.append("正在为你");
		if (slotParsenr == null) {
			return sb.toString();
		}
		sb.append(getMessageHint(slotParsenr.normValue));
		return sb.toString();
	}

	private String getUserName(String name) {
		if (TextUtils.equals(CoreZygote.getLoginUserServices().getUserName(), name)) {
			return "你";
		}
		return name;
	}

	private String getMessageHint(String operation) {
		if (TextUtils.equals(operation, Robot.operation.createType)) {
			return "创建";
		}
		else if (TextUtils.equals(operation, Robot.operation.openType)) {
			return "打开";
		}
		else if (TextUtils.equals(operation, Robot.operation.invitaType)) {
			return "发起邀请";
		}
		else if (TextUtils.equals(operation, Robot.operation.searchType)) {
			return "搜索";
		}
		return "打开";
	}

	//天气(系统提示)
	void setWeatherModuleItem() {
		setUserInputHint();
		if (CommonUtil.isEmptyList(getRobotData().semantic)) {
			grammarError();
			return;
		}
		notificationData(new RobotModuleItem.Builder()
				.setService(getRobotData().service)
				.setIndexType(Robot.adapter.ROBOT_WEATHER_HINT_LIST)
				.setContent(getRobotData().answerText)
				.setProcess(Robot.process.content)
				.setWeatherDatas(getRobotData().weatherDatas)
				.setDate(getRobotData().semantic.get(0) != null ? getRobotData().semantic.get(0).time : "")
				.create());
	}

	//诗词
	void setPoetryModuleItem() {
		setUserInputHint();
		if (CommonUtil.isEmptyList(getRobotData().results)) {
			grammarError();
			return;
		}
		notificationData(new RobotModuleItem.Builder()
				.setService(getRobotData().service)
				.setIndexType(Robot.adapter.ROBOT_CONTENT_HINT)
				.setResults(getRobotData().results)
				.setContent(getRobotData().answerText)
				.setProcess(Robot.process.content)
				.create());
	}

	//播放mp3
	void setPlayVoiceModule() {
		setUserInputHint();
		if (CommonUtil.isEmptyList(getRobotData().results)) {
			grammarError();
			return;
		}
		notificationData(new RobotModuleItem.Builder()
				.setService(getRobotData().service)
				.setIndexType(Robot.adapter.ROBOT_PLAY_VOICE)
				.setResults(getRobotData().results)
				.setContent(getRobotData().answerText)
				.setProcess(Robot.process.content)
				.create());
	}

	//火车模块
	void setTrainModule() {
		setUserInputHint();
		if (CommonUtil.isEmptyList(getRobotData().results)) {
			grammarError();
			return;
		}
		notificationData(new RobotModuleItem.Builder()
				.setService(getRobotData().service)
				.setIndexType(Robot.adapter.ROBOT_CONTENT_TRAIN)
				.setTrainItems(getRobotData().trainItems)
				.setContent(getRobotData().answerText)
				.setProcess(Robot.process.content)
				.create());
	}

	//谜语
	void setRiddle() {
		setUserInputHint();
		if (CommonUtil.isEmptyList(getRobotData().results)) {
			grammarError();
			return;
		}
		RiddleResultItem resultItem = AnalysisBrainTeaser.analysisRiddle(getRobotData().results);
		setRiddleBrainTeaser(resultItem);
	}

	//脑筋急转弯
	void setBrainTeaser() {
		setUserInputHint();
		RiddleResultItem resultItem = AnalysisBrainTeaser.analysisBrainTeaser(getRobotData().answerText);
		setRiddleBrainTeaser(resultItem);
	}

	private void setRiddleBrainTeaser(RiddleResultItem resultItem) {
		if (resultItem == null) {
			grammarError();
			return;
		}
		notificationData(new RobotModuleItem.Builder()
				.setService(getRobotData().service)
				.setIndexType(Robot.adapter.ROBOT_CONTENT_RIDDLE)
				.setRiddleItem(resultItem)
				.setContent(resultItem.title)
				.setProcess(Robot.process.content)
				.create());
	}

	//节假日查询
	void setHolidayQuery() {
		setUserInputHint();
		notificationData(new RobotModuleItem.Builder()
				.setService(getRobotData().service)
				.setIndexType(Robot.adapter.ROBOT_CONTENT_HOLIDAY)
				.setHolidayItems(getRobotData().holidayItems)
				.setContent(getRobotData().answerText)
				.setProcess(Robot.process.content)
				.create());
	}

	//闲聊以及一些无法理解的语句
	void setOpenQAAndGrammarError(List<RobotAdapterListData> listDatas) {
		if (TextUtils.isEmpty(getRobotData().answerText) || TextUtils.isEmpty(getRobotData().service)) {
			setUserInputHint();
			grammarError();
		}
		else {
			setQAServiceModuleItemHint(listDatas);
		}
	}

	private void setQAServiceModuleItemHint(List<RobotAdapterListData> listDatas) {
		int process;
		if (getServiceModify(getRobotData().service, listDatas)) {
			process = Robot.process.start;
		}
		else {
			process = Robot.process.content;
		}
		setServiceModuleItemHint(getRobotData().service, getRobotData().text, Robot.adapter.ROBOT_INPUT_RIGHT, process);
		setServiceModuleItemHint(getRobotData().service, getRobotData().answerText, Robot.adapter.ROBOT_INPUT_LEFT, Robot.process.content);
	}

	/******新建日程**********/

	public RobotModuleItem scheduleAiuiInput(int parentType, String text) { //提示说出内容
		return new RobotModuleItem.Builder()
				.setModuleParentType(parentType)
				.setOperationType(Robot.operation.createType)
				.setIndexType(Robot.adapter.ROBOT_INPUT_LEFT)
				.setProcess(Robot.schedule.content_hint)
				.setTitle(text)
				.create();
	}

	public RobotModuleItem scheduleUserInput(int parentType, String content) {
		return new RobotModuleItem.Builder()
				.setModuleParentType(parentType)
				.setOperationType(Robot.operation.createType)
				.setIndexType(Robot.adapter.ROBOT_INPUT_RIGHT)
				.setProcess(Robot.schedule.content)
				.setTitle(content)
				.create();
	}

	public RobotModuleItem scheduleSendSuccessModule(String service, int parentTpe, String htmlText) {
		return new RobotModuleItem.Builder()
				.setService(service)
				.setModuleParentType(parentTpe)
				.setOperationType(Robot.operation.createType)
				.setIndexType(Robot.adapter.ROBOT_CONTENT_HINT)
				.setProcess(Robot.schedule.end)
				.setTitle(mContext.getResources().getString(R.string.robot_create_success))
				.setIcon(R.drawable.robot_understander_schedule)
				.setHtmlContent(RobotTextModifyUtil.fromHtml(htmlText))
				.create();
	}

	/******新建日程END*******/

	//联系人异常
	void setTheContaceGrammarError() {
		setServiceModuleItemHint("联系人数据异常", Robot.adapter.ROBOT_INPUT_LEFT, Robot.process.content);
	}

	//判断服务是否修改
	private boolean getServiceModify(String currentService, List<RobotAdapterListData> listDatas) {
		if (TextUtils.isEmpty(currentService) || CommonUtil.isEmptyList(listDatas)) {
			return true;
		}
		RobotAdapterListData pro;
		for (int i = listDatas.size() - 1; i >= 0; i--) {
			pro = listDatas.get(i);
			if (pro == null) {
				continue;
			}
			if (pro.adapterIndex == Robot.adapter.ROBOT_INPUT_RIGHT
					&& TextUtils.equals(pro.service, currentService)) {
				return false;
			}
		}
		return true;
	}

	private void notificationData(RobotModuleItem robotModuleItem) {
		if (robotModuleItem.indexType == adapter.ROBOT_INPUT_RIGHT && getRobotData().inputType == input.text) return;
		EventBus.getDefault().post(new EventRobotModule(robotModuleItem));
	}
}
