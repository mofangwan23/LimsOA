package cn.flyrise.feep.commonality.util;

import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;

public class PictureCompression {
	public static Bitmap getCompressImage(String imgPath) {
		Bitmap bitmap;
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imgPath, options);
		options.inJustDecodeBounds = false;
		int width = options.outWidth;
		int height = options.outHeight;
		float w = 720;
		float h = 1280;
		int be = 1;
		if (width > height && width > w) {
			be = (int) (width / w);
		} else if (width < height && height > h) {
			be = (int) (height / h);
		}

		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(imgPath, options);
		int rotate = getBitmapDegree(imgPath);
		bitmap = rotateBitmapByDegree(bitmap,rotate);
		return bitmap;
	}

	private static int getBitmapDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
		Bitmap returnBm = null;
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		try {
			returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
		}
		if (returnBm == null) {
			returnBm = bm;
		}
		if (bm != returnBm) {
			bm.recycle();
		}
		return returnBm;
	}
}
