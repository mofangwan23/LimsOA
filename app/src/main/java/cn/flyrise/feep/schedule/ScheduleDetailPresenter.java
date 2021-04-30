package cn.flyrise.feep.schedule;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.schedule.AgendaDetailData;
import cn.flyrise.android.protocol.entity.schedule.PromptRequest;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.schedule;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.util.HtmlUtil;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.schedule.data.ScheduleDataRepository;
import cn.flyrise.feep.utils.Patches;
import java.util.Arrays;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016-12-20 18:37
 */
public class ScheduleDetailPresenter implements ScheduleDetailContract.IPresenter {

	private ScheduleDetailContract.IView mScheduleDetailView;
	private ScheduleDataRepository mRepository;

	private String eventSourceId;
	private String mScheduleId;
	private String mRepeatTime;
	private String mEventSource;
	private AgendaDetailData mScheduleDetail;

	public ScheduleDetailPresenter(ScheduleDetailContract.IView scheduleDetailView) {
		this.mScheduleDetailView = scheduleDetailView;
		this.mRepository = new ScheduleDataRepository();
	}

	@Override
	public void start(Intent intent) {
		eventSourceId = intent.getStringExtra(K.schedule.event_source_id);
		mScheduleId = intent.getStringExtra(K.schedule.schedule_id);
		mEventSource = intent.getStringExtra(K.schedule.event_source);
		if (TextUtils.isEmpty(eventSourceId)) {
			throw new NullPointerException("The event source id is null, please pass the correct id.");
		}
		fetchScheduleDetail(eventSourceId);
	}

	@Override
	public void fetchScheduleDetail(String eventSourceId) {
		mScheduleDetailView.showLoading();
		mRepository.getScheduleDetail(eventSourceId, mEventSource)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(agendaDetail -> {
							mScheduleDetailView.getScheduleDetailSuccess(mScheduleDetail = agendaDetail);
							if (FunctionManager.hasPatch(Patches.PATCH_SCHEDULE_REPLY)) {
								fetchScheduleReplyList(eventSourceId);
							}
							else {
								mScheduleDetailView.hideLoading();
							}
						},
						exception -> {
							exception.printStackTrace();
							mScheduleDetailView.hideLoading();
							mScheduleDetailView
									.getScheduleDetailFailed(CommonUtil.getString(R.string.schedule_lbl_get_schedule_detail_failed));
						});
	}

