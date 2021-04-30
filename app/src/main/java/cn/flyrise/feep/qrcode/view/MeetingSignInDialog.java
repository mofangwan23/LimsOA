package cn.flyrise.feep.qrcode.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.location.contract.RxCountDownTimerContract;
import cn.flyrise.feep.location.util.RxCountDownTimer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * klc on 2018/3/23.
 */

public class MeetingSignInDialog extends DialogFragment implements RxCountDownTimerContract.RxCountDownTimerListener {

	private final static int MAX_TIME = 10;

	private TextView mTvTime;
	private TextView mTvTitle;
	private TextView mTvMeetingTime;
	private TextView mTvAddress;
	private TextView mTvMeetingMaster;
	private TextView mTvSignDateTime;
	private ImageView mImgSignType;

	private RxCountDownTimerContract mCountDownTimer;

	private Builder mBuilder;

	public void setBuilder(Builder builder) {
		this.mBuilder = builder;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		View view = inflater.inflate(R.layout.success_content_dialog_fragment, container, false);
		mTvTitle = view.findViewById(R.id.title);
		mTvMeetingTime = view.findViewById(R.id.time);
		mTvAddress = view.findViewById(R.id.address);
		mTvMeetingMaster = view.findViewById(R.id.meeting_master);
		mTvTime = view.findViewById(R.id.tvTime);
		mTvSignDateTime = view.findViewById(R.id.meeting_sign_date_time);
		mImgSignType = view.findViewById(R.id.icon);
		bindData();
		bindListener();
		return view;
	}

	private void bindData() {
		if (mBuilder == null) return;
		if (!TextUtils.isEmpty(mBuilder.title))
			mTvTitle.setText(getString(R.string.meeting_titile) + mBuilder.title);
		if (!TextUtils.isEmpty(mBuilder.startTime) && !TextUtils.isEmpty(mBuilder.endTime))
			mTvMeetingTime.setText(getString(R.string.meeting_time) + getTimeText(mBuilder.startTime, mBuilder.endTime));
		if (!TextUtils.isEmpty(mBuilder.address))
			mTvAddress.setText(getString(R.string.meeting_place) + mBuilder.address);
		if (!TextUtils.isEmpty(mBuilder.meetingMaster))
			mTvMeetingMaster.setText(getString(R.string.meeting_master) + mBuilder.meetingMaster);
		if (!TextUtils.isEmpty(mBuilder.signTime)) {
			String title = getString(TextUtils.equals("1", mBuilder.signType)
					? R.string.meeting_sign_back_time : R.string.meeting_sign_time);
			mTvSignDateTime.setText(title + mBuilder.signTime);
		}
		mTvSignDateTime.setVisibility(TextUtils.isEmpty(mBuilder.signTime) ? View.GONE : View.VISIBLE);
		int signIcon = TextUtils.equals("1", mBuilder.signType) ? R.drawable.metting_sign_out : R.drawable.metting_sign_success_;
		mImgSignType.setBackgroundResource(signIcon);
		mTvTime.setText(String.format(getResources().getString(R.string.location_meeting_sign_dismiss), String.valueOf(MAX_TIME)));
		mCountDownTimer = new RxCountDownTimer(this);
		mCountDownTimer.startCountDown(MAX_TIME);
	}

	private void bindListener() {
		mTvTime.setOnClickListener(v -> dismiss());
	}

	public void show(FragmentManager manager, String tag) {
		FragmentTransaction ft = manager.beginTransaction();
		ft.add(this, tag);
		ft.commitAllowingStateLoss();//小米手机容易见鬼
	}

	@Override
	public void dismiss() {
		super.dismiss();
		mCountDownTimer.unSubscription();
	}

	@Override
	public void onCompleted() {
		dismiss();
	}

	@Override
	public void onNext(String integer) {
		mTvTime.setText(String.format(getResources()
				.getString(R.string.location_meeting_sign_dismiss), String.valueOf(integer)));
	}


	public static class Builder {

		private String title;
		private String startTime;
		private String endTime;
		private String address;
		private String meetingMaster;
		private String signTime;
		private String signType;

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setStartTime(String startTime) {
			this.startTime = startTime;
			return this;
		}

		public Builder setEndTime(String endTime) {
			this.endTime = endTime;
			return this;
		}

		public Builder setAddress(String address) {
			this.address = address;
			return this;
		}

		public Builder setSignTime(String signTime) {
			this.signTime = signTime;
			return this;
		}

		public Builder setSignType(String signType) {
			this.signType = signType;
			return this;
		}

		public Builder setMeetingMaster(String meetingMaster) {
			this.meetingMaster = meetingMaster;
			return this;
		}

		public MeetingSignInDialog create() {
			MeetingSignInDialog dialog = new MeetingSignInDialog();
			dialog.setBuilder(this);
			return dialog;
		}
	}

	private String getTimeText(String startTime, String endTime) {
		Calendar start = stringToCalendar(startTime);
		Calendar end = stringToCalendar(endTime);
		if (start == null || end == null || TextUtils.equals(startTime, endTime)) {
			return startTime;
		}
		StringBuilder builder = new StringBuilder();
		if (start.get(Calendar.YEAR) == end.get(Calendar.YEAR)
				&& start.get(Calendar.MONTH) == end.get(Calendar.MONTH)
				&& start.get(Calendar.DAY_OF_MONTH) == end.get(Calendar.DAY_OF_MONTH)) {
			builder.append(getDateText(start.get(Calendar.MONTH) + 1));
			builder.append("-");
			builder.append(getDateText(start.get(Calendar.DAY_OF_MONTH)));
			builder.append(" ");
			builder.append(getDateText(start.get(Calendar.HOUR_OF_DAY)));
			builder.append(":");
			builder.append(getDateText(start.get(Calendar.MINUTE)));
			builder.append(" ");
			builder.append("～");
			builder.append(" ");
			builder.append(getDateText(end.get(Calendar.HOUR_OF_DAY)));
			builder.append(":");
			builder.append(getDateText(end.get(Calendar.MINUTE)));
		}
		else {
			builder.append(startTime);
			builder.append(" ");
			builder.append("～");
			builder.append(" ");
			builder.append(endTime);
		}
		return builder.toString();
	}

	private String getDateText(int time) {
		return time > 9 ? String.valueOf(time) : 0 + String.valueOf(time);
	}

	private Calendar stringToCalendar(String time) {
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(sd.parse(time));
			return calendar;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}