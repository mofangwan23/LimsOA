package cn.flyrise.feep.media.attachments.bean;

import android.support.annotation.Keep;

/**
 * @author ZYP
 * @since 2017-10-26 17:17
 * 表示一个正在进行的下载任务，下载成功之后，该记录会被清除.
 */
@Keep
public class TaskInfo {

	public String userID;      // user id
	public String taskID;      // attachment id
	public String fileName;    // attachment name

	public String url;         // 下载路径
	public String filePath;    // 文件路径

	public long fileSize;      // 真实文件大小
	public long downloadSize;  // 已下载的内容大小

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TaskInfo)) return false;

		TaskInfo taskInfo = (TaskInfo) o;

		if (!userID.equals(taskInfo.userID)) return false;
		if (!taskID.equals(taskInfo.taskID)) return false;
		if (!fileName.equals(taskInfo.fileName)) return false;
		return url.equals(taskInfo.url);

	}

	@Override public int hashCode() {
		int result = userID.hashCode();
		result = 31 * result + taskID.hashCode();
		result = 31 * result + fileName.hashCode();
		result = 31 * result + url.hashCode();
		return result;
	}
}
