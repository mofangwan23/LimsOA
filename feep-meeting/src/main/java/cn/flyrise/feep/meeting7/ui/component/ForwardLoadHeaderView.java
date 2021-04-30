package cn.flyrise.feep.meeting7.ui.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.flyrise.feep.meeting7.R;

public class ForwardLoadHeaderView extends LinearLayout implements BaseRefreshHeader {

	private int mState = STATE_NORMAL;
	private ViewGroup mContainer;
	private ProgressBar mProgressBar;
	private TextView mStatusTextView;
	public int mMeasuredHeight;

	public ForwardLoadHeaderView(Context context) {
		this(context, null);
	}

	public ForwardLoadHeaderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ForwardLoadHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContainer = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.nms_meeting_forward_header_view, null);

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 0);
		this.setLayoutParams(params);
		this.setPadding(0, 0, 0, 0);

		addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
		setGravity(Gravity.BOTTOM);

		mStatusTextView = findViewById(R.id.nmsTvRefreshStatus);
		mProgressBar = findViewById(R.id.nmsProgressBar);

		measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		mMeasuredHeight = getMeasuredHeight();
	}

	public void setState(int state) {
		if (state == mState) return;

		if (state == STATE_REFRESHING) {
			// 显示进度
			mProgressBar.setVisibility(View.VISIBLE);
			smoothScrollTo(mMeasuredHeight);
		}
		else {
			// 显示箭头图片
			mProgressBar.setVisibility(View.INVISIBLE);
		}

		switch (state) {
			case STATE_NORMAL:
				mStatusTextView.setText("下拉加载更多");
				break;
			case STATE_RELEASE_TO_REFRESH:
				if (mState != STATE_RELEASE_TO_REFRESH) {
					mStatusTextView.setText("松开立即加载");
				}
				break;
			case STATE_REFRESHING:
				mStatusTextView.setText("正在加载");
				break;
			case STATE_DONE:
				mStatusTextView.setText("加载完成");
				break;
			default:
		}

		mState = state;
	}

	public int getState() {
		return mState;
	}

	@Override
	public void refreshComplete() {
		setState(STATE_DONE);
		postDelayed(this::reset, 200);
	}

	public void setVisibleHeight(int height) {
		if (height < 0) height = 0;
		LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
		lp.height = height;
		mContainer.setLayoutParams(lp);
	}

	public int getVisibleHeight() {
		return mContainer.getLayoutParams().height;
	}

	@Override
	public void onMove(float delta) {
		if (getVisibleHeight() > 0 || delta > 0) {
			setVisibleHeight((int) delta + getVisibleHeight());
			if (mState <= STATE_RELEASE_TO_REFRESH) { // 未处于刷新状态，更新箭头
				setState(getVisibleHeight() > mMeasuredHeight ? STATE_RELEASE_TO_REFRESH : STATE_NORMAL);
			}
		}
	}

	@Override
	public boolean releaseAction() {
		boolean isOnRefresh = false;
		int height = getVisibleHeight();
		if (height == 0) isOnRefresh = false;

		if (getVisibleHeight() > mMeasuredHeight && mState < STATE_REFRESHING) {
			setState(STATE_REFRESHING);
			isOnRefresh = true;
		}

		if (mState != STATE_REFRESHING) {
			smoothScrollTo(0);
		}

		if (mState == STATE_REFRESHING) {
			int destHeight = mMeasuredHeight;
			smoothScrollTo(destHeight);
		}

		return isOnRefresh;
	}

	public void reset() {
		smoothScrollTo(0);
		postDelayed(() -> setState(STATE_NORMAL), 500);
	}

	private void smoothScrollTo(int destHeight) {
		ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
		animator.addUpdateListener(animation -> setVisibleHeight((int) animation.getAnimatedValue()));
		animator.setDuration(300).start();
	}
}