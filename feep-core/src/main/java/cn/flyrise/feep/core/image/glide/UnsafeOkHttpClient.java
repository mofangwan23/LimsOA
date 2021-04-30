package cn.flyrise.feep.core.image.glide;

import android.annotation.SuppressLint;
import cn.flyrise.feep.core.network.TokenInterceptor;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;

/**
 * Create by cm132 on 2019/9/19 16:12.
 * Describe:网络配置
 */
public class UnsafeOkHttpClient {

	public static OkHttpClient getUnsafeOkHttpClient() {
		try {
			final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
				@SuppressLint("TrustAllX509TrustManager") @Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
				}

				@SuppressLint("TrustAllX509TrustManager") @Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[]{};
				}
			}
			};        // Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier((hostname, session) -> true);

			builder.addInterceptor(new TokenInterceptor());
			builder.addNetworkInterceptor(new StethoInterceptor());

			builder.connectTimeout(20, TimeUnit.SECONDS);
			builder.readTimeout(20, TimeUnit.SECONDS);

			return builder.build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
