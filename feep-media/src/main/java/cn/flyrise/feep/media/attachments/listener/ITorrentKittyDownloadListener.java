package cn.flyrise.feep.media.attachments.listener;

import cn.flyrise.feep.media.attachments.bean.TaskInfo;

/**
 * @author ZYP
 * @since 2017-10-31 13:55
 */
public interface ITorrentKittyDownloadListener {

	void notifyProgressChange(TaskInfo taskInfo);

	void notifyDownloadCompleted(TaskInfo taskInfo);

	void notifyDownloadFailed(TaskInfo taskInfo);

}
