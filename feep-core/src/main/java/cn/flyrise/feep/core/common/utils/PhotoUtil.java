/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-7-5 上午11:46:33
 */

package cn.flyrise.feep.core.common.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import cn.flyrise.feep.core.BuildConfig;
import cn.flyrise.feep.core.CoreZygote;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 类功能描述：图片工具（功能包括：拍照，裁剪照片，从相册选择图片）</br>
 * @author 钟永健
 */
public class PhotoUtil {

	/**
	 * 照相请求码
	 */
	public static final int TAKE_PHOTO_RESULT = 200;

	/**
	 * 裁剪图片请求码
	 */
	public static final int CUT_PICTURE_RESULT = 300;

	/**
	 * 相册请求码
	 */
	public static final int PHOTOALBUM_RESULT = 400;

	private final Context context;

	private final String basePath;

	private String photoName;

	private final Uri uri;

	/**
	 * 宽高比例
	 */
	private final int aspectX = 1;
	private final int aspectY = 1;

	private int outputX = 280;

	/**
	 * 构造PhotoUtil的同时创建图片名称、路径和Uri，所以每次拍照时最好都创建PhotoUtil
	 * @param context 上下文
	 */
	public PhotoUtil(Context context) {
		this.context = context;
		basePath = CoreZygote.getPathServices().getImageCachePath();
		final File file = new File(basePath);
		file.mkdirs();
		photoName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".png";
		uri = Uri.fromFile(new File(basePath, photoName));
	}

	/**
	 * 拍照
	 */
	public void takePhoto() {
		takePhoto(TAKE_PHOTO_RESULT);
	}

	public void takePhoto(int requestCode) {
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.setPackage(BuildConfig.APPLICATION_ID);
		((Activity) context).startActivityForResult(intent, requestCode);
	}

	/**
	 * 裁剪图片
	 */
//	public void cutPhoto() {
//
//        /*
//         * axpectX 与 aspectY为宽高比例，outputX为输出图片的宽，高通过宽来计算
//         */
//		int outputY = (outputX * aspectY) / aspectX;
//		if (outputX > 280) {
//			outputX = 280;
//			outputY = 420;
//		}
//		else if (outputY > 420) {
//			outputY = 420;
//		}
//
//        /*
//         * 至于下面这个Intent的ACTION是怎么知道的，大家可以看下自己路径下的如下网页 sdk路径/docs/reference/android/content/Intent.html 直接在里面Ctrl+F搜：CROP
//         */
//		final Intent intent = new Intent("com.android.camera.action.CROP");
//		intent.setDataAndType(uri, "image/*");
//		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
//		intent.putExtra("crop", "true");
//		// aspectX aspectY 是宽高的比例
//		intent.putExtra("aspectX", aspectX);
//		intent.putExtra("aspectY", aspectY);
//		// outputX outputY 是裁剪图片宽高
//		intent.putExtra("outputX", outputX);
//		intent.putExtra("outputY", outputY);
//		intent.putExtra("return-data", true);
//		((Activity) context).startActivityForResult(intent, CUT_PICTURE_RESULT);
//	}

	/**
	 * 将裁剪后的照片保存至文件，并返回照片的路径
	 * @param data 裁剪界面返回的intent对象
	 */
//	public String getPhotoPathAfterCut(Intent data) {
//		if (null != data && null != data.getExtras() && null != data.getExtras().getParcelable("data")) {
//			final Bitmap photo = data.getExtras().getParcelable("data");
//			File aimfile;
//			try {
//				aimfile = File.createTempFile("IMG_", ".png", new File(basePath));
//				if (!aimfile.exists()) {
//					aimfile.createNewFile();
//				}
//				BufferedOutputStream stream;
//				stream = new BufferedOutputStream(new FileOutputStream(aimfile));
//				// 将获得的截图通过流输出到文件
//				photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
//				stream.flush();
//				stream.close();
//				try {
//					new File(basePath + "/" + photoName).delete();
//					photoName = aimfile.getName();
//				} catch (final Exception e) {
//					e.printStackTrace();
//				}
//
//			} catch (final Exception e) {
//				e.printStackTrace();
//				return basePath;
//			}
//			return aimfile.toString();
//		}
//		else {
//			return basePath;
//		}
//	}

