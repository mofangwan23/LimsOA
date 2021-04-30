package cn.flyrise.feep.collection;

import android.text.TextUtils;
import cn.flyrise.feep.collection.bean.ExecuteResult;
import cn.flyrise.feep.collection.bean.FavoriteData;
import cn.flyrise.feep.collection.bean.FavoriteFolder;
import cn.flyrise.feep.collection.protocol.FavoriteAddRequest;
import cn.flyrise.feep.collection.protocol.FavoriteFolderListResponse;
import cn.flyrise.feep.collection.protocol.FavoriteFolderRequest;
import cn.flyrise.feep.collection.protocol.FavoriteListRequest;
import cn.flyrise.feep.collection.protocol.FavoriteListResponse;
import cn.flyrise.feep.collection.protocol.FavoriteRemoveRequest;
import cn.flyrise.feep.collection.protocol.SimpleExecuteResponseCallback;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import java.util.Collections;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2018-05-21 10:28
 */
public class FavoriteRepository {

	// 1.新增收藏夹
	public Observable<ExecuteResult> createFolder(String name) {
		return Observable.create(f -> {
			final FavoriteFolderRequest request = FavoriteFolderRequest.requestCreateFolder(name);
			FEHttpClient.getInstance().post(request, new SimpleExecuteResponseCallback(f));
		});
	}

	// 2.修改收藏夹
	public Observable<ExecuteResult> updateFolder(String folderId, String name) {
		return Observable.create(f -> {
			final FavoriteFolderRequest request = FavoriteFolderRequest.requestUpdateFolder(folderId, name);
			FEHttpClient.getInstance().post(request, new SimpleExecuteResponseCallback(f));
		});
	}

	// 3.删除收藏夹
	public Observable<ExecuteResult> deleteFolder(String folderId) {
		return Observable.create(f -> {
			final FavoriteFolderRequest request = FavoriteFolderRequest.requestDeleteFolder(folderId);
			FEHttpClient.getInstance().post(request, new SimpleExecuteResponseCallback(f));
		});
	}

	// 4.获取所有收藏夹
	public Observable<List<FavoriteFolder>> queryAllCollectionFolders() {
		return Observable.create(f -> {
			final FavoriteFolderRequest request = FavoriteFolderRequest.requestFavoriteFolderList();
			FEHttpClient.getInstance().post(request, new ResponseCallback<FavoriteFolderListResponse>() {
				@Override public void onCompleted(FavoriteFolderListResponse response) {
					if (response != null
							&& TextUtils.equals(response.getErrorCode(), "0")) {
						f.onNext(response.folders);
						return;
					}

					f.onNext(Collections.emptyList());
				}

				@Override public void onFailure(RepositoryException repositoryException) {
					FELog.e("FavoriteRepository query all collection failed. Error: " + repositoryException.exception().getMessage());
					f.onNext(Collections.emptyList());
				}
			});
		});
	}

	// 5.添加到指定收藏夹
	public Observable<ExecuteResult> addToFolder(String favoriteId,
			String businessId, String type, String title, String userId, String sendTime) {
		// This type is an addType.
		return Observable.create(f -> {
			final FavoriteAddRequest request = FavoriteAddRequest.newInstance(favoriteId, businessId, type, title, userId, sendTime);
			FEHttpClient.getInstance().post(request, new SimpleExecuteResponseCallback(f));
		});
	}

	// 6.从收藏夹中移除
	public Observable<ExecuteResult> removeFromFolder(String favoriteId, String businessId, String type) {
		// This type is an normal type
		return Observable.create(f -> {
			final FavoriteRemoveRequest request = FavoriteRemoveRequest.newInstance(favoriteId, businessId, type);
			FEHttpClient.getInstance().post(request, new SimpleExecuteResponseCallback(f));
		});
	}

	// 7.获取收藏夹下所有列表
	public Observable<FavoriteData> queryFavoriteFromFolder(String favoriteId, int page) {
		return Observable.create(f -> {
			final FavoriteListRequest request = FavoriteListRequest.create(page, favoriteId);
			FEHttpClient.getInstance().post(request, new ResponseCallback<FavoriteListResponse>() {
				@Override public void onCompleted(FavoriteListResponse response) {
					if (response != null && TextUtils.equals(response.getErrorCode(), "0")) {
						f.onNext(response.result);
						return;
					}

					f.onError(new RuntimeException("Empty data."));
				}

				@Override public void onFailure(RepositoryException exception) {
					FELog.e("FavoriteRepository queryFavoriteFromFolder exception. Error: " + exception.errorMessage());
					f.onError(new RuntimeException("Query failure."));
				}
			});
		});
	}

}
