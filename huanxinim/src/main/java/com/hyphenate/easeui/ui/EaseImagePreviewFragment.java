package com.hyphenate.easeui.ui;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
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
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.ui.EaseImagePreviewAdapter.OnImagePreviewClickListener;
import com.hyphenate.easeui.widget.photoview.EasePhotoView;

/**
 * Create by Mo 2019/01/09
 */

public class EaseImagePreviewFragment extends Fragment {

	private static final String TAG = "ShowBigImage";
	private ProgressDialog pd;
	private EasePhotoView image;
	private ImageView gifView;
	private ProgressBar loadLocalPb;
	private String imageUrl;
	private OnImagePreviewClickListener mPreViewClickListener;


	public static EaseImagePreviewFragment newInstance(String url, OnImagePreviewClickListener listener){
		EaseImagePreviewFragment jsImagePreviewFragment = new EaseImagePreviewFragment();
		jsImagePreviewFragment.imageUrl = url;
		jsImagePreviewFragment.mPreViewClickListener = listener;
		return jsImagePreviewFragment;
	}

	@Nullable @Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.ease_fragment_js_image_preview,container,false);
		image = view.findViewById(R.id.easePhotoView);
		gifView = view.findViewById(R.id.easeGift);
		loadLocalPb = view.findViewById(R.id.pb_load_local);
		loadLocalPb.setVisibility(View.VISIBLE);
		gifView.setVisibility(View.VISIBLE);
		image.setVisibility(View.GONE);
		image.setOnClickPhotoViewListener(() -> mPreViewClickListener.onImagePreviewClick());
		gifView.setOnClickListener(v -> {mPreViewClickListener.onImagePreviewClick();});
		String serverAddress = CoreZygote.getLoginUserServices().getServerAddress();
		Headers headers;
		if (imageUrl != null && imageUrl.contains(serverAddress)) {
			CookieManager cookieManager = CookieManager.getInstance();
			String cookie = cookieManager.getCookie(CoreZygote.getLoginUserServices().getServerAddress());
			headers = new LazyHeaders.Builder().addHeader("Cookie", cookie).build();
		}
		else {
			headers = Headers.DEFAULT;
		}

		if (imageUrl.startsWith("data:image/") && imageUrl.contains("base64")) {
			loadLocalPb.setVisibility(View.GONE);
			gifView.setVisibility(View.GONE);
			image.setVisibility(View.VISIBLE);
			image.setImageBitmap(stringToBitmap(imageUrl));
		}
		else {
			RequestBuilder<GifDrawable> requestBuilder = Glide.with(this).asGif();
			requestBuilder.load(new GlideUrl(imageUrl, headers))
					.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
					.listener(new RequestListener<GifDrawable>() {
						@Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target,
								boolean isFirstResource) {
							gifView.setVisibility(View.GONE);
							image.setVisibility(View.VISIBLE);

							Glide.with(getActivity())
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
		return view;
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
}
