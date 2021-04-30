package cn.flyrise.feep.email.utils;

import android.text.TextUtils;
import android.webkit.CookieManager;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author ZYP
 * @since 2017-11-07 10:09
 */
public class EmailAttachmentCleaner {

	private final List<NetworkAttachment> mToDeleteAttachments;
	private final Executor mExecutor;

	public EmailAttachmentCleaner(List<NetworkAttachment> attachments) {
		mToDeleteAttachments = attachments;
		mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
	}

	public void executeDelete(final String serverAddress) {
		for (NetworkAttachment attachment : mToDeleteAttachments) {
			mExecutor.execute(() -> {
				try {
					URL url = new URL(serverAddress + "/MailUploadFile");
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("POST");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					connection.setDoInput(true);
					connection.setDoOutput(true);
					connection.setRequestProperty("User-agent",CoreZygote.getUserAgent());
					CookieManager cookieManager = CookieManager.getInstance();
					String cookie = cookieManager.getCookie(serverAddress);
					connection.setRequestProperty("Cookie", cookie);

					ILoginUserServices userServices = CoreZygote.getLoginUserServices();
					if (userServices != null && !TextUtils.isEmpty(userServices.getAccessToken())) {
						connection.setRequestProperty("token", userServices.getAccessToken());
					}

					StringBuffer sb = new StringBuffer();
					sb.append("attachPK=").append(URLEncoder.encode(attachment.su00, "UTF-8")).append("&");
					sb.append("actionType=").append("deleteAttachment");
					connection.getOutputStream().write(sb.toString().getBytes());
					int responseCode = connection.getResponseCode();
					FELog.i("Delete Result = " + responseCode);
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			});
		}
	}
}
