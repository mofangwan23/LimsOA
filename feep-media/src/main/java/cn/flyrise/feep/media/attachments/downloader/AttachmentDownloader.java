package cn.flyrise.feep.media.attachments.downloader;

import android.text.TextUtils;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.listener.IDownloadListener;
import cn.flyrise.feep.media.attachments.listener.ITorrentKittyDownloadListener;
import cn.flyrise.feep.media.attachments.repository.AttachmentDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author ZYP
 * @since 2017-10-31 11:07
 * 附件下载器
 */
public class AttachmentDownloader implements IDownloadListener {

	private final Executor mExecutor;
	private final AttachmentDataSource mDataSource;                         // 数据源
	private final List<AttachmentDownloadTask> mDownloadingTasks;           // 正在下载的任务
	private ITorrentKittyDownloadListener mRepositoryDownloadListener;

	public AttachmentDownloader(AttachmentDataSource dataSource) {
		this.mDataSource = dataSource;
		this.mDownloadingTasks = new ArrayList<>();
		this.mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
	}

	public void setTorrentKittyDownloadListener(ITorrentKittyDownloadListener listener) {
		this.mRepositoryDownloadListener = listener;
	}

	/**
	 * 如果任务正在下载，返回 -1
	 */
	public int startDownloading(TaskInfo taskInfo) {
		AttachmentDownloadTask downloadTask = getDownloadingTask(taskInfo.taskID);
		if (downloadTask != null) {                 // 任务正在进行
			return -1;
		}

		downloadTask = new AttachmentDownloadTask(taskInfo, this);
		mDownloadingTasks.add(downloadTask);
		mExecutor.execute(downloadTask);
		return 0;
	}

	public void stopDownload(TaskInfo taskInfo) {
		AttachmentDownloadTask downloadTask = getDownloadingTask(taskInfo.taskID);
		if (downloadTask == null || downloadTask.isCompleted()) {
			return;
		}

		downloadTask.stop();
		mDownloadingTasks.remove(downloadTask);                             // 移出正在下载的附件任务
		mDataSource.updateDownloadTask(downloadTask.getTaskInfo());         // 更新附件下载进度.
	}

	public int deleteDownloadTask(TaskInfo taskInfo) {
		AttachmentDownloadTask downloadTask = getDownloadingTask(taskInfo.taskID);
		if (downloadTask != null && downloadTask.isRunning()) {
			downloadTask.stop();
			mDownloadingTasks.remove(downloadTask);
		}

		return mDataSource.deleteDownloadTask(taskInfo);
	}

	public AttachmentDownloadTask getDownloadingTask(String taskId) {
		if (mDownloadingTasks.isEmpty()) {
			return null;
		}

		for (AttachmentDownloadTask downloadTask : mDownloadingTasks) {
			if (TextUtils.equals(downloadTask.getTaskId(), taskId)) {
				return downloadTask;
			}
		}
		return null;
	}

	public boolean isDownloading(String taskId) {
		if (mDownloadingTasks.isEmpty()) {
			return false;
		}

		for (AttachmentDownloadTask downloadTask : mDownloadingTasks) {
			if (TextUtils.equals(downloadTask.getTaskId(), taskId)) {
				return true;
			}
		}

		return false;
	}

	@Override public void onStart(TaskInfo taskInfo) {
		if (taskInfo.downloadSize == 0) {
			mDataSource.addDownloadTask(taskInfo);
		}
	}

	@Override public void onProgress(AttachmentDownloadTask downloadTask) {
		mRepositoryDownloadListener.notifyProgressChange(downloadTask.getTaskInfo());
	}

	@Override public void onCompleted(AttachmentDownloadTask downloadTask) {
		mDataSource.deleteDownloadTask(downloadTask.getTaskInfo());
		mDownloadingTasks.remove(downloadTask);

		if (mRepositoryDownloadListener != null) {
			mRepositoryDownloadListener.notifyDownloadCompleted(downloadTask.getTaskInfo());
		}
	}

	@Override public void onStop(AttachmentDownloadTask downloadTask) {
		mDataSource.updateDownloadTask(downloadTask.getTaskInfo());
		mDownloadingTasks.remove(downloadTask);
		mRepositoryDownloadListener.notifyProgressChange(downloadTask.getTaskInfo());
	}

	@Override public void onFailed(AttachmentDownloadTask downloadTask) {
		mDownloadingTasks.remove(downloadTask);
		mRepositoryDownloadListener.notifyDownloadFailed(downloadTask.getTaskInfo());
	}
}
