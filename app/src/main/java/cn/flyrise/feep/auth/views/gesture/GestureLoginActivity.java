package cn.flyrise.feep.auth.views.gesture;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.BaseThreeLoginActivity;
import cn.flyrise.feep.commonality.view.LockPatternView;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.utils.LockPatternUtils;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-10-19 08:56
 */
public class GestureLoginActivity extends BaseThreeLoginActivity {

	private LockPatternView mLockPatternView;
	private LockPatternUtils mLockPatternUtils;
	private int mFailedPatternAttemptsSinceLastTimeout = 0;
	private CountDownTimer timer;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_login);
	}

	@Override public void bindView() {
		super.bindView();
		mLockPatternView = (LockPatternView) findViewById(R.id.lockPatternView);
	}

	@Override public void bindData() {
		super.bindData();
		mLockPatternUtils = new LockPatternUtils(this);
	}

	@Override public void bindListener() {
		super.bindListener();

		mLockPatternView.setTactileFeedbackEnabled(true);
		mLockPatternView.setOnPatternListener(new LockPatternView.OnPatternListener() {

			@Override public void onPatternCellAdded(List<LockPatternView.Cell> pattern) { }

			@Override public void onPatternStart() {
				mLockPatternView.removeCallbacks(mClearPatternCallback);
			}

			@Override public void onPatternCleared() {
				mLockPatternView.removeCallbacks(mClearPatternCallback);
			}

			@Override public void onPatternDetected(List<LockPatternView.Cell> pattern) {
				if (mLockPatternUtils.checkPattern(pattern)) {
					mTvRetryPrompt.setVisibility(View.INVISIBLE);
					mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
					if (mUserBean != null && mUserBean.isVPN()) {
						initVpnSetting();
					}
					else {
						mAuthPresenter.executeLogin();
					}
					return;
				}

				mTvRetryPrompt.setVisibility(View.VISIBLE);
				mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);

				if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
					mFailedPatternAttemptsSinceLastTimeout++;
					final int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mFailedPatternAttemptsSinceLastTimeout;
					if (retry >= 0) {
						mTvErrorPrompt.setText(getResources().getString(R.string.lockpattern_need_off_password) + retry + "次");
						mTvErrorPrompt.setTextColor(Color.RED);
						mTvErrorPrompt.startAnimation(AnimationUtils.loadAnimation(GestureLoginActivity.this, R.anim.shake_x));
					}
				}
				else {
					FEToast.showMessage(getResources().getString(R.string.lockpattern_recording_incorrect_min));
				}

				if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
					new Handler(getMainLooper()).postDelayed(mTimerLockOut, 200);
				}
				else {
					mLockPatternView.postDelayed(mClearPatternCallback, 2000);
				}
			}
		});
	}

	private final Runnable mClearPatternCallback = new Runnable() {     // 用于清理
		@Override public void run() {
			mLockPatternView.clearPattern();
		}
	};

	private final Runnable mTimerLockOut = new Runnable() {     // 定时器，30s 内不能操作
		@Override public void run() {
			mLockPatternView.clearPattern();
			mLockPatternView.setEnabled(false);
			timer = new CountDownTimer(LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS + 1500, 1000) {
				@Override public void onTick(long millisUntilFinished) {
					int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
					if (secondsRemaining > 0) {
						mTvErrorPrompt.setText(secondsRemaining + getResources().getString(R.string.lockpattern_need_reset));
					}
					else {
						mTvRetryPrompt.setVisibility(View.INVISIBLE);
						mTvErrorPrompt.setText(getResources().getString(R.string.lockpattern_confirm));
						mTvErrorPrompt.setTextColor(getResources().getColor(R.color.lock_pattern_password_title));
					}
				}

				@Override public void onFinish() {
					mLockPatternView.setEnabled(true);
					mFailedPatternAttemptsSinceLastTimeout = 0;
				}
			}.start();
		}
	};

	@Override
	protected void onDestroy() {
		if (timer != null) {
			timer.cancel();
		}
		super.onDestroy();
	}
}
