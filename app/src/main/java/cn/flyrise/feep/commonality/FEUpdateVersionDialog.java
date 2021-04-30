package cn.flyrise.feep.commonality;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.autoupdatesdk.AppUpdateInfo;

import cn.flyrise.feep.R;

/**
 * 新建：陈冕;
 * 日期： 2018-1-16-11:05.
 */

public class FEUpdateVersionDialog extends DialogFragment {

    private AppUpdateInfo info;

    private OnClickeUpdateListener mListener;

    public FEUpdateVersionDialog setAppUpdateInfo(AppUpdateInfo info) {
        this.info = info;
        return this;
    }

    public FEUpdateVersionDialog setOnClickeUpdateListener(OnClickeUpdateListener listener) {
        this.mListener = listener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View view = inflater.inflate(R.layout.update_version_dialog_layout, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.85), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void initView(View view) {
        if (info == null) {
            return;
        }
        TextView updateVersion = view.findViewById(R.id.version_text);
        EditText updateDetail = view.findViewById(R.id.update_detail);
        updateDetail.setCursorVisible(false);
        updateDetail.setFocusable(false);
        updateDetail.setFocusableInTouchMode(false);
        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            mListener.onIgnoreVersion(info.getAppVersionCode());
            dismiss();
        });
        view.findViewById(R.id.start_update).setOnClickListener(v -> {
            mListener.onUpdate(info);
            dismiss();
        });
        updateVersion.setText("V"+info.getAppVersionName());
        updateDetail.setText(Html.fromHtml(info.getAppChangeLog()));
    }

    public interface OnClickeUpdateListener {
        void onUpdate(AppUpdateInfo info);

        void onIgnoreVersion(int appVersionCode);
    }
}
