package cn.flyrise.feep.location.contract

import cn.flyrise.feep.K
import cn.flyrise.feep.location.bean.*
import cn.flyrise.feep.location.event.EventLocationSignSuccess
import com.amap.api.maps.model.LatLng

/**
 * 新建：陈冕;
 *日期： 2018-6-22-17:52.
 */


const val SIGN_TYPE = K.location.LOCATION_SIGN//签到类型

const val search = 500 //默认搜索周边范围

const val pageSize = 15//设置搜索周边每一页的数量

const val REQUESTE_LOCATION = 1013//重新请求考勤组

const val REQUESTE_LOCATION_POIITEM = 1019//重新请求数据

const val REQUESTE_MOVE_MAP = 1020//重新滑动地图

const val REQUESTE_LOCATION_TIME = 6 * 1000//重新请求考勤组的时间

const val MOVE_CURRENT_LOCATION = 1016//移动到当前位置

const val SIGN_DEFAULT = 101//默认签到fragment

const val SIGN_ATTENDANCE = 102//考勤组签到fragment

const val CZ_PHONE = "icesky_msm8992"//锤子手机

const val OPEN_SEARCH_SIGN = 1002//打开搜索签到

interface SignInMainContractPresenter {

    fun isLeaderOrSubordinate(): Boolean//是否为领导，是否存在下属

    fun workState(): WorkingSignState//考勤组状态

    fun isCanReport(): Boolean//是否允许打卡

    fun getGPSLocation() //请求位置

    fun requestWQT()  //请求考勤组

    fun getMorePoiSearch()

    fun delayedRefreshActivityView() //延迟刷新活动界面

    fun photoRequestHistory(isTakePhotoError: Boolean, signSuccess: EventLocationSignSuccess?) //获取最新签到记录

    fun intentShowPhoto() //拍照签到

    fun onResume()

    fun onPause()

    fun onDestroy()

    fun signSelectedPoiItem(saveItem: LocationSaveItem?)

    fun setAMapStyle(style: SignInSetAMapStyle?) //地图样式。地点签到、列表签到

    fun setTouchMap() //用户正在操作地图

    fun hasTimes(): Boolean //是否在考勤组
}

interface SignInMainContractView {

    fun refreshListData(items: List<SignPoiItem>?)

    fun loadMoreListData(items: List<SignPoiItem>?)

    fun setAttendanceServiceTime(serviceCurrentTiem: LocationSignTime?) //考勤列表服务端当前时间

    fun setAttendanceWorkingTimeInterval(timeInterval: String?)  //考勤组服务端签到时间区间

    fun signSuccessShowIcon() // 签到成功显示徽章

    fun notifitionSignInStyle(style: Int, latLng: LatLng?, isNotifiGpsLocation: Boolean) //签到样式k.sign；用户当前坐标

    fun setSwipeRefresh(isRefresh: Boolean)

    fun restartWorkingTime()

    fun startSignViewOperation(isEnabled: Boolean)

    fun setSignInButtomEnabled(isEnabled: Boolean)

}