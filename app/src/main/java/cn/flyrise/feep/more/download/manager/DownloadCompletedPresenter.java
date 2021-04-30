package cn.flyrise.feep.more.download.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FeepDecrypt;
import cn.flyrise.feep.core.services.ISecurity.IDecryptListener;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.repository.AttachmentDataSource;
import cn.flyrise.feep.media.common.AttachmentUtils;
import java.io.File;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-11-09 16:55
 */
public class DownloadCompletedPresenter {

	private DownloadCompletedView mDownloadCompletedView;
	private AttachmentDataSource mDataSource;

	public DownloadCompletedPresenter(DownloadCompletedView completedView) {
		this.mDownloadCompletedView = completedView;
		this.mDataSource = new AttachmentDataSource(completedView.getContext());
	}

	public void loadDownloadCompletedAttachments() {
		String safeDirPath = CoreZygote.getPathServices().getSafeFilePath();
		mDataSource.queryEncryptedAttachments(mDownloadCompletedView.getContext(), safeDirPath)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mDownloadCompletedView::showDownloadedAttachments, exception -> {});
	}

	/**
	 * 打开附件，注意这里附件的路径是 SAFEFILE 下的路径
	 * Note：SAFEFILE 目录下是加密文件，是无法直接打开的...
	 */
	public void openAttachment(Attachment attachment) {
		File encryptedFile = new File(attachment.path);

		// 1. 首先检查下 TEMPFILE 目录下面是否有这个文件，如果有，直接用，如果没有，那就拜拜
		String decryptedDirPath = CoreZygote.getPathServices().getTempFilePath();
		File decryptedFile = new File(decryptedDirPath + File.separator + attachment.name);
		if (decryptedFile.exists() && decryptedFile.lastModified() == encryptedFile.lastModified()) {
			realStartOpenAttachment(attachment, decryptedFile.getPath());
			return;
		}

		new FeepDecrypt().decrypt(attachment.path, decryptedFile.getPath(), new IDecryptListener() {
			@Override public void onDecryptSuccess(File decryptedFile) {
				realStartOpenAttachment(attachment, decryptedFile.getPath());
			}

			@Override public void onDecryptProgress(int progress) {
				mDownloadCompletedView.decryptProgressChange(progress);
			}

			@Override public void onDecryptFailed() {
				mDownloadCompletedView.decryptFileFailed();
			}
		});
	}

	private void realStartOpenAttachment(Attachment attachment, String attachmentPath) {
		if (AttachmentUtils.isAudioAttachment(attachment)) {
			mDownloadCompletedView.playAudioAttachment(attachment, attachmentPath);
			return;
		}

		String fileType = AttachmentUtils.getAttachmentFileType(Integer.valueOf(attachment.type));
		if (TextUtils.isEmpty(fileType)) {
			mDownloadCompletedView.openAttachment(null);
			return;
		}

		String filePath = TextUtils.isEmpty(attachmentPath) ? attachment.path : attachmentPath;
		Intent intent = AttachmentUtils.getIntent(mDownloadCompletedView.getContext(),filePath,fileType);
		mDownloadCompletedView.openAttachment(intent);
	}

	public void deleteAttachments(List<Attachment> toDeleteAttachments) {
		Context context = mDownloadCompletedView.getContext();
		String decryptedDir = CoreZygote.getPathServices().getTempFilePath();
		String encryptedDir = CoreZygote.getPathServices().getSafeFilePath();

		for (Attachment attachment : toDeleteAttachments) {
			File encryptedFile = new File(attachment.path);
			// 还是得拆出一个 name 出来
			String[] idAndNames = AttachmentUtils.parseTaskIdAndStorageName(encryptedFile.getName());
			if (idAndNames != null) {
				mDataSource.deleteControlGroup(idAndNames[0], idAndNames[1]);
			}
			if (!encryptedFile.exists()) {
				continue;
			}
			String fileName = encryptedFile.getName();
			encryptedFile.delete();

			File decryptedFile = new File(decryptedDir + File.separator + fileName);
			if (!decryptedFile.exists()) {
				continue;
			}
			decryptedFile.delete();
		}

		mDataSource.queryEncryptedAttachments(context, encryptedDir)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mDownloadCompletedView::showDownloadedAttachments, exception -> {});
	}
}
