package cn.flyrise.feep.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.support.annotation.Nullable;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Create by cm132 on 2019/8/15 18:19.
 * Describe:下载临时图片
 */
public class DownloadUtils {

	public static void downloadImage(Context context, String url) {
		File save = saveImageFile(url);
		if (save.exists()) {
			FEToast.showMessage("图片已存在");
			return;
		}
		Observable
				.unsafeCreate(f -> {
					try {
						downloadImages(context, url, save);
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(text -> {

				});
	}

	private static File saveImageFile(String url) {
		final File dFile = new File(CoreZygote.getPathServices().getMediaPath());
		if (!dFile.exists()) {
			dFile.mkdirs();
		}
		String fileName;
		if (url.contains("/")) {
			fileName = url.substring(url.lastIndexOf("/")) + ".png";
		}
		else {
			fileName = System.currentTimeMillis() + ".png";
		}
		return new File(CoreZygote.getPathServices().getMediaPath(), fileName);
	}

	private static void downloadImages(Context context, String url, File saveFile) throws ExecutionException, InterruptedException {
		Glide.with(context)
				.asBitmap()
				.apply(new RequestOptions()
						.diskCacheStrategy(DiskCacheStrategy.NONE))
				.listener(new RequestListener<Bitmap>() {
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target,
							boolean isFirstResource) {
						return false;
					}

					@Override
					public boolean onResourceReady(final Bitmap resource, Object model, Target<Bitmap> target,
							DataSource dataSource,
							boolean isFirstResource) {
						if (resource != null) {
							saveBitmap(resource, saveFile);
						}
						return false;
					}
				})
				.load(url)
				.submit().get();
	}

	private static void saveBitmap(Bitmap bitmap, File saveFile) {
		try {
			// 保存到本地
			final FileOutputStream out = new FileOutputStream(saveFile);
			bitmap.compress(CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			FEToast.showMessage("保存成功");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
