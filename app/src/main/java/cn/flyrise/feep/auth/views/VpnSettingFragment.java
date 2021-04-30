package cn.flyrise.feep.auth.views;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.util.SystemManagerUtils;

/**
 * @author ZYP
 * @since 2016-10-18 14:41
 */
public class VpnSettingFragment extends DialogFragment {

    private EditText mEtVpnAddress;
    private EditText mEtVpnPort;
    private EditText mEtUsername;
    private EditText mEtPassword;

    private String mVpnAddress;
    private String mVpnPort;
    private String mVpnUsername;
    private String mVpnPassword;

    private OnCompleteListener mCompletedListener;
    private OnCancelListener mCancelListener;

    private boolean isSuccess = false;

    public static VpnSettingFragment newInstance() {
        return new VpnSettingFragment();
    }

    public VpnSettingFragment setVpnAddress(String vpnAddress) {
        this.mVpnAddress = vpnAddress;
        return this;
    }

    public VpnSettingFragment setVpnPort(String port) {
        this.mVpnPort = port;
        return this;
    }

    public VpnSettingFragment setVpnUsername(String username) {
        this.mVpnUsername = username;
        return this;
    }

    public VpnSettingFragment setVpnPassword(String password) {
        this.mVpnPassword = password;
        return this;
    }

    public VpnSettingFragment setOnCompletedListener(OnCompleteListener listener) {
        this.mCompletedListener = listener;
        return this;
    }

    public VpnSettingFragment setOnCancelListener(OnCancelListener listener) {
        this.mCancelListener = listener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return initViews(inflater);
    }

    private View initViews(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_vpn_dialog, null);
        mEtVpnAddress = (EditText) view.findViewById(R.id.etVPNHost);
        mEtVpnPort = (EditText) view.findViewById(R.id.etVPNPort);
        mEtUsername = (EditText) view.findViewById(R.id.etVPNAccount);
        mEtPassword = (EditText) view.findViewById(R.id.etVPNPassword);

        mEtVpnAddress.setText(mVpnAddress);
        mEtVpnPort.setText(mVpnPort);
        mEtUsername.setText(mVpnUsername);
        mEtPassword.setText(mVpnPassword);

        view.findViewById(R.id.tvConfirm).setOnClickListener(v -> {
            String address = mEtVpnAddress.getText().toString().trim();
            String port = mEtVpnPort.getText().toString().trim();
            String username = mEtUsername.getText().toString().trim();
            String password = mEtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(address)
                    && TextUtils.isEmpty(port)
                    && TextUtils.isEmpty(username)
                    && TextUtils.isEmpty(password)) {
                dismiss();
                return;
            }

            if (mCompletedListener != null) {
                isSuccess = true;
                mCompletedListener.onCompleted(address, port, username, password);
            }
            dismiss();
        });

        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dismiss());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (TextUtils.isEmpty(mVpnAddress)) {
                return;
            }
            mEtVpnAddress.requestFocus();
            String text = mEtVpnAddress.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                mEtVpnAddress.setSelection(text.length());
            }
            SystemManagerUtils.shwoInputMethod(mEtVpnAddress);
        }, 200);
        return view;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!isSuccess) {
            if (mCancelListener != null) {
                mCancelListener.onCancel();
            }
        }
    }

    public interface OnCompleteListener {
        void onCompleted(String address, String port, String username, String password);
    }

    public interface OnCancelListener {
        void onCancel();
    }
}
