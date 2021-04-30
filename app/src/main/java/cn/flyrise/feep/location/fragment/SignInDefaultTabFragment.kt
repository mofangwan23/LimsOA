package cn.flyrise.feep.location.fragment

import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.utils.UIUtil
import cn.flyrise.feep.location.adapter.SignInFragmentAdapter
import cn.flyrise.feep.location.bean.SignInSetAMapStyle
import cn.flyrise.feep.location.bean.SignPoiItem
import cn.flyrise.feep.location.contract.LocationQueryPoiContract
import cn.flyrise.feep.location.contract.SignInMainTabContract
import cn.flyrise.feep.location.util.LocationCustomSaveUtil
import cn.flyrise.feep.location.util.SignInUtil
import com.amap.api.maps.model.LatLng
import kotlinx.android.synthetic.main.location_sign_in_default_tab_fragment.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-25-15:08.
 * 默认考勤点（自定义考勤点、附件列表考勤、异常考勤）
 */

class SignInDefaultTabFragment : BaseSignInTabFragment() {

    private var mCurrentUserLatLng: LatLng? = null//用户当前位置
    private var mCurrentPostion: Int = 0
    private var mListener: SignInMainTabContract.SignInTabListener? = null
    private val mHandler = Handler()
    private val style = SignInSetAMapStyle()
    private var isOpenCustom = false
    private var signRange: Int = 0

    companion object {

        fun getInstance(latLng: LatLng?, listener: SignInMainTabContract.SignInTabListener, isOpen: Boolean): SignInDefaultTabFragment {
            val fragment = SignInDefaultTabFragment()
            fragment.setLatLng(latLng!!)
            fragment.setListener(listener)
            fragment.setOpenCustom(isOpen)
            return fragment
        }
    }

    override fun setSignRange(signRange: Int) {
        super.setSignRange(signRange)
        this.signRange = signRange
    }

    private fun setOpenCustom(isOpen: Boolean) {
        isOpenCustom = isOpen
    }

    private fun setLatLng(latLng: LatLng) {
        this.mCurrentUserLatLng = latLng
    }

    private fun setListener(listener: SignInMainTabContract.SignInTabListener) {
        this.mListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.location_sign_in_default_tab_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mFragmentNearby = SignInDefaultNearbyFragment.getInstance(mListener!!)
        mFragmentPlace = SignInCustomFragment.getInstance(mCurrentUserLatLng!!, mListener!!, { isShowCustomFragmnet() })
        mFragmentNearby?.mRange = signRange
        mFragmentPlace?.mRange = signRange
        mTabLayout?.apply {
            addTab(mTabLayout!!.newTab())
            addTab(mTabLayout!!.newTab())
            mViewPager!!.adapter = SignInFragmentAdapter(childFragmentManager, arrayListOf(mFragmentNearby, mFragmentPlace))
            setupWithViewPager(mViewPager)
            getTabAt(0)!!.text = getString(R.string.location_sign_in_nearby_addendance)
            getTabAt(1)!!.text = getString(R.string.location_sign_in_custiom_addendance)
        }
        UIUtil.fixTabLayoutIndicatorWidth(mTabLayout, 36)
        mViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                mCurrentPostion = position
                notifyAMapStyle(position == 1)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun notifyAMapStyle(isCustom: Boolean) {
        if (mListener == null || mFragmentNearby == null || mFragmentPlace == null) return
        style.apply {
            isAMapSignStyle = isCustom
            latLng = mCurrentUserLatLng
            signPoiItems = if (isCustom) null else mFragmentNearby?.poiItem
            signRange = mFragmentNearby?.mRange ?: LocationQueryPoiContract.search
            saveLatLng = if (isCustom) mFragmentPlace!!.getCurrentSaveItem()?.latLng else null
            isDottedLine = isCustom && mFragmentPlace?.getExceedDistance(mCurrentUserLatLng!!)!! > 50
            isMoveMap = true
        }
        mListener!!.onSetAMapStyle(style)
    }

    override fun notifiCurentLocation(latLng: LatLng) {//更新用户当前位置
        this.mCurrentUserLatLng = latLng
        notifyAMapStyle(mCurrentPostion == 1)
        super.notifiCurentLocation(latLng)
    }

    override fun refreshListData(items: List<SignPoiItem>?) {
        super.refreshListData(items)
        if (mViewPager != null) notifyAMapStyle(mViewPager!!.currentItem == 1)
    }

    override fun getCurrentSignInItem() = mFragmentPlace!!.getCurrentSaveItem()

    override fun loadMoreState(state: Int) {
        mFragmentNearby?.loadMoreState(state)
    }

    //选中的自定义考勤点在考勤范围内，滑动到考勤点签到
    private fun isMoreCustomView() = SignInUtil.getExceedDistance(mCurrentUserLatLng
            , LocationCustomSaveUtil.getSelectedLocationItem()?.latLng, mFragmentNearby?.mRange ?: LocationQueryPoiContract.search) <= 0

    private fun isShowCustomFragmnet() {
        mHandler.postDelayed({ mViewPager?.currentItem = if (isMoreCustomView() || isOpenCustom) 1 else 0 }, 500)
    }
}
