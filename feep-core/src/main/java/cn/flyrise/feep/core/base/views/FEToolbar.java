package cn.flyrise.feep.core.base.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.R;

/**
 * @author ZYP
 * @since 2016/8/10 14:25
 */
public class FEToolbar extends Toolbar {

	private TextView mLeftTextView;
	private TextView mTitleTextView;
	private TextView mRightTextView;
	private TextView mRightTextViewLeft;
	private ImageView mRightImageView;
	private ImageView mNavigationButton;
	private ImageView mRightImageViewSearch;
	private View mLine;
	private String mTitleText;

	private Button mSubmitBtn;

	public FEToolbar(Context context) {
		this(context, null);
	}

	public FEToolbar(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FEToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		View.inflate(context, R.layout.core_toolbar_container, this);
		mLine = findViewById(R.id.toolBarLine);
		mLeftTextView = (TextView) findViewById(R.id.toolBarLeftTextView);
		mTitleTextView = (TextView) findViewById(R.id.toolBarTitleTextView);
		mRightTextView = (TextView) findViewById(R.id.toolBarRightTextView);
		mRightTextViewLeft = (TextView) findViewById(R.id.toolBarRightTextViewleft);
		mRightImageView = (ImageView) findViewById(R.id.toolBarRightImageView);
		mNavigationButton = (ImageView) findViewById(R.id.toolbarNavigation);
		mRightImageViewSearch = findViewById(R.id.toolBarRightImageView_search);
		mSubmitBtn = (Button) findViewById(R.id.btnSubmit);
	}

	@Override
	public void setNavigationIcon(@DrawableRes int resId) {
		setNavigationIcon(getResources().getDrawable(resId));
	}

	@Override
	public void setNavigationIcon(@Nullable Drawable icon) {
		mNavigationButton.setVisibility(View.VISIBLE);
		mNavigationButton.setImageDrawable(icon);
	}

	public void setNavigationVisibility(int visibility) {
		mNavigationButton.setVisibility(visibility);
	}

	@Override
	public void setNavigationOnClickListener(OnClickListener listener) {
		mNavigationButton.setOnClickListener(listener);
	}

	public void showNavigationIcon() {
		mNavigationButton.setVisibility(VISIBLE);
		mLeftTextView.setVisibility(GONE);
	}

	@Override
	public void setTitleTextColor(@ColorInt int color) {
		mTitleTextView.setTextColor(color);
		invalidate();
	}

	@Override
	public void setTitle(@StringRes int resId) {
		setTitle(getResources().getString(resId));
	}

	@Override
	public void setTitle(CharSequence title) {
		setTitleWithNavigation(title, true);
	}

	public void setTitleWithNavigation(CharSequence title, boolean withNavigation) {
		mTitleText = title.toString();
		mTitleTextView.setText(title);

		if (withNavigation) {
			Drawable navigation = getResources().getDrawable(R.mipmap.core_icon_back);
			navigation.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
			setNavigationIcon(navigation);
		}
		invalidate();
	}

	public void setTitleFakeBoldText(){
		TextPaint tp = mTitleTextView.getPaint();
		tp.setFakeBoldText(true);
	}

	@Override
	public CharSequence getTitle() {
		return mTitleText;
	}

	public TextView getToolbarTitle() {
		return mTitleTextView;
	}

	public void setLeftText(String text) {
		mLeftTextView.setText(text);
		mLeftTextView.setVisibility(View.VISIBLE);
		mNavigationButton.setVisibility(View.GONE);
		invalidate();
	}

	public void setLeftTextClickListener(OnClickListener onClickListener) {
		mLeftTextView.setOnClickListener(onClickListener);
	}

	public void showLeftText() {
		mLeftTextView.setVisibility(VISIBLE);
		mNavigationButton.setVisibility(GONE);
	}


	// *************************************** Right Image Start. *****************************************
	public void setRightImageViewSearchVisible() {
		mRightImageViewSearch.setVisibility(VISIBLE);
		mRightImageViewSearch.setColorFilter(Color.parseColor("#141414"), PorterDuff.Mode.SRC_ATOP);
		invalidate();
	}

