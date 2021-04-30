package cn.flyrise.feep.email.presenter;


import android.os.Build;
import android.text.TextUtils;

import cn.flyrise.android.protocol.entity.BoxDetailRequest;
import cn.flyrise.android.protocol.entity.BoxDetailResponse;
import cn.flyrise.android.protocol.entity.CommonResponse;
import cn.flyrise.android.protocol.entity.EmailDetailsRequest;
import cn.flyrise.android.protocol.entity.EmailDetailsResponse;
import cn.flyrise.android.protocol.entity.EmailReplyRequest;
import cn.flyrise.android.protocol.entity.EmailReplyResponse;
import cn.flyrise.android.protocol.entity.EmailSendDoRequest;
import cn.flyrise.android.protocol.model.EmailNumber;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * @author ZYP
 * @since 2016/7/21 16:47
 */
public class MailDetailPresenter {

    public final static String IMAGE_CENTER = "<style type='text/css'>body{" +
            "line-height:1.5!important;" +
            "padding-left: 16px!important;padding-right: 16px!important;padding-top: 16px!important;" +
            "padding-bottom: 60px!important;margin:auto auto!important;}" +
            "img{width:100%!important;} " +
            "table{max-width:100%!important;} " +
            "div,span,p{word-wrap:break-word; word-break:break-all}</style>";
    public final static String TABLE_STYLE = "<style type='text/css'>table{border-collapse:collapse;border: 1px solid #000;} th, tr, td {border: 1px solid #000;}</style>";

    private boolean boxNameWithMail;
    private String mMailId;
    private String mBoxName;
    private String mMailAccount;
    private MailDetailView mMailDetailView;

    public MailDetailPresenter(String mailId, String boxName, String mailAccount, MailDetailView mailDetailView) {
        this.mMailId = mailId;
        this.mBoxName = boxName;
        if (!TextUtils.isEmpty(mailAccount) && TextUtils.equals(this.mBoxName, EmailNumber.INBOX)) {
            this.mBoxName = this.mBoxName + "/" + mailAccount;
            boxNameWithMail = true;
        }
        this.mMailAccount = mailAccount;
        this.mMailDetailView = mailDetailView;
    }

    public void start() {
        mMailDetailView.showLoading();
        EmailDetailsRequest request = new EmailDetailsRequest(mBoxName, mMailId);

        if (!TextUtils.isEmpty(mMailAccount) && mMailAccount.contains("@")) {
            request.mailName = mMailAccount;
            request.typeTrash = BoxDetailRequest.TYPE_TRASH;
        }

        FEHttpClient.getInstance().post(request, new ResponseCallback<EmailDetailsResponse>(mMailDetailView) {
            @Override
            public void onCompleted(EmailDetailsResponse responseContent) {
                initFooterBar();
                mMailDetailView.hideLoading();
                mMailDetailView.onLoadDetailMailSuccess(responseContent);
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                mMailDetailView.hideLoading();
                mMailDetailView.onLoadDetailMailFailed(repositoryException);
            }
        });
    }

    private void initFooterBar() {
        boolean noHandler = false;
        switch (mBoxName) {
            case EmailNumber.INBOX_INNER:    // 发件箱
                mMailDetailView.displayInboxFooter();
                break;
            case EmailNumber.SENT:          // 已发送
                mMailDetailView.displaySentFooter();
                break;
            case EmailNumber.DRAFT:         // 草稿箱
                mMailDetailView.displayDraftFooter();
                break;
            case EmailNumber.TRASH:         // 垃圾箱
                mMailDetailView.displayTrashFooter();
                break;
            default:
                noHandler = true;
                break;
        }

        if (noHandler && TextUtils.equals(mBoxName, EmailNumber.INBOX + "/" + mMailAccount)) {
            mMailDetailView.displayInboxFooter();
        }
    }

