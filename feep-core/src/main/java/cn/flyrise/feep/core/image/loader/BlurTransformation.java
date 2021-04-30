package cn.flyrise.feep.core.image.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.renderscript.RSRuntimeException;

import android.support.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import java.security.MessageDigest;


/**
 * @author ZYP
 * @since 2016/8/2 13:28
 */
public class BlurTransformation implements Transformation {
    private BitmapPool mBitmapPool;

    private static int MAX_RADIUS = 25;
    private static int DEFAULT_DOWN_SAMPLING = 1;

    private Context mContext;

    private int mRadius;
    private int mSampling;

    public BlurTransformation(Context context, int radius, int sampling) {
        mContext = context;
        mBitmapPool = Glide.get(context).getBitmapPool();
        mRadius = radius;
        mSampling = sampling;
    }

    @NonNull @Override
    public Resource transform(@NonNull Context context, @NonNull Resource resource, int outWidth, int outHeight) {
        Bitmap source = (Bitmap) resource.get();
        int width = source.getWidth();
        int height = source.getHeight();
        int scaledWidth = width / mSampling;
        int scaledHeight = height / mSampling;

        Bitmap bitmap = mBitmapPool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#00E5EE"));
        canvas.scale(1 / (float) mSampling, 1 / (float) mSampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                bitmap = RSBlur.blur(mContext, bitmap, mRadius);
            } catch (RSRuntimeException e) {
                bitmap = FastBlur.blur(bitmap, mRadius, true);
            }
        } else {
            bitmap = FastBlur.blur(bitmap, mRadius, true);
        }

        return BitmapResource.obtain(bitmap, mBitmapPool);
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }

//    @Override
//    public Resource transform(Resource resource, int outWidth, int outHeight) {
//
//    }

//    @Override
//    public String getId() {
//        return "BlurTransformation(radius=" + mRadius + ", sampling=" + mSampling + ")";
//    }
}
