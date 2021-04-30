package cn.flyrise.feep.location.fragment

import android.support.v4.app.Fragment
import cn.flyrise.feep.location.bean.LocationSaveItem
import cn.flyrise.feep.location.bean.SignInAttendanceData
import cn.flyrise.feep.location.bean.SignPoiItem
import cn.flyrise.feep.location.bean.WorkingSignState
import cn.flyrise.feep.location.contract.SignInMainTabContract
import com.amap.api.maps.model.LatLng

/**
 * 新建：陈冕;
 * 日期： 2018-5-29-14:07.
 * 考勤组和非考勤组父类
 */

abstract class BaseSignInTabFragment : Fragment(), SignInMainTabContract.SignInMainListener {

    protected var mFragmentNearby: SignInDefaultNearbyFragment? = null//附件地点（列表签到）
    protected var mFragmentPlace: BaseSignInPlaceFragment? = null//地点签到（自定义考勤点、考勤点）

    override fun notifiCurentLocation(latLng: LatLng) {
        mFragmentPlace?.notifiCurentLocation(latLng)
    }

    override fun notifiCurentWorkingData(signState: WorkingSignState, signData: SignInAttendanceData) {
        mFragmentPlace?.notifiCurentWorkingData(signState, signData)
    }

    override fun refreshListData(items: List<SignPoiItem>?) {
        mFragmentNearby?.refreshListData(items)
    }

    override fun loadMoreListData(items: List<SignPoiItem>?) {
        mFragmentNearby?.loadMoreListData(items)
    }

    override fun signSuccessShowIcon() {
        mFragmentNearby?.signSuccessShowIcon()
        mFragmentPlace?.setSignInSuccessIcon(true)
    }

    override fun restartWorkingTime() {
        mFragmentNearby?.restartWorkingTime()
        mFragmentPlace?.setSignInSuccessIcon(false)
    }

    override fun setRefreshing(isRefresh: Boolean) {
        mFragmentNearby?.setRefreshing(isRefresh)
    }

    override fun setSwipeRefreshEnabled(isEnabled: Boolean) {
        mFragmentNearby?.setSwipeRefreshEnabled(isEnabled)
    }

    override fun setSignInButtomEnabled(isEnabled: Boolean) {
        mFragmentNearby?.setSignInButtomEnabled(isEnabled)
        mFragmentPlace?.setSignInButtomEnabled(isEnabled)
    }

    override fun getCurrentSignInItem(): LocationSaveItem? {
        return null
    }

    override fun loadMoreState(state: Int) {
    }

    override fun setSignRange(signRange: Int) {
        mFragmentNearby?.mRange = signRange
        mFragmentPlace?.mRange = signRange
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if (mFragmentNearby != null) mFragmentNearby?.onDestroy()
//        if (mFragmentPlace != null) mFragmentPlace?.onDestroy()
//    }
}
