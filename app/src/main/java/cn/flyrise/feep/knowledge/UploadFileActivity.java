package cn.flyrise.feep.knowledge;

import static cn.flyrise.feep.knowledge.FolderFileListActivity.KNOW_KEY_VALUE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.knowledge.contract.UploadFileContract;
import cn.flyrise.feep.knowledge.model.Folder;
import cn.flyrise.feep.knowledge.presenter.UploadFilePresenterImpl;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.media.attachments.LocalAttachmentListFragment;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.listener.ILocalAttachmentItemHandleListener;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.flyrise.feep.media.files.FileSelectionActivity;
import cn.flyrise.feep.media.images.ImageSelectionActivity;
import cn.flyrise.feep.media.record.camera.CameraManager;
import cn.flyrise.feep.utils.Patches;
import cn.flyrise.feep.workplan7.view.BottomWheelSelectionDialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Created by KLC on 2016/12/7.
 */
public class UploadFileActivity extends DateTimeBaseActivity
		implements UploadFileContract.View, ILocalAttachmentItemHandleListener {

	private static final String SAVE_INSTANCE_STATE = "save_instance_state";
	private List<String> mSpinnerList;
	private UploadFileContract.Presenter mPresenter;
	private FEToolbar mToolBar;
	private ImageView mIvDetailImformation;
	private LocalAttachmentListFragment mAttachmentListFragment;
	private BottomWheelSelectionDialog dialog;
	private TextView mTvRemindDays;
	private String remindTime;
	private LinearLayout mLlAddFile;
	private RelativeLayout mRlAddFile;
	private TextView mTvFileTitle;
	//    private PhotoUtil mPhotoUtil;
//    private String takePhoto;
	private CameraManager mCamera;


	public static void StartUploadFileActivity(Context context, Intent data, Folder folder, int KeyValue) {
		ArrayList<String> selectedFiles = data.getStringArrayListExtra("SelectionData");
		Intent intent = new Intent(context, UploadFileActivity.class);
		intent.putStringArrayListExtra("SelectionData", selectedFiles);
		intent.putExtra(KNOW_KEY_VALUE, KeyValue);
		intent.putExtra(KnowKeyValue.EXTRA_FOLDERID, folder.id);
		intent.putExtra(KnowKeyValue.EXTRA_ISPICFOLDER, folder.isPicFolder);
		((Activity) context).startActivityForResult(intent, KnowKeyValue.STARTUPLOADCODE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.knowledge_upload_file);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		mToolBar = toolbar;
		mToolBar.setTitle(R.string.know_upload_file);
		mToolBar.showNavigationIcon();
		mToolBar.setNavigationOnClickListener(v -> {
			if (mAttachmentListFragment.isEditMode()) {
				notifyEditModeChange(false);
				return;
			}
			finish();
		});

		mToolBar.setRightText(R.string.upload);
		mToolBar.setRightTextColor(R.color.core_default_accent_color);

		mToolBar.setRightTextClickListener(v -> {
			int toDeleteAttachmentSize = mAttachmentListFragment.getToDeleteAttachmentSize();
			if (toDeleteAttachmentSize != 0) {
				List<Attachment> toDeleteAttachments = mAttachmentListFragment.getToDeleteAttachments();
				mPresenter.deleteSelectedAttachments(toDeleteAttachments);
				mAttachmentListFragment.clearToDeleteAttachments();
			}
			if (mToolBar.getRightText().equals(getString(R.string.cancel))) {
				mToolBar.setRightText(R.string.upload);
			}
			else if (mToolBar.getRightText().equals(getString(R.string.upload))) {
				onSubmitClick();
			}
			notifyEditModeChange(false);
		});
	}

	@Override
	public void bindView() {
		super.bindView();
		mTvRemindDays = (TextView) this.findViewById(R.id.file_reminder_time_tv_days);
		mIvDetailImformation = (ImageView) findViewById(R.id.knowledge_upload_file_iv_detail);
		mLlAddFile = (LinearLayout) findViewById(R.id.knowledge_upload_file_ll_add);
		mRlAddFile = (RelativeLayout) findViewById(R.id.knowledge_upload_file_rl_add_file);
		mTvFileTitle = (TextView) findViewById(R.id.knowledge_upload_file_tv_title);
		mAttachmentListFragment = LocalAttachmentListFragment.newInstance(true, null, this);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.layoutAttachments, mAttachmentListFragment)
				.show(mAttachmentListFragment)
				.commit();
	}

	@Override
	public void bindData() {
		super.bindData();
		String mFolderID = getIntent().getStringExtra(KnowKeyValue.EXTRA_FOLDERID);
		String[] spinnerItems = getResources().getStringArray(R.array.Reminder_time);
		mSpinnerList = Arrays.asList(spinnerItems);
		mTvRemindDays.setText(mSpinnerList.get(0));
		mPresenter = new UploadFilePresenterImpl(mFolderID, this);

		if (FunctionManager.hasPatch(Patches.PATCH_KNOWLEDGE_FILTER)) {
			mPresenter.requestFilter(true, null);
			mPresenter.requestFilter(false, null);
		}

		ArrayList<String> selectedFiles = getIntent().getStringArrayListExtra("SelectionData");
		switch (getIntent().getIntExtra(KNOW_KEY_VALUE, 0)) {
			case KnowKeyValue.START_SELECT_FILE_CODE:   // 添加附件返回来的结果
				if (selectedFiles != null) {
					mPresenter.addFileAttachments(selectedFiles);
				}
				mTvFileTitle.setText(R.string.file);
				break;
			case KnowKeyValue.START_SELECT_IMAGE_CODE:  // 添加选择图片返回来的结果
				if (selectedFiles != null) {
					mPresenter.addImageAttachments(selectedFiles);
				}
				mTvFileTitle.setText(R.string.choose_attachment_photo);
				break;
			case CameraManager.TAKE_PHOTO_RESULT:
				String photoPath = getIntent().getStringExtra(KnowKeyValue.EXTRA_PHOTOPATH);
				mPresenter.addCameraImage(photoPath);
				mTvFileTitle.setText(R.string.choose_attachment_camera);
				mRlAddFile.setVisibility(View.GONE);
				findViewById(R.id.knowledge_upload_file_iv_add).setVisibility(View.GONE);
			default:
				break;
		}

	}


	@Override
	public void bindListener() {
		super.bindListener();
		mIvDetailImformation.setOnClickListener(v -> {
			Intent intent = new Intent(this, TermOfValidityActivity.class);
			startActivity(intent);
		});

		reminderLayout.setOnClickListener(v -> {
			dialog = new BottomWheelSelectionDialog();
			dialog.setTitle("选择天数");
			dialog.addValue(mSpinnerList, 0);
			dialog.setOnClickListener(new Function1<List<String>, Unit>() {
				@Override
				public Unit invoke(List<String> strings) {
					remindTime = strings.get(0);
					mTvRemindDays.setText(remindTime);
					return null;
				}
			});

			dialog.show(getSupportFragmentManager(), "");
		});

		mLlAddFile.setOnClickListener(v -> {
			addFileOrImage();
		});

		mRlAddFile.setOnClickListener(v -> {
			addFileOrImage();
		});
	}

	@Override
	public void showStartTimeMaxMessage() {
		FEToast.showMessage(getString(R.string.know_startdate_mix));
	}

	@Override
	public void showStartTimeMixMessage() {
		FEToast.showMessage(getString(R.string.know_enddate_max_startdate_forUpload));
	}

	@Override
	void onSubmitClick() {

		if (mAttachmentListFragment.isEditMode()) {
			notifyEditModeChange(false);
		}

		if (!checkTime()) {
			return;
		}
		if (!mPresenter.hasFile()) {
			showMessage(R.string.know_no_add_file);
			return;
		}
		String startTime = DateUtil.calendar2StringDateTime(startCalendar);
		String endTime = DateUtil.calendar2StringDateTime(endCalendar);
		mPresenter.uploadFile(this, remindTime, startTime, endTime);

	}

	@Override
	public void onRefreshList(List<Attachment> attachments) {
		mAttachmentListFragment.setAttachments(attachments);
		if (CommonUtil.isEmptyList(attachments)) {
			mRlAddFile.setVisibility(View.VISIBLE);
		}
		else {
			mRlAddFile.setVisibility(View.GONE);
		}
	}

	@Override
	public void showUploadProgress(int progress) {
		LoadingHint.showProgress(progress, getString(R.string.know_uploading));
	}

	@Override
	public void uploadFinish() {
		setResult(RESULT_OK);
		this.finish();
	}

	@Override
	public void showDealLoading(boolean show) {
		if (show) {
			LoadingHint.show(this);
		}
		else {
			LoadingHint.hide();
		}
	}

	@Override
	public void showMessage(int resourceID) {
		FEToast.showMessage(getString(resourceID));
	}

	@Override
	public void finish() {
		super.finish();
		mPresenter.clearData();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override
	public void onAttachmentItemClick(int position, Attachment attachment) {
		if (mAttachmentListFragment.isEditMode()) {
			mAttachmentListFragment.addAttachmentToDelete(position, attachment);
			notifyEditModeChange(true);
			return;
		}

		String fileType = AttachmentUtils.getAttachmentFileType(Integer.valueOf(attachment.type));
		String filePath = attachment.path;
		if (TextUtils.isEmpty(fileType)) {
			FEToast.showMessage("暂不支持查看此文件类型");
			return;
		}
		Intent intent = AttachmentUtils.getIntent(this, filePath, fileType);
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

	@Override
	public void onAttachmentItemLongClick(Attachment attachment) {
		if (mAttachmentListFragment.isEditMode()) {
			return;
		}
		notifyEditModeChange(true);
	}

	@Override
	public void onAttachmentItemToBeDeleteCheckChange() {
		notifyEditModeChange(true);
	}

	private void notifyEditModeChange(boolean isEditMode) {
		if (isEditMode != mAttachmentListFragment.isEditMode()) {
			mAttachmentListFragment.setEditMode(isEditMode);
			mAttachmentListFragment.getAdapter().notifyDataSetChanged();
		}

		if (isEditMode) {
			int toDeleteAttachmentSize = mAttachmentListFragment.getToDeleteAttachmentSize();
			mToolBar.getRightTextView().setVisibility(View.VISIBLE);
			mToolBar.setRightText(
					toDeleteAttachmentSize == 0 ? getString(R.string.cancel) : String.format("删除(%d)", toDeleteAttachmentSize));
			return;
		}

		mAttachmentListFragment.clearToDeleteAttachments();
//        mToolBar.getRightTextView().setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		if (mAttachmentListFragment.isEditMode()) {
			notifyEditModeChange(false);
			return;
		}
		super.onBackPressed();
	}

	@Override
	protected void openDatePicket(Calendar calendar, boolean canClear) {
		super.openDatePicket(calendar, canClear);
		if (mAttachmentListFragment.isEditMode()) {
			notifyEditModeChange(false);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			notifyEditModeChange(false);
			mToolBar.getRightTextView().setVisibility(View.VISIBLE);
			mToolBar.setRightText(R.string.upload);
			mToolBar.setRightTextColor(R.color.core_default_accent_color);
			ArrayList<String> selectedFiles;
			switch (getIntent().getIntExtra(KNOW_KEY_VALUE, 0)) {
				case CameraManager.TAKE_PHOTO_RESULT:
					if (TextUtils.isEmpty(mCamera.getAbsolutePath())) {
						return;
					}
					mPresenter.addCameraImage(mCamera.getAbsolutePath());
					findViewById(R.id.knowledge_upload_file_iv_add).setVisibility(View.GONE);
					break;
				case KnowKeyValue.START_SELECT_FILE_CODE:   // 添加附件返回来的结果
					selectedFiles = data.getStringArrayListExtra("SelectionData");
					if (selectedFiles != null) {
						mPresenter.addFileAttachments(selectedFiles);
					}
					break;
				case KnowKeyValue.START_SELECT_IMAGE_CODE:  // 添加选择图片返回来的结果
					selectedFiles = data.getStringArrayListExtra("SelectionData");
					if (selectedFiles != null) {
						mPresenter.addImageAttachments(selectedFiles);
					}
					break;
			}
		}
	}

	private void addFileOrImage() {
		switch (getIntent().getIntExtra(KNOW_KEY_VALUE, 0)) {
			case CameraManager.TAKE_PHOTO_RESULT:
				mCamera.start(CameraManager.TAKE_PHOTO_RESULT);
				break;
			case KnowKeyValue.START_SELECT_FILE_CODE:   // 添加附件
				Intent intent = new Intent(UploadFileActivity.this, FileSelectionActivity.class);
				intent.putExtra("extra_except_path", CoreZygote.getPathServices().getUserPath());
				intent.putExtra("extra_expect_type", mPresenter.getFileTypeList());
				intent.putExtra("extra_max_select_count", mPresenter.remaining());

				ArrayList<String> selectedFiles = (ArrayList<String>) mPresenter.getSelectedFilePaths();
				intent.putStringArrayListExtra("extra_selected_files", selectedFiles);
				startActivityForResult(intent, KnowKeyValue.START_SELECT_FILE_CODE);
				break;
			case KnowKeyValue.START_SELECT_IMAGE_CODE:  // 添加图片
				Intent intentImage = new Intent(UploadFileActivity.this, ImageSelectionActivity.class);
				intentImage.putExtra("extra_except_path", CoreZygote.getPathServices().getUserPath());
				intentImage.putExtra("extra_expect_type", mPresenter.getImageTypeList());
				intentImage.putExtra("extra_max_select_count", mPresenter.remaining());

				ArrayList<String> selectedImages = (ArrayList<String>) mPresenter.getSelectedImagePaths();
				intentImage.putStringArrayListExtra("extra_selected_files", selectedImages);
				startActivityForResult(intentImage, KnowKeyValue.START_SELECT_IMAGE_CODE);
				break;
		}
	}

}
