package cn.flyrise.feep.email.views;

import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.EditText;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.services.ILoginUserServices;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * @author ZYP
 * @since 2016/7/29 18:41
 * 用于解析邮件内容中的 img
 */
public class UrlImageParser implements Html.ImageGetter {

	private EditText mView;
	private URI mBaseUri;
	private boolean mMatchParentWidth;
	public static String mBaseUrl;

	public UrlImageParser(EditText textView) {
		this.mView = textView;
		this.mMatchParentWidth = false;
	}

	public UrlImageParser(EditText textView, String baseUrl) {
		this.mView = textView;
		if (baseUrl != null) {
			this.mBaseUri = URI.create(baseUrl);
			mBaseUrl = baseUrl;
		}
	}

	public UrlImageParser(EditText textView, String baseUrl, boolean mMatchParentWidth) {
		this.mView = textView;
		this.mMatchParentWidth = mMatchParentWidth;
		if (baseUrl != null) {
			this.mBaseUri = URI.create(baseUrl);
		}
	}

	public Drawable getDrawable(String source) {
		UrlDrawable urlDrawable = new UrlDrawable();
		ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable, this, mView, mMatchParentWidth);
		asyncTask.execute(source);
		return urlDrawable;
	}

	private static class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {

		private final WeakReference<UrlDrawable> drawableReference;
		private final WeakReference<UrlImageParser> imageGetterReference;
		private final WeakReference<View> containerReference;
		private String source;
		private boolean matchParentWidth;
		private float scale;

		public ImageGetterAsyncTask(UrlDrawable d, UrlImageParser imageGetter, View mView, boolean matchParentWidth) {
			this.drawableReference = new WeakReference<>(d);
			this.imageGetterReference = new WeakReference<>(imageGetter);
			this.containerReference = new WeakReference<>(mView);
			this.matchParentWidth = matchParentWidth;
		}

		@Override protected Drawable doInBackground(String... params) {
			source = params[0];
			return fetchDrawable(source);
		}

		@Override protected void onPostExecute(Drawable result) {
			if (result == null) {
				return;
			}
			final UrlDrawable urlDrawable = drawableReference.get();
			if (urlDrawable == null) {
				return;
			}
			urlDrawable.setBounds(0, 0, (int) (result.getIntrinsicWidth() * scale), (int) (result.getIntrinsicHeight() * scale));
			urlDrawable.drawable = result;

			final UrlImageParser imageGetter = imageGetterReference.get();
			if (imageGetter == null) {
				return;
			}
			imageGetter.mView.invalidate();
			imageGetter.mView.setText(imageGetter.mView.getText());
		}

		public Drawable fetchDrawable(String urlString) {
			try {
				if (urlString.contains("/AttachmentServlet39?attachPK=")) {
					if (!urlString.startsWith("/AttachmentServlet39?attachPK=")) {
						// 不需要 前缀
						urlString = urlString.replace(mBaseUrl, "");
					}
				}

				InputStream is = fetch(urlString);
				Drawable drawable = Drawable.createFromStream(is, "src");
				scale = getScale(drawable);
				drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * scale), (int) (drawable.getIntrinsicHeight() * scale));
				return drawable;
			} catch (Exception e) {
				return null;
			}
		}

		private float getScale(Drawable drawable) {
			View view = containerReference.get();
			if (!matchParentWidth || view == null) {
				return 4f;
			}
			float maxWidth = view.getWidth();
			float originalDrawableWidth = drawable.getIntrinsicWidth();
			return maxWidth / originalDrawableWidth;
		}

		private InputStream fetch(String urlString) throws IOException {
			if (urlString.contains("record:image/")) {
				try {
					urlString = urlString.split(",")[1];
					byte[] decode = Base64.decode(urlString.getBytes(), Base64.DEFAULT);
					return new ByteArrayInputStream(decode, 0, decode.length);
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}

			File localFile = null;
			try {
				localFile = new File(urlString);
			} catch (Exception exp) {
				localFile = null;
				exp.printStackTrace();
			}

			if (localFile != null && localFile.exists()) {
				return new FileInputStream(localFile);
			}

			URL url;
			final UrlImageParser imageGetter = imageGetterReference.get();
			if (imageGetter == null) {
				return null;
			}

			if (urlString.startsWith("http")) {
				url = new URL(urlString);
			}
			else {
				if (imageGetter.mBaseUri != null) {
					url = imageGetter.mBaseUri.resolve(urlString).toURL();
				}
				else {
					url = URI.create(urlString).toURL();
				}
			}

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(8000);
			connection.setReadTimeout(8000);
			connection.setRequestProperty("User-agent",CoreZygote.getUserAgent());
			if (!urlString.startsWith("http")) {
				CookieManager cookieManager = CookieManager.getInstance();
				String cookie = cookieManager.getCookie(mBaseUrl);
				connection.setRequestProperty("Cookie", cookie);
				connection.setDoInput(true);
				connection.setDoOutput(true);
			}

			ILoginUserServices userServices = CoreZygote.getLoginUserServices();
			if (userServices != null && !TextUtils.isEmpty(userServices.getAccessToken())) {
				connection.setRequestProperty("token", userServices.getAccessToken());
			}

			return connection.getInputStream();
		}
	}

	@SuppressWarnings("deprecation")
	public class UrlDrawable extends BitmapDrawable {

		protected Drawable drawable;

		@Override public void draw(Canvas canvas) {
			if (drawable != null) {
				drawable.draw(canvas);
			}
		}
	}

}
