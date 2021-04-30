package cn.flyrise.feep.core.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import java.security.MessageDigest;

/**
 * Glide 加载圆角矩形
 */

public class GlideRoundTransformation extends BitmapTransformation {

	private static float radius = 0f;
	private static final String ID = "cn.flyrise.feep.core.image.loader.GlideRoundTransformation";
	private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

	public GlideRoundTransformation(Context context) {
		this(context, 4);
	}

	private GlideRoundTransformation(Context context, int dp) {
		radius = Resources.getSystem().getDisplayMetrics().density * dp;
	}

	@Override
	protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
		Bitmap bitmap = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
		return roundCrop(pool, bitmap);
	}

	private static Bitmap roundCrop(BitmapPool pool, Bitmap source) {
		if (source == null) return null;

		Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		if (result == null) {
			result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
		paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
		paint.setAntiAlias(true);
		RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
		canvas.drawRoundRect(rectF, radius, radius, paint);
		return result;
	}

	@Override public boolean equals(Object obj) {
		return obj instanceof GlideRoundTransformation;
	}

	@Override public int hashCode() {
		return ID.hashCode();
	}

	@Override public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
		messageDigest.update(ID_BYTES);
	}
}
