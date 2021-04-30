package cn.flyrise.feep.knowledge.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.flyrise.feep.R;

public class PopwindowKnowLedgeUpload extends BasePopwindow {

    private RelativeLayout mRlTakePhoto;
    private RelativeLayout mRlSelectPicter;
    private RelativeLayout mRlSelectFile;
    private ImageView mIvCancel;

    public PopwindowKnowLedgeUpload(Activity activity, View viewParent, int width, int height, boolean isClippingEnabled, float alpha, PopwindowMenuClickLister listener) {
        super(activity, viewParent, width,height,isClippingEnabled,alpha,listener);
    }

    @Override
    public void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        //绑定布局
        view = inflater.inflate(R.layout.layout_popwindow_upload_file, null);
        mRlTakePhoto = view.findViewById(R.id.layout_popwindow_upload_file_rl_take_photo);
        mRlSelectPicter = view.findViewById(R.id.layout_popwindow_upload_file_rl_select_picter);
        mRlSelectFile = view.findViewById(R.id.layout_popwindow_upload_file_rl_select_file);
        mIvCancel = view.findViewById(R.id.layout_popwindow_upload_file_iv_cancel);
    }

    @Override
    public void initListener() {
        mRlTakePhoto.setOnClickListener(v -> {
            listener.setPopWindowClicklister(mRlTakePhoto);
        });
        mRlSelectPicter.setOnClickListener(v -> {
            listener.setPopWindowClicklister(mRlSelectPicter);
        });

        mRlSelectFile.setOnClickListener(v -> {
            listener.setPopWindowClicklister(mRlSelectFile);
        });

        mIvCancel.setOnClickListener(v -> {
            dismiss();
        });
    }
}
