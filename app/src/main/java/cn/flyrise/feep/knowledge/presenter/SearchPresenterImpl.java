package cn.flyrise.feep.knowledge.presenter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.knowledge.contract.SearchListContract;
import cn.flyrise.feep.knowledge.model.SearchFile;
import cn.flyrise.feep.knowledge.repository.SearchRepository;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.media.attachments.AttachmentViewer;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.SimpleAttachmentViewerListener;
import java.util.List;

/**
 * Created by KLC on 2016/12/7.
 */

public class SearchPresenterImpl implements SearchListContract.Presenter {

	private SearchListContract.View mView;
	private String searchKey;
	private int mNowPage;
	private int mTotalNumber;
	private SearchRepository mRepository;

	private AttachmentViewer mViewer;

	public SearchPresenterImpl(SearchListContract.View view, int folderType) {
		this.mView = view;
		mRepository = new SearchRepository(folderType);

		DownloadConfiguration configuration = new DownloadConfiguration.Builder()
				.owner(CoreZygote.getLoginUserServices().getUserId())
				.downloadDir(CoreZygote.getPathServices().getKnowledgeCachePath())
				.encryptDir(CoreZygote.getPathServices().getSafeFilePath())
				.decryptDir(CoreZygote.getPathServices().getTempFilePath())
				.create();
		mViewer = new AttachmentViewer(CoreZygote.getContext(), configuration);
		this.mViewer.setAttachmentViewerListener(new XSimpleAttachmenntViewerListener());
	}

	@Override
	public void refreshListData(String key) {
		this.searchKey = key;
		if (TextUtils.isEmpty(key)) {
			mView.showRefreshLoading(false);
			return;
		}
		mRepository.loadListData(key, mNowPage = 1, new SearchListContract.LoadListCallback() {
			@Override
			public void loadListDataSuccess(List<SearchFile> dataList, int totalNumber) {
				if (!TextUtils.isEmpty(searchKey)) {
					mView.refreshListData(dataList);
					mView.showRefreshLoading(false);
					mTotalNumber = totalNumber;
					mView.setCanPullUp(hasMoreData());
				}
			}

			@Override
			public void loadListDataError() {
				if (!TextUtils.isEmpty(searchKey)) {
					mView.showRefreshLoading(false);
					mView.setEmptyView();
				}
			}
		});
	}

	@Override
	public void loadMore() {
		mRepository.loadListData(searchKey, ++mNowPage, new SearchListContract.LoadListCallback() {
			@Override
			public void loadListDataSuccess(List<SearchFile> dataList, int totalNumber) {
				if (!TextUtils.isEmpty(searchKey)) {
					mTotalNumber = totalNumber;
					mView.loadMoreListData(dataList);
					mView.setCanPullUp(hasMoreData());
				}
			}

			@Override
			public void loadListDataError() {
				mNowPage--;
				if (!TextUtils.isEmpty(searchKey)) {
					mView.loadMoreListFail();
				}
			}
		});
	}

	@Override
	public boolean hasMoreData() {
		return mNowPage * KnowKeyValue.LOADPAGESIZE < mTotalNumber;
	}

	@Override
	public void opeFile(Context context, SearchFile searchFile) {
		mView.showDealLoading(true);

		String url = FEHttpClient.getInstance().getHost() + FEHttpClient.KNOWLEDGE_DOWNLOAD_PATH + searchFile.id;
		String taskId = searchFile.id;
		String fileName = searchFile.remark.substring(searchFile.remark.lastIndexOf("/") + 1);
		mViewer.openAttachment(url, taskId, fileName);

		LoadingHint.setOnKeyDownListener((keyCode, event) -> {
			TaskInfo taskInfo = mViewer.createTaskInfo(url, taskId, fileName);
			mViewer.getDownloader().deleteDownloadTask(taskInfo);
		});
	}

	@Override
	public void cancelSearch() {
		this.searchKey = null;
		mView.showRefreshLoading(false);
		mView.setCanPullUp(false);
	}

	private class XSimpleAttachmenntViewerListener extends SimpleAttachmentViewerListener {

		@Override public void prepareOpenAttachment(Intent intent) {
			mView.showDealLoading(false);
			mView.openFile(intent);
		}

		@Override public void onDownloadProgressChange(int progress) {
			mView.showProgress(R.string.know_opening, progress);
		}

		@Override public void onDownloadFailed() {
			mView.showMessage(R.string.know_open_fail);
			mView.showDealLoading(false);
		}

		@Override public void onDecryptFailed() {
			mView.showMessage(R.string.know_open_fail);
			mView.showDealLoading(false);
		}

		@Override public void onDecryptProgressChange(int progress) {
			mView.showProgress(R.string.know_decode_open, progress);
		}
	}

}
