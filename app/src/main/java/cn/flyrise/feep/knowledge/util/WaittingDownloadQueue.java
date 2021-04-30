package cn.flyrise.feep.knowledge.util;

import android.net.Uri;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.media.attachments.bean.AttachmentControlGroup;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.repository.AttachmentDataSource;
import cn.flyrise.feep.media.common.AttachmentUtils;
import java.io.File;

/**
 * @author ZYP
 * @since 2017-11-21 18:15
 * 等待下载队列：负责将附件添加下载管理器，但不开始具体的下载任务
 */
public class WaittingDownloadQueue {

	private final AttachmentDataSource mDataSource;
	private final DownloadConfiguration mConfiguration;

	public WaittingDownloadQueue(AttachmentDataSource dataSource, DownloadConfiguration configuration) {
		this.mDataSource = dataSource;
		this.mConfiguration = configuration;
	}

	/**
	 * 添加到下载队列，注意这里还没开始下载...
	 * 如果已经下载完成，返回 false.
	 */
	public boolean enqueue(String url, String taskId, String fileName) {
		String storageName = AttachmentUtils.encryptAttachmentName(taskId, fileName);
		File decryptFile = new File(mConfiguration.getDecryptDir() + File.separator + storageName);
		if (decryptFile.exists()) {     // 1. 下载完成：存在解密文件
			return false;
		}

		File encryptFile = new File(mConfiguration.getEncryptDir() + File.separator + storageName);
		if(encryptFile.exists()) {      // 2. 下载完成：存在加密文件
			return false;
		}

		TaskInfo taskInfo = mDataSource.queryTaskInfo(mConfiguration.getOwner(), taskId); // 未下载完成的任务
		if (taskInfo == null) {
			taskInfo = new TaskInfo();
			taskInfo.url = url;
			taskInfo.userID = mConfiguration.getOwner();
			taskInfo.taskID = taskId;
			taskInfo.fileName = fileName;
			taskInfo.filePath = mConfiguration.getDownloadDir()
					+ File.separator + AttachmentUtils.encryptAttachmentName(taskId, fileName);
			final Uri uri = mDataSource.addDownloadTask(taskInfo);
			FELog.i("enqueue : "+uri.toString());
		}

		storageName = CommonUtil.getMD5(taskInfo.fileName);
		AttachmentControlGroup controlGroup = mDataSource.queryControlGroup(taskId, storageName);
		if (controlGroup == null) {
			controlGroup = new AttachmentControlGroup();
			controlGroup.taskId = AttachmentUtils.fixAttachmentId(taskId);
			controlGroup.storageName = storageName;
			controlGroup.realName = taskInfo.fileName;
			mDataSource.addControlGroup(controlGroup);
		}
		return true;
	}

}