	/**
	 * 从相册中获取图片
	 */
//	public void getPhotoFromAlbum() {
//		final Intent intent = new Intent(Intent.ACTION_PICK, null);
//		/*
//         * "image/*"：数据类型-表示所有图片 还可以可以直接写如："image/jpeg 、 image/png等的类型"
//         */
//		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//		((Activity) context).startActivityForResult(intent, PHOTOALBUM_RESULT);
//	}
//
//	/**
//	 * 获取从相册来的图片的路径
//	 */
//	public String getFilePathFromAlbum(Intent data) {
//		return getImagePath(data);
//	}

//	private String getImagePath(Intent data) {
//		Uri uri = getUri(data);
//		String photo_path = null;
//		String[] proj = {MediaStore.Images.Media.DATA};
//		// 获取选中图片的路径
//		Cursor cursor = context.getContentResolver().query(uri,
//				proj, null, null, null);
//		if (cursor == null) {
//			return "";
//		}
//		if (cursor.moveToFirst()) {
//
//			int column_index = cursor
//					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//			photo_path = cursor.getString(column_index);
//			if (photo_path == null) {
//				photo_path = UriPathHelper.getPath(context.getApplicationContext(), uri);
//				Log.i("123path  Utils", photo_path);
//			}
//			Log.i("123path", photo_path);
//
//		}
//		cursor.close();
//
//		return photo_path;
//	}

	/**
	 * 解决小米手机上获取图片路径为null的情况
	 */
//	private Uri getUri(Intent intent) {
//		Uri uri = intent.getData();
//		String type = intent.getType();
//		if ("file".equals(uri.getScheme()) && (type.contains("image/"))) {
//			String path = uri.getEncodedPath();
//			if (path != null) {
//				path = Uri.decode(path);
//				ContentResolver cr = context.getContentResolver();
//				StringBuffer buff = new StringBuffer();
//				buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
//						.append("'" + path + "'").append(")");
//				Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//						new String[]{MediaStore.Images.ImageColumns._ID},
//						buff.toString(), null, null);
//				int index = 0;
//				for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
//					index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
//					index = cur.getInt(index);
//				}
//				if (index != 0) {
//					Uri uri_temp = Uri
//							.parse("content://media/external/images/media/"
//									+ index);
//					if (uri_temp != null) {
//						uri = uri_temp;
//					}
//				}
//			}
//		}
//		return uri;
//	}

	/**
	 * 获取拍照的图片的路径
	 */
	public String getPhotoPath() {
		return basePath + "/" + photoName;
	}

	public String getTakePhotoPath() {
		File path = getBitmaFile(uri);
		if (path == null) {
			return "";
		}
		return path.getPath();
	}

	private File getBitmaFile(Uri fileUri) {
		if (fileUri == null) {
			return null;
		}
		return compressBmpToFile(fileUri.getPath());
	}

