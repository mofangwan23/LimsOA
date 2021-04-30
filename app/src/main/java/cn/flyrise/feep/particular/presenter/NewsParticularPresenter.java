package cn.flyrise.feep.particular.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.android.protocol.entity.NewsDetailsResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collection.CollectionFolderActivity;
import cn.flyrise.feep.collection.CollectionFolderFragment;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.news.bean.RelatedNews;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.utils.Patches;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-10-24 16:50
 */
public class NewsParticularPresenter extends ParticularPresenter {

	private String mFavoriteId;
	private String mBusinessId;
	private String mSendUserId;
	private String mSendTime;
	private String mTitile;

	protected NewsParticularPresenter(Builder builder) {
		super(builder);
		String title = mParticularIntent.getParticularType() == PARTICULAR_NEWS
				? mParticularView.getContext().getResources().getString(R.string.news_detail_news_title)
				: mParticularView.getContext().getResources().getString(R.string.news_detail_notice_title);
		mParticularView.setToolBarTitle(title);
	}

	@Override public void toolBarRightTextClick(View view) {
		TextView tv = (TextView) view;
		String text = tv.getText().toString();
		if (TextUtils.equals(text, "收藏")) {
			Intent intent = new Intent(mContext, CollectionFolderActivity.class);
			intent.putExtra("mode", CollectionFolderFragment.MODE_SELECT);
			((Activity) mContext).startActivityForResult(intent, ParticularActivity.CODE_SELECT_COLLECTION_FOLDER);
		}
		else {
			removeFromFavoriteFolder();
		}
	}

	@Override public void start() {
		mParticularView.showLoading();
		mParticularRepository
				.fetchNewsDetail(mParticularIntent.getBusinessId(), mParticularIntent.getListRequestType(),
						mParticularIntent.getMessageId())
				.start(new ResponseCallback<NewsDetailsResponse>(mParticularView) {
					@Override public void onCompleted(NewsDetailsResponse response) {
						mParticularView.dismissLoading(null);
						if (TextUtils.equals("-95", response.getErrorCode()) || TextUtils.equals("-99", response.getErrorCode())) {
							mParticularView.fetchDetailError(response.getErrorMessage());
							return;
						}

						mFavoriteId = response.favoriteId;
						mSendUserId = response.getSendUserID();
						mSendTime = response.getSendTime();
						mBusinessId = response.getId();
						mTitile = response.getTitle();

						displayHeadInformation(response.getSendUserID(), response.getSendUser(), response.getSendTime(),
								response.getTitle());
						fetchUserDetailInfo(response.getSendUserID());

						mParticularView.displayParticularContent(response.getContent(), true, null);
						boolean hasAttachment = configAttachments(response.getAttachments());
						boolean hasRelated = configRelatedNews(response.getRelatedNews());

						FabVO fabVO = new FabVO();
						fabVO.hasAttachment = hasAttachment;
						fabVO.hasReply = hasRelated;
						mParticularView.configFloatingActionButton(fabVO);
						mParticularView.configBottomMenu(null);

						if (FunctionManager.hasPatch(Patches.PATCH_COLLECTIONS)) {
							if (TextUtils.isEmpty(mFavoriteId)) {
								mParticularView.configToolBarRightText("收藏");
							}
							else {
								mParticularView.configToolBarRightText("取消收藏");
							}
						}
					}

					@Override public void onFailure(RepositoryException repositoryException) {
						mParticularView.dismissLoading(null);
					}
				});
	}

	@Override
	public void handleBackButton() { }

	@Override protected int getReplyType() {
		return -1;
	}

	private boolean configRelatedNews(List<RelatedNews> relatedNews) {
		if (CommonUtil.isEmptyList(relatedNews)) return false;
		mParticularView.displayRelatedNews(relatedNews);
		return true;
	}

	protected String getBusinessId() {
		return mBusinessId;
	}

	protected String getFavoriteId() {
		return mFavoriteId;
	}

	protected String getAddType() {
		return mParticularIntent.getListRequestType() + "";
	}

	protected String getRemoveType() {
		return mParticularIntent.getListRequestType() + "";
	}

	protected void setFavoriteId(String favoriteId) {
		this.mFavoriteId = favoriteId;
		mParticularView.configToolBarRightText(TextUtils.isEmpty(favoriteId) ? "收藏" : "取消收藏");
	}

	protected String getUserId() {
		return mSendUserId;
	}

	protected String getSendTime() {
		return mSendTime;
	}

	protected String getTitle() {
		return mTitile;
	}
}
