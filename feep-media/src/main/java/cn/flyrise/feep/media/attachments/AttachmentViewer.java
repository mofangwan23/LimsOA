package cn.flyrise.feep.media.attachments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.FeepDecrypt;
import cn.flyrise.feep.core.common.FeepEncrypt;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.ISecurity.IDecryptListener;
import cn.flyrise.feep.core.services.ISecurity.IEncryptListener;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.AttachmentControlGroup;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.AttachmentDownloader;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.IAttachmentViewerListener;
import cn.flyrise.feep.media.attachments.listener.ITorrentKittyDownloadListener;
import cn.flyrise.feep.media.attachments.repository.AttachmentDataSource;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.io.File;

/**
 * @author ZYP
 * @since 2017-11-21 14:24
 * 封装了附件下载、加密、解密、查看的我也不知道叫什么类
 */
public class AttachmentViewer implements ITorrentKittyDownloadListener {

	private AttachmentDataSource mDataSource;
	private AttachmentDownloader mDownloader;
	private DownloadConfiguration mConfiguration;
	private IAttachmentViewerListener mViewerListener;
	private Context mContext;

	public AttachmentViewer(Context context, DownloadConfiguration configuration) {
		this(new AttachmentDataSource(context), configuration);
		this.mContext = context;
	}

	public AttachmentViewer(AttachmentDataSource dataSource, DownloadConfiguration configuration) {
		this.mConfiguration = configuration;
		this.mDataSource = dataSource;
		this.mDownloader = new AttachmentDownloader(mDataSource);
		this.mDownloader.setTorrentKittyDownloadListener(this);
	}

	public void setAttachmentViewerListener(IAttachmentViewerListener listener) {
		this.mViewerListener = listener;
	}

	/**
	 * 打开确定已经存在的文件件，如果文件不存在，会丢异常
	 * @param attachment 附件实体对象，不能为空
	 * @param attachmentPath 可能是解密后附件的路径，可以为空
	 */
	public void openExistAttachment(Attachment attachment, String attachmentPath) {
		if (attachment == null) {
			throw new IllegalArgumentException("The attachment is null.");
		}

		if (TextUtils.isEmpty(attachment.path) && TextUtils.isEmpty(attachmentPath)) {
			throw new IllegalArgumentException("The attachment path is not exist.");
		}

		if (TextUtils.equals(attachment.type, FileCategoryTable.TYPE_AUDIO + "")) {
			if (mViewerListener != null) {
				mViewerListener.preparePlayAudioAttachment(attachment, attachmentPath);
			}
			return;
		}

		String fileType = AttachmentUtils.getAttachmentFileType(Integer.valueOf(attachment.type));
		String filePath = TextUtils.isEmpty(attachmentPath) ? attachment.path : attachmentPath;
		if (TextUtils.isEmpty(fileType)) {
			if (mViewerListener != null) {
				mViewerListener.prepareOpenAttachment(null);
			}
			return;
		}

		Intent intent = AttachmentUtils.getIntent(mContext,filePath,fileType);
		// TODO WPS 专用，先留着，还不知道后面有没有用 ！！！
		Bundle bundle = new Bundle();
		bundle.putString("OpenMode", "Normal");         // 打开模式
		bundle.putBoolean("SendCloseBroad", true);      // 关闭时是否发送广播
		bundle.putBoolean("ClearTrace", true);          // 清除打开记录
		intent.putExtras(bundle);

		if (mViewerListener != null) {
			mViewerListener.prepareOpenAttachment(intent);
		}
	}

