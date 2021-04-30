package cn.flyrise.feep.core.network;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.services.ILoginUserServices;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author ZYP
 * @since 2018-02-28 15:38
 */
public class TokenInterceptor implements Interceptor {

	@Override public Response intercept(Chain chain) throws IOException {
		ILoginUserServices userServices = CoreZygote.getLoginUserServices();
		Request orginRequest = chain.request();
		if (userServices == null) {
			return chain.proceed(orginRequest);
		}

		String accessToken = userServices.getAccessToken();
		if (TextUtils.isEmpty(accessToken)) {
			return chain.proceed(orginRequest);
		}

		Request newRequest = orginRequest.newBuilder()
				.addHeader("token", accessToken)
				.build();
		return chain.proceed(newRequest);
	}
}
