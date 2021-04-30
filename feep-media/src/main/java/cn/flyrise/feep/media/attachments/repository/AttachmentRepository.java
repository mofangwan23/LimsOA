package cn.flyrise.feep.media.attachments.repository;

import static cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask.STATE_COMPLETED;
import static cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask.STATE_RUNNING;
import static cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask.STATE_STOP;

import android.content.Context;
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
import cn.flyrise.feep.media.attachments.listener.IRepositoryDownloadListener;
import cn.flyrise.feep.media.attachments.listener.ITorrentKittyDownloadListener;
import cn.flyrise.feep.media.common.AttachmentUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-26 17:23
 */
public class AttachmentRepository implements ITorrentKittyDownloadListener {

	private final List<Attachment> mSelectedAttachments;                    // 已选择的附件
	private final AttachmentDataSource mSQLiteDataSource;                   // 本地数据源

	private final DownloadConfiguration mConfiguration;                     // 下载配置(contain userId、storage path、save path and so on...)
	private AttachmentDownloader mAttachmentDownloader;                 // 附件下载器
	private IRepositoryDownloadListener mRepositoryDownloadListener;

	public AttachmentRepository(Context context, DownloadConfiguration configuration) {
		this.mConfiguration = configuration;
		this.mSQLiteDataSource = new AttachmentDataSource(context);
		this.mSelectedAttachments = new ArrayList<>();
		this.mAttachmentDownloader = new AttachmentDownloader(mSQLiteDataSource);
		this.mAttachmentDownloader.setTorrentKittyDownloadListener(this);
	}

	public DownloadConfiguration getDownloadConfiguration() {
		return this.mConfiguration;
	}

	public void setRepositoryDownloadListener(IRepositoryDownloadListener repositoryDownloadListener) {
		this.mRepositoryDownloadListener = repositoryDownloadListener;
	}

	public void addAttachment(Attachment attachment) {
		if (!mSelectedAttachments.contains(attachment)) {
			mSelectedAttachments.add(attachment);
		}
	}

	public void deleteAttachment(Attachment attachment) {
		if (mSelectedAttachments.contains(attachment)) {
			mSelectedAttachments.remove(attachment);

			if (attachment instanceof NetworkAttachment) {
				TaskInfo taskInfo = AttachmentConverter.convertToTaskInfo((NetworkAttachment) attachment, mConfiguration);
				mSQLiteDataSource.deleteDownloadTask(taskInfo);
			}
		}
	}

	public List<Attachment> getSelectedAttachments() {
		return this.mSelectedAttachments;
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

		// 暂停的嘛?
		TaskInfo taskInfo = mSQLiteDataSource.queryTaskInfo(mConfiguration.getOwner(), attachment.getId());
		if (taskInfo != null) {
			progress = new DownloadProgress();
			progress.setState(STATE_STOP);
			int p = taskInfo.fileSize == 0 ? 0 : (int) (taskInfo.downloadSize * 100 / taskInfo.fileSize);
			progress.setProgress(p);
		}
		return progress;
	}

	public TaskInfo getDownloadTaskInfo(Attachment attachment) {
		return mSQLiteDataSource.queryTaskInfo(mConfiguration.getOwner(), attachment.getId());
	}

	/**
	 * 下载附件
	 * @param attachment 下载的远程附件
	 */
	public void downloadAttachment(NetworkAttachment attachment) {
		TaskInfo taskInfo = mSQLiteDataSource.queryTaskInfo(mConfiguration.getOwner(), attachment.getId()); // 未下载完成的任务
		if (taskInfo == null) {
			taskInfo = AttachmentConverter.convertToTaskInfo(attachment, mConfiguration);                   // 新任务
		}

		String taskId = taskInfo.taskID;
		String storageName = CommonUtil.getMD5(taskInfo.fileName);
		AttachmentControlGroup controlGroup = mSQLiteDataSource.queryControlGroup(taskId, storageName);
		if (controlGroup == null) {
			controlGroup = new AttachmentControlGroup();
			controlGroup.taskId = AttachmentUtils.fixAttachmentId(taskId);
			controlGroup.storageName = storageName;
			controlGroup.realName = taskInfo.fileName;
			mSQLiteDataSource.addControlGroup(controlGroup);
		}
		mAttachmentDownloader.startDownloading(taskInfo);
	}

	/**
	 * 停止下载
	 */
	public void stopDownload(NetworkAttachment attachment) {    // 未下载完成的任务.
		TaskInfo taskInfo = AttachmentConverter.convertToTaskInfo(attachment, mConfiguration);
		if (mAttachmentDownloader != null) {
			mAttachmentDownloader.stopDownload(taskInfo);
		}
	}

	@Override public void notifyProgressChange(TaskInfo taskInfo) {
		if (mRepositoryDownloadListener != null) {
			mRepositoryDownloadListener.onAttachmentDownloadStateChange(taskInfo);
		}
	}

	@Override public void notifyDownloadCompleted(TaskInfo taskInfo) {
		new FeepEncrypt().encrypt(taskInfo.filePath, new IEncryptListener() {   // 下载成功后，将文件加密并转移到 SAFE 目录.
			@Override public void onEncryptSuccess(String filePath) {
				File tempFile = new File(filePath);
				if (tempFile.exists()) {
					tempFile.delete();
				}
				if (mRepositoryDownloadListener != null) {
					mRepositoryDownloadListener.onAttachmentDownloadStateChange(taskInfo);
					mRepositoryDownloadListener.onAttachmentFinalCompleted(taskInfo);
				}
			}

			@Override public void onEncryptFailed(String filePath) {}
		});

		if (mRepositoryDownloadListener != null) {
			mRepositoryDownloadListener.onAttachmentDownloadStateChange(taskInfo);
		}
	}

	@Override public void notifyDownloadFailed(TaskInfo taskInfo) {

	}
}
