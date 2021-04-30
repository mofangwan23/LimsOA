package cn.flyrise.feep.media.attachments;

import android.content.Context;
import android.content.Intent;
import cn.flyrise.feep.media.attachments.bean.Attachment;

/**
 * @author ZYP
 * @since 2017-11-07 14:39
 */
public interface NetworkAttachmentListView {

	/**
	 * 附件下载进度更新
	 * @param position 附件对应的 item view
	 */
	void attachmentDownloadProgressChange(int position);

	/**
	 * 解密进度
	 */
	void decryptProgressChange(int progress);

	/**
	 * 解密失败
	 */
	void decryptFileFailed();

	/**
	 * 显示错误信息，使用 Toast 进行显示
	 */
	void errorMessageReceive(String errorMessage);

	/**
	 * 播放录音
	 */
	void playAudioAttachment(Attachment attachment, String audioPath);

	/**
	 * 打开附件
	 */
	void openAttachment(Intent intent);

	Context getContext();

}
