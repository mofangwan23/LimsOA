package cn.flyrise.feep.location.bean;

import android.content.Context;
import cn.flyrise.android.protocol.entity.LocationLocusRequest;
import cn.flyrise.android.protocol.entity.LocationLocusResponse;
import cn.flyrise.feep.core.common.X.LocationType;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * 类描述：轨迹数据请求的工具类
 * @author 罗展健
 * @version 1.0
 */
public class LocusDataProvider {

	private OnLocationResponseListener responseListener;
	private Context context;

	public LocusDataProvider(Context context) {
		this.context = context;
	}

	public interface OnLocationResponseListener {

		void onSuccess(LocationLocusResponse responses, String locationType);

		void onFailed(Throwable error, String content);
	}

	/**
	 * 请求外勤通人员
	 */
	public void requestPerson() {
		final LocationLocusRequest request = new LocationLocusRequest();
		request.setRequestType(LocationType.Person);
		FEHttpClient.getInstance().post(request, createResponseCallback());
	}

	/**
	 * 请求外勤通轨迹
	 * @param userId 用户id，可以为null
	 * @param date 日期，可以为null
	 */
	public void requestLocus(String userId, String date) {
		final LocationLocusRequest request = new LocationLocusRequest();
		request.setRequestType(LocationType.Locus);
		request.setDate(date);
		request.setUserId(userId);
		FEHttpClient.getInstance().post(request, createResponseCallback());
	}


	private ResponseCallback<LocationLocusResponse> createResponseCallback() {
		return new ResponseCallback<LocationLocusResponse>(context) {
			@Override public void onCompleted(LocationLocusResponse locationLocusResponse) {
				try {
					if (locationLocusResponse == null) {
						if (responseListener != null) {
							responseListener.onFailed(null, "异常出错");
						}
						return;
					}

					if (responseListener != null) {
						responseListener.onSuccess(locationLocusResponse, locationLocusResponse.getRequestType());
					}

				} catch (final Exception e) {
					if (responseListener != null) {
						responseListener.onFailed(e, "异常出错");
					}
					e.printStackTrace();
				}
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				FEToast.showMessage(repositoryException.errorMessage());
			}
		};
	}

	/**
	 * 设置响应的监听器
	 */
	public void setResponseListener(OnLocationResponseListener responseListener) {
		this.responseListener = responseListener;
	}

	/**
	 * 请求工作时间
	 */
	public void requestWorkingTime() {
		final LocationLocusRequest request = new LocationLocusRequest();
		request.setRequestType(LocationType.WorkingTime);
		FEHttpClient.getInstance().post(request, createResponseCallback());
	}

}
