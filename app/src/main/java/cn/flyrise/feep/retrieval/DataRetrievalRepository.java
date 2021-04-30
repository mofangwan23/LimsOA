package cn.flyrise.feep.retrieval;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.retrieval.protocol.RetrievalTypeRequest;
import cn.flyrise.feep.retrieval.protocol.RetrievalTypeResponse;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import cn.flyrise.feep.retrieval.vo.RetrievalType;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2018-04-28 17:20
 */
public class DataRetrievalRepository {

	private static Class<? extends IRetrievalServices> sServiceClass;
	private static List<RetrievalType> sRetrievalTypes;//聊天、联系人、全部消息
	private static List<RetrievalType> sSearchTypes;//显示搜索所有
	private IRetrievalServices mRetrievalService;

	public static void init(Class<? extends IRetrievalServices> serviceClass) {
		sServiceClass = serviceClass;
	}

	public Observable<RetrievalResults> execute(Context context, String keyword) {
		if (mRetrievalService == null) {
			mRetrievalService = newInstance();
			mRetrievalService.setContext(context);
		}

		if (CommonUtil.isEmptyList(sRetrievalTypes)) {
			return Observable.empty();
		}

		return mRetrievalService.execute(sRetrievalTypes, keyword);
	}

	private IRetrievalServices newInstance() {
		if (sServiceClass == null) {
			throw new NullPointerException("The IRetrievalService class is null, must call the init() before use.");
		}
		IRetrievalServices instance = null;
		try {
			instance = sServiceClass.newInstance();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return instance;
	}

	public IRetrievalServices getRetrievalService() {
		return mRetrievalService;
	}

	// 查询支持的搜索功能列表
	public Observable<List<RetrievalType>> obtainSearchTypes() {
		if (CommonUtil.nonEmptyList(sSearchTypes)) {
			return Observable.just(sSearchTypes);
		}
		return Observable.create(f -> {
			RetrievalTypeRequest request = new RetrievalTypeRequest();
			FEHttpClient.getInstance().post(request, new ResponseCallback<RetrievalTypeResponse>() {
				@Override public void onCompleted(RetrievalTypeResponse response) {
					if (response != null && TextUtils.equals(response.getErrorCode(), "0")) {
						sSearchTypes = response.searchResults;
						List<RetrievalType> typs = new ArrayList<>();
						for (RetrievalType item : response.searchResults) {
							if (item.getRetrievalType() == RetrievalType.TYPE_CONTACT) {
								typs.add(item);
							}
							else if (item.getRetrievalType() == RetrievalType.TYPE_GROUP) {
								typs.add(item);
							}
							else if (item.getRetrievalType() == RetrievalType.TYPE_CHAT) {
								typs.add(item);
							}
						}
						sRetrievalTypes = typs;
						typs.add(new RetrievalType("2010", ""));
					}
					f.onNext(sSearchTypes);
					f.onCompleted();
				}

				@Override public void onFailure(RepositoryException repositoryException) {
					f.onError(repositoryException.exception());
					f.onCompleted();
				}
			});
		});
	}

	List<RetrievalType> getRetrievalTypes() {
		return sRetrievalTypes;
	}
}