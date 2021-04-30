package cn.flyrise.feep.core.network;

import java.util.Date;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * @author ZYP
 * @since 2017-11-21 10:32
 */
public class FEHttpHeaders {

	private Headers mHeaders;

	private static class Singleton {

		private static final FEHttpHeaders sHeaders = new FEHttpHeaders();
	}

	public static FEHttpHeaders getInstance() {
		return Singleton.sHeaders;
	}

	public void inject(Response response) {
		mHeaders = response == null ? null : response.headers();
	}

	public String getLatestDateString() {
		return mHeaders == null ? null : mHeaders.get("Date");
	}

	public Date getLatestDate() {
		return mHeaders == null ? null : mHeaders.getDate("Date");
	}


}
