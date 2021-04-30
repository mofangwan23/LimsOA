package cn.flyrise.feep.particular.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import cn.flyrise.android.protocol.model.MeetingAttendUser;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;

/**
 * @author ZYP
 * @since 2016-10-27 18:07
 */
public class MeetingAttendView extends ScrollView {

	TextView mTvMaster;
	TextView mTvAttendNumber;
	TextView mTvNotAttendNumber;
	TextView mTvConsiderNumber;
	TextView mTvNotDealNumber;
	LinearLayout mLySignInNumber;
	TextView mTvSignInNumber;
	LinearLayout content_layout;

	public MeetingAttendView(Context context) {
		this(context, null);
	}

	public MeetingAttendView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MeetingAttendView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.meeting_info, this);
		mTvMaster = (TextView) findViewById(R.id.Moderator);
		mTvAttendNumber = (TextView) findViewById(R.id.Participate_no);
		mTvNotAttendNumber = (TextView) findViewById(R.id.not_Participate_no);
		mTvConsiderNumber = (TextView) findViewById(R.id.unkown);
		mTvNotDealNumber = (TextView) findViewById(R.id.untreated);
		content_layout = (LinearLayout) findViewById(R.id.content_layout);
		mTvSignInNumber = (TextView) findViewById(R.id.tvSignInNumber);
		mLySignInNumber = findViewById(R.id.lySignInNumber);
	}

	public void setMeetingAttendUsers(ParticularPresenter.MeetingAttendUserVO attendUserVO) {
		mTvMaster.setText(attendUserVO.master);
		mTvAttendNumber.setText(attendUserVO.attendNumber);
		mTvNotAttendNumber.setText(attendUserVO.notAttendNumber);
		mTvConsiderNumber.setText(attendUserVO.considerNumber);
		mTvNotDealNumber.setText(attendUserVO.notDealNumber);
		if (TextUtils.isEmpty(attendUserVO.signInNumber)) {
			mLySignInNumber.setVisibility(GONE);
		}
		else {
			mTvSignInNumber.setText(attendUserVO.signInNumber);
		}
		if (CommonUtil.nonEmptyList(attendUserVO.meetingAttendUser)) {
			int size = attendUserVO.meetingAttendUser.size();
			for (int i = 0; i < size; i++) {
				MeetingAttendUser Users = attendUserVO.meetingAttendUser.get(i);
				LinearLayout meeting_content = (LinearLayout) LayoutInflater.from(getContext())
						.inflate(R.layout.meeting_info_content, null);
				TextView tvName = (TextView) meeting_content.findViewById(R.id.tvUserName);
				TextView tvStatus = (TextView) meeting_content.findViewById(R.id.status);
				tvName.setText(Users.getMeetingAttendUser());
				switch (Users.getMeetingAttendStatus()) {
					case "0":
						tvStatus.setTextColor(0xff000000);
						tvStatus.setText(getContext().getResources().getString(R.string.meeting_untreated_show));
						break;
					case "1":
						tvStatus.setTextColor(0xff29992a);
						tvStatus.setText(getContext().getResources().getString(R.string.meeting_attend));
						break;
					case "2":
						tvStatus.setTextColor(0xffff0000);
						tvStatus.setText(getContext().getResources().getString(R.string.meeting_not_attend));
						break;
					case "3":
						tvStatus.setTextColor(0xffff8000);
						tvStatus.setText(getContext().getResources().getString(R.string.meeting_unknown));
						break;
					case "4":
						tvStatus.setTextColor(0xff29992a);
						tvStatus.setText(getContext().getResources().getString(R.string.meeting_signIn));
				}
				content_layout.addView(meeting_content);
			}
		}
	}
}
