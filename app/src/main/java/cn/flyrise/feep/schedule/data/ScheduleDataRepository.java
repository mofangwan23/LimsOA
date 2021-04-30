package cn.flyrise.feep.schedule.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.schedule.AgendaDetailData;
import cn.flyrise.android.protocol.entity.schedule.AgendaDetailDataResponse;
import cn.flyrise.android.protocol.entity.schedule.AgendaDetailRequest;
import cn.flyrise.android.protocol.entity.schedule.AgendaReplyDeleteRequest;
import cn.flyrise.android.protocol.entity.schedule.AgendaReplyListRequest;
import cn.flyrise.android.protocol.entity.schedule.AgendaReplyListResponse;
import cn.flyrise.android.protocol.entity.schedule.AgendaReplyRequest;
import cn.flyrise.android.protocol.entity.schedule.AgendaReplyUpdateRequest;
import cn.flyrise.android.protocol.entity.schedule.AgendaRequest;
import cn.flyrise.android.protocol.entity.schedule.AgendaResponse;
import cn.flyrise.android.protocol.entity.schedule.AgendaResponseItem;
import cn.flyrise.android.protocol.entity.schedule.NewAgendaRequest;
import cn.flyrise.android.protocol.entity.schedule.PromptRequest;
import cn.flyrise.android.protocol.entity.schedule.PromptResponse;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.schedule.model.ScheduleReply;
import cn.flyrise.feep.schedule.utils.ScheduleUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import rx.Observable;

/**
 * @author ZYP
 * @since 2016-12-23 16:05
 */
public class ScheduleDataRepository {

	public static final String CALENDAR_URL = "content://com.android.calendar/calendars";
	public static final String CALENDAR_EVENT_URL = "content://com.android.calendar/events";
	public static final String CALENDAR_REMINDERS_URL = "content://com.android.calendar/reminders";

	/**
	 * 获取日程列表
	 * @param date yyyy-MM 日期格式
	 */
	public Observable<List<AgendaResponseItem>> getAgendaList(String date) {
		return Observable
				.create(subscriber -> {
					AgendaRequest request = new AgendaRequest();
					request.date = date;
					FEHttpClient.getInstance().post(request, new ResponseCallback<AgendaResponse>() {
						@Override
						public void onCompleted(AgendaResponse agendaResponse) {
							List<AgendaResponseItem> items = agendaResponse.items;
							List<AgendaResponseItem> newItems = new ArrayList<>();
							for (AgendaResponseItem item : items) {
								if (!TextUtils.isEmpty(item.eventSource) && item.eventSource.contains("leader")) {
									continue;
								}
								newItems.add(item);
							}
							subscriber.onNext(newItems);
						}

						@Override
						public void onFailure(RepositoryException repositoryException) {
							super.onFailure(repositoryException);
							subscriber.onError(repositoryException.exception());
						}
					});
				});
	}

	/**
	 * 获取日程详情
	 */
	public Observable<AgendaDetailData> getScheduleDetail(String eventSourceId, String eventSource) {
		return Observable.create(subscriber -> {
			AgendaDetailRequest agendaDetailRequest = new AgendaDetailRequest(AgendaDetailRequest.METHOD_VIEW, eventSourceId, eventSource);
			FEHttpClient.getInstance().post(agendaDetailRequest, new ResponseCallback<AgendaDetailDataResponse>() {
				@Override
				public void onCompleted(AgendaDetailDataResponse agendaDetailDataResponse) {
					subscriber.onNext(agendaDetailDataResponse.data);
					subscriber.onCompleted();
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					super.onFailure(repositoryException);
					FELog.e("getScheduleDetail Failure.");
					subscriber.onError(repositoryException.exception());
					subscriber.onCompleted();
				}
			});
		});
	}


	public Observable<ResponseContent> deleteSchedule(String agendaId) {
		return Observable.create(subscriber -> {
			AgendaDetailRequest agendaDetailRequest = new AgendaDetailRequest(AgendaDetailRequest.METHOD_DELETE, agendaId);
			FEHttpClient.getInstance().post(agendaDetailRequest, new ResponseCallback<ResponseContent>() {
				@Override
				public void onCompleted(ResponseContent responseContent) {
					FELog.i("onCompleted : " + responseContent);
					subscriber.onNext(responseContent);
					subscriber.onCompleted();
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					super.onFailure(repositoryException);
					FELog.e("onFailure : " + repositoryException.exception());
					subscriber.onError(repositoryException.exception());
					subscriber.onCompleted();
				}
			});
		});
	}

