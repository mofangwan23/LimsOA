package cn.flyrise.feep.robot.operation.iflytek;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.bean.RobotNewAgendaRequest;
import cn.flyrise.feep.robot.entity.MoreResults;
import cn.flyrise.feep.robot.entity.RobotResultData;
import cn.flyrise.feep.robot.entity.SemanticParsenr;
import cn.flyrise.feep.robot.entity.UsedState;
import cn.flyrise.feep.robot.manager.AiuiOperationManager;
import cn.flyrise.feep.robot.manager.FeepOperationManager;
import cn.flyrise.feep.robot.util.RobotDateUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 新建：陈冕;
 * 日期： 2017-6-29.
 */

public class NewScheduleIflytek {

	private final int SCHEDULE_ENUM = Func.Schedule;
	@SuppressLint("StaticFieldLeak")
	private static NewScheduleIflytek scheduleOperation;
	private Context mContext;
	private AiuiOperationManager mManager;

	private FeepOperationManager.OnMessageGrammarResultListener mListener;

	public static NewScheduleIflytek getInstance() {
		if (scheduleOperation == null) scheduleOperation = new NewScheduleIflytek();
		return scheduleOperation;
	}

	public NewScheduleIflytek setContext(Context content) {
		this.mContext = content;
		this.mManager = new AiuiOperationManager(content);
		return this;
	}

	public NewScheduleIflytek setListener(FeepOperationManager.OnMessageGrammarResultListener listener) {
		this.mListener = listener;
		return this;
	}

	public void createSecedule(RobotResultData data) {
		MoreResults results = data.moreResults;
		UsedState state = results.mUsedState;
		mListener.onGrammarModule(mManager.scheduleUserInput(SCHEDULE_ENUM, data.text));
		if (isStateDataSuccess(state.datetime_time)) {//有时间就能创建日程
			seceduleCreate(data.semantic.get(0), data.service);
		}
		else {
			mListener.onGrammarModule(mManager.scheduleAiuiInput(SCHEDULE_ENUM, data.answerText));
		}
	}

	private boolean isStateDataSuccess(String state) {
		return TextUtils.equals(state, "1");
	}

	private void seceduleCreate(SemanticParsenr semantic, String service) {//创建
		if (semantic == null || TextUtils.isEmpty(semantic.time)) {
			FEToast.showMessage(mContext.getResources().getString(R.string.robot_create_error));
			return;
		}

		String startTime = TextUtils.isEmpty(semantic.time) ? semantic.time : RobotDateUtil.subTime(semantic.time);

		String content = TextUtils.isEmpty(semantic.content) ? mContext.getResources().getString(R.string.robot_hint_schedule_context)
				: semantic.content;
		RobotNewAgendaRequest newAgendaRequest = new RobotNewAgendaRequest();
		newAgendaRequest.title = mContext.getResources().getString(R.string.robot_create);
		newAgendaRequest.startTime = startTime;
		newAgendaRequest.endTime = RobotDateUtil.scheduleEndTime(semantic.time);
		newAgendaRequest.promptTime = "0";
		newAgendaRequest.repeatTime = "0";
		newAgendaRequest.content = content;
		newAgendaRequest.sharePerson = "";
		newAgendaRequest.method = "edit";
		Observable.create(f -> {
			FEHttpClient.getInstance().post(newAgendaRequest, new ResponseCallback<ResponseContent>() {
				@Override
				public void onCompleted(ResponseContent responseContent) {
					if (responseContent != null) f.onNext(responseContent.getErrorCode());
					else f.onError(new NullPointerException("Share Schedule Failed, the response content is null."));
					f.onCompleted();
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					super.onFailure(repositoryException);
					f.onError(repositoryException.exception());
					f.onCompleted();
				}
			});
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(errorCode -> {
							if (TextUtils.equals(errorCode + "", "0")) {
								mListener.onGrammarModule(mManager.scheduleSendSuccessModule(service
										, SCHEDULE_ENUM, getSendSuccessHtml(startTime, content)));
							}
							else {
								FEToast.showMessage(mContext.getResources().getString(R.string.robot_create_error));
							}
						},
						exception -> FEToast.showMessage(mContext.getResources().getString(R.string.robot_create_error)));
	}

	private String getSendSuccessHtml(String startTime, String content) {
		return mContext.getResources().getString(R.string.robot_create_schedule_success_left)
				+ "<b><tt>" + startTime + content + "</tt></b>"
				+ mContext.getResources().getString(R.string.robot_create_schedule_success_right);
	}

	public void onDestroy() {
		if (scheduleOperation == null) return;
		mContext = null;
		scheduleOperation = null;
	}
}
