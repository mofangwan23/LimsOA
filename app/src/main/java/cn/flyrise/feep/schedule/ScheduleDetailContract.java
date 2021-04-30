package cn.flyrise.feep.schedule;

import android.content.Context;
import android.content.Intent;

import cn.flyrise.android.protocol.entity.schedule.AgendaDetailData;
import cn.flyrise.android.protocol.model.Reply;
import cn.flyrise.feep.schedule.model.ScheduleReply;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-12-20 18:35
 */
public interface ScheduleDetailContract {

	interface IView {

		void getScheduleDetailSuccess(AgendaDetailData detailData);

		void getScheduleDetailFailed(String errorMessage);

		void deleteScheduleSuccess(String scheduleId);

		void deleteScheduleFailed();

		void getPromptTimeValue(String promptTime);

		void getRepeatTimeValue(String repeatTime);

		void getScheduleReplySuccess(List<ScheduleReply> replyList);

		void getScheduleReplyFailed();

		void deleteReplySuccess();

		void deleteReplyFailed();

		void updateReplySuccess();

		void updateReplyFailed();

		void replySuccess();

		void replyFailed();

		void shareOtherSuccess();

		void shareOtherFailed();

		void syncCalendarSuccess();

		void syncCalendarFailed();

		void showLoading();

		void hideLoading();

	}

	interface IPresenter {

		void start(Intent intent);

		void fetchScheduleDetail(String eventSourceId);

		void fetchScheduleReplyList(String scheduleId);

		void deleteReply(String replyId);

		void updateReply(String replyId, String content);

		void reply(String content);

		void deleteSchedule();

		void fetchPromptTime(String promptTime);

		void fetchRepeatTime(String repeatTime);

		void shareSchedule(String ids);

		boolean isSharedSchedule();

		void syncCalendarToSystem(Context context);

		AgendaDetailData getScheduleDetail();

	}

}
