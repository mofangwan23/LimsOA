package cn.flyrise.feep.more.download.manager;

import static cn.flyrise.feep.core.CoreZygote.getLoginUserServices;
import static cn.flyrise.feep.more.download.manager.IDownloadManagerOperationListener.DOWNLOADING_VIEW;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.DividerItemDecoration;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.services.IPathServices;
import cn.flyrise.feep.media.attachments.adapter.AttachmentListAdapter;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration.Builder;
import cn.flyrise.feep.media.attachments.listener.IAttachmentItemHandleListener;
import cn.flyrise.feep.media.attachments.listener.IDownloadProgressCallback;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2017-11-10 14:14
 */
public class DownloadingFragment extends Fragment
		implements DownloadingView, IDownloadProgressCallback, IAttachmentItemHandleListener {

	private boolean forceDownload;              // 强制开始下载
	private List<String> mPrepareDownloadTasks;  // 指定下载的任务 id.

	private View mEmptyView;
	private FELoadingDialog mLoadingDialog;
	private RecyclerView mRecyclerView;
	private AttachmentListAdapter mAdapter;
	private DownloadingPresenter mPresenter;
	private IDownloadManagerOperationListener mOperationListener;

	public static DownloadingFragment newInstance(IDownloadManagerOperationListener listener, boolean forceDownload) {
		DownloadingFragment instance = new DownloadingFragment();
		instance.mOperationListener = listener;
		instance.forceDownload = forceDownload;
		return instance;
	}

	public void setPrepareDownloadTasks(List<String> prepareDownloadTasks) {
		this.mPrepareDownloadTasks = prepareDownloadTasks;
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		IPathServices pathService = CoreZygote.getPathServices();
		DownloadConfiguration configuration = new Builder()
				.owner(getLoginUserServices().getUserId())
				.downloadDir(pathService.getDownloadDirPath())
				.encryptDir(pathService.getSafeFilePath())
				.decryptDir(pathService.getTempFilePath())
				.create();

		mPresenter = new DownloadingPresenter(this, configuration);
		View contentView = inflater.inflate(R.layout.fragment_downloading, container, false);
		mEmptyView = contentView.findViewById(R.id.layoutEmptyView);
		mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setItemAnimator(null);

		Drawable drawable = getResources().getDrawable(R.drawable.ms_divider_album_item);
		DividerItemDecoration dividerDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
		dividerDecoration.setDrawable(drawable);
		mRecyclerView.addItemDecoration(dividerDecoration);

		mRecyclerView.setAdapter(mAdapter = new AttachmentListAdapter());
		mAdapter.setDownloadProgressCallback(this);
		mAdapter.setOnAttachmentItemHandleListener(this);

		mPresenter.loadDownloadingAttachments(getLoginUserServices().getUserId());
		return contentView;
	}

	@Override public void showDownloadingAttachments(List<Attachment> attachments) {
		hideLoading();
		mEmptyView.setVisibility(CommonUtil.isEmptyList(attachments) ? View.VISIBLE : View.GONE);
		mAdapter.setAttachments(attachments);

		// 强制进入下载状态...
		if (forceDownload && CommonUtil.nonEmptyList(attachments)) {
			List<Attachment> toDownloadAttachments = null;
			if (CommonUtil.nonEmptyList(mPrepareDownloadTasks)) {
				toDownloadAttachments = new ArrayList<>();
				for (Attachment attachment : attachments) {
					if (mPrepareDownloadTasks.contains(attachment.getId())) {
						toDownloadAttachments.add(attachment);
					}
				}
				FELog.i("Download Size = " + toDownloadAttachments.size());
			}
			else {
				toDownloadAttachments = attachments;
			}
			Observable.from(toDownloadAttachments).forEach(mPresenter::downloadAttachment);
		}
	}

	@Override public void onDownloadProgressChange(int position) {
		mAdapter.notifyItemChanged(position);
	}

	@Override public void onDownloadCompleted() {
		if (mOperationListener != null) {
			mOperationListener.refreshDownloadList();
		}
	}

	private void hideLoading() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
		}
		mLoadingDialog = null;
	}

	@Override public void onAttachmentItemClick(int position, Attachment attachment) {
		if (mAdapter.isEditMode()) {
			mAdapter.addAttachmentToDelete(position, attachment);
			if (mOperationListener != null) {
				mOperationListener.notifyEditModeChange(DOWNLOADING_VIEW, true);
			}
			return;
		}
	}

	@Override public void onAttachmentItemLongClick(Attachment attachment) {
		if (mAdapter.isEditMode()) {
			return;
		}
		if (mOperationListener != null) {
			mOperationListener.notifyEditModeChange(DOWNLOADING_VIEW, true);
		}

		// 进入编辑模式的时候暂停全部下载任务。
		final List<Attachment> attachments = mAdapter.getAttachments();
		for (Attachment a : attachments) {
			mPresenter.stopAttachmentDownload(a);
		}
	}

	@Override public void onAttachmentItemToBeDeleteCheckChange() {
		if (mOperationListener != null) {
			mOperationListener.notifyEditModeChange(DOWNLOADING_VIEW, true);
		}
	}

	@Override public void onAttachmentDownloadStopped(Attachment attachment) {
		mPresenter.stopAttachmentDownload(attachment);
	}

	@Override public void onAttachmentDownloadResume(Attachment attachment) {
		mPresenter.downloadAttachment(attachment);
	}

	@Override public DownloadProgress downloadProgress(Attachment attachment) {
		return mPresenter.getAttachmentDownloadProgress(attachment);
	}

	public boolean isEidtMode() {
		return mAdapter.isEditMode();
	}

	public AttachmentListAdapter getAdapter() {
		return mAdapter;
	}

	public void deleteAttachments(List<Attachment> toDeleteAttachments) {
		mLoadingDialog = new FELoadingDialog.Builder(getActivity()).setCancelable(false).create();
		mLoadingDialog.show();
		mPresenter.deleteAttachments(toDeleteAttachments);
	}

	/**
	 * 刷新已经下载完成的附件列表数据。
	 */
	public void refreshDownloadingAttachments() {
		mPresenter.loadDownloadingAttachments(CoreZygote.getLoginUserServices().getUserId());
	}
}
