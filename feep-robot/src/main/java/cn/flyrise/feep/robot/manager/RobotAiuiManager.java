package cn.flyrise.feep.robot.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.robot.Robot.input;
import cn.flyrise.feep.robot.analysis.AiuiAnalysis;
import cn.flyrise.feep.robot.bean.RobotGrammarVersion;
import cn.flyrise.feep.robot.entity.DynamicEntity;
import cn.flyrise.feep.robot.entity.RobotAiuiMessage;
import cn.flyrise.feep.robot.entity.RobotResultData;
import cn.flyrise.feep.robot.util.FileAssetsUtil;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;

/**
 * 新建：陈冕;
 * 日期： 2017-11-24-16:46.
 * aiui语音输入输出管理
 */

public class RobotAiuiManager {

	private AIUIAgent mAIUIAgent = null;
	private int mAIUIState = AIUIConstant.STATE_IDLE;
	private DynamicEntity dynamicEntity;
	private Context mContext;
	private AIUIEventReturnListener mListener;

	private boolean isVadRecord = false;//没说话
	private final static int noRecord = 202;//3200毫秒没有回调
	private int inputType;//输入方式

	private final static int SYNC_DATA_TIME = 1200;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (noRecord == msg.what && mListener != null) mListener.eventNoTalk();
		}
	};

	public RobotAiuiManager(Context context, AIUIEventReturnListener listener) {
		dynamicEntity = new DynamicEntity(context);
		mContext = context;
		mListener = listener;
		initAIUIAgent();
	}

	private void initAIUIAgent() { //初始化AIUI
		if (null == mAIUIAgent) {
			try {
				mAIUIAgent = AIUIAgent.createAgent(mContext,
						new FileAssetsUtil().getAssetsFileText(mContext, "cfg/aiui_phone.cfg"),
						mAIUIListener);
				mAIUIAgent.sendMessage(new RobotAiuiMessage.Builder()
						.setMessageType(AIUIConstant.CMD_START)
						.setArg1(0)
						.setArg2(0)
						.setParams(null)
						.setData(null).build().create());
				mHandler.postDelayed(this::postSyncSchemaContact, SYNC_DATA_TIME);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (null == mAIUIAgent && mListener != null) {
			final String strErrorTip = "创建 AIUI Agent 失败！";
			mListener.eventError(strErrorTip);
		}
	}

	public void startVoiceUnderstander() { //开始语音识别、理解
		inputType = input.voice;
		sendMessage(new RobotAiuiMessage.Builder()
				.setMessageType(AIUIConstant.CMD_START_RECORD)
				.setArg1(0)
				.setArg2(0)
				.setParams("data_type=audio,sample_rate=16000")
				.setData(null).build().create());
	}

	public void stopVoiceUnderstander() { //停止语音识别、理解
		sendMessage(new RobotAiuiMessage.Builder()
				.setMessageType(AIUIConstant.CMD_STOP_RECORD)
				.setArg1(0)
				.setArg2(0)
				.setParams("data_type=audio,sample_rate=16000")
				.setData(null).build().create());
	}

	public void startTextUnderstander(String text) { //文本识别、理解
		sendMessage(new RobotAiuiMessage.Builder()
				.setMessageType(AIUIConstant.CMD_WRITE)
				.setArg1(0)
				.setArg2(0)
				.setParams("data_type=text")
				.setData(getTextUnderstanderData(text)).build().create());
		inputType = input.text;
	}

	private boolean isUploadCloudGrammar() { //是否更新联系人动态实体
		String versionText = SpUtil.get("upload_contact_entity", "");
		if (TextUtils.isEmpty(versionText)) {
			return true;
		}
		RobotGrammarVersion grammarVersion = null;
		try {
			grammarVersion = GsonUtil.getInstance().fromJson(versionText, RobotGrammarVersion.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (grammarVersion == null) {
			return true;
		}

		String dbVersion = SpUtil.get("AddressBookVersion", "");
		String serviceUrl = CoreZygote.getLoginUserServices().getNetworkInfo() == null ? ""
				: CoreZygote.getLoginUserServices().getNetworkInfo().buildServerURL();
		String userId = CoreZygote.getLoginUserServices().getUserId();
		return !TextUtils.equals(grammarVersion.userId, userId)
				|| !TextUtils.equals(grammarVersion.serviceUrl, serviceUrl)
				|| !TextUtils.equals(grammarVersion.dbVersion, dbVersion);
	}

	private void postSyncSchemaContact() { //上传动态实体
		if (!isUploadCloudGrammar()) {
			openCodSetParams();
			return;
		}
		sendMessage(dynamicEntity.postSyncSchemaContact());
	}

	public void searchPostContact() {  //查询动态实体结果
		sendMessage(dynamicEntity.searchPostContact());
	}

	private void openCodSetParams() { //动态实体生效
		mHandler.postDelayed(() -> sendMessage(dynamicEntity.takeEffect()), SYNC_DATA_TIME);
	}

	private void sendMessage(AIUIMessage message) { //执行AIUI语句
		if (message == null || mAIUIAgent == null) {
			return;
		}
		// 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
		// 默认为oneshot 模式，即一次唤醒后就进入休眠，如果语音唤醒后
		// ，需要进行文本语义，请将改段逻辑copy至startTextNlp()开头处
		if (mAIUIState != AIUIConstant.STATE_WORKING) {
			mAIUIAgent.sendMessage(new RobotAiuiMessage.Builder()
					.setMessageType(AIUIConstant.CMD_WAKEUP)
					.setArg1(0)
					.setArg2(0)
					.setParams("")
					.setData(null).build().create());
		}
		mAIUIAgent.sendMessage(message);
	}

	private AIUIListener mAIUIListener = new AIUIListener() {

		@Override
		public void onEvent(AIUIEvent event) {
			if (mListener == null || event == null) return;
			FELog.i("-->>>>robotEvent:" + event.eventType);
			switch (event.eventType) {
				case AIUIConstant.EVENT_WAKEUP:
					mListener.eventWakeup();
					break;
				case AIUIConstant.EVENT_RESULT:
					mHandler.removeMessages(noRecord);
					mListener.eventResult(AiuiAnalysis.INSTANCE.analysis(event.info, event.data, inputType));
					break;
				case AIUIConstant.EVENT_ERROR:
					mHandler.removeMessages(noRecord);
					mListener.eventError(event.eventType + "");
					break;
				case AIUIConstant.EVENT_VAD:
					isVadRecord = true;
					eventCmdVad(event.arg1, event.arg2);
					break;
				case AIUIConstant.EVENT_START_RECORD:
					mHandler.removeMessages(noRecord);
					isVadRecord = false;
					mListener.eventStartRecord();
					break;
				case AIUIConstant.EVENT_STOP_RECORD:
					mListener.eventStopRecord();
					mHandler.sendEmptyMessageDelayed(noRecord, 3200);
					if (!isVadRecord) mListener.eventNoTalk();
					break;
				case AIUIConstant.EVENT_STATE:
					mAIUIState = event.arg1;
					eventState();
					break;
				case AIUIConstant.EVENT_CMD_RETURN: //日志回调
					eventCmdReturn(event.arg1, event.arg2, event.data);
					break;
				default:
					break;
			}
		}
	};

	private void eventCmdVad(int arg1, int arg2) {
		if (mListener == null) return;
		if (AIUIConstant.VAD_BOS == arg1) {
			FELog.i("RobotAiuiManager", "-->>>>>VAD_BOS:");
			return;
		}
		mListener.eventVad(AIUIConstant.VAD_EOS == arg1 ? 0 : arg2);
	}

	//动态实体监听
	private void eventCmdReturn(int arg1, int arg2, Bundle data) {
		if (data == null) {
			return;
		}
		if (AIUIConstant.CMD_UPLOAD_LEXICON == arg1) {
			cmdUploadLexiconReturn(arg2);
		}
		else if (AIUIConstant.CMD_SYNC == arg1) { //上传实体回调
			cmdSyncReturn(arg2, data);
		}
		else if (AIUIConstant.CMD_QUERY_SYNC_STATUS == arg1) { //动态实体上传查询结果
			cmdQuerySyncStatus(arg2, data);
		}
	}

	private void cmdUploadLexiconReturn(int arg2) {
		Log.i("NlpDemo", "-->>>>上传" + (0 == arg2 ? "成功" : "失败"));
	}

	private void cmdSyncReturn(int arg2, Bundle data) {
		int syncType = data.getInt("sync_dtype");
		if (AIUIConstant.SYNC_DATA_SCHEMA != syncType) return;
		if (0 == arg2) {
			syncDataSchema(data.getString("sid"));
		}
		else {
			Log.i("NlpDemo", "-->>>>schema数据同步出错：" + arg2 + "，sid=" + data.getString("sid"));
			SpUtil.put("SYNC_SID", data.getString("sid"));
		}
	}

	private void syncDataSchema(String sid) {
		Log.i("NlpDemo", "-->>>>schema数据同步成功，sid=" + sid);
		RobotGrammarVersion version = new RobotGrammarVersion();
		version.userId = CoreZygote.getLoginUserServices().getUserId();
		version.serviceUrl = CoreZygote.getLoginUserServices().getNetworkInfo() == null ? ""
				: CoreZygote.getLoginUserServices().getNetworkInfo().buildServerURL();
		version.dbVersion = SpUtil.get("AddressBookVersion", "");
		SpUtil.put("upload_contact_entity", GsonUtil.getInstance().toJson(version));
		SpUtil.put("SYNC_SID", sid);
		openCodSetParams();
	}

	private void cmdQuerySyncStatus(int resultCode, Bundle data) {
		if (AIUIConstant.SYNC_DATA_QUERY != data.getInt("sync_dtype")) {
			return;
		}
		if (0 == resultCode) {
			Log.i("NlpDemo", "-->>>>查询结果：" + data.getString("result"));
		}
		else {
			Log.i("NlpDemo", "-->>>>schema数据状态查询出错：" + resultCode +
					", result:" + data.getString("result"));
		}
	}

	private byte[] getTextUnderstanderData(String text) {
		return TextUtils.isEmpty(text) ? null : text.getBytes();
	}

	private void eventState() {
		if (AIUIConstant.STATE_IDLE == mAIUIState) {// 闲置状态，AIUI未开启
			Log.i("NlpDemo", "-->>>>闲置状态，AIUI未开启--STATE_IDLE");
		}
		else if (AIUIConstant.STATE_READY == mAIUIState) {// AIUI已就绪，等待唤醒
			Log.i("NlpDemo", "-->>>>AIUI已就绪，等待唤醒--STATE_READY");
		}
		else if (AIUIConstant.STATE_WORKING == mAIUIState) {// AIUI工作中，可进行交互
			Log.i("NlpDemo", "-->>>>AIUI工作中，可进行交互--STATE_WORKING");
		}
	}

	public interface AIUIEventReturnListener {

		void eventWakeup(); //进入识别状态

		void eventResult(RobotResultData robotResultData); //识别成功回调

		void eventError(String text); //识别异常

		void eventVad(int vad); //音量

		void eventStartRecord(); //开始录音

		void eventStopRecord(); //停止录音

		void eventNoTalk(); //没说话
	}

	public void onDestroy() {
		if (null == this.mAIUIAgent) {
			return;
		}
		AIUIMessage stopMsg = new AIUIMessage(AIUIConstant.CMD_STOP, 0, 0, null, null);
		mAIUIAgent.sendMessage(stopMsg);
		this.mAIUIAgent.destroy();
		this.mAIUIAgent = null;
	}
}
