package cn.flyrise.feep.more.download.manager;

import android.content.Context;
import android.content.Intent;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-09 16:55
 */
public interface DownloadCompletedView {

	void showDownloadedAttachments(List<Attachment> attachments);

	/**
	 * 解密进度
	 */
	void decryptProgressChange(int progress);

	/**
	 * 解密失败
	 */
	void decryptFileFailed();


	/**
	 * 播放录音
	 */
	void playAudioAttachment(Attachment attachment, String audioPath);

	/**
	 * 打开附件
	 */
	void openAttachment(Intent intent);

	/**
	 * 显示错误信息，使用 Toast 进行显示
	 */
	void errorMessageReceive(String errorMessage);

	Context getContext();

}
