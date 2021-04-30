package cn.flyrise.feep.mobilekey;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.mokey.MoKeyActivateRequest;
import cn.flyrise.android.protocol.entity.mokey.MokeyLogoutRequest;
import cn.flyrise.android.protocol.entity.mokey.MokeyResetEventDataRequest;
import cn.flyrise.android.protocol.entity.mokey.MokeyEventDataResponse;
import cn.flyrise.android.protocol.entity.mokey.MokeySendTokenRequest;
import cn.flyrise.android.protocol.entity.mokey.MokeyUserSignEventDataRequest;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.services.IMobileKeyService;
import cn.flyrise.feep.mobilekey.MokeySettingContract.IProvider;
import cn.trust.mobile.key.sdk.api.Interface.OnActivateResult;
import cn.trust.mobile.key.sdk.api.Interface.OnCompanyPDFStampResult;
import cn.trust.mobile.key.sdk.api.Interface.OnGetKeyStateResult;
import cn.trust.mobile.key.sdk.api.Interface.OnModifyResult;
import cn.trust.mobile.key.sdk.api.Interface.OnUserLoginResult;
import cn.trust.mobile.key.sdk.api.Interface.OnUserResetResult;
import cn.trust.mobile.key.sdk.api.Interface.OnUserSignResult;
import cn.trust.mobile.key.sdk.api.MoKeyEngine;
import cn.trust.mobile.key.sdk.entity.Configs;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * 手机盾的数据请求
 * Created by klc on 2018/3/20.
 */

public class MokeyProvider implements IProvider {

	private MoKeyEngine moKeyEngine;
	private IMobileKeyService mokeyInfo;

	private final String FESAFEPWD_ERROR = "-996";   //安全密码错误代号


	public MokeyProvider(Context context) {
		mokeyInfo = CoreZygote.getMobileKeyService();
		Configs configs = new Configs();
		configs.setRootPath(mokeyInfo.getServer());
		moKeyEngine = new MoKeyEngine(context, configs);
	}

	@Override
	public Observable<Integer> getKeyState() {
		return Observable.create(subscriber -> moKeyEngine.getKeyState(mokeyInfo.getKeyID(), new OnGetKeyStateResult() {
			@Override
			public void onGetKeyStateResult(int code) {
				subscriber.onNext(code);
				subscriber.onCompleted();
			}
		}));
	}

	/**
	 * 手机盾激活
	 */
	public Observable<Integer> active() {
		return Observable.create(subscriber -> moKeyEngine.activateMoKey(mokeyInfo.getKeyID(), new OnActivateResult() {
			@Override
			public void onActivateResult(int code) {
				if (code == MoKeyEngine.SUCCESS) {
					subscriber.onNext(code);
				}
				else {
					subscriber.onError(new Throwable(getMokeyErrorMsg(code)));
				}
				subscriber.onCompleted();
			}
		}));
	}


