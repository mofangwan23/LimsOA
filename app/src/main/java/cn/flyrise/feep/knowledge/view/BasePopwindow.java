package cn.flyrise.feep.knowledge.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drag.framework.DragGridView;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.UIUtil;

public abstract class BasePopwindow extends PopupWindow {

    public Activity activity;
    private Context context;
    public View view;
    public View viewParent;
    public PopwindowMenuClickLister listener;
    public int width;
    public int height;
    private float alpha;

    public BasePopwindow(Context context){
        this.context = context;
    }

    public BasePopwindow(Activity activity, View viewParent, int width, int height, PopwindowMenuClickLister listener){
        super(activity);
        this.activity = activity;
        this.listener = listener;
        this.viewParent = viewParent;
        this.width = width;
        this.height = height;
        initView(activity);
        setPopupWindow(false);
        initListener();
    }
    public BasePopwindow(Activity activity, View viewParent, int width, int height, boolean isClippingEnabled, float alpha,PopwindowMenuClickLister listener){
        super(activity);
        this.activity = activity;
        this.listener = listener;
        this.viewParent = viewParent;
        this.width = width;
        this.height = height;
        this.alpha = alpha;
        initView(activity);
        setPopupWindow(isClippingEnabled);
        initListener();
    }

    public abstract void initView(Context context);

    public abstract void initListener();

    public void setPopupWindow(boolean isClippingEnabled) {
        if(isClippingEnabled){
            this.setClippingEnabled(false);
        }
        this.setContentView(view);// 设置View
        this.setWidth(width);// 设置弹出窗口的宽
        this.setHeight(height);// 设置弹出窗口的高
        this.setFocusable(true);// 设置弹出窗口可
        this.setBackgroundDrawable(new ColorDrawable(0xffffff));
        // 产生背景变暗效果
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        if(alpha == 0){
            alpha = 0.6f;
        }
        lp.alpha = alpha;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        activity.getWindow().setAttributes(lp);
        view.setOnTouchListener(new View.OnTouchListener() {// 如果触摸位置在窗口外面则销毁

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = view.getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        WindowManager.LayoutParams lp = activity.getWindow()
                .getAttributes();
        lp.alpha = 1f;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        activity.getWindow().setAttributes(lp);

    }


    public void showPopwindow(int gravity){
        showAtLocation(viewParent, gravity, 0, 0);
    }

    public interface PopwindowMenuClickLister{
        void setPopWindowClicklister(View view);
    }
}
