package cn.flyrise.feep.knowledge.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collection.FavoriteRepository;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.knowledge.MoveFileAndFolderActivity;
import cn.flyrise.feep.knowledge.ShareFileActivity;
import cn.flyrise.feep.knowledge.UploadFileActivity;
import cn.flyrise.feep.knowledge.contract.FolderFileListContract;
import cn.flyrise.feep.knowledge.contract.KnowBaseContract;
import cn.flyrise.feep.knowledge.contract.RenameCreateContract;
import cn.flyrise.feep.knowledge.model.FileAndFolder;
import cn.flyrise.feep.knowledge.model.Folder;
import cn.flyrise.feep.knowledge.model.FolderManager;
import cn.flyrise.feep.knowledge.repository.FolderFileListRepository;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.knowledge.util.WaittingDownloadQueue;
import cn.flyrise.feep.knowledge.view.BasePopwindow;
import cn.flyrise.feep.knowledge.view.PopwindowKnowLedgeMoreMenu;
import cn.flyrise.feep.knowledge.view.PopwindowKnowLedgeUpload;
import cn.flyrise.feep.media.attachments.AttachmentViewer;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.SimpleAttachmentViewerListener;
import cn.flyrise.feep.media.attachments.repository.AttachmentDataSource;
import cn.flyrise.feep.more.download.manager.DownLoadManagerTabActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by KLC on 2016/12/6.
 */

public class FolderFileListPresenterImpl implements FolderFileListContract.Presenter {

	private FolderManager mManager;
	private Folder mNowFolder;
	private FolderFileListContract.View mView;
	private FolderFileListRepository mRepository;
	private Map<String, Folder> mLocalFolder;
	private RenameCretePresenterImpl renamePresenter;

	private AttachmentViewer mViewer;
	private WaittingDownloadQueue mDownloadQueue;
	private PopwindowKnowLedgeMoreMenu popwindowKnowLedgeMoreMenu;
	private PopwindowKnowLedgeUpload popwindowKnowLedgeUpload;
	private FavoriteRepository mCollectionRepository;

	public FolderFileListPresenterImpl(FolderManager manager, FolderFileListContract.View view, RenameCreateContract.View renameView) {
		this.mManager = manager;
		this.mView = view;
		this.mLocalFolder = new HashMap<>();
		this.mNowFolder = manager.nowFolder;
		this.mRepository = new FolderFileListRepository(this.mManager.folderType);
		this.renamePresenter = new RenameCretePresenterImpl(renameView, manager.folderType);

		DownloadConfiguration configuration = new DownloadConfiguration.Builder()
				.owner(CoreZygote.getLoginUserServices().getUserId())
				.downloadDir(CoreZygote.getPathServices().getKnowledgeCachePath())
				.encryptDir(CoreZygote.getPathServices().getSafeFilePath())
				.decryptDir(CoreZygote.getPathServices().getTempFilePath())
				.create();
		AttachmentDataSource dataSource = new AttachmentDataSource(CoreZygote.getContext());
		this.mViewer = new AttachmentViewer(dataSource, configuration);
		this.mViewer.setAttachmentViewerListener(new XSimpleAttachmentViewerListener());
		this.mDownloadQueue = new WaittingDownloadQueue(dataSource, configuration);
	}

	@Override
	public void onStart() {
		mLocalFolder.put(mNowFolder.id, mNowFolder);
		refreshListData();
		setPermission();
	}

	@Override
	public void refreshListData() {
		mView.showRefreshLoading(true);
		mRepository.loadListData(mNowFolder.id, mNowFolder.currentPage = 1, new FolderFileListContract.LoadListCallback() {
			@Override
			public void loadListDataSuccess(List<FileAndFolder> dataList, int totalPage) {
				mView.showRefreshLoading(false);
				mView.refreshListData(dataList);
				mNowFolder.totalPage = totalPage;
				mView.setCanPullUp(hasMoreData());
			}

			@Override
			public void loadListDataError() {
				mView.showRefreshLoading(false);
				mView.setEmptyView();
			}
		});
	}

