package cn.flyrise.feep.knowledge.presenter;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.knowledge.FilterRequest;
import cn.flyrise.android.protocol.entity.knowledge.FilterResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.knowledge.contract.UploadFileContract;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.repository.AttachmentConverter;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.flyrise.feep.utils.Patches;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KLC on 2016/12/7.
 */

public class UploadFilePresenterImpl implements UploadFileContract.Presenter {

	private static final int LIMIT = 20;
	private String mFolderID;
	private UploadFileContract.View mView;

	private boolean requestPicFilterSuccess;
	private List<String> picTypeList;

	private boolean requestDocFilterSuccess;
	private List<String> docTypeList;

	private List<String> mCameraImages;
	private List<String> mTempSelectedFiles;
	private List<String> mTempSelectedImages;
	private List<Attachment> mSelectedAttachments;

	public UploadFilePresenterImpl(String mFolderID, UploadFileContract.View view) {
		this.mFolderID = mFolderID;
		this.mView = view;
		this.mSelectedAttachments = new ArrayList<>();

		mCameraImages = new ArrayList<>();
		mTempSelectedFiles = new ArrayList<>();
		mTempSelectedImages = new ArrayList<>();

		String picFilter = ".jpg,.bmp,.png,.gif,.jpeg";
		String docFilter = ".jpg,.bmp,.png,.gif,.jpeg,.doc,.docx,.txt,.log,.pdf,.ppt,.xls,.html,.xlsx,.htm,.pptx,.vsd,.swf,.dot";
		picTypeList = stringToList(picFilter);
		docTypeList = stringToList(docFilter);

		if (!FunctionManager.hasPatch(Patches.PATCH_KNOWLEDGE_FILTER)) {
			requestPicFilterSuccess = true;
			requestDocFilterSuccess = true;
		}
	}

