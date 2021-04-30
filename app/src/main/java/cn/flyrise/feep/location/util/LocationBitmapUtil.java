package cn.flyrise.feep.location.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.DrawableRes;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-15:09.
 */

public class LocationBitmapUtil {

	public static Bitmap rotateBitmap(Context context, @DrawableRes int resource) {//图片旋转180
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
		Matrix matrix = new Matrix();
		matrix.setRotate(180, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

	public static Bitmap tintBitmap(Context context, @DrawableRes int resource, int tintColor) {//修改图片颜色
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
		if (bitmap == null) {
			return null;
		}
		Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
		Canvas canvas = new Canvas(outBitmap);
		Paint paint = new Paint();
		paint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return outBitmap;
	}
}
