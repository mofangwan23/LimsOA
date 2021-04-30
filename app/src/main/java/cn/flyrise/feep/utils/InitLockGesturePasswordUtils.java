package cn.flyrise.feep.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import java.util.List;

import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.commonality.view.LockPatternView;

/**
 * cm
 * 手势密码工具类
 */
public class InitLockGesturePasswordUtils {
    public static boolean isAttemptLockout = true;
    private static LockPatternView mLockPatternView;
    private static TextView mHeadTextView;
    private static TextView gesturepwdUnlockFailtip;
    private static int mFailedPatternAttemptsSinceLastTimeout = 0;
    private static Context myContext;

    private static Handler mHandler = new Handler();
    private static Animation mShakeAnim;
    private static PasswordEntrySuccess myPasswordEntrySuccess;
    private static boolean isProhibitLogin = true;
    private static CountDownTimer countDownTimer = null;


    public static void InitLockGesturePasswordUtils(Context context, LockPatternView lockPatternView, TextView headTextView, TextView unlockFailtip) {
        myContext = context;
        new LockPatternUtils(context);
        mLockPatternView = lockPatternView;
        mHeadTextView = headTextView;
        gesturepwdUnlockFailtip = unlockFailtip;
        mShakeAnim = AnimationUtils.loadAnimation(myContext, R.anim.shake_x);
    }

    /**
     * 密码输入状态监听
     */
    public static LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

        private LockPatternUtils lockPatternUtils = new LockPatternUtils(myContext);

        @Override
        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        @Override
        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        @Override
        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (pattern == null) return;
            if (lockPatternUtils.checkPattern(pattern)) {
                if (!isProhibitLogin) {
                    return;
                }
                gesturepwdUnlockFailtip.setVisibility(View.INVISIBLE);
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
                if (myPasswordEntrySuccess != null) {
                    myPasswordEntrySuccess.Success();
                }
            }
            else {
                gesturepwdUnlockFailtip.setVisibility(View.VISIBLE);
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                    mFailedPatternAttemptsSinceLastTimeout++;
                    final int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mFailedPatternAttemptsSinceLastTimeout;
                    if (retry >= 0) {
                        if (retry == 0) {//五次输入失败，30秒后重新输入
                            isProhibitLogin = false;
                        }
                        mHeadTextView.setText(myContext.getResources().getString(R.string.lockpattern_need_off_password) + retry + "次");
                        mHeadTextView.setTextColor(Color.RED);
                        mHeadTextView.startAnimation(mShakeAnim);
                    }
                }
                else {
                    FEToast.showMessage(myContext.getResources().getString(R.string.lockpattern_recording_incorrect_min));
                }

                if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
                    isProhibitLogin = false;
                    if (isAttemptLockout) {
                        isAttemptLockout = false;
                        mHandler.postDelayed(attemptLockout, 500);
                    }
                }
                else {
                    mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
                }
            }
        }

        @Override public void onPatternCellAdded(List<LockPatternView.Cell> pattern) { }
    };
    /**
     * 密码错误时30秒计时
     */
    private static Runnable attemptLockout = new Runnable() {
        @Override public void run() {
            countDownTimer = new CountDownTimer(LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS + 1, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    final int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
                    if (secondsRemaining > 0) {
                        mHeadTextView.setText(secondsRemaining + myContext.getResources().getString(R.string.lockpattern_need_reset));
                        mHeadTextView.setTextColor(Color.RED);
                        mLockPatternView.clearPattern();
                        mLockPatternView.setEnabled(false);
                    }
                    else {
                        isAttemptLockout = true;
                        isProhibitLogin = true;
                        gesturepwdUnlockFailtip.setVisibility(View.INVISIBLE);
                        mHeadTextView.setText(myContext.getResources().getString(R.string.lockpattern_confirm));
                        mHeadTextView.setTextColor(myContext.getResources().getColor(R.color.lock_pattern_password_title));
                    }

                }

                @Override
                public void onFinish() {
                    mLockPatternView.setEnabled(true);
                    mFailedPatternAttemptsSinceLastTimeout = 0;
                }
            }.start();
        }
    };
    private static Runnable mClearPatternRunnable = new Runnable() {
        @Override
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    /**
     * 忘记密码
     */
    public static void forgetGestruePassword() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("FORGET_GESTRUE_PASSWORD", true);
        intent.setClass(myContext, NewLoginActivity.class);
        myContext.startActivity(intent);
        myContext.sendBroadcast(new Intent(FEMainActivity.CANCELLATION_LOCK_RECEIVER));
    }

    public interface PasswordEntrySuccess {
        void Success();
    }

    /**
     * 监听密码输入正确
     */
    public static void setPasswordEntrySuccess(PasswordEntrySuccess passwordEntrySuccess) {
        myPasswordEntrySuccess = passwordEntrySuccess;
    }
}
