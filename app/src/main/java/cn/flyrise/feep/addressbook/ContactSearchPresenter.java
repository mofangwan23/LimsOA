package cn.flyrise.feep.addressbook;

import android.os.Handler;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.addressbook.model.ContactQueryVO;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;

/**
 * @author ZYP
 * @since 2016-12-12 16:13
 */
public class ContactSearchPresenter implements ContactSearchContract.IPresenter {

    private ContactSearchContract.IView mContactSearchView;
    private Handler mHandler;

    private boolean isLoading;
    private int mCurrentPage, mTotalPage;

    public ContactSearchPresenter(ContactSearchContract.IView view, Handler handler) {
        this.mContactSearchView = view;
        this.mHandler = handler;
    }

    @Override public void executeQuery(String nameLike) {
        this.mCurrentPage = 0;
        this.queryContact(nameLike);
    }

    @Override public void loadMoreContact(String nameLike) {
        if (!isLoading && mCurrentPage < mTotalPage) {
            isLoading = true;
            mCurrentPage += 1;
            queryContact(nameLike);
        }
        else if (mCurrentPage >= mTotalPage) {
            mContactSearchView.removeFooterView();
        }
    }

    private void queryContact(String nameLike) {
        new Thread(() -> {
            final ContactQueryVO contactQueryVO = AddressBookRepository.get().queryContactByNameLike(nameLike,
                    mCurrentPage * AddressBookRepository.PAGE_MAX_COUNT);

            mHandler.post(() -> {                       // 切回 UI 线程，更新视图
                mTotalPage = contactQueryVO.totalPage;
                FELog.i("Total Page = " + mTotalPage);
                isLoading = false;
                if (mTotalPage > 1) {   // 需要分页
                    mContactSearchView.addFooterView();
                    if (mCurrentPage == 0) mContactSearchView.showContacts(contactQueryVO.contacts);
                    else mContactSearchView.addContacts(contactQueryVO.contacts);
                }
                else {
                    mCurrentPage = mTotalPage;
                    mContactSearchView.removeFooterView();
                    mContactSearchView.showContacts(contactQueryVO.contacts);
                }
            });
        }).start();
    }
}