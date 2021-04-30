package cn.flyrise.feep.media.attachments;

import static cn.flyrise.feep.media.attachments.AttachmentListActivity.EXTRA_LOCAL_FILE;
import static cn.flyrise.feep.media.attachments.AttachmentListActivity.EXTRA_NETWORK_FILE;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FeepDecrypt;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.ISecurity.IDecryptListener;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.IRepositoryDownloadListener;
import cn.flyrise.feep.media.attachments.repository.AttachmentConverter;
import cn.flyrise.feep.media.attachments.repository.AttachmentRepository;
import cn.flyrise.feep.media.common.AttachmentUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-10-30 13:42
 */
public class AttachmentListPresenter implements IRepositoryDownloadListener {

	/**
	 * 所选附件最大上限不能超过 20 个
	 */
	private static final int LIMIT = 20;
	private List<String> mTempSelectedFiles;
	private List<String> mTempSelectedImages;

	private List<String> mCameraImages;
	private AttachmentRepository mRepository;
	private AttachmentListView mAttachmentListView;

	AttachmentListPresenter(AttachmentListView attachmentListView, DownloadConfiguration configuration) {
		this.mAttachmentListView = attachmentListView;
		this.mCameraImages = new ArrayList<>();
		this.mRepository = new AttachmentRepository((Context) attachmentListView, configuration);
		this.mRepository.setRepositoryDownloadListener(this);
	}

	/**
	 * 初始化数据
	 */
	public void initialize(List<String> selectedFiles, List<NetworkAttachment> networkAttachments) {
		mTempSelectedFiles = new ArrayList<>();
		mTempSelectedImages = new ArrayList<>();

		if (!CommonUtil.isEmptyList(selectedFiles)) {
			List<Attachment> localAttachments = AttachmentConverter.convertAttachments(selectedFiles);
			if (!CommonUtil.isEmptyList(localAttachments)) {
				final String cameraCacheDir = CoreZygote.getPathServices().getImageCachePath();
				for (Attachment attachment : localAttachments) {
					if (AttachmentUtils.isImageAttachment(attachment)) {
						String imagePath = attachment.path;
						if (imagePath.startsWith(cameraCacheDir)) {
							mCameraImages.add(imagePath);
						}
						else {
							mTempSelectedImages.add(attachment.path);
						}
					}
					else {
						mTempSelectedFiles.add(attachment.path);
					}

					mRepository.addAttachment(attachment);
				}
			}
		}

		if (!CommonUtil.isEmptyList(networkAttachments)) {
			for (NetworkAttachment attachment : networkAttachments) {
				mRepository.addAttachment(attachment);
			}
		}

		notifySelectedAttachmentsChange();
	}

	/**
	 * 判断是否还能继续添加附件，最大只能同时存在 20 个附件
	 * @return true 还能继续添加
	 */
	public boolean hasRemaining() {
		return mRepository.getSelectedAttachments().size() < LIMIT;
	}

	/**
	 * 还能添加的附件数量
	 */
	public int remaining() {
		return LIMIT - mRepository.getSelectedAttachments().size();
	}

	/**
	 * 所选附件最大上限
	 */
	public int limit() {
		return LIMIT;
	}

	/**
	 * 添加附件，单个附件（照片、录音）
	 */
	public void addAttachment(String selectedFile) {
		Attachment attachment = AttachmentConverter.convertAttachment(selectedFile);
		if (attachment != null) {
			if (AttachmentUtils.isImageAttachment(attachment)) {
				// 保存拍照的照骗
				mCameraImages.add(attachment.path);
			}
			else {
				mTempSelectedFiles.add(attachment.path);
			}

			mRepository.addAttachment(attachment);
			notifySelectedAttachmentsChange();
		}
	}

	/**
	 * 添加文件附件
	 */
	public void addFileAttachments(List<String> selectedFiles) {
		addLocalAttachments(selectedFiles, mTempSelectedFiles);
	}

	/**
	 * 获取本地文档附件
	 */
	public List<String> getLocalSelectedFiles() {
		List<String> selectedFiles = new ArrayList<>();
		final List<Attachment> selectedAttachments = mRepository.getSelectedAttachments();
		for (Attachment attachment : selectedAttachments) {
			if (attachment instanceof NetworkAttachment) {
				continue;
			}

			if (!AttachmentUtils.isImageAttachment(attachment)
					&& !AttachmentUtils.isTempAudioAttachment(attachment)) {
				selectedFiles.add(attachment.path);
			}
		}
		return selectedFiles;
	}

