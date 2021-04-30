package cn.flyrise.feep.knowledge.repository;


import cn.flyrise.android.protocol.entity.knowledge.SearchFileRequest;
import cn.flyrise.android.protocol.entity.knowledge.SearchFileResponse;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.knowledge.contract.SearchListContract;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by KLC on 2016/12/6.
 */

public class SearchRepository {

    private int mFolderType;

    public SearchRepository() { }

    public SearchRepository(int folderType) {
        this.mFolderType = folderType;
    }

    public void loadListData(String key, int nowPage, SearchListContract.LoadListCallback callback) {
        if (mFolderType == 0) {
            mFolderType = KnowKeyValue.FOLDERTYPE_PERSON;
        }
        this.searchKnowledges(key, mFolderType, nowPage, callback);
    }

    public void searchKnowledges(String key, int folderType, int nowPage, SearchListContract.LoadListCallback callback) {
        final SearchFileRequest request = new SearchFileRequest(key, nowPage, KnowKeyValue.LOADPAGESIZE, folderType);
        FEHttpClient.getInstance().post(request, new ResponseCallback<SearchFileResponse>() {
            @Override
            public void onCompleted(SearchFileResponse searchFileResponse) {
                SearchFileResponse.Result result = searchFileResponse.getResult();
                if (result == null || result.getDoc() == null) {
                    callback.loadListDataError();
                }
                else {
                    callback.loadListDataSuccess(result.getDoc(), result.getNumFound());
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callback.loadListDataError();
            }
        });
    }

    public void cancelSearchRequest() {
        FEHttpClient.getInstance().cancelCall(SearchFileRequest.NAMESPACE);
    }
}