package cn.flyrise.feep.email.presenter;

import static cn.flyrise.android.protocol.entity.EmailReplyRequest.B_REPLY;
import static cn.flyrise.android.protocol.entity.EmailReplyRequest.B_TRANSMIT;
import static cn.flyrise.android.protocol.model.EmailNumber.DRAFT;
import static cn.flyrise.android.protocol.model.EmailNumber.INBOX;
import static cn.flyrise.android.protocol.model.EmailNumber.SENT;

import android.app.Activity;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.AttachmentUpdateRequest;
import cn.flyrise.android.protocol.entity.CommonResponse;
import cn.flyrise.android.protocol.entity.EmailReplyRequest;
import cn.flyrise.android.protocol.entity.EmailReplyResponse;
import cn.flyrise.android.protocol.entity.EmailSendDoRequest;
import cn.flyrise.android.protocol.model.Accessory;
import cn.flyrise.android.protocol.model.EmailNumber;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.SelectedPerson;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.email.utils.EmailAttachmentCleaner;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2016/7/20 09:30
 */
public class NewReplyPresenter {

	private boolean boxNameWithEmail;
	private NewReplyView mNewReplyView;
	private String mMailId;
	private String mBoxName;
	private String mTransmit;
	private String mMailAccount;

	private List<String> mSelectedLocalAttachments;
	private List<NetworkAttachment> mSelectedNetworkAttachments;

	public NewReplyPresenter(String mailId, String boxName, String transmit, String mailAccount, NewReplyView view) {
		this.mMailId = mailId;
		this.mBoxName = boxName;
		this.mTransmit = transmit;
		this.mMailAccount = mailAccount;
		this.mNewReplyView = view;

		if (!TextUtils.isEmpty(mMailAccount) && TextUtils.equals(mBoxName, INBOX)) {
			mBoxName = mBoxName + "/" + mMailAccount;
			boxNameWithEmail = true;
		}
	}

	public void start() {
		if (isNewMail()) {
			mNewReplyView.setTitle(CommonUtil.getString(R.string.lbl_message_title_new_mail));
			return;
		}
		else if (TextUtils.equals(mBoxName, SENT)) {
			mNewReplyView.setTitle(CommonUtil.getString(R.string.lbl_message_title_send_again));
		}
		else if (TextUtils.equals(mTransmit, B_REPLY)) {
			mNewReplyView.setTitle(CommonUtil.getString(R.string.lbl_message_title_reply_mail));
		}
		else if (TextUtils.equals(mTransmit, B_TRANSMIT)) {
			mNewReplyView.setTitle(CommonUtil.getString(R.string.lbl_message_title_copyto_mail));
		}
		else if (TextUtils.equals(mBoxName, DRAFT)) {
			mNewReplyView.setTitle(CommonUtil.getString(R.string.lbl_message_title_edit_mail));
		}
		else {
			mNewReplyView.setTitle(CommonUtil.getString(R.string.lbl_message_title_new_mail));
		}
		loadReplyData();
	}

	private void loadReplyData() {
		mNewReplyView.showLoading();
		EmailReplyRequest replyRequest = new EmailReplyRequest(mBoxName, mMailId);
		replyRequest.bTransmit = this.mTransmit;

		if (!TextUtils.isEmpty(mMailAccount) && mMailAccount.contains("@")) {
			replyRequest.mailname = mMailAccount;
		}

		FEHttpClient.getInstance().post(replyRequest, new ResponseCallback<EmailReplyResponse>(mNewReplyView) {
			@Override public void onCompleted(EmailReplyResponse responseContent) {
				mNewReplyView.hideLoading();
				mNewReplyView.onLoadReplyDataSuccess(responseContent);
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				mNewReplyView.hideLoading();
				mNewReplyView.onLoadReplyDataFailed(repositoryException);
			}
		});
	}

