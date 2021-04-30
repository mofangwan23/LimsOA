package cn.flyrise.feep.core.network;

import static cn.flyrise.feep.core.network.RemoteException.CODE_MAC_CHECK_ERROR;
import static cn.flyrise.feep.core.network.RemoteException.CODE_NET_ERROR;
import static cn.flyrise.feep.core.network.RemoteException.CODE_REQUEST_CANCEL;
import static cn.flyrise.feep.core.network.RemoteException.CODE_UNKNOWN_ERROR;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.SparseArray;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.common.RsaManager;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.network.callback.AbstractCallback;
import cn.flyrise.feep.core.network.callback.Callback;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.callback.StringCallback;
import cn.flyrise.feep.core.network.cookie.PersistentCookieJar;
import cn.flyrise.feep.core.network.cookie.SetCookieCache;
import cn.flyrise.feep.core.network.cookie.SharedPrefsCookiePersistent;
import cn.flyrise.feep.core.network.listener.OnNetworkExceptionListener;
import cn.flyrise.feep.core.network.request.Request;
import cn.flyrise.feep.core.network.request.RequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.services.model.NetworkInfo;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ZYP
 * @since 2016-09-01 14:56 这破玩意应该在用户设置好 ip 和 端口之后就进行初始化...
 */
public class FEHttpClient {

	private static final String HTTP_CACHE_DIR = "ok-http-cache";
	private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024;
	private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024;
	private static final String BASE_PATH = "/servlet/mobileServlet?";
	public static final String KNOWLEDGE_DOWNLOAD_PATH = "/servlet/mobileAttachmentServlet?fileGuid=";

	private static FEHttpClient sInstance;
	private static OnNetworkExceptionListener sNetworkExceptionListener;

	private final Context mContext;
	private final String mHost;
	private final String mPath;
	private final OkHttpClient mOkHttpClient;
	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private final RepositoryExceptionHandler repositoryExceptionHandler;

	private final static SparseArray<Set<Callback>> mCacheCallBacks = new SparseArray<>();

	public static FEHttpClient getInstance() {
		if (sInstance == null) {
			NetworkInfo networkInfo = null;
			if (CoreZygote.getLoginUserServices() != null) {
				networkInfo = CoreZygote.getLoginUserServices().getNetworkInfo();
			}
			String serverAddress = "";
			String serverPort = "";
			boolean isHttps = false;

			if (networkInfo == null) {
				String userIp = SpUtil.get("USER_IP", "");
				if (!TextUtils.isEmpty(userIp)) {
					if (userIp.contains(":")) {
						isHttps = userIp.contains("https");
						String[] split = userIp.split(":");
						serverAddress = split[1];
						serverAddress = serverAddress.substring(2, serverAddress.length());
						serverPort = split[2];
					}
					else {
						serverAddress = userIp.substring(2, serverAddress.length());
					}
				}
			}
			else {
				serverAddress = networkInfo.serverAddress;
				serverPort = networkInfo.serverPort;
				isHttps = networkInfo.isHttps;
			}

//			if (!TextUtils.isEmpty(serverAddress) && !TextUtils.isEmpty(serverPort)) {
			if (!TextUtils.isEmpty(serverAddress)) {
				new FEHttpClient.Builder(CoreZygote.getContext())
						.address(serverAddress)
						.port(serverPort)
						.isHttps(isHttps)
						.keyStore(CoreZygote.getPathServices().getKeyStoreFile())
						.build();
			}

			if (sInstance != null) {
				return sInstance;
			}
			throw new NullPointerException("FEHttpClient is null. perhaps not initialization.");
		}
		return sInstance;
	}