	@Override
	public void uploadFile(Context context, String remindTime, String startTime, String endTime) {
		final FileRequest filerequest = new FileRequest();
		final FileRequestContent fileRequestContent = new FileRequestContent();
		String version = "1.0";//新建时是固定的
		Map<String, String> keyValue = new HashMap<>();
		keyValue.put("fid", mFolderID);
		keyValue.put("remindertime", remindTime);
		keyValue.put("failuretime", startTime);
		keyValue.put("endtime", endTime);
		keyValue.put("version", version);

		fileRequestContent.setValueMap(keyValue);
		fileRequestContent.setFiles(getSelectedAttachmentPaths());
		fileRequestContent.setUpdateType("knowledge");
		filerequest.setFileContent(fileRequestContent);

		final UploadManager uploadManager = new UploadManager(context, true);
		uploadManager.fileRequest(filerequest).progressUpdateListener(new OnProgressUpdateListenerImpl() {
			@Override
			public void onPreExecute() {
				mView.showDealLoading(true);
				LoadingHint.setOnKeyDownListener((keyCode, event) -> uploadManager.cancelTask());
			}

			@Override
			public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
				int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
				mView.showUploadProgress(progress);
			}

			@Override
			public void onPostExecute(String jsonBody) {
				super.onPostExecute(jsonBody);
				mView.showMessage(R.string.know_upload_success);
				mView.showDealLoading(false);
				mView.uploadFinish();
			}

			@Override
			public void onFailExecute(Throwable ex) {
				ex.printStackTrace();
				mView.showMessage(R.string.know_upload_error);
				mView.showDealLoading(false);
			}
		}).execute();
	}

	@Override
	public boolean hasFile() {
		return CommonUtil.nonEmptyList(mSelectedAttachments);
	}

	@Override
	public void clearData() {
		mSelectedAttachments.clear();
	}

	@Override
	public int getSelectedAttachmentCount() {
		return CommonUtil.isEmptyList(mSelectedAttachments) ? 0 : mSelectedAttachments.size();
	}

	@Override
	public void requestFilter(boolean isPic, UploadFileContract.GetFilterListener listener) {
		if (listener != null) {
			mView.showDealLoading(true);
		}
		FEHttpClient.getInstance().post(new FilterRequest(isPic), new ResponseCallback<FilterResponse>() {
			@Override
			public void onCompleted(FilterResponse response) {
				if ("1119".equals(response.getErrorCode())) {
					requestPicFilterSuccess = true;
					requestDocFilterSuccess = true;
					mView.showDealLoading(false);
					return;
				}
				if (isPic) {
					picTypeList = stringToList(response.result);
					requestPicFilterSuccess = true;
				}
				else {
					docTypeList = stringToList(response.result);
					requestDocFilterSuccess = true;
				}
				if (listener != null) {
					listener.onSuccess();
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				if (listener != null) {
					FEToast.showMessage("获取文件上传限制类型失败");
					mView.showDealLoading(false);
				}
			}
		});
	}

	private List<String> stringToList(String strFilter) {
		if (!TextUtils.isEmpty(strFilter)) {
			String[] filter = strFilter.split(",");
			return Arrays.asList(filter);
		}
		else {
			return null;
		}
	}

	@Override
	public String[] getImageTypeList() {
		return picTypeList.toArray(new String[]{});
	}

	/**
	 * 获取选中附件中，图片附件的路径
	 */
	@Override public List<String> getSelectedImagePaths() {
		List<String> selectedImages = new ArrayList<>();
		if (CommonUtil.nonEmptyList(mSelectedAttachments)) {
			for (Attachment attachment : mSelectedAttachments) {
				if (AttachmentUtils.isImageAttachment(attachment)) {
					selectedImages.add(attachment.path);
				}
			}
		}
		return selectedImages;
	}

	@Override
	public String[] getFileTypeList() {
		return docTypeList.toArray(new String[]{});
	}

	public boolean isRequestPicFilterSuccess() {
		return requestPicFilterSuccess;
	}

	public boolean isRequestDocFilterSuccess() {
		return requestDocFilterSuccess;
	}

	/**
	 * 获取选择附件的路径，包括
	 */
	@Override public List<String> getSelectedFilePaths() {
		return getSelectedAttachmentPaths();
	}

	/**
	 * 获取已选择的附件路径
	 */
	private List<String> getSelectedAttachmentPaths() {
		List<String> paths = new ArrayList<>();
		if (CommonUtil.nonEmptyList(mSelectedAttachments)) {
			for (Attachment attachment : mSelectedAttachments) {
				paths.add(attachment.path);
			}
		}
		return paths;
	}

	@Override public int remaining() {
		return LIMIT - getSelectedAttachmentCount();
	}

	@Override public void addFileAttachments(List<String> selectedFiles) {
		addLocalAttachments(selectedFiles, mTempSelectedFiles);
	}

	@Override public void addImageAttachments(List<String> selectedPaths) {
		addLocalAttachments(selectedPaths, mTempSelectedImages);
	}

	@Override
	public void addCameraImage(String selectedFile) {
		Attachment attachment = AttachmentConverter.convertAttachment(selectedFile);
		if (attachment != null) {
			if (AttachmentUtils.isImageAttachment(attachment)) {
				// 保存拍照的照骗
				mCameraImages.add(attachment.path);
			}
			else {
				mTempSelectedFiles.add(attachment.path);
			}
			mSelectedAttachments.add(attachment);
			mView.onRefreshList(mSelectedAttachments);
		}
	}

	private void addLocalAttachments(List<String> selectedFiles, List<String> tempSelectedFiles) {
		if (CommonUtil.isEmptyList(tempSelectedFiles)) {
			tempSelectedFiles.addAll(selectedFiles);
			if (executeLocalAttachmentAdd(selectedFiles)) {
				mView.onRefreshList(mSelectedAttachments);
			}
			return;
		}

		if (CommonUtil.isEmptyList(selectedFiles)) {    // 智障 test case
			List<Attachment> toDeleteAttachments = AttachmentConverter.convertAttachments(tempSelectedFiles);
			if (!CommonUtil.isEmptyList(toDeleteAttachments)) {
				deleteSelectedAttachments(toDeleteAttachments);
			}

			tempSelectedFiles.clear();
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
			deleteSelectedAttachments(toDeleteAttachments);
		}

		if (executeLocalAttachmentAdd(tempSelectedFiles)) {
			mView.onRefreshList(mSelectedAttachments);
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
			if (!mSelectedAttachments.contains(attachment)) {
				mSelectedAttachments.add(attachment);
			}
		}
		return true;
	}

	@Override public void deleteSelectedAttachments(List<Attachment> selectedAttachments) {
		if (CommonUtil.nonEmptyList(selectedAttachments)) {
			mSelectedAttachments.removeAll(selectedAttachments);
			mView.onRefreshList(mSelectedAttachments);
		}
	}

	@Override public boolean hasRemaining() {
		return getSelectedAttachmentCount() < LIMIT;
	}

	@Override public int limit() {
		return LIMIT;
	}


}
