package cn.flyrise.feep.location.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.library.utility.PhotoUtil;
import cn.flyrise.android.protocol.entity.OnsiteSignRequest;
import cn.flyrise.android.protocol.entity.location.LocationRequest;
import cn.flyrise.android.protocol.entity.location.LocationResponse;
import cn.flyrise.android.shared.utility.FEDate;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.location.ShowNetPhotoActivity;
import cn.flyrise.feep.location.bean.LocationDetailItem;
import cn.flyrise.feep.location.bean.LocationPhotoItem;
import cn.flyrise.feep.location.bean.LocationSignTime;
import cn.flyrise.feep.location.contract.LocationQueryPoiContract;
import cn.flyrise.feep.location.contract.LocationReportSignContract;
import cn.flyrise.feep.location.contract.TakePhotoSignContract;
import cn.flyrise.feep.location.contract.WorkingTimerContract;
import cn.flyrise.feep.location.contract.WorkingTimerContract.WorkingTimerListener;
import cn.flyrise.feep.location.dialog.SignInResultDialog;
import cn.flyrise.feep.location.event.EventLocationSignSuccess;
import cn.flyrise.feep.location.event.EventPhotographSignSuccess;
import cn.flyrise.feep.location.util.GpsHelper;
import cn.flyrise.feep.location.util.LocationSignDate;
import cn.flyrise.feep.location.util.RxWorkingTimer;
import cn.flyrise.feep.location.util.SignInSuccessResultUtil;
import cn.flyrise.feep.location.util.SignInUtil;
import cn.flyrise.feep.location.views.OnSiteSignActivity;
import cn.flyrise.feep.media.record.camera.CameraManager;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;

/**
 * 新建：陈冕;
 * 日期： 2017-11-20-9:49.
 */

