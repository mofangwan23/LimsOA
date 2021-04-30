package cn.flyrise.feep.location.presenter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.app.AppCompatActivity
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.commonality.MainMenuRecyclerViewActivity
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.location.bean.*
import cn.flyrise.feep.location.contract.*
import cn.flyrise.feep.location.dialog.SignInResultDialog
import cn.flyrise.feep.location.event.EventLocationSignSuccess
import cn.flyrise.feep.location.fragment.SignInMainFragment
import cn.flyrise.feep.location.model.LocationQueryPoiItemModel
import cn.flyrise.feep.location.model.LocationReportSignModule
import cn.flyrise.feep.location.model.LocationWorkingModel
import cn.flyrise.feep.location.service.LocationService
import cn.flyrise.feep.location.util.GpsHelper
import cn.flyrise.feep.location.util.MarkerOperationUtil
import cn.flyrise.feep.location.util.RxWorkingTimer
import cn.flyrise.feep.location.util.SignInUtil
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.PoiItem
import org.greenrobot.eventbus.EventBus

/**
 * 新建：陈冕;
 *日期： 2018-6-22-17:43.
 */
class SignInMainPresenter(private val mContext: Context, private val mView: SignInMainFragment, aMap: AMap
                          , private val mLeaderListener: (Boolean) -> Unit) : SignInMustContract(), SignInMainContractPresenter {

    private var currentLatlng: LatLng? = null
    private var mWorking: LocationWorkingModel? = null //考勤组
    private var mQueryPoiItem: LocationQueryPoiItemModel? = null //搜索周边
    private var mReportSign: LocationReportSignModule? = null //打卡（位置详情上报、拍照、历史记录）
    private var mMarkerOperation: MarkerOperationUtil? = null //标记添加到地图
    private var mTimerTask: RxWorkingTimer? = null//定时器

    private var isRestartGpsLocation = false//是否为GPS定位
    private var isAllowRefreshList = false//禁止刷新列表
    private var isAllowMoveMap = true//禁止自动移动地图
    private var isRestartWorking = false//是否重新请求考勤组
    private var isPhotoResult: Boolean = false//是否为现场签到返回
    private var lastLatlng: LatLng? = null//缓存上次搜索周边用到的坐标

    private var aMapStyle: SignInSetAMapStyle? = null

    private val mHandle = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when {
                msg.what == REQUESTE_LOCATION -> {
                    mView.startSignViewOperation(true)
                    mView.restartWorkingTime()
                    requestWQT()
                }
                msg.what == MOVE_CURRENT_LOCATION && mMarkerOperation != null -> mMarkerOperation!!.moveToLocation(currentLatlng)
                msg.what == REQUESTE_LOCATION_POIITEM -> isAllowRefreshList = true
                msg.what == REQUESTE_MOVE_MAP -> isAllowMoveMap = true
            }
        }
    }

    //获取当前签到数据
    fun signInData(saveItem: LocationSaveItem?) = if (saveItem == null) createSignInAttendance() else createTempSignInAttendance(saveItem)

    private fun createSignInAttendance() = SignInAttendanceData.Builder()
            .setPname(mWorking!!.pname)
            .setPaddress(mWorking!!.paddress)
            .setRange(mWorking!!.range)
            .setTimes(mWorking!!.times)
            .setLatitude(mWorking!!.latitude)
            .setLongitude(mWorking!!.longitude)
            .builder()

    private fun createTempSignInAttendance(saveItem: LocationSaveItem) = SignInAttendanceData.Builder()
            .setPname(saveItem.title)
            .setPaddress(saveItem.content)
            .setRange(mWorking!!.range)
            .setTimes(mWorking!!.times)
            .setLatLng(saveItem.latLng)
            .builder()

     val signRange: Int
