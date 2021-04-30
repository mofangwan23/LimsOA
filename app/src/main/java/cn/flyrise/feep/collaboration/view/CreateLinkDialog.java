package cn.flyrise.feep.collaboration.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEToast;

/**
 * @author ZYP
 * @since 2017-04-26 16:01
 */
public class CreateLinkDialog extends DialogFragment {

    private EditText mEtLinkValue;
    private EditText mEtLinkName;
    private OnLinkCreateListener mLinkCreateListener;

    public void setOnLinkCreateListener(OnLinkCreateListener linkCreateListener) {
        this.mLinkCreateListener = linkCreateListener;
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View contentView = inflater.inflate(R.layout.dialog_create_link, container, false);
        bindView(contentView);
        return contentView;
    }

    private void bindView(View contentView) {
        mEtLinkName = (EditText) contentView.findViewById(R.id.etLinkName);
        mEtLinkValue = (EditText) contentView.findViewById(R.id.etLinkValue);

        contentView.findViewById(R.id.tvConfirm).setOnClickListener(view -> {
            String linkValue = mEtLinkValue.getText().toString();
            if (TextUtils.isEmpty(linkValue)) {
                FEToast.showMessage(getString(R.string.input_link));
                return;
            }

            String linkName = mEtLinkName.getText().toString();
            if (TextUtils.isEmpty(linkName)) {
                linkName = linkValue;
            }

            if (mLinkCreateListener != null) {
                mLinkCreateListener.onLinkCreate(linkName, linkValue);
            }
            dismiss();
        });

        contentView.findViewById(R.id.tvCancel).setOnClickListener(view -> dismiss());
    }

    public interface OnLinkCreateListener {
        void onLinkCreate(String linkName, String linkValue);
    }

}
