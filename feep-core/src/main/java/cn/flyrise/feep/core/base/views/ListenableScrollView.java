package cn.flyrise.feep.core.base.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-10-28 14:38
 */
public class ListenableScrollView extends ScrollView {

    private List<OnScrollChangeListener> mListeners = new ArrayList<>();

    public ListenableScrollView(Context context) {
        this(context, null);
    }

    public ListenableScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListenableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        for(OnScrollChangeListener listener : mListeners) {
            listener.onScrollChange(t, oldt);
        }
    }

    public void addOnScrollChangeListener(OnScrollChangeListener listener) {
        if(listener != null) {
            mListeners.add(listener);
        }
    }

    public interface OnScrollChangeListener {
        void onScrollChange(int scrollY, int lastScrollY);
    }
}
