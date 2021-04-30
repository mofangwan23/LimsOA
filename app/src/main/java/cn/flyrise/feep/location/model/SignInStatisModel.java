package cn.flyrise.feep.location.model;

import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.LocationLocusRequest;
import cn.flyrise.android.protocol.entity.LocationLocusResponse;
import cn.flyrise.android.protocol.entity.location.SignInLeadeDayStatisDetailResponse;
import cn.flyrise.android.protocol.entity.location.SignInLeaderDayStatisResponse;
import cn.flyrise.android.protocol.entity.location.SignInLeaderMonthStatisDetailResponse;
import cn.flyrise.android.protocol.entity.location.SignInLeaderMonthStatisResponse;
import cn.flyrise.android.protocol.entity.location.SignInMonthStatisResponse;
import cn.flyrise.android.protocol.entity.location.SignInStatisRequest;
import cn.flyrise.feep.core.common.X.LocationType;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.location.bean.LocusPersonLists;
import cn.flyrise.feep.location.bean.SignInLeaderDayDetail;
import cn.flyrise.feep.location.bean.SignInLeaderDayStatis;
import cn.flyrise.feep.location.bean.SignInLeaderMonthDetail;
import cn.flyrise.feep.location.bean.SignInLeaderMonthStatisList;
import cn.flyrise.feep.location.bean.SignInMonthStatisItem;
import java.util.List;
import rx.Observable;
import rx.Subscriber;

/**
 * 新建：陈冕;
 * 日期： 2018-5-17-10:36.
 */

public class SignInStatisModel {

	private static final String METHOD = "getMonthSummary";//个人月统计
	private static final String LEADER_DAY_METHOD = "LeaderOfDay";//领导日统计
	private static final String LEADER_DAY_METHOD_DETAIL = "SignStatis";//领导日统计详情/签到明细
	private static final String LEADER_MONTH_METHOD = "getLeaderMonthSummary";//领导月统计
	private static final String LEADER_MONTH_METHOD_DETAIL = "MonthStatisDetail";//领导月统计详情

	//个人月统计
	public Observable<List<SignInMonthStatisItem>> requestMonth(String month, String userId) {//month:2018-03
		return Observable.create((Subscriber<? super List<SignInMonthStatisItem>> f) -> {
			FEHttpClient.getInstance().post(new SignInStatisRequest(METHOD, month, userId)
					, new ResponseCallback<SignInMonthStatisResponse>() {

						@Override
						public void onCompleted(SignInMonthStatisResponse response) {
							if (TextUtils.equals(response.getErrorCode(), "0")) f.onNext(response.data);
							else f.onError(new NullPointerException());
						}

						@Override public void onFailure(RepositoryException repositoryException) {
							super.onFailure(repositoryException);
							f.onError(new NullPointerException());
						}
					});
		});
	}


	//领导日统计
	public Observable<SignInLeaderDayStatis> requestLeaderDay(String month) {//month:2018-03-05
		return Observable.create((Subscriber<? super SignInLeaderDayStatis> f) -> {
			FEHttpClient.getInstance().post(new SignInStatisRequest(LEADER_DAY_METHOD, month)
					, new ResponseCallback<SignInLeaderDayStatisResponse>() {

						@Override
						public void onCompleted(SignInLeaderDayStatisResponse response) {
							if (TextUtils.equals(response.getErrorCode(), "0")) f.onNext(response.data);
							else f.onError(new NullPointerException());
						}

						@Override public void onFailure(RepositoryException repositoryException) {
							super.onFailure(repositoryException);
							f.onError(new NullPointerException());
						}
					});

		});
	}

