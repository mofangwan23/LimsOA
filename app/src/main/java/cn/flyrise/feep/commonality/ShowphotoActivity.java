//
// ShowphotoActivity.java
// feep
//
// Created by Administrator on 2012-1-9.
// Copyright 2012 flyrise. All rights reserved.
//

package cn.flyrise.feep.commonality;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import java.io.File;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEStatusBar;

/**
 * 类功能描述：图片查看器</br>
 *
 * @version 1.0</br> 修改时间：2012-7-18</br> 修改备注：</br>
 */
public class ShowphotoActivity extends BaseActivity {
    // 加入图片放大缩小代码
    private final Matrix matrix = new Matrix();

    private final Matrix savedMatrix = new Matrix();

    private DisplayMetrics dm;

    private Bitmap bitmap;

    String downMessage = " ";

    private float minScaleR;                    // 最小缩放比例

    private static final float MAX_SCALE = 8f;           // 最大缩放比例

    private static final int NONE = 0;            // 初始状态

    private static final int DRAG = 1;            // 拖动

    private static final int ZOOM = 2;            // 缩放

    private int mode = NONE;

    private final PointF prev = new PointF();

    private final PointF mid = new PointF();

    private float dist = 1f;

    // 根据这个时间来判断是否需要更新界面（产生双击事件200毫秒内部更新）
    private long doubleclicked = 0;

    // 判断是否双击
    private long firstClick = 0;

    // 加入完毕
    private String path = null;

    private ImageView imageview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置堆内存超过初始值的70%时重新分配
        // VMRuntime.getRuntime().setMinimumHeapSize(16 * 1024 * 1024);
        // VMRuntime.getRuntime().setTargetHeapUtilization(0.7f);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.collaboration_show_photo);

        final Intent intent = this.getIntent();
        if (intent != null) {
            final Uri data = intent.getData();
            final String type = intent.getType();
            if (data != null) {
                path = data.getPath();
            }
            if (!"image/*".equals(type)) {
                FEToast.showMessage(getResources().getString(R.string.check_attachment_no_format));
                return;

            }
        }
        imageview = (ImageView) this.findViewById(R.id.takephoto_show);
        imageview.setOnTouchListener(new Ontouchlistener());
        imageview.setScaleType(ImageView.ScaleType.MATRIX);
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);// 获取分辨率

        final File isLocal = new File(path);
        if (isLocal.exists()) {
            try {
                bitmap = BitmapFactory.decodeFile(path);
            } catch (final OutOfMemoryError err) {
                try {
                    final BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 4;
                    bitmap = BitmapFactory.decodeFile(path, opts);
                } catch (final OutOfMemoryError errf) {
                    final BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 8;
                    try {
                        bitmap = BitmapFactory.decodeFile(path, opts);
                    } catch (final OutOfMemoryError errff) {
                        FEToast.showMessage(this.getString(R.string.collaboration_out_of_memory));
                    }
                }
            }
            imageview.setImageBitmap(bitmap);// 填充控件
            minZoom();
            center();
            imageview.setImageMatrix(matrix);
        }
    }

    @Override protected boolean optionStatusBar() {
        return FEStatusBar.setLightStatusBar(this);
    }

    // 加入图片放大缩小代码
    private class Ontouchlistener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 主点按下
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    prev.set(event.getX(), event.getY());
                    mode = DRAG;

                    if (System.currentTimeMillis() - firstClick > 200) {
                        firstClick = System.currentTimeMillis();
                    }
                    else {
                        doubleclicked = System.currentTimeMillis();
                        matrix.reset();
                        minZoom();
                        center();
                        imageview.setImageMatrix(matrix);
                    }
                    break;
                // 副点按下
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (System.currentTimeMillis() - doubleclicked > 200) {
                        dist = spacing(event);
                        // 如果连续两点距离大于10，则判定为多点模式
                        if (spacing(event) > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    if (System.currentTimeMillis() - doubleclicked > 200) {
                        mode = NONE;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (System.currentTimeMillis() - doubleclicked > 200) {
                        if (mode == DRAG) {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - prev.x, event.getY() - prev.y);
                        }
                        else if (mode == ZOOM) {
                            final float newDist = spacing(event);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                final float tScale = newDist / dist;
                                matrix.postScale(tScale, tScale, mid.x, mid.y);
                            }
                        }
                    }
                    break;
            }
            if (System.currentTimeMillis() - doubleclicked > 200) {
                CheckView();
                imageview.setImageMatrix(matrix);
            }
            return true;
        }
    }

    /**
     * 限制最大最小缩放比例，自动居中
     */
    private void CheckView() {
	    final float[] p = new float[9];
        matrix.getValues(p);
        if (mode == ZOOM) {
            if (p[0] < minScaleR) {
                matrix.setScale(minScaleR, minScaleR);
            }
            if (p[0] > MAX_SCALE) {
                matrix.set(savedMatrix);
            }
        }
        center();
    }

    /**
     * 最小缩放比例，最大为100%
     */
    private void minZoom() {
        if (dm == null || bitmap == null) {
            return;
        }
        minScaleR = Math.min((float) dm.widthPixels / (float) bitmap.getWidth(), (float) dm.heightPixels / (float) bitmap.getHeight());
        if (minScaleR < 2.0) {
            matrix.postScale(minScaleR, minScaleR);
        }
    }

    /**
     * 横向、纵向居中
     */
    private void center() {
        if (bitmap != null) {
            final Matrix m = new Matrix();
            m.set(matrix);
            final RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            m.mapRect(rect);

            final float height = rect.height();
            final float width = rect.width();
            float deltaX = 0, deltaY = 0;

            // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移
            final int screenHeight = dm.heightPixels;
            if (height < screenHeight) {
                deltaY = (screenHeight - height) / 2 - rect.top;
            }
            else if (rect.top > 0) {
                deltaY = -rect.top;
            }
            else if (rect.bottom < screenHeight) {
                deltaY = imageview.getHeight() - rect.bottom;
            }
            final int screenWidth = dm.widthPixels;
            if (width < screenWidth) {
                deltaX = (screenWidth - width) / 2 - rect.left;
            }
            else if (rect.left > 0) {
                deltaX = -rect.left;
            }
            else if (rect.right < screenWidth) {
                deltaX = screenWidth - rect.right;
            }
            matrix.postTranslate(deltaX, deltaY);
        }
    }

    /**
     * 两点的距离
     */
    private float spacing(MotionEvent event) {
        final float x = event.getX(0) - event.getX(1);
        final float y = event.getY(0) - event.getY(1);
        return (float) (Math.sqrt(x * x + y * y + 0d));
    }

    /**
     * 两点的中点
     */
    private void midPoint(PointF point, MotionEvent event) {
        final float x = event.getX(0) + event.getX(1);
        final float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
