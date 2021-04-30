package cn.flyrise.feep.core.premission;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import cn.flyrise.feep.core.R;

/**
 * @author ZYP
 * @since 2016-09-20 13:09
 * <p>
 * 权限提示 Dialog。
 */
public class PermissionSettingDialog {

    private final String mMessage;
    private final Context mContext;
    private final String mPositiveText;
    private final String mNeutralText;
    private final boolean isCancelable;
    private final View.OnClickListener mPositiveListener;
    private final View.OnClickListener mNeutralListener;
    private final DialogInterface.OnCancelListener mCancelListener;

    private String mTitle;
    private AlertDialog mAlertDialog;

    private PermissionSettingDialog(Builder builder) {
        this.mTitle = builder.title;
        this.mContext = builder.context;
        this.mMessage = builder.message;
        this.mNeutralText = builder.neutralText;
        this.isCancelable = builder.isCancelable;
        this.mPositiveText = builder.positiveText;
        this.mCancelListener = builder.cancelListener;
        this.mPositiveListener = builder.positiveListener;
        this.mNeutralListener = builder.neutralListener;
    }

    public void show() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }

        View view = View.inflate(mContext, R.layout.core_dialog_permission, null);
        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = mContext.getResources().getString(R.string.permission_title);
        }
        tvTitle.setText(mTitle);

        TextView textView = (TextView) view.findViewById(R.id.tvContent);
        textView.setText(Html.fromHtml(mMessage));

        TextView tvConfirm = (TextView) view.findViewById(R.id.tvConfirm);
        if (!TextUtils.isEmpty(mPositiveText)) {
            tvConfirm.setVisibility(View.VISIBLE);
            tvConfirm.setText(mPositiveText);
            tvConfirm.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mAlertDialog.dismiss();
                    if (mPositiveListener != null) {
                        mPositiveListener.onClick(v);
                    }
                }
            });
        }

        TextView tvNeutral = (TextView) view.findViewById(R.id.tvDefault);
        if (!TextUtils.isEmpty(mNeutralText)) {
            tvNeutral.setVisibility(View.VISIBLE);
            tvNeutral.setText(mNeutralText);
            tvNeutral.setOnClickListener(v -> {
                mAlertDialog.dismiss();
                if (mNeutralListener != null) {
                    mNeutralListener.onClick(v);
                }
            });
        }

        mAlertDialog = new AlertDialog.Builder(mContext).setView(view).setCancelable(isCancelable).create();
        if (mCancelListener != null) {
            mAlertDialog.setOnCancelListener(mCancelListener);
        }
        mAlertDialog.show();
    }


    public static class Builder {
        private String title;
        private String message;
        private Context context;
        private String positiveText;
        private String neutralText;
        private boolean isCancelable = true;
        private View.OnClickListener positiveListener;
        private View.OnClickListener neutralListener;
        private DialogInterface.OnCancelListener cancelListener;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setPositiveText(String positiveText) {
            this.positiveText = positiveText;
            return this;
        }

        public Builder setNeutralText(String neutralText) {
            this.neutralText = neutralText;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.isCancelable = cancelable;
            return this;
        }

        public Builder setPositiveListener(View.OnClickListener positiveListener) {
            this.positiveListener = positiveListener;
            return this;
        }

        public Builder setNeutralListener(View.OnClickListener neutralListener) {
            this.neutralListener = neutralListener;
            return this;
        }

        public Builder setCancelListener(DialogInterface.OnCancelListener cancelListener) {
            this.cancelListener = cancelListener;
            return this;
        }

        public PermissionSettingDialog build() {
            return new PermissionSettingDialog(this);
        }
    }


}
