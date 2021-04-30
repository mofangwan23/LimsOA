package cn.flyrise.feep.location.presenter

import android.content.Context
import android.text.TextUtils
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.location.Sign
import cn.flyrise.feep.location.assistant.SignInRapidlyActivity
import cn.flyrise.feep.location.bean.LocationSaveItem
import cn.flyrise.feep.location.bean.LocationSignTime
import cn.flyrise.feep.location.bean.PhotoSignTempData
import cn.flyrise.feep.location.contract.LocationQueryPoiContract
import cn.flyrise.feep.location.contract.SIGN_TYPE
import cn.flyrise.feep.location.contract.SignInMustContract
import cn.flyrise.feep.location.event.EventLocationSignSuccess
import cn.flyrise.feep.location.model.LocationQueryPoiItemModel
import cn.flyrise.feep.location.model.LocationReportSignModule
import cn.flyrise.feep.location.model.LocationWorkingModel
import cn.flyrise.feep.location.util.LocationCustomSaveUtil
import cn.flyrise.feep.location.util.RxWorkingTimer
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.PoiItem

/**
 * 新建：陈冕;
 *日期： 2018-8-7-10:14.
 * //急速签到前提条件:
 * 1、考勤组情况下，存在固定考勤点，直接用考勤点签到；
 * 2、非考勤组情况下，使用自定义考勤点签到.
 */
class SignInRapidlyPresenter(val mContext: Context) : SignInMustContract() {

    private var mWorking: LocationWorkingModel? = null //考勤组
    private var mQueryPoiItem: LocationQueryPoiItemModel? = null //搜索周边
    private var mReportSign: LocationReportSignModule? = null //打卡（位置详情上报、拍照、历史记录）
    private var mTimerTask: RxWorkingTimer? = null//定时器
    private var isExistPlace = false//是否存在周边
    private var currentLatlng: LatLng? = null//用户当前位置
    private var mView: SignInRapidlyActivity? = null

    init {
        FELog.i("-->>>调用签到--init")
        mView = mContext as SignInRapidlyActivity
        mQueryPoiItem = LocationQueryPoiItemModel(mContext, this)
        mWorking = LocationWorkingModel(mContext, this)
        mReportSign = LocationReportSignModule(mContext, this)
        mTimerTask = RxWorkingTimer(this)
    }

    //    private fun getSignRange() = if (hasTimes() && mWorking != null) mWorking!!.signRange() else LocationQueryPoiContract.search
    private fun getSignRange() = if (mWorking != null) mWorking!!.signRange() else LocationQueryPoiContract.search

    fun requestWQT() {//开始请求考勤组
        mView?.netWork()
        mView?.showLoading()
        mWorking?.requestWQT(SIGN_TYPE)
    }

    //当前位置请求成功，周边签到、考勤点签到
    override fun gpsLocationSuccess(curLatlng: LatLng?) {
        if (mWorking == null) return
        currentLatlng = curLatlng
        mWorking!!.setResponseSignStyle()
        mQueryPoiItem!!.stopLocationGps()

        if (mWorking!!.hasTimes()) {//考勤组
            attendanceUnitSignIn()
        } else {//非考勤组,先搜索周边
            mQueryPoiItem!!.requestLoactionPoiItem(mWorking!!.style, getSignRange())
        }
    }

    override fun refreshListData(items: List<PoiItem>) {
        isExistPlace = !CommonUtil.isEmptyList(items)
        nonAttendanceUnitSignIn()
    }

    private fun attendanceUnitSignIn() {
        when {
            mWorking == null -> return
            isSignInNoTime(mWorking) ->
                signInErrorDialog(mContext.resources.getString(R.string.location_time_overs), Sign.error.noTime)
            isWorkingEmpty(mWorking) -> signInErrorDialog("", Sign.error.workSuperRange)
            else -> signSelectedPoiItem(LocationSaveItem().apply {
                title = mWorking?.pname
                content = mWorking?.paddress
                Latitude = getTextToDouble(mWorking!!.latitude)
                Longitude = getTextToDouble(mWorking!!.longitude)
            })
        }
    }

