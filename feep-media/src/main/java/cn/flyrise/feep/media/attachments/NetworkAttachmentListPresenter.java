package cn.flyrise.feep.media.attachments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.FeepDecrypt;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.services.ISecurity.IDecryptListener;
import cn.flyrise.feep.media.activity.VideoPlayerActivity;
import cn.flyrise.feep.media.activity.VideoTestActivity;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.IRepositoryDownloadListener;
import cn.flyrise.feep.media.attachments.repository.AttachmentRepository;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.flyrise.feep.media.common.FileCategoryTable;
import cn.squirtlez.frouter.FRouter;
import java.io.File;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-11-07 14:39
 */
public class NetworkAttachmentListPresenter implements IRepositoryDownloadListener {

	private List<? extends Attachment> mAttachments;
	private AttachmentRepository mRepository;
	private NetworkAttachmentListView mAttachmentListView;
	private Context context;

	public NetworkAttachmentListPresenter(Context context, NetworkAttachmentListView attachmentListView,
			List<? extends Attachment> attachments, DownloadConfiguration configuration) {
		this.context = context;
		this.mAttachments = attachments;
		this.mAttachmentListView = attachmentListView;
		this.mRepository = new AttachmentRepository(attachmentListView.getContext(), configuration);
		this.mRepository.setRepositoryDownloadListener(this);
	}

	/**
	 * ????????????
	 */
	public void openAttachment(final Attachment attachment) {
		if (TextUtils.equals("video",attachment.type)){
			Intent intent = new Intent(context, VideoTestActivity.class);
			intent.putExtra("videoPath",attachment.path);
			intent.putExtra("title",attachment.name);
			intent.putExtra("id",attachment.getId());
//			context.startActivity(intent);
			if (context instanceof Activity){
				((Activity)context).startActivityForResult(intent, 10010);
			}
			return;
		}

		if (!(attachment instanceof NetworkAttachment)) {
			realStartOpenAttachment(attachment, null);
			return;
		}

		DownloadConfiguration configuration = mRepository.getDownloadConfiguration();
		String storageName = AttachmentUtils.encryptAttachmentName(attachment.getId(), attachment.name);

		File encryptFile = new File(configuration.getEncryptDir() + File.separator + storageName);
		if (!encryptFile.exists()) {
			// ??????????????????????????????????????????
			mRepository.downloadAttachment((NetworkAttachment) attachment);
			return;
		}

		File decryptFile = new File(configuration.getDecryptDir() + File.separator + attachment.name);
		if (decryptFile.exists() && decryptFile.lastModified() == encryptFile.lastModified()) {
			// ????????????????????????????????????????????????
			realStartOpenAttachment(attachment, decryptFile.getPath());
			return;
		}

		// ????????????
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
	 * ?????? Intent ????????????????????????????????????
	 * @param attachment ??????
	 * @param attachmentPath ????????????????????????null???????????? attachment.path
	 */
	private void realStartOpenAttachment(Attachment attachment, String attachmentPath) {

		if (AttachmentUtils.isAudioAttachment(attachment)) {
			mAttachmentListView.playAudioAttachment(attachment, attachmentPath);
			return;
		}
		String fileType = AttachmentUtils.getAttachmentFileType(CommonUtil.parseInt(attachment.type));
		String filePath = TextUtils.isEmpty(attachmentPath) ? attachment.path : attachmentPath;
		if (TextUtils.equals("pdf",attachment.type)){
			fileType = "application/pdf";
		}
		if (TextUtils.isEmpty(fileType)) {
			mAttachmentListView.openAttachment(null);
			return;
		}
		Intent intent = AttachmentUtils.getIntent(context, filePath, fileType);
		mAttachmentListView.openAttachment(intent);
	}


	@Override public void onAttachmentDownloadStateChange(TaskInfo taskInfo) {
		Observable
				.create((OnSubscribe<Integer>) f -> {
					for (int i = 0; i < mAttachments.size(); i++) {
						Attachment attachment = mAttachments.get(i);
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
				.subscribe(mAttachmentListView::attachmentDownloadProgressChange, exception -> {
					exception.printStackTrace();
				});
	}

	@Override public void onAttachmentFinalCompleted(TaskInfo taskInfo) {
	}

	/**
	 * ??????????????????
	 */
	public void stopAttachmentDownload(Attachment attachment) {
		if (attachment instanceof NetworkAttachment) {
			mRepository.stopDownload((NetworkAttachment) attachment);
		}
	}

	/**
	 * ????????????
	 */
	public void downloadAttachment(Attachment attachment) {
		mRepository.downloadAttachment((NetworkAttachment) attachment);
	}

	/**
	 * ??????????????????????????????
	 */
	public DownloadProgress getAttachmentDownloadProgress(Attachment attachment) {
		return mRepository.getAttachmentDownloadProgress(attachment);
	}
}
