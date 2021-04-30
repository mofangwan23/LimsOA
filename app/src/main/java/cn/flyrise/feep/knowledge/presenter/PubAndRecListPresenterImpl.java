package cn.flyrise.feep.knowledge.presenter;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.knowledge.contract.KnowBaseContract;
import cn.flyrise.feep.knowledge.contract.PubAndRecListContract;
import cn.flyrise.feep.knowledge.model.PubAndRecFile;
import cn.flyrise.feep.knowledge.repository.PubAndRecRepository;

/**
 * Created by klc
 */

public class PubAndRecListPresenterImpl<T> implements PubAndRecListContract.Presenter {

    private int mNowPage;
    private int mTotalPage;

    private int mListType;
    private String msgID;
    private PubAndRecListContract.View mView;
    private PubAndRecRepository mRepository;


    public PubAndRecListPresenterImpl(int mListType, PubAndRecListContract.View view) {
        this.mListType = mListType;
        this.mView = view;
        this.mRepository = new PubAndRecRepository();
    }

    public PubAndRecListPresenterImpl(String msgID, PubAndRecListContract.View mView) {
        this.msgID = msgID;
        this.mView = mView;
        this.mRepository = new PubAndRecRepository();
    }

    @Override
    public void refreshList() {
        mView.showRefreshLoading(true);
        if (TextUtils.isEmpty(msgID))
            mRepository.getPubOrRecList(mListType, mNowPage = 1, loadListCallback);
        else {
            mNowPage = 1;
            mRepository.getRecListForMsgID(msgID, loadListCallback);
        }
    }


    private PubAndRecListContract.LoadListCallback loadListCallback = new PubAndRecListContract.LoadListCallback<T>() {
        @Override
        public void loadListDataSuccess(List dataList, int totalPage) {
            mView.showRefreshLoading(false);
            mTotalPage = totalPage;
            mView.setCanPullUp(hasMore());
            mView.refreshListData(dataList);
        }

        @Override
        public void loadListDataError() {
            mView.showRefreshLoading(false);
            mView.setEmptyView();
        }
    };


    @Override
    public void loadMoreData() {
        mRepository.getPubOrRecList(mListType, ++mNowPage, new PubAndRecListContract.LoadListCallback() {
            @Override
            public void loadListDataSuccess(List dataList, int totalPage) {
                    mTotalPage = totalPage;
                    mView.loadMoreListData(dataList);
                    mView.setCanPullUp(hasMore());
            }

            @Override
            public void loadListDataError() {
                mView.loadMoreListFail();
                mNowPage--;
            }
        });
    }

    @Override
    public void cancelPublish(List<PubAndRecFile> dataList) {
        dataList = getSelectItem(dataList);
        StringBuilder selectId = new StringBuilder();
        for (PubAndRecFile item : dataList) {
            selectId.append("'").append(item.publishid).append("',");
        }
        mView.showConfirmDialog(R.string.know_cancel_publish, dialog -> {
            mView.showDealLoading(true);
            mRepository.cancelPublish(selectId.toString(), new KnowBaseContract.DealWithCallBack() {
                @Override
                public void success() {
                    mView.showMessage(R.string.know_cancel_publish_success);
                    mView.showDealLoading(false);
                    mView.dealComplete();
                }

                @Override
                public void fail() {
                    mView.showMessage(R.string.know_cancel_publish_error);
                    mView.showDealLoading(false);
                }
            });
        });

    }

    @Override
    public boolean hasMore() {
        return mNowPage < mTotalPage;
    }


    private List<PubAndRecFile> getSelectItem(List<PubAndRecFile> dataList) {
        List<PubAndRecFile> choiceList = new ArrayList<>();
        for (PubAndRecFile item : dataList) {
            if (item.isChoice)
                choiceList.add(item);
        }
        return choiceList;
    }
}
