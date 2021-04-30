package cn.flyrise.android.library.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * 类功能描述：气泡框</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-6-5</br> 修改备注：</br>
 */
public class BubbleWindow extends PopupWindow {
    // private String TAG = "osean";

    private final int[] location = new int[2];

    private final Rect rect = new Rect();

    private final int screenWidth;

    private final int screenHeight;

    private boolean isOnTop;

    /**
     * 标示气泡框尚未完全消失
     */
    private boolean isVisible;

    /**
     * 标示气泡框正在exit动画中
     */
    private boolean isExiting;

    /**
     * 标示气泡框正在enter动画中
     */
    private boolean isEntering;

    private int popupY;

    private int popupX;

    private int contentViewHeight;

    private ScaleAnimation enterAnim;

    private ScaleAnimation exitAnim;

    /**
     * 箭头view的中点位置相对整个气泡框的左边的距离
     */
    // private int marginX;

    private final int animEnterTime = 200;

    private final int animExitTime = 100;

    private boolean isNeedInitLocation;

    private final Handler handler;

    private int contentViewWidth;

    /**
     * 标志气泡框是否充满屏幕的宽
     */
    private boolean isCustomWidth;

    private boolean isDismissWithoutAnima;

    public BubbleWindow(View contentView) {
        this(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    public BubbleWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }

    public BubbleWindow(View contentView, int width, int height, boolean isSubHeight) {
        super(contentView.getContext());
        setContentView(combinationView(contentView, width, height));
        setBackgroundDrawable(new BitmapDrawable());// 响应点击popupwindow外关闭菜单和返回键必须加上这句
        setOutsideTouchable(true);// 当点击菜单外时使气泡框消失
        setContentViewFocusable(true);

        /*--获取手机屏幕宽高-*/
        final DisplayMetrics dm = new DisplayMetrics();
        ((Activity) contentView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        // 取得窗口宽高
        screenWidth = dm.widthPixels;
        screenHeight = isSubHeight ? (int) (dm.heightPixels / 1.5F) : dm.heightPixels;

        measureContentView();
        pressMenuDismissAble(true);

        isCustomWidth = width > 0;
        if (width == LayoutParams.MATCH_PARENT) {
            setWidth(screenWidth);
            isCustomWidth = true;
        } else if (width == LayoutParams.WRAP_CONTENT) {
            setWidth(width);
        } else {
            width = width + 10;
            if (width <= screenWidth) {
                setWidth(width);
            } else {
                setWidth(screenWidth);
            }
            isCustomWidth = true;
        }
        if (height == LayoutParams.MATCH_PARENT) {
            setHeight(screenHeight);
        } else {
            setHeight(height);
        }

        isNeedInitLocation = true;
        handler = new Handler();
    }


    /**
     * 设置气泡框内部的View是否能响应点击事件(默认是true)
     */
    public void setContentViewFocusable(boolean isFocusable) {
        setFocusable(isFocusable);// 如果不加这个，childView不会响应ItemClick
        setTouchable(isFocusable);
    }

    /**
     * --获取气泡框内view的宽高--
     */
    private void measureContentView() {
        final int[] contentViewSqecs = measureViewSpecs(getContentView());
        contentViewWidth = contentViewSqecs[0];
        contentViewHeight = contentViewSqecs[1];
    }

    /**
     * 点击MENU键是否让气泡框消失
     *
     * @param dissmissAble true：点击菜单键消失气泡框；false:点击菜单键不消失气泡框
     */
    public void pressMenuDismissAble(boolean dissmissAble) {
        getContentView().setFocusableInTouchMode(dissmissAble);
        getContentView().setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU && isShowing()) {
                    dismiss();
                }
                return false;
            }
        });

    }

    /**
     * 拼凑view
     *
     * @param contentView 气泡框的view
     */
    private View combinationView(View contentView, int width, int height) {
        final Context context = contentView.getContext();
        final LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        contentView.setFadingEdgeLength(0);
        contentView.setBackgroundResource(R.drawable.popwindow_detail_bg);
        contentView.setPadding(PixelUtil.dipToPx(5), PixelUtil.dipToPx(5),
                PixelUtil.dipToPx(5), PixelUtil.dipToPx(5));

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final ImageView topArrow = new ImageView(context);
        topArrow.setId(R.id.top_arrow_view_id);
        topArrow.setBackgroundResource(R.drawable.bubble_arrow_upward_fe);

        final ImageView bottomArrow = new ImageView(context);
        bottomArrow.setId(R.id.bottom_arrow_view_id);
        bottomArrow.setBackgroundResource(R.drawable.bubble_arrow_downward_fe);

        layout.addView(topArrow, params);
        layout.addView(contentView, new LayoutParams(width, height));
        layout.addView(bottomArrow, params);
        return layout;
    }

    public void setNeedInitLocation(boolean isNeedInitLocation) {
        this.isNeedInitLocation = isNeedInitLocation;
    }

    /**
     * 显示气泡框
     *
     * @param parent 相对显示的view
     */
    public void show(View parent) {
        if (!isShowing()) {
            if (isNeedInitLocation) {
                update();
                getContentView().invalidate();
                measureContentView();
                parent.getLocationOnScreen(location);
                final int w = parent.getMeasuredWidth();
                final int h = parent.getMeasuredHeight();
                rect.set(location[0], location[1], location[0] + w, location[1] + h);
                measureShowLocation(rect);
                showArrow();
                initAnimation();
                isNeedInitLocation = false;
            }
            if (rect.centerX() <= screenWidth && rect.centerX() >= 0) {// 相对点不在屏幕之外
                showAtLocation(parent, Gravity.NO_GRAVITY, popupX, popupY);
                isVisible = true;
            }
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        int width = anchor.getMeasuredWidth();
        int height = anchor.getMeasuredHeight();
        anchor.getLocationOnScreen(location);
        rect.set(location[0], location[1], location[0] + width, location[1] + height);

        final int dyTop = rect.top;
        final int dyBottom = screenHeight - rect.bottom;
        isOnTop = dyTop > dyBottom;

        showArrow();
        initAnimation();

        isVisible = true;
        isNeedInitLocation = false;
        super.showAsDropDown(anchor);
    }

    /**
     * 显示箭头图片
     */
    private void showArrow() {
        final View contentView = getContentView();
        final View arrow = contentView.findViewById(isOnTop ? R.id.bottom_arrow_view_id : R.id.top_arrow_view_id);
        final View arrowUp = contentView.findViewById(R.id.bottom_arrow_view_id);
        final View arrowDown = contentView.findViewById(R.id.top_arrow_view_id);

        if (isOnTop) {
            arrowUp.setVisibility(View.VISIBLE);
            arrowDown.setVisibility(View.INVISIBLE);
        } else {
            arrowUp.setVisibility(View.INVISIBLE);
            arrowDown.setVisibility(View.VISIBLE);
        }
        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) arrow.getLayoutParams();
        final int centerX = rect.centerX();// 相对显示控件的中点x坐标
        final int centerArrow = arrow.getMeasuredWidth() / 2;// 箭头图片的宽度的1/2

        if (popupX > 0) {
            /* 计算气泡框偏了屏幕右边的距离 */
            final int offset = screenWidth - (popupX + (isCustomWidth ? getWidth() : contentViewWidth));

            if (offset < 0) {
                params.leftMargin = centerX - centerArrow - popupX - offset;
            } else {
                params.leftMargin = centerX - centerArrow - popupX;
            }
        } else {
            params.leftMargin = centerX - centerArrow;
        }
    }

    /**
     * 计算与判断气泡框显示的位置
     */
    private void measureShowLocation(Rect rect) {
        final int dyTop = rect.top;
        final int dyBottom = screenHeight - rect.bottom;
        isOnTop = dyTop > dyBottom;

        adjustShowHight();

        popupY = isOnTop ? dyTop - getHeight() : rect.bottom;
        popupX = rect.centerX() - contentViewWidth / 2;
    }

    /**
     * 调整view显示的高度
     */
    private void adjustShowHight() {
        // int r = rect.top;
        FELog.i("ContentHeight = " + contentViewHeight);
        if (isOnTop) {
            if (rect.top < contentViewHeight) {
                final int height = rect.top - PixelUtil.dipToPx(100);
                setHeight(height);
            } else {
                setHeight(contentViewHeight);
            }
        } else {
            if ((screenHeight - rect.bottom) < contentViewHeight) {
                setHeight(screenHeight - Math.abs(rect.bottom));
            }
        }
    }

    /**
     * 初始化动画
     */
    private void initAnimation() {
        // int center = rect.centerX();
        final float pivotX = ((float) rect.centerX()) / (screenWidth);
        final float pivotY = isOnTop ? 1 : 0;
        enterAnim = new ScaleAnimation(0, 1f, 0, 1f, Animation.RELATIVE_TO_PARENT, pivotX, Animation.RELATIVE_TO_PARENT, pivotY);
        enterAnim.setDuration(animEnterTime);
        exitAnim = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_PARENT, pivotX, Animation.RELATIVE_TO_PARENT, pivotY);
        exitAnim.setDuration(animExitTime);

        enterAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                isEntering = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isEntering = false;
            }
        });
    }

    /**
     * 计算控件的宽高
     *
     * @param view 需要计算的view
     * @return 宽:int[0],高:int[1]
     */
    public int[] measureViewSpecs(View view) {
        final int[] specs = new int[2];
        final int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        final int width = view.getMeasuredWidth();
        final int height = view.getMeasuredHeight();
        specs[0] = width;
        specs[1] = height;
        return specs;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (!isExiting) {
            getContentView().startAnimation(enterAnim);
            super.showAtLocation(parent, gravity, x, y);
        }
    }

    @Override
    public void dismiss() {
        if (isDismissWithoutAnima) {
            super.dismiss();
            return;
        }
        if (isVisible && !isEntering && isShowing()) {
            isExiting = true;
            getContentView().startAnimation(exitAnim);
            handler.postDelayed(dismissCallback, animExitTime);
            isVisible = false;// 已经触发了一次dismiss(),在其消失前不让dismiss()再次触发
        }
    }

    private final Runnable dismissCallback = new Runnable() {
        @Override
        public void run() {
            BubbleWindow.super.dismiss();
            handler.removeCallbacks(this);
            isExiting = false;
        }
    };

    /**
     * 无动画的退出
     */
    public void dismisWithoutAnima() {
        BubbleWindow.super.dismiss();
    }

    public void setDismissWithoutAnima(boolean isDismissWithoutAnima) {
        this.isDismissWithoutAnima = isDismissWithoutAnima;
    }

}
