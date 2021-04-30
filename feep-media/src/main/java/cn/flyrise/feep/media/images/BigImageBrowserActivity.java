package cn.flyrise.feep.media.images;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.flyrise.feep.media.R;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import java.io.File;

/**
 * @author ZYP
 * @since 2017-10-23 09:53
 * 查看大图，支持普通大图、GIF、网络图片，但不支持环信
 */
@Route("/media/image/big/browser")
@RequestExtras({
		"imagePath",    // 查看图片所在的 path 或 url
		"imageCookie"   // 查看网络图片时所需的 Cookie
})
public class BigImageBrowserActivity extends AppCompatActivity implements OnClickListener {

	/**
	 * 图片的路径.
	 */
	public static final String IMAGE_PATH = "imagePath";

	/**
	 * 加载图片所需的 Cookie.
	 */
	public static final String IMAGE_COOKIE = "imageCookie";

	private PhotoView mPhotoView;
	private ImageView mGifView;
	private ProgressBar mProgressBar;

	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ms_activity_big_image_browser);
		mPhotoView = (PhotoView) findViewById(R.id.msPhotoView);
		mGifView = (ImageView) findViewById(R.id.msGifView);
		mProgressBar = (ProgressBar) findViewById(R.id.msProgressBar);

		mPhotoView.setOnClickListener(this);
		mGifView.setOnClickListener(this);
		bindView();
	}

	private void bindView() {
		Intent intent = getIntent();
		String imagePath = intent.getStringExtra(IMAGE_PATH);
		if (TextUtils.isEmpty(imagePath)) {
			this.finish();
			return;
		}

		File localImage = new File(imagePath);
		if (localImage.exists()) {                              // 本地图片，非常好...
			loadLocalImage(imagePath);
			return;
		}

		String cookie = intent.getStringExtra(IMAGE_COOKIE);    // 网络图片...
		loadNetworkImage(imagePath, cookie);
	}

	/**
	 * 加载本地图片
	 * @param imagePath 本地图片 URL
	 */
	private void loadLocalImage(String imagePath) {
		// 1. 加载 Gif 图片
		if (imagePath.endsWith("gif")) {
			mPhotoView.setVisibility(View.GONE);
			mGifView.setVisibility(View.VISIBLE);
			RequestBuilder<GifDrawable> request = Glide.with(this).asGif();
			request.load(imagePath)
					.apply(new RequestOptions().error(R.mipmap.ms_image_preview))
					.listener(new GifDrawableStringRequestListener(mProgressBar))
					.into(mGifView);
			return;
		}

		// 2. 如果不是 Gif 按普通图片加载
		mGifView.setVisibility(View.GONE);
		mPhotoView.setVisibility(View.VISIBLE);
		Glide.with(this).load(imagePath)
				.apply(new RequestOptions().error(R.mipmap.ms_image_preview))
				.listener(new GlideDrawableStringRequestListener(mProgressBar))
				.into(mPhotoView);
	}

	/**
	 * 加载网络图片
	 * @param imagePath 网络图片的 URL
	 * @param cookie 网络图片所需的 Cookie（如果有的话）
	 */
	@SuppressLint("CheckResult")
	private void loadNetworkImage(final String imagePath, final String cookie) {
		// 没办法确定是否是 gif，所以只是先加载 gif，如果是 gif，那么第一次加载肯定就加载失败了...
		mGifView.setVisibility(View.VISIBLE);
		mPhotoView.setVisibility(View.GONE);

		Headers headers = TextUtils.isEmpty(cookie)
				? Headers.DEFAULT
				: new LazyHeaders.Builder().addHeader("Cookie", cookie).build();

		RequestOptions requestOptions = new RequestOptions();
		requestOptions.error(R.mipmap.ms_image_preview);
		requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

		GlideUrl glideUrl = new GlideUrl(imagePath, headers);
		RequestBuilder<GifDrawable> requestBuilder=Glide.with(this).asGif();
		requestBuilder.load(glideUrl)
				.apply(requestOptions)
				.listener(new RequestListener<GifDrawable>() {
					@Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target,
							boolean isFirstResource) {
						loadStaticNetworkImage(glideUrl);                        // 第二次加载咯
						return false;
					}

					@Override
					public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource,
							boolean isFirstResource) {
						mProgressBar.setVisibility(View.GONE);
						return false;
					}
				})
				.into(mGifView);
	}

	/**
	 * 加载静态的网络图片
	 */
	@SuppressLint("CheckResult")
	private void loadStaticNetworkImage(GlideUrl glideUrl) {
		mPhotoView.setVisibility(View.VISIBLE);
		mGifView.setVisibility(View.GONE);

		RequestOptions requestOptions = new RequestOptions();
		requestOptions.error(R.mipmap.ms_image_preview);
		requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

		Glide.with(this)
				.load(glideUrl)
				.apply(requestOptions)
				.listener(new RequestListener<Drawable>() {
					@Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
							boolean isFirstResource) {
						mProgressBar.setVisibility(View.GONE);
						Toast.makeText(getApplicationContext(), "图片加载失败", Toast.LENGTH_SHORT).show();
						return false;
					}

					@Override
					public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource,
							boolean isFirstResource) {
						mProgressBar.setVisibility(View.GONE);
						return false;
					}
				})
				.into(mPhotoView);
	}

	@Override public void onClick(View v) {
		finish();
	}

	private class GifDrawableStringRequestListener implements RequestListener<GifDrawable> {

		private ProgressBar mProgressBar;

		GifDrawableStringRequestListener(ProgressBar progressBar) {
			this.mProgressBar = progressBar;
		}

		@Override
		public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
			mProgressBar.setVisibility(View.GONE);
			Toast.makeText(getApplicationContext(), "图片加载失败", Toast.LENGTH_SHORT).show();
			return false;
		}

		@Override
		public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource,
				boolean isFirstResource) {
			mProgressBar.setVisibility(View.GONE);
			return false;
		}
	}

	private class GlideDrawableStringRequestListener implements RequestListener<Drawable> {

		private ProgressBar mProgressBar;

		GlideDrawableStringRequestListener(ProgressBar progressBar) {
			this.mProgressBar = progressBar;
		}

		@Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
			mProgressBar.setVisibility(View.GONE);
			Toast.makeText(getApplicationContext(), "图片加载失败", Toast.LENGTH_SHORT).show();
			return false;
		}

		@Override public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource,
				boolean isFirstResource) {
			mProgressBar.setVisibility(View.GONE);
			return false;
		}
	}
}
