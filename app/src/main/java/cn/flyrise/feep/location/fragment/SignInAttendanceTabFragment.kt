package cn.flyrise.feep.location.fragment

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.location.bean.*
import cn.flyrise.feep.location.contract.LocationQueryPoiContract
import cn.flyrise.feep.location.contract.SignInMainTabContract
import cn.flyrise.feep.location.util.SignInUtil
import com.amap.api.maps.model.LatLng
import kotlinx.android.synthetic.main.location_sign_in_attendance_tab_fragment.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-25-15:10.
 * 考勤组签到（考勤点签到、考勤列表签到）
 */

class SignInAttendanceTabFragment : BaseSignInTabFragment() {

    private var mStyle: Int = 0
    private var mUserLatLng: LatLng? = null//距离考勤点多少米
    private var mListener: SignInMainTabContract.SignInTabListener? = null
    private var mSignState: WorkingSignState? = null//当前考勤状态
    private var mSignData: SignInAttendanceData? = null//当前考勤状态
    private val style = SignInSetAMapStyle()
    private var signRange: Int = 0

    private var fragmentData: SignInFragmentData? = null

    private val isNearbySignin: Boolean//考勤列表签到
        get() {
            when {
                mStyle == K.sign.STYLE_MANY ->
                    return true//多次签到
                mSignData == null ->
                    return mStyle == K.sign.STYLE_LIST_ATT//考勤数据为空,考勤组用户不存在
                mStyle == K.sign.STYLE_LIST_ATT && (TextUtils.isEmpty(mSignData!!.pname) || TextUtils.isEmpty(mSignData!!.paddress)) ->
                    return true//兼容65以前考版本考勤组签到
                mStyle == K.sign.STYLE_LIST_ATT && SignInUtil.getExceedDistance(mUserLatLng, mSignData!!.latLng, mSignData!!.range) > 0 ->
                    return true//考勤组人员不为空，考勤数据异常
                else -> return false
            }
        }

    private fun setSignInFragmentData(data: SignInFragmentData) {
        this.fragmentData = data
        this.mStyle = data.style ?: 0
        this.mSignState = data.signState
        this.mUserLatLng = data.latLng
        this.mSignData = data.signData
        this.mListener = data.listener
    }

    override fun setSignRange(signRange: Int) {
        super.setSignRange(signRange)
        this.signRange = signRange
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
        if (isNearbySignin) { //考勤组列表考勤
            if (mFragmentNearby == null) {
                mFragmentNearby = SignInAttendanceNearbyFragment.getInstance(mStyle == K.sign.STYLE_MANY, mListener!!)
                ft.add(R.id.fragment_layout_attendance, mFragmentNearby!!)
                mFragmentNearby?.mRange = signRange
            } else if (mFragmentNearby!!.isHidden) ft.show(mFragmentNearby!!)
        } else {
            if (mFragmentPlace == null) {
                mFragmentPlace = SignInAttendanceFragment.getInstance(fragmentData!!)
                mFragmentPlace?.mRange = signRange
                ft.add(R.id.fragment_layout_attendance, mFragmentPlace!!)
            } else if (mFragmentPlace!!.isHidden) ft.show(mFragmentPlace!!)
        }
        ft.commitAllowingStateLoss()
    }

    override fun notifiCurentLocation(latLng: LatLng) {
        super.notifiCurentLocation(latLng)
        setAttendanceFragment()
        mTvTitle!!.text = context!!.resources.getString(if (isNearbySignin) R.string.location_worktime_title
        else R.string.location_sign_in_addendance)
        notifyAMapStyle(!isNearbySignin)
    }

    override fun loadMoreState(state: Int) {
        mFragmentNearby?.loadMoreState(state)
    }

    fun setAttendanceTime(time: String) {//考勤区间
        if (mStyle == K.sign.STYLE_MANY) return
        if (mTvAttendanceTime != null) mTvAttendanceTime!!.text = time
    }

    fun setServiceTime(serviceTime: LocationSignTime, isCanReport: Boolean) {//服务器当前时间
        if (mFragmentPlace != null && mFragmentPlace is SignInAttendanceFragment)
            (mFragmentPlace as SignInAttendanceFragment).setServiceTime(serviceTime, isCanReport)
        if (mFragmentNearby != null && mFragmentNearby is SignInAttendanceNearbyFragment)
            (mFragmentNearby as SignInAttendanceNearbyFragment).setCurrentServiceTime(serviceTime, isCanReport)
    }

    override fun refreshListData(items: List<SignPoiItem>?) {
        super.refreshListData(items)
        notifyAMapStyle(!isNearbySignin)
    }

    private fun notifyAMapStyle(isAttendance: Boolean) {
        if (mListener == null) return
        style.isAMapSignStyle = isAttendance
        style.latLng = mUserLatLng
        style.signPoiItems = if (isAttendance && mFragmentNearby == null) null else mFragmentNearby!!.poiItem
        style.signRange = if (mSignState == null || mSignState!!.signRange <= 0) LocationQueryPoiContract.search else mSignState!!.signRange
        style.saveLatLng = if (isAttendance && mSignData != null) mSignData!!.latLng else null
        style.isDottedLine = isAttendance && mFragmentPlace != null && mFragmentPlace!!.getExceedDistance(mUserLatLng!!) > 50
        style.isMoveMap = true
        mListener!!.onSetAMapStyle(style)
    }

    override fun onStart() {
        super.onStart()
        mTvTitle!!.text = context!!.resources.getString(if (isNearbySignin) R.string.location_worktime_title
        else R.string.location_sign_in_addendance)
        notifyAMapStyle(!isNearbySignin)
    }

    companion object {

        fun getInstance(data: SignInFragmentData): SignInAttendanceTabFragment {
            val fragment = SignInAttendanceTabFragment()
            fragment.setSignInFragmentData(data)
            return fragment
        }
    }
}