	/**
	 * 打开附件，但不确定附件是否存在，如果不存在，则需要先下载...
	 * @param url 附件的 url，必须是完整的 url（http 开头）
	 * @param taskId 附件/文件/或其他什么鬼的 id
	 * @param fileName 附件的名字，比如什么 SNIS-280.torrent
	 */
	public void openAttachment(String url, String taskId, String fileName) {
		String storageName = AttachmentUtils.encryptAttachmentName(taskId, fileName);
		String type = FileCategoryTable.getType(fileName);// 期待这个真实文件里有个 type 咯

		Attachment attachment = new Attachment();
		attachment.type = type;
		attachment.name = fileName;

		// 1. 加密的文件都不存在，直接下载
		File encryptFile = new File(mConfiguration.getEncryptDir() + File.separator + storageName);
		if (!encryptFile.exists()) {
			downloadAttachment(url, taskId, fileName);
			return;
		}

		// 2. 存在解密的文件，直接打开
		File decryptFile = new File(mConfiguration.getDecryptDir() + File.separator + fileName);
		if (decryptFile.exists() && decryptFile.lastModified() == encryptFile.lastModified()) {
			openExistAttachment(attachment, decryptFile.getPath());
			return;
		}

		// 3. 存在加密的文件，直接进行解密
		if (mViewerListener != null) {
			mViewerListener.onDecryptBegin();
		}
		new FeepDecrypt().decrypt(encryptFile.getPath(), decryptFile.getPath(), new IDecryptListener() {
			@Override public void onDecryptSuccess(File decryptedFile) {        // 3.1 解密成功
				if (mViewerListener != null) {
					mViewerListener.onDecryptSuccess();
				}
				openExistAttachment(attachment, decryptedFile.getPath());
			}

			@Override public void onDecryptProgress(int progress) {             // 3.2 解密进度
				if (mViewerListener != null) {
					mViewerListener.onDecryptProgressChange(progress);
				}
			}

			@Override public void onDecryptFailed() {                           // 3.3 解密失败
				if (mViewerListener != null) {
					mViewerListener.onDecryptFailed();
				}
			}
		});
	}

	/**
	 * 下载附件
	 */
	public void downloadAttachment(String url, String taskId, String fileName) {
		TaskInfo taskInfo = createTaskInfo(url, taskId, fileName);
		String storageName = CommonUtil.getMD5(taskInfo.fileName);
		AttachmentControlGroup controlGroup = mDataSource.queryControlGroup(taskId, storageName);
		if (controlGroup == null) {
			controlGroup = new AttachmentControlGroup();
			controlGroup.taskId = AttachmentUtils.fixAttachmentId(taskId);
			controlGroup.storageName = storageName;
			controlGroup.realName = taskInfo.fileName;
			mDataSource.addControlGroup(controlGroup);
		}

		if (mDownloader.startDownloading(taskInfo) == 0) {
			if (mViewerListener != null) {
				mViewerListener.onDownloadBegin(taskInfo);
			}
		}
	}

	public TaskInfo createTaskInfo(String url, String taskId, String fileName) {
		TaskInfo taskInfo = mDataSource.queryTaskInfo(mConfiguration.getOwner(), taskId); // 未下载完成的任务
		if (taskInfo == null) {
			taskInfo = new TaskInfo();
			taskInfo.url = url;
			taskInfo.userID = mConfiguration.getOwner();
			taskInfo.taskID = taskId;
			taskInfo.fileName = fileName;
			taskInfo.filePath = mConfiguration.getDownloadDir()
					+ File.separator + AttachmentUtils.encryptAttachmentName(taskId, fileName);
		}
		return taskInfo;
	}

	public AttachmentDownloader getDownloader() {
		return mDownloader;
	}

	@Override public void notifyProgressChange(TaskInfo taskInfo) {
		int progress = taskInfo.fileSize == 0 ? 0 : (int) (taskInfo.downloadSize * 100 / taskInfo.fileSize);
		if (mViewerListener != null) {
			mViewerListener.onDownloadProgressChange(progress);
		}
	}

	// 附件下载完成
	@Override public void notifyDownloadCompleted(TaskInfo taskInfo) {
		if (mViewerListener != null) mViewerListener.onDownloadSuccess(taskInfo.filePath);
		new FeepEncrypt().encrypt(taskInfo.filePath, new IEncryptListener() {
			// 附件加密成功
			@Override public void onEncryptSuccess(String filePath) {
				openAttachment(taskInfo.url, taskInfo.taskID, taskInfo.fileName);
			}

			// TODO 如果加密失败的话，需要进行什么样的处理?
			@Override public void onEncryptFailed(String filePath) {
				if (mViewerListener != null) {
					mViewerListener.onEncryptFailed(filePath);
				}
			}
		});
	}

	@Override public void notifyDownloadFailed(TaskInfo taskInfo) {
		if (mViewerListener != null) {
			mViewerListener.onDownloadFailed();
		}
	}
}
