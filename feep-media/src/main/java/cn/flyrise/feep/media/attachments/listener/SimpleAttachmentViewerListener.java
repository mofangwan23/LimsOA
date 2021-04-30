package cn.flyrise.feep.media.attachments.listener;

import android.content.Intent;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;

/**
 * @author ZYP
 * @since 2017-11-21 15:44
 */
public class SimpleAttachmentViewerListener implements IAttachmentViewerListener {

	@Override public void prepareOpenAttachment(Intent intent) {
		// 打开附件
	}

	@Override public void preparePlayAudioAttachment(Attachment attachment, String audioPath) {
		// 播放录音
	}

	@Override public void onDownloadBegin(TaskInfo taskInfo) {
		// 开始下载
	}

	@Override public void onDownloadProgressChange(int progress) {
		// 下载进度
	}

	@Override public void onDownloadFailed() {
		// 下载失败
	}

	@Override public void onDecryptBegin() {
		// 开始解密
	}

	@Override public void onDecryptProgressChange(int progress) {
		// 解密进度改变
	}

	@Override public void onDecryptSuccess() {
		// 解密成功
	}

	@Override public void onDecryptFailed() {
		// 解密失败
	}

	@Override public void onEncryptBegin() {
		// 开始加密
	}

	@Override public void onEncryptSuccess(String filePath) {
		// 加密成功
	}

	@Override public void onEncryptFailed(String filePath) {
		// 加密失败
	}

	@Override public void onDownloadSuccess(String filePath) {
		//下载成功
	}
}
