package cn.flyrise.feep.qrcode;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.MeetingSignInRequest;
import cn.flyrise.android.protocol.entity.MeetingSignInResponse;
import cn.flyrise.android.protocol.entity.QrLoadTypeResponse;
import cn.flyrise.android.protocol.entity.mokey.MokeySendTokenRequest;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.services.IMobileKeyService;
import cn.flyrise.feep.location.util.GpsHelper;
import cn.flyrise.feep.location.util.GpsHelper.LocationCallBack;
import cn.flyrise.feep.mobilekey.MokeyProvider;
import cn.flyrise.feep.qrcode.QRResultContract.IView;
import cn.flyrise.feep.study.activity.TrainSingActivity;
import cn.squirtlez.frouter.FRouter;
import cn.trust.mobile.key.sdk.api.Interface.OnUserLoginResult;
import cn.trust.mobile.key.sdk.api.MoKeyEngine;
import cn.trust.mobile.key.sdk.entity.Configs;
import com.amap.api.location.AMapLocation;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by klc on 2018/3/15.
 * 二维码处理者
 */

public class QRResultPresenter implements QRResultContract.IPresenter {

	public static final String 签章失败 = "签章失败";
	private Context mContext;
	private QRResultContract.IView mView;
	private String meetingId;

	private final int QRCODE_MOKEYLOGIN = 1; // 手机盾的扫码登录
	private final int QRCODE_OPENURL = 2; // 打开网页
	private final int QRCODE_REQUEST = 3; //网络请求
	private final int QRCODE_MEETINGSIGN = 4; // 会议签到
	private final int QRCODE_COMPANY_PDFSTAMP = 5; //企业签章
	private final int QRCODE_LOGIN = 6; //兼容手机盾跟普通的扫码登录。
	private static final String QRLOGIN_REQUEST_CODE = "3";


	public QRResultPresenter(Context mContext, IView mView) {
		this.mContext = mContext;
		this.mView = mView;
	}


	@Override
	public void handleCode(String codeContent) {
		if (TextUtils.isEmpty(codeContent)) {
			return;
		}
		Intent intent = new Intent(mContext, TrainSingActivity.class);
		intent.putExtra("code", codeContent);
		mContext.startActivity(intent);
//		try {
//			JSONObject jsonObject = new JSONObject(codeContent);
//			int code = jsonObject.getInt("code");
//			String data = jsonObject.getString("data");
//			switch (code) {
//				case QRCODE_MOKEYLOGIN:
//					mobileKeyLogin(data);
//					break;
//				case QRCODE_OPENURL:
//					openWeb(data);
//					break;
//				case QRCODE_REQUEST:
//					doRequest(data);
//					break;
//				case QRCODE_MEETINGSIGN:
//					meetingId = data;
//					mView.requestLocationPermission();
//					break;
//				case QRCODE_COMPANY_PDFSTAMP:
//					doCompanyPDFStamp(data);
//					break;
//				case QRCODE_LOGIN:
//					requestLoginType(data);
//					break;
//				default:
//					FEToast.showMessage(R.string.not_support_action);
//					break;
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//			Intent intent = new Intent(mContext, QRCodeErrorActivity.class);
//			intent.putExtra("content", codeContent);
//			mContext.startActivity(intent);
//		}
	}


	/**
	 * 手机盾登录
	 * @param data 挑战数据
	 */
	private void mobileKeyLogin(String data) {
		IMobileKeyService mobileKeyInfo = CoreZygote.getMobileKeyService();
		if (mobileKeyInfo == null || !mobileKeyInfo.isNormal()) {
			FEToast.showMessage(R.string.mobilekey_init_hint);
			return;
		}
		Configs configs = new Configs();
		configs.setRootPath(mobileKeyInfo.getServer());
		MoKeyEngine engine = new MoKeyEngine(mContext, configs);
		engine.doUserLogin(mobileKeyInfo.getKeyID(), data, new OnUserLoginResult() {
			@Override
			public void onUserLoginResult(int i, String accToken) {
				if (i == MoKeyEngine.SUCCESS) {
					mView.showLoading();
					FEHttpClient.getInstance().post(new MokeySendTokenRequest(
							data, accToken), new ResponseCallback<ResponseContent>() {
						@Override
						public void onCompleted(ResponseContent responseContent) {
							if (responseContent.getErrorCode().equals("0")) {
								FEToast.showMessage(R.string.qrcode_login_success);
							}
							else {
								FEToast.showMessage(responseContent.getErrorMessage());
							}
							mView.hideLoading();
						}

						@Override
						public void onFailure(RepositoryException repositoryException) {
							super.onFailure(repositoryException);
							FEToast.showMessage(R.string.qrcode_login_error);
							mView.hideLoading();
						}
					});
				}
				else {
					FEToast.showMessage(R.string.qrcode_login_error);
				}
			}
		});
	}

