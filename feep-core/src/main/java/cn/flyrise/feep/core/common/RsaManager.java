package cn.flyrise.feep.core.common;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.RSAEncrypt;
import cn.flyrise.feep.core.network.TokenInterceptor;
import cn.flyrise.feep.core.network.entry.RsaResponse;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import rx.Observable;

public class RsaManager {

	/**
	 * 获取移动端加密公钥
	 */
	public static Observable<String> obtainRsaPuclicKey(final String path) {
		return Observable.create(f -> {
			try {
				okhttp3.Request request = new okhttp3.Request.Builder()
						.url(path + "/chapis")
						.addHeader("Content-Type", "application/x-www-form-urlencoded")
						.addHeader("User-Agent", CoreZygote.getUserAgent())
						.post(new FormBody.Builder().add("act", "rsa-public").build()).build();
				OkHttpClient.Builder builder = new OkHttpClient.Builder()
						.readTimeout(60, TimeUnit.SECONDS)
						.writeTimeout(60, TimeUnit.SECONDS)
						.connectTimeout(60, TimeUnit.SECONDS)
						.addInterceptor(new TokenInterceptor())
						.addNetworkInterceptor(new StethoInterceptor());
				builder.build().newCall(request).enqueue(new okhttp3.Callback() {
					@Override
					public void onResponse(Call call, Response response) {
						if (call.isCanceled()) return;
						if (!response.isSuccessful()) {
							f.onError(new NullPointerException("获取公钥失败"));
							return;
						}
						try {
							final String result = response.body().string();
							if (TextUtils.isEmpty(result)) {
								f.onError(new NullPointerException("获取公钥失败"));
								return;
							}
							RsaResponse rsa = GsonUtil.getInstance().fromJson(result, RsaResponse.class);
							if (rsa == null || !rsa.isSuccess || rsa.data == null || TextUtils.isEmpty(rsa.data.publicKey)) {
								f.onError(new NullPointerException("获取公钥失败"));
								return;
							}
							f.onNext(rsa.data.publicKey);
						} catch (IOException e) {
							f.onError(e);
						}
					}

					@Override
					public void onFailure(Call call, final IOException exception) {
						if (call.isCanceled()) return;
						f.onError(exception);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				f.onError(e);
			}
		});
	}

	/**
	 * 加密部分参数
	 * zcy：act不加密
	 */
	private static Map<String, String> encrptByPublicKey(Map<String, String> data) throws Exception {
		if (data == null || data.isEmpty()) return data;
		String publicKey = CoreZygote.getRsaService().getPublicKey();
		if (TextUtils.isEmpty(publicKey)) return data;
		Map<String, String> params = new HashMap<>();//不加密的数据
		Map<String, String> encodeParams = new HashMap<>();//需要加密的数据
		for (String key : data.keySet()) {
			if (TextUtils.equals("act", key)) {
				params.put(key, data.get(key));
				continue;
			}
			encodeParams.put(key, data.get(key));
		}
		params.put("json", RSAEncrypt.encrypt(GsonUtil.getInstance().toJson(encodeParams)));
		return params;
	}

	/**
	 * 加密字符串
	 */
	public static String encrptString(String text) {
		String data = "";
		if (CoreZygote.getRsaService() != null && !TextUtils.isEmpty(CoreZygote.getRsaService().getPublicKey())) {
			try {
//				FELog.i("--->>>>rsa;要加密1:" + text);
				data = RSAEncrypt.encrypt(text);
//				FELog.i("-->>>>rsa加密f：" + RSAEncrypt.decrypt(data,RSAEncrypt.getPrivateKey(privateKey)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return TextUtils.isEmpty(data) ? text : data;
	}

	/**
	 * 加密参数型请求
	 */
	public static FormBody.Builder encrptParams(final Map<String, String> params) {
		if (params == null || params.isEmpty()) return new FormBody.Builder();
		Map<String, String> data = null;
		try {
			data = encrptByPublicKey(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (data == null) data = params;
		FormBody.Builder builder = new FormBody.Builder();
		Set<String> keys = data.keySet();
		for (String key : keys) {
			String value = data.get(key);
			builder.add(key, value);
		}
		return builder;
	}
}