	public void sendMail(final EmailSendDoRequest request, final String operator) {
		mNewReplyView.showLoading();
		EmailSendDoRequest getGuidRequest = new EmailSendDoRequest();
		getGuidRequest.operator = EmailSendDoRequest.OPERATOR_GET;

		FEHttpClient.getInstance().post(getGuidRequest, new ResponseCallback<CommonResponse>(mNewReplyView) {
			@Override public void onCompleted(CommonResponse responseContent) {
				// 1. 获取附件 GUID
				final String guid = responseContent.guid;
				if (TextUtils.isEmpty(guid)) {
					mNewReplyView.onGetMailGUIDFail(CommonUtil.getString(R.string.lbl_text_mail_send_failed));
					return;
				}
				request.sa01 = guid;
				request.operator = operator;

				// 2. 上传附件，如果存在的话
				FileRequest fileRequest = buildFileRequest(guid);
				if (fileRequest != null) {
					mNewReplyView.hideLoading();
					uploadEmailAttachment(fileRequest, request);
				}
				else {
					// 3. 发送邮件内容
					sendEmailContent(request);
				}
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				mNewReplyView.hideLoading();
				mNewReplyView.onSendEmailFailed(repositoryException, NewReplyView.STAGE_GET_GUID);
			}
		});
	}

	/**
	 * 转发/回复/草稿 已经存在 guid(sa01) 的情况下，不用再次获取 guid .
	 * @param sendRequest 已包含 guid 跟 operator 的请求.
	 */
	public void sendMail(EmailSendDoRequest sendRequest) {
		mNewReplyView.showLoading();
		FileRequest fileRequest = buildFileRequest(sendRequest.sa01);
		if (fileRequest != null) {
			mNewReplyView.hideLoading();
			uploadEmailAttachment(fileRequest, sendRequest);
		}
		else {
			sendEmailContent(sendRequest);
		}
	}

