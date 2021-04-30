package cn.flyrise.feep.core.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cn.flyrise.feep.core.R;

/**
 * @author ZYP
 * @since 2016-10-18 11:54
 */
public class FELoadingDialog {
    private Dialog mLoadingDialog;
    private TextView mLoadingText;
    private Builder mBuilder;

    private FELoadingDialog(Builder builder) {
        this.mBuilder = builder;
        View customView = LayoutInflater.from(builder.context).inflate(R.layout.core_view_loading_dialog, null);
        mLoadingText = (TextView) customView.findViewById(R.id.tvLoadingLabel);
        if (!TextUtils.isEmpty(builder.loadingLabel)) {
            mLoadingText.setText(builder.loadingLabel);
            mLoadingText.setVisibility(View.VISIBLE);
        }

        mLoadingDialog = new Dialog(builder.context, R.style.TransparentDialogStyle);
        mLoadingDialog.setContentView(customView);
        mLoadingDialog.setCancelable(builder.cancelable);
        mLoadingDialog.setOnKeyListener((dialog, keyCode, event) -> {
            if (mBuilder == null) {
                return false;
            }
            return mBuilder.keyListener != null && mBuilder.keyListener.onKeyDown(keyCode, event);
        });
        mLoadingDialog.setOnCancelListener(dialog -> {
            if (mBuilder != null && mBuilder.dismissListener != null) {
                mBuilder.dismissListener.onDismiss();
            }
        });
    }

    public boolean isShowing() {
        return mLoadingDialog != null && mLoadingDialog.isShowing();
    }

    public void show() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            return;
        }
        mLoadingDialog.show();
    }

    public void hide() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        mBuilder = null;
        mLoadingText = null;
        mLoadingDialog = null;
    }

    public void updateProgress(int progress) {
        if (mLoadingText.getVisibility() == View.GONE) {
            mLoadingText.setVisibility(View.VISIBLE);
        }
        mLoadingText.setText(mLoadingDialog.getContext().getResources().getString(R.string.core_downloading) + ":" + progress + "%");
    }

    public void setOnDismissListener(OnDismissListener dismissListener) {
        if (mLoadingDialog != null) {
            mLoadingDialog.setOnCancelListener(dialog -> {
                if (dismissListener != null) dismissListener.onDismiss();
            });
        }
    }

    public void removeDismissListener() {
        mLoadingDialog.setOnDismissListener(null);
    }

    public void setCancelable(boolean flag) {
        mLoadingDialog.setCancelable(flag);
    }

    public static class Builder {
        private Context context;
        private boolean cancelable = true;
        private OnDismissListener dismissListener;
        private OnKeyListener keyListener;
        private String loadingLabel;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setLoadingLabel(String loadingLabel) {
            this.loadingLabel = loadingLabel;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener dismissListener) {
            this.dismissListener = dismissListener;
            return this;
        }

        public Builder setOnKeyListener(OnKeyListener keyListener) {
            this.keyListener = keyListener;
            return this;
        }

        public FELoadingDialog create() {
            return new FELoadingDialog(this);
        }
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    interface OnKeyListener {
        boolean onKeyDown(int keyCode, KeyEvent event);
    }

}
