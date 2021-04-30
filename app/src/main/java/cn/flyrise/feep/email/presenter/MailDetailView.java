package cn.flyrise.feep.email.presenter;

import cn.flyrise.android.protocol.entity.CommonResponse;
import cn.flyrise.android.protocol.entity.EmailDetailsResponse;
import cn.flyrise.feep.core.network.RepositoryException;

/**
 * @author ZYP
 * @since 2016/7/21 16:47
 */
public interface MailDetailView extends BaseMailView {

    int getPaddingTop();

    void onLoadDetailMailSuccess(EmailDetailsResponse rsp);

    void onLoadDetailMailFailed(RepositoryException repositoryException);

    void onSendDraftSuccess(CommonResponse rsp);

    void onSendDraftFailed(RepositoryException repositoryException);

    void checkDraftFailed(String errorMessage);

    void displayInboxFooter();

    void displayDraftFooter();

    void displaySentFooter();

    void displayTrashFooter();

    void onDealWithActionSuccess(int action, String content);

    void onDealWithActionFailed(int action, RepositoryException repositoryException);
}
