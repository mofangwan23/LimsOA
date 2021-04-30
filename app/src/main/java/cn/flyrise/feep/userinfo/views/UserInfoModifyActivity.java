package cn.flyrise.feep.userinfo.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.userInfo;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseEditableActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.userinfo.contract.ModifyContract;
import cn.flyrise.feep.userinfo.modle.UserInfoDetailItem;
import cn.flyrise.feep.userinfo.presenter.ModifyPresenter;
import com.borax12.materialdaterangepicker.DateTimePickerDialog;
import java.util.Calendar;

/**
 * Created by Administrator on 2017-4-25.
 * 修改用户信息
 */
public class UserInfoModifyActivity extends BaseEditableActivity implements ModifyContract.View, DateTimePickerDialog.ButtonCallBack,
		DateTimePickerDialog.DismissListener {

	private UserInfoDetailItem mBean;

	private EditText mTextContent;
	private TextView mTextError;
	private TextView mTextMaxNum;
	private TextView mTextHint;

	private LinearLayout mTextLayout;
	private String mText = "";
	private FEToolbar mToolbar;
	private int maxNum = 70;
	private ModifyContract.presenter mPresenter;
	private Handler mHandler = new Handler();
	private String modifyText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_userinfo_layout);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		mToolbar = toolbar;
	}

	@Override
	public void bindView() {
		super.bindView();
		mTextLayout = this.findViewById(R.id.text_layout);
		mTextContent = this.findViewById(R.id.modify_text);
		mTextError = this.findViewById(R.id.text_error);
		mTextMaxNum = this.findViewById(R.id.max_nums);
		mTextHint = this.findViewById(R.id.text_hint);
	}

	@Override
	public void bindData() {
		super.bindData();
		mPresenter = new ModifyPresenter(this);
		if (getIntent() == null) return;
		String text = getIntent().getStringExtra("USER_BEAN");
		if (TextUtils.isEmpty(text)) return;
		mBean = GsonUtil.getInstance().fromJson(text, UserInfoDetailItem.class);
		if (mBean == null) return;
		mToolbar.setTitle(getString(R.string.userinfo_change) + mBean.title);
		modifyText();
		notifiText();
		if (getType() == K.userInfo.DETAIL_EMAIL) {
			mTextContent.setFilters(mPresenter.addressFileter());
			mTextContent.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			mTextHint.setText(getResources().getString(R.string.modify_email_error));
			mTextHint.setVisibility(View.VISIBLE);
		}
		else if (mBean.itemType == K.userInfo.DETAIL_LOCATION) {
			mTextContent.setFilters(mPresenter.addressFileter());
			mTextMaxNum.setVisibility(View.VISIBLE);
		}
		else if (mBean.itemType == K.userInfo.DETAIL_TEL) {
			maxNum = 50;
		}
		else if (mBean.itemType == K.userInfo.DETAIL_PHONE) {
			maxNum = 13;//电话号码11位加2个下划线
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mToolbar.setRightText(getResources().getString(R.string.submit_ok));
		mToolbar.setRightTextClickListener(v -> {
			if (getType() == K.userInfo.DETAIL_PHONE) {
				if (!TextUtils.isEmpty(modifyText) && TextUtils.isEmpty(mText)) {
					FEToast.showMessage(getResources().getString(R.string.input_success_phone));
					return;
				}
			}
			if (mBean == null || mText.equals(mBean.content)) {
				finish();
				return;
			}
			if (mPresenter.regexText(mBean.itemType, mText)) {
				mPresenter.successModifyText(mText);
			}
		});
		mToolbar.setNavigationOnClickListener(v -> {
			notifiText();
			if (!TextUtils.equals(mText, mBean.content)) {
				showExitDialog();
			}
			else {
				finish();
			}
		});

		//普通文字修改
		mTextContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				notifiText();
				if (getType() != K.userInfo.DETAIL_LOCATION) {
					onTextChangeds(s, start, before);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				notifiText();
				isShowTextError(s);
				maxTextNums(s);
			}
		});

	}

	private void maxTextNums(Editable s) {
		String textNums;
		String chars = getString(R.string.userinfo_word_number);
		if (mBean.itemType == K.userInfo.DETAIL_LOCATION) {
			if (TextUtils.isEmpty(s)) {
				mTextMaxNum.setText(mPresenter.addressMaxNums + chars);
			}
			else {
				int nums = mPresenter.addressMaxNums - s.toString().length();
				if (nums < 0) {
					nums = 0;
				}
				textNums = nums + chars;
				mTextMaxNum.setText(textNums);
			}
		}
	}

	private void onTextChangeds(CharSequence s, int start, int before) {
		Editable editable = mTextContent.getText();
		int len = mTextContent.length();
		if (len > maxNum) {
			int selEndIndex = Selection.getSelectionEnd(editable);
			String str = editable.toString();
			//截取新字符串
			String newStr = str.substring(0, maxNum);
			mTextContent.setText(newStr);
			editable = mTextContent.getText();
			//新字符串的长度
			int newLen = editable.length();
			//旧光标位置超过字符串长度
			if (selEndIndex > newLen) {
				selEndIndex = editable.length();
			}
			//设置新光标所在的位置
			Selection.setSelection(editable, selEndIndex);
			int nums;
			if (mPresenter.getPhoneType()) {
				nums = maxNum - 2;
			}
			else {
				nums = maxNum;
			}
			String text = String.format(getString(R.string.userinfo_chat_check), mBean.title, nums);
			if (!TextUtils.isEmpty(text)) {
				mTextError.setVisibility(View.VISIBLE);
				mTextError.setText(text);
			}
		}
		else {
			if (mPresenter.getPhoneType()) {
				mTextError.setVisibility(View.GONE);
				try {
					mPresenter.getTextToPhone(s, start, before);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public EditText getContextEt() {
		return mTextContent;
	}

	@Override
	public int getType() {
		return mBean == null ? -1 : mBean.itemType;
	}

	/**
	 * 普通文本修改
	 */
	private void modifyText() {
		mTextLayout.setVisibility(View.VISIBLE);
		if (getType() == K.userInfo.DETAIL_PHONE || getType() == K.userInfo.DETAIL_TEL) {
			mTextContent.setInputType(InputType.TYPE_CLASS_PHONE);
		}
		String hint = getResources().getString(R.string.modify_text_hind);
		mTextContent.setHint(hint + mBean.title);

		mPresenter.setBeforeText(mBean.itemType, mBean.content);

		if (mBean.itemType == K.userInfo.DETAIL_BIRTHDAY) {
			mTextContent.setInputType(InputType.TYPE_NULL);
			mTextContent.setOnClickListener(v -> openDatePicket(mText));
		}
		else {
			mHandler.postDelayed(() -> {
				InputMethodManager inputManager = (InputMethodManager) mTextContent.getContext().getSystemService(INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mTextContent, 0);
			}, 360);
		}

		if (mBean.itemType == K.userInfo.DETAIL_LOCATION) {
			mTextMaxNum.setVisibility(View.VISIBLE);
			Editable editable = mTextContent.getText();
			maxTextNums(editable);
		}
		else {
			mTextMaxNum.setVisibility(View.GONE);
		}
	}

	private void notifiText() {
		if (mTextContent == null) {
			return;
		}
		mText = mTextContent.getText().toString().trim();
		if (getType() == K.userInfo.DETAIL_PHONE || getType() == K.userInfo.DETAIL_TEL) {
			modifyText = mText;
			mText = mPresenter.getPhoneToText(mText);
		}
	}

	@Override
	public boolean isSubmitText() {

		if (mBean == null) {
			return false;
		}

		if (TextUtils.isEmpty(mText) && !TextUtils.isEmpty(mBean.content)) {
			return true;
		}

		if (TextUtils.isEmpty(mText)) {
			return false;
		}

		if (mText.equals(mBean.content)) {
			return false;
		}
		else {
			return true;
		}
	}

	private void isShowTextError(Editable s) {
		if (TextUtils.isEmpty(mText) || TextUtils.isEmpty(mBean.content)) {
			return;
		}
//		if (mBean.itemType == K.userInfo.DETAIL_PHONE) {
//			if (s.length() == 12) {
//				mTextError.setText(getResources().getString(R.string.phone_repeat_modify_error));
//				if (mText.equals(mBean.content)) {
//					showTextError(true);
//				}
//				else {
//					showTextError(false);
//				}
//			}
//
//		}
	}

	private void showTextError(boolean isShow) {
		if (isShow) {
			mTextError.setVisibility(View.VISIBLE);
		}
		else {
			mTextError.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClearClick() {

	}

	@Override
	public void onOkClick(Calendar calendar, DateTimePickerDialog dateTimePickerDialog) {
		if (calendar == null) {
			return;
		}
		mTextContent.setText(DateUtil.subDatBirthday(calendar));
		dateTimePickerDialog.dismiss();
	}

	@Override
	public void onDismiss(DateTimePickerDialog dateTimePickerDialog) {

	}

	private void openDatePicket(String dateTime) {
		Calendar calendar = DateUtil.str2Calendar(dateTime);
		DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
		dateTimePickerDialog.setDateTime(calendar);
		dateTimePickerDialog.setButtonCallBack(this);
		dateTimePickerDialog.setDismissListener(this);
		dateTimePickerDialog.setMaxCalendar(Calendar.getInstance());
		dateTimePickerDialog.setTimeLevel(3);
		dateTimePickerDialog.show(getFragmentManager(), "dateTimePickerDialog");
	}

	@Override
	public void showLoading() {
		LoadingHint.show(UserInfoModifyActivity.this);
	}

	@Override
	public void hideLoading() {
		LoadingHint.hide();
	}

	@Override
	public void successModify() {
		Intent intent = new Intent();
		intent.putExtra("MODIFY_TYPE", getType());
		intent.putExtra("MODIFY_CONTENT", mText);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			notifiText();
			if (!TextUtils.equals(mText, mBean.content)) {
				showExitDialog();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
