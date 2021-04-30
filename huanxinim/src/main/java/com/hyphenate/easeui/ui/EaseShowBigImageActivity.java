/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.BitmapUtil;
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
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.model.EaseImageCache;
import com.hyphenate.easeui.widget.photoview.EasePhotoView;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.ImageUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;


/**
 * download and show original image
 */
public class EaseShowBigImageActivity extends AppCompatActivity {

	private static final String TAG = "ShowBigImage";
	private ProgressDialog pd;
	private EasePhotoView image;
	private String localFilePath;
	private Bitmap bitmap;
	private boolean isDownloaded;

	private ImageView gifView;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.ease_activity_show_big_image);
		super.onCreate(savedInstanceState);
		FEStatusBar.setupStatusBar(this.getWindow(), Color.TRANSPARENT);

		image = findViewById(R.id.image);
		gifView = findViewById(R.id.gif);
		final ProgressBar loadLocalPb = findViewById(R.id.pb_load_local);
		Uri uri = getIntent().getParcelableExtra("uri");
		localFilePath = getIntent().getExtras().getString("localUrl");
		String msgId = getIntent().getExtras().getString("messageId");
		EMLog.d(TAG, "show big msgId:" + msgId);

		if (uri != null && new File(uri.getPath()).exists()) {
			if (BitmapUtil.isPictureGif(uri.getPath())) {
				gifView.setVisibility(View.VISIBLE);
				image.setVisibility(View.GONE);
				RequestBuilder<GifDrawable> requestBuilder = Glide.with(this).asGif();
				requestBuilder.load(uri.getPath())
						.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.ease_default_image))
						.into(gifView);
				return;
			}
			else {
				gifView.setVisibility(View.GONE);
				image.setVisibility(View.VISIBLE);
				Glide.with(this).load(uri.getPath()).apply(new RequestOptions().placeholder(R.drawable.ease_default_image)).into(image);
			}
		}
		else if (msgId != null) {
			downloadImage(msgId);
		}
		else {
			loadLocalPb.setVisibility(View.VISIBLE);
			gifView.setVisibility(View.VISIBLE);
			image.setVisibility(View.GONE);

			String serverAddress = CoreZygote.getLoginUserServices().getServerAddress();
			Headers headers;
			if (localFilePath != null && localFilePath.contains(serverAddress)) {
				CookieManager cookieManager = CookieManager.getInstance();
				String cookie = cookieManager.getCookie(CoreZygote.getLoginUserServices().getServerAddress());
				headers = new LazyHeaders.Builder().addHeader("Cookie", cookie).build();
			}
			else {
				headers = Headers.DEFAULT;
			}

			if (localFilePath.startsWith("data:image/") && localFilePath.contains("base64")) {
				loadLocalPb.setVisibility(View.GONE);
				gifView.setVisibility(View.GONE);
				image.setVisibility(View.VISIBLE);
				image.setImageBitmap(stringToBitmap(localFilePath));
			}
			else {
				RequestBuilder<GifDrawable> requestBuilder = Glide.with(this).asGif();
				requestBuilder.load(new GlideUrl(localFilePath, headers))
						.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
						.listener(new RequestListener<GifDrawable>() {
							@Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target,
									boolean isFirstResource) {
								gifView.setVisibility(View.GONE);
								image.setVisibility(View.VISIBLE);

								Glide.with(EaseShowBigImageActivity.this)
										.load(model)
										.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
										.listener(new RequestListener<Drawable>() {
											@Override
											public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
													boolean isFirstResource) {
												loadLocalPb.setVisibility(View.GONE);
												FEToast.showMessage(getString(R.string.iamge_load_error));
												return false;
											}

											@Override
											public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
													DataSource dataSource,
													boolean isFirstResource) {
												loadLocalPb.setVisibility(View.GONE);
												return false;
											}
										})
										.into(image);
								return true;
							}

							@Override public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target,
									DataSource dataSource,
									boolean isFirstResource) {
								loadLocalPb.setVisibility(View.GONE);
								return false;
							}
						})
						.into(gifView);

			}
		}

		image.setOnClickPhotoViewListener(() -> finish());
		gifView.setOnClickListener((view) -> finish());
	}

	private Bitmap stringToBitmap(String string) {
		Bitmap bitmap = null;
		try {
			byte[] bitmapArray = Base64.decode(string.split(",")[1], Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * download image
	 */
	@SuppressLint("NewApi")
	private void downloadImage(final String msgId) {
		EMLog.e(TAG, "download with messageId: " + msgId);
		String str1 = getResources().getString(R.string.Download_the_pictures);
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage(str1);
		pd.show();
		File temp = new File(localFilePath);
		final String tempPath = temp.getParent() + "/temp_" + temp.getName();
		final EMCallBack callback = new EMCallBack() {
			public void onSuccess() {
				EMLog.e(TAG, "onSuccess");
				runOnUiThread(() -> {
					new File(tempPath).renameTo(new File(localFilePath));

					DisplayMetrics metrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int screenWidth = metrics.widthPixels;
					int screenHeight = metrics.heightPixels;

					bitmap = ImageUtils.decodeScaleImage(localFilePath, screenWidth, screenHeight);

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
					byte[] bytes = baos.toByteArray();

					image.setVisibility(View.VISIBLE);
					Glide.with(EaseShowBigImageActivity.this)
							.load(bytes)
							.apply(new RequestOptions().placeholder(R.drawable.ease_default_image))
							.into(image);
					if (bitmap != null) {
						EaseImageCache.getInstance().put(localFilePath, bitmap);
						isDownloaded = true;
					}
					if (isFinishing() || isDestroyed()) {
						return;
					}
					if (pd != null) {
						pd.dismiss();
					}
				});
			}

			public void onError(int error, String msg) {
				EMLog.e(TAG, "offline file transfer error:" + msg);
				File file = new File(tempPath);
				if (file.exists() && file.isFile()) {
					file.delete();
				}
				runOnUiThread(() -> {
					if (EaseShowBigImageActivity.this.isFinishing() || EaseShowBigImageActivity.this.isDestroyed()) {
						return;
					}
					image.setImageResource(R.drawable.ease_default_image);
					pd.dismiss();
				});
			}

			public void onProgress(final int progress, String status) {
				EMLog.d(TAG, "Progress: " + progress);
				final String str2 = getResources().getString(R.string.Download_the_pictures_new);
				runOnUiThread(() -> {
					if (EaseShowBigImageActivity.this.isFinishing() || EaseShowBigImageActivity.this.isDestroyed()) {
						return;
					}
					pd.setMessage(str2 + progress + "%");
				});
			}
		};

		EMMessage msg = EMClient.getInstance().chatManager().getMessage(msgId);
		msg.setMessageStatusCallback(callback);
		EMClient.getInstance().chatManager().downloadAttachment(msg);
	}

	@Override
	public void onBackPressed() {
		if (isDownloaded) {
			setResult(RESULT_OK);
		}
		finish();
	}
}
