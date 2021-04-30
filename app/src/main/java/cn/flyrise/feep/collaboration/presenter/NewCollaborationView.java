package cn.flyrise.feep.collaboration.presenter;

import cn.flyrise.feep.collaboration.model.Collaboration;

/**
 * Created by klc on 2017/4/26.
 */

public interface NewCollaborationView {

    void showLoading();

    void showProgress(int progress);

    void hideLoading();

    void displayView(Collaboration collaboration);

    void setTitle(int resId);

    void setFileTextCount(int count);

    void setHasFlow(boolean hasFlow);

    void setAssociationCount(int count);

    void setImportValue(String value);

    void hideSaveButton();

    void showImportDialog(String[] value);

    void finish();
}
