package cn.flyrise.feep.email.presenter;

import cn.flyrise.android.protocol.entity.CommonResponse;
import cn.flyrise.android.protocol.entity.EmailReplyResponse;
import cn.flyrise.feep.core.network.RepositoryException;

/**
 * @author ZYP
 * @since 2016/7/20 10:34
 */
public interface NewReplyView extends BaseMailView {

    int STAGE_GET_GUID = 1;
    int STAGE_UPLOAD_ATTACHMENT = 2;
    int STAGE_SEND_MAIL = 3;

    void setTitle(String title);

    void onLoadReplyDataSuccess(EmailReplyResponse rsp);

    void onLoadReplyDataFailed(RepositoryException repository);

    void onSendEmailSuccess(CommonResponse rsp);

    void onGetMailGUIDFail(String errorMessage);

    void onSendEmailFailed(RepositoryException repository, int stage);

    void onUploadAttachmentProgress(int progress);

    void onUploadAttachmentFailed(RepositoryException repository);

}
