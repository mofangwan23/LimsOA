package cn.flyrise.feep.schedule.data;

import android.Manifest;
import android.text.TextUtils;
import android.util.SparseArray;
import cn.flyrise.android.protocol.entity.schedule.AgendaResponseItem;
import cn.flyrise.feep.commonality.util.HtmlUtil;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.schedule.utils.ScheduleUtil;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2016-11-28 15:34
 */
public class NativeScheduleDataSource {

	private final SparseArray<List<AgendaResponseItem>> mScheduleItems = new SparseArray<>();
	private String mCurrentDate;    // yyyy-MM
	private boolean isMonthChange;
	private ScheduleDataRepository mRepository;

	public NativeScheduleDataSource(ScheduleDataRepository remoteScheduleRepository) {
		this.mRepository = remoteScheduleRepository;
	}

	public List<AgendaResponseItem> filterItems(List<AgendaResponseItem> items, int day) {
		mScheduleItems.clear();
		if (CommonUtil.isEmptyList(items)) return null;
		FELog.i("filterItems : " + items.size());
		for (AgendaResponseItem item : items) {
			int[] date = item.getDate();
			int key = date == null ? 1024 : date[2];
			if (mScheduleItems.indexOfKey(key) >= 0) {
				List<AgendaResponseItem> agendaResponseItems = mScheduleItems.get(key);
				if (!agendaResponseItems.contains(item)) {
					agendaResponseItems.add(item);
				}
			}
			else {
				List<AgendaResponseItem> agendaResponseItems = new ArrayList<>();
				agendaResponseItems.add(item);
				mScheduleItems.put(key, agendaResponseItems);
			}
		}
		return getScheduleItems(day);
	}

	private List<AgendaResponseItem> syncSchedules(List<AgendaResponseItem> scheduleItems) {
		if (CommonUtil.isEmptyList(scheduleItems)) {
			return scheduleItems;
		}

		boolean isPermissionGranted = FePermissions.isPermissionGranted(CoreZygote.getContext(),
				new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR});
		if (!isPermissionGranted) { // 未授予日历权限
			return scheduleItems;
		}

		boolean autoSync = ScheduleUtil.hasUserScheduleSetting();
		FELog.i("AutoSchedule item = " + scheduleItems.size());
		if (autoSync) {
			Observable.from(scheduleItems)
					.flatMap(item -> mRepository.addToSystemCalendar(
							CoreZygote.getContext(), item.title, HtmlUtil.delHTMLTag(item.content),
							item.promptTime, item.startTime, item.endTime, item.loopRule, item.id))
					.subscribe(resultCode -> {
						FELog.i("ResultCode = " + resultCode);
					});
		}
		return scheduleItems;
	}

	public List<AgendaResponseItem> getScheduleItems(int day) {
		return mScheduleItems.get(day);
	}

	public boolean removeScheduleItem(String eventSourceId) {
		boolean remove = false;
		int key = -1;
		for (int i = 0, size = mScheduleItems.size(); i < size; i++) {
			key = mScheduleItems.keyAt(i);
			List<AgendaResponseItem> agendaResponseItems = mScheduleItems.get(key);
			List<Integer> removeIndexs = new ArrayList<>();
			for (int j = 0; j < agendaResponseItems.size(); j++) {
				AgendaResponseItem item = agendaResponseItems.get(j);
				if (TextUtils.equals(item.eventSourceId, eventSourceId)) {
					removeIndexs.add(j);
				}
			}

			for (int removeIndex : removeIndexs) {
				agendaResponseItems.remove(removeIndex);
			}
		}
		return remove;
	}

	public List<Integer> getAgendaDays(boolean refresh) {
		if (refresh) {
			return getAgendaDays();
		}
		if (isMonthChange) {
			return getAgendaDays();
		}
		return null;
	}

	private List<Integer> getAgendaDays() {
		int size = mScheduleItems.size();
		List<Integer> agendaDays = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int key = mScheduleItems.keyAt(i);
			List<AgendaResponseItem> agendaResponseItems = mScheduleItems.get(key);
			if (CommonUtil.isEmptyList(agendaResponseItems)) {
				continue;
			}
			agendaDays.add(key);
		}
		return agendaDays;
	}

	/**
	 * 获取日程列表
	 * 判断月份是否发生了改变，如果未发生改变，直接从缓存 mScheduleItems 中根据具体日期获取数据。
	 * 如果月份发生了改变，直接请求服务器数据。筛选完毕后，再根据具体日期获取数据。
	 */
	public Observable<List<AgendaResponseItem>> getAgendaList(String date, int day, boolean refefresh) {
		if (TextUtils.equals(mCurrentDate, date) && !refefresh) {
			isMonthChange = false;
			return Observable.create(subscriber -> {
				subscriber.onNext(getScheduleItems(day));
			});
		}

		this.mCurrentDate = date;
		this.isMonthChange = true;
		return mRepository
				.getAgendaList(date)
				.map(items -> syncSchedules(items))
				.map(items -> filterItems(items, day));
	}
}
