package cn.flyrise.feep.location.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.flyrise.feep.R;
import cn.flyrise.feep.location.adapter.NewLocationRecylerAdapter;
import cn.flyrise.feep.location.bean.SignPoiItem;

/**
 * 新建：陈冕;
 * 日期： 2017-11-1-18:18.
 * 签到成功盖章
 */

public class LocationSignTagDecoration extends RecyclerView.ItemDecoration {
    private int tagWidth;
    private Paint leftPaint;
    private Bitmap mBitmap;
    private Rect mBitmapRect;

    public LocationSignTagDecoration(Context context) {
        leftPaint = new Paint();
        tagWidth = 50;
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.location_sign_clocksuccess);
        mBitmapRect = new Rect(0, 0, mBitmap.getWidth() + tagWidth, mBitmap.getHeight() + tagWidth);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);
        int cound = parent.getChildCount();
        View child;
        int position;
        SignPoiItem item;
        for (int i = 0; i < cound; i++) {
            child = parent.getChildAt(i);
            position = parent.getChildAdapterPosition(child);
            item = ((NewLocationRecylerAdapter) parent.getAdapter()).getItem(position);
            if (item != null && item.isSignSuccess) {
                int right = child.getRight() - tagWidth / 2;
                int left = right - mBitmap.getWidth() - tagWidth;

                int chilTop = child.getTop();
                int chilBottom = child.getBottom();

                int itemHeight = chilBottom - chilTop;

                int bottom = 0;
                int top = 0;
                if ((itemHeight - mBitmap.getHeight() - tagWidth) > 0) {//item大于图片时，居中
                    top = (chilTop + itemHeight / 2) - (mBitmap.getHeight() / 2 + tagWidth / 2);
                    bottom = (chilTop + itemHeight / 2) + (mBitmap.getHeight() / 2 + tagWidth / 2);
                } else {
                    if (position == 0) { //第一个数
                        top = chilTop;
                        bottom = chilTop + mBitmap.getHeight() + tagWidth;
                    } else if (position == parent.getAdapter().getItemCount() - 1) {//最后一个时
                        bottom = chilBottom;
                        top = chilBottom - mBitmap.getHeight() - tagWidth;
                    } else {
                        bottom = chilBottom + tagWidth / 2;
                        top = chilBottom - mBitmap.getHeight() - tagWidth / 2;
                    }
                }
                Rect locationRect = new Rect(left, top, right, bottom);
                canvas.drawBitmap(mBitmap, mBitmapRect, locationRect, leftPaint);
            }
        }
    }

    public void onDestroy() {
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }
}