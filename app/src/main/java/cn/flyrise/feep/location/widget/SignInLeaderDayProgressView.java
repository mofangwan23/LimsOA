package cn.flyrise.feep.location.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.location.util.LeaderDayCricleCororKt;
import cn.flyrise.feep.location.util.LocationBitmapUtil;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-19:13.
 */

public class SignInLeaderDayProgressView extends RelativeLayout {


	private CricleProgressView mCricleProgress;

	private TextView mTvArrive;
	private TextView mTvShouldBe;

	private float arriveNum = 0;
	private float shouldBeNum = 0;

	private OnClickListener onClickListener;

	public SignInLeaderDayProgressView(Context context) {
		this(context, null);
	}

	public SignInLeaderDayProgressView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SignInLeaderDayProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.location_leader_kanban_day_layout, this);
		mCricleProgress = this.findViewById(R.id.preogress);
		mTvArrive = this.findViewById(R.id.arrive_num);
		mTvShouldBe = this.findViewById(R.id.should_be_num);
		findViewById(R.id.leader_kanban_detail_layout).setOnClickListener(v -> {
			if (onClickListener != null) onClickListener.onClick(v);
		});
		ImageView mImgIcon = this.findViewById(R.id.detail_icon);
		mImgIcon.setImageBitmap(LocationBitmapUtil.tintBitmap(context, R.drawable.detail_right_icon, Color.parseColor("#9DA3A6")));
	}

	public void setProgress() {
		if (mCricleProgress != null) mCricleProgress.setProgress(shouldBeNum == 0 ? 0 : arriveNum / shouldBeNum);
	}

	public void setArriveNum(int arriveNum) {//实到人数
		if (mTvArrive == null) return;
		this.arriveNum = arriveNum;
		mTvArrive.setText(arriveNum + "");
		mTvArrive.setTextColor(Color.parseColor(LeaderDayCricleCororKt.resetColor(shouldBeNum == 0 ? 0 : arriveNum / shouldBeNum)));
	}

	public void setShouldBeNum(int shouldBeNum) {//总人数
		this.shouldBeNum = shouldBeNum;
		if (mTvShouldBe != null) mTvShouldBe.setText("/" + shouldBeNum + "");
	}

	public void resetData() {
		shouldBeNum = 0;
		arriveNum = 0;
		if (mTvShouldBe != null) mTvShouldBe.setText("/" + 0 + "");
		if (mTvArrive != null) mTvArrive.setText(0 + "");
	}

	public void setOnClickeDetailListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}
}