public class OnSiteSignPresenter implements TakePhotoSignContract.Presenter
		, WorkingTimerListener {

	private Context context;
	private long fileCreate = 0;                // 文件创建的时间
	private boolean isForced = false;
	private boolean hasPhoto = false;
	private String photoPath = "";
	private String takePhoto = "";
	private Bitmap photoBitmap = null;

	private LocationPhotoItem photoItem;
	//	private PhotoUtil photoUtil;
	private CameraManager mCameraManager;
	private OnSiteSignActivity mView;
	private WorkingTimerContract mServiceTimer;
	private EventLocationSignSuccess signSuccess;

	public OnSiteSignPresenter(Context context) {
		this.context = context;
		this.mView = (OnSiteSignActivity) context;
		mServiceTimer = new RxWorkingTimer(this);
	}

	@Override
	public void getIntentData(Intent intent) {
		if (intent == null) return;// 如果是强制拍照
		final Bundle bundle = intent.getExtras();
		if (bundle == null) return;
		String data = intent.getStringExtra(LocationReportSignContract.LOCATION_PHOTO_ITEM);
		if (TextUtils.isEmpty(data)) return;
		photoItem = GsonUtil.getInstance().fromJson(data, LocationPhotoItem.class);
		isForced = photoItem != null && photoItem.takePhoto;
		initView(photoItem);
	}

	@Override
	public void getSavedTakePhoto(Bundle savedInstanceState) {
		if (mCameraManager == null) takePhoto = savedInstanceState.getString("save_instance_state");
	}

	@Override
	public void setSavedTakePhoto(Bundle outState) {
		if (mCameraManager != null) outState.putString("save_instance_state", mCameraManager.getAbsolutePath());
	}

	@Override
	public void submit(String text) {
		if (isWorkingTimeNotAllowSign()) {
			FEToast.showMessage(context.getResources().getString(R.string.location_time_over));
			return;
		}
		if (isRequestLocation())
			gpsLocationType(text);
		else
			signSubmit(text);
	}

	public boolean isWorkingTimeNotAllowSign() {//超出时间不允许签到
		return !TextUtils.isEmpty(photoItem.time)
				&& !TextUtils.isEmpty(photoItem.endWorkingSignTime)
				&& mServiceTimer != null
				&& mServiceTimer.getTimeInMillis() > FEDate.getDateSS(photoItem.endWorkingSignTime).getTime();
	}

	private boolean isRequestLocation() {
		return (photoItem.workingLatLng != null
				|| (!TextUtils.isEmpty(photoItem.latitude)
				&& !TextUtils.isEmpty(photoItem.longitude)))
				&& photoItem.takePhotoType == TakePhotoSignContract.WORKING_TAKE_PHOTO;
	}

	@Override
	public void openTakePhoto() {
		mCameraManager = new CameraManager((Activity) context);
		mCameraManager.start(CameraManager.TAKE_PHOTO_RESULT);
	}

	@Override
	public void clickDeleteView() {
		if (photoBitmap != null && !photoBitmap.isRecycled()) photoBitmap.recycle();
		deletePhoto();
		mView.setmImgPhotoView(null);
		mView.setDeleteViewVisible(false);
		hasPhoto = false;
	}

	@Override
	public void deletePhoto() {
		final File file = new File(photoPath);
		if (file.exists()) file.delete();
		if (photoBitmap != null && photoBitmap.isRecycled()) {
			photoBitmap.recycle();
			mView.setmImgPhotoView(null);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!"".equals(photoPath)) PhotoUtil.deleteFile(photoPath);
		if (mCameraManager != null) {
			photoPath = mCameraManager.getAbsolutePath();
		}
		else {
			photoPath = PhotoUtil.compressBmpToFile(takePhoto) == null ? "" : PhotoUtil.compressBmpToFile(takePhoto).getPath();
		}
		photoBitmap = PhotoUtil.decodeBitmap(photoPath);
		if (photoBitmap == null) {
			final BitmapDrawable drawable = mView.getmImgPhotoView();
			if (drawable != null) {
				photoBitmap = drawable.getBitmap();
				mView.setmImgPhotoView(photoBitmap);
			}
		}
		else {
			fileCreate = new Date().getTime();// 获得拍照的时间
			mView.setmImgPhotoView(photoBitmap);
			hasPhoto = true;
			mView.setDeleteViewVisible(true);
		}
	}

	@Override
	public void locationPermissionGranted() {
		if (photoItem == null) {
			new SignInResultDialog().setContext(context).setError("")
					.show(((AppCompatActivity) context).getSupportFragmentManager(), "signError");
			return;
		}
		EventPhotographSignSuccess sign = new EventPhotographSignSuccess();
		sign.isSearchAvitity = photoItem.type == K.location.LOCATION_SEARCH;
		sign.isTakePhotoError = photoItem.takePhotoType == TakePhotoSignContract.POIITEM_SEARCH_ERROR
				|| photoItem.takePhotoType == TakePhotoSignContract.WORKING_POIITEM_SEARCH_ERROR;
		if (signSuccess != null) {
			signSuccess.isTakePhotoError = sign.isTakePhotoError;
			sign.signSuccess = signSuccess;
		}
		Intent intent = new Intent();
		intent.putExtra("sign_in_success_data", GsonUtil.getInstance().toJson(sign));
		((Activity) context).setResult(Activity.RESULT_OK, intent);
		((Activity) context).finish();
		EventBus.getDefault().post(sign);
	}

	@Override
	public void hasPhoto() {
		if (hasPhoto) seePhotoDetail();
		else mView.permissionCamera();
	}

	private void seePhotoDetail() {
		final Intent intent = new Intent(context, ShowNetPhotoActivity.class);
		LocationDetailItem item = new LocationDetailItem();
		item.title = mView.getTitleText();
		item.address = photoItem.address;
		item.describe = mView.getDescribeText();
		item.iconUrl = photoPath;
		item.date = photoItem.time;
		intent.putExtra("location_detail_data", GsonUtil.getInstance().toJson(item));
		context.startActivity(intent);
	}

	//初始化界面
	private void initView(LocationPhotoItem photoItem) {
		if (photoItem == null) return;
		if (!TextUtils.isEmpty(photoItem.time)) {
			mServiceTimer.startServiceDateTimer(photoItem.time);
			mView.setTime(photoItem.time);
		}
		else {
			mView.setTimeVisible(false);
		}
		if (!TextUtils.isEmpty(photoItem.title)) mView.setTitle(photoItem.title);
		if (!TextUtils.isEmpty(photoItem.address)) mView.setAddress(photoItem.address);
		mView.setTitleEnabled(false);
		if (photoItem.takePhotoType == TakePhotoSignContract.WORKING_POIITEM_SEARCH_ERROR) {
			setHaedVisibile();
		}
		else if (photoItem.takePhotoType == TakePhotoSignContract.POIITEM_SEARCH_ERROR) {
			setHaedVisibile();
			mView.setTimeVisible(false);
		}
	}

	private void setHaedVisibile() {
		mView.setAddressVisible(false);
		mView.setTitleEnabled(true);
		mView.setTitle("");
		mView.setTitleNumberVisibile(true);
		mView.setEdTitleMaxLen();
	}

	private boolean isSubmit() {
		if (TextUtils.isEmpty(mView.getTitleText())) {
			FEToast.showMessage(context.getResources().getString(R.string.onsite_hint_sign_title));
			return false;
		}

		if (TextUtils.isEmpty(mView.getDescribeText())) {
			FEToast.showMessage(context.getResources().getString(R.string.onsite_edit_hint));
			return false;
		}

		if (!hasPhoto) {
			FEToast.showMessage(context.getResources().getString(R.string.onsite_sign_nophoto_toast));
			return false;
		}
		final File file = new File(photoPath);
		if (!file.exists()) {// 图片不存在了，可能删除了
			FEToast.showMessage(context.getResources().getString(R.string.lbl_text_pic_not_exist));
			return false;
		}
		final long lastFile = file.lastModified();// 文件最后修改的日期
		// 如果文件创建的时间与文件最后修改的时间超过10s，则认为用户修改过图片
		if (Math.abs(fileCreate - lastFile) > (10 * 1000)) {
			FEToast.showMessage(context.getResources().getString(R.string.lbl_text_check_not_modify));
			return false;
		}

		if (photoItem == null || "0.0".equals(photoItem.latitude) || "0.0".equals(photoItem.longitude)) {
			FEToast.showMessage(context.getResources().getString(R.string.location_sign_error));
			return false;
		}
		return true;
	}

	private void gpsLocationType(final String text) {
		new GpsHelper(context).getSingleLocation(new GpsHelper.LocationCallBack() {
			@Override
			public void success(AMapLocation location) {
				LatLng currentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
				float range = getRange(currentLatlng);
				FELog.i("自动定位", "-->>>>定位sign距离：" + range);
				FELog.writeSdCard("-->>>>拍照定位sign超过选择距离1-：" + range + "米");
				FELog.writeSdCard("-->>>>拍照定位sign超过选择距离2-：" + GsonUtil.getInstance().toJson(photoItem));
				if (range <= 0)
					signSubmit(text);
				else
					FEToast.showMessage(context.getResources().getString(R.string.location_exceed_range));
			}

			@Override
			public void error() {
			}
		});
	}

	private float getRange(LatLng currentLatlng) {
		int range = photoItem.workingRange > 0 ? photoItem.workingRange : LocationQueryPoiContract.search;
		if (photoItem.workingLatLng == null) {
			LatLng signLatlng = null;
			try {
				signLatlng = new LatLng(Double.valueOf(photoItem.latitude), Double.valueOf(photoItem.longitude));
			} catch (Exception e) {
				FEToast.showMessage(context.getResources().getString(R.string.location_sign_error));
			}
			return SignInUtil.getExceedDistance(currentLatlng, signLatlng, range);
		}
		else {
			return SignInUtil.getExceedDistance(currentLatlng, photoItem.workingLatLng, range);
		}
	}

	//提交
	private void signSubmit(String description) {
		if (GpsHelper.inspectMockLocation(context)) return;
		if (!isSubmit()) return;
		final String GUID = UUID.randomUUID().toString();
		final OnsiteSignRequest signRequest = new OnsiteSignRequest();
		if ("".equals(description))
			description = context.getResources().getString(R.string.onsite_no_description);
		signRequest.setName(description);
		signRequest.setGUID(GUID);
		signRequest.setLatitude("" + photoItem.latitude);
		signRequest.setLongitude("" + photoItem.longitude);

		String title = mView.getTitleText() == null ? "未知名称" : mView.getTitleText();
		final LocationRequest locationRequest = new LocationRequest();
		locationRequest.setSendType(photoItem.sendType);
		locationRequest.setLatitude(photoItem.latitude + "");
		locationRequest.setLongitude(photoItem.longitude + "");
		locationRequest.setGuid(GUID);
		locationRequest.setPdesc(description);
		locationRequest.setName(title);
		locationRequest.setAddress(TextUtils.isEmpty(photoItem.address) ? title : photoItem.address);

		final FileRequest fileRequest = new FileRequest();
		final FileRequestContent filerequestcontent = new FileRequestContent();

		filerequestcontent.setAttachmentGUID(GUID);
		final List<String> files = new ArrayList<>();
		files.add(photoPath);
		filerequestcontent.setFiles(files);
		filerequestcontent.setDeleteFileIds(null);

		fileRequest.setFileContent(filerequestcontent);
		fileRequest.setRequestContent(isForced ? locationRequest : signRequest);

		new UploadManager(context)
				.fileRequest(fileRequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override
					public void onPreExecute() {
						LoadingHint.show(context);
					}

					@Override
					public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
						int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
						LoadingHint.showProgress(progress);
					}
				})
				.responseCallback(new ResponseCallback<LocationResponse>() {
					@Override
					public void onCompleted(LocationResponse responseContent) {
						LoadingHint.hide();
						final String errorCode = responseContent.getErrorCode();
						final String errorMessage = responseContent.getErrorMessage();
						if (!TextUtils.equals("0", errorCode)) {
							FEToast.showMessage(errorMessage);
							return;
						}
						signSuccess = SignInSuccessResultUtil.getSignReportSuccess(responseContent.data, false);
						mView.permissionLocation();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						LoadingHint.hide();
					}
				})
				.execute();
	}

	@Override
	public void notifyRefreshServiceTime(LocationSignTime signData) {
		mView.setTime(signData.data + " " + LocationSignDate.getServiceTime(signData));
	}

	@Override
	public void onDestroy() {
		mServiceTimer.onDestroy();
	}

}
