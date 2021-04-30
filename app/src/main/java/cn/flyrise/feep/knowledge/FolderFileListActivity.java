package cn.flyrise.feep.knowledge;

import static cn.flyrise.feep.meeting7.ui.component.StatusViewKt.STATE_EMPTY;
import static cn.flyrise.feep.particular.ParticularActivity.CODE_SELECT_COLLECTION_FOLDER;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collection.CollectionFolderActivity;
import cn.flyrise.feep.collection.CollectionFolderFragment;
import cn.flyrise.feep.collection.FavoriteRepository;
import cn.flyrise.feep.collection.bean.CollectionEvent;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.PullAndLoadMoreRecyclerView;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.dialog.FEMaterialEditTextDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.knowledge.adpater.FolderFileListAdapter;
import cn.flyrise.feep.knowledge.contract.FolderFileListContract;
import cn.flyrise.feep.knowledge.contract.RenameCreateContract;
import cn.flyrise.feep.knowledge.contract.UploadFileContract;
import cn.flyrise.feep.knowledge.model.FileAndFolder;
import cn.flyrise.feep.knowledge.model.FolderManager;
import cn.flyrise.feep.knowledge.presenter.FolderFileListPresenterImpl;
import cn.flyrise.feep.knowledge.presenter.UploadFilePresenterImpl;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.knowledge.view.BasePopwindow;
import cn.flyrise.feep.knowledge.view.PopwindowKnowLedgeUpload;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.files.FileSelectionActivity;
import cn.flyrise.feep.media.images.ImageSelectionActivity;
import cn.flyrise.feep.media.record.camera.CameraManager;
import cn.flyrise.feep.meeting7.ui.component.StatusView;
import cn.flyrise.feep.utils.Patches;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by KLC on 2016/12/6.
 */

