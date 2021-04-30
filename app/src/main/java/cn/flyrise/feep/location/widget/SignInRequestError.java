package cn.flyrise.feep.location.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;

/**
 * 新建：陈冕;
 * 日期： 2018-5-19-10:33.
 */

public class SignInRequestError extends RelativeLayout {

	private TextView mTvTitle;
	private ImageView mImgIcon;

	public SignInRequestError(Context context) {
		this(context, null);
	}

	public SignInRequestError(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SignInRequestError(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.location_sign_list_error, this);
		mTvTitle = findViewById(R.id.emptyView);
		mImgIcon = findViewById(R.id.empty_icon);
	}

	public void setTitle(String text) {
		if (mTvTitle != null) mTvTitle.setText(text);
	}

	public void setEmptyIcon(int resouts) {
		if (mImgIcon != null) mImgIcon.setImageResource(resouts);
	}
}