    public void sendDraft() {
        mMailDetailView.showLoading();
        EmailReplyRequest replyRequest = new EmailReplyRequest(mBoxName, mMailId);
        if (TextUtils.isEmpty(mMailAccount) && mMailAccount.contains("@")) {
            replyRequest.mailname = mMailAccount;
        }
        replyRequest.bTransmit = EmailReplyRequest.B_DRAFT;

        FEHttpClient.getInstance().post(replyRequest, new ResponseCallback<EmailReplyResponse>(mMailDetailView) {
            @Override
            public void onCompleted(EmailReplyResponse responseContent) {
                EmailSendDoRequest sendRequest = new EmailSendDoRequest();
                sendRequest.operator = EmailSendDoRequest.OPERATOR_DRAFT;
                sendRequest.sa01 = responseContent.guid;
                sendRequest.title = responseContent.title;
                sendRequest.content = responseContent.content;
                sendRequest.mailid = responseContent.mailId;
                sendRequest.mailname = responseContent.mailname;
                sendRequest.to = responseContent.su00;
                sendRequest.cc = responseContent.su00cc;
                sendRequest.bcc = responseContent.su00bcc;

                if (TextUtils.isEmpty(sendRequest.title)) {
                    mMailDetailView.hideLoading();
                    mMailDetailView.checkDraftFailed(CommonUtil.getString(R.string.lbl_text_mail_theme_not_empty));
                    return;
                }

                if (TextUtils.isEmpty(sendRequest.to)) {
                    mMailDetailView.hideLoading();
                    mMailDetailView.checkDraftFailed(CommonUtil.getString(R.string.lbl_text_mail_receiver_empty));
                    return;
                }

                FEHttpClient.getInstance().post(sendRequest, new ResponseCallback<CommonResponse>(mMailDetailView) {
                    @Override
                    public void onCompleted(CommonResponse responseContent) {
                        mMailDetailView.hideLoading();
                        mMailDetailView.onSendDraftSuccess(responseContent);
                    }

                    @Override
                    public void onFailure(RepositoryException repositoryException) {
                        mMailDetailView.hideLoading();
                        mMailDetailView.onSendDraftFailed(repositoryException);
                    }
                });
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                mMailDetailView.hideLoading();
            }
        });
    }

    public void dealWithAction(final int action, String operator) {
        mMailDetailView.showLoading();
        BoxDetailRequest request = buildBoxDetailRequest(operator);

        if (!TextUtils.isEmpty(mMailAccount) && mMailAccount.contains("@")) {
            request.mailname = mMailAccount;
            if (TextUtils.equals(operator, BoxDetailRequest.OPERATOR_DELETE)) {
                request.typeTrash = "2";
            }
        }

        FEHttpClient.getInstance().post(request, new ResponseCallback<BoxDetailResponse>(mMailDetailView) {
            @Override
            public void onCompleted(BoxDetailResponse responseContent) {
                mMailDetailView.hideLoading();
                mMailDetailView.onDealWithActionSuccess(action, CommonUtil.getString(R.string.message_operation_alert));
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                mMailDetailView.hideLoading();
                mMailDetailView.onDealWithActionFailed(action, repositoryException);
            }
        });
    }

    private BoxDetailRequest buildBoxDetailRequest(String operator) {
        BoxDetailRequest request = new BoxDetailRequest();
        request.pageNumber = 1;
        request.operator = operator;
        request.optMailList = mMailId;
        request.boxName = this.mBoxName;
        return request;
    }

    public String getMailId() {
        return this.mMailId;
    }

    public String getBoxName() {
        if (boxNameWithMail) {
            return EmailNumber.INBOX;
        } else {
            return this.mBoxName;
        }
    }

    public boolean isBoxNameWithMail() {
        return this.boxNameWithMail;
    }

    public String getMailAccount() {
        return this.mMailAccount;
    }

    public static String getHtml(String boxName, String content) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String url = CoreZygote.getLoginUserServices().getServerAddress();
            sb.append("<base href=\"").append(url).append("\">");
        }

        sb.append(IMAGE_CENTER);
        if (content.toLowerCase().contains("<table")
                || content.toLowerCase().contains("</table>")) {
            sb.append(TABLE_STYLE);
        }

        sb.append("</head>");
        sb.append("<body>");
        sb.append(content);
        sb.append("</body>");
        sb.append("</html>");
        FELog.i(sb.toString());
        return sb.toString();
    }

}
