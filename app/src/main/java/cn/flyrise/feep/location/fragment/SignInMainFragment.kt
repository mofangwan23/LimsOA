package cn.flyrise.feep.location.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.android.shared.utility.FEUmengCfg
import cn.flyrise.feep.FEApplication
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.core.common.utils.NetworkUtil
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.location.bean.*
import cn.flyrise.feep.location.contract.*
import cn.flyrise.feep.location.dialog.SignInResultDialog
import cn.flyrise.feep.location.event.EventPhotographSignSuccess
import cn.flyrise.feep.location.presenter.SignInMainPresenter
import cn.flyrise.feep.location.util.LocationSaveFileUtil
import cn.flyrise.feep.location.views.OnSiteSignActivity
import cn.flyrise.feep.location.views.SignInSearchActivity
import cn.flyrise.feep.location.views.SignInSettingActivity
import cn.flyrise.feep.utils.Patches
import com.amap.api.maps.model.LatLng
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.location_sign_in_main_layout.*
import java.util.concurrent.TimeUnit

/**
 * 新建：陈冕;
 *日期： 2018-6-22-17:38.
 * 签到打卡主界面
 */
open class SignInMainFragment : BaseLocationFragment(), SignInMainContractView, SignInMainTabContract.SignInTabListener {

    @SuppressLint("UseSparseArrays")
    private var fragmentMap = mutableMapOf<Int, BaseSignInTabFragment>()
    private var isReStart = false//是否重新启动定位，签到成功会重新请求考勤组，所以无需重新开启
    private var isNetworkConnected = false//是否已经判断网络连接
    private var mSignDialog: SignInResultDialog? = null

    protected var mPresenter: SignInMainPresenter? = null
    protected var signInMainFragment: BaseSignInTabFragment? = null
    protected var signStyle: Int? = null //考勤类型(考勤组、非考勤组)
    private lateinit var mLeaderListener: (Boolean) -> Unit
    private var isOpenCustom = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.location_sign_in_main_layout, container, false)
    }