	@Override
	public void loadMoreData() {
		mRepository.loadListData(mNowFolder.id, ++mNowFolder.currentPage, new FolderFileListContract.LoadListCallback() {
			@Override
			public void loadListDataSuccess(List<FileAndFolder> dataList, int totalPage) {
				mNowFolder.totalPage = totalPage;
				mView.loadMoreListData(dataList);
				mView.setCanPullUp(hasMoreData());
			}

			@Override
			public void loadListDataError() {
				mView.loadMoreListFail();
				mNowFolder.currentPage--;
			}
		});
	}

	@Override
	public void openFile(Context context, FileAndFolder openFolder) {
		mView.showDealLoading(true);
		String url = FEHttpClient.getInstance().getHost() + FEHttpClient.KNOWLEDGE_DOWNLOAD_PATH + openFolder.fileid;
		String taskId = openFolder.fileid;
		String fileName = openFolder.getFileRealName();
		mViewer.openAttachment(url, taskId, fileName);

		LoadingHint.setOnKeyDownListener((keyCode, event) -> {
			TaskInfo taskInfo = mViewer.createTaskInfo(url, taskId, fileName);
			mViewer.getDownloader().deleteDownloadTask(taskInfo);
		});
	}


	@Override
	public  void openFolder(FileAndFolder clickItem) {
		if (mLocalFolder.containsKey(clickItem.folderid)) {
			mNowFolder = mLocalFolder.get(clickItem.folderid);
		}
		else {
			if (mManager.folderType == KnowKeyValue.FOLDERTYPE_PERSON) {
				boolean isPicFolder = mNowFolder.isPicFolder;
				mNowFolder = Folder.CreatePersonFolder(mNowFolder.id, clickItem.folderid, clickItem.foldername, mNowFolder.level + 1);
				mNowFolder.isPicFolder = isPicFolder;
			}
			else {
				mNowFolder = Folder.CreateUnitFolder(mNowFolder.id, clickItem.folderid, clickItem.foldername, mNowFolder.level + 1,
						clickItem.rightPower, clickItem.canManage);
			}
			mLocalFolder.put(mNowFolder.id, mNowFolder);
		}
		List<FileAndFolder> dataList = mRepository.getLocalData(mNowFolder.id);
		if (dataList == null) {
			refreshListData();
		}
		else {
			mView.refreshListData(dataList);
			mView.setCanPullUp(hasMoreData());
		}
		setPermission();
	}

	private void setPermission() {
		if (mNowFolder.canManage) {
			mView.setFloatEnable(true, true);
		}
		else {
			mView.setFloatEnable(false, mNowFolder.uploadPermission);
		}
	}

	@Override
	public void setPermission(int choiceCount, List<FileAndFolder> foldersList, List<FileAndFolder> fileLists) {
		boolean canDown = mNowFolder.downPermission,
				canPublish = mNowFolder.publishPermission,
				canRename = mNowFolder.renameFilePermission,
				canMove = mNowFolder.moveFilePermission,
				canDelete = mNowFolder.deleteFilePermission,
				canCollect = false,
				canMore = true;

		if (choiceCount == 0) {
			mView.showBottomMenu(false);
			return;
		}
		mView.showEditMode(true);
		mView.setFloatEnable(false,false);
		mView.showBottomMenu(true);
		if (!canMove && !canRename) {
			canMore = false;
		}

		if (foldersList.size() > 0) {
			canDown = false;
			canPublish = false;
		}

		//对于文件
		for(FileAndFolder file:fileLists){
			if(TextUtils.isEmpty(file.favoriteId)){
				canCollect = true;
			}
		}
		if (choiceCount > 1) {
			canRename = false;
			canCollect = false;
		}



		if (mManager.folderType == KnowKeyValue.FOLDERTYPE_PERSON) {
			mView.setBottomEnable(canDown, canPublish, canRename, canMove, canDelete, canCollect, canMore);
			return;
		}
		//对于文件夹
		for (FileAndFolder folder : foldersList) {
			if (!folder.canManage) {
				canDelete = false;
				canMove = false;
				canRename = false;
				break;
			}
		}
		if (choiceCount > 1) {
			canRename = false;
			canCollect = false;
		}

		mView.setBottomEnable(canDown, canPublish, canRename, canMove, canDelete, canCollect, canMove);

	}

