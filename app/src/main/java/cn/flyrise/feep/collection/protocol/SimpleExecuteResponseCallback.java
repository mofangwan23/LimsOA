package cn.flyrise.feep.collection.protocol;

import android.text.TextUtils;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collection.bean.ExecuteResult;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-23 10:12
 */
public class SimpleExecuteResponseCallback extends ResponseCallback<ResponseContent> {


	private Subscriber<? super ExecuteResult> subscriber;

	public SimpleExecuteResponseCallback(Subscriber<? super ExecuteResult> f) {
		this.subscriber = f;
	}

	@Override public void onCompleted(ResponseContent response) {
		if (response != null && TextUtils.equals(response.getErrorCode(), "0")) {
			subscriber.onNext(ExecuteResult.successResult());
			return;
		}

		if (response == null) {
			subscriber.onNext(packageResultWithException(null));
			return;
		}

		int errorCode = CommonUtil.parseInt(response.getErrorCode());
		String errorMessage = TextUtils.isEmpty(response.getErrorMessage())
				? CommonUtil.getString(R.string.lbl_retry_operator)
				: response.getErrorMessage();
		subscriber.onNext(ExecuteResult.errorResult(errorCode, errorMessage));
	}

	@Override public void onFailure(RepositoryException repositoryException) {
		subscriber.onNext(packageResultWithException(repositoryException));
	}

	private ExecuteResult packageResultWithException(RepositoryException exception) {
		int errorCode;
		String errorMessage;
		if (exception == null) {
			errorCode = -1;
			errorMessage = CommonUtil.getString(R.string.lbl_retry_operator);
		}
		else {
			errorCode = exception.errorCode();
			errorMessage = TextUtils.isEmpty(exception.errorMessage())
					? CommonUtil.getString(R.string.lbl_retry_operator)
					: exception.errorMessage();
		}

		return ExecuteResult.errorResult(errorCode, errorMessage);
	}

}
