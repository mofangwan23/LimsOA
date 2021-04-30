package cn.flyrise.feep.location.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.LocationLocusResponse;
import cn.flyrise.android.shared.utility.FEDate;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.LocationWorkingTimes;
import cn.flyrise.feep.location.bean.LocusDataProvider;
import cn.flyrise.feep.location.bean.WorkingSignState;
import cn.flyrise.feep.location.contract.LocationQueryPoiContract;
import cn.flyrise.feep.location.contract.LocationWorkingContract;
import com.amap.api.maps.model.LatLng;
import java.text.SimpleDateFormat;

/**
 * 新建：陈冕;
 * 日期： 2017-12-19-9:22.
 */

public class LocationWorkingModel implements LocationWorkingContract
		, LocusDataProvider.OnLocationResponseListener {

	private Context mContext;
	private LocationLocusResponse mResponses;

	private LocationWorkingTimes mWorkingTimes;
	private WorkingSignState mSignState;

	private WorkingListener mListener;
	private final LocusDataProvider dataProvider;
	private int mLocationStyle = K.sign.STYLE_LIST;//签到页面的样式

	public LocationWorkingModel(Context context, WorkingListener listener) {
		this.mContext = context;
		this.mListener = listener;
		mSignState = new WorkingSignState();
		dataProvider = new LocusDataProvider(mContext);
		dataProvider.setResponseListener(this);
	}

	@Override
	public WorkingSignState getWorkingSignState() {
		return mSignState;
	}

	@Override
	public boolean isCanReport() {
		return mSignState.isCanReport;
	}

	@Override
	public void setCanReport(boolean isSign) {
		mSignState.isCanReport = isSign;
	}

	@Override
	public int signRange() {
		return mSignState.signRange > 0 ? mSignState.signRange : LocationQueryPoiContract.search;
	}

	@Override
	public LatLng signLatLng() {
		return mSignState.signLatLng;
	}

	@Override
	public boolean hasTimes() {
		return mSignState.hasTimes;
	}

	@Override
	public boolean isSignMany() {
		return mSignState.isSignMany;
	}

	@Override
	public boolean isLeaderOrSubordinate() {
		return mSignState.hasSubordinate || mSignState.isLeader;
	}

	@Override
	public boolean isPhotoSign() {//是否强制拍照
		return "2".equals(getForced());
	}

	@Override
	public boolean isWorkingTimeNull() {
		return mWorkingTimes == null;
	}

	@Override
	public String getTimes() {
		return isWorkingTimeNull() ? "" : mWorkingTimes.getTimes();
	}

	@Override
	public String getForced() {
		return mWorkingTimes == null ? "" : mWorkingTimes.getForced();
	}

	@Override
	public String getType() {
		return mWorkingTimes == null ? "" : mWorkingTimes.getType();
	}

	@Override
	public String getServiceTime() {
		return isWorkingTimeNull() ? "" : mWorkingTimes.getServiceTime();
	}

	@Override
	public String getPname() {
		return mResponses == null ? "" : mResponses.getPname();
	}

	@Override
	public String getPaddress() {
		return mResponses == null ? "" : mResponses.getPaddress();
	}

	@Override
	public String getLatitude() {
		return mResponses == null ? "" : mResponses.getLatitude();
	}

	@Override
	public String getLongitude() {
		return mResponses == null ? "" : mResponses.getLongitude();
	}

	@Override
	public String getRange() {
		return mResponses == null ? "" : mResponses.getRange();
	}

	@Override
	public String getEndWorkingSignTime() {
		return mSignState.endWorkingSignTime;
	}

	@Override
	public void requestWQT(int locationType) {
		dataProvider.requestWorkingTime();
	}

	@Override
	public void onSuccess(LocationLocusResponse responses, String locationType) {//考勤组请求成功
		if (!TextUtils.equals(locationType, X.LocationType.WorkingTime)) {
			mListener.workingRequestEnd();
			return;
		}
		if (responses.getWorkingTimes() == null) {
			mListener.workingRequestEnd();
			return;
		}
		setWorkingTimeExist(responses);
		mListener.workingLeaderListener(mSignState.isLeader);
	}

	@Override
	public void onFailed(Throwable error, String content) {
		if (LoadingHint.isLoading()) LoadingHint.hide();
		mSignState.isLeader = false;
		mSignState.hasSubordinate = false;
		mListener.workingRequestEnd();
	}

	private void setWorkingTimeExist(LocationLocusResponse responses) {
		if (CommonUtil.isEmptyList(responses.getWorkingTimes())) {
			mListener.workingRequestEnd();
			return;
		}
		mWorkingTimes = responses.getWorkingTimes().get(0);
		if (mWorkingTimes != null && !TextUtils.isEmpty(mWorkingTimes.getTimes())) {
			mSignState.isLeader = false;
			mSignState.hasTimes = true;
		}
		else mSignState.isLeader = true;
		if (isExistSubordinate()) {
			mSignState.hasSubordinate = true;
		}
		else {
			mSignState.hasSubordinate = false;
			mSignState.isLeader = false;
		}
		setWorkingTimes(responses);
	}

	private void setWorkingTimes(LocationLocusResponse responses) {
		if (mWorkingTimes == null) return;
		mSignState.isSignMany = mWorkingTimes.getSignMany() != null && TextUtils.equals("1", mWorkingTimes.getSignMany());
		int range = -1;
		try {
			range = initWorkingSignRange(responses.getRange());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (range >= 0) this.mSignState.signRange = range;
//		if (range >= 0) this.mSignState.signRange = 100;//测试
		initIsoutsign(responses);
		initWorkingTimes();
	}

	private boolean isExistSubordinate() {// 存在下属
		return (mWorkingTimes != null ? mWorkingTimes.getHasSubordinate() : null) != null && "1"
				.equals(mWorkingTimes.getHasSubordinate());
	}

	@Override
	public String distanceSignTime(long timeInMillis) {
		if (mWorkingTimes == null || mWorkingTimes.getTimes() == null) return "";
		mSignState.isCanReport = isCanReportSign(timeInMillis);//是否可以打卡
		if (workingTimeRestartRequestWQT(timeInMillis)) mListener.workingTimeRestartRequestWQT();
		if (isExceeEndWorkingTime(timeInMillis)) {
			mSignState.isCanReport = false;
			return mContext.getResources().getString(R.string.location_time_over);
		}
		else return getWorkingSignTimeText();
	}

	@Override
	public int getStyle() {
		return mLocationStyle;
	}

	private boolean isExceeEndWorkingTime(long timeInMillis) {// 如果已经过了最后打卡时间
		return timeInMillis > getTextToTime(endWorkingSignTime());
	}

	@SuppressLint("SimpleDateFormat")
	private String getWorkingSignTimeText() {//考勤组签到时间区间
		final String startTime = new SimpleDateFormat("MM-dd HH:mm:ss").format(FEDate.getDateSS(startWorkingSignTime()));
		final String endTime = new SimpleDateFormat("HH:mm:ss").format(FEDate.getDateSS(endWorkingSignTime()));
		return mContext.getResources().getString(R.string.location_worktime) + startTime + "-" + endTime;
	}

	private boolean isCanReportSign(long timeInMillis) {// 剩余多少时间打卡
		final long leftTime = getTextToTime(startWorkingSignTime()) - timeInMillis;
		return (leftTime / (1000 * 60)) <= 0 && ((leftTime % (1000 * 60)) / 1000) <= 0;
	}

	private boolean workingTimeRestartRequestWQT(long timeInMillis) {//考勤时间触碰到边缘，通知更新考勤组数据
		return (getTextToTime(startWorkingSignTime()) - timeInMillis) == 0 || (getTextToTime(endWorkingSignTime()) - timeInMillis) == 0;
	}

	private long getTextToTime(String text) {
		return FEDate.getDateSS(text).getTime();
	}

	private String[] workingSignTime() {
		return mWorkingTimes.getTimes().split(",");
	}

	private String startWorkingSignTime() { //开始考勤的时间
		return workingSignTime().length > 0 ? workingSignTime()[0] : "";
	}

	private String endWorkingSignTime() { //结束考勤的时间
		return mSignState.endWorkingSignTime = workingSignTime().length > 0 ? workingSignTime()[1] : "";
	}

	private void initIsoutsign(LocationLocusResponse responses) {
		if (mSignState.isSignMany) {
			mListener.workingRequestEnd();
			return;
		}
		mResponses = responses;
		LatLng latLng = null;
//		int range = -1;
		try {
			latLng = initWorkingSignLatLng(responses.getLatitude(), responses.getLongitude());
//			range = initWorkingSignRange(responses.getRange());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (latLng != null) this.mSignState.signLatLng = latLng;
//		if (range >= 0) this.mSignState.signRange = range;
		mListener.workingRequestEnd();
	}

	private LatLng initWorkingSignLatLng(String latitude, String longitude) {
		if (TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) return null;
		return new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
	}

	private int initWorkingSignRange(String range) {
		return TextUtils.isEmpty(range) ? LocationQueryPoiContract.search : Integer.valueOf(range);
	}

	@Override
	public void setResponseSignStyle() {
		mLocationStyle = getLocationStyle();
	}

	private int getLocationStyle() {
		if (mSignState.isSignMany) return K.sign.STYLE_MANY;//多次签到
		else if (mSignState.hasTimes) {
			if (isWorkingDataExist()) {
				return isNotAllowExceedSign() ? K.sign.STYLE_ATT : K.sign.STYLE_LIST_ATT;//考勤点签到
			}
			else {
				return K.sign.STYLE_LIST_ATT;//有考勤时间，去考勤列表签到
			}
		}
		else return K.sign.STYLE_LIST;//默认列表签到
	}

	private boolean isWorkingDataExist() {//考勤点签到数据是否存在
		return mResponses != null && !isLatLngNull() && !isSignAddressNull() && !TextUtils.isEmpty(mResponses.getRange());
	}

	private boolean isNotAllowExceedSign() {//不允许超范围签到("N"返回不允许超范围签到，空或Y时返回允许超范围打开)
		return TextUtils.equals("N", mResponses.getIsoutsign());
	}

	private boolean isSignAddressNull() {
		return TextUtils.isEmpty(mResponses.getPname()) || TextUtils.isEmpty(mResponses.getPaddress());
	}

	private boolean isLatLngNull() {
		return TextUtils.isEmpty(mResponses.getLatitude()) || TextUtils.isEmpty(mResponses.getLongitude());
	}

	private void initWorkingTimes() { //初始化考勤组时间
		if (TextUtils.isEmpty(mWorkingTimes.getTimes())) return;
		if (mWorkingTimes.getEachTime() == null) mWorkingTimes.setEachTime("0");// 没有时间，替换为0，防止报错
		mListener.workingTimerExistence(mWorkingTimes.getServiceTime(), mSignState.isSignMany);
	}
}