	@Override
	public void fetchScheduleReplyList(String eventSourceId) {
		mRepository.getScheduleReplyList(eventSourceId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(replyList -> {
							mScheduleDetailView.hideLoading();
							if (replyList != null) {
								mScheduleDetailView.getScheduleReplySuccess(replyList);
							}
							else {
								mScheduleDetailView.getScheduleReplyFailed();
							}
						},
						exception -> {
							mScheduleDetailView.getScheduleReplyFailed();
							mScheduleDetailView.hideLoading();
						});
	}

	@Override
	public void deleteReply(String replyId) {
		mScheduleDetailView.showLoading();
		mRepository.deleteReply(replyId, mScheduleDetail.title, mScheduleDetail.sendUserId, eventSourceId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(code -> {
					switch (code) {
						case "0":
							mScheduleDetailView.deleteReplySuccess();
							fetchScheduleReplyList(eventSourceId);
							return;
						case schedule.ERROR_CODE_REPLY_NO_PERMISSION:
							FEToast.showMessage(R.string.schedule_reply_no_permission);
							break;
						default:
							mScheduleDetailView.deleteReplyFailed();
							break;
					}
					mScheduleDetailView.hideLoading();
				}, throwable -> mScheduleDetailView.hideLoading());
	}

	@Override
	public void reply(String content) {
		mScheduleDetailView.showLoading();
		mRepository.replyTheSchedule(eventSourceId, content, mScheduleDetail.title, mScheduleDetail.sendUserId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(code -> {
					switch (code) {
						case "0":
							mScheduleDetailView.replySuccess();
							fetchScheduleReplyList(eventSourceId);
							return;
						case schedule.ERROR_CODE_REPLY_NO_PERMISSION:
							FEToast.showMessage(R.string.schedule_reply_no_permission);
							break;
						default:
							mScheduleDetailView.replyFailed();
							break;
					}
					mScheduleDetailView.hideLoading();
				}, throwable -> mScheduleDetailView.hideLoading());
	}

	@Override
	public void updateReply(String replyId, String content) {
		mScheduleDetailView.showLoading();
		mRepository.updateReply(replyId, content, mScheduleDetail.title, mScheduleDetail.sendUserId, eventSourceId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(code -> {
					switch (code) {
						case "0":
							mScheduleDetailView.updateReplySuccess();
							fetchScheduleReplyList(eventSourceId);
							return;
						case schedule.ERROR_CODE_REPLY_NO_PERMISSION:
							FEToast.showMessage(R.string.schedule_reply_no_permission);
							break;
						default:
							mScheduleDetailView.updateReplyFailed();
							break;
					}
					mScheduleDetailView.hideLoading();
				}, throwable -> mScheduleDetailView.hideLoading());
	}

	@Override
	public void deleteSchedule() {
		mScheduleDetailView.showLoading();
		mRepository.deleteSchedule(mScheduleId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(responseContent -> {
							mScheduleDetailView.hideLoading();
							try {
								mRepository.deleteSystemSchedule(CoreZygote.getContext(), mScheduleId);
							} catch (Exception exp) {
								exp.printStackTrace();
							}
							if (TextUtils.equals(responseContent.getErrorCode(), "0")) {
								mScheduleDetailView.deleteScheduleSuccess(mScheduleId);
							}
							else {
								mScheduleDetailView.deleteScheduleFailed();
							}
						},
						exception -> {
							exception.printStackTrace();
							mScheduleDetailView.hideLoading();
							mScheduleDetailView.deleteScheduleFailed();
						});
	}

	@Override
	public void fetchPromptTime(String promptTime) {//提醒
		mRepository.getReferenceItem(PromptRequest.METHOD_PROMPT, "")
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(referenceItems -> {
							mScheduleDetailView.hideLoading();
							String value = null;
							for (ReferenceItem item : referenceItems) {
								if (TextUtils.equals(item.getKey(), promptTime)) {
									value = item.getValue();
									break;
								}
							}
							mScheduleDetailView.getPromptTimeValue(TextUtils.isEmpty(value) ?
									CommonUtil.getString(R.string.schedule_detail_lbl_share_none) : value);
						},
						exception -> {
							exception.printStackTrace();
							mScheduleDetailView.hideLoading();
							mScheduleDetailView.getPromptTimeValue(CommonUtil.getString(R.string.schedule_detail_lbl_share_none));
						});
	}

	@Override
	public void fetchRepeatTime(String repeatTime) {//重复
		mRepository.getReferenceItem(PromptRequest.METHOD_REPEAT, "")
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(referenceItems -> {
							mScheduleDetailView.hideLoading();
							for (ReferenceItem item : referenceItems) {
								if (TextUtils.equals(item.getKey(), repeatTime)) {
									mRepeatTime = item.getValue();
									break;
								}
							}
							mScheduleDetailView.getRepeatTimeValue(TextUtils.isEmpty(mRepeatTime) ?
									CommonUtil.getString(R.string.schedule_detail_lbl_share_none) : mRepeatTime);
						},
						exception -> {
							exception.printStackTrace();
							mScheduleDetailView.hideLoading();
							mScheduleDetailView.getRepeatTimeValue(CommonUtil.getString(R.string.schedule_detail_lbl_share_none));
						});
	}

	@Override
	public void shareSchedule(String ids) {
		mScheduleDetailView.showLoading();
		String allIds = null;
		if (TextUtils.isEmpty(mScheduleDetail.shareOther)) {
			allIds = ids;
		}
		else {
			StringBuilder builder = new StringBuilder();
			List<String> oldIds = Arrays.asList(mScheduleDetail.shareOther.split(","));
			List<String> newIds = Arrays.asList(ids.split(","));
			for (String oldId : oldIds) {
				builder.append(oldId).append(",");
			}

			for (String newId : newIds) {
				if (oldIds.contains(newId)) {
					continue;
				}
				builder.append(newId).append(",");
			}
			allIds = builder.substring(0, builder.length() - 1);
		}

		mScheduleDetail.shareOther = allIds;
		mRepository.shareSchedule(mScheduleDetail)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(errorCode -> {
							mScheduleDetailView.hideLoading();
							if (TextUtils.equals(errorCode, "-1017004")) {
								FEToast.showMessage("人数过多分享失败");
								return;
							}
							if (TextUtils.equals(errorCode, "0")) {
								mScheduleDetailView.shareOtherSuccess();
							}
							else {
								mScheduleDetailView.shareOtherFailed();
							}
						},
						exception -> {
							exception.printStackTrace();
							mScheduleDetailView.hideLoading();
							mScheduleDetailView.shareOtherFailed();
						});
	}

	@Override
	public boolean isSharedSchedule() {
		return !TextUtils.equals(CoreZygote.getLoginUserServices().getUserId(), mScheduleDetail.sendUserId);
	}

	@Override
	public void syncCalendarToSystem(Context context) {
		mScheduleDetailView.showLoading();
		mRepository.addToSystemCalendar(context, mScheduleDetail.title,
				HtmlUtil.delHTMLTag(mScheduleDetail.content), mScheduleDetail.promptTime,
				mScheduleDetail.startTime, mScheduleDetail.endTime, mRepeatTime, mScheduleId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(resultCode -> {
							mScheduleDetailView.hideLoading();
							if (resultCode == 200) {
								mScheduleDetailView.syncCalendarSuccess();
							}
							else {
								mScheduleDetailView.syncCalendarFailed();
							}
						},
						exception -> {
							exception.printStackTrace();
							mScheduleDetailView.hideLoading();
							mScheduleDetailView.syncCalendarFailed();
						});
	}

	@Override
	public AgendaDetailData getScheduleDetail() {
		return mScheduleDetail;
	}

}
