package cn.flyrise.feep.schedule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @author ZYP
 * @since 2016-12-19 14:56
 */
public class NativeScheduleListView extends ListView {

    public NativeScheduleListView(Context context) {
        this(context, null);
    }

    public NativeScheduleListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NativeScheduleListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
