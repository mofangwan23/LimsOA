package com.google.android.apps.brushes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;

public interface CanvasLite {
    void drawRect(float l, float t, float r, float b, Paint paint);
    void drawCircle(float x, float y, float r, Paint paint);
    void drawColor(int color, PorterDuff.Mode mode);
    void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint);
    void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint);

    void drawTo(Canvas drawCanvas, float left, float top, Paint paint, boolean dirtyOnly);
    Bitmap toBitmap();
    void recycleBitmaps();
    int getWidth();
    int getHeight();
}
