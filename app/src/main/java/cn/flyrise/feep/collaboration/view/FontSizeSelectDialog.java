package cn.flyrise.feep.collaboration.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * Created by yuepeng on 2017/5/11.
 */
public class FontSizeSelectDialog extends DialogFragment {

    public static final int FONT_SIZE_SMALL = 1;
    public static final int FONT_SIZE_DEFAULT = 2;
    public static final int FONT_SIZE_BIG = 3;

    private TextView mBigTextView;
    private TextView mMiddleTextView;
    private TextView mSmallTextView;

    private ImageView mIvBigCheck;
    private ImageView mIvMiddleCheck;
    private ImageView mIvSmallCheck;

    private int mDefaultFontSize;
    private OnFontSizeSelectedListener mListener;

    public void setOnFontSizeSelectedListener(OnFontSizeSelectedListener listener) {
        this.mListener = listener;
    }

    public void setDefaultSize(int defaultSize) {
        this.mDefaultFontSize = defaultSize;
        if (mDefaultFontSize == 0 || mDefaultFontSize == -1) {
            mDefaultFontSize = FONT_SIZE_DEFAULT;
        }
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View contentView = inflater.inflate(R.layout.dialog_font_size, container, false);
        bindView(contentView);
        return contentView;
    }

    private void bindView(View contentView) {
        mBigTextView = (TextView) contentView.findViewById(R.id.tvBig);
        mMiddleTextView = (TextView) contentView.findViewById(R.id.tvMiddle);
        mSmallTextView = (TextView) contentView.findViewById(R.id.tvSmall);

        mIvBigCheck = (ImageView) contentView.findViewById(R.id.ivBid);
        mIvMiddleCheck = (ImageView) contentView.findViewById(R.id.ivMiddle);
        mIvSmallCheck = (ImageView) contentView.findViewById(R.id.ivSmall);

        mBigTextView.setTextColor(mDefaultFontSize == FONT_SIZE_BIG
                ? getResources().getColor(R.color.login_btn_defulit) : Color.BLACK);
        mIvBigCheck.setVisibility(mDefaultFontSize == FONT_SIZE_BIG ? View.VISIBLE : View.GONE);

        mMiddleTextView.setTextColor(mDefaultFontSize == FONT_SIZE_DEFAULT
                ? getResources().getColor(R.color.login_btn_defulit) : Color.BLACK);
        mIvMiddleCheck.setVisibility(mDefaultFontSize == FONT_SIZE_DEFAULT ? View.VISIBLE : View.GONE);

        mSmallTextView.setTextColor(mDefaultFontSize == FONT_SIZE_SMALL
                ? getResources().getColor(R.color.login_btn_defulit) : Color.BLACK);
        mIvSmallCheck.setVisibility(mDefaultFontSize == FONT_SIZE_SMALL ? View.VISIBLE : View.GONE);

        contentView.findViewById(R.id.layoutBig).setOnClickListener(view -> onFontSizeSelected(FONT_SIZE_BIG));
        contentView.findViewById(R.id.layoutMiddle).setOnClickListener(view -> onFontSizeSelected(FONT_SIZE_DEFAULT));
        contentView.findViewById(R.id.layoutSmall).setOnClickListener(view -> onFontSizeSelected(FONT_SIZE_SMALL));
    }

    private void onFontSizeSelected(int fontSize) {
        if (mListener != null) {
            mListener.onFontSizeSelected(fontSize);
        }
        dismiss();
    }

    public interface OnFontSizeSelectedListener {
        void onFontSizeSelected(int fontSize);
    }

}
