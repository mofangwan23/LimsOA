package cn.flyrise.feep.knowledge.contract;

import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.view.View;

import java.util.List;

import cn.flyrise.feep.knowledge.model.FileAndFolder;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.knowledge.view.BasePopwindow;

/**
 * Created by KLC on 2016/12/6.
 */

public interface FolderFileListContract {

	interface View extends ListContract.View<FileAndFolder> {

		void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener onClickListener);

		void setTitle(String title);

		void showProgress(int text, int progress);

		void setBottomEnable(boolean canDown, boolean canPublish, boolean canRename, boolean canMove, boolean canDelete, boolean canCollect,boolean canMore);

		void setFloatEnable(boolean canCreate, boolean canUpload);

		void showBottomMenu(boolean isShowBottomMenu);

		void finish();

		void openFile(Intent intent);

		void showEditMode(boolean isEditState);

		void collectionSuccess(boolean isCancle);

	}

	interface Presenter {

		void onStart();

		void refreshListData();

		void loadMoreData();

		boolean hasMoreData();

		void openFile(Context context, FileAndFolder openFolder);

		void openFolder(FileAndFolder clickItem);

		void setPermission(int choiceCount, List<FileAndFolder> foldersList, List<FileAndFolder> fileLists);

		void downloadFile(Context context, List<FileAndFolder> dataList);

		void moveFileAndFolder(Context context, List<FileAndFolder> dataList);

		void publishFile(Context context, List<FileAndFolder> dataList);

		void createFolder(String userID);

		void renameFolderOrFile(String userID, List<FileAndFolder> dataList);

		void deleteFolderAndFile(List<FileAndFolder> folders);

		void uploadFile(Context context, Intent data, int keyValue);

		void cancleCollectFile(String favoriteId, String fildId, String type);

		void backToPrentFolder();

		void showPopwindowRenameAndMove(Activity activity, android.view.View viewParent, boolean canMove, boolean canRename, BasePopwindow.PopwindowMenuClickLister lister);

		void showPopwindowUploadFile(Activity activity, android.view.View viewParent, BasePopwindow.PopwindowMenuClickLister lister);

		void dismissPopwindow();

		void dealCompleted();

	}


	interface LoadListCallback {

		void loadListDataSuccess(List<FileAndFolder> dataList, int totalPage);

		void loadListDataError();
	}

	interface FolderTypeCallBack {

		void callBack(boolean isPic, boolean isDoc);

		void onError();
	}

}