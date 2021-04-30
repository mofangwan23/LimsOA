package cn.flyrise.feep.media.attachments.repository;

import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.AttachmentControlGroup;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-27 10:26
 * 附件转换器，用于将
 * 1. File       -> Attachment
 * 2. String     -> Attachment
 * 3. Attachment -> String
 */
public class AttachmentConverter {

	/**
	 * 将已加密过的 File 转化成 Attachment 对象
	 * @param controlGroups 加密附件名称数据对照列表
	 * @param files 已加密的文件
	 */
	public static List<Attachment> convertEncryptAttachments(List<AttachmentControlGroup> controlGroups, File[] files) {
		List<Attachment> attachments = new ArrayList<>();
		for (File file : files) {
			Attachment attachment = new Attachment();
			String name = file.getName();
			String path = file.getPath();
			attachment.size = file.length();
			attachment.path = path;
			attachment.name = name;
			attachment.type = FileCategoryTable.getType(path);

			String[] result = AttachmentUtils.parseTaskIdAndStorageName(name);
			if (result != null) {
				attachment.setId(result[0]);
				attachment.name = getRealNameFromControlGroup(controlGroups, result[0], result[1]);
				attachment.type = FileCategoryTable.getType(attachment.name);
			}
			attachments.add(attachment);
		}
		return attachments;
	}

	/**
	 * 将用户选择的文件路径转化成 Attachment 实体对象
	 */
	public static Attachment convertAttachment(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		return convertAttachment(file);
	}

	/**
	 * 将用户选择的本地附件转化为 Attachment 实体对象
	 */
	public static Attachment convertAttachment(File file) {
		Attachment attachment = new Attachment();
		attachment.name = file.getName();
		attachment.path = file.getPath();
		attachment.size = file.length();
		attachment.type = FileCategoryTable.getType(file.getPath());
		return attachment;
	}

	public static List<Attachment> convertAttachments(List<String> selectedFiles) {
		if (CommonUtil.isEmptyList(selectedFiles)) {
			return null;
		}

		List<Attachment> selectedAttachments = new ArrayList<>();
		for (String path : selectedFiles) {
			selectedAttachments.add(convertAttachment(path));
		}
		return selectedAttachments;
	}

	/**
	 * 将远程附件 NetworkAttachment 转换成 TaskInfo，用于下载任务
	 */
	public static TaskInfo convertToTaskInfo(NetworkAttachment attachment, DownloadConfiguration configuration) {
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.taskID = attachment.getId();
		taskInfo.userID = configuration.getOwner();
		taskInfo.fileName = attachment.name;
		taskInfo.fileSize = attachment.size;
		taskInfo.downloadSize = 0;
		taskInfo.filePath = configuration.getDownloadDir() + File.separator
				+ AttachmentUtils.encryptAttachmentName(attachment.getId(), attachment.name);
		taskInfo.url = attachment.path;
		if (!TextUtils.isEmpty(attachment.fileGuid)) {
			taskInfo.url += (taskInfo.url.indexOf("?") > 0) ? "&" : "?";
			taskInfo.url += "fileGuid=" + attachment.fileGuid;
		}
		return taskInfo;
	}

	/**
	 * 将下载任务重新转换成 NetworkAttachment
	 */
	public static NetworkAttachment convertToNetworkAttachment(TaskInfo taskInfo) {
		NetworkAttachment attachment = new NetworkAttachment();
		attachment.setId(taskInfo.taskID);
		attachment.size = taskInfo.fileSize;
		attachment.name = taskInfo.fileName;
		attachment.type = FileCategoryTable.getType(attachment.name);
		attachment.path = taskInfo.url;
		return attachment;
	}

	/**
	 * 获取本地加密文件的真实文件名
	 * @param controlGroups 加密参照项
	 * @param taskId taskId
	 * @param storageName 文件名（不包含 taskId 的部分）
	 */
	private static String getRealNameFromControlGroup(List<AttachmentControlGroup> controlGroups, String taskId, String storageName) {
		if (CommonUtil.isEmptyList(controlGroups)) {
			return null;
		}

		for (AttachmentControlGroup controlGroup : controlGroups) {
			if (TextUtils.equals(controlGroup.taskId, taskId) &&
					TextUtils.equals(controlGroup.storageName, storageName)) {
				return controlGroup.realName;
			}
		}
		return null;
	}

}
