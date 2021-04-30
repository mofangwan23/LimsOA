package cn.flyrise.feep.core.base.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * @author ZYP
 * @since 2017-03-01 09:49
 */
public class HomeKeyWatchDog {
    private final Context mContext;
    private final IntentFilter mFilter;
    private OnHomePressedListener mListener;
    private InnerReceiver mReceiver;

    // 回调接口
    public interface OnHomePressedListener {
        void onHomePressed();

        void onHomeLongPressed();
    }

    public HomeKeyWatchDog(Context context) {
        mContext = context;
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }

    /**
     * 设置监听
     * @param listener
     */
    public void setOnHomePressedListener(OnHomePressedListener listener) {
        mListener = listener;
        mReceiver = new InnerReceiver();
    }

    /**
     * 开始监听，注册广播
     */
    public void startWatch() {
        if (mReceiver != null) {
            try {
                mContext.registerReceiver(mReceiver, mFilter);
            } catch (final Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 停止监听，注销广播
     */
    public void stopWatch() {
        if (mReceiver != null) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 广播接收者
     */
    private class InnerReceiver extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                final String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (mListener != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {// 短按home键
                            mListener.onHomePressed();
                        }
                        else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {// 长按home键
                            mListener.onHomeLongPressed();
                        }
                    }
                }
            }
        }
    }
}
