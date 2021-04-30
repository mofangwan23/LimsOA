package cn.flyrise.feep.more.download.manager;

import static cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask.STATE_COMPLETED;
import static cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask.STATE_RUNNING;
import static cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask.STATE_STOP;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FeepEncrypt;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.ISecurity.IEncryptListener;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.AttachmentControlGroup;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.downloader.AttachmentDownloader;
import cn.flyrise.feep.media.attachments.listener.ITorrentKittyDownloadListener;
import cn.flyrise.feep.media.attachments.repository.AttachmentConverter;
import cn.flyrise.feep.media.attachments.repository.AttachmentDataSource;
import cn.flyrise.feep.media.common.AttachmentUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-11-10 14:40
 */
public class DownloadingPresenter implements ITorrentKittyDownloadListener {

	private List<Attachment> mAttachments;
	private AttachmentDataSource mDataSource;
	private DownloadingView mDownloadingView;
	private DownloadConfiguration mConfiguration;
	private AttachmentDownloader mAttachmentDownloader;

	public DownloadingPresenter(DownloadingView downloadingView, DownloadConfiguration configuration) {
		this.mConfiguration = configuration;
		this.mDownloadingView = downloadingView;
		mDataSource = new AttachmentDataSource(downloadingView.getContext());
		mAttachmentDownloader = new AttachmentDownloader(mDataSource);
		mAttachmentDownloader.setTorrentKittyDownloadListener(this);
	}

	public void loadDownloadingAttachments(String userId) {
		mDataSource.queryTaskInfos(userId)
				.flatMap(taskInfos -> Observable.from(taskInfos))
				.map(AttachmentConverter::convertToNetworkAttachment)
				.toList()
				.map(networkAttachments -> new ArrayList<Attachment>(networkAttachments))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(attachments -> {
					mAttachments = attachments;
					mDownloadingView.showDownloadingAttachments(attachments);
				}, exception -> {});
	}


	@Override public void notifyProgressChange(TaskInfo taskInfo) {
		Observable
				.create((OnSubscribe<Integer>) f -> {
					for (int i = 0; i < mAttachments.size(); i++) {
						Attachment attachment = mAttachments.get(i);
						if (TextUtils.equals(attachment.getId(), taskInfo.taskID)
								&& TextUtils.equals(attachment.name, taskInfo.fileName)
								&& TextUtils.equals(attachment.path, taskInfo.url)) {
							f.onNext(i);
							break;
						}
					}
					f.onCompleted();
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mDownloadingView::onDownloadProgressChange);
	}

	@Override public void notifyDownloadCompleted(TaskInfo taskInfo) {
		new FeepEncrypt().encrypt(taskInfo.filePath, new IEncryptListener() {   // 下载成功后，将文件加密并转移到 SAFE 目录.
			@Override public void onEncryptSuccess(String filePath) {
				File tempFile = new File(filePath);
				if (tempFile.exists()) {
					tempFile.delete();
				}
				mDownloadingView.onDownloadCompleted();
			}

			@Override public void onEncryptFailed(String filePath) {}
		});

	}

	@Override public void notifyDownloadFailed(TaskInfo taskInfo) {

	}

	public void downloadAttachment(Attachment attachment) {
		TaskInfo taskInfo = mDataSource.queryTaskInfo(mConfiguration.getOwner(), attachment.getId()); // 未下载完成的任务
		if (taskInfo == null) {
			taskInfo = AttachmentConverter.convertToTaskInfo((NetworkAttachment) attachment, mConfiguration);                   // 新任务
		}

		String taskId = taskInfo.taskID;
		String storageName = CommonUtil.getMD5(taskInfo.fileName);
		AttachmentControlGroup controlGroup = mDataSource.queryControlGroup(taskId, storageName);
		if (controlGroup == null) {
			controlGroup = new AttachmentControlGroup();
			controlGroup.taskId = AttachmentUtils.fixAttachmentId(taskId);
			controlGroup.storageName = storageName;
			controlGroup.realName = taskInfo.fileName;
			mDataSource.addControlGroup(controlGroup);
		}
		mAttachmentDownloader.startDownloading(taskInfo);
	}

	public void stopAttachmentDownload(Attachment attachment) {
		TaskInfo taskInfo = AttachmentConverter.convertToTaskInfo((NetworkAttachment) attachment, mConfiguration);
		if (mAttachmentDownloader != null) {
			mAttachmentDownloader.stopDownload(taskInfo);
		}
	}

	/**
	 * 获取附件的下载进度情况
	 */
	public DownloadProgress getAttachmentDownloadProgress(Attachment attachment) {
		DownloadProgress progress = null;

		AttachmentDownloadTask downloadingTask = mAttachmentDownloader.getDownloadingTask(attachment.getId());
		// 该附件正在下载.
		if (downloadingTask != null) {
			progress = new DownloadProgress();
			if (downloadingTask.isRunning()) {
				TaskInfo taskInfo = downloadingTask.getTaskInfo();
				progress.setState(STATE_RUNNING);
				int p = taskInfo.fileSize == 0 ? 0 : (int) (taskInfo.downloadSize * 100 / taskInfo.fileSize);
				progress.setProgress(p);
			}
			else if (downloadingTask.isCompleted()) {
				progress.setState(STATE_COMPLETED);
			}
			return progress;
		}

		TaskInfo taskInfo = mDataSource.queryTaskInfo(mConfiguration.getOwner(), attachment.getId());
		if (taskInfo != null) {
			progress = new DownloadProgress();
			progress.setState(STATE_STOP);
			int p = taskInfo.fileSize == 0 ? 0 : (int) (taskInfo.downloadSize * 100 / taskInfo.fileSize);
			progress.setProgress(p);
		}
		return progress;
	}

	public void deleteAttachments(List<Attachment> toDeleteAttachments) {
		for (Attachment attachment : toDeleteAttachments) {
			TaskInfo taskInfo = AttachmentConverter.convertToTaskInfo((NetworkAttachment) attachment, mConfiguration);
			if (taskInfo != null) {
				mAttachmentDownloader.deleteDownloadTask(taskInfo);
				File tempFile = new File(taskInfo.filePath);
				String[] idAndNames = AttachmentUtils.parseTaskIdAndStorageName(tempFile.getName());
				if (idAndNames != null) {
					mDataSource.deleteControlGroup(idAndNames[0], idAndNames[1]);
				}

				if (tempFile.exists()) {
					tempFile.delete();
				}
			}
		}

		loadDownloadingAttachments(CoreZygote.getLoginUserServices().getUserId());
	}
}