    private fun isSignInNoTime(work: LocationWorkingModel?) = !isCanReport() && work?.style != K.sign.STYLE_LIST

    private fun isWorkingEmpty(work: LocationWorkingModel?) = textEmpty(work?.latitude)
            || textEmpty(work?.longitude) || textEmpty(work?.pname) || textEmpty(work?.paddress)

    private fun textEmpty(text: String?) = TextUtils.isEmpty(text)

    private fun nonAttendanceUnitSignIn() {
        val saveItem = LocationCustomSaveUtil.getSelectedLocationItem()
        when {
            saveItem == null -> signInErrorDialog("", Sign.error.noCustom)
            isSaveEmpty(saveItem) -> signInErrorDialog("", Sign.error.noCustom)
            else -> signSelectedPoiItem(saveItem)
        }
    }

    private fun isSaveEmpty(item: LocationSaveItem) = item.Latitude == 0.0 || item.Longitude == 0.0
            || textEmpty(item.title) || textEmpty(item.content)

    private fun getTextToDouble(text: String): Double {
        try {
            return java.lang.Double.valueOf(text)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return 0.0
    }

    override fun workingTimerExistence(serviceTime: String?, isSignMany: Boolean) {
        mTimerTask!!.startServiceDateTimer(serviceTime)
        mWorking!!.distanceSignTime(mTimerTask!!.timeInMillis)
    }

    override fun workingRequestEnd() {//考勤组数据请求结束，开始定位签到
        if (mWorking?.isSignMany ?: false) {
            onReportFailure("", Sign.error.signMany)
            return
        }
        mQueryPoiItem?.stopLocationGps()
        mQueryPoiItem?.getRapidlyLocation(SIGN_TYPE)
    }

    override fun onReportSetCheckedItem() {
        mQueryPoiItem?.stopLocationGps()
    }

    override fun onReportFailure(errorText: String, errorType: Int) {
        signInErrorDialog(errorText, when {
            errorType != Sign.error.superRange -> errorType
            mWorking?.hasTimes() ?: false -> Sign.error.workSuperRange
            !isExistPlace -> Sign.error.superRangeNoPlace
            else -> errorType
        })
    }

    override fun onReportHistorySuccess(signSuccess: EventLocationSignSuccess?) {
        mView?.hideLoading()
        if (signSuccess == null) return
        mView?.startSuccess(signSuccess.time, mWorking?.isLeaderOrSubordinate() ?: false, signSuccess.title)
    }

    override fun onReportPhotoDismiss(isSurePhoto: Boolean) {
        mView?.hideLoading()
        if (!isSurePhoto) {
            mView?.finish()
        }
    }

    override fun notifyRefreshServiceTime(signData: LocationSignTime?) {
        mWorking!!.distanceSignTime(mTimerTask!!.timeInMillis)
    }

    //拍照成功，开始弹出提示框
    fun photoRequestHistory(isTakePhotoError: Boolean, success: EventLocationSignSuccess?) {
        if (success != null) onReportHistorySuccess(success) else mReportSign?.requestHistory(isTakePhotoError)
    }

    private fun signInErrorDialog(errorText: String, errorType: Int) {
        mView?.hideLoading()
        mView?.startFailure(errorText, errorType)
    }

    private fun signSelectedPoiItem(saveItem: LocationSaveItem?) {
        mReportSign?.reportDataRequest(PhotoSignTempData.Bulider()
                .setChoiceItem(saveItem)
                .setWorking(mWorking)
                .setLocationType(SIGN_TYPE)
                .setCurrentLocation(currentLatlng)
                .setCurrentRange(getSignRange())
                .setServiceTime(mTimerTask?.getCurrentServiceTime(mWorking!!.serviceTime))
                .bulider())
    }

    private fun isCanReport() = mWorking?.isCanReport ?: false//是否允许打卡

    private fun hasTimes() = mWorking?.hasTimes() ?: false//是否在考勤组

    fun onDestroy() {
        mQueryPoiItem?.stopLocationGps()
        mQueryPoiItem = null
        mWorking = null
        mReportSign = null
        if (mTimerTask != null) {
            mTimerTask?.onDestroy()
            mTimerTask = null
        }
    }
}