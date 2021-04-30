package cn.flyrise.feep.core.base.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import cn.flyrise.feep.core.R;

public class BadgeView extends android.support.v7.widget.AppCompatTextView {

	private boolean mHideOnNull = true;

	public BadgeView(Context context) {
		this(context, null);
	}

	public BadgeView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public BadgeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (!(getLayoutParams() instanceof LayoutParams)) {
			@SuppressLint("RtlHardcoded")
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
					Gravity.RIGHT | Gravity.TOP);
			setLayoutParams(layoutParams);
		}

		setTextColor(Color.parseColor("#FFFFFF"));
		setTextSize(12);
		setPadding(getResources().getDimensionPixelSize(R.dimen.mdp_6), getResources().getDimensionPixelSize(R.dimen.mdp_1),
				getResources().getDimensionPixelSize(R.dimen.mdp_6), getResources().getDimensionPixelSize(R.dimen.mdp_1));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setBackground(getResources().getDrawable(R.drawable.fe_badg_background));
		}
		else {
			setBackgroundDrawable(getResources().getDrawable(R.drawable.fe_badg_background));
		}

		setGravity(Gravity.CENTER);

		setHideOnNull(true);
		setBadgeCount(0);
	}

	public boolean isHideOnNull() {
		return mHideOnNull;
	}

	public void setHideOnNull(boolean hideOnNull) {
		mHideOnNull = hideOnNull;
		setText(getText());
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		setVisibility(isHideOnNull() && (text == null || "0".equalsIgnoreCase(text.toString())) ? View.GONE : View.VISIBLE);
		super.setText(text, type);
	}

	public void setBadgeCount(int count) {
		setText(String.valueOf(count));
	}
}
