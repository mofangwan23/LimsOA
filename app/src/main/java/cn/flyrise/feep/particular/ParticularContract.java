package cn.flyrise.feep.particular;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.protocol.model.Reply;
import cn.flyrise.android.protocol.model.SupplyContent;
import cn.flyrise.android.protocol.model.TrailContent;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.news.bean.RelatedNews;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-10-20 11:00
 */
public interface ParticularContract {

	interface IPresenter {

		void start();

		void fetchUserDetailInfo(String userId);

		void toolBarRightTextClick(android.view.View view);

		void clickToReply(String replyId);

		UploadManager executeBusinessReply(List<String> attachmentLists, String replyContent);

		UploadManager executeCommentReply(List<String> attachments, String replyContent, String replyId);

		void handleBottomButton1(android.view.View view);

		void handleBottomButton2(android.view.View view);

		void handleBottomButton3(android.view.View view);

		void handleBottomButton4(android.view.View view);

		void handleBackButton();

		ParticularIntent getParticularIntent();

		void handlePopMenu(int id, Context context);

		void addToFavoriteFolder(String collectionFolderId, String collectionFolderName);

	}

	interface IView {

		void showLoading();

		void showLoadingWithProgress(int progress);

		void dismissLoading(String text);

		void fetchDetailError(String errorMessage);

		void setToolBarTitle(String title);

		void configToolBarRightText(String rightText);

		void configFloatingActionButton(ParticularPresenter.FabVO fabVO);

		void displayHeadInformation(ParticularPresenter.HeadVO headVO);

		void configSendUserContactInfo(AddressBookItem addressBookItem);

		void configBottomMenu(ParticularPresenter.BottomMenuVO bottomMenuVO);

		void displayParticularContent(String content, boolean needSupplementStyle, String mobileFormUrl);

		void displayContentSupplement(List<SupplyContent> supplyContents);

		void displayContentModify(List<TrailContent> trailContents);

		void displayAttachment(List<NetworkAttachment> attachments);

		void displayOriginalReplyList(List<Reply> replies);

		void displayReplyList(List<Reply> replies, boolean showReplyButton);

		void displayRelatedNews(List<RelatedNews> relatedNews);

		void displayReplyView(boolean withAttachment, String replyId, String btnText);

		void replySuccess();

		void startIntent(Intent intent);

		void finishViewWithResult(Intent result);

		Context getContext();

		void showMeetingAttendUserInfo(android.view.View v, ParticularPresenter.MeetingAttendUserVO attendUserVO);

		void showCollaborationMenu(View view, List<Integer> permissions);

		void showWorkPlanMenu(View view);

		void showConfirmDialog(String message, FEMaterialDialog.OnClickListener onClickListener);

		void isMettingReply();

	}

}
