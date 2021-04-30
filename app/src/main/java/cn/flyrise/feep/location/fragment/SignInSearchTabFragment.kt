package cn.flyrise.feep.location.fragment

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.location.bean.*
import cn.flyrise.feep.location.contract.LocationQueryPoiContract
import cn.flyrise.feep.location.contract.SignInMainTabContract
import com.amap.api.maps.model.LatLng
import kotlinx.android.synthetic.main.location_sign_in_attendance_tab_fragment.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-25-15:10.
 * 搜索签到（考勤组、自定义位置）
 */

class SignInSearchTabFragment : BaseSignInTabFragment() {

    private var mStyle: Int = 0
    private var mUserLatLng: LatLng? = null//距离考勤点多少米
    private var mListener: SignInMainTabContract.SignInTabListener? = null
    private var mSignState: WorkingSignState? = null//当前考勤状态
    private var mSignData: SignInAttendanceData? = null//当前考勤状态
    private val style = SignInSetAMapStyle()

    private var isSignInGroup: Boolean = false
    private var fragmentData: SignInFragmentData? = null

    private var curRange: Int = 0

    private fun setSignInFragmentData(data: SignInFragmentData) {
        this.apply {
            fragmentData = data
            mStyle = data.style ?: 0
            mSignState = data.signState
            mUserLatLng = data.latLng
            mSignData = data.signData
            mListener = data.listener
        }
        isSignInGroup = mStyle == K.sign.STYLE_ATT || mStyle == K.sign.STYLE_LIST_ATT || mStyle == K.sign.STYLE_MANY
    }

    override fun setSignRange(signRange: Int) {
        super.setSignRange(signRange)
        this.curRange = signRange
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.location_sign_in_attendance_tab_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setAttendanceFragment()
    }

    private fun setAttendanceFragment() {
        val ft: FragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        if (mFragmentPlace == null) {
            if (isSignInGroup) {
                mFragmentPlace = SignInAttendanceFragment.getInstance(fragmentData!!)
            } else {
                mFragmentPlace = SignInCustomFragment.getInstance(fragmentData!!)
            }
            mFragmentPlace!!.mRange = curRange
            ft.add(R.id.fragment_layout_attendance, mFragmentPlace!!)
        } else if (mFragmentPlace!!.isHidden) {
            ft.show(mFragmentPlace!!)
        }
        ft.commitAllowingStateLoss()
    }

    override fun notifiCurentLocation(latLng: LatLng) {
        super.notifiCurentLocation(latLng)
        setAttendanceFragment()
        mTvTitle!!.text = context!!.resources.getString(if (false) R.string.location_worktime_title
        else R.string.location_sign_in_addendance)
        notifyAMapStyle(true)
    }

    fun setAttendanceTime(time: String) {//考勤区间
        if (mStyle == K.sign.STYLE_MANY) return
        if (mTvAttendanceTime != null) mTvAttendanceTime!!.text = time
    }

    fun setServiceTime(serviceTime: LocationSignTime, isCanReport: Boolean) {//服务器当前时间
        if (mFragmentPlace != null && mFragmentPlace is SignInAttendanceFragment)
            (mFragmentPlace as SignInAttendanceFragment).setServiceTime(serviceTime, isCanReport)
    }

    override fun refreshListData(items: List<SignPoiItem>?) {
        super.refreshListData(items)
        notifyAMapStyle(true)
    }

    private fun notifyAMapStyle(isAttendance: Boolean) {
        if (mListener == null) return
        style.isAMapSignStyle = isAttendance
        style.latLng = mUserLatLng
        style.signPoiItems = null
        style.signRange = if (mSignState == null || mSignState!!.signRange <= 0) LocationQueryPoiContract.search else mSignState!!.signRange
        style.saveLatLng = mFragmentPlace?.getCurrentSaveItem()?.latLng
        style.isDottedLine = mFragmentPlace != null && mFragmentPlace!!.getExceedDistance(mUserLatLng!!) > 50
        style.isMoveMap = true
        mListener!!.onSetAMapStyle(style)
    }

    override fun onStart() {
        super.onStart()
        mTvTitle!!.text = context!!.resources.getString(if (false) R.string.location_worktime_title
        else R.string.location_sign_in_addendance)
        notifyAMapStyle(true)
    }

    companion object {

        fun getInstance(data: SignInFragmentData): SignInSearchTabFragment {
            val fragment = SignInSearchTabFragment()
            fragment.setSignInFragmentData(data)
            return fragment
        }
    }
}

