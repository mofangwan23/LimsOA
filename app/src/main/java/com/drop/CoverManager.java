package com.drop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;

import cn.flyrise.feep.core.common.FELog;

public class CoverManager {
    private static CoverManager mCoverManager;
    private DropCover mDropCover;
    private WindowManager mWindowManager;

    WindowManager getWindowManager() {
        return mWindowManager;
    }

    public static CoverManager getInstance() {
        if (mCoverManager == null) {
            mCoverManager = new CoverManager();
        }
        return mCoverManager;
    }

    public void init(Activity activity) {
        if (mDropCover == null) {
            mDropCover = new DropCover(activity);
        }
        mDropCover.setStatusBarHeight(getStatusBarHeight(activity));
    }

    public void start(View target, DropCover.OnDragCompeteListener onDragCompeteListener) {
        if (mDropCover == null) {
            return;
        }
        if (mDropCover.getParent() != null) {
            return;
        } else {
            mDropCover.setOnDragCompeteListener(onDragCompeteListener);
        }
        target.setVisibility(View.INVISIBLE);
        mDropCover.setTarget(drawViewToBitmap(target));
        int[] locations = new int[2];
        target.getLocationOnScreen(locations);
        attachToWindow(target.getContext());
        mDropCover.init(locations[0], locations[1]);
    }

    public void update(float x, float y) {
        mDropCover.update(x, y);
    }


    @SuppressLint("NewApi")
    public void finish(final View target, final float x, final float y) {
        target.postDelayed(() -> {
            mDropCover.finish(target, x, y);
            mDropCover.setOnDragCompeteListener(null);
        }, 30);

    }

    private Bitmap drawViewToBitmap(View view) {
        if (mDropCover == null) {
            mDropCover = new DropCover(view.getContext());
        }
        int width = view.getWidth();
        int height = view.getHeight();
        Bitmap mDest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mDest);
        view.draw(c);
        return mDest;
    }

    private void attachToWindow(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mDropCover == null) {
            mDropCover = new DropCover(context);
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mWindowManager.addView(mDropCover, params);
    }

    boolean isRunning() {
        return mDropCover != null && mDropCover.getParent() != null;
    }

    boolean isNull() {
        return mDropCover == null;
    }

    @SuppressLint("PrivateApi")
    private static int getStatusBarHeight(Activity activity) {
        Class<?> c;
        Object obj;
        Field field;
        int x, sbar = 38;

        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = activity.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

}
