package cn.flyrise.feep.media.attachments;

import static cn.flyrise.feep.core.CoreZygote.getLoginUserServices;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.services.IPathServices;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.attachments.adapter.AttachmentListAdapter;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration.Builder;
import cn.flyrise.feep.media.attachments.listener.IAttachmentItemHandleListener;
import cn.flyrise.feep.media.attachments.listener.IDownloadProgressCallback;
import cn.flyrise.feep.media.common.SelectionSpec;
import cn.flyrise.feep.media.files.FileSelectionActivity;
import cn.flyrise.feep.media.images.ImageSelectionActivity;
import cn.flyrise.feep.media.record.RecordActivity;
import cn.flyrise.feep.media.record.camera.CameraManager;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.ResultExtras;
import cn.squirtlez.frouter.annotations.Route;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-10-25 11:54
 * 附件选择、编辑（只有删除） 界面
 */
@Route("/media/attachments")
@RequestExtras({
		/**
		 * 傻逼用户已经选择过的本地文件
		 * 【ArrayList<String> 类型：intent.putStringArrayListExtra("extra_local_file", localAttachments)】
		 */
		"extra_local_file",

		/**
		 * 智障用户已经选择的远程网络附件
		 * 【ArrayList<NetworkAttachment> 类型: intent.putParcelableArrayListExtra("extra_network_file", networkAttachments)】
		 */
		"extra_network_file"
})

/**
 * 返回用户最终确定的附件，有两个可能的数据，本地附件、远程网络附件
 */
