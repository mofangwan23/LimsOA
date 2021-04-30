package cn.flyrise.feep.media.attachments.downloader;

import android.content.Context;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.media.attachments.AttachmentViewer;
import cn.flyrise.feep.media.attachments.listener.SimpleAttachmentViewerListener;

/**
 * Create by cm132 on 2019/3/4.
 * Describe:文件下载管理器
 */
public class DownloaderFileManager {

	private AttachmentViewer attachmentViewer;

	public DownloaderFileManager(Context context) {
		if (CoreZygote.getLoginUserServices() != null) {
			DownloadConfiguration configuration = new DownloadConfiguration.Builder()
					.owner(CoreZygote.getLoginUserServices().getUserId())
					.downloadDir(CoreZygote.getPathServices().getDownloadDirPath())
					.encryptDir(CoreZygote.getPathServices().getSafeFilePath())
					.decryptDir(CoreZygote.getPathServices().getTempFilePath())
					.create();
			this.attachmentViewer = new AttachmentViewer(context, configuration);
		}
	}

	public DownloaderFileManager setListner(SimpleAttachmentViewerListener listener) {
		if (attachmentViewer != null) attachmentViewer.setAttachmentViewerListener(listener);
		return this;
	}

	public void startDownload(String url, String fileName) {
		if (attachmentViewer != null) attachmentViewer.downloadAttachment(url, url, fileName);
	}
}
