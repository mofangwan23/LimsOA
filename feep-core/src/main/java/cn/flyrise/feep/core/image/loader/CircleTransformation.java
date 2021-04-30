package cn.flyrise.feep.core.image.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.support.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import java.security.MessageDigest;

/**
 * @author ZYP
 * @since 2016/5/30 10:57
 */
public class CircleTransformation implements Transformation<Bitmap> {

    private String mKey;
    private BitmapPool mBitmapPool;

    public CircleTransformation(Context context, String key) {
        this(Glide.get(context).getBitmapPool(), key);
    }

    public CircleTransformation(BitmapPool pool, String key) {
        this.mBitmapPool = pool;
        this.mKey = key;
    }

//    @Override public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
//
//    }
//
//    @Override public String getId() {
//        return mKey;
//    }

    @NonNull @Override
    public Resource<Bitmap> transform(@NonNull Context context, @NonNull Resource<Bitmap> resource, int outWidth, int outHeight) {
        Bitmap source = resource.get();
        int size = Math.min(source.getWidth(), source.getHeight());

        int width = (source.getWidth() - size) / 2;
        int height = (source.getHeight() - size) / 2;

        Bitmap bitmap = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader =
                new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        if (width != 0 || height != 0) {
            // source isn't square, move viewport to center
            Matrix matrix = new Matrix();
            matrix.setTranslate(-width, -height);
            shader.setLocalMatrix(matrix);
        }
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        return BitmapResource.obtain(bitmap, mBitmapPool);
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }
}

//public class CircleTransformation implements Transformation {
//
//    private String mKey;
//
//    public CircleTransformation(String key) {
//        this.mKey = key;
//    }
//
//    @Override public Bitmap transform(Bitmap source) {
//        int size = Math.min(source.getWidth(), source.getHeight());
//        Bitmap target = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
//
//        Canvas canvas = new Canvas(target);
//        Paint paint = new Paint();
//        BitmapShader bitmapShader = new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
//        paint.setShader(bitmapShader);
//        paint.setAntiAlias(true);
//
//        float radius = size / 2.0F;
//        canvas.drawCircle(radius, radius, radius, paint);
//        source.recycle();
//        return target;
//    }
//
//    @Override public String key() {
//        return mKey;
//    }
//}