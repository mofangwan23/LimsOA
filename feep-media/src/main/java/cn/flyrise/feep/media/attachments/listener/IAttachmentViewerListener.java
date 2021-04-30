package cn.flyrise.feep.media.attachments.listener;

import android.content.Intent;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;

/**
 * @author ZYP
 * @since 2017-11-21 14:46
 */
public interface IAttachmentViewerListener {

	/**
	 * 打开附件、已经将附件的信息封装进 Intent
	 * @param intent 当 intent = null 要么解析出错，要么找不到可以打开该附件的 APP.
	 */
	void prepareOpenAttachment(Intent intent);

	/**
	 * 播放录音附件
	 * @param attachment 录音附件
	 * @param audioPath 录音附件的解密后的路径，如果为空，则使用 attachment.path
	 */
	void preparePlayAudioAttachment(Attachment attachment, String audioPath);

	/**
	 * 任务开始下载
	 */
	void onDownloadBegin(TaskInfo taskInfo);

	/**
	 * 下载进度变化监听
	 * @param progress 下载进度 0-100
	 */
	void onDownloadProgressChange(int progress);

	/**
	 * 下载失败
	 */
	void onDownloadFailed();

	/**
	 * 开始解密
	 */
	void onDecryptBegin();

	/**
	 * 解密进度 0-100
	 */
	void onDecryptProgressChange(int progress);

	/**
	 * 解密成功
	 */
	void onDecryptSuccess();

	/**
	 * 解密失败
	 */
	void onDecryptFailed();

	/**
	 * 开始加密
	 */
	void onEncryptBegin();

	/**
	 * 加密成功
	 */
	void onEncryptSuccess(String filePath);

	/**
	 * 加密失败
	 */
	void onEncryptFailed(String filePath);

	/**
	 * 下载成功
	 */
	void onDownloadSuccess(String filePath);
}