//        get() = if (hasTimes()) mWorking!!.signRange() else LocationQueryPoiContract.search
        get() = mWorking!!.signRange()

    init {
        mQueryPoiItem = LocationQueryPoiItemModel(mContext, this)
        mWorking = LocationWorkingModel(mContext, this)
        mReportSign = LocationReportSignModule(mContext, this)
        mTimerTask = RxWorkingTimer(this)
        mMarkerOperation = MarkerOperationUtil(mContext, aMap)
    }

    @SuppressLint("DefaultLocale")
    override fun notifyRefreshServiceTime(signData: LocationSignTime?) { //定时器更新时间
        if (mWorking!!.isSignMany || signData == null) return
        mView.setAttendanceServiceTime(signData)
        mView.setAttendanceWorkingTimeInterval(mWorking!!.distanceSignTime(mTimerTask!!.timeInMillis))
    }

    fun clickMoreSetting() {
        val intent = Intent(mContext, MainMenuRecyclerViewActivity::class.java)
        intent.putExtra(MainMenuRecyclerViewActivity.MENU_TYPE, MainMenuRecyclerViewActivity.ATTENDANCE_MENU)
        intent.putExtra("IS_LOCATION_SIGN_LEADER", mWorking?.isLeaderOrSubordinate())
        intent.putExtra("IS_LOCATION_SIGN_TIME", mWorking?.hasTimes())
        mContext.startActivity(intent)
    }

    override fun requestWQT() {
        if (!mView.isResultDialogSuccess()) LoadingHint.show(mContext)
        mWorking?.requestWQT(SIGN_TYPE)
    }

    override fun workState(): WorkingSignState {
        return mWorking!!.workingSignState
    }

    override fun isCanReport(): Boolean {//是否允许打卡
        return mWorking!!.isCanReport
    }

    override fun hasTimes(): Boolean {
        return mWorking!!.hasTimes()
    }

    override fun isLeaderOrSubordinate(): Boolean {
        return mWorking!!.isLeaderOrSubordinate
    }

    override fun setAMapStyle(style: SignInSetAMapStyle?) {
        this.aMapStyle = style
        if (style!!.isAMapSignStyle)
            mMarkerOperation!!.moveSignLocationLatlng(style)
        else
            mMarkerOperation!!.showPoiItemMarkers(style.latLng, style.signPoiItems, style.isMoveMap)
    }

    override fun setTouchMap() {
        this.isAllowMoveMap = false
        mHandle.removeMessages(REQUESTE_MOVE_MAP)
        mHandle.sendEmptyMessageDelayed(REQUESTE_MOVE_MAP, LocationQueryPoiItemModel.spaceTime.toLong())
    }

    /*********考勤时间回调 */
    override fun workingTimerExistence(serviceTime: String, isSignMany: Boolean) {
        LocationService.startLocationService(mContext, LocationService.REQUESTCODE)
        mTimerTask?.startServiceDateTimer(serviceTime)
        mWorking?.distanceSignTime(mTimerTask!!.timeInMillis)
    }

    override fun workingTimeRestartRequestWQT() {
        mView.startSignViewOperation(false)
        mView.setSignInButtomEnabled(false)
        mHandle.removeMessages(REQUESTE_LOCATION)
        mHandle.sendEmptyMessageDelayed(REQUESTE_LOCATION, (6 * 1000).toLong())
    }

    override fun workingRequestEnd() {//考勤组请求结束
        isRestartWorking = true
        getGPSLocation()
    }

    override fun workingLeaderListener(isLeader: Boolean) {
        mLeaderListener(isLeader)
    }

    /*********考勤时间回调END */
    override fun delayedRefreshActivityView() {
        isAllowRefreshList = false
        mHandle.removeMessages(REQUESTE_LOCATION_POIITEM)
        mHandle.sendEmptyMessageDelayed(REQUESTE_LOCATION_POIITEM, LocationQueryPoiItemModel.spaceTime.toLong())
    }

    override fun getGPSLocation() {//重新定位立即刷新周边
        mHandle.removeMessages(REQUESTE_LOCATION_POIITEM)
        mQueryPoiItem?.stopLocationGps()
        isRestartGpsLocation = true
        isAllowRefreshList = true
        isAllowMoveMap = true
        mQueryPoiItem?.getGPSLocation(SIGN_TYPE)
    }

    override fun getMorePoiSearch() {
        mQueryPoiItem?.loadMorePoiSearch()
    }

    /********定位搜索周边回调 */
    override fun gpsLocationSuccess(curLatlng: LatLng) {//当前位置请求成功，周边签到、考勤点签到。
        if (LoadingHint.isLoading()) LoadingHint.hide()
        currentLatlng = curLatlng
        if (aMapStyle == null || isRestartGpsLocation || isAllowRefreshList && !isExceptionalRefreshSignLayout(curLatlng))
            refreshMapAndList(curLatlng)
        else
            refreshMap(curLatlng)
    }

    private fun refreshMap(curLatlng: LatLng) {//只允许移动地图，如果用户在手动操作地图，禁止自动移动地图
        FELog.i("location", "-->>>>toun:RestartGps：距离太近，阻止$signRange")
        aMapStyle?.latLng = curLatlng
        aMapStyle?.isMoveMap = isAllowMoveMap
        setAMapStyle(aMapStyle)
        isAllowMoveMap = true
        if (LoadingHint.isLoading()) LoadingHint.hide()
    }

    private fun refreshMapAndList(curLatlng: LatLng) {//移动当前位置和刷新列表
        FELog.i("location", "-->>>>toun:RestartGps：一切正常，刷新考勤点:$signRange")
        isRestartGpsLocation = false
        lastLatlng = curLatlng
        mWorking?.setResponseSignStyle()
        mView.notifitionSignInStyle(mWorking!!.style, curLatlng, isRestartWorking)//初始化布局
        mView.setSwipeRefresh(true)
        isRestartWorking = false
        mQueryPoiItem!!.requestLoactionPoiItem(mWorking!!.style, signRange)

        onSignInRange(SignInUtil.getExceedDistance(curLatlng, mWorking!!.latitude, mWorking!!.longitude, mWorking!!.range) <= 0)
    }

    private fun onSignInRange(isRange: Boolean) {
        mView.setSignInSearchLayout(!(mWorking?.hasTimes() ?: false) || (mWorking?.isCanReport ?: true && isRange))
    }

    private fun isExceptionalRefreshSignLayout(curLatlng: LatLng?): Boolean {//位置移动超过10米
        return curLatlng != null && lastLatlng != null && AMapUtils.calculateLineDistance(curLatlng, lastLatlng) <= 10
    }

    override fun showSwipeRefresh(isShow: Boolean) {
        mView.setSwipeRefresh(isShow)
    }

    override fun refreshListData(items: List<PoiItem>) {
        mView.refreshListData(getLocationSearchDataFilter(items))
    }

    override fun loadMoreListData(items: List<PoiItem>) {
        mView.loadMoreListData(getLocationSearchDataFilter(items))
    }

    override fun setEmptyingAdapter() {

    }

    override fun loadMoreListFail() {
        mView.refreshListData(null)
    }

    override fun loadMoreState(state: Int) {
        mView.loadMoreState(state)
    }

    private fun getLocationSearchDataFilter(items: List<PoiItem>): List<SignPoiItem> {
        return LocationSearchDataFilter.Builder()
                .setType(SIGN_TYPE)
                .setNotAllowSuperRange(mWorking!!.style == K.sign.STYLE_ATT)
                .setItems(items)
                .setSearch(LocationQueryPoiContract.search)
                .setSignLatLng(mWorking!!.signLatLng())
                .setSignRange(signRange)
                .builder()
                .data
    }

    /**********签到数据上传 */
    override fun onReportSetCheckedItem() {//开始上传签到数据，停止定位、禁止用户操作
        mHandle.removeMessages(REQUESTE_LOCATION_POIITEM)
        mQueryPoiItem!!.stopLocationGps()

        mView.startSignViewOperation(false)
        mView.setSignInButtomEnabled(false)
        mView.setSwipeRefresh(false)
        mHandle.sendEmptyMessageDelayed(REQUESTE_LOCATION, REQUESTE_LOCATION_TIME.toLong())
    }

    override fun onReportFailure(text: String, errorType: Int) {
        SignInResultDialog().setContext(mContext).setError(text)
                .show((mContext as AppCompatActivity).supportFragmentManager, "signError")
    }

    override fun onReportHistorySuccess(signSuccess: EventLocationSignSuccess) {
        mView.locationSignSuccess(signSuccess.time, signSuccess.title)
        EventBus.getDefault().post(signSuccess)
    }

    override fun onReportPhotoDismiss(isSurePhoto: Boolean) {
    }

    /**********签到数据上传END */

    override fun intentShowPhoto() {
        if (!mWorking!!.isCanReport && mWorking!!.style != K.sign.STYLE_LIST) {
            FEToast.showMessage(mContext.resources.getString(R.string.location_time_overs))
            return
        }
        GpsHelper(mContext).getSingleLocation(object : GpsHelper.LocationCallBack {
            override fun success(location: AMapLocation?) {
                signShowPhoto(if (location == null) currentLatlng else LatLng(location.getLatitude(), location.getLongitude()))
            }

            override fun error() {
                signShowPhoto(currentLatlng)
            }
        })
    }

    private fun signShowPhoto(curLatlng: LatLng?) {
        mReportSign!!.photoSignError(PhotoSignTempData.Bulider()
                .setWorking(mWorking)
                .setLocationType(SIGN_TYPE)
                .setCurrentLocation(curLatlng)
                .setCurrentRange(signRange)
                .setServiceTime(mTimerTask!!.getCurrentServiceTime(mWorking!!.serviceTime))
                .bulider())
    }

    override fun photoRequestHistory(isTakePhotoError: Boolean, signSuccess: EventLocationSignSuccess?) {//请求历史记录
        this.isPhotoResult = true
        if (signSuccess != null)
            onReportHistorySuccess(signSuccess)
        else
            mReportSign?.requestHistory(isTakePhotoError)
    }

    //签到是重新定位位置，防止用户位置存在缓存
    override fun signSelectedPoiItem(saveItem: LocationSaveItem?) {
        GpsHelper(mContext).getSingleLocation(object : GpsHelper.LocationCallBack {
            override fun success(location: AMapLocation?) {
                signSelectedPoiItem(saveItem, if (location == null) currentLatlng else LatLng(location.getLatitude(), location.getLongitude()))
            }

            override fun error() {
                signSelectedPoiItem(saveItem, currentLatlng)
            }
        })
    }

    private fun signSelectedPoiItem(saveItem: LocationSaveItem?, curLatlng: LatLng?) {
        mReportSign!!.reportDataRequest(PhotoSignTempData.Bulider()
                .setChoiceItem(saveItem)
                .setWorking(mWorking)
                .setLocationType(SIGN_TYPE)
                .setCurrentLocation(curLatlng)
                .setCurrentRange(signRange)
                .setServiceTime(mTimerTask!!.getCurrentServiceTime(mWorking!!.serviceTime))
                .bulider())
    }

    override fun onResume() {
        mMarkerOperation?.onResume()
        //其它界面打开键盘后，地图会被移动，目测是因为沉侵模式引起的
        mHandle.sendEmptyMessageDelayed(MOVE_CURRENT_LOCATION, 100)
    }

    override fun onPause() {
        mHandle.removeMessages(REQUESTE_LOCATION_POIITEM)
        mHandle.removeMessages(MOVE_CURRENT_LOCATION)
        mHandle.removeMessages(REQUESTE_LOCATION)
        mView.startSignViewOperation(true)
        mView.restartWorkingTime()
        mMarkerOperation?.onPause()
        mQueryPoiItem?.destroyLocationGps()
    }

    override fun onDestroy() {
        mMarkerOperation?.onDestroy()
        mQueryPoiItem = null
        mWorking = null
        mReportSign = null
        mMarkerOperation = null
        if (mTimerTask != null) {
            mTimerTask?.onDestroy()
            mTimerTask = null
        }
    }

}