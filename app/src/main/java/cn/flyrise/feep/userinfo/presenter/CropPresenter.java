package cn.flyrise.feep.userinfo.presenter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import cn.flyrise.android.shared.model.user.UserInfo;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.event.EventUpdataUserIcon;
import cn.flyrise.feep.protocol.FeepLoginUserServices;
import cn.flyrise.feep.userinfo.contract.CropContract;
import cn.flyrise.feep.userinfo.modle.CommonResponse;
import cn.flyrise.feep.userinfo.modle.UserModifyData;
import cn.flyrise.feep.userinfo.views.CropActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017-5-15.
 */

public class CropPresenter implements CropContract.Presenter {

	private Context mContext;

	private CropContract.View mView;

	private UploadManager uploadManager;

	public CropPresenter(Context context) {
		mView = (CropActivity) context;
		mContext = context;
	}

	private String iconPath;

	/**
	 * 处理剪切成功的返回值
	 */
	@Override
	public void handleCropResult(Uri uri) {
		if (null != uri) {
			iconPath = uri.getPath();
			submitModifyText();
		}
		else {
			FEToast.showMessage(mContext.getString(R.string.can_not_crop));
		}
	}

	@Override
	public void cancleUploader() {
		if (uploadManager == null) {
			return;
		}
		uploadManager.cancelTask();
	}

	private void submitModifyText() {
		mView.showLoading();
		if (!TextUtils.isEmpty(iconPath) && new File(iconPath).exists()) {
			postModifyIcon(iconPath);
		}
		else {
			FEToast.showMessage(mContext.getString(R.string.modify_error));
			mView.modifyFailure();
		}
	}

	private void postModifyIcon(final String path) {
		if (TextUtils.isEmpty(path)) {
			return;
		}

		File file = new File(path);
		if (!file.exists()) {
			return;
		}

		List<String> paths = new ArrayList<>();
		paths.add(path);
		final FileRequestContent fileRequestContent = new FileRequestContent();
		String attachmentGUID = UUID.randomUUID().toString();
		fileRequestContent.setAttachmentGUID(attachmentGUID);
		fileRequestContent.setFiles(paths);
		fileRequestContent.setUpdateType("userImage");

		FileRequest fileRequest = new FileRequest();
		fileRequest.setFileContent(fileRequestContent);
		fileRequest.setRequestContent(UserModifyData.getDetailRequest());
		uploadManager = new UploadManager(mContext)
				.fileRequest(fileRequest)
				.progressUpdateListener(
						new OnProgressUpdateListenerImpl() {
							@Override
							public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
								int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
								mView.showProgress(progress);
							}

						}
				)
				.responseCallback(new ResponseCallback<CommonResponse>() {
					@Override
					public void onCompleted(CommonResponse responseContent) {
						mView.hideLoading();
						final String errorCode = responseContent.getErrorCode();
						final CommonResponse.result result = responseContent.result;
						if (!TextUtils.equals("0", errorCode)) {
							FEToast.showMessage(mContext.getResources().getString(R.string.modify_info_error));
							return;
						}
						FEToast.showMessage(mContext.getResources().getString(R.string.modify_info_success));
						if (result == null || TextUtils.isEmpty(result.userImage)) {
							return;
						}
						notifierDataBase(result.userImage);
						mView.modifySuccess();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						mView.modifyFailure();
					}
				});
		uploadManager.execute();
	}

	private void notifierDataBase(String imagePath) {
		if (TextUtils.isEmpty(imagePath)) {
			return;
		}
		if (imagePath.contains("\\")) {
			imagePath = imagePath.replace("\\", "/");
		}

		FeepLoginUserServices loginUserServices = (FeepLoginUserServices) CoreZygote.getLoginUserServices();
		loginUserServices.setImageHref(imagePath);

		EventUpdataUserIcon updata = new EventUpdataUserIcon();
		updata.version = imagePath;
		EventBus.getDefault().post(updata);

		String userGesturesInfo = SpUtil.get(PreferencesUtils.NINEPOINT_USER_INFO, "");
		if (TextUtils.isEmpty(userGesturesInfo)) return;
		UserInfo userInfo = GsonUtil.getInstance().fromJson(userGesturesInfo, UserInfo.class);
		if (userInfo == null) return;
		userInfo.setAvatarUrl(imagePath);
		SpUtil.put(PreferencesUtils.NINEPOINT_USER_INFO, GsonUtil.getInstance().toJson(userInfo));
	}

}