	@Override
	public boolean hasMoreData() {
		return mNowFolder.currentPage < mNowFolder.totalPage;
	}

	@Override
	public void downloadFile(Context context, List<FileAndFolder> dataList) {
		dataList = getChoiceItem(dataList);
		boolean isAllCompleted = true;
		for (FileAndFolder item : dataList) {
			String taskId = item.fileid;
			String fileName = item.getFileRealName();
			String url = FEHttpClient.getInstance().getHost() + FEHttpClient.KNOWLEDGE_DOWNLOAD_PATH + item.fileid;

			if (mDownloadQueue.enqueue(url, taskId, fileName)) {
				isAllCompleted = false;
				continue;
			}

			isAllCompleted = true;
		}

		mView.dealComplete();
		if (isAllCompleted) {
			mView.showMessage(R.string.know_has_been_downloaded);
			DownLoadManagerTabActivity.startDownLoadManagerTabActivity(context, 1);
		}
		else {
			// 添加到下载目录。
			// 就是他妈的 fileid
			ArrayList<String> prepareDownloadTasks = new ArrayList<>();
			for (FileAndFolder file : dataList) {
				prepareDownloadTasks.add(file.fileid);
			}
			mView.showMessage(R.string.know_add_to_downList);
			DownLoadManagerTabActivity.startAndSwitchToDownloadingView(context, prepareDownloadTasks, true);
		}
	}

	@Override
	public void moveFileAndFolder(Context context, List<FileAndFolder> dataList) {
		dataList = getChoiceItem(dataList);
		StringBuilder folders = new StringBuilder();
		StringBuilder fileIds = new StringBuilder();
		ArrayList<String> fileType = new ArrayList<>();
		for (FileAndFolder item : dataList) {
			if (item.isFolder()) {
				folders.append(",").append(item.folderid);
			}
			else {
				fileIds.append('\'').append(item.fileid).append('\'').append(",");
				fileType.add(item.filetype);
			}
		}
		if (folders.length() > 0) folders.deleteCharAt(0);
		String moveFiles = fileIds.toString();
		String moveFolders = folders.toString();
		FolderManager manager = new FolderManager(mManager.folderType, mManager.isRootFolderManager,
				Folder.CreateRootFolder(mManager.folderType));
		MoveFileAndFolderActivity.startMoveActivity(context, mNowFolder.id, moveFolders, moveFiles, fileType, manager);
	}

	@Override
	public void publishFile(Context context, List<FileAndFolder> dataList) {
		dataList = getChoiceItem(dataList);
		StringBuilder fileIds = new StringBuilder();
		for (FileAndFolder item : dataList) {
			fileIds.append(item.fileid).append(',');
		}
		ShareFileActivity.startKPublicFileActivity(context, fileIds.toString(), mNowFolder.id);
	}

	@Override
	public void createFolder(String userID) {
		renamePresenter.createFolder(userID, mNowFolder.id, mNowFolder.level + 1);
	}

	@Override
	public void renameFolderOrFile(String userID, List<FileAndFolder> dataList) {
		dataList = getChoiceItem(dataList);
		FileAndFolder clickItem = dataList.get(0);
		if (clickItem.isFolder()) {
			if (mManager.folderType == KnowKeyValue.FOLDERTYPE_PERSON) {
				renamePresenter.renameFolder(mNowFolder.id, clickItem);
			}
			else {
				renamePresenter.renameUnitFolder(clickItem, mNowFolder.level + 1, userID);
			}
		}
		else {
			renamePresenter.renameFile(clickItem);
		}
	}

	@Override
	public void deleteFolderAndFile(List<FileAndFolder> folders) {
		folders = getChoiceItem(folders);
		StringBuilder foldIDs = new StringBuilder();
		StringBuilder fileIDs = new StringBuilder();
		for (FileAndFolder item : folders) {
			if (item.isFolder()) {
				foldIDs.append(item.folderid).append(",");
			}
			else {
				fileIDs.append('\'').append(item.fileid).append('\'').append(",");
			}
		}
		mView.showConfirmDialog(R.string.know_delete_file_or_folder, dialog -> {
			mView.showDealLoading(true);
			mRepository.deleteFolderAndFile(fileIDs.toString(), foldIDs.toString(), new KnowBaseContract.DealWithCallBack() {
				@Override
				public void success() {
					mView.showDealLoading(false);
					mView.showMessage(R.string.delete_success);
					mView.dealComplete();
					refreshListData();
				}

				@Override
				public void fail() {
					mView.showDealLoading(false);
					mView.showMessage(R.string.delete_fail);
				}
			});
		});
	}

