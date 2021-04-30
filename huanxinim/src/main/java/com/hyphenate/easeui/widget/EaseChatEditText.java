package com.hyphenate.easeui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by klc on 2017/11/1.
 */

public class EaseChatEditText extends EditText {

	private OnMenuItemClick onMenuItemClick;

	public EaseChatEditText(Context context) {
		super(context);
	}

	public EaseChatEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnMenuItemClick(OnMenuItemClick onMenuItemClick) {
		this.onMenuItemClick = onMenuItemClick;
	}

	@Override
	public boolean onTextContextMenuItem(int id) {
		boolean result = super.onTextContextMenuItem(id);
		if (id == android.R.id.paste) {
			if (onMenuItemClick != null) {
				onMenuItemClick.onPaste();
			}
		}
		return result;
	}

	interface OnMenuItemClick {
		void onPaste();
	}
}