	/**
	 * 发送激活状态到服务器
	 * @param fePwd 手机盾的安全保护密码
	 */
	public void sendActiveState(String fePwd) {
		Observable.create(new OnSubscribe<Boolean>() {
			@Override
			public void call(Subscriber<? super Boolean> subscriber) {
				MoKeyActivateRequest request = new MoKeyActivateRequest(CommonUtil.getMD5(fePwd));
				FEHttpClient.getInstance().post(request, new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						if (responseContent.getErrorCode().equals("0")) {
							subscriber.onCompleted();
						}
						else {
							subscriber.onError(new Exception("sendActiveState error"));
						}
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						subscriber.onError(repositoryException.exception());
					}
				});
			}
		}).retry(3).subscribe(aBoolean -> {
		});
	}

	/**
	 * 用户登录
	 * @param eventData 挑战数据
	 */
	@Override
	public Observable<String> userLogin(String eventData) {
		return doMokeyUserLogin(eventData).flatMap((Func1<String, Observable<String>>) accToken -> sendToken2FEServer(eventData, accToken));
	}

	private Observable<String> doMokeyUserLogin(String eventData) {
		return Observable.create(subscriber -> moKeyEngine.doUserLogin(mokeyInfo.getKeyID(), eventData, new OnUserLoginResult() {
			@Override
			public void onUserLoginResult(int code, String accToken) {
				if (code == MoKeyEngine.SUCCESS) {
					subscriber.onNext(accToken);
				}
				else {
					subscriber.onError(new Throwable(getMokeyErrorMsg(code)));
				}
				subscriber.onCompleted();
			}
		}));
	}

	/**
	 * 更改手机盾密码
	 */
	public Observable<Integer> modifyMokeyPwd() {
		return Observable.create(subscriber -> moKeyEngine.modifyPin(mokeyInfo.getKeyID(), new OnModifyResult() {
			@Override
			public void onModifyResult(int code) {
				if (code == 0)
					subscriber.onNext(code);
				else
					subscriber.onError(new Throwable(getMokeyErrorMsg(code)));
				subscriber.onCompleted();
			}
		}));
	}

	@Override
	public Observable<Integer> reset(String pwd) {
		return getResetEventData(pwd).flatMap((Func1<String, Observable<Integer>>) this::doMokeyReset);
	}

	private Observable<String> getResetEventData(String pwd) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				MokeyResetEventDataRequest request = new MokeyResetEventDataRequest(CommonUtil.getMD5(pwd));
				FEHttpClient.getInstance().post(request, new ResponseCallback<MokeyEventDataResponse>() {
							@Override
							public void onCompleted(MokeyEventDataResponse response) {
								if ("0".equals(response.getErrorCode())) {
									subscriber.onNext(response.getData().getEventData());
								}
								else if (FESAFEPWD_ERROR.equals(response.getErrorCode())) {
									subscriber.onError(new Throwable("安全密码错误"));
								}
								else
									subscriber.onError(new Throwable(response.getErrorMessage()));
								subscriber.onCompleted();
							}

							@Override
							public void onFailure(RepositoryException repositoryException) {
								super.onFailure(repositoryException);
								subscriber.onError(repositoryException.exception());
								subscriber.onCompleted();
							}
						}

				);
			}
		});
	}

	private Observable<Integer> doMokeyReset(String eventData) {
		return Observable.create(subscriber -> moKeyEngine.doUserReset(mokeyInfo.getKeyID(), eventData, new OnUserResetResult() {
			@Override
			public void onUserResetResult(int code) {
				if (code == 0)
					subscriber.onNext(code);
				else
					subscriber.onError(new Throwable(getMokeyErrorMsg(code)));
				subscriber.onCompleted();
			}
		}));
	}

	@Override
	public Observable<String> companyPDFStamp(String eventData) {
		return doMokeyCompanyPDFStamp(eventData).flatMap((Func1<String, Observable<String>>) token -> sendToken2FEServer(eventData, token));
	}

	private Observable<String> doMokeyCompanyPDFStamp(String eventData) {
		return Observable
				.create(subscriber -> moKeyEngine.doCompanyPDFStamp(mokeyInfo.getKeyID(), eventData, new OnCompanyPDFStampResult() {
					@Override
					public void onCompanyPDFStampResult(int code, String accToken) {
						if (code == MoKeyEngine.SUCCESS) {
							subscriber.onNext(accToken);
						}
						else {
							subscriber.onError(new Throwable("Can't get accToken"));
						}
						subscriber.onCompleted();
					}
				}));
	}

	@Override
	public Observable<Integer> userSign() {
		return getUserSignEventData().flatMap((Func1<String, Observable<Integer>>) this::doMokeyUserSign);
	}

	private Observable<String> getUserSignEventData() {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				MokeyUserSignEventDataRequest request = new MokeyUserSignEventDataRequest();
				FEHttpClient.getInstance().post(request, new ResponseCallback<MokeyEventDataResponse>() {
					@Override
					public void onCompleted(MokeyEventDataResponse response) {
						if (response.getErrorCode().equals("0") && !TextUtils.isEmpty(response.getData().getEventData())) {
							subscriber.onNext(response.getData().getEventData());
						}
						else {
							subscriber.onError(new Throwable("Can't get userSign eventData"));
						}
						subscriber.onCompleted();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						subscriber.onError(repositoryException.exception());
						subscriber.onCompleted();
					}
				});
			}
		});
	}

	private Observable<Integer> doMokeyUserSign(String eventData) {
		return Observable.create(subscriber -> moKeyEngine.doUserSign(mokeyInfo.getKeyID(), eventData, new OnUserSignResult() {
			@Override
			public void onUserSignResult(int code, String accToken) {
				if (code == 0)
					subscriber.onNext(code);
				else
					subscriber.onError(new Throwable(getMokeyErrorMsg(code)));
				subscriber.onCompleted();
			}
		}));
	}


	/***
	 *  注销手机盾
	 * @param pwd 安全密码
	 */
	@Override
	public Observable<String> logout(String pwd) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				MokeyLogoutRequest request = new MokeyLogoutRequest(CommonUtil.getMD5(pwd));
				FEHttpClient.getInstance().post(request, new ResponseCallback<MokeyEventDataResponse>() {
					@Override
					public void onCompleted(MokeyEventDataResponse responseContent) {
						subscriber.onNext(responseContent.getErrorCode());
						subscriber.onCompleted();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						subscriber.onError(repositoryException.exception());
						subscriber.onCompleted();
					}
				});
			}
		});
	}

	private Observable<String> sendToken2FEServer(String eventData, String accToken) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				MokeySendTokenRequest request = new MokeySendTokenRequest(eventData, accToken);
				FEHttpClient.getInstance().post(request, new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						subscriber.onNext(responseContent.getErrorCode());
						subscriber.onCompleted();
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						subscriber.onError(repositoryException.exception());
						subscriber.onCompleted();
					}
				});
			}
		});
	}


	private String getMokeyErrorMsg(int errorCode) {
		switch (errorCode) {
			case MoKeyEngine.ERROR_NULL_LOCAL_KEY:
				return "客户端本地密钥丢失或未激活";
			case MoKeyEngine.ERROR_GET_SIGN_CONTENT_TIME_INVALID:
				return "二维码已失效";
			default:
				return "操作失败";
		}
	}

}
