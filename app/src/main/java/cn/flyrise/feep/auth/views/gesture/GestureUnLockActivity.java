package cn.flyrise.feep.auth.views.gesture;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import cn.flyrise.feep.auth.views.BaseAuthActivity;
import cn.flyrise.feep.auth.views.BaseUnLockActivity;
import cn.flyrise.feep.commonality.view.LockPatternView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.services.IApplicationServices;
import cn.flyrise.feep.protocol.FeepApplicationServices;
import cn.flyrise.feep.utils.LockPatternUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GestureUnLockActivity extends BaseUnLockActivity {

    private LockPatternUtils mLockPatternUtils;
    private LockPatternView mLockPatternView;

    private static int sRetryTimes = 5;
    private static int sSurplusTimes;
    private static CountDownTimer sCountDownTimer;

    private Handler mHandler = new Handler();

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesturepassword_unlock);
    }

    @Override public void bindView() {
        super.bindView();
        mLockPatternView =  this.findViewById(R.id.gesturepwd_unlock_lockview);
    }

    @Override public void bindData() {
        super.bindData();
        mTvErrorPrompt.setText(getResources().getString(R.string.lockpattern_confirm));
        mLockPatternUtils = new LockPatternUtils(this);
    }

    @Override public void bindListener() {
        super.bindListener();
        mLockPatternView.setTactileFeedbackEnabled(true);
        if (isAllowForgetPwd) {
            mTvForgetPwd.setVisibility(View.VISIBLE);
            mTvForgetPwd.setOnClickListener(view -> {
                if (isLockMainActivity) {
                    CoreZygote.getApplicationServices().exitApplication();
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(BaseAuthActivity.EXTRA_FORGET_PASSWORD, true);
                    intent.setClass(GestureUnLockActivity.this, NewLoginActivity.class);
                    startActivity(intent);
                    sendBroadcast(new Intent(FEMainActivity.CANCELLATION_LOCK_RECEIVER));
                    finish();
                }
                else {
                    final Intent intent = new Intent(GestureUnLockActivity.this, CreateGesturePasswordActivity.class);
                    intent.putExtra(RESET_PASSWORD, true);
                    startActivity(intent);
                    Observable
                            .timer(1, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(code -> {
                                setResult(404);
                                finish();
                            }, exception -> {
                                exception.printStackTrace();
                                finish();
                            });
                }
            });
        }
        else {
            mTvForgetPwd.setVisibility(View.GONE);
        }

        mLockPatternView.setTactileFeedbackEnabled(true);
        mLockPatternView.setOnPatternListener(new LockPatternView.OnPatternListener() {

            @Override public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
            }

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
                    passwordEntrySuccess();
                    return;
                }

                mTvRetryPrompt.setVisibility(View.VISIBLE);
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);

                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                    sRetryTimes = sRetryTimes - 1;
                    if (sRetryTimes >= 0) {
                        mTvErrorPrompt.setText(getResources().getString(R.string.lockpattern_need_off_password) + sRetryTimes + "次");
                        mTvErrorPrompt.setTextColor(Color.RED);
                        mTvErrorPrompt.startAnimation(AnimationUtils.loadAnimation(GestureUnLockActivity.this, R.anim.shake_x));
                    }
                }
                else {
                    FEToast.showMessage(getResources().getString(R.string.lockpattern_recording_incorrect_min));
                }

                if (sRetryTimes == 0) {
                    mHandler.postDelayed(mTimerLockOut, 1000);
                }
                else {
                    mLockPatternView.postDelayed(mClearPatternCallback, 2000);
                }
            }
        });

        boolean isFromSalary = getIntent().getBooleanExtra("isSalary", false);
        if (isFromSalary) {
            if (sSurplusTimes > 0) {
                if (sCountDownTimer != null) {
                    sCountDownTimer.cancel();
                }
                mHandler.post(mTimerLockOut);
            }
        }
        else {
            sSurplusTimes = 0;
            if (sCountDownTimer != null) {
                sCountDownTimer.cancel();
            }
        }
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
            mTvErrorPrompt.setTextColor(Color.RED);

            if (sCountDownTimer != null) {
                sCountDownTimer.cancel();
            }

            long millisInFuture = sSurplusTimes == 0
                    ? LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS + 1000
                    : sSurplusTimes * 1000;

            sCountDownTimer = new CountDownTimer(millisInFuture, 1000) {
                @Override public void onTick(long millisUntilFinished) {
                    int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
                    sSurplusTimes = secondsRemaining;
                    if (secondsRemaining > 0) {
                        if (mTvErrorPrompt != null) {
                            mTvErrorPrompt.setText(secondsRemaining + " " +
                                    getResources().getString(R.string.lockpattern_need_reset));
                        }
                    }
                    else {
                        if (mTvRetryPrompt != null) {
                            mTvRetryPrompt.setVisibility(View.INVISIBLE);
                            mTvErrorPrompt.setText(getResources().getString(R.string.lockpattern_confirm));
                            mTvErrorPrompt.setTextColor(getResources().getColor(R.color.lock_pattern_password_title));
                        }
                    }
                }

                @Override public void onFinish() {
                    sSurplusTimes = 0;
                    if (mLockPatternView != null) {
                        mLockPatternView.setEnabled(true);
                        sRetryTimes = 5;
                    }
                }

            }.start();
        }
    };

    //密码输入成功后
    private void passwordEntrySuccess() {
        if (isLockMainActivity) {
            FeepApplicationServices.sLastTimeUnlockSuccess = System.currentTimeMillis();
            CoreZygote.getApplicationServices().setHomeKeyState(IApplicationServices.HOME_PRESS_AND_UN_LOCKED);
        }
        else {
            setResult(1001);
        }
        finish();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