public class FolderFileListActivity extends BaseActivity implements FolderFileListContract.View,
		UploadFileContract.View, RenameCreateContract.View {

	private static final String SAVE_INSTANCE_STATE = "save_instance_state";
	public static final String KNOW_KEY_VALUE = "KnowKeyValue";
	private FEToolbar mToolBar;
	private LinearLayout mBottomMenu;
	private LinearLayout mDownLayout;
	private LinearLayout mShareLayout;
	private LinearLayout mRenameLayout;
	private LinearLayout mMoveLayout;
	private LinearLayout mDeleteLayout;
	private LinearLayout mCollectLayout;
	private LinearLayout mMoreLayout;
	private LinearLayout mCreateAndUpload;
	private StatusView mStatusView;
	private PullAndLoadMoreRecyclerView mListView;
	private RelativeLayout mRlSearch;
	private ImageView mIvNewFolder;
	private ImageView mIvUploadFile;
	private ImageView mIvCollect;
	private TextView mTvCollect;

	private FolderFileListAdapter mAdapter;
	private FolderFileListContract.Presenter mPresenter;
	private UploadFileContract.Presenter mPresenterUploadFile;
	private Handler mHandler;
	private int searchType;
	private static final String SEARCH_TYPE = "search_TYPE";
	private String userId;
	//	private PhotoUtil mPhotoUtil;
//	private String takePhoto = "";
	private CameraManager mCamera;
	private boolean canRename, canMove;
	private FolderManager folderManager;
	private FavoriteRepository mRepository;
	private PopwindowKnowLedgeUpload.PopwindowMenuClickLister popwindowClickLister;

	public static void startChildFileListActivity(Context context, FolderManager folderManager) {
		Intent intent = new Intent(context, FolderFileListActivity.class);
		intent.putExtra(KnowKeyValue.EXTRA_FOLDERMANAGER, folderManager);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (FunctionManager.hasPatch(Patches.PATCH_COLLECTIONS)) {
			mRepository = new FavoriteRepository();
		}
		setContentView(R.layout.knowledge_main);
		mCamera = new CameraManager(this);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		this.mToolBar = toolbar;
		toolbar.setLineVisibility(View.VISIBLE);
		toolbar.setLeftText(getString(R.string.dialog_default_cancel_button_text));
		toolbar.showNavigationIcon();

	}

	@Override
	public void bindView() {
		super.bindView();
		mBottomMenu = (LinearLayout) findViewById(R.id.llBottomMenu);
		mDownLayout = (LinearLayout) findViewById(R.id.down_layout);
		mShareLayout = (LinearLayout) findViewById(R.id.share_layout);
		mRenameLayout = (LinearLayout) findViewById(R.id.rename_layout);
		mMoveLayout = (LinearLayout) findViewById(R.id.move_layout);
		mDeleteLayout = (LinearLayout) findViewById(R.id.delete_layout);
		mCollectLayout = (LinearLayout) findViewById(R.id.collect_layout);
		mMoreLayout = (LinearLayout) findViewById(R.id.more_layout);
		mCreateAndUpload = (LinearLayout) findViewById(R.id.layout_knowledge_create_and_upload);
		mRlSearch = (RelativeLayout) findViewById(R.id.layout_knowledge_rl_Search);
		findViewById(R.id.cancel_publish_layout).setVisibility(View.GONE);
		mStatusView = (StatusView) findViewById(R.id.knowledge_statusview);
		mListView = (PullAndLoadMoreRecyclerView) findViewById(R.id.listview);
		mIvNewFolder = (ImageView) findViewById(R.id.layout_knowledge_search_iv_new_folder);
		mIvUploadFile = (ImageView) findViewById(R.id.layout_knowledge_search_iv_upload_file);
		mIvCollect = (ImageView) findViewById(R.id.iv_collect);
		mTvCollect = (TextView) findViewById(R.id.tv_collect);
		mIvUploadFile.setVisibility(View.VISIBLE);
		mMoveLayout.setVisibility(View.GONE);
		mRenameLayout.setVisibility(View.GONE);
		if (FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) { //7.0以上才有收藏功能
			mCollectLayout.setVisibility(View.VISIBLE);
		}
		else {
			mCollectLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void bindData() {
		super.bindData();
		mHandler = new Handler();
		mAdapter = new FolderFileListAdapter(this, false);
		mListView.setAdapter(mAdapter);
		folderManager = getIntent().getParcelableExtra(KnowKeyValue.EXTRA_FOLDERMANAGER);
		mPresenter = new FolderFileListPresenterImpl(folderManager, this, this);
		mPresenterUploadFile = new UploadFilePresenterImpl(folderManager.nowFolder.id, this);
		mHandler.postDelayed(() -> mPresenter.onStart(), 500);
		mToolBar.setTitle(folderManager.nowFolder.name);
		Intent intent = getIntent();
		if (intent != null) {
			searchType = intent.getIntExtra(SEARCH_TYPE, -1);
		}

	}

	@Override
	public void bindListener() {
		super.bindListener();
		userId = CoreZygote.getLoginUserServices().getUserId();
		mToolBar.setRightTextClickListener(v -> selectCancel());
		mToolBar.setNavigationOnClickListener(v -> onBackPressed());
		mToolBar.setLeftTextClickListener(v -> onBackPressed());
		mDownLayout.setOnClickListener(v -> mPresenter.downloadFile(FolderFileListActivity.this, mAdapter.getDataList()));
		mShareLayout.setOnClickListener(v -> mPresenter.publishFile(FolderFileListActivity.this, mAdapter.getDataList()));
		mRenameLayout.setOnClickListener(v -> mPresenter.renameFolderOrFile(userId, mAdapter.getDataList()));
		mMoveLayout.setOnClickListener(v -> mPresenter.moveFileAndFolder(FolderFileListActivity.this, mAdapter.getDataList()));
		mDeleteLayout.setOnClickListener(v -> mPresenter.deleteFolderAndFile(mAdapter.getDataList()));
		mMoreLayout.setOnClickListener(v -> {
			mPresenter.showPopwindowRenameAndMove(FolderFileListActivity.this,
					findViewById(R.id.knowledge_main_relative_layout), canMove, canRename, popwindowClickLister);

		});
		mCollectLayout.setOnClickListener(v -> {
			if (mTvCollect.getText().toString().equals("收藏")) {
				Intent intent = new Intent(this, CollectionFolderActivity.class);
				intent.putExtra("mode", CollectionFolderFragment.MODE_SELECT);
				startActivityForResult(intent, CODE_SELECT_COLLECTION_FOLDER);
			}
			else {
				if (mAdapter.getSelectedFiles().size() > 0) {
					FileAndFolder file = mAdapter.getSelectedFiles().get(0);
					if (file == null) {
						return;
					}
					mPresenter.cancleCollectFile(file.favoriteId, file.fileid, RequestType.Knowledge + "");
				}
			}

		});
		mListView.setRefreshListener(() -> mPresenter.refreshListData());
		mListView.setLoadMoreListener(() -> mPresenter.loadMoreData());
		mAdapter.setChoiceListener(
				(choiceCount, clickFolderList, clickFileList) -> mPresenter.setPermission(choiceCount, clickFolderList, clickFileList));
		mAdapter.setOnItemClickListener(fileAndFolder -> {
			if (!mAdapter.isEditStand) {
				mCreateAndUpload.setVisibility(View.VISIBLE);
				if (fileAndFolder.isFolder()) {
					mAdapter.refreshData(null);
					mPresenter.openFolder(fileAndFolder);
					mToolBar.setTitle(fileAndFolder.foldername);
				}
				else
					mPresenter.openFile(FolderFileListActivity.this, fileAndFolder);
			}
		});

		mAdapter.setOnItemLongClickListener((view, object) -> FolderFileListActivity.this.onItemLongClick());

		mRlSearch.setOnClickListener(v -> {
			KnowledgeSearchActivity.StartSearchListActivity(this, searchType);
		});

		mIvNewFolder.setOnClickListener(v -> {
			mPresenter.createFolder(userId);
		});

		mIvUploadFile.setOnClickListener(v -> {
			mPresenter.showPopwindowUploadFile(FolderFileListActivity.this, findViewById(R.id.knowledge_main_relative_layout),
					popwindowClickLister);
		});

		popwindowClickLister = new BasePopwindow.PopwindowMenuClickLister() {
			@Override
			public void setPopWindowClicklister(View view) {
				switch (view.getId()) {
					case R.id.layout_popwindow_upload_file_rl_take_photo:
						FePermissions.with(FolderFileListActivity.this)
								.permissions(new String[]{Manifest.permission.CAMERA})
								.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
								.requestCode(PermissionCode.CAMERA)
								.request();
						break;

					case R.id.layout_popwindow_upload_file_rl_select_picter:
						if (mPresenterUploadFile.isRequestPicFilterSuccess()) {
							Intent intent = new Intent(FolderFileListActivity.this, ImageSelectionActivity.class);
							intent.putExtra("extra_except_path", CoreZygote.getPathServices().getUserPath());
							intent.putExtra("extra_expect_type", mPresenterUploadFile.getImageTypeList());
							intent.putExtra("extra_max_select_count", mPresenterUploadFile.remaining());

							ArrayList<String> selectedImages = (ArrayList<String>) mPresenterUploadFile.getSelectedImagePaths();
							intent.putStringArrayListExtra("extra_selected_files", selectedImages);
							startActivityForResult(intent, KnowKeyValue.START_SELECT_IMAGE_CODE);
						}
						else {
							requestFilter(true, view);
						}
						break;
					case R.id.layout_popwindow_upload_file_rl_select_file:
						if (mPresenterUploadFile.isRequestDocFilterSuccess()) {
							Intent intent = new Intent(FolderFileListActivity.this, FileSelectionActivity.class);
							intent.putExtra("extra_except_path", CoreZygote.getPathServices().getUserPath());
							intent.putExtra("extra_expect_type", mPresenterUploadFile.getFileTypeList());
							intent.putExtra("extra_max_select_count", mPresenterUploadFile.remaining());

							ArrayList<String> selectedFiles = (ArrayList<String>) mPresenterUploadFile.getSelectedFilePaths();
							intent.putStringArrayListExtra("extra_selected_files", selectedFiles);
							startActivityForResult(intent, KnowKeyValue.START_SELECT_FILE_CODE);
						}
						else {
							requestFilter(false, view);
						}
						break;
					case R.id.layout_knowledge_moremenu_tv_move:
						mPresenter.moveFileAndFolder(FolderFileListActivity.this, mAdapter.getDataList());
						break;
					case R.id.layout_knowledge_moremenu_tv_rename:
						mPresenter.renameFolderOrFile(userId, mAdapter.getDataList());
						mPresenter.dismissPopwindow();
						break;
				}
			}
		};
	}


	@Override
	public void onBackPressed() {
		if (mAdapter.isEditStand) {
			selectCancel();
		}
		else {
			mPresenter.backToPrentFolder();
		}
	}


	@Override
	public void showDealLoading(boolean show) {
		if (show)
			LoadingHint.show(this);
		else
			LoadingHint.hide();
	}

	@Override
	public void showRefreshLoading(boolean show) {
		if (show)
			mListView.setRefreshing(true);
		else
			mHandler.postDelayed(() -> mListView.setRefreshing(false), 500);
	}

	@Override
	public void refreshListData(List<FileAndFolder> dataList) {
		mAdapter.refreshData(dataList);
		setEmptyView();
		mListView.scroll2Top();
		selectCancel();
	}

	@Override
	public void setEmptyView() {
		if (mAdapter.getItemCount() == 0) {
			mStatusView.setVisibility(View.VISIBLE);
			mStatusView.setText("这是个空文件夹");
			mStatusView.setStatus(STATE_EMPTY);
		}
		else {
			mStatusView.setVisibility(View.GONE);
		}
	}

	@Override
	public void loadMoreListData(List<FileAndFolder> dataList) {
		mAdapter.addData(dataList);
	}

	@Override
	public void loadMoreListFail() {
		mListView.scrollLastItem2Bottom();
	}


	@Override
	public void showMessage(int resourceID) {
		FEToast.showMessage(getString(resourceID));
		mPresenter.dismissPopwindow();
	}

	@Override
	public void dealComplete() {
		selectCancel();
	}

	@Override public void showErrorMessage(String errorMessage) {
		FEToast.showMessage(errorMessage);
		mPresenter.dismissPopwindow();
	}

	@Override
	public void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener onClickListener) {
		new FEMaterialDialog.Builder(this).setMessage(getString(resourceID))
				.setPositiveButton(null, onClickListener)
				.setNegativeButton(null, null)
				.build()
				.show();
	}


	@Override
	public void setTitle(String title) {
		mToolBar.setTitle(title);
	}

	@Override
	public void showProgress(int text, int progress) {
		LoadingHint.showProgress(progress, getString(text));
	}

	@Override
	public void setBottomEnable(boolean canDown, boolean canPublish, boolean canRename, boolean canMove, boolean canDelete,
			boolean canCollect, boolean canMore) {
		setEnable(mDownLayout, canDown);
		setEnable(mRenameLayout, canRename);
		setEnable(mMoveLayout, canMove);
		setEnable(mShareLayout, canPublish);
		setEnable(mDeleteLayout, canDelete);
		setEnable(mMoreLayout, canMore);

		this.canRename = canRename;
		this.canMove = canMove;
		if (!canMove && !canRename) {
			mMoreLayout.setVisibility(View.GONE);
		}
		else {
			mMoreLayout.setVisibility(View.VISIBLE);
		}

		if (mAdapter.getSelectedFiles().size() > 1 || mAdapter.getClickFolderList().size() > 0) {
			setEnable(mCollectLayout, false);
			mIvCollect.setImageResource(R.drawable.icon_collect_file_gray);
			mTvCollect.setText("收藏");
		}
		else {
			setEnable(mCollectLayout, true);
			if (canCollect) {
				mIvCollect.setImageResource(R.drawable.icon_collect_file_gray);
				mTvCollect.setText("收藏");
			}
			else {
				mTvCollect.setText("已收藏");
				mIvCollect.setImageResource(R.drawable.icon_collect_file_light);
			}
		}

		if (!canDelete) {
			mDeleteLayout.setVisibility(View.GONE);
			mShareLayout.setVisibility(View.GONE);
			if (FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) { //7.0以上才有收藏功能
				mCollectLayout.setVisibility(View.VISIBLE);
			}
			else {
				mCollectLayout.setVisibility(View.GONE);
			}
		}


	}

	@Override
	public void setFloatEnable(boolean canCreate, boolean canUpload) {
		setEnable(mIvNewFolder, canCreate);
		setEnable(mIvUploadFile, canUpload);
	}


	@Override
	public void showInputDialog(int titleResourceID, int hintResourceID, String checkBoxText,
			FEMaterialEditTextDialog.OnClickListener onClickListener) {
		new FEMaterialEditTextDialog.Builder(this)
				.setTitle(getString(titleResourceID))
				.setHint(getString(hintResourceID))
				.setPositiveButton("确定", onClickListener)
				.setNegativeButton("", null)
				.setCheckBoxText(checkBoxText)
				.build()
				.show();
	}

	@Override
	public void refreshList() {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void refreshListByNet() {
		mPresenter.refreshListData();
	}


	@Override
	public void setCanPullUp(boolean hasMore) {
		if (hasMore)
			mListView.addFootView();
		else
			mListView.removeFootView();
	}


	public void showBottomMenu(boolean show) {
		if (show) {
			mBottomMenu.setVisibility(View.VISIBLE);
			mToolBar.setRightText(R.string.cancel);
		}
		else {
			mBottomMenu.setVisibility(View.GONE);
		}
	}

	@Override
	public void openFile(Intent intent) {
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
	public void showEditMode(boolean isEditState) {
		if (isEditState) {
			mListView.setCanRefresh(false);
		}
		else {
			mListView.setCanRefresh(true);
		}
	}

	@Override
	public void collectionSuccess(boolean isCancle) {
		mTvCollect.setText("收藏");
		mIvCollect.setImageResource(R.drawable.icon_collect_file_gray);
		mPresenter.onStart();
	}

	public void setEnable(View view, boolean enable) {
		if (enable) {
			view.setAlpha(1);
			view.setEnabled(true);
		}
		else {
			view.setAlpha(0.3f);
			view.setEnabled(false);
		}
	}

	private void onItemLongClick() {
		if (mAdapter.isEditStand) {
			mAdapter.setCanChoice(false);
			mAdapter.isEditStand = false;
			mListView.setCanRefresh(true);
			showBottomMenu(false);
		}
		else {
			mAdapter.setCanChoice(true);
			mAdapter.isEditStand = true;
			mListView.setCanRefresh(false);
			showBottomMenu(true);
		}
	}

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGrated() {
		mCamera.start(CameraManager.TAKE_PHOTO_RESULT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case KnowKeyValue.STARTMOVECODE:
					mPresenter.refreshListData();
					mPresenter.dismissPopwindow();
					break;
				case KnowKeyValue.STARTPUBLISHCODE:
					selectCancel();
					break;
				case KnowKeyValue.STARTUPLOADCODE:
					mPresenter.refreshListData();
					mPresenter.dismissPopwindow();
					break;
				case KnowKeyValue.START_SELECT_FILE_CODE:   // 添加附件返回来的结果
					if (data != null) {
						mPresenter.uploadFile(FolderFileListActivity.this, data, KnowKeyValue.START_SELECT_FILE_CODE);
						showDealLoading(false);
					}
					break;
				case KnowKeyValue.START_SELECT_IMAGE_CODE:  // 添加选择图片返回来的结果
					if (data != null) {
						mPresenter.uploadFile(FolderFileListActivity.this, data, KnowKeyValue.START_SELECT_IMAGE_CODE);
						showDealLoading(false);
					}
					break;
				case CameraManager.TAKE_PHOTO_RESULT:
					if (!mCamera.isExistPhoto()) {
						return;
					}
					Intent intent = new Intent(FolderFileListActivity.this, UploadFileActivity.class);
					intent.putExtra(KNOW_KEY_VALUE, CameraManager.TAKE_PHOTO_RESULT);
					intent.putExtra(KnowKeyValue.EXTRA_PHOTOPATH, mCamera.getAbsolutePath());
					intent.putExtra(KnowKeyValue.EXTRA_FOLDERID, folderManager.nowFolder.id);
					intent.putExtra(KnowKeyValue.EXTRA_ISPICFOLDER, folderManager.nowFolder.isPicFolder);
					startActivityForResult(intent, KnowKeyValue.STARTUPLOADCODE);
					break;
				case CODE_SELECT_COLLECTION_FOLDER:
					if (mRepository != null) {
						String favoriteId = data.getStringExtra("favoriteId");
						List<FileAndFolder> selectedFiles = mAdapter.getSelectedFiles();
						if (!TextUtils.isEmpty(favoriteId) && CommonUtil.nonEmptyList(selectedFiles)) {
							showDealLoading(true);
							FileAndFolder file = selectedFiles.get(0);
							mRepository.addToFolder(favoriteId, file.fileid, Func.Knowledge + "",
									file.title + file.filetype, file.sendUserId, file.pubTime)
									.subscribeOn(Schedulers.io())
									.observeOn(AndroidSchedulers.mainThread())
									.subscribe(result -> {
										showDealLoading(false);
										if (result.errorCode == 0) {
											FEToast.showMessage("添加成功");
											mAdapter.restoreOriginalState(false);
											showBottomMenu(false);
											EventBus.getDefault().post(new CollectionEvent(200));
											mPresenter.onStart();
											return;
										}
										FEToast.showMessage(result.errorMessage);
									}, exception -> {
										showDealLoading(false);
										FEToast.showMessage("添加收藏失败，请稍后重试！");
									});
						}
					}
			}
		}
	}

	@Override
	public void onRefreshList(List<Attachment> attachments) {

	}

	@Override
	public void showUploadProgress(int progress) {

	}

	@Override
	public void uploadFinish() {

	}

	public void selectCancel() {
		mAdapter.restoreOriginalState(false);
		showBottomMenu(false);
		mToolBar.setRightText(null);
		mPresenter.dealCompleted();
		showEditMode(false);
	}

	private void requestFilter(boolean isPic, View view) {
		mPresenterUploadFile.requestFilter(isPic, () -> popwindowClickLister.setPopWindowClicklister(view));
	}

	@Override
	protected void onResume() {
		super.onResume();
		showDealLoading(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPresenter.dismissPopwindow();
	}
}
