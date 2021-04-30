package com.hyphenate.chatui.utils.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.List;

public class WechatLayoutManager {

	private Paint mLinePaint;

	WechatLayoutManager() {
		mLinePaint = new Paint();
		mLinePaint.setColor(Color.WHITE);
	}

	public Bitmap combineBitmap(int size, int subSize, int gap, int gapColor, List<Bitmap> bitmaps) {
		Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawColor(gapColor == 0 ? Color.WHITE : gapColor);
		int count = bitmaps.size();
		Bitmap subBitmap;

		for (int i = 0; i < count; i++) {
			if (bitmaps.get(i) == null) continue;
			subBitmap = Bitmap.createScaledBitmap(bitmaps.get(i), subSize, subSize, true);
			float x = 0;
			float y = 0;
			if (count == 2) {
				x = i * (subSize + gap);
				y = (size - subSize) / 2.0f;
				if (i == 1) canvasLine(canvas, subSize, y, subSize + gap, y + subSize);
			}
			else if (count == 3) {
				if (i == 0) x = (size - subSize) / 2.0f;
				else {
					x = (i - 1) * (subSize + gap);
					y = subSize + gap;
				}
				if (i == 2) {
					canvasLine(canvas, 0, subSize, size, subSize + gap);
					canvasLine(canvas, subSize, subSize + gap, subSize + gap, size);
				}
			}
			else if (count == 4) {
				x = (i % 2) * (subSize + gap);
				if (i >= 2) y = subSize + gap;
				if (i == 3) {
					canvasLine(canvas, 0, subSize, size, subSize + gap);
					canvasLine(canvas, subSize, 0, subSize + gap, size);
				}
			}
			canvas.drawBitmap(subBitmap, x, y, null);
		}
		return result;
	}

	private void canvasLine(Canvas canvas, float left, float top, float right, float bottom) {
		RectF mLineRectF = new RectF(left, top, right, bottom);
		canvas.drawRect(mLineRectF, mLinePaint);
	}
}