//    override fun setupStatusBar() {//锤子在沉侵模式下弹出键盘，地图会卡死
//        if (Build.DEVICE == CZ_PHONE) return
//        super.setupStatusBar()
//    }

    fun setToolbarListener(context: Context, mToolBar: FEToolbar) {
        mToolBar.title = context.getString(R.string.location_report)
        mToolBar.setRightIcon(R.drawable.sign_in_main_setting)
        mToolBar.setRightImageClickListener {
            if (FunctionManager.hasPatch(Patches.PATCH_SIGN_IN_STATICS)) {
                SignInSettingActivity.start(context, signInMainFragment?.getCurrentSignInItem()?.poiId ?: "", hasTimes())
            } else {
                mPresenter?.clickMoreSetting()
            }

        }
        if (((context as Activity).application as FEApplication).isOnSite) {  // 现场拍照
            mToolBar.setRightIcon(R.drawable.camara)
            mToolBar.setRightImageClickListener {
                startActivityForResult(Intent(context, OnSiteSignActivity::class.java), LocationReportSignContract.POST_PHOTO_SIGN_DATA)
            }
        }
//        mToolBar.setLineVisibility(View.GONE)
    }

    fun setLeaderListner(listener: (Boolean) -> Unit) {//是否为领导
        mLeaderListener = listener
    }

    fun setOpenCustom(isOpen: Boolean?) {
        isOpenCustom = isOpen ?: false
    }

    fun isLeader() = mPresenter?.isLeaderOrSubordinate() ?: false

    private fun hasTimes() = mPresenter?.hasTimes() ?: false

    open fun signInData() = mPresenter!!.signInData(null)

    override fun bindData() {
        mPresenter = SignInMainPresenter(context!!, this, aMap, mLeaderListener)
        super.bindData()
    }

    override fun onResume() {
        super.onResume()
        FEUmengCfg.onActivityResumeUMeng(context, FEUmengCfg.LocationChoose)
        mPresenter!!.onResume()
        if (isReStart) {
            isReStart = false
            mPresenter!!.getGPSLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        FEUmengCfg.onActivityPauseUMeng(context, FEUmengCfg.LocationChoose)
        mPresenter!!.onPause()
        isReStart = true
        mSignDialog?.dismiss()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindListener() {
        super.bindListener()

        RxView.clicks(the_contact_relative_search!!)//搜索签到
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe { startActivityForResult(Intent(context, SignInSearchActivity::class.java), OPEN_SEARCH_SIGN) }

        RxView.clicks(mImgLocation!!)//定位按钮
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe {
                    if (!NetworkUtil.isWifiEnabled(context) && !isNetworkConnected) {
                        isNetworkConnected = true
                        FEMaterialDialog.Builder(context)
                                .setMessage(resources.getString(R.string.location_error_hint))
                                .setPositiveButton(null, null).build().show()
                    }
                    mPresenter!!.getGPSLocation()
                }

        aMap.setOnMapTouchListener { mPresenter!!.setTouchMap() }
    }

    open fun setSignInSearchLayout(isShow: Boolean) {
        the_contact_relative_search?.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OPEN_SEARCH_SIGN) {
            isReStart = false
            mPresenter!!.requestWQT()
        }
        if (LocationReportSignContract.POST_PHOTO_SIGN_DATA == requestCode && resultCode == Activity.RESULT_OK) {
            val text = data?.getStringExtra("sign_in_success_data")
            val signSuccess: EventPhotographSignSuccess? = GsonUtil.getInstance().fromJson(text, EventPhotographSignSuccess::class.java)
            FELog.i("-->>>>sign:${text}")
            isReStart = false
            mPresenter!!.photoRequestHistory(signSuccess?.isTakePhotoError ?: false, signSuccess?.signSuccess)
        }
    }

    override fun notifitionSignInStyle(style: Int, latLng: LatLng?, isNotifiGpsLocation: Boolean) {//根据样式调整布局
        signStyle = if (style == K.sign.STYLE_ATT || style == K.sign.STYLE_LIST_ATT || style == K.sign.STYLE_MANY) SIGN_ATTENDANCE else SIGN_DEFAULT
        if (fragmentMap.containsKey(signStyle!!)) {
            if (fragmentMap[signStyle!!]!!.isHidden) {
                val ft: FragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
                ft.show(fragmentMap[signStyle!!]!!)
                ft.commitAllowingStateLoss()
            }
            notifiCurentLocation(latLng!!, isNotifiGpsLocation)
            return
        }
        signInMainFragment = when (signStyle!!) {
            SIGN_ATTENDANCE -> {
                val data = SignInFragmentData()
                data.style = style
                data.latLng = latLng
                data.signData = signInData()
                data.signState = mPresenter!!.workState()
                data.listener = this
                SignInAttendanceTabFragment.getInstance(data)
            }
            else -> SignInDefaultTabFragment.getInstance(latLng, this, isOpenCustom)
        }
        signInMainFragment!!.setSignRange(mPresenter!!.signRange)
        fragmentMap[signStyle!!] = signInMainFragment!!
        val ft: FragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
        ft.add(mLayoutFragment!!.id, signInMainFragment!!)
        ft.commitAllowingStateLoss()
    }

    protected fun notifiCurentLocation(latLng: LatLng, isRestartWorking: Boolean) {//更新用户当前位置
        signInMainFragment?.notifiCurentLocation(latLng)
        if (signInMainFragment != null && isRestartWorking)
            signInMainFragment!!.notifiCurentWorkingData(mPresenter!!.workState(), signInData())
    }

    fun loadMoreState(state: Int) {
        signInMainFragment?.loadMoreState(state)
    }

    fun isResultDialogSuccess() = mSignDialog?.isVisible ?: false

    override fun onRestartLocation() {
        mPresenter!!.getGPSLocation()
    }

    override fun onLoadMoreData() {
        mPresenter!!.getMorePoiSearch()
    }

    override fun onSignInItem(saveItem: LocationSaveItem) {
        mPresenter!!.signSelectedPoiItem(saveItem)
    }

    override fun onSignInErrorItem() {
        mPresenter!!.intentShowPhoto()
    }

    override fun onSetAMapStyle(style: SignInSetAMapStyle) {
        mPresenter!!.setAMapStyle(style)
    }

    override fun onFrontViewClick() {
        mPresenter!!.delayedRefreshActivityView()
    }

    @SuppressLint("SetTextI18n")
    override fun setAttendanceServiceTime(serviceCurrentTiem: LocationSignTime?) {//服务端当前时间
        if (signInMainFragment == null) return
        if (signInMainFragment is SignInAttendanceTabFragment)
            (signInMainFragment as SignInAttendanceTabFragment).setServiceTime(serviceCurrentTiem!!, mPresenter!!.isCanReport())
    }

    override fun setAttendanceWorkingTimeInterval(timeInterval: String?) {//考勤区间
        if (signInMainFragment == null) return
        if (signInMainFragment is SignInAttendanceTabFragment)
            (signInMainFragment as SignInAttendanceTabFragment).setAttendanceTime(timeInterval!!)
    }

    override fun restartWorkingTime() {
        signInMainFragment?.restartWorkingTime()
    }

    override fun setSwipeRefresh(isRefresh: Boolean) {
        signInMainFragment?.setRefreshing(isRefresh)
    }

    override fun startSignViewOperation(isEnabled: Boolean) {
        if (mImgLocation != null) mImgLocation!!.isEnabled = isEnabled
        if (mImgLocation != null) mImgLocation!!.isSelected = !isEnabled
        signInMainFragment?.setSwipeRefreshEnabled(isEnabled)
    }

    override fun signSuccessShowIcon() {
        signInMainFragment?.signSuccessShowIcon()
    }

    override fun setSignInButtomEnabled(isEnabled: Boolean) {
        signInMainFragment?.setSignInButtomEnabled(isEnabled)
    }

    override fun refreshListData(items: List<SignPoiItem>?) {
        signInMainFragment?.refreshListData(items)
    }

    override fun loadMoreListData(items: List<SignPoiItem>?) {
        signInMainFragment?.loadMoreListData(items)
    }

    override fun restartRequesstGPSLocation() {
        mPresenter!!.getGPSLocation()
    }

    override fun locationPermissionGranted() {//权限设置成功，开始请求考勤参数
        mPresenter!!.requestWQT()
    }

    open fun locationSignSuccess(time: String, address: String) {//签到成功，弹出提示框
        mSignDialog = SignInResultDialog().setContext(context!!).setTime(time).setSuccessText(address).setLeader(isLeader())
        mSignDialog?.show(activity!!.supportFragmentManager, "SignInResultDialog")
        mPresenter!!.requestWQT()
    }

    override fun onDestroy() {
        LoadingHint.hide()
        mPresenter!!.onDestroy()
        LocationSaveFileUtil.getInstance().onDestroy()
        super.onDestroy()
        this.fragmentMap.clear()
        signInMainFragment?.onDestroy();
        signInMainFragment = null
    }
}