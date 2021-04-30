package cn.flyrise.feep.media.attachments;

import static cn.flyrise.feep.core.CoreZygote.getLoginUserServices;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.FeepDecrypt;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.IPathServices;
import cn.flyrise.feep.core.services.ISecurity.IDecryptListener;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration.Builder;
import cn.flyrise.feep.media.attachments.listener.IRepositoryDownloadListener;
import cn.flyrise.feep.media.attachments.repository.AttachmentRepository;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.io.File;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author 社会主义接班人
 * @since 2018-08-13 17:19
 */
public class SingleAttachmentActivity extends BaseActivity implements IRepositoryDownloadListener {

	private ImageView mIvIcon;
	private TextView mTvName;
	private TextView mTvDownload;
	private TextView mTvProgress;
	private FEToolbar mToolBar;

	private View mProgressLayout;
	private ProgressBar mProgressBar;
	private ImageView mIvReStart;
	private ImageView mIvPause;

	private NetworkAttachment mAttachment;
	private AttachmentRepository mRepository;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAttachment = getIntent().getParcelableExtra("NetworkAttachment");
		IPathServices pathService = CoreZygote.getPathServices();
		DownloadConfiguration configuration = new Builder()
				.owner(getLoginUserServices().getUserId())
				.downloadDir(pathService.getDownloadDirPath())
				.encryptDir(pathService.getSafeFilePath())
				.decryptDir(pathService.getTempFilePath())
				.create();
		mRepository = new AttachmentRepository(this, configuration);
		mRepository.setRepositoryDownloadListener(this);
		setContentView(R.layout.ms_activity_single_attachment);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		this.mToolBar = toolbar;
	}

	@Override public void bindView() {
		mIvIcon = findViewById(R.id.msIvAttachmentIcon);
		mTvName = findViewById(R.id.msTvAttachmentName);
		mTvDownload = findViewById(R.id.msTvAttachmentDownload);
		mTvProgress = findViewById(R.id.msTvDownloadProgress);
		mProgressBar = findViewById(R.id.msDownloadProgressBar);
		mProgressLayout = findViewById(R.id.msLayoutProgress);
		mIvReStart = findViewById(R.id.msIvReStartDownload);
		mIvPause = findViewById(R.id.msIvPauseDownload);
	}

	@Override public void bindData() {
		if (mAttachment == null) {
			FEToast.showMessage("无法打开此附件");
			finish();
			return;
		}

		// 附件已经存在
		File encryptFile = AttachmentUtils.getDownloadedAttachment(mAttachment);
		if (encryptFile != null && encryptFile.exists()) {
			mTvDownload.setVisibility(View.GONE);
			tryOpenAttachment(encryptFile);
			return;
		}

		// 附件不存在，等待用户自己下载
		mIvIcon.setImageResource(FileCategoryTable.getIcon(mAttachment.type));
		mTvName.setText(mAttachment.name);
		mToolBar.setTitle(mAttachment.name);
		String downloadText = mAttachment.size == 0 ? "立即下载" : "立即下载(" + mAttachment.size + ")";
		mTvDownload.setText(downloadText);

		DownloadProgress progress = mRepository.getAttachmentDownloadProgress(mAttachment);
		if (progress != null && !progress.isCompleted()) {
			mIvPause.setVisibility(View.GONE);
			mTvDownload.setVisibility(View.GONE);
			mTvProgress.setVisibility(View.VISIBLE);
			mIvReStart.setVisibility(View.VISIBLE);
			mProgressLayout.setVisibility(View.VISIBLE);
			mProgressBar.setProgress(progress.getProgress());

			TaskInfo taskInfo = mRepository.getDownloadTaskInfo(mAttachment);
			if (taskInfo != null) {
				String text = "下载中";
				if (taskInfo.fileSize != 0) {
					text = String.format("下载中(%s/%s)", Formatter.formatFileSize(this, taskInfo.downloadSize),
							Formatter.formatFileSize(this, taskInfo.fileSize));
				}
				mTvProgress.setText(text);
			}
		}
		else {
			mIvReStart.setVisibility(View.GONE);
			mIvPause.setVisibility(View.GONE);
			mProgressBar.setProgress(0);
		}

		mTvDownload.setOnClickListener(view -> {
			view.setVisibility(View.GONE);
			mProgressLayout.setVisibility(View.VISIBLE);
			mIvPause.setVisibility(View.VISIBLE);
			mTvProgress.setVisibility(View.VISIBLE);
			mTvProgress.setText("下载中");
			mRepository.downloadAttachment(mAttachment);
		});

		mIvPause.setOnClickListener(view -> {
			mRepository.stopDownload(mAttachment);
			view.setVisibility(View.GONE);
			mIvReStart.setVisibility(View.VISIBLE);
		});

		mIvReStart.setOnClickListener(view -> {
			mRepository.downloadAttachment(mAttachment);
			view.setVisibility(View.GONE);
			mIvPause.setVisibility(View.VISIBLE);
		});
	}

	@Override public void onAttachmentDownloadStateChange(TaskInfo taskInfo) {
		Observable.just(1)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(it -> {
					int progress = 0;
					String text = "下载中";
					if (taskInfo.fileSize != 0) {
						progress = (int) (taskInfo.downloadSize * 100 / taskInfo.fileSize);
						text = String.format("下载中(%s/%s)", Formatter.formatFileSize(this, taskInfo.downloadSize),
								Formatter.formatFileSize(this, taskInfo.fileSize));
					}
					mProgressBar.setProgress(progress);
					mTvProgress.setText(text);
				});
	}

	@Override public void onAttachmentFinalCompleted(TaskInfo taskInfo) {
		Observable.just(1)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(it -> {
					FEToast.showMessage("下载成功");
					File encryptFile = AttachmentUtils.getDownloadedAttachment(mAttachment);
					if (encryptFile != null && encryptFile.exists()) {
						tryOpenAttachment(encryptFile);
					}
				});
	}

	private void tryOpenAttachment(File encryptFile) {
		DownloadConfiguration configuration = mRepository.getDownloadConfiguration();
		File decryptFile = new File(configuration.getDecryptDir() + File.separator + mAttachment.name);

		if (decryptFile.exists() && decryptFile.lastModified() == encryptFile.lastModified()) {
			realStartOpenAttachment(mAttachment, decryptFile.getPath());
			return;
		}

		// 还是得解密
		new FeepDecrypt().decrypt(encryptFile.getPath(), decryptFile.getPath(), new IDecryptListener() {
			@Override public void onDecryptSuccess(File decryptedFile) {
				realStartOpenAttachment(mAttachment, decryptedFile.getPath());
			}

			@Override public void onDecryptProgress(int progress) { }

			@Override public void onDecryptFailed() { }
		});
	}

	private void realStartOpenAttachment(NetworkAttachment attachment, String attachmentPath) {
		if (AttachmentUtils.isAudioAttachment(attachment)) {
			AudioPlayer player = AudioPlayer.newInstance(attachment, attachmentPath);
			player.show(getSupportFragmentManager(), "Audio");
			return;
		}

		String fileType = AttachmentUtils.getAttachmentFileType(Integer.valueOf(attachment.type));
		String filePath = TextUtils.isEmpty(attachmentPath) ? attachment.path : attachmentPath;
		if (TextUtils.isEmpty(fileType)) {
			executeIntent(null);
			return;
		}
		Intent intent = AttachmentUtils.getIntent(this, filePath, fileType);
		executeIntent(intent);
	}

	private void executeIntent(Intent intent) {
		if (intent == null) {
			FEToast.showMessage("暂不支持查看此文件类型");
		}
		else {
			try {
				startActivity(intent);
			} catch (Exception exp) {
				FEToast.showMessage("无法打开，建议安装查看此类型文件的软件");
			}
		}
		finish();
	}
}