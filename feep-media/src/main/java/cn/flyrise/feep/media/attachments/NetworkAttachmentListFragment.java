package cn.flyrise.feep.media.attachments;

import static cn.flyrise.feep.core.CoreZygote.getLoginUserServices;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.DividerItemDecoration;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.services.IPathServices;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.attachments.adapter.NetworkAttachmentListAdapter;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration.Builder;
import cn.flyrise.feep.media.attachments.listener.IAttachmentItemClickInterceptor;
import cn.flyrise.feep.media.attachments.listener.IDownloadProgressCallback;
import cn.flyrise.feep.media.attachments.listener.INetworkAttachmentItemHandleListener;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-07 14:07
 * 远程附件展示列表界面，仅支持查看、下载，
 * 在这个界面显示的全都是 NetworkAttachment
 */
@Keep
public class NetworkAttachmentListFragment extends Fragment
		implements NetworkAttachmentListView, INetworkAttachmentItemHandleListener, IDownloadProgressCallback {

	private boolean nestedScrollingEnabled;
	private RecyclerView mRecyclerView;
	private NetworkAttachmentListAdapter mAdapter;
	private List<NetworkAttachment> mAttachments;

	private FELoadingDialog mLoadingDialog;
	private NetworkAttachmentListPresenter mPresenter;
	private IAttachmentItemClickInterceptor mItemHandleInterceptor;

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		IPathServices pathService = CoreZygote.getPathServices();
		DownloadConfiguration configuration = new Builder()
				.owner(getLoginUserServices().getUserId())
				.downloadDir(pathService.getDownloadDirPath())
				.encryptDir(pathService.getSafeFilePath())
				.decryptDir(pathService.getTempFilePath())
				.create();
		mPresenter = new NetworkAttachmentListPresenter(getActivity(),this, mAttachments, configuration);
	}

	public static NetworkAttachmentListFragment newInstance(List<NetworkAttachment> attachments
			, IAttachmentItemClickInterceptor itemHandleInterceptor) {
		return newInstance(false, attachments, itemHandleInterceptor);
	}

	public static NetworkAttachmentListFragment newInstance(boolean isNestedScrollingEnabled, List<NetworkAttachment> attachments
			, IAttachmentItemClickInterceptor itemHandleInterceptor) {
		NetworkAttachmentListFragment instance = new NetworkAttachmentListFragment();
		instance.nestedScrollingEnabled = isNestedScrollingEnabled;
		instance.mAttachments = attachments;
		instance.mItemHandleInterceptor = itemHandleInterceptor;
		return instance;
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.ms_fragment_simple_attachment_list, container, false);
		mRecyclerView = contentView.findViewById(R.id.msAttachmentList);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setHasFixedSize(true);

		if (nestedScrollingEnabled) {
			mRecyclerView.setNestedScrollingEnabled(false);
		}

		Drawable drawable = getResources().getDrawable(R.drawable.ms_divider_album_item);
		DividerItemDecoration dividerDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
		dividerDecoration.setDrawable(drawable);
		mRecyclerView.addItemDecoration(dividerDecoration);

		mRecyclerView.setItemAnimator(null);
		mRecyclerView.setAdapter(mAdapter = new NetworkAttachmentListAdapter(mAttachments));
		mAdapter.setDownloadProgressCallback(this);                 // 设置下载进度监听的回调
		mAdapter.setOnSimpleAttachmentItemHandleListener(this);     // 设置点击、下载、播放、暂停下载的监听
		return contentView;
	}

	@Override public void onAttachmentItemClick(int position, Attachment attachment) {
		if (mItemHandleInterceptor == null) {
			mPresenter.openAttachment(attachment);
			return;
		}

		if (!mItemHandleInterceptor.isInterceptAttachmentClick(attachment)) {
			mPresenter.openAttachment(attachment);
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

	@Override public void attachmentDownloadProgressChange(int position) {
		mAdapter.notifyItemChanged(position);
	}

	@Override public void decryptProgressChange(int progress) {
		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog.Builder(getActivity()).setCancelable(false).create();
		}
		mLoadingDialog.updateProgress(progress);
		mLoadingDialog.show();
	}

	@Override public void decryptFileFailed() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		FEToast.showMessage("文件解密失败，请重试！");
	}

	@Override public void errorMessageReceive(String errorMessage) {
		FEToast.showMessage(errorMessage);
	}

	@Override public void playAudioAttachment(Attachment attachment, String audioPath) {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		AudioPlayer player = AudioPlayer.newInstance(attachment, audioPath);
		player.show(getChildFragmentManager(), "Audio");
	}

	@Override public void openAttachment(Intent intent) {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}

		if (intent == null) {
			FEToast.showMessage("暂不支持查看此文件类型");
			return;
		}

		try {
			startActivity(intent);
		} catch (Exception exp) {
			FEToast.showMessage("无法打开，建议安装查看此类型文件的软件");
		}
	}

//	interface OnAttachmentClickListener{
//		fun onItemClickListener(String attachmentId);
//	}
}
