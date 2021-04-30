package cn.flyrise.feep.media.attachments.listener;

import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import java.io.File;

/**
 * @author ZYP
 * @since 2017-11-01 10:34
 */
public interface IRepositoryDownloadListener {

	void onAttachmentDownloadStateChange(TaskInfo taskInfo);

	void onAttachmentFinalCompleted(TaskInfo finalFile);

}
