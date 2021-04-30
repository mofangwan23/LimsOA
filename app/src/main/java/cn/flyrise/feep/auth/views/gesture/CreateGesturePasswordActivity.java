package cn.flyrise.feep.auth.views.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.view.LockPatternView;
import cn.flyrise.feep.commonality.view.LockPatternView.Cell;
import cn.flyrise.feep.commonality.view.LockPatternView.DisplayMode;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.more.SetPassWordTypeActivity;
import cn.flyrise.feep.utils.LockPatternUtils;

public class CreateGesturePasswordActivity extends BaseActivity {

    private static final int ID_EMPTY_MESSAGE = -1;
    private static final String KEY_UI_STAGE = "uiStage";
    private static final String KEY_PATTERN_CHOICE = "chosenPattern";
    private LockPatternView mLockPatternView;
    private Button mStartCreateButton;
    protected TextView mHeaderText;
    protected List<Cell> mChosenPattern = null;
    private Toast mToast;
    private Stage mUiStage = Stage.Introduction;
    private View[][] mPreviewViews = new View[3][3];
    private final List<Cell> mAnimatePattern = new ArrayList<>();
    private boolean resetPassword = false;
    private boolean isFirstUse;

    enum LeftButtonMode {
        Cancel(android.R.string.cancel, true),
        CancelDisabled(android.R.string.cancel, false),
        Retry(R.string.lockpattern_retry_button_text, true),
        RetryDisabled(R.string.lockpattern_retry_button_text, false),
        Gone(ID_EMPTY_MESSAGE, false);

        /**
         * @param text    The displayed text for this mode.
         * @param enabled Whether the button should be enabled.
         */
        LeftButtonMode(int text, boolean enabled) {
            this.text = text;
            this.enabled = enabled;
        }

        final int text;
        final boolean enabled;
    }

    /**
     * The states of the right button.
     */
    enum RightButtonMode {
        Continue(R.string.lockpattern_continue_button_text, true), ContinueDisabled(R.string.lockpattern_continue_button_text, false), Confirm(R.string.lockpattern_confirm_button_text, true), ConfirmDisabled(R.string.lockpattern_confirm_button_text, false), Ok(android.R.string.ok, true);

        /**
         * @param text    The displayed text for this mode.
         * @param enabled Whether the button should be enabled.
         */
        RightButtonMode(int text, boolean enabled) {
            this.text = text;
            this.enabled = enabled;
        }

        final int text;
        final boolean enabled;
    }

    /**
     * Keep track internally of where the user is in choosing a pattern.
     */
    protected enum Stage {

        Introduction(R.string.lockpattern_recording_intro_header, LeftButtonMode.Cancel, RightButtonMode.ContinueDisabled, ID_EMPTY_MESSAGE, true), HelpScreen(R.string.lockpattern_settings_help_how_to_record, LeftButtonMode.Gone, RightButtonMode.Ok, ID_EMPTY_MESSAGE, false), ChoiceTooShort(R.string.lockpattern_recording_incorrect_too_short, LeftButtonMode.Retry, RightButtonMode.ContinueDisabled,
                ID_EMPTY_MESSAGE, true), FirstChoiceValid(R.string.lockpattern_pattern_entered_header, LeftButtonMode.Retry, RightButtonMode.Continue, ID_EMPTY_MESSAGE, false), NeedToConfirm(R.string.lockpattern_need_to_confirm, LeftButtonMode.Cancel, RightButtonMode.ConfirmDisabled, ID_EMPTY_MESSAGE, true), ConfirmWrong(R.string.lockpattern_need_to_unlock_wrong, LeftButtonMode.Cancel,
                RightButtonMode.ConfirmDisabled, ID_EMPTY_MESSAGE, true), ChoiceConfirmed(R.string.lockpattern_pattern_confirmed_header, LeftButtonMode.Cancel, RightButtonMode.Confirm, ID_EMPTY_MESSAGE, false);

        /**
         * @param headerMessage  The message displayed at the top.
         * @param leftMode       The mode of the left button.
         * @param rightMode      The mode of the right button.
         * @param footerMessage  The footer message.
         * @param patternEnabled Whether the pattern widget is enabled.
         */
        Stage(int headerMessage, LeftButtonMode leftMode, RightButtonMode rightMode, int footerMessage, boolean patternEnabled) {
            this.headerMessage = headerMessage;
            this.leftMode = leftMode;
            this.rightMode = rightMode;
            this.footerMessage = footerMessage;
            this.patternEnabled = patternEnabled;
        }

        final int headerMessage;
        final LeftButtonMode leftMode;
        final RightButtonMode rightMode;
        final int footerMessage;
        final boolean patternEnabled;
    }

