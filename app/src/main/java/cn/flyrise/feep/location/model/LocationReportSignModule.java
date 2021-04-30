package cn.flyrise.feep.location.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.location.LocationRequest;
import cn.flyrise.android.protocol.entity.location.LocationResponse;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.sign;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.commonality.util.ListDataProvider;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.location.Sign.error;
import cn.flyrise.feep.location.bean.LocationPhotoItem;
import cn.flyrise.feep.location.bean.PhotoSignTempData;
import cn.flyrise.feep.location.contract.LocationReportSignContract;
import cn.flyrise.feep.location.contract.TakePhotoSignContract;
import cn.flyrise.feep.location.dialog.SignInResultDialog;
import cn.flyrise.feep.location.event.EventLocationSignSuccess;
import cn.flyrise.feep.location.util.GpsHelper;
import cn.flyrise.feep.location.util.SignInSuccessResultUtil;
import cn.flyrise.feep.location.util.SignInUtil;
import cn.flyrise.feep.location.views.OnSiteSignActivity;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-12-19-15:38.
 * 签到数据上传模块、拍照签到、历史数据请求
 */

public class LocationReportSignModule implements LocationReportSignContract
		, ListDataProvider.OnListDataResponseListener {

	private Context mContext;
	private ReportSignListener mListener;
	private boolean isTakePhotoError;
	private boolean isSurePhoto = false;

	public LocationReportSignModule(Context context, ReportSignListener listener) {
		this.mContext = context;
		this.mListener = listener;
	}

	@Override
	public void reportDataRequest(PhotoSignTempData tempData) {
		if (tempData == null || tempData.signInItem == null) return;
		LocationRequest locationRequest = new LocationRequest();
		locationRequest.setLatitude(tempData.signInItem.Latitude + "");
		locationRequest.setLongitude(tempData.signInItem.Longitude + "");
		locationRequest.setAddress(tempData.signInItem.content);
		locationRequest.setName(tempData.signInItem.title);
		reportDataRequest(locationRequest, tempData);
	}

	private void reportDataRequest(LocationRequest locationRequest, PhotoSignTempData tempData) {
		if (isSignInNotRange(tempData.signInItem.getLatLng(), tempData.currentLocation, tempData.currentRange)) {
			mListener.onReportFailure(mContext.getResources().getString(R.string.location_distance_error), error.superRange);
			return;
		}

		if (tempData.mWorking.hasTimes() && tempData.mWorking.getStyle() != sign.STYLE_MANY) {//在考勤组，并且非多次打开
			workingSign(locationRequest, tempData);
			return;
		}

		if (tempData.mWorking.isPhotoSign() && tempData.mWorking.getStyle() == sign.STYLE_MANY) {//多次考勤强制拍照
			showPhotoDialog("", tempData);
			return;
		}
		mListener.onReportSetCheckedItem();
		reportLocationData(locationRequest, tempData);
	}

	private void workingSign(LocationRequest locationRequest, PhotoSignTempData tempData) {//考勤组签到，判断是否符合签到要求
		if (tempData.mWorking.isWorkingTimeNull()) return;

		if (!tempData.mWorking.isCanReport()) {//在考勤时间内
			FEToast.showMessage(mContext.getResources().getString(R.string.location_time_overs));
			return;
		}

		if (tempData.mWorking.isPhotoSign()) {//考勤组强制拍照
			showPhotoDialog("", tempData);
			return;
		}

		locationRequest.setSendType(tempData.mWorking.getType());
		locationRequest.setForced(tempData.mWorking.getForced());
		mListener.onReportSetCheckedItem();
		reportLocationData(locationRequest, tempData);
	}

	private void showPhotoDialog(String errorText, PhotoSignTempData tempData) {
		if (mContext instanceof Service) {
			intentShowPhoto(TakePhotoSignContract.WORKING_TAKE_PHOTO, tempData);
			return;
		}
		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(errorText)) {
			sb.append(errorText);
			sb.append(",");
		}
		sb.append(mContext.getResources().getString(R.string.location_need_photo));
		new FEMaterialDialog.Builder(mContext)
				.setTitle(null)
				.setMessage(sb.toString())
				.setPositiveButton(null, dialog -> {
					isSurePhoto = true;
					intentShowPhoto(TakePhotoSignContract.WORKING_TAKE_PHOTO, tempData);
				})
				.setDismissListener(dialog -> {
					if (mListener != null) mListener.onReportPhotoDismiss(isSurePhoto);
				})
				.build()
				.show();
	}

	//当前位置距离签到地址过远
	private boolean isSignInNotRange(LatLng signLatLng, LatLng latLng, int range) {
		FELog.i("location", "-->>>>toun:RestartGps：签到范围：" + range);
		FELog.i("location", "-->>>>toun:RestartGps：签到距离多远：" + AMapUtils.calculateLineDistance(latLng, signLatLng));
		return SignInUtil.getExceedDistance(signLatLng, latLng, range) > 0;
	}

	private void reportLocationData(LocationRequest locationRequest, PhotoSignTempData tempData) {
		if (GpsHelper.inspectMockLocation(mContext)) return;
		FEHttpClient.getInstance().post(locationRequest, createResponeCallback(tempData));
	}

	private ResponseCallback<LocationResponse> createResponeCallback(PhotoSignTempData tempData) {
		return new ResponseCallback<LocationResponse>(this) {

			@Override
			public void onCompleted(LocationResponse response) {
				if (LoadingHint.isLoading()) LoadingHint.hide();

				if (TextUtils.equals("2", response.getErrorCode())) {
					showPhotoDialog(response.getErrorMessage(), tempData);
					return;
				}

				if (!TextUtils.equals("0", response.getErrorCode())) {
					new SignInResultDialog().setContext(mContext).setError(response.getErrorMessage())
							.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "signError");
					return;
				}

				EventLocationSignSuccess signSuccess = SignInSuccessResultUtil.getSignReportSuccess(response.data, isTakePhotoError);
				if (signSuccess == null) requestHistory(isTakePhotoError);//未返回数据，去请求历史记录
				else mListener.onReportHistorySuccess(signSuccess);//返回数据，直接结束
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				mListener.onReportFailure(mContext.getResources().getString(R.string.location_request_data_error), 0);
			}
		};
	}

	@Override
	public void photoSignError(PhotoSignTempData tempData) {
		if (tempData.mWorking.hasTimes())
			intentShowPhoto(TakePhotoSignContract.WORKING_POIITEM_SEARCH_ERROR, tempData);
		else
			intentShowPhoto(TakePhotoSignContract.POIITEM_SEARCH_ERROR, tempData);
	}

	private void intentShowPhoto(int takePhotoType, PhotoSignTempData tempData) {//拍照签到
		LocationPhotoItem photoItem = new LocationPhotoItem();
		photoItem.takePhoto = true;
		photoItem.sendType = tempData.mWorking.getType();
		if (takePhotoType == TakePhotoSignContract.WORKING_TAKE_PHOTO) {//考勤组强制拍照
			if (tempData.signInItem == null) return;
			photoItem.latitude = tempData.signInItem.Latitude + "";
			photoItem.longitude = tempData.signInItem.Longitude + "";
			photoItem.title = tempData.signInItem.title;
			photoItem.address = tempData.signInItem.content;
			photoItem.poiId = tempData.signInItem.poiId;
			if (noListTypeSign(tempData)) {//不允许超范围签到需要校验考勤点
				photoItem.workingRange = tempData.mWorking.signRange();
				photoItem.workingLatLng = tempData.mWorking.signLatLng();
				if (isSignInNotRange(photoItem.stringToLatLng(), tempData.currentLocation, tempData.currentRange)) {
					mListener.onReportFailure(mContext.getResources().getString(R.string.location_distance_error), error.superRange);
					return;
				}
			}
		}
		else {
			if (TextUtils.equals(photoItem.latitude, "0.0")) photoItem.latitude = tempData.currentLocation.latitude + "";
			if (TextUtils.equals(photoItem.longitude, "0.0")) photoItem.longitude = tempData.currentLocation.longitude + "";
		}
		photoItem.type = tempData.locationType;
		photoItem.time = tempData.serviceTime;
		photoItem.takePhotoType = takePhotoType;
		photoItem.endWorkingSignTime = tempData.mWorking.getEndWorkingSignTime();
		sratrIntent(GsonUtil.getInstance().toJson(photoItem));
	}

	private void sratrIntent(String data) {
		final Intent intent = new Intent(mContext, OnSiteSignActivity.class);
		intent.putExtra(LOCATION_PHOTO_ITEM, data);
		if (mContext instanceof Service) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}
		else {
			((Activity) mContext).startActivityForResult(intent, POST_PHOTO_SIGN_DATA);
		}
	}

	private boolean noListTypeSign(PhotoSignTempData tempData) {//是考勤点，需要校验距离
		return tempData.mWorking.getStyle() == K.sign.STYLE_ATT
				|| (tempData.mWorking.getStyle() == K.sign.STYLE_LIST_ATT
				&& (SignInUtil.getExceedDistance(tempData.currentLocation, tempData.mWorking.signLatLng(), tempData.currentRange)) <= 0);
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void requestHistory(boolean isTakePhotoError) {
		this.isTakePhotoError = isTakePhotoError;
		ListDataProvider provider = new ListDataProvider(mContext);
		provider.setOnListResposeListener(this);
		String locationtime = new SimpleDateFormat("yyyy-MM-dd").format(DateUtil.getServiceTime());
		provider.request(RequestType.LocationHistory, locationtime, 1
				, CoreZygote.getLoginUserServices().getUserId());
	}

	//请求考勤组成功
	@Override
	public void onSuccess(List<FEListItem> listItems, int totalNums, int requestType, boolean isSearch) {
		if (CommonUtil.isEmptyList(listItems)) {
			mListener.onReportFailure(mContext.getResources().getString(R.string.location_history_data_error), 0);
			return;
		}
		FEListItem listItem = listItems.get(0);
		EventLocationSignSuccess signSuccess = SignInSuccessResultUtil.getSignReportSuccess(listItem, isTakePhotoError);
		if (signSuccess == null) {
			mListener.onReportFailure(mContext.getResources().getString(R.string.location_history_data_error), 0);
			return;
		}
		mListener.onReportHistorySuccess(signSuccess);
	}

	@Override
	public void onFailure(Throwable error, String content, boolean isSearch) {
		mListener.onReportFailure(mContext.getResources().getString(R.string.location_history_data_error), 0);
	}
}