	//检测图片是否旋转
	public static File compressBmpToFile(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		File outputFile = new File(path);
		if (!outputFile.isFile()) {
			return null;
		}
		String flieName = outputFile.getName();
		String savePath = CoreZygote.getPathServices().getImageCachePath();
		File file = new File(savePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		int degree = getBitmapDegree(path);//获取图片是否选择
		Bitmap bitmap = BitmapUtil.getimage(path);

		Bitmap sBitmap = rotaingImageView(degree, bitmap);

		outputFile = new File(file, flieName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(outputFile);
			sBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!sBitmap.isRecycled()) {
			sBitmap.recycle();
		}
		if (!bitmap.isRecycled()) {
			bitmap.recycle();
		}
		return outputFile;
	}

	/**
	 * 删除之前拍摄的图片，不保留在用户SD Card上
	 */
//	public static void deleteFile(String path) {
//		if (path == null || "".equals(path)) {
//			return;
//		}
//		final File file = new File(path);
//		if (file.exists()) {
//			file.delete();
//		}
//	}

	/**
	 * 从文件读取的图片，压缩内存加载，保证不会加载太大而崩溃
	 */
//	public static Bitmap decodeBitmap(String path) {
//		final BitmapFactory.Options opts = new BitmapFactory.Options();
//		opts.inJustDecodeBounds = true;// 设置成了true,不占用内存，只获取bitmap宽高
//		BitmapFactory.decodeFile(path, opts);
//		// 设置采样率,让图片变为之前的多少倍
//		opts.inSampleSize = computeSampleSize(opts, -1, 1024 * 800);
//		opts.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
//		opts.inPurgeable = true;
//		opts.inInputShareable = true;
//		opts.inDither = false;
//		opts.inTempStorage = new byte[16 * 1024];
//		FileInputStream is = null;
//		Bitmap bmp = null;
//		ByteArrayOutputStream baos = null;
//		Bitmap bmp2 = null;
//		try {
//			is = new FileInputStream(path);
//			bmp = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
//			final double scale = getScaling(opts.outWidth * opts.outHeight, 1024 * 600);
//			bmp2 = Bitmap.createScaledBitmap(bmp, (int) (opts.outWidth * scale), (int) (opts.outHeight * scale), true);
//			bmp.recycle();
//			baos = new ByteArrayOutputStream();
//			bmp2.compress(Bitmap.CompressFormat.PNG, 100, baos);
//		} catch (final IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				is.close();
//				baos.close();
//			} catch (final IOException e) {
//				e.printStackTrace();
//			}
//			System.gc();
//		}
//		return bmp2;
//	}

//	/**
//	 * 缩放比例
//	 */
//	private static double getScaling(int src, int des) {
//		/**
//		 * 48 目标尺寸÷原尺寸 sqrt开方，得出宽高百分比 49
//		 */
//		final double scale = Math.sqrt((double) des / (double) src);
//		return scale;
//	}

	/**
	 * 查看Android源码，我们得知，为了得到恰当的inSampleSize，Android提供了这种动态计算的方法。
	 */
//	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
//		final int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
//
//		int roundedSize;
//		if (initialSize <= 8) {
//			roundedSize = 1;
//			while (roundedSize < initialSize) {
//				roundedSize <<= 1;
//			}
//		}
//		else {
//			roundedSize = (initialSize + 7) / 8 * 8;
//		}
//
//		return roundedSize;
//	}

//	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
//		final double w = options.outWidth;
//		final double h = options.outHeight;
//
//		final int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
//		final int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
//
//		if (upperBound < lowerBound) {
//			return lowerBound;
//		}
//
//		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
//			return 1;
//		}
//		else if (minSideLength == -1) {
//			return lowerBound;
//		}
//		else {
//			return upperBound;
//		}
//	}
//
//	public static boolean isGifFile(String filePath) {
//		final int index = filePath.lastIndexOf(".");
//		boolean isAudioFile = false;
//		if (index != -1) {
//			final String n = filePath.substring(index, filePath.length());
//			if (".gif".equals(n)) {
//				isAudioFile = true;
//			}
//		}
//		return isAudioFile;
//	}

	/**
	 * 读取图片的旋转的角度
	 * @param path 图片绝对路径
	 * @return 图片的旋转角度
	 */
	private static int getBitmapDegree(String path) {
		int degree = 0;
		try {
			// 从指定路径下读取图片，并获取其EXIF信息
			ExifInterface exifInterface = new ExifInterface(path);
			// 获取图片的旋转信息
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

	/*
	 * 旋转图片
	 * @param angle
	 * @param bitmap
	 * @return Bitmap
	 */
	private static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		//旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		System.out.println("angle2=" + angle);
		// 创建新的图片
		return Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}
}