	/**
	 * 添加图片附件
	 */
	public void addImageAttachments(List<String> selectedImages) {
		addLocalAttachments(selectedImages, mTempSelectedImages);
	}

	public List<String> getLocalImageSelectedImages() {
		List<String> selectedImages = new ArrayList<>();
		final List<Attachment> selectedAttachments = mRepository.getSelectedAttachments();
		for (Attachment attachment : selectedAttachments) {
			if (attachment instanceof NetworkAttachment) {
				continue;
			}

			if (isCameraImage(attachment.path)) {
				continue;
			}

			if (AttachmentUtils.isImageAttachment(attachment)) {
				selectedImages.add(attachment.path);
			}
		}
		return selectedImages;
	}

	private void addLocalAttachments(List<String> selectedFiles, List<String> tempSelectedFiles) {
		if (CommonUtil.isEmptyList(tempSelectedFiles)) {
			tempSelectedFiles.addAll(selectedFiles);
			if (executeLocalAttachmentAdd(selectedFiles)) {
				notifySelectedAttachmentsChange();
			}
			return;
		}

		if (CommonUtil.isEmptyList(selectedFiles)) {    // 智障 test case
			List<Attachment> toDeleteAttachments = AttachmentConverter.convertAttachments(tempSelectedFiles);
			if (!CommonUtil.isEmptyList(toDeleteAttachments)) {
				for (Attachment toDeleteAttachment : toDeleteAttachments) {
					mRepository.deleteAttachment(toDeleteAttachment);
				}
			}

			tempSelectedFiles.clear();
			notifySelectedAttachmentsChange();
			return;
		}

		List<String> intersection = new ArrayList<>();
		intersection.addAll(tempSelectedFiles);
		intersection.removeAll(selectedFiles);                 // 1. 差集
		tempSelectedFiles.retainAll(selectedFiles);            // 2. 交集
		selectedFiles.removeAll(tempSelectedFiles);            // 3. 补集
		tempSelectedFiles.addAll(selectedFiles);               // 4. 并集
		List<Attachment> toDeleteAttachments = AttachmentConverter.convertAttachments(intersection);

		if (!CommonUtil.isEmptyList(toDeleteAttachments)) {
			for (Attachment toDeleteAttachment : toDeleteAttachments) {
				mRepository.deleteAttachment(toDeleteAttachment);
			}
		}

		if (executeLocalAttachmentAdd(tempSelectedFiles)) {
			notifySelectedAttachmentsChange();
		}
	}

	/**
	 * 删除所选附件
	 * @param toDeleteAttachments 待选出的附件
	 */
	public void deleteAttachment(List<Attachment> toDeleteAttachments) {
		for (Attachment toDeleteAttachment : toDeleteAttachments) {
			mRepository.deleteAttachment(toDeleteAttachment);
		}
		notifySelectedAttachmentsChange();
	}

	public void deleteAttachment(Attachment toDeleteAttachment) {
		mRepository.deleteAttachment(toDeleteAttachment);
		notifySelectedAttachmentsChange();
	}

	/**
	 * 将所选择的附件塞进 Intent 并通过 Activity setResult 返回
	 */
	public Intent fillResultData() {
		Intent intent = new Intent();
		List<Attachment> selectedAttachments = mRepository.getSelectedAttachments();
		ArrayList<String> loadAttachments = new ArrayList<>();                  // 本地附件
		ArrayList<NetworkAttachment> networkAttachments = new ArrayList<>();    // 远程附件

		for (Attachment attachment : selectedAttachments) {
			if (attachment == null) continue;//华为手机附件选择出问题
			if (attachment instanceof NetworkAttachment) {
				networkAttachments.add((NetworkAttachment) attachment);
			}
			else {
				loadAttachments.add(attachment.path);
			}
		}
		intent.putStringArrayListExtra(EXTRA_LOCAL_FILE, loadAttachments);
		intent.putParcelableArrayListExtra(EXTRA_NETWORK_FILE, networkAttachments);
		return intent;
	}

