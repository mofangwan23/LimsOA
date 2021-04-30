package cn.flyrise.feep.schedule;

import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.schedule.NewAgendaRequest;
import cn.flyrise.android.protocol.entity.schedule.PromptRequest;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.schedule.data.ScheduleDataRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016-11-28 15:30
 */
public class NewSchedulePresenter implements NewScheduleContract.IPresenter {

	private NewScheduleContract.IView mNewScheduleView;
	private ScheduleDataRepository mRepository;

	private List<String> mPromptTimeKeys = new ArrayList<>();
	private List<String> mPromptTimeValues = new ArrayList<>();
	private List<String> mRepeatTimeKeys = new ArrayList<>();
	private List<String> mRepeatTimeValues = new ArrayList<>();
	private List<AddressBook> mSeletedUsers;

	private String mPromptKey = "0";
	private String mRepeatKey = "0";
	private String mMasterKey = "";
	private String attachmentId = "";

	public NewSchedulePresenter(NewScheduleContract.IView newScheduleView) {
		this.mNewScheduleView = newScheduleView;
		this.mRepository = new ScheduleDataRepository();
	}

	@Override
	public void start(Intent intent) {
		String title = null, content = null, startTime = null, endTime = null;
		this.mMasterKey = intent.getStringExtra("marsterKey");
		if (isEdit()) {
			title = intent.getStringExtra("title");
			content = intent.getStringExtra("content");
			startTime = processScheduleTime(intent.getStringExtra("startTime"));
			endTime = processScheduleTime(intent.getStringExtra("endTime"));
			this.mPromptKey = intent.getStringExtra("promptTime");
			this.mRepeatKey = intent.getStringExtra("repeatTime");
			this.attachmentId = intent.getStringExtra("attachmentId");
		}
		else if (intent.getBooleanExtra("isFromWorkPlan", false)) {
			title = intent.getStringExtra("title");
			content = intent.getStringExtra("content");
		}
		if (TextUtils.isEmpty(startTime)) {
			String defaultDate = intent.getStringExtra(K.schedule.schedule_default_date);
			int year, month, day;
			if (!TextUtils.isEmpty(defaultDate)) {
				String[] dates = defaultDate.split("\\.");
				year = CommonUtil.parseInt(dates[0]);
				month = CommonUtil.parseInt(dates[1]);
				day = CommonUtil.parseInt(dates[2]);
			}
			else {
				Calendar calendar = Calendar.getInstance();
				year = calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH) + 1;
				day = calendar.get(Calendar.DAY_OF_MONTH);
			}
			startTime = String.format("%d-%02d-%02d 08:30", year, month, day);
			endTime = String.format("%d-%02d-%02d 17:30", year, month, day);
		}
		
		mNewScheduleView.initNewSchedule(title, content, startTime, endTime);

		mRepository.getReferenceItem(PromptRequest.METHOD_PROMPT, "")
				.retry(3)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(referenceItems -> {
					addToList(referenceItems, mPromptTimeKeys, mPromptTimeValues);
					int keyIndex = getKeyIndex(mPromptTimeKeys, mPromptKey);
					mNewScheduleView.configPromptTime(mPromptTimeValues, keyIndex == -1 ? null : mPromptTimeValues.get(keyIndex));
				}, exception -> {
					exception.printStackTrace();
				});