	public Observable<List<ReferenceItem>> getReferenceItem(String prompt, String key) {
		return Observable.create(subscriber -> {
			PromptRequest request = new PromptRequest(prompt, key);//部分环境key传过去并不能获取到数据
			FEHttpClient.getInstance().post(request, new ResponseCallback<PromptResponse>() {
				@Override
				public void onCompleted(PromptResponse promptResponse) {
					List<ReferenceItem> referenceItems = promptResponse.referenceItems;
					if (CommonUtil.isEmptyList(referenceItems)) {
						subscriber.onError(new NullPointerException("No such prompt value by " + key));
					}
					else {
						subscriber.onNext(referenceItems);
					}
					subscriber.onCompleted();
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					super.onFailure(repositoryException);
					subscriber.onError(repositoryException.exception());
					subscriber.onCompleted();
				}
			});
		});
	}

	/**
	 * 分享日程
	 */
	public Observable<String> shareSchedule(AgendaDetailData agendaDetailData) {
		return Observable.create(subscriber -> {
			NewAgendaRequest newAgendaRequest = new NewAgendaRequest();
			newAgendaRequest.method = "edit";
			newAgendaRequest.title = agendaDetailData.title;
			newAgendaRequest.content = agendaDetailData.content;
			newAgendaRequest.startTime = agendaDetailData.startTime;
			newAgendaRequest.endTime = agendaDetailData.endTime;
			newAgendaRequest.master_key = agendaDetailData.marsterKey;
			newAgendaRequest.promptTime = agendaDetailData.promptTime;
			newAgendaRequest.repeatTime = agendaDetailData.repeatTime;
			newAgendaRequest.sharePerson = agendaDetailData.shareOther;

			FEHttpClient.getInstance().post(newAgendaRequest, new ResponseCallback<ResponseContent>() {
				@Override
				public void onCompleted(ResponseContent responseContent) {
					if (responseContent != null) {
						subscriber.onNext(responseContent.getErrorCode());
					}
					else {
						subscriber.onError(new NullPointerException("Share Schedule Failed, the response content is null."));
					}
					subscriber.onCompleted();
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					super.onFailure(repositoryException);
					subscriber.onError(repositoryException.exception());
					subscriber.onCompleted();
				}
			});
		});
	}

	public Observable<String> saveSchedule(NewAgendaRequest newAgenda) {
		return Observable.create(subscriber -> {
			FEHttpClient.getInstance().post(newAgenda, new ResponseCallback<ResponseContent>() {
				@Override
				public void onCompleted(ResponseContent responseContent) {
					if (responseContent != null) {
						subscriber.onNext(responseContent.getErrorCode());
					}
					else {
						subscriber.onError(new NullPointerException("Share Schedule Failed, the response content is null."));
					}
					subscriber.onCompleted();
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					super.onFailure(repositoryException);
					subscriber.onError(repositoryException.exception());
					subscriber.onCompleted();
				}
			});
		});
	}

	/**
	 * 获取日程回复详情
	 */
	public Observable<List<ScheduleReply>> getScheduleReplyList(String scheduleId) {
		return Observable.create(subscriber -> FEHttpClient.getInstance()
				.post(new AgendaReplyListRequest(scheduleId), new ResponseCallback<AgendaReplyListResponse>() {
					@Override
					public void onCompleted(AgendaReplyListResponse agendaReplyListResponse) {
						subscriber.onNext(agendaReplyListResponse.getErrorCode().equals("0") ?
								agendaReplyListResponse.getData() : null);
						subscriber.onCompleted();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						subscriber.onError(repositoryException.exception());
						subscriber.onCompleted();
					}
				}));
	}