	//领导月统计
	public Observable<SignInLeaderMonthStatisList> requestLeaderMonth(String month) {//month:2018-03
		return Observable.create((Subscriber<? super SignInLeaderMonthStatisList> f) -> {
			FEHttpClient.getInstance().post(new SignInStatisRequest(LEADER_MONTH_METHOD, month)
					, new ResponseCallback<SignInLeaderMonthStatisResponse>() {

						@Override
						public void onCompleted(SignInLeaderMonthStatisResponse response) {
							if (TextUtils.equals(response.getErrorCode(), "0")) f.onNext(response.data);
							else f.onError(new NullPointerException());
						}

						@Override public void onFailure(RepositoryException repositoryException) {
							super.onFailure(repositoryException);
							f.onError(new NullPointerException());
						}
					});

		});
	}

	//领导月统计详情
	public Observable<List<SignInLeaderMonthDetail>> requestLeaderMonthDetail(String month, int type) {//month:2018-03
		return Observable.create((Subscriber<? super List<SignInLeaderMonthDetail>> f) -> {
			FEHttpClient.getInstance().post(new SignInStatisRequest(LEADER_MONTH_METHOD_DETAIL, month, type)
					, new ResponseCallback<SignInLeaderMonthStatisDetailResponse>() {

						@Override
						public void onCompleted(SignInLeaderMonthStatisDetailResponse response) {
							if (TextUtils.equals(response.getErrorCode(), "0")) f.onNext(response.data);
							else f.onError(new NullPointerException());
						}

						@Override public void onFailure(RepositoryException repositoryException) {
							super.onFailure(repositoryException);
							f.onError(new NullPointerException());
						}
					});

		});
	}

	//领导日统计详情/签到明细
	public Observable<List<SignInLeaderDayDetail>> requestLeaderDayDetail(String month, int type) {//month:2018-03-05
		return Observable.create((Subscriber<? super List<SignInLeaderDayDetail>> f) -> {
			FEHttpClient.getInstance().post(new SignInStatisRequest(LEADER_DAY_METHOD_DETAIL, month, type)
					, new ResponseCallback<SignInLeadeDayStatisDetailResponse>() {

						@Override
						public void onCompleted(SignInLeadeDayStatisDetailResponse response) {
							if (TextUtils.equals(response.getErrorCode(), "0")) f.onNext(response.data);
							else f.onError(new NullPointerException());
						}

						@Override public void onFailure(RepositoryException repositoryException) {
							super.onFailure(repositoryException);
							f.onError(new NullPointerException());
						}
					});

		});
	}

	public Observable<List<LocusPersonLists>> requestPerson() {//请求下属人员，兼容7.0以下
		final LocationLocusRequest request = new LocationLocusRequest();
		request.setRequestType(LocationType.Person);
		return Observable.create((Subscriber<? super List<LocusPersonLists>> f) -> {
			FEHttpClient.getInstance().post(request, new ResponseCallback<LocationLocusResponse>() {

				@Override
				public void onCompleted(LocationLocusResponse response) {
					if (TextUtils.equals(response.getErrorCode(), "0")) f.onNext(response.getPersonList());
					else f.onError(new NullPointerException());
				}

				@Override public void onFailure(RepositoryException repositoryException) {
					super.onFailure(repositoryException);
					f.onError(new NullPointerException());
				}
			});

		});
	}

	public Observable<LocationLocusResponse> requestTrack(String userId, String date) {//请求考勤轨迹，兼容7.0以下
		final LocationLocusRequest request = new LocationLocusRequest();
		request.setRequestType(LocationType.Locus);
		request.setDate(date);
		request.setUserId(userId);
		return Observable.create((Subscriber<? super LocationLocusResponse> f) -> {
			FEHttpClient.getInstance().post(request, new ResponseCallback<LocationLocusResponse>() {

				@Override
				public void onCompleted(LocationLocusResponse response) {
					if (TextUtils.equals(response.getErrorCode(), "0")) f.onNext(response);
					else f.onError(new NullPointerException());
				}

				@Override public void onFailure(RepositoryException repositoryException) {
					super.onFailure(repositoryException);
					f.onError(new NullPointerException());
				}
			});

		});
	}

}
