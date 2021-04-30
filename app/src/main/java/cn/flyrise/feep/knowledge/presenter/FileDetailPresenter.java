package cn.flyrise.feep.knowledge.presenter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.knowledge.contract.FileDetailContract;
import cn.flyrise.feep.knowledge.model.FileDetail;
import cn.flyrise.feep.knowledge.repository.FileDetailRepository;
import cn.flyrise.feep.media.attachments.AttachmentViewer;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.SimpleAttachmentViewerListener;

/**
 * Created by klc
 */

public class FileDetailPresenter implements FileDetailContract.Presenter {

	private Context mContext;
	private AttachmentViewer mViewer;
	private FileDetailContract.View mView;
	private FileDetailRepository mRepository;

	public FileDetailPresenter(FileDetailContract.View view, Context context) {
		this.mView = view;
		this.mContext = context;
		this.mRepository = new FileDetailRepository();

		DownloadConfiguration configuration = new DownloadConfiguration.Builder()
				.owner(CoreZygote.getLoginUserServices().getUserId())
				.downloadDir(CoreZygote.getPathServices().getKnowledgeCachePath())
				.encryptDir(CoreZygote.getPathServices().getSafeFilePath())
				.decryptDir(CoreZygote.getPathServices().getTempFilePath())
				.create();
		mViewer = new AttachmentViewer(mContext, configuration);
		mViewer.setAttachmentViewerListener(new XSimpleAttachmentViewerListener());
	}

	@Override
	public void getFileDetailById(String fileId) {
		mView.showDealLoading(true);
		mRepository.getFileDetailInfo(fileId, new FileDetailContract.LoadDetailCallBack() {
			@Override
			public void loadSuccess(FileDetail fileDetail) {
				if (fileDetail == null || TextUtils.isEmpty(fileDetail.getFileid())) {
					mView.showMessage(R.string.know_getDetail_fail);
				}
				else {
					mView.showFileDetail(fileDetail);
				}
				mView.showDealLoading(false);
			}

			@Override
			public void loadError() {
				mView.showDealLoading(false);
			}
		});
	}

	@Override
	public boolean haveDownloaded(FileDetail file) {
		return mViewer.getDownloader().isDownloading(file.getFileid());
	}

	@Override public void openFile(FileDetail file) {
		if (file == null) return;
		mView.showDealLoading(true);
		String taskId = file.getFileid();
		String url = FEHttpClient.getInstance().getHost() + FEHttpClient.KNOWLEDGE_DOWNLOAD_PATH + file.getFileid();
		String fileName = file.getRealname();
		mViewer.openAttachment(url, taskId, fileName);

		LoadingHint.setOnKeyDownListener((keyCode, event) -> {
			TaskInfo taskInfo = mViewer.createTaskInfo(url, taskId, fileName);
			mViewer.getDownloader().deleteDownloadTask(taskInfo);
		});
	}

	private class XSimpleAttachmentViewerListener extends SimpleAttachmentViewerListener {

		@Override public void onDownloadFailed() {
			mView.showMessage(R.string.know_down_error);
			mView.showDealLoading(false);
		}

		@Override public void onDownloadProgressChange(int progress) {
			mView.showProgress(R.string.know_downloading, progress);
		}

		@Override public void prepareOpenAttachment(Intent intent) {
			mView.showDealLoading(false);
			mView.showDownLayout(false);
			mView.showConfirmDialog(R.string.know_openfile_after_downlaod, dialog -> mView.openFile(intent));
		}

		@Override public void onDecryptProgressChange(int progress) {
			mView.showProgress(R.string.know_decode_open, progress);
		}

		@Override public void onDecryptFailed() {
			mView.showMessage(R.string.know_open_fail);
			mView.showDealLoading(false);
		}
	}
}
