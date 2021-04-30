package cn.flyrise.feep.location.fragment

import android.support.v4.app.FragmentTransaction
import android.view.View
import cn.flyrise.feep.K
import cn.flyrise.feep.location.bean.LocationSaveItem
import cn.flyrise.feep.location.bean.LocationSignTime
import cn.flyrise.feep.location.bean.SignInAttendanceData
import cn.flyrise.feep.location.bean.SignInFragmentData
import cn.flyrise.feep.location.contract.SIGN_ATTENDANCE
import cn.flyrise.feep.location.contract.SIGN_DEFAULT
import cn.flyrise.feep.location.util.LocationSearchRecordSaveUtil
import com.amap.api.maps.model.LatLng
import kotlinx.android.synthetic.main.location_sign_in_main_layout.*

/**
 * 新建：陈冕;
 *日期： 2018-8-4-13:33.
 */
class SignInMainSearchFragment : SignInMainFragment() {

    private var isSearchAddress: Boolean = false
    private var searchTempData: LocationSaveItem? = null

    fun setLocationSaveItem(item: LocationSaveItem) {
        isSearchAddress = true
        searchTempData = item
    }

    override fun signInData(): SignInAttendanceData = mPresenter!!.signInData(searchTempData)

    override fun bindView() {
        super.bindView()
        the_contact_relative_search.visibility = if (isSearchAddress) View.GONE else View.VISIBLE
    }

    override fun setSignInSearchLayout(isShow: Boolean) {

    }

    override fun notifitionSignInStyle(style: Int, latLng: LatLng?, isNotifiGpsLocation: Boolean) {
        signStyle = if (style == K.sign.STYLE_ATT || style == K.sign.STYLE_LIST_ATT || style == K.sign.STYLE_MANY) SIGN_ATTENDANCE else SIGN_DEFAULT
        val ft: FragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
        if (signInMainFragment == null) {
            val data = SignInFragmentData()
            data.style = style
            data.latLng = latLng
            data.signData = signInData()
            data.signState = mPresenter!!.workState()
            data.listener = this
            data.isSearch = true
            data.saveItem = searchTempData
            signInMainFragment = SignInSearchTabFragment.getInstance(data)
            signInMainFragment!!.setSignRange(mPresenter!!.signRange)
            ft.add(mLayoutFragment!!.id, signInMainFragment!!)
            ft.commitAllowingStateLoss()
        }
        notifiCurentLocation(latLng!!, isNotifiGpsLocation)
    }

    override fun setAttendanceServiceTime(serviceCurrentTiem: LocationSignTime?) {
        if (signInMainFragment == null) return
        if (signInMainFragment is SignInSearchTabFragment)
            (signInMainFragment as SignInSearchTabFragment).setServiceTime(serviceCurrentTiem!!, mPresenter!!.isCanReport())

    }

    override fun setAttendanceWorkingTimeInterval(timeInterval: String?) {
        if (signInMainFragment == null) return
        if (signInMainFragment is SignInSearchTabFragment)
            (signInMainFragment as SignInSearchTabFragment).setAttendanceTime(timeInterval!!)
    }

    override fun locationSignSuccess(time: String, address: String) {
        super.locationSignSuccess(time, address)
        LocationSearchRecordSaveUtil.setSavePoiItem(searchTempData)
    }

}