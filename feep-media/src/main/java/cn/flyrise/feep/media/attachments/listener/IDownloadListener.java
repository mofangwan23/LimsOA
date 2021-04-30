package cn.flyrise.feep.media.attachments.listener;

import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask;

/**
 * @author ZYP
 * @since 2017-10-31 17:06
 */
public interface IDownloadListener {

	void onStart(TaskInfo taskInfo);

	void onProgress(AttachmentDownloadTask downloadTask);

	void onCompleted(AttachmentDownloadTask downloadTask);

	void onStop(AttachmentDownloadTask downloadTask);

	void onFailed(AttachmentDownloadTask downloadTask);

}