	private FEHttpClient(Builder builder) {
		this.mHost = builder.getHost();
		this.mPath = mHost + BASE_PATH;
		this.mContext = builder.context;
		this.mOkHttpClient = builder.newOkHttpClient();
		this.repositoryExceptionHandler = new RepositoryExceptionHandler();
		this.repositoryExceptionHandler.setOnNetworkExceptionListener(sNetworkExceptionListener);

		if (builder.isHttps) {
			HttpsURLConnection.setDefaultSSLSocketFactory(mOkHttpClient.sslSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(mOkHttpClient.hostnameVerifier());
		}
	}

	public static void addNetworkExceptionHandler(OnNetworkExceptionListener listener) {
		sNetworkExceptionListener = listener;
	}

	public OkHttpClient getOkHttpClient() {
		return this.mOkHttpClient;
	}

	public String getHost() {
		return this.mHost;
	}

	public String getPath() {
		return this.mPath;
	}

	public List<Cookie> getAllCookies() {
		if (this.mOkHttpClient == null) {
			return null;
		}

		CookieJar cookieJar = this.mOkHttpClient.cookieJar();
		if (cookieJar == null) {
			return null;
		}

		return cookieJar.loadForRequest(HttpUrl.parse(this.mHost));
	}

	public void bindWebViewCookie() {
		List<Cookie> cookies = getAllCookies();
		if (cookies == null) {
			return;
		}
		for (Cookie cookie : cookies) {
			CookieSyncManager.createInstance(this.mContext);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(true);
			String cookieString = cookie.name() + "=" + cookie.value() + "; domain=" + cookie.domain();
			cookieManager.setCookie(mHost, cookieString);
			CookieSyncManager.getInstance().sync();
		}
	}

	public <T> void post(Request request, Callback<T> callback) {
		String sb = RsaManager.encrptString("{\"iq\":" + GsonUtil.getInstance().toJson(request) + "}");
		String nameSpace = "";
		RequestContent reqContent = request.getReqContent();
		if (reqContent != null) {
			nameSpace = reqContent.getNameSpace();
		}
		FormBody body = new FormBody.Builder().add("json", sb).build();
		execute(setUserAgent(new okhttp3.Request.Builder().tag(nameSpace).url(mPath).post(body)), callback);
	}

	public <R extends RequestContent, T> void post(R requestContent, Callback<T> callback) {
		Request<R> requestBody = new Request<>();
		requestBody.setReqContent(requestContent);
		post(requestBody, callback);
	}

	public <T> void post(String url, Map<String, String> params, Callback<T> callback) {
		if (TextUtils.isEmpty(url)) {
			url = mPath;
		}

		FormBody.Builder builder = new FormBody.Builder();
		if (params != null) {
			Set<String> keys = params.keySet();
			for (String key : keys) {
				String value = params.get(key);
				builder.add(key, value);
			}
		}
		execute(setUserAgent(new okhttp3.Request.Builder().url(url).post(builder.build())), callback);
	}

	private okhttp3.Request setUserAgent(okhttp3.Request.Builder builder) {
		return builder
				.addHeader("User-Agent", CoreZygote.getUserAgent())
				.build();

	}

	private <T> void execute(okhttp3.Request request, final Callback<T> callback) {
		if (callback != null) {
			callback.onPreExecute();
			addCallback(callback);
		}

		this.mOkHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
			@Override
			public void onFailure(Call call, final IOException exception) {
				if (call.isCanceled()) {
					return;
				}
				handleFailure(callback, new RemoteException.Builder()
						.exception(exception).canceled(call.isCanceled()).isReLogin(false).build());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (call.isCanceled()) {
					return;
				}

				if (!response.isSuccessful()) {
					handleFailure(callback, new RemoteException.Builder().isReLogin(false).response(response).build());
					return;
				}

				FEHttpHeaders.getInstance().inject(response);
				//保存一个服务器的时间差
				if (FEHttpHeaders.getInstance().getLatestDate() != null)
					DateUtil.setServiceTime(FEHttpHeaders.getInstance().getLatestDate().getTime());
				if (callback instanceof StringCallback) {
					dispatchStringCallback((StringCallback) callback, response);
					return;
				}

				dispatchResponseCallback((ResponseCallback) callback, response);
			}
		});
	}

	private void dispatchStringCallback(final StringCallback callback, Response response) {
		try {
			final String result = response.body().string();
			if (TextUtils.isEmpty(result)) {
				throw new NullPointerException(mContext.getResources().getString(R.string.core_http_success_exception));
			}

			if (callback == null || callback.isCanceled()) {
				return;
			}
			mHandler.post(() -> callback.onCompleted(result));
			removeCallback(callback);
		} catch (Exception exception) {
			String errorMessage = mContext.getResources().getString(R.string.core_http_success_exception);
			handleFailure(callback, new RemoteException.Builder().isReLogin(false).exception(exception).message(errorMessage).build());
		}
	}

	private void dispatchResponseCallback(final ResponseCallback callback, Response response) {
		if (callback == null) {
			return;
		}

		Class clazz = (Class) ((ParameterizedType) callback.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		try {
			ResponseBody responseBody = response.body();
			JSONObject properties = new JSONObject(responseBody.string());
			String query;
			if (properties.has("iq")) {
				JSONObject iq = properties.getJSONObject("iq");
				query = iq.get("query").toString();
			}
			else {
				query = properties.get("query").toString();
			}
			final ResponseContent responseContent = (ResponseContent) GsonUtil.getInstance().fromJson(query, clazz);
			String errorCode = responseContent.getErrorCode();
			if (TextUtils.equals(errorCode, "-1")
					|| TextUtils.equals(errorCode, "-96")
					|| TextUtils.equals(errorCode, "100001")) {
				String errorMessage = responseContent.getErrorMessage();
				handleFailure(callback, new RemoteException.Builder()
						.isLoadLogout(!TextUtils.equals(errorCode, "100001"))
						.isReLogin(true)
						.message(errorMessage)
						.response(response)
						.build());
				return;
			}

			if (callback.isCanceled()) {
				return;
			}
			mHandler.post(() -> callback.onCompleted(responseContent));
			removeCallback(callback);
		} catch (JSONException ex) {
			String errorMessage = mContext.getResources().getString(R.string.core_http_success_exception);
			handleFailure(callback, new RemoteException.Builder()
					.isReLogin(false)
					.exception(ex)
					.message(errorMessage)
					.errorCode(CODE_MAC_CHECK_ERROR)
					.build());
		} catch (IOException ex) {
			String errorMessage = mContext.getResources().getString(R.string.core_network_error_retry);
			handleFailure(callback, new RemoteException.Builder()
					.isReLogin(false)
					.exception(ex)
					.message(errorMessage)
					.errorCode(CODE_NET_ERROR)
					.build());
		} catch (JsonSyntaxException exp) {
			String errorMessage = mContext.getResources().getString(R.string.core_http_success_exception);
			handleFailure(callback, new RemoteException.Builder()
					.isReLogin(false)
					.exception(exp)
					.message(errorMessage)
					.errorCode(CODE_REQUEST_CANCEL)
					.build());
		} catch (IllegalStateException ex) {  // Expected a string but was BEGIN_OBJECT at line 1 column 18 path $.errorMessage
			String errorMessage = mContext.getResources().getString(R.string.core_http_success_exception);
			handleFailure(callback, new RemoteException.Builder()
					.isReLogin(false)
					.exception(ex)
					.message(errorMessage)
					.errorCode(CODE_REQUEST_CANCEL)
					.build());
		} catch (Exception ex) {
			String errorMessage = mContext.getResources().getString(R.string.core_http_success_exception);
			handleFailure(callback, new RemoteException.Builder()
					.isReLogin(false)
					.exception(ex)
					.message(errorMessage)
					.errorCode(CODE_UNKNOWN_ERROR)
					.build());
		}
	}

	private <T> void handleFailure(final Callback<T> callback, final RepositoryException repositoryException) {
		if (callback == null || callback.isCanceled()) {
			return;
		}
		mHandler.post(() -> {
			repositoryExceptionHandler.handleRemoteException(repositoryException);
			callback.onFailure(repositoryException);
		});
		removeCallback(callback);
	}

	/**
	 * 取消所有发生在这个 object 上的网络请求，不管 object 是 Activity / Fragment / IView / other ...
	 * 跟下面的 #removeCallback() 是不同的，removeCallback 仅仅是移除当前的 callback .
	 */
	public static void cancel(Object object) {
		if (mCacheCallBacks.size() == 0) {
			return;
		}

		if (object == null) {
			for (int i = 0; i < mCacheCallBacks.size(); i++) {
				int key = mCacheCallBacks.keyAt(i);
				Set<Callback> callbacks = mCacheCallBacks.get(key);
				Iterator<Callback> iterator = callbacks.iterator();
				while (iterator.hasNext()) {
					iterator.next().cancel();
					iterator.remove();
				}
			}
			return;
		}

		if (object.getClass() != null) {
			int key = object.hashCode();
			Set<Callback> callbacks = mCacheCallBacks.get(key);
			if (CommonUtil.isEmptyList(callbacks)) {
				return;
			}

			Iterator<Callback> iterator = callbacks.iterator();
			while (iterator.hasNext()) {
				iterator.next().cancel();
				iterator.remove();
			}

			// 宁可杀错不可放过
			Set<Callback> unknowCallback = mCacheCallBacks.get(AbstractCallback.DEFAULT_KEY);
			if (CommonUtil.isEmptyList(unknowCallback)) {
				return;
			}
			iterator = unknowCallback.iterator();
			while (iterator.hasNext()) {
				iterator.next().cancel();
				iterator.remove();
			}
		}
	}

	public void addCallback(Callback callback) {
		if (callback == null) {
			return;
		}

		Set<Callback> callbacks = mCacheCallBacks.get(callback.key());
		if (callbacks == null) {
			callbacks = new HashSet<>();
			mCacheCallBacks.put(callback.key(), callbacks);
		}
		callbacks.add(callback);
	}

	public void removeCallback(Callback callback) {
		if (callback == null) {
			return;
		}

		Set<Callback> callbacks = mCacheCallBacks.get(callback.key());
		if (CommonUtil.isEmptyList(callbacks)) {
			return;
		}

		callbacks.remove(callback);
	}

	public Set<Callback> getCallbacks(Object object) {
		if (object == null) {
			return null;
		}

		int key = object.getClass().getCanonicalName().hashCode();
		return mCacheCallBacks.get(key);
	}

	/**
	 * 取消正在执行的网络。跟 removeCallback 不同。
	 * `removeCallback` 取消的是响应结果的回调接口。
	 * @param namespace 正在执行的请求的 NAME_SPACE
	 */
	public void cancelCall(String namespace) {
		if (mOkHttpClient == null) {
			return;
		}
		if (TextUtils.isEmpty(namespace)) {
			return;
		}

		List<Call> calls = this.mOkHttpClient.dispatcher().queuedCalls();
		for (Call call : calls) {
			Object tag = call.request().tag();
			if (tag == null) {
				continue;
			}
			if (TextUtils.equals(tag.toString(), namespace)) {
				call.cancel();
			}
		}
	}

	public static class Builder {

		private String host;
		private String port;
		private String address;
		private String keyStore;
		private Context context;
		private boolean isHttps;

		public Builder(Context context) {
			if (!(context instanceof Application)) {
				throw new IllegalArgumentException("The context what i need is an Application, don't pass a activity or other context");
			}
			this.context = context;
		}

		public Builder address(String address) {
			this.address = address;
			return this;
		}

		public Builder port(String port) {
			this.port = port;
			return this;
		}

		public Builder isHttps(boolean isHttps) {
			this.isHttps = isHttps;
			return this;
		}

		public Builder keyStore(String keyStore) {
			this.keyStore = keyStore;
			return this;
		}

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public String getHost() {
			if (TextUtils.isEmpty(host)) {
				String schema = isHttps ? "https" : "http";
				return schema + "://" + address + (TextUtils.isEmpty(port) ? "" : ":" + port);
			}

			return host;
		}

		private File newCacheDir() {
			File cacheDir = new File(context.getApplicationContext().getCacheDir(), HTTP_CACHE_DIR);
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			return cacheDir;
		}

		private long calculateDiskCacheSize(File cacheDir) {
			long size = MIN_DISK_CACHE_SIZE;

			try {
				StatFs statFs = new StatFs(cacheDir.getAbsolutePath());
				long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
				size = available / 50;
			} catch (IllegalArgumentException ignored) {
				ignored.printStackTrace();
			}

			return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
		}

		private OkHttpClient newOkHttpClient() {
			File cacheDir = newCacheDir();
			OkHttpClient.Builder builder = new OkHttpClient.Builder()
					.readTimeout(15, TimeUnit.SECONDS)
					.writeTimeout(15, TimeUnit.SECONDS)
					.connectTimeout(15, TimeUnit.SECONDS)
					.cache(new Cache(cacheDir, calculateDiskCacheSize(cacheDir)))
					.addInterceptor(new TokenInterceptor())
					.addNetworkInterceptor(new StethoInterceptor())
					.cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistent(context)));

			if (isHttps) {
				if (!TextUtils.isEmpty(this.keyStore)) {
					File keyFile = new File(this.keyStore);
					if (keyFile.exists()) {
						try {
							TrustManagerFactory trustManagerFactory = TrustManagerFactory
									.getInstance(TrustManagerFactory.getDefaultAlgorithm());
							KeyStore trustKeyStore = null;
							if (this.keyStore != null) {
								if (keyFile.exists()) {
									trustKeyStore = KeyStore.getInstance("BKS");
									trustKeyStore.load(new FileInputStream(keyFile), "password".toCharArray());
								}
							}

							trustManagerFactory.init(trustKeyStore);
							SSLContext sslContext = SSLContext.getInstance("TLS");
							sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
							SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
							return builder.sslSocketFactory(sslSocketFactory).hostnameVerifier((name, session) -> true).build();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				try {
					final SSLContext tls = SSLContext.getInstance("TLS");
					tls.init(null, new TrustManager[]{new X509TrustManager() {
						@Override public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						}

						@Override public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						}

						@Override public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}
					}}, null);
					SSLSocketFactory socketFactory = tls.getSocketFactory();

					return builder
							.sslSocketFactory(socketFactory)
							.hostnameVerifier((name, session) -> true)
							.build();
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}
			return builder.build();
		}

		public void build() {
			sInstance = new FEHttpClient(this);
		}
	}

	public static void cancelHttpClient() {
		if (sInstance == null) {
			return;
		}
		sInstance = null;
	}

}