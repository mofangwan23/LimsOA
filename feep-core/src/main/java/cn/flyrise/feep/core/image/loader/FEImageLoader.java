package cn.flyrise.feep.core.image.loader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import java.io.File;

/**
 * @author ZYP
 * @since 2016/5/30 10:26
 */
public class FEImageLoader {

	public static void load(View view, Drawable drawable) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackground(drawable);
		}
		else {
			view.setBackgroundDrawable(drawable);
		}
	}

	public static void load(Context context, ImageView target, int resId) {
		if (context==null||(context instanceof Activity && (((Activity) context).isFinishing()))) return;
		Glide.with(context).load(resId).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)).into(target);
	}

	public static void load(Context context, ImageView target, int resId, int error) {
		if (context==null||(context instanceof Activity && (((Activity) context).isFinishing()))) return;
		Glide.with(context).load(resId).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).error(error)).into(target);
	}

	public static void load(Context context, ImageView target, File file) {
		if (context==null||(context instanceof Activity && (((Activity) context).isFinishing()))) return;
		Glide.with(context).load(file).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)).into(target);
	}

	public static void load(Context context, ImageView target, String url) {
		load(context, target, url, null, null);
	}

	public static void load(Context context, ImageView target, String url, int defaultImage, RequestListener listener) {
		load(context, target, url, null, null, defaultImage, listener);
	}

	public static void load(Context context, ImageView target, String url, int defaultImage) {
		load(context, target, url, null, null, defaultImage, null);
	}

	public static void load(Context context, ImageView target, String url, String userId, String username) {
		load(context, target, url, userId, username, 0, null);
	}

	@SuppressLint("CheckResult")
	public static void load(Context context, ImageView target, String url, String userId, String username, int defaultImage,
			RequestListener listener) {
		if (context==null||(context instanceof Activity && (((Activity) context).isFinishing()))) return;
		AvatarDrawable drawable = null;
		if (!TextUtils.isEmpty(username)) {
			if (defaultImage == 0) {
				drawable = new AvatarDrawable(userId, username);
			}
		}

		if (!TextUtils.isEmpty(url) && url.contains("/UserUploadFile/photo/photo.png")) {
			url = null; // MD 使用默认背景+文字~
		}

		if (url != null && url.contains("\\")) {
			url = url.replace("\\", "/");
		}

		RequestBuilder<Drawable> request = Glide.with(context).load(url);

		RequestOptions requestOptions = new RequestOptions();
		if (defaultImage != 0) {
			requestOptions.placeholder(defaultImage);
			requestOptions.error(defaultImage);
		}
		else if (drawable != null) {
			requestOptions.placeholder(drawable);
		}
		requestOptions.optionalTransform(new GlideRoundTransformation(context));
		requestOptions.diskCacheStrategy(DiskCacheStrategy.DATA);
		request.apply(requestOptions).listener(listener).into(target);
	}

	/**
	 * 暂停图片加载
	 */
	public static void pauseLoad(Context context) {
		Glide.with(context).pauseRequests();
	}

	/**
	 * 继续图片加载
	 */
	public static void resumeLoad(Context context) {
		Glide.with(context).resumeRequests();
	}

	public static void clear(Context context, ImageView target) {
		Glide.with(context).clear(target);
	}
}
