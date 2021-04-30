package cn.flyrise.feep.location.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import cn.flyrise.android.shared.utility.FEUmengCfg
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.location.adapter.BaseSelectedAdapter
import cn.flyrise.feep.location.adapter.LocationDateAdapter
import cn.flyrise.feep.location.adapter.LocationPersonAdapter
import cn.flyrise.feep.location.bean.LocusDates
import cn.flyrise.feep.location.bean.LocusPersonLists
import cn.flyrise.feep.location.contract.LocationLocusContract
import cn.flyrise.feep.location.presenter.LocationLocusPersenter
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapFragment
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.location_locus.*

/**
 * 类描述：考勤轨迹
 * @author 陈冕
 * @version 1.0
 */
class SignInLocusActivity : BaseActivity(), LocationLocusContract.View, BaseSelectedAdapter.OnSelectedClickeItemListener, LocationDateAdapter
.OnDateClickeItemListener {

    private var aMap: AMap? = null
    private var mPresenter: LocationLocusContract.presenter? = null

    //在地图表面的点击事件顺序必须是： 1、关闭listview 2、关闭标记提示框
    private val OnMapClickListener = AMap.OnMapClickListener {
        // 如果是正在显示人员或者日期，则是关闭
        if (isSelectListVisible) mPresenter!!.resetSelectedLayout()
        mPresenter!!.hideSelectedMarker()
    }

    private val InfoWindowAdapter = object : AMap.InfoWindowAdapter {
        override fun getInfoWindow(marker: Marker): View {
            return setInfoWindowView(marker)
        }

        override fun getInfoContents(arg0: Marker): View? {
            return null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_locus)
    }

    override fun toolBar(toolbar: FEToolbar) {
        toolbar.setTitle(R.string.location_locus)
    }

    override fun bindView() {
        mRecyclerViewDate.layoutManager=LinearLayoutManager(this)
        mRecyclerViewPerson.layoutManager=LinearLayoutManager(this)
        aMap = (fragmentManager.findFragmentById(R.id.aMap) as TextureMapFragment).map
        setUpMap()
    }

    private fun setUpMap() {
        if (aMap == null) return
        aMap!!.uiSettings.isScaleControlsEnabled = true// 设置默认标尺
        aMap!!.uiSettings.isRotateGesturesEnabled = false// 禁止地图旋转手势
        aMap!!.uiSettings.isTiltGesturesEnabled = false// 禁止倾斜手势
        aMap!!.setOnMarkerClickListener { marker ->
            mPresenter!!.setSelectedMarker(marker)
            if (marker.isInfoWindowShown) marker.hideInfoWindow()
            if (isSelectListVisible) mPresenter!!.resetSelectedLayout()
            false
        }// 点击地图标记事件
        aMap!!.setInfoWindowAdapter(InfoWindowAdapter)// 弹出框适配
        aMap!!.setOnInfoWindowClickListener { marker -> if (marker.isInfoWindowShown()) marker.hideInfoWindow() }// 设置点击infoWindow事件监听器
        aMap!!.setOnMapClickListener(OnMapClickListener)// 触摸地图表面事件
        aMap!!.moveCamera(CameraUpdateFactory.zoomTo(14f))// 默认放大到500米
    }

    override fun bindData() {
        super.bindData()
        mPresenter = LocationLocusPersenter(this)
    }

    override fun bindListener() {
        super.bindListener()
        dateSelect!!.setOnClickListener { mPresenter!!.clickeDateButton() }//日期选择（只有一周）
        personSelect!!.setOnClickListener { mPresenter!!.clickPersonButton() }//人员选择（只限自己的下属）
        mLayoutSelect!!.setOnClickListener { setSelectListVisibility(false) }
    }

    override fun setRecyclerVisibility(isDateVisibility: Boolean) {
        mRecyclerViewPerson!!.visibility = if (isDateVisibility) View.GONE else View.VISIBLE
        mRecyclerViewDate!!.visibility = if (isDateVisibility) View.VISIBLE else View.GONE
    }

    override fun isSelectListVisible() = mLayoutSelect!!.visibility == View.VISIBLE

    override fun setSelectListVisibility(isVisibility: Boolean) {
        mLayoutSelect!!.visibility = if (isVisibility) View.VISIBLE else View.GONE
    }

    override fun setDateButText(text: String) {
        dateSelect!!.text = text
    }

    override fun setPersonButText(text: String) {
        personSelect!!.text = text
    }

    override fun setSelectDateBut(isSelected: Boolean) {
        dateSelect!!.isSelected = isSelected
    }

    override fun setSelectPersonBut(isSelected: Boolean) {
        personSelect!!.isSelected = isSelected
    }

    override fun setDateButVisibility(isVisibility: Boolean) {
        dateSelect!!.visibility = if (isVisibility) View.VISIBLE else View.GONE
    }

    override fun setPersonButVisibility(isVisibility: Boolean) {
        personSelect!!.visibility = if (isVisibility) View.VISIBLE else View.GONE
    }

    override fun setSelectButtonLayout(personlist: List<LocusPersonLists>) {
        if (buttonlayout != null && !CommonUtil.nonEmptyList(personlist)) {
            mPresenter!!.latLngsEmpty()
            buttonlayout!!.visibility = View.GONE
        }
    }

    override fun setAMapMoveCamera(update: CameraUpdate) {
        if (aMap != null) aMap!!.moveCamera(update)
    }

    override fun setAMapAddMarker(markeroptions: MarkerOptions) {
        if (aMap != null) aMap!!.addMarker(markeroptions)
    }

    override fun setAMapAddPolyline(polyline: PolylineOptions) {
        if (aMap != null) aMap!!.addPolyline(polyline)
    }

    override fun setAMapClear() {
        if (aMap != null) aMap!!.clear()// 清空标记
    }

    override fun initPersonSelectWindow(personLists: List<LocusPersonLists>, userId: String) {
        if (mRecyclerViewPerson == null) return
        val personAdapter = LocationPersonAdapter(this, personLists)
        personAdapter.setListener(this)
        personAdapter.setCurrentPosition(userId)
        mRecyclerViewPerson!!.adapter = personAdapter
        mRecyclerViewPerson!!.scrollToPosition(personAdapter.currentPosition)
    }

    override fun initDateSelectWindow(dates: List<LocusDates>) {
        if (mRecyclerViewDate == null) return
        val dateAdapter = LocationDateAdapter(this, dates, this)
        mRecyclerViewDate!!.adapter = dateAdapter
        mRecyclerViewDate!!.scrollToPosition(dateAdapter.currenterPostion)
    }

    @SuppressLint("InflateParams")
    private fun setInfoWindowView(marker: Marker): View {
        val view = LayoutInflater.from(this@SignInLocusActivity).inflate(R.layout.location_locus_marker_layout, null)
        val useraddress = view.findViewById<TextView>(R.id.useraddress)
        val userPhotoLayout = view.findViewById<RelativeLayout>(R.id.userPhotoLayout)
        useraddress.width = PixelUtil.dipToPx(180f)
        if (!TextUtils.isEmpty(marker.title)) useraddress.text = marker.title
        if (!TextUtils.isEmpty(mPresenter!!.userName)) {
            (view.findViewById<View>(R.id.username) as TextView).text = mPresenter!!.userName
        }
        if (!TextUtils.isEmpty(mPresenter!!.userPhoto)) {
            userPhotoLayout.visibility = View.VISIBLE
            (view.findViewById<View>(R.id.userphoto) as TextView).text = mPresenter!!.userPhoto
            view.findViewById<View>(R.id.photoimage).setOnClickListener {
                if (!TextUtils.isEmpty(mPresenter!!.userPhoto)) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mPresenter!!.userPhoto))
                    this@SignInLocusActivity.startActivity(intent)
                }
            }
        } else
            userPhotoLayout.visibility = View.GONE
        if (!TextUtils.isEmpty(marker.snippet)) {
            (view.findViewById<View>(R.id.usertimes) as TextView).text = marker.snippet
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        mPresenter!!.resetSelectedLayout()
        FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.LocationLocus)
    }

    override fun onPause() {
        super.onPause()
        FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.LocationLocus)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter!!.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isSelectListVisible) {
            mPresenter!!.resetSelectedLayout()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSelectedClickeItem(id: String?, position: Int) {
        mPresenter!!.onPersonClickeItem(position)
    }

    override fun onDateClickeItem(dates: LocusDates, position: Int) {
        mPresenter!!.onDateClickeItem(position)
    }

    companion object {

        fun start(context: Context, day: String, userId: String) {
            val intent = Intent(context, SignInLocusActivity::class.java)
            intent.putExtra(LocationLocusContract.View.USER_ID, userId)
            intent.putExtra(LocationLocusContract.View.LOCATION_DAY, day)
            context.startActivity(intent)
        }
    }
}