	/**
	 * 打开附件
	 */
	public void openAttachment(final Attachment attachment) {
		if (!(attachment instanceof NetworkAttachment)) {
			realStartOpenAttachment(attachment, null);
			return;
		}

		DownloadConfiguration configuration = mRepository.getDownloadConfiguration();
		String storageName = AttachmentUtils.encryptAttachmentName(attachment.getId(), attachment.name);

		File encryptFile = new File(configuration.getEncryptDir() + File.separator + storageName);
		if (!encryptFile.exists()) {
			// 不存在加密的文件，直接下载把
			mRepository.downloadAttachment((NetworkAttachment) attachment);
			return;
		}

		File decryptFile = new File(configuration.getDecryptDir() + File.separator + attachment.name);
		if (decryptFile.exists() && decryptFile.lastModified() == encryptFile.lastModified()) {
			// 存在已经解密的文件，可以直接打开
			realStartOpenAttachment(attachment, decryptFile.getPath());
			return;
		}

		// 开始解密
		new FeepDecrypt().decrypt(encryptFile.getPath(), decryptFile.getPath(), new IDecryptListener() {
			@Override public void onDecryptSuccess(File decryptedFile) {
				realStartOpenAttachment(attachment, decryptedFile.getPath());
			}

			@Override public void onDecryptProgress(int progress) {
				mAttachmentListView.decryptProgressChange(progress);
			}

			@Override public void onDecryptFailed() {
				mAttachmentListView.decryptFileFailed();
			}
		});
	}

	/**
	 * 获取附件下载状态信息
	 */
	public DownloadProgress getAttachmentDownloadProgress(Attachment attachment) {
		return mRepository.getAttachmentDownloadProgress(attachment);
	}

	/**
	 * 下载附件
	 */
	public void downloadAttachment(Attachment attachment) {
		if (attachment instanceof NetworkAttachment) {
			mRepository.downloadAttachment((NetworkAttachment) attachment);
		}
	}

	/**
	 * 停止附件下载
	 */
	public void stopAttachmentDownload(Attachment attachment) {
		if (attachment instanceof NetworkAttachment) {
			mRepository.stopDownload((NetworkAttachment) attachment);
		}
	}

	/**
	 * 添加选中的附件，将选中的附件路径转化成 Attachment 对象
	 */
	private boolean executeLocalAttachmentAdd(List<String> selectedFiles) {
		if (CommonUtil.isEmptyList(selectedFiles)) return false;

		List<Attachment> localAttachments = AttachmentConverter.convertAttachments(selectedFiles);
		if (CommonUtil.isEmptyList(localAttachments)) return false;

		for (Attachment attachment : localAttachments) {
			mRepository.addAttachment(attachment);
		}
		return true;
	}

	/**
	 * 更新界面
	 */
	private void notifySelectedAttachmentsChange() {
		List<Attachment> selectedAttachments = mRepository.getSelectedAttachments();
		mAttachmentListView.showSelectedAttachments(selectedAttachments);
	}

	/**
	 * 通过 Intent 调用能打开对应附件的程序
	 * @param attachment 附件
	 * @param attachmentPath 附件路径，如果为null，则使用 attachment.path
	 */
	private void realStartOpenAttachment(Attachment attachment, String attachmentPath) {
		if (AttachmentUtils.isAudioAttachment(attachment)) {
			mAttachmentListView.playAudioAttachment(attachment, attachmentPath);
			return;
		}

		String fileType = AttachmentUtils.getAttachmentFileType(Integer.valueOf(attachment.type));
		if (TextUtils.isEmpty(fileType)) {
			mAttachmentListView.openAttachment(null);
			return;
		}

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		String path = TextUtils.isEmpty(attachmentPath) ? attachment.path : attachmentPath;
		Uri data = Uri.fromFile(new File(path));
		intent.setDataAndType(data, fileType);
		mAttachmentListView.openAttachment(intent);
	}

	/**
	 * 切换线程，更新附件的下载进度
	 */
	@Override public void onAttachmentDownloadStateChange(TaskInfo taskInfo) {
		Observable
				.create((OnSubscribe<Integer>) f -> {
					List<Attachment> attachments = mRepository.getSelectedAttachments();
					for (int i = 0; i < attachments.size(); i++) {
						Attachment attachment = attachments.get(i);
						if (TextUtils.equals(attachment.getId(), taskInfo.taskID)
								&& TextUtils.equals(attachment.name, taskInfo.fileName)
								&& TextUtils.equals(attachment.path, taskInfo.url)) {
							f.onNext(i);
							break;
						}
					}
					f.onCompleted();
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mAttachmentListView::attachmentDownloadProgressChange);
	}

	@Override public void onAttachmentFinalCompleted(TaskInfo taskInfo) {
	}

	private boolean isCameraImage(String path) {
		if (CommonUtil.isEmptyList(mCameraImages)) {
			return false;
		}

		return mCameraImages.contains(path);
	}
}
