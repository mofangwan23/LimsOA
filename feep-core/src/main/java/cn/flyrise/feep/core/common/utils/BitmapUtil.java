package cn.flyrise.feep.core.common.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.R;

/**
 * Created by Administrator on 2017-2-21.
 */

public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    /**
     * 通过资源id转化成Bitmap
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitmapById(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /**
     * 缩放Bitmap满屏
     * @param bitmap
     * @param screenWidth
     * @param screenHight
     * @return
     */
    public static Bitmap getBitmap(Bitmap bitmap, int screenWidth,
                                   int screenHight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scale = (float) screenWidth / w;
        float scale2 = (float) screenHight / h;
        // scale = scale < scale2 ? scale : scale2;
        matrix.postScale(scale, scale);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;// Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    /**
     * 按最大边按一定大小缩放图片
     */
    public static Bitmap scaleImage(byte[] buffer, float size) {
        // 获取原图宽度
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        options.inInputShareable = true;
        Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length,
                options);
        // 计算缩放比例
        float reSize = options.outWidth / size;
        if (options.outWidth < options.outHeight) {
            reSize = options.outHeight / size;
        }
        // 如果是小图则放大
        if (reSize <= 1) {
            int newWidth = 0;
            int newHeight = 0;
            if (options.outWidth > options.outHeight) {
                newWidth = (int) size;
                newHeight = options.outHeight * (int) size / options.outWidth;
            }
            else {
                newHeight = (int) size;
                newWidth = options.outWidth * (int) size / options.outHeight;
            }
            bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            bm = scaleImage(bm, newWidth, newHeight);
            if (bm == null) {
                Log.e(TAG, "convertToThumb, decode fail:" + null);
                return null;
            }
            return bm;
        }
        // 缩放
        options.inJustDecodeBounds = false;
        options.inSampleSize = (int) reSize;
        bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);
        if (bm == null) {
            Log.e(TAG, "convertToThumb, decode fail:" + null);
            return null;
        }
        return bm;
    }

    /**
     * 检查图片是否超过一定值，是则缩小
     */
    public static Bitmap convertToThumb(byte[] buffer, float size) {
        // 获取原图宽度
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        options.inInputShareable = true;
        Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length,
                options);
        // 计算缩放比例
        float reSize = options.outWidth / size;
        if (options.outWidth > options.outHeight) {
            reSize = options.outHeight / size;
        }
        if (reSize <= 0) {
            reSize = 1;
        }
        Log.d(TAG, "convertToThumb, reSize:" + reSize);
        // 缩放
        options.inJustDecodeBounds = false;
        options.inSampleSize = (int) reSize;
        if (bm != null && !bm.isRecycled()) {
            bm.recycle();
            bm = null;
            Log.e(TAG, "convertToThumb, recyle");
        }
        bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);
        if (bm == null) {
            Log.e(TAG, "convertToThumb, decode fail:" + null);
            return null;
        }
        return bm;
    }

    /**
     * Bitmap --> byte[]
     * @param bmp
     * @return
     */
    private static byte[] readBitmap(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    /**
     * Bitmap --> byte[]
     * @return
     */
    public static byte[] readBitmapFromBuffer(byte[] buffer, float size) {
        return readBitmap(convertToThumb(buffer, size));
    }

    /**
     * 以屏幕宽度为基准，显示图片
     * @return
     */
    public static Bitmap decodeStream(Context context, Intent data, float size) {
        Bitmap image = null;
        try {
            Uri dataUri = data.getData();
            // 获取原图宽度
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPurgeable = true;
            options.inInputShareable = true;
            BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(dataUri), null, options);
            // 计算缩放比例
            float reSize = (int) (options.outWidth / size);
            if (reSize <= 0) {
                reSize = 1;
            }
            Log.d(TAG, "old-w:" + options.outWidth + ", llyt-w:" + size
                    + ", resize:" + reSize);
            // 缩放
            options.inJustDecodeBounds = false;
            options.inSampleSize = (int) reSize;
            image = BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(dataUri), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 按新的宽高缩放图片
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        if (bm != null & !bm.isRecycled()) {
            bm.recycle();
            bm = null;
        }
        return newbm;
    }

    /**
     * fuction: 设置固定的宽度，高度随之变化，使图片不会变形,高度过大固定高度
     * @param target   需要转化bitmap参数
     * @param newWidth 设置新的宽度
     * @return
     */
    public static Bitmap fitBitmap(Bitmap target, int newWidth) {
        int width = target.getWidth();
        int height = target.getHeight();
        if (height > width * 3.5) {
            height = ((int) (width * 3.5)) * 3 / 4;
        }
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        matrix.postScale(scaleWidth, scaleWidth);
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(target, 0, 0, width, height, matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (target != null && !target.equals(bmp) && !target.isRecycled()) {
//            target.recycle();
        }
        return bmp;
    }

    /**
     * 根据指定的宽度平铺图像
     * @param width
     * @param src
     * @return
     */
    public static Bitmap createRepeater(int width, Bitmap src) {
        int count = (width + src.getWidth() - 1) / src.getWidth();
        Bitmap bitmap = Bitmap.createBitmap(width, src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (int idx = 0; idx < count; ++idx) {
            canvas.drawBitmap(src, idx * src.getWidth(), 0, null);
        }
        return bitmap;
    }

    /**
     * 图片的质量压缩方法
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        if (baos != null) {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (isBm != null) {
            try {
                isBm.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (image != null && !image.isRecycled()) {
            image.recycle();
            image = null;
        }
        return bitmap;
    }

    /**
     * 图片按比例大小压缩方法(根据Bitmap图片压缩)
     * @param image
     * @return
     */
    public static Bitmap getImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        }
        else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        if (isBm != null) {
            try {
                isBm.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (image != null && !image.isRecycled()) {
            image.recycle();
            image = null;
        }
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    /**
     * 通过资源id转化成Bitmap 全屏显示
     * @param context
     * @param drawableId
     * @param screenWidth
     * @param screenHight
     * @return
     */
    public static Bitmap ReadBitmapById(Context context, int drawableId, int screenWidth, int screenHight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inInputShareable = true;
        options.inPurgeable = true;
        InputStream stream = context.getResources().openRawResource(drawableId);
        Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
        return getBitmap(bitmap, screenWidth, screenHight);
    }

    /**
     * 聊天气泡图片
     */
    public static Bitmap fillet(Bitmap showBitmap, boolean isLeft) {
        try {
            final int width = showBitmap.getWidth();
            final int height = showBitmap.getHeight();

            Paint paint = new Paint();

            Bitmap bitmapBg = null;
            if (isLeft) {
                bitmapBg = BitmapFactory.decodeResource(CoreZygote.getContext().getResources(), R.drawable.chat_adapter_to_bg_left);
            }
            else {
                bitmapBg = BitmapFactory.decodeResource(CoreZygote.getContext().getResources(), R.drawable.chat_adapter_to_bg_right);
            }
            Bitmap roundConcerImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(roundConcerImage);

            Rect rectF = new Rect(0, 0, width, height);
            paint.setAntiAlias(true);

            NinePatch patch = new NinePatch(bitmapBg, bitmapBg.getNinePatchChunk(), null);
            patch.draw(canvas, rectF);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(showBitmap, rectF, rectF, paint);

            final Bitmap bp2 = getShardImage(bitmapBg, roundConcerImage, width, height);

            return bp2;
        } catch (Exception exp) {
            return showBitmap;
        }
    }

    public static Bitmap getShardImage(Bitmap bitmap_bg, Bitmap bitmap_in, int width, int height) {

        Bitmap roundConcerImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(roundConcerImage);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        paint.setAntiAlias(true);

        NinePatch patch = new NinePatch(bitmap_bg, bitmap_bg.getNinePatchChunk(), null);
        patch.draw(canvas, rect);

        int starkWidth = 2;
        Rect rect2 = new Rect(starkWidth, starkWidth, width - starkWidth, height - starkWidth);
        canvas.drawBitmap(bitmap_in, rect, rect2, paint);
        return roundConcerImage;
    }

    public static boolean isPictureGif(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        if (!path.contains("gif")) {
            return false;
        }
        final int index = path.lastIndexOf(".");
        if (index != -1) {
            final String n = path.substring(index, path.length());
            if (".gif".equals(n)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 图片按比例大小压缩方法
     *
     * @param srcPath （根据路径获取图片并压缩）
     * @return
     */
    public static Bitmap getimage(String srcPath) {

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }
}
