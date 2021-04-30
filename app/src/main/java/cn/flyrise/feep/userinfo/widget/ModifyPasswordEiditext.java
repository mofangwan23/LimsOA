package cn.flyrise.feep.userinfo.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.HideReturnsTransformationMethod;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * 新建：陈冕;
 * 日期： 2018-3-21-10:43.
 */

public class ModifyPasswordEiditext extends LinearLayout {

    private final static String inputType = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "_";

    private TextView mTvTitle;
    private TextView mTvError;
    private EditText mEtContent;
    private CheckBox mCbxButton;

    public ModifyPasswordEiditext(Context context) {
        this(context, null);
    }

    public ModifyPasswordEiditext(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ModifyPasswordEiditext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.modify_password_view, this);
        mTvTitle = findViewById(R.id.content_title);
        mTvError = findViewById(R.id.two_modify_error);
        mEtContent = findViewById(R.id.content);
        mCbxButton = findViewById(R.id.content_showcheck);
        mCbxButton.setOnClickListener(v -> passwordContentShow());
        mEtContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mEtContent.setKeyListener(DigitsKeyListener.getInstance(inputType));
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setErrorVisibility(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void setTitle(String title) {
        if (mTvTitle == null) {
            return;
        }
        mTvTitle.setText(title);
    }

    public void setHint(String text) {
        if (mEtContent == null) {
            return;
        }
        mEtContent.setHint(text);
    }

    public String getContent() {
        if (mEtContent == null) {
            return "";
        }
        return mEtContent.getText().toString().trim();
    }

    public void setError(String error) {
        if (mTvError == null || TextUtils.isEmpty(error)) {
            return;
        }
        if (mTvError.getVisibility() == GONE) {
            setErrorVisibility(true);
        }
        mTvError.setText(error);
    }

    public void setErrorVisibility(boolean isVisibility) {
        if (mTvError == null) {
            return;
        }
        mTvError.setVisibility(isVisibility ? VISIBLE : GONE);
    }

    private void passwordContentShow() {//显示或隐藏密码
        if (mEtContent == null) {
            return;
        }
        if (mCbxButton.isChecked()) {
            mEtContent.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            mEtContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (!TextUtils.isEmpty(mEtContent.getText())) {
            mEtContent.setSelection(mEtContent.length());
        }
    }

}
