package cn.flyrise.feep.knowledge.contract;


import android.content.Context;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import java.util.List;

/**
 * Created by KLC on 2016/12/7.
 */

public interface UploadFileContract {

	interface View extends KnowBaseContract.View {

		void onRefreshList(List<Attachment> attachments);

		void showUploadProgress(int progress);

		void uploadFinish();
	}

	interface Presenter {

		void addCameraImage(String path);

		void uploadFile(Context context, String remindTime, String startTime, String endTime);

		boolean hasFile();

		void clearData();

		int getSelectedAttachmentCount();

		String[] getImageTypeList();

		List<String> getSelectedImagePaths();

		boolean isRequestPicFilterSuccess();

		String[] getFileTypeList();

		boolean isRequestDocFilterSuccess();

		List<String> getSelectedFilePaths();

		void requestFilter(boolean isPic, UploadFileContract.GetFilterListener listener);

		int remaining();

		boolean hasRemaining();

		int limit();

		void addFileAttachments(List<String> selectedPaths);

		void addImageAttachments(List<String> selectedPaths);

		void deleteSelectedAttachments(List<Attachment> toDeletedAttachments);

	}

	interface GetFilterListener {

		void onSuccess();
	}
}