@ResultExtras({
		"extra_local_file",     // 【ArrayList<String> 类型：intent.getStringArrayListExtra("extra_local_file")】
		"extra_network_file"    // 【ArrayList<NetworkAttachment> 类型: intent.getParcelableArrayListExtra("extra_network_file")】
})
public class AttachmentListActivity extends BaseActivity
		implements AttachmentListView, OnClickListener, IAttachmentItemHandleListener, IDownloadProgressCallback {

	public static final String EXTRA_LOCAL_FILE = "extra_local_file";
	public static final String EXTRA_NETWORK_FILE = "extra_network_file";
	public static final int CODE_CHOICE_FILE = 1;       // 选择文件
	public static final int CODE_TAKE_PHOTOS = 2;       // 拍照
	public static final int CODE_CHOICE_IMAGE = 3;      // 选择图片
	public static final int CODE_RECORD = 4;            // 录音

	private View mFileOption;
	private View mCameraOption;
	private View mImageOption;
	private View mRecordOption;

	private TextView mTvSelectedCount;
	private CheckBox mSelectAllDeleteBtn;

	private FEToolbar mToolBar;
	private FELoadingDialog mLoadingDialog;
	private AttachmentListAdapter mAdapter;
	private RecyclerView mRecyclerView;

	private CameraManager mCamera;//拍照管理
	private AttachmentListPresenter mPresenter;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IPathServices pathService = CoreZygote.getPathServices();
		DownloadConfiguration configuration = new Builder()
				.owner(getLoginUserServices().getUserId())
				.downloadDir(pathService.getDownloadDirPath())
				.encryptDir(pathService.getSafeFilePath())
				.decryptDir(pathService.getTempFilePath())
				.create();

		mPresenter = new AttachmentListPresenter(this, configuration);
		setContentView(R.layout.ms_activity_attachment_list);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
		this.mToolBar.setLineVisibility(View.GONE);
		this.mToolBar.setTitle("选择附件");
	}

	@Override public void bindView() {
		mFileOption = findViewById(R.id.msLayoutFileOption);
		mFileOption.setOnClickListener(this);
		mCameraOption = findViewById(R.id.msLayoutCameraOption);
		mCameraOption.setOnClickListener(this);
		mImageOption = findViewById(R.id.msLayoutImageOption);
		mImageOption.setOnClickListener(this);
		mRecordOption = findViewById(R.id.msLayoutRecordOption);
		mRecordOption.setOnClickListener(this);

		mTvSelectedCount = (TextView) findViewById(R.id.msTvAttachmentSelectedCount);
		mSelectAllDeleteBtn = (CheckBox) findViewById(R.id.msCbxAttachmentSelectAll);

		mRecyclerView = (RecyclerView) findViewById(R.id.msRecyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(null);
		mRecyclerView.setAdapter(mAdapter = new AttachmentListAdapter());
		mAdapter.setDownloadProgressCallback(this);
		mAdapter.setOnAttachmentItemHandleListener(this);
	}

	@Override public void bindData() {
		Intent intent = getIntent();
		ArrayList<String> localAttachments = intent.getStringArrayListExtra(EXTRA_LOCAL_FILE);                            // 本地附件
		ArrayList<NetworkAttachment> networkAttachments = intent.getParcelableArrayListExtra(EXTRA_NETWORK_FILE);         // 远程附件
		mPresenter.initialize(localAttachments, networkAttachments);
		mCamera = new CameraManager(this);
	}

	@Override public void bindListener() {
		mToolBar.setNavigationOnClickListener(v -> {
			if (mAdapter.isEditMode()) {
				notifyEditModeChange(false);
				return;
			}

			setResult(Activity.RESULT_OK, mPresenter.fillResultData());
			finish();
		});

		mToolBar.setRightTextClickListener(v -> {
			int toDeleteAttachmentSize = mAdapter.getToDeleteAttachmentSize();
			if (toDeleteAttachmentSize != 0) {
				List<Attachment> toDeleteAttachments = mAdapter.getToDeleteAttachments();
				mPresenter.deleteAttachment(toDeleteAttachments);
				mAdapter.clearToDeleteAttachments();
			}
			notifyEditModeChange(false);
		});

		mSelectAllDeleteBtn.setOnClickListener(v -> {
			mAdapter.notifyAllAttachmentDeleteState(mSelectAllDeleteBtn.isChecked());
			notifyEditModeChange(true);
		});
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE_TAKE_PHOTOS && resultCode == Activity.RESULT_OK) {
			if (mCamera.isExistPhoto()) {
				if (mLoadingDialog != null) {
					mLoadingDialog.hide();
					mLoadingDialog = null;
				}
				mLoadingDialog = new FELoadingDialog.Builder(this).setCancelable(false).create();
				Observable
						.create((OnSubscribe<? extends String>) f -> {
							String imagePath = mCamera.getAbsolutePath();
							if (!TextUtils.isEmpty(imagePath)) f.onNext(imagePath);
							else f.onError(new NullPointerException("Empty photo path."));
							f.onCompleted();
						})
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(mPresenter::addAttachment,
								exception -> {},
								() -> {
									if (mLoadingDialog != null) {
										mLoadingDialog.hide();
										mLoadingDialog = null;
									}
								});
			}
			return;
		}

		if (resultCode != Activity.RESULT_OK || data == null) {
			return;
		}

		if (requestCode == CODE_CHOICE_FILE) {
			ArrayList<String> selectedFiles = data.getStringArrayListExtra("SelectionData");
			mPresenter.addFileAttachments(selectedFiles);
			return;
		}

		if (requestCode == CODE_CHOICE_IMAGE) {
			ArrayList<String> selectedImages = data.getStringArrayListExtra("SelectionData");
			mPresenter.addImageAttachments(selectedImages);
			return;
		}

		if (requestCode == CODE_RECORD) {
			String record = data.getStringExtra("Record");
			mPresenter.addAttachment(record);
		}
	}

	/**
	 * Note: 这个 OnClick 方法只给顶部，其他控件需要 onClick 事件自己写去
	 */
	@Override public void onClick(View v) {
		notifyEditModeChange(false);    // 退出编辑模式
		if (!mPresenter.hasRemaining()) {
			FEToast.showMessage("选择的附件不能多于" + mPresenter.limit() + "个");
			return;
		}

		int viewId = v.getId();
		if (viewId == R.id.msLayoutFileOption) {
			Intent intent = new Intent(this, FileSelectionActivity.class);
			intent.putExtra(SelectionSpec.EXTRA_MAX_SELECT_COUNT, mPresenter.remaining());              // 最大选择个数
			ArrayList<String> selectedFiles = (ArrayList<String>) mPresenter.getLocalSelectedFiles();
			intent.putStringArrayListExtra(SelectionSpec.EXTRA_SELECTED_FILES, selectedFiles);          // 已选择的附件

			intent.putExtra(SelectionSpec.EXTRA_EXCEPT_PATH,                                            // 不加载指定路径下的文件
					new String[]{CoreZygote.getPathServices().getUserPath()});

			startActivityForResult(intent, CODE_CHOICE_FILE);
			return;
		}

		if (viewId == R.id.msLayoutImageOption) {
			Intent intent = new Intent(this, ImageSelectionActivity.class);
			intent.putExtra(SelectionSpec.EXTRA_MAX_SELECT_COUNT, mPresenter.remaining());
			ArrayList<String> selectedImages = (ArrayList<String>) mPresenter.getLocalImageSelectedImages();
			intent.putStringArrayListExtra(SelectionSpec.EXTRA_SELECTED_FILES, selectedImages);

			intent.putExtra(SelectionSpec.EXTRA_EXCEPT_PATH,
					new String[]{CoreZygote.getPathServices().getUserPath()});

			startActivityForResult(intent, CODE_CHOICE_IMAGE);
			return;
		}

		if (viewId == R.id.msLayoutCameraOption) {  // 申请权限
			FePermissions.with(this)
					.permissions(new String[]{Manifest.permission.CAMERA})
					.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
					.requestCode(PermissionCode.CAMERA)
					.request();
			return;
		}

		if (viewId == R.id.msLayoutRecordOption) {  // 申请权限
			FePermissions.with(this)
					.permissions(new String[]{Manifest.permission.RECORD_AUDIO})
					.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
					.requestCode(PermissionCode.RECORD)
					.request();
		}
	}

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGrated() {    // 打开相机
		mCamera.start(CODE_TAKE_PHOTOS);
	}

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {   // 进入录音
		Intent intent = new Intent(this, RecordActivity.class);
		startActivityForResult(intent, CODE_RECORD);
	}

	@Override public void showSelectedAttachments(List<Attachment> selectedAttachments) {
		mAdapter.setAttachments(selectedAttachments);
		notifyEditModeChange(false);
	}

	@Override public void attachmentDownloadProgressChange(int position) {
		mAdapter.notifyItemChanged(position);
	}

	@Override public void decryptProgressChange(int progress) {
		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog.Builder(this).setCancelable(false).create();
		}
		mLoadingDialog.updateProgress(progress);
		mLoadingDialog.show();
	}

	@Override public void decryptFileFailed() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		FEToast.showMessage("文件解密失败，请重试！");
	}

	@Override public void errorMessageReceive(String errorMessage) {                        // 显示错误信息
		FEToast.showMessage(errorMessage);
	}

	@Override public void playAudioAttachment(Attachment attachment, String audioPath) {    // 播放录音
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		AudioPlayer player = AudioPlayer.newInstance(attachment, audioPath);
		player.show(getSupportFragmentManager(), "Audio");
	}

	@Override public void openAttachment(Intent intent) {                               // 调用第三方 APP 打开相关附件
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}

		if (intent == null) {
			FEToast.showMessage("暂不支持查看此文件类型");
			return;
		}

		try {
			startActivity(intent);
		} catch (Exception exp) {
			FEToast.showMessage("无法打开，建议安装查看此类型文件的软件");
		}
	}

	@Override public void onAttachmentItemClick(int position, Attachment attachment) {
		if (mAdapter.isEditMode()) {
			mAdapter.addAttachmentToDelete(position, attachment);
			notifyEditModeChange(true);
			return;
		}
		mPresenter.openAttachment(attachment);
	}

	@Override public void onAttachmentItemLongClick(Attachment attachment) {                // 长按附件
		if (mAdapter.isEditMode()) {
			return;
		}
		notifyEditModeChange(true);
	}

	@Override public void onAttachmentItemToBeDeleteCheckChange() {                         // 添加待删除的附件
		notifyEditModeChange(true);
	}

	@Override public void onAttachmentDownloadStopped(Attachment attachment) {      // 暂停下载
		mPresenter.stopAttachmentDownload(attachment);
	}

	@Override public void onAttachmentDownloadResume(Attachment attachment) {       // 恢复下载
		mPresenter.downloadAttachment(attachment);
	}

	@Override public DownloadProgress downloadProgress(Attachment attachment) {
		return mPresenter.getAttachmentDownloadProgress(attachment);
	}

	@Override public void onBackPressed() {
		if (mAdapter.isEditMode()) {
			notifyEditModeChange(false);
			return;
		}

		setResult(Activity.RESULT_OK, mPresenter.fillResultData());
		super.onBackPressed();
	}

	/**
	 * 进入 or 退出删除模式
	 */
	private void notifyEditModeChange(boolean isEditMode) {
		if (isEditMode != mAdapter.isEditMode()) {               // 已经处于 Edit Mode 的 Adapter 没必要再次进行界面刷新操作
			mAdapter.setEditMode(isEditMode);
			mAdapter.notifyDataSetChanged();
		}

		if (isEditMode) {
			int toDeleteAttachmentSize = mAdapter.getToDeleteAttachmentSize();
			mToolBar.getRightTextView().setVisibility(View.VISIBLE);
			mToolBar.setRightText(toDeleteAttachmentSize == 0 ? "取消" : String.format("删除(%d)", toDeleteAttachmentSize));

			int totalItemCount = mAdapter.getItemCount();
			mTvSelectedCount.setTextColor(Color.parseColor("#f96262"));                 // 红色
			mTvSelectedCount.setText("已选：" + toDeleteAttachmentSize);                   // 待删除的个数
			mSelectAllDeleteBtn.setChecked(toDeleteAttachmentSize == totalItemCount);
			mSelectAllDeleteBtn.setText(mSelectAllDeleteBtn.isChecked() ? "全不选" : "全选");
			mSelectAllDeleteBtn.setVisibility(View.VISIBLE);
			return;
		}

		mAdapter.clearToDeleteAttachments();
		mToolBar.getRightTextView().setVisibility(View.GONE);

		int totalItemCount = mAdapter.getItemCount();
		mTvSelectedCount.setTextColor(Color.parseColor("#EDEDED"));
		mTvSelectedCount.setText("已选：" + totalItemCount);
		mSelectAllDeleteBtn.setVisibility(View.GONE);
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}
}