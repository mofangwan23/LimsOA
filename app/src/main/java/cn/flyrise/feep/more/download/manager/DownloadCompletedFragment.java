package cn.flyrise.feep.more.download.manager;

import static cn.flyrise.feep.more.download.manager.IDownloadManagerOperationListener.DOWNLOAD_COMPLETED_VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.media.attachments.AudioPlayer;
import cn.flyrise.feep.media.attachments.LocalAttachmentListFragment;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.listener.ILocalAttachmentItemHandleListener;
import java.io.File;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-09 15:39
 */
public class DownloadCompletedFragment extends Fragment
		implements DownloadCompletedView, ILocalAttachmentItemHandleListener {

	private View mEmptyView;
	private DownloadCompletedPresenter mPresenter;
	private LocalAttachmentListFragment mAttachmentListView;
	private IDownloadManagerOperationListener mOperationListener;
	private FELoadingDialog mLoadingDialog;

	public static DownloadCompletedFragment newInstance(IDownloadManagerOperationListener listener) {
		DownloadCompletedFragment instance = new DownloadCompletedFragment();
		instance.mOperationListener = listener;
		return instance;
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mPresenter = new DownloadCompletedPresenter(this);
		View contentView = inflater.inflate(R.layout.fragment_download_completed, container, false);
		mEmptyView = contentView.findViewById(R.id.layoutEmptyView);
		mAttachmentListView = LocalAttachmentListFragment.newInstance(false, null, this);
		getChildFragmentManager().beginTransaction()
				.add(R.id.layoutDownloadCompleted, mAttachmentListView)
				.show(mAttachmentListView)
				.commit();
		initializeDownloadedAttachments();
		return contentView;
	}

	private void initializeDownloadedAttachments() {
		String safeDirPath = CoreZygote.getPathServices().getSafeFilePath();
		File safeDir = new File(safeDirPath);

		// 1. ???????????????????????????
		if (!safeDir.exists()) {
			mEmptyView.setVisibility(View.VISIBLE);
			return;
		}

		// 2. ???????????????????????????
		File[] attachmentFiles = safeDir.listFiles();
		if (attachmentFiles == null || attachmentFiles.length == 0) {
			mEmptyView.setVisibility(View.VISIBLE);
			return;
		}

		// 3. ???????????????????????????????????????????????????????????????
		mPresenter.loadDownloadCompletedAttachments();
	}

	@Override public void onAttachmentItemClick(int position, Attachment attachment) {
		// ??????????????????
		if (mAttachmentListView.isEditMode()) {
			mAttachmentListView.addAttachmentToDelete(position, attachment);
			if (mOperationListener != null) {
				mOperationListener.notifyEditModeChange(DOWNLOAD_COMPLETED_VIEW, true);
			}
			return;
		}
		mPresenter.openAttachment(attachment);
	}

	@Override public void onAttachmentItemLongClick(Attachment attachment) {
		if (mAttachmentListView.isEditMode()) {
			return;
		}
		if (mOperationListener != null) {
			mOperationListener.notifyEditModeChange(DOWNLOAD_COMPLETED_VIEW, true);
		}
	}

	@Override public void onAttachmentItemToBeDeleteCheckChange() {
		if (mOperationListener != null) {
			mOperationListener.notifyEditModeChange(DOWNLOAD_COMPLETED_VIEW, true);
		}
	}

	@Override public void showDownloadedAttachments(List<Attachment> attachments) {
		hideLoading();
		mEmptyView.setVisibility(CommonUtil.isEmptyList(attachments) ? View.VISIBLE : View.GONE);
		mAttachmentListView.setAttachments(attachments);
	}

	@Override public void decryptProgressChange(int progress) {
		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog.Builder(getActivity()).setCancelable(false).create();
		}
		mLoadingDialog.updateProgress(progress);
		mLoadingDialog.show();
	}

	@Override public void decryptFileFailed() {
		hideLoading();
		FEToast.showMessage("?????????????????????????????????");
	}

	@Override public void playAudioAttachment(Attachment attachment, String audioPath) {
		hideLoading();
		AudioPlayer player = AudioPlayer.newInstance(attachment, audioPath);
		player.show(getChildFragmentManager(), "Audio");
	}

	@Override public void openAttachment(Intent intent) {
		hideLoading();
		if (intent == null) {
			FEToast.showMessage("?????????????????????????????????");
			return;
		}

		try {
			startActivity(intent);
		} catch (Exception exp) {
			FEToast.showMessage("?????????????????????????????????????????????????????????");
		}
	}

	@Override public void errorMessageReceive(String errorMessage) {
		hideLoading();
		FEToast.showMessage(errorMessage);
	}

	public boolean isEditMode() {
		return mAttachmentListView.isEditMode();
	}

	public LocalAttachmentListFragment getAttachmentListView() {
		return mAttachmentListView;
	}

	public void deleteAttachments(List<Attachment> toDeleteAttachments) {
		mLoadingDialog = new FELoadingDialog.Builder(getActivity()).setCancelable(false).create();
		mLoadingDialog.show();
		mPresenter.deleteAttachments(toDeleteAttachments);
	}

	private void hideLoading() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
		}
		mLoadingDialog = null;
	}

	/**
	 * ????????????????????????????????????????????????
	 */
	public void refreshDownloadCompletedAttachments() {
		mPresenter.loadDownloadCompletedAttachments();
	}
}