	@Override
	public void uploadFile(Context context, Intent data, int keyValue) {
		UploadFileActivity.StartUploadFileActivity(context, data, mNowFolder, keyValue);
	}

	@Override
	public void cancleCollectFile(String favoriteId,String fildId, String type) {
		if (mCollectionRepository == null) {
			mCollectionRepository = new FavoriteRepository();
		}
		mCollectionRepository.removeFromFolder(favoriteId, fildId, type)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(result -> {
					LoadingHint.hide();
					if (result.errorCode == 0) {
						FEToast.showMessage("取消收藏成功");
						mView.collectionSuccess(true);
						return;
					}
					FEToast.showMessage(result.errorMessage);
				}, exception -> {
					LoadingHint.hide();
					FEToast.showMessage("取消收藏失败，请稍后重试！");
				});
	}

	@Override
	public void backToPrentFolder() {
		if (TextUtils.isEmpty(mNowFolder.parentFolderID)) {
			mView.finish();
		}
		else {
			mNowFolder = mLocalFolder.get(mNowFolder.parentFolderID);
			List<FileAndFolder> dataList = mRepository.getLocalData(mNowFolder.id);
			mView.refreshListData(dataList);
			mView.setCanPullUp(hasMoreData());
			mView.setTitle(mNowFolder.name);
			setPermission();
		}
	}

	@Override
	public void showPopwindowRenameAndMove(Activity activity, View viewParent,boolean canMove, boolean canRename,
			BasePopwindow.PopwindowMenuClickLister lister) {
		popwindowKnowLedgeMoreMenu = new PopwindowKnowLedgeMoreMenu()
				.setCanMove(canMove)
				.setCanRename(canRename)
				.setListener(lister);
		popwindowKnowLedgeMoreMenu.show(((AppCompatActivity)activity).getSupportFragmentManager(),"showFragment");
	}

	@Override
	public void showPopwindowUploadFile(Activity activity, View viewParent, BasePopwindow.PopwindowMenuClickLister lister) {
		popwindowKnowLedgeUpload = new PopwindowKnowLedgeUpload(
				activity, viewParent,
				RelativeLayout.LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true, 1.0f, lister);

		popwindowKnowLedgeUpload.showPopwindow(Gravity.NO_GRAVITY);
	}

	@Override
	public void dismissPopwindow() {
		if (popwindowKnowLedgeMoreMenu != null) {
			popwindowKnowLedgeMoreMenu.dismiss();
		}
		if (popwindowKnowLedgeUpload != null) {
			popwindowKnowLedgeUpload.dismiss();
		}
	}

    @Override
    public void dealCompleted() {
        setPermission();
    }

    private List<FileAndFolder> getChoiceItem(List<FileAndFolder> dataList) {
		List<FileAndFolder> choiceList = new ArrayList<>();
		for (FileAndFolder folder : dataList) {
			if (folder.isChoice) {
				choiceList.add(folder);
			}
		}
		return choiceList;
	}

	private class XSimpleAttachmentViewerListener extends SimpleAttachmentViewerListener {

		@Override public void prepareOpenAttachment(Intent intent) {
			mView.showDealLoading(false);
			mView.openFile(intent);
		}

		@Override public void onDownloadFailed() {
			mView.showMessage(R.string.know_open_fail);
			mView.showDealLoading(false);
		}

		@Override public void onDownloadProgressChange(int progress) {
			mView.showProgress(R.string.know_opening, progress);
		}

		@Override public void onDecryptFailed() {
			mView.showMessage(R.string.know_open_fail);
			mView.showDealLoading(false);
		}

		@Override public void onDecryptProgressChange(int progress) {
			mView.showProgress(R.string.know_decode_open, progress);
		}
	}
}