	public void setRightIcon(@DrawableRes int resId) {
		setRightIcon(getResources().getDrawable(resId));
	}

	public void setRightIcon(Drawable drawable) {
		mRightImageView.setImageDrawable(drawable);
		mRightTextView.setVisibility(View.GONE);
		mRightImageView.setVisibility(View.VISIBLE);
		invalidate();
	}

	public void setRightButtonText(String text) {
		if (TextUtils.isEmpty(text)) {
			return;
		}
		mSubmitBtn.setText(text);
	}

	public void setRightButtonListener(OnClickListener onClickListener) {
		if (onClickListener == null || mSubmitBtn == null) {
			return;
		}
		mSubmitBtn.setOnClickListener(onClickListener);
		mRightImageView.setVisibility(GONE);
		mRightTextView.setVisibility(GONE);
		mSubmitBtn.setVisibility(VISIBLE);
	}

	public void setRightButtonEnabled(boolean isEnabled) {
		mSubmitBtn.setEnabled(isEnabled);
	}

	public void setRightIconVisibility(int visibility) {
		mRightImageView.setVisibility(visibility);
	}

	public void showRightIcon() {
		mRightImageView.setVisibility(VISIBLE);
		mRightTextView.setVisibility(GONE);
	}

	public void setRightImageClickListener(OnClickListener onClickListener) {
		mRightImageView.setOnClickListener(onClickListener);
	}

	public void setRightImageSearchClickListener(OnClickListener onClickListener) {
		mRightImageViewSearch.setOnClickListener(onClickListener);
	}

	public void setRightIconVisbility(int visibility) {
		mRightImageView.setVisibility(visibility);
		invalidate();
	}

	public void setRightText(@StringRes int resId) {
		setRightText(getResources().getString(resId));
	}

	public void setRightTextColor(int color) {
		mRightTextView.setTextColor(color);
	}

	public void setRightText(String text) {
		mRightTextView.setText(text);
		mRightTextView.setVisibility(View.VISIBLE);
		mRightImageView.setVisibility(View.GONE);
		invalidate();
	}

	public void setRightTextViewLeft(String text){
		if(!TextUtils.isEmpty(text)){
			mRightTextViewLeft.setVisibility(VISIBLE);
			mRightTextViewLeft.setText(text);
		}

	}

	public void setRightTextWithImage(String text) {
		mRightTextView.setText(text);
		mRightTextView.setVisibility(View.VISIBLE);
		invalidate();
	}

	public void setRightTextClickListener(OnClickListener onClickListener) {
		mRightTextView.setOnClickListener(onClickListener);
	}

	public void setRightTextViewLeftTextClickListener(OnClickListener onClickListener){
		mRightTextViewLeft.setOnClickListener(onClickListener);
	}

	public String getRightText() {
		return mRightTextView.getText().toString();
	}

	public TextView getRightTextView() {
		return this.mRightTextView;
	}

	public TextView getLeftTextView() {
		return this.mLeftTextView;
	}

	public void setRightTextVisbility(int visibility) {
		mRightTextView.setVisibility(visibility);
	}

	public void setRightLeftTextVisbility(int visibility) {
		mRightTextViewLeft.setVisibility(visibility);
	}

	public void setLineVisibility(int visibility) {
		mLine.setVisibility(visibility);
	}

	public void setLightMode() {
		changeStatsBarMode(Color.BLACK);
	}

	public void setDarkMode() {
		changeStatsBarMode(Color.WHITE);
	}

	private void changeStatsBarMode(int color) {
		mTitleTextView.setTextColor(color);

		if (mNavigationButton.getVisibility() == View.VISIBLE) {
			mNavigationButton.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		}

		if (mRightTextView.getVisibility() == View.VISIBLE) {
			mRightTextView.setTextColor(color);
		}

		if (mRightImageView.getVisibility() == View.VISIBLE) {
			mRightImageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		}

		invalidate();
	}

	public void setRightTextAndImage(String rightText, @DrawableRes int rightDrawable) {
		mRightTextView.setVisibility(View.VISIBLE);
		mRightImageView.setVisibility(View.VISIBLE);
		mRightTextView.setText(rightText);
		mRightImageView.setImageResource(rightDrawable);
	}

}
