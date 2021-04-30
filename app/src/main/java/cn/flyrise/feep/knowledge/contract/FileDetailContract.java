package cn.flyrise.feep.knowledge.contract;


import android.content.Intent;
import cn.flyrise.feep.knowledge.model.FileDetail;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;

/**
 * Created by KLC on 2016/12/6.
 */

public interface FileDetailContract {

	interface View extends KnowBaseContract.View {

		void showFileDetail(FileDetail file);

		void showProgress(int resourceID, int progress);

		void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener onClickListener);

		void showDownLayout(boolean show);

		void openFile(Intent intent);
	}

	interface Presenter {

		void getFileDetailById(String fileId);

		boolean haveDownloaded(FileDetail file);

		void openFile(FileDetail file);
	}


	interface LoadDetailCallBack {

		void loadSuccess(FileDetail fileDetail);

		void loadError();
	}

}