	/**
	 * 跳转到某个URL界面
	 * @param url url
	 */
	private void openWeb(String url) {
		FRouter.build(mContext, "/x5/browser")
				.withString("appointURL", url)
				.withInt("moduleId", Func.Default)
				.go();
	}

	private void doRequest(String data) {
		mView.showLoading();
		FEHttpClient.getInstance().post(data, null, new ResponseCallback<ResponseContent>() {
			@Override
			public void onCompleted(ResponseContent response) {
				FEToast.showMessage(response.getErrorMessage());
				mView.hideLoading();
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				FEToast.showMessage("请求失败");
				mView.hideLoading();
			}
		});
	}


	@Override
	public void meetingSignIn() {
		if (!NetworkUtil.isNetworkAvailable(mContext)) {
			FEToast.showMessage(CommonUtil.getString(R.string.core_http_timeout));
			return;
		}
		mView.showLoading();
		new GpsHelper(mContext).getSingleLocation(new LocationCallBack() {
			@Override
			public void success(AMapLocation location) {
				MeetingSignInRequest request = new MeetingSignInRequest(meetingId,
						String.valueOf(location.getLatitude()),
						String.valueOf(location.getLongitude()), location.getAddress());
				FEHttpClient.getInstance()
						.post(request, new ResponseCallback<MeetingSignInResponse>() {
							@Override
							public void onCompleted(MeetingSignInResponse response) {
								if (response.getErrorCode().equals("0")) {
									mView.showMeetingSignDialog(response.data);
								}
								else if (!TextUtils.isEmpty(response.getErrorMessage())
										&& response.getErrorMessage().contains("无效命名空间")) {
									FEToast.showMessage(mContext.getResources()
											.getString(R.string.meeting_sign_error));
								}
								else {
									FEToast.showMessage(response.getErrorMessage());
								}
								mView.hideLoading();
							}


							@Override
							public void onFailure(RepositoryException repositoryException) {
								super.onFailure(repositoryException);
								mView.hideLoading();
							}
						});
			}

			@Override
			public void error() {
				FEToast.showMessage("获取当前地址失败，无法签到");
				mView.hideLoading();
			}
		});
	}

	@Override
	public void checkGpsOpen() {
		LocationManager locationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null && locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			meetingSignIn();
		}
		else {
			mView.showOpenGpsDialog();
		}
	}

	private void doCompanyPDFStamp(String eventData) {
		if (mokeyIsNormal()) {
			return;
		}
		new MokeyProvider(mContext).companyPDFStamp(eventData).subscribe(code -> {
			FEToast.showMessage(
					code.equals("0") ? mContext.getString(R.string.mokey_signet_success)
							: mContext.getString(R.string.mokey_signet_fail));
			mView.hideLoading();
		}, throwable -> {
			throwable.printStackTrace();
			FEToast.showMessage(mContext.getString(R.string.mokey_signet_fail));
		});
	}


	private boolean mokeyIsNormal() {
		IMobileKeyService mobileKeyInfo = CoreZygote.getMobileKeyService();
		if (mobileKeyInfo == null || !mobileKeyInfo.isNormal()) {
			FEToast.showMessage(R.string.mobilekey_init_hint);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 扫码登第一次请求
	 */
	private void requestLogin(String data) {

	}

	/**
	 * 判断是手机盾登录还是普通的扫码登录
	 */
	private void requestLoginType(String data) {
		String url = "";
		if (data.contains("https://") || data.contains("http://")) {
			url = data;
		}
		else {
			url = SpUtil.get(PreferencesUtils.USER_IP, "http://oa.flyrise.cn:8089") + data;
		}
		FEHttpClient.getInstance().post(url, null, new ResponseCallback<QrLoadTypeResponse>() {
			@Override
			public void onCompleted(QrLoadTypeResponse response) {
				if (response.getErrorCode().equals("0")) {
					if (QRLOGIN_REQUEST_CODE.equals(response.getData().getCode())) {
						mView.startLoginZXAciivity(response.getData().getUrl());
					}
					else if (response.getData().getCode()
							.equals(String.valueOf(QRCODE_MOKEYLOGIN))) {
						mobileKeyLogin(response.getData().getEventData());
					}
				}
				else {
					if (response.getErrorMessage() != null) {
						FEToast.showMessage(response.getErrorMessage());
					}
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				FEToast.showMessage(mContext.getString(R.string.qrcode_login_error));
			}
		});
	}

}
