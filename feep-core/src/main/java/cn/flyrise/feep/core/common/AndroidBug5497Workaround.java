package cn.flyrise.feep.core.common;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import cn.flyrise.feep.core.common.utils.DevicesUtil;

/**
 * @author ZYP
 * @since 2016/7/8 08:45
 * <p/>
 * 自 Android 2.1 以来，全屏或透明状态栏 windowSoftInputMode="adjustResize" 失效的解决方案。
 * 至今该 bug 未修复。
 */
public class AndroidBug5497Workaround {

    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    public static void assistActivity(Activity activity) {
        new AndroidBug5497Workaround(activity);
    }

    private View mChildOfContent;
    private int mUsableHeightPrevious;
    private FrameLayout.LayoutParams mFrameLayoutParams;
    private int mNavigationBarHeight;

    private AndroidBug5497Workaround(Activity activity) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mNavigationBarHeight = DevicesUtil.getNavigationBarSize(activity).y;
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
        mFrameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != mUsableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible
                mFrameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            }
            else {
                // keyboard probably just became hidden
                int navigationBarHeight = computeNavigationBarHeight();
                mFrameLayoutParams.height = usableHeightSansKeyboard - navigationBarHeight;
            }
            mChildOfContent.requestLayout();
            mUsableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return r.bottom;
    }

    private int computeNavigationBarHeight() {
        if (mNavigationBarHeight == 0) {
            return 0;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                && TextUtils.equals(Build.BRAND.toLowerCase(), "huawei")) {
            return 0;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                && Build.MODEL.toLowerCase().contains("eben")) {
            return 0;
        }

        return mNavigationBarHeight;
    }
}
