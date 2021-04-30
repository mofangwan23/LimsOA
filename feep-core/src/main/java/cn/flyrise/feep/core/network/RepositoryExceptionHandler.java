package cn.flyrise.feep.core.network;


import android.text.TextUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.listener.OnNetworkExceptionListener;

/**
 * @author ZYP
 * @since 2016-09-12 16:15 以组合模式的形式，负责数据层异常的处理
 */
public final class RepositoryExceptionHandler {

	private OnNetworkExceptionListener mNetworkExceptionListener;

	public void setOnNetworkExceptionListener(OnNetworkExceptionListener listener) {
		this.mNetworkExceptionListener = listener;
	}

	public void handleRemoteException(RepositoryException repositoryException) {
		if (repositoryException == null) {
			return;
		}
		if (repositoryException.isReLogin()) {                                                      // 判断是否需要重新登录
			String errorMessage = repositoryException.errorMessage();
			if (mNetworkExceptionListener != null) {
				mNetworkExceptionListener.onNetworkException(true, repositoryException.isLoadLogout(), errorMessage);
			}
			return;
		}

		if (repositoryException.errorCode() == RemoteException.CODE_REQUEST_CANCEL) {
			if (mNetworkExceptionListener != null) {
				mNetworkExceptionListener.onNetworkException(false, repositoryException.isLoadLogout(), null);
			}
			return;
		}

		String errorMessage = null;
		Exception exception = repositoryException.exception();
		if (exception != null) {
			if (exception instanceof ConnectException) {                                           // 连接服务器失败，可能是没联网
				errorMessage = CommonUtil.getString(R.string.core_http_failure);

			}
			else if (exception instanceof SocketTimeoutException) {                                 // 连接超时，可能网络太渣
				errorMessage = CommonUtil.getString(R.string.core_http_timeout);
			}
		}

		if (!TextUtils.isEmpty(errorMessage)) {
			if (mNetworkExceptionListener != null) {
				mNetworkExceptionListener.onNetworkException(false, repositoryException.isLoadLogout(), errorMessage);
			}
			return;
		}

		int responseCode = repositoryException.errorCode();
		if (responseCode != -1) {
			errorMessage = CommonUtil.getString(R.string.core_http_network_exception) + " : " + responseCode;
		}
		else if (!TextUtils.isEmpty(repositoryException.errorMessage())) {
			errorMessage = repositoryException.errorMessage();
		}
		else {
			errorMessage = CommonUtil.getString(R.string.core_http_network_exception);
		}

		if (mNetworkExceptionListener != null) {
			mNetworkExceptionListener.onNetworkException(false, repositoryException.isLoadLogout(), errorMessage);
		}

	}
}