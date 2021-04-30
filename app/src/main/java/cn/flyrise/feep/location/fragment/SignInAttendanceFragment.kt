package cn.flyrise.feep.location.fragment

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.location.bean.*
import cn.flyrise.feep.location.contract.SignInMainTabContract
import cn.flyrise.feep.location.util.LocationSignDistanceUtil
import cn.flyrise.feep.location.util.SignInUtil
import com.amap.api.maps.model.LatLng
import kotlinx.android.synthetic.main.location_sign_in_custom_fragment.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-25-15:14.
 * 考勤组-考勤点签到
 */

class SignInAttendanceFragment : BaseSignInPlaceFragment() {

    private val currentSaveItem = LocationSaveItem()
    private var mListener: SignInMainTabContract.SignInTabListener? = null
    private var mSignState: WorkingSignState? = null
    private var latLng: LatLng? = null//距离考勤点多少米
    private var isPostSignInData = false//是否正在上次数据
    private var isStartSignIn: Boolean = false//开始签到，暂停时间更新
    private var signTimeAdd = false

    private var mSignData: SignInAttendanceData? = null//当前考勤状态
    private val mHandler = Handler()
    private var mData: SignInFragmentData? = null

    private fun setSignInFragmentData(data: SignInFragmentData) {
        this.apply {
            mData = data;
            mSignState = data.signState
            latLng = data.latLng
            mSignData = data.signData
            mListener = data.listener
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.location_sign_in_custom_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mLayout = layout
        mLayoutSignIns = mLayoutSignIn
        super.onActivityCreated(savedInstanceState)
        initSetAttendanceData()
        mLayoutSignIn!!.setOnClickListener {
            FELog.i("-->>>>Sign-save-:" + GsonUtil.getInstance().toJson(currentSaveItem))
            isStartSignIn = true
            mHandler.postDelayed({ isStartSignIn = false }, 3600)
            if (mListener != null) mListener!!.onSignInItem(currentSaveItem)
        }
        mListener?.onRestartLocation()
    }

    private fun initSetAttendanceData() {
        if (mSignData == null || mSignState == null) return
        mTvTitle!!.text = mSignData!!.pname
        mTvAddress!!.text = mSignData!!.paddress
        setSignInButtomEnabled(getExceedDistance(latLng!!) <= 0 && mSignState!!.isCanReport)//在考勤范围内，并且在考勤时间内
        currentSaveItem.apply {
            title = mSignData!!.pname
            content = mSignData!!.paddress
            Latitude = if (mSignData!!.latLng == null) 0.0 else mSignData!!.latLng.latitude
            Longitude = if (mSignData!!.latLng == null) 0.0 else mSignData!!.latLng.longitude
        }
        mHandler.postDelayed({
            mTvServiceTime?.visibility = if (mData?.style == K.sign.STYLE_MANY) View.GONE else View.VISIBLE
        }, 500)
    }

    fun setServiceTime(serviceTime: LocationSignTime, isCanReport: Boolean) {//更新当前服务端时间，并判断是否允许签到
        if (isStartSignIn) return
        signTimeAdd = !signTimeAdd
        mTvServiceTime?.text = getSignTime(serviceTime)
        setSignInButtomEnabled(getExceedDistance(latLng!!) <= 0 && isCanReport && !isPostSignInData)
        setAttendanceErrorText(isCanReport, getExceedDistance(latLng!!))
    }

    override fun getCurrentSaveItem() = currentSaveItem

    override fun notifiCurentWorkingData(signState: WorkingSignState, signData: SignInAttendanceData) {
        this.mSignState = signState
        this.mSignData = signData
        initSetAttendanceData()
    }

    override fun notifiCurentLocation(latLng: LatLng) {//更新用户当前位置
        this.latLng = latLng
    }

    override fun setSignInSuccessIcon(isShow: Boolean) {
        this.isPostSignInData = isShow
    }

    override fun setSignInButtomEnabled(isEnabled: Boolean?) {
        if (mLayoutSignIn == null) return
        mLayoutSignIn!!.isEnabled = isEnabled!!
        mLayoutSignIn!!.isSelected = !isEnabled
    }

    override fun getExceedDistance(latLng: LatLng) = SignInUtil.getExceedDistance(latLng, mSignData!!.latLng, mSignData!!.range)

    private fun setAttendanceErrorText(isCanReport: Boolean, mExceed: Float) {
        mTvError.text = when {
            !isCanReport -> context!!.resources.getString(R.string.location_time_overs)
            mExceed > 0 -> LocationSignDistanceUtil.getExceedText(context, mExceed)
            else -> ""
        }
        mTvErrorLayout.visibility = if (!isCanReport || mExceed > 0) View.VISIBLE else View.GONE
    }

    private fun getSignTime(signData: LocationSignTime): String {
        return if (signTimeAdd) String.format("%02d : %02d", signData.hour, signData.minute)
        else String.format("%02d   %02d", signData.hour, signData.minute)
    }

    companion object {

        fun getInstance(data: SignInFragmentData): SignInAttendanceFragment {
            val fragment = SignInAttendanceFragment()
            fragment.setSignInFragmentData(data)
            return fragment
        }
    }
}
