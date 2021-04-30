package cn.flyrise.feep.core.watermark;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import cn.flyrise.feep.core.CoreZygote;

/**
 * @author ZYP
 * @since 2017-11-23 15:02
 */
public class WMAddressDecoration extends RecyclerView.ItemDecoration {

	private Paint mPaint;
	private String mWatermark;
	private final boolean isDrawWatermark;

	public WMAddressDecoration(String watermark) {
		this.mWatermark = watermark;
		isDrawWatermark = !TextUtils.isEmpty(watermark);
		if (isDrawWatermark) {
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			DisplayMetrics displayMetrics = CoreZygote.getContext().getResources().getDisplayMetrics();
			mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, displayMetrics));
			mPaint.setColor(Color.parseColor("#16666666"));
		}
	}

	@Override public void onDrawOver(Canvas c, RecyclerView parent, State state) {
		if (!isDrawWatermark) {
			return;
		}

		Rect textBound = new Rect();
		mPaint.getTextBounds(mWatermark, 0, mWatermark.length(), textBound);
		final int textWidth = textBound.width();

		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View childView = parent.getChildAt(i);
			int top = (childView.getBottom() - childView.getTop()) / 2 + childView.getTop();
			int index = parent.getChildAdapterPosition(childView);
			int maxWidth = childView.getMeasuredWidth();
			int currentWidth = index % 2 == 0
					? -textWidth / 3
					: textWidth / 3;
			while (currentWidth <= maxWidth) {
				c.save();
				c.rotate(-20, currentWidth + textWidth / 2, top);
				c.drawText(mWatermark, currentWidth, top + textBound.height(), mPaint);
				c.restore();
				currentWidth += textWidth * 1.5;
			}
		}
	}

	@Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
		outRect.set(0, 0, 0, 0);
	}

}
