package cn.flyrise.feep.core.image.glide;

import android.content.Context;
import android.support.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import java.io.InputStream;


/**
 * 初始化glide配置
 */
@GlideModule
public final class FeOkHttpGlideModule extends AppGlideModule {

	@Override
	public void applyOptions(@NonNull final Context context, @NonNull GlideBuilder builder) {
		int maxMemory = (int) Runtime.getRuntime().maxMemory();//获取系统分配给应用的总内存大小
		int memoryCacheSize = maxMemory / 8;//设置图片内存缓存占用八分之一
		//设置内存缓存大小
		builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
		builder.setBitmapPool(new LruBitmapPool(memoryCacheSize));
	}

	@Override public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
		//配置glide网络加载框架
		registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(UnsafeOkHttpClient.getUnsafeOkHttpClient()));

	}

	@Override public boolean isManifestParsingEnabled() {
		return false;
	}
}
