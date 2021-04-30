package cn.flyrise.feep.location.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.location.bean.LocationSaveItem
import cn.flyrise.feep.location.bean.SignInFragmentData
import cn.flyrise.feep.location.contract.LocationQueryPoiContract
import cn.flyrise.feep.location.contract.SignInMainTabContract
import cn.flyrise.feep.location.event.EventCustomSettingAddress
import cn.flyrise.feep.location.event.EventTempCustomSignAddress
import cn.flyrise.feep.location.util.LocationCustomSaveUtil
import cn.flyrise.feep.location.util.LocationSignDistanceUtil
import cn.flyrise.feep.location.util.SignInUtil
import cn.flyrise.feep.location.views.LocationSendActivity
import cn.flyrise.feep.location.views.SignInCustomSelectedActivity
import com.amap.api.maps.model.LatLng
import kotlinx.android.synthetic.main.location_sign_in_custom_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 新建：陈冕;
 * 日期： 2018-5-25-15:06.
 * 自定义考勤点签到
 */

class SignInCustomFragment : BaseSignInPlaceFragment() {

    private var currentSaveItem: LocationSaveItem? = null
    private var isSignInSuccess: Boolean = false//是否为点击签到按钮签到
    private var mListener: SignInMainTabContract.SignInTabListener? = null
    private var mCurrentLatLng: LatLng? = null
    private var fragmentData: SignInFragmentData? = null

    private var mInitListener: (() -> Unit)? = null

    private fun setSignInFragmentData(data: SignInFragmentData) {
        this.fragmentData = data
        this.mCurrentLatLng = data.latLng
        this.mListener = data.listener
    }

    fun setLatLng(latLng: LatLng) {
        this.mCurrentLatLng = latLng
    }

    fun setListener(listener: SignInMainTabContract.SignInTabListener) {
        this.mListener = listener
    }

    fun setInitListener(mInitListener: () -> Unit) {
        this.mInitListener = mInitListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        return inflater.inflate(R.layout.location_sign_in_custom_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mLayout = layout
        mLayoutSignIns = mLayoutSignIn
        super.onActivityCreated(savedInstanceState)
        if (fragmentData?.isSearch ?: false) {
            currentSaveItem = fragmentData?.saveItem
            mImgRightIcon?.visibility = View.GONE
        } else {
            mImgRightIcon?.visibility = View.VISIBLE
            showSettingLayout(LocationCustomSaveUtil.getSelectedLocationItem() == null)
            currentSaveItem = LocationCustomSaveUtil.getSelectedLocationItem()
            mLayoutHead!!.setOnClickListener { SignInCustomSelectedActivity.start(context!!, currentSaveItem?.poiId ?: "") }
        }
        initCustomData()
        bindListener()
        mListener?.onRestartLocation()
        mInitListener?.invoke()
    }

    private fun initCustomData() {
        if (currentSaveItem == null) return
        mTvTitle!!.text = currentSaveItem!!.title
        mTvAddress!!.text = currentSaveItem!!.content
        initCustomSignInView(getExceedDistance(mCurrentLatLng!!))
    }

    private fun showSettingLayout(isCustomNull: Boolean) {
        mLayoutSetting!!.visibility = if (isCustomNull) View.VISIBLE else View.GONE
        mLayoutCustom!!.visibility = if (isCustomNull) View.GONE else View.VISIBLE
    }

    private fun bindListener() {
        mTvSetting!!.setOnClickListener {
            //自定义为空去到添加地址界面，不为空去到设置界面
            LocationSendActivity.start(context, K.location.LOCATION_CUSTOM_SETTING, null)
        }
        mLayoutSignIn!!.setOnClickListener {
            if (mListener != null) {
                isSignInSuccess = true
                mListener!!.onSignInItem(currentSaveItem!!)
            }
        }
    }

    override fun getCurrentSaveItem() = currentSaveItem

    //    override fun getExceedDistance(latLng: LatLng) = SignInUtil.getExceedDistance(latLng, currentSaveItem, LocationQueryPoiContract.search)
    override fun getExceedDistance(latLng: LatLng) = SignInUtil.getExceedDistance(latLng, currentSaveItem, getCurRange())

    private fun getCurRange() = if (mRange >= 0) mRange else LocationQueryPoiContract.search

    override fun notifiCurentLocation(latLng: LatLng) {//更新用户当前位置
        this.mCurrentLatLng = latLng
        initCustomSignInView(getExceedDistance(latLng))
    }

    override fun setSignInSuccessIcon(isShow: Boolean) {
        isSignInSuccess = false
        if (fragmentData?.isSearch ?: false) return
        mImgRightIcon?.visibility = if (isShow) View.GONE else View.VISIBLE
    }

    private fun initCustomSignInView(exceedDistance: Float) {
        setSignInButtomEnabled(exceedDistance <= 0)
        mTvError!!.text = LocationSignDistanceUtil.getExceedText(context, exceedDistance)
        mTvErrorLayout.visibility = if (exceedDistance <= 0) View.GONE else View.VISIBLE
    }

    override fun setSignInButtomEnabled(isEnabled: Boolean?) {
        if (mLayoutSignIn == null) return
        mLayoutSignIn!!.isEnabled = isEnabled!!
        mLayoutSignIn!!.isSelected = !isEnabled
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventSignInAutoModify(signAddress: EventTempCustomSignAddress) {//临时修改
        currentSaveItem = signAddress.mSaveItem
        initCustomData()
        if (mListener != null) mListener!!.onRestartLocation()
        showSettingLayout(LocationCustomSaveUtil.getSelectedLocationItem() == null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventSignInAutoModify(address: EventCustomSettingAddress) {
        if (address.saveItem != null) {//不为空一般为第一次进来设置的第一个地址
            address.saveItem.isCheck = true
            LocationCustomSaveUtil.setSavePoiItems(address.saveItem)
        }
        showSettingLayout(LocationCustomSaveUtil.getSelectedLocationItem() == null)
        currentSaveItem = LocationCustomSaveUtil.getSelectedLocationItem()
        initCustomData()
        FEToast.showContentMessage(getString(R.string.location_sign_in_custiom_custom))
        if (mListener != null) mListener!!.onRestartLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        isSignInSuccess = false
        EventBus.getDefault().unregister(this)
    }

    companion object {

        fun getInstance(latLng: LatLng, listener: SignInMainTabContract.SignInTabListener, mInitListener: () -> Unit): SignInCustomFragment {
            val fragment = SignInCustomFragment()
            fragment.setLatLng(latLng)
            fragment.setListener(listener)
            fragment.setInitListener(mInitListener)
            return fragment
        }

        fun getInstance(data: SignInFragmentData): SignInCustomFragment {
            val fragment = SignInCustomFragment()
            fragment.setSignInFragmentData(data)
            return fragment
        }
    }
}