	/**
	 * 回复日程
	 */
	public Observable<String> replyTheSchedule(String scheduleId, String content, String scheduleIdTitle, String scheduleCreater) {
		AgendaReplyRequest request = new AgendaReplyRequest(scheduleId, content, scheduleIdTitle, scheduleCreater);
		return Observable.create(subscriber -> FEHttpClient.getInstance()
				.post(request, new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						subscriber.onNext(responseContent.getErrorCode());
						subscriber.onCompleted();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						subscriber.onError(repositoryException.exception());
						subscriber.onCompleted();
					}
				}));
	}

	/**
	 * 更新日程回复
	 */
	public Observable<String> updateReply(String replyId, String replyContent, String eventTitle, String arrangerId, String eventId) {
		return Observable.create(subscriber -> FEHttpClient.getInstance()
				.post(new AgendaReplyUpdateRequest(replyId, replyContent, eventTitle, arrangerId, eventId),
						new ResponseCallback<ResponseContent>() {
							@Override
							public void onCompleted(ResponseContent responseContent) {
								subscriber.onNext(responseContent.getErrorCode());
								subscriber.onCompleted();
							}

							@Override
							public void onFailure(RepositoryException repositoryException) {
								super.onFailure(repositoryException);
								subscriber.onError(repositoryException.exception());
								subscriber.onCompleted();
							}
						}));
	}

	/**
	 * 删除程回复
	 */
	public Observable<String> deleteReply(String replyId, String eventTitle, String arrangerId, String eventId) {
		return Observable.create(subscriber -> FEHttpClient.getInstance()
				.post(new AgendaReplyDeleteRequest(replyId, eventTitle, arrangerId, eventId), new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						subscriber.onNext(responseContent.getErrorCode());
						subscriber.onCompleted();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						subscriber.onError(repositoryException.exception());
						subscriber.onCompleted();
					}
				}));
	}

	/**
	 * 把事件添加到系统日历事件项
	 * @param context context
	 * @param title 事件标题
	 * @param content 事件备注
	 * @param promptTime 提前提醒时间
	 * @param startTime 事件开始时间
	 * @param endTime 事件结束时间
	 * @param loopRule 重复规则
	 * @param scheduleId 日程id
	 */
	public Observable<Integer> addToSystemCalendar(Context context, String title, String content, String promptTime,
			String startTime, String endTime, String loopRule, String scheduleId) {
		if (isSystemScheduleExist(context, scheduleId)) {
			deleteSystemSchedule(context, scheduleId);
			removeScheduleEvent(context, scheduleId);
		}

		FELog.e("Auto : " + title + " , " + content + " , " + promptTime + " , " + startTime + " , " + endTime + " , " + loopRule + " , "
				+ scheduleId);

		return Observable
				.create(subscriber -> {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						Date startDate = sdf.parse(startTime);
						Date endDate = sdf.parse(endTime);
						long start = startDate.getTime();
						long end = endDate.getTime();
						String newLoopRule = TextUtils.isEmpty(loopRule) ? "" : ScheduleUtil.getLoopRule(loopRule, startDate, endDate);
						String calendarId = "";
						Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDAR_URL), null, null, null, null);
						if (userCursor.moveToFirst()) {
							calendarId = userCursor.getString(userCursor.getColumnIndex("_id"));
						}
						else {
							subscriber.onNext(404);
							return;
						}

						ContentValues event = new ContentValues();
						event.put("title", title);
						event.put("description", content);
						event.put("calendar_id", calendarId);
						event.put("dtstart", start);
						event.put("dtend", end);
						event.put("hasAlarm", 1);
						event.put("hasAttendeeData", 1);
						event.put("eventStatus", 1);
						event.put("eventTimezone", TimeZone.getDefault().getID().toString());
						event.put(CalendarContract.Events.RRULE, newLoopRule);
						Uri newEvent = context.getContentResolver().insert(Uri.parse(CALENDAR_EVENT_URL), event);

						long calendarEventId = Long.parseLong(newEvent.getLastPathSegment());
						saveScheduleEvent(context, scheduleId, calendarEventId + "");
						ContentValues values = new ContentValues();
						values.put("event_id", calendarEventId);
						values.put("method", 1);
						values.put("minutes", ScheduleUtil.getPromptMinute(promptTime));
						context.getContentResolver().insert(Uri.parse(CALENDAR_REMINDERS_URL), values);
						subscriber.onNext(200);
					} catch (Exception e) {
						e.printStackTrace();
						subscriber.onNext(404);
					} finally {
						subscriber.onCompleted();
					}
				});
	}

	/**
	 * 检查系统日历是否已经存在此日程的相关信息
	 * @param scheduleId 日程 id
	 */
	private Boolean isSystemScheduleExist(Context context, String scheduleId) {
		Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALENDAR_EVENT_URL), null, null, null, null);
		boolean hasExist = false;
		String calendarEventId = getScheduleEvent(context, scheduleId);
		while (eventCursor.moveToNext()) {
			String _id = eventCursor.getString(eventCursor.getColumnIndex("_id"));
			if (TextUtils.equals(_id, calendarEventId)) {
				hasExist = true;
				break;
			}
		}
		eventCursor.close();
		return hasExist;
	}

	/**
	 * 删除系统日程
	 * @param scheduleId 日程id
	 */
	public int deleteSystemSchedule(Context context, String scheduleId) {
		String calendarEventId = getScheduleEvent(context, scheduleId);
		int eventId = context.getContentResolver().delete(Uri.parse(CALENDAR_EVENT_URL), "_id = ?", new String[]{calendarEventId});
		int remindersId = context.getContentResolver()
				.delete(Uri.parse(CALENDAR_REMINDERS_URL), "event_id = ?", new String[]{calendarEventId});
		return eventId;
	}

	public void saveScheduleEvent(Context context, String scheduleId, String calendarEventId) {
		SharedPreferences preferences = context.getSharedPreferences("schedule_event", Context.MODE_PRIVATE);
		preferences.edit().putString(scheduleId, calendarEventId).commit();
	}

	public String getScheduleEvent(Context context, String scheduleId) {
		SharedPreferences preferences = context.getSharedPreferences("schedule_event", Context.MODE_PRIVATE);
		return preferences.getString(scheduleId, "");
	}

	public void removeScheduleEvent(Context context, String scheduleId) {
		SharedPreferences preferences = context.getSharedPreferences("schedule_event", Context.MODE_PRIVATE);
		preferences.edit().remove(scheduleId);
	}
}
