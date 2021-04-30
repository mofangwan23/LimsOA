package cn.flyrise.feep.knowledge.presenter;

import cn.flyrise.feep.R;
import cn.flyrise.feep.knowledge.contract.KnowBaseContract;
import cn.flyrise.feep.knowledge.contract.PublicFileContract;
import cn.flyrise.feep.knowledge.repository.PublicRepository;

/**
 * Created by KLC on 2016/12/7.
 */

public class PublicFilePresenterImpl implements PublicFileContract.Presenter {

    private PublicFileContract.View mView;
    private String mPublicFileIDs;
    private String mFolderID;
    private PublicRepository mPublicRepository;

    public PublicFilePresenterImpl(PublicFileContract.View mView, String publicFileIDs, String folderID) {
        this.mView = mView;
        this.mPublicFileIDs = publicFileIDs;
        this.mFolderID = folderID;
        mPublicRepository = new PublicRepository();
    }

    @Override
    public void publicFile(String receiver, String publicUserID, String startTime, String endTime) {
        mView.showDealLoading(true);
        mPublicRepository.publishFile(mPublicFileIDs, receiver, publicUserID, mFolderID, startTime, endTime, new KnowBaseContract.DealWithCallBack() {
            @Override
            public void success() {
                mView.showDealLoading(false);
                mView.showMessage(R.string.know_publish_success);
                mView.publishSuccess();
            }

            @Override
            public void fail() {
                mView.showDealLoading(false);
                mView.showMessage(R.string.know_publish_error);
            }
        });
    }

}