	private void uploadEmailAttachment(FileRequest fileRequest, final EmailSendDoRequest request) {
		new UploadManager((Activity) mNewReplyView)
				.fileRequest(fileRequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override public void onPreExecute() {
						mNewReplyView.showLoading();
					}

					@Override public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
						int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
						mNewReplyView.onUploadAttachmentProgress(progress);
					}
				})
				.responseCallback(new ResponseCallback<ResponseContent>() {
					@Override public void onCompleted(ResponseContent responseContent) {
						mNewReplyView.showLoading();
						sendEmailContent(request);
					}

					@Override public void onFailure(RepositoryException repositoryException) {
						mNewReplyView.hideLoading();
						mNewReplyView.onUploadAttachmentFailed(repositoryException);
					}
				})
				.execute();
	}

	private void sendEmailContent(EmailSendDoRequest request) {
		FEHttpClient.getInstance().post(request, new ResponseCallback<CommonResponse>(mNewReplyView) {
			@Override public void onCompleted(CommonResponse responseContent) {
				mNewReplyView.hideLoading();
				mNewReplyView.onSendEmailSuccess(responseContent);
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				mNewReplyView.hideLoading();
				mNewReplyView.onSendEmailFailed(repositoryException, NewReplyView.STAGE_SEND_MAIL);
			}
		});

	}

	private FileRequest buildFileRequest(String guid) {
		if (CommonUtil.isEmptyList(mSelectedLocalAttachments)
				&& CommonUtil.isEmptyList(mSelectedNetworkAttachments)) {
			return null;
		}

		FileRequest fileRequest = new FileRequest();

		AttachmentUpdateRequest attachmentRequest = new AttachmentUpdateRequest();
		attachmentRequest.attachmentGUID = guid;
		attachmentRequest.UpdateType = "mail";
		fileRequest.setRequestContent(attachmentRequest);

		FileRequestContent fileRequestContent = new FileRequestContent();
		fileRequestContent.setAttachmentGUID(guid);
		fileRequestContent.setUpdateType("mail");
		fileRequestContent.setFiles(mSelectedLocalAttachments);
		fileRequest.setFileContent(fileRequestContent);
		return fileRequest;
	}

	public String getBoxName() {
		if (boxNameWithEmail) {
			return EmailNumber.INBOX;
		}
		else {
			return this.mBoxName;
		}
	}

	public boolean isNewMail() {
		return this.mMailId == null;
	}

	public boolean isDraft() {
		return TextUtils.equals(mBoxName, DRAFT);
	}

	public boolean isSent() {
		return TextUtils.equals(mBoxName, SENT);
	}

	public void buildFileInfo(List<Accessory> accessories) {
		if (mSelectedNetworkAttachments == null) {
			mSelectedNetworkAttachments = new ArrayList<>(accessories.size());
		}
		for (Accessory accessory : accessories) {
			NetworkAttachment networkAttachment = new NetworkAttachment();
			networkAttachment.name = accessory.title;
			networkAttachment.size = 0;
			networkAttachment.attachPK = accessory.attachPK;
			networkAttachment.su00 = accessory.SA00;
			networkAttachment.path = CoreZygote.getLoginUserServices().getServerAddress() +
					"/servlet/mobileAttachmentServlet?mailAttachment=1&attachPK=" + accessory.attachPK;
			networkAttachment.setId(mMailId + "_" + accessory.accid);
			networkAttachment.type = FileCategoryTable.getType(accessory.title);
			mSelectedNetworkAttachments.add(networkAttachment);
		}
	}

	public List<AddressBook> buildDefaultRecipients(String ids, String names, List<SelectedPerson> defaultPersons) {
		List<SelectedPerson> selectedPersons = buildRecipients(ids, names, defaultPersons);
		if (CommonUtil.isEmptyList(selectedPersons)) {
			return null;
		}

		List<AddressBook> addressBooks = new ArrayList<>();
		for (SelectedPerson person : selectedPersons) {
			AddressBook addressBook = new AddressBook();
			addressBook.userId = person.userId;
			addressBook.name = person.userName;
			addressBooks.add(addressBook);
		}
		return addressBooks;
	}

	private List<SelectedPerson> buildRecipients(String ids, String names, List<SelectedPerson> defaultPersons) {
		if (TextUtils.equals(mTransmit, B_REPLY)) {
			return CommonUtil.isEmptyList(defaultPersons) ? new ArrayList<>() : defaultPersons;
		}
		List<SelectedPerson> persons = buildRecipientLists(ids, names);
		if (CommonUtil.isEmptyList(persons)) {
			persons = new ArrayList<>();
		}

		if (!CommonUtil.isEmptyList(defaultPersons)) {
			for (SelectedPerson person : defaultPersons) {
				if (TextUtils.equals(person.userId, ids)) continue;
				persons.add(person);
			}
		}
		return persons;
	}


	public static ArrayList<SelectedPerson> buildRecipientLists(String recipientIds, String recipientNames) {
		ArrayList<SelectedPerson> persons = null;
		if (!TextUtils.isEmpty(recipientIds) && !TextUtils.isEmpty(recipientNames)) {
			persons = new ArrayList<>();
			String[] ids = recipientIds.split(",");
			String[] names = recipientNames.split(",");
			for (int i = 0, len = ids.length; i < len; i++) {
				SelectedPerson recipient = new SelectedPerson();
				recipient.userId = ids[i];
				recipient.userName = names[i];
				persons.add(recipient);
			}
		}
		return persons;
	}

	public void addLocalAttachments(List<String> localAttachments) {
		if (mSelectedLocalAttachments == null) {
			mSelectedLocalAttachments = new ArrayList<>();
		}

		mSelectedLocalAttachments.clear();
		mSelectedLocalAttachments.addAll(localAttachments);
	}

	public List<String> getSelectedLocalAttachments() {
		return mSelectedLocalAttachments;
	}

	public void addNetworkAttachments(List<NetworkAttachment> networkAttachments) {
		if (mSelectedNetworkAttachments == null) {
			mSelectedNetworkAttachments = new ArrayList<>();
			mSelectedNetworkAttachments.addAll(networkAttachments);
			return;
		}
		// 检查一下哪些是不在 mSelectedNetworkAttachment 中的
		List<NetworkAttachment> tempDeleteFiles = new ArrayList<>();
		for (NetworkAttachment attachment : mSelectedNetworkAttachments) {
			if (networkAttachments.contains(attachment)) {
				continue;
			}

			tempDeleteFiles.add(attachment);
		}

		if (tempDeleteFiles.isEmpty()) {    // 原先是什么鸟样就什么样
			return;
		}

		String host = CoreZygote.getLoginUserServices().getServerAddress();
		new EmailAttachmentCleaner(tempDeleteFiles).executeDelete(host);    // 调用 api 删除已经被干掉的附件
		mSelectedNetworkAttachments.removeAll(tempDeleteFiles);             // 移除掉已经被删除的部分

	}

	public List<NetworkAttachment> getSelectedNetworkAttachments() {
		return mSelectedNetworkAttachments;
	}

	public int getSelectedAttachmentSize() {
		int totalCount = 0;
		if (CommonUtil.nonEmptyList(mSelectedLocalAttachments)) {
			totalCount += mSelectedLocalAttachments.size();
		}

		if (CommonUtil.nonEmptyList(mSelectedNetworkAttachments)) {
			totalCount += mSelectedNetworkAttachments.size();
		}
		return totalCount;
	}

}