		mRepository.getReferenceItem(PromptRequest.METHOD_REPEAT, "")
				.retry(3)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(referenceItems -> {
					addToList(referenceItems, mRepeatTimeKeys, mRepeatTimeValues);
					int keyIndex = getKeyIndex(mRepeatTimeKeys, mRepeatKey);
					mNewScheduleView.configRepeatTime(mRepeatTimeValues, keyIndex == -1 ? null : mRepeatTimeValues.get(keyIndex));
				}, exception -> {
					exception.printStackTrace();
				});
	}

	@Override
	public void saveSchedule(String title, String content, String startTime, String endTime) {
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(title.trim())) {
			mNewScheduleView
					.saveScheduleFailed(CommonUtil.getString(R.string.schedule_new_title_null));
			return;
		}

		if (TextUtils.isEmpty(content) || TextUtils.isEmpty(content.trim())) {
			mNewScheduleView
					.saveScheduleFailed(CommonUtil.getString(R.string.schedule_new_content_null));
			return;
		}

		if (isEndTimeAfterStartTime(startTime, endTime)) {
			mNewScheduleView
					.saveScheduleFailed(CommonUtil.getString(R.string.schedule_new_time_out));
			return;
		}

		mNewScheduleView.showLoading();

		NewAgendaRequest newAgendaRequest = new NewAgendaRequest();
		newAgendaRequest.title = title;
		newAgendaRequest.startTime = startTime + ":00";
		newAgendaRequest.endTime = endTime + ":00";
		newAgendaRequest.promptTime = mPromptKey;
		newAgendaRequest.repeatTime = mRepeatKey;
		newAgendaRequest.content = content;
		newAgendaRequest.sharePerson = getSeletedUserIds();
		newAgendaRequest.method = "edit";
		if (!TextUtils.isEmpty(mMasterKey)) {
			newAgendaRequest.master_key = mMasterKey;
			newAgendaRequest.attachmentId = attachmentId;
		}

		mRepository.saveSchedule(newAgendaRequest)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(errorCode -> {
							mNewScheduleView.hideLoading();
							if (TextUtils.equals(errorCode, "0")) {
								mNewScheduleView.saveScheduleSuccess();
							}
							else if (TextUtils.equals(errorCode, "-1017004")) {
								mNewScheduleView.saveScheduleFailed(isEdit()
										? CommonUtil.getString(R.string.schedule_update_failed_share_more)
										: CommonUtil.getString(R.string.schedule_create_failed_share_more));
							}
							else {
								mNewScheduleView.saveScheduleFailed(isEdit()
										? CommonUtil.getString(R.string.schedule_update_save_failed)
										: CommonUtil.getString(R.string.schedule_new_save_failed));
							}
						},
						exception -> {
							mNewScheduleView.hideLoading();
							exception.printStackTrace();
							mNewScheduleView.saveScheduleFailed(isEdit()
									? CommonUtil.getString(R.string.schedule_update_save_failed)
									: CommonUtil.getString(R.string.schedule_new_save_failed));
						});
	}

	@Override
	public void setSeletedUsers(List<AddressBook> seletedUsers) {
		this.mSeletedUsers = seletedUsers;
		this.mNewScheduleView.setSelectedUsers(getSeletedUserNames());
	}

	@Override
	public List<AddressBook> getSeletedUsers() {
		return mSeletedUsers;
	}

	private String getSeletedUserIds() {
		if (CommonUtil.isEmptyList(mSeletedUsers)) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		int size = mSeletedUsers.size() - 1;
		for (int i = 0; i < size; i++) {
			builder.append(mSeletedUsers.get(i).userId).append(",");
		}
		builder.append(mSeletedUsers.get(size).userId);
		return builder.toString();
	}

	@Override
	public String getSeletedUserNames() {
		if (CommonUtil.isEmptyList(mSeletedUsers)) {
			return CommonUtil.getString(R.string.schedule_detail_lbl_share_none);
		}
		StringBuilder builder = new StringBuilder();
		int size = mSeletedUsers.size() - 1;
		for (int i = 0; i < size; i++) {
			builder.append(mSeletedUsers.get(i).name).append(",");
		}
		builder.append(mSeletedUsers.get(size).name);
		return builder.toString();
	}

	@Override
	public List<String> getPromptValues() {
		return mPromptTimeValues;
	}

	@Override
	public List<String> getRepeatValues() {
		return mRepeatTimeValues;
	}

	@Override
	public void setPrompt(int position) {
		mPromptKey = mPromptTimeKeys.get(position);
	}

	@Override
	public void setRepeat(int position) {
		mRepeatKey = mRepeatTimeKeys.get(position);
	}

	@Override
	public boolean isEdit() {
		return !TextUtils.isEmpty(mMasterKey);
	}

	private void addToList(List<ReferenceItem> referenceItems, List<String> keys, List<String> values) {
		for (ReferenceItem item : referenceItems) {
			keys.add(item.getKey());
			values.add(item.getValue());
		}
	}

	private boolean isEndTimeAfterStartTime(String startTime, String endTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			Date date1 = sdf.parse(startTime);
			Date date2 = sdf.parse(endTime);
			return date1.after(date2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	private String processScheduleTime(String scheduleTime) {
		int lastIndex = scheduleTime.lastIndexOf(":");
		return scheduleTime.substring(0, lastIndex);
	}

	private int getKeyIndex(List<String> keys, String defaultKey) {
		int keyIndex = -1;
		if (TextUtils.isEmpty(defaultKey)) {
			return keyIndex;
		}
		for (int i = 0, n = keys.size(); i < n; i++) {
			if (TextUtils.equals(keys.get(i), defaultKey)) {
				keyIndex = i;
				break;
			}
		}
		return keyIndex;
	}
}
