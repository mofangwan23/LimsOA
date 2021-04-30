package cn.flyrise.feep.core.network;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.ILoginUserServices;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Cookie;

/**
 * @author ZYP
 * @since 2018-02-28 15:54
 */
public class TokenInject {

	public static void injectToken(WebView webView, String url) {
		if (webView == null) {
			return;
		}
		setCookie(webView.getContext(), webView);
		setUserAgent(webView);
		ILoginUserServices userServices = CoreZygote.getLoginUserServices();
		if (userServices == null) {
			webView.loadUrl(url);
			return;
		}

		String accessToken = userServices.getAccessToken();
		if (TextUtils.isEmpty(accessToken)) {
			webView.loadUrl(url);
			return;
		}
		Map<String, String> headers = new HashMap<>();
		headers.put("token", accessToken);
		webView.loadUrl(url, headers);
	}

	private static void setUserAgent(WebView webView) {
		String ua = webView.getSettings().getUserAgentString();
		if (ua.contains(CoreZygote.getUserAgent())) {
			webView.getSettings().setUserAgentString(ua);
			return;
		}
		webView.getSettings().setUserAgentString(ua + CoreZygote.getUserAgent());
	}

	public static void setCookie(Context context, WebView webView) {
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			cookieManager.setAcceptThirdPartyCookies(webView, true);
		}
		if (CoreZygote.getLoginUserServices() != null) {
			List<Cookie> allCookies = FEHttpClient.getInstance().getAllCookies();
			if (CommonUtil.nonEmptyList(allCookies)) {
				String host = FEHttpClient.getInstance().getHost();
				for (Cookie cookie : allCookies) {
					String cookieString = cookie.name() + "=" + cookie.value() + "; domain=" + cookie.domain();
					cookieManager.setCookie(host, cookieString);
				}
			}
		}

		if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.getInstance().sync();
		}
		else {
			cookieManager.flush();
		}
	}
}