    private void showToast() {
        if (null == mToast) {
            mToast = Toast.makeText(this, R.string.set_pw_success, Toast.LENGTH_SHORT);
        }
        else {
            mToast.setText(R.string.set_pw_success);
        }

        mToast.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesturepassword_create);
        if (savedInstanceState == null) {
            updateStage(Stage.Introduction);
            if (!resetPassword) {
                if (isFirstUse) {
                    updateStage(Stage.HelpScreen);
                }
            }
        }
        else {
            final String patternString = savedInstanceState.getString(KEY_PATTERN_CHOICE);
            if (patternString != null) {
                mChosenPattern = LockPatternUtils.stringToPattern(patternString);
            }
            updateStage(Stage.values()[savedInstanceState.getInt(KEY_UI_STAGE)]);
        }
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle(resetPassword ? R.string.reset_password : R.string.gestrue_password_title);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clerListener();
                if (!resetPassword) {
                    SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
                }
                finish();
            }
        });
    }

    @Override
    protected void onSwipeOpened() {
        clerListener();
        if (!resetPassword) {
            SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
        }
    }

    @Override
    public void bindView() {
        mLockPatternView = (LockPatternView) this.findViewById(R.id.gesturepwd_create_lockview);
        mHeaderText = (TextView) findViewById(R.id.gesturepwd_create_text);
        mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
        mLockPatternView.setTactileFeedbackEnabled(true);
        mStartCreateButton = (Button) this.findViewById(R.id.start_create_btn);
    }

    @Override
    public void bindData() {
        resetPassword = getIntent().getBooleanExtra(SetPassWordTypeActivity.RESET_PASSWORD, false);
        isFirstUse = SpUtil.get(PreferencesUtils.FIRST_USE_GESTURE_PASSWORD, true);
        if (isFirstUse) {
            SpUtil.put(PreferencesUtils.FIRST_USE_GESTURE_PASSWORD, false);
        }
        if (resetPassword) {
            isFirstUse = false;
        }
        // 初始化演示动画
        mAnimatePattern.add(Cell.of(0, 0));
        mAnimatePattern.add(Cell.of(0, 1));
        mAnimatePattern.add(Cell.of(1, 1));
        mAnimatePattern.add(Cell.of(2, 1));
        mAnimatePattern.add(Cell.of(2, 2));
        initPreviewViews();
        if (resetPassword || !isFirstUse) {
            mStartCreateButton.setVisibility(View.INVISIBLE);
            mHeaderText.setText(getResources().getString(R.string.lockpattern_recording_intro_header));
        }
        else {
            mStartCreateButton.setVisibility(View.VISIBLE);
            mHeaderText.setText(getResources().getString(R.string.lockpattern_settings_help_how_to_record));
        }
    }

    @Override
    public void bindListener() {
        super.bindListener();
        mStartCreateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hintCourse();
            }
        });
    }

    private void hintCourse() {
        if (mStartCreateButton.getVisibility() == View.VISIBLE) {
            mStartCreateButton.setVisibility(View.INVISIBLE);
            mHeaderText.setText(getResources().getString(R.string.lockpattern_recording_intro_header));
        }
        if (mUiStage.rightMode == RightButtonMode.Ok) {
            if (mUiStage != Stage.HelpScreen) {
                throw new IllegalStateException("Help screen is only mode with ok button, but " + "stage is " + mUiStage);
            }
            mLockPatternView.clearPattern();
            mLockPatternView.setDisplayMode(DisplayMode.Correct);
            updateStage(Stage.Introduction);
        }
    }

    private void initPreviewViews() {
        mPreviewViews = new View[3][3];
        mPreviewViews[0][0] = findViewById(R.id.gesturepwd_setting_preview_0);
        mPreviewViews[0][1] = findViewById(R.id.gesturepwd_setting_preview_1);
        mPreviewViews[0][2] = findViewById(R.id.gesturepwd_setting_preview_2);
        mPreviewViews[1][0] = findViewById(R.id.gesturepwd_setting_preview_3);
        mPreviewViews[1][1] = findViewById(R.id.gesturepwd_setting_preview_4);
        mPreviewViews[1][2] = findViewById(R.id.gesturepwd_setting_preview_5);
        mPreviewViews[2][0] = findViewById(R.id.gesturepwd_setting_preview_6);
        mPreviewViews[2][1] = findViewById(R.id.gesturepwd_setting_preview_7);
        mPreviewViews[2][2] = findViewById(R.id.gesturepwd_setting_preview_8);
    }

    private void updatePreviewViews() {
        if (mChosenPattern == null) {
            return;
        }
        for (final Cell cell : mChosenPattern) {
            mPreviewViews[cell.getRow()][cell.getColumn()].setBackgroundResource(R.drawable.gesture_create_grid_selected);
        }
    }

    // 清除已绘制的图案
    private void clearPreviewViews() {
        if (mChosenPattern == null) {
            return;
        }
        for (final Cell cell : mChosenPattern) {
            mPreviewViews[cell.getRow()][cell.getColumn()].setBackgroundResource(R.drawable.gesture_create_grid_background);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_UI_STAGE, mUiStage.ordinal());
        if (mChosenPattern != null) {
            outState.putString(KEY_PATTERN_CHOICE, LockPatternUtils.patternToString(mChosenPattern));
        }
    }

    private final Runnable mClearPatternRunnable = new Runnable() {
        @Override
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            patternInProgress();
        }

        @Override
        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        // 松开手之后相应事件
        @Override
        public void onPatternDetected(List<Cell> pattern) {
            if (pattern == null) {
                return;
            }
            // Log.i("way", "result = " + pattern.toString());
            if (mUiStage == Stage.NeedToConfirm || mUiStage == Stage.ConfirmWrong) {
                if (mChosenPattern == null) {
                    throw new IllegalStateException("null chosen pattern in stage 'need to confirm");
                }
                if (mChosenPattern.equals(pattern)) {
                    updateStage(Stage.ChoiceConfirmed);
                    correctListener();
                }
                else {
                    updateStage(Stage.ConfirmWrong);
                    mHeaderText.setText(getResources().getString(R.string.lockpattern_need_to_unlock_wrong));
                    clerListener();
                }
            }
            else if (mUiStage == Stage.Introduction || mUiStage == Stage.ChoiceTooShort) {
                if (pattern.size() < LockPatternUtils.MIN_LOCK_PATTERN_SIZE) {
                    updateStage(Stage.ChoiceTooShort);
                    mHeaderText.setText(getResources().getString(R.string.lockpattern_recording_incorrect_too_short));
                }
                else {
                    mChosenPattern = new ArrayList<>(pattern);
                    updateStage(Stage.FirstChoiceValid);
                    mHeaderText.setText(getResources().getString(R.string.lockpattern_need_to_confirm));
                    correctListener();
                }
            }
            else {
                throw new IllegalStateException("Unexpected stage " + mUiStage + " when " + "entering the pattern.");
            }
        }

        @Override
        public void onPatternCellAdded(List<Cell> pattern) {

        }

        // 绘制中的相应事件
        private void patternInProgress() {
            mHeaderText.setText(R.string.lockpattern_recording_inprogress);
        }
    };

    private void updateStage(Stage stage) {
        mUiStage = stage;
        // if (stage == Stage.ChoiceTooShort) {
        // mHeaderText.setText(getResources().getString(stage.headerMessage,
        // LockPatternUtils.MIN_LOCK_PATTERN_SIZE));
        // } else {
        // mHeaderText.setText(stage.headerMessage);
        // }
        if (stage.patternEnabled) {
            mLockPatternView.enableInput();
        }
        else {
            mLockPatternView.disableInput();
        }

        mLockPatternView.setDisplayMode(DisplayMode.Correct);
        switch (mUiStage) {
            case Introduction:
                mLockPatternView.clearPattern();
                break;
            case HelpScreen:
                mLockPatternView.setPattern(DisplayMode.Animate, mAnimatePattern);
                break;
            case ChoiceTooShort:
                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case FirstChoiceValid:
                break;
            case NeedToConfirm:
                mLockPatternView.clearPattern();
                updatePreviewViews();
                break;
            case ConfirmWrong:
                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case ChoiceConfirmed:
                break;
        }

    }

    private void postClearPatternRunnable() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
    }

    // 清空事件
    public void clerListener() {
        clearPreviewViews();
        mChosenPattern = null;
        mLockPatternView.clearPattern();
        updateStage(Stage.Introduction);
    }

    // 输入正确后的事件
    private void correctListener() {
        if (mUiStage.rightMode == RightButtonMode.Continue) {
            if (mUiStage != Stage.FirstChoiceValid) {
                throw new IllegalStateException("expected ui stage " + Stage.FirstChoiceValid + " when button is " + RightButtonMode.Continue);
            }
            updateStage(Stage.NeedToConfirm);
        }
        else if (mUiStage.rightMode == RightButtonMode.Confirm) {
            if (mUiStage != Stage.ChoiceConfirmed) {
                throw new IllegalStateException("expected ui stage " + Stage.ChoiceConfirmed + " when button is " + RightButtonMode.Confirm);
            }
            saveChosenPatternAndFinish();
        }
        else if (mUiStage.rightMode == RightButtonMode.Ok) {
            if (mUiStage != Stage.HelpScreen) {
                throw new IllegalStateException("Help screen is only mode with ok button, but " + "stage is " + mUiStage);
            }
            mLockPatternView.clearPattern();
            mLockPatternView.setDisplayMode(DisplayMode.Correct);
            updateStage(Stage.Introduction);
        }
    }

    private void saveChosenPatternAndFinish() {
        new LockPatternUtils(this).saveLockPattern(mChosenPattern);
        showToast();
        sendBroadcast(new Intent(FEMainActivity.OPEN_LOCK_RECEIVER));
        SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, true);
        SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            clerListener();
            if (!resetPassword) {
                SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
            }
            finish();
            return true;
        }
        return false;
    }
}
