package cn.flyrise.feep.schedule.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016-11-28 11:34
 */
public class SchedulePreferenceView extends LinearLayout {

    private ImageView mIvScheduleIcon;
    private TextView mTvScheduleLabel;
    private TextView mTvScheduleText;

    public SchedulePreferenceView(Context context) {
        this(context, null);
    }

    public SchedulePreferenceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SchedulePreferenceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_schedule_preference, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SchedulePreferenceView, defStyleAttr, 0);
        String label = typedArray.getString(R.styleable.SchedulePreferenceView_scheduleLabel);
        Drawable drawable = typedArray.getDrawable(R.styleable.SchedulePreferenceView_scheduleIcon);
        String text = typedArray.getString(R.styleable.SchedulePreferenceView_scheduleTitle);

        mIvScheduleIcon = (ImageView) findViewById(R.id.ivScheduleIcon);
        mTvScheduleLabel = (TextView) findViewById(R.id.tvSchedulePreLabel);
        mTvScheduleText = (TextView) findViewById(R.id.tvSchedulePreTitle);

        if (!TextUtils.isEmpty(label)) {
            mTvScheduleLabel.setText(label);
        }

        if (drawable != null) {
            mIvScheduleIcon.setImageDrawable(drawable);
        }

        if (!TextUtils.isEmpty(text)) {
            mTvScheduleText.setText(text);
        }

        typedArray.recycle();
    }

    public void setScheduleIcon(@DrawableRes int resId) {
        setScheduleIcon(getResources().getDrawable(resId));
    }

    public void setScheduleIcon(Drawable drawable) {
        mIvScheduleIcon.setImageDrawable(drawable);
    }

    public void setScheduleLabel(@StringRes int resId) {
        setScheduleIcon(getResources().getString(resId));
    }

    public void setScheduleIcon(String text) {
        mTvScheduleLabel.setText(text);
    }

    public void setScheduleText(@StringRes int resId) {
        setScheduleText(getResources().getString(resId));
    }

    public void setScheduleText(String title) {
        mTvScheduleText.setText(title);
    }

    public String getScheduleText() {
        return mTvScheduleText.getText().toString();
    }
}
