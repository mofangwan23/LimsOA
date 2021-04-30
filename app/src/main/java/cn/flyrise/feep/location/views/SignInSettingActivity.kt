package cn.flyrise.feep.location.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import cn.flyrise.android.shared.utility.FEUmengCfg
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.PreferencesUtils
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.location.service.LocationService
import cn.flyrise.feep.location.util.GpsStateUtils
import kotlinx.android.synthetic.main.location_settings.*

/**
 * 类描述：考勤设置
 * @author 罗展健
 * 2015年3月20日 下午2:06:53
 */
class SignInSettingActivity : BaseActivity(), GpsStateUtils.GpsStateListener {

    private var mGpsStateUtils: GpsStateUtils? = null
    private var lastAutoLocation = false

    companion object {
        fun start(context: Context, poiId: String, hasTimes: Boolean) {
            val intent = Intent(context, SignInSettingActivity::class.java)
            intent.putExtra("selected_poiId", poiId)//poiId自定义考勤点，选中当前自定义项
            intent.putExtra("IS_LOCATION_SIGN_TIME", hasTimes)//考勤组情况下显示自定位置上报开关
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGpsStateUtils = GpsStateUtils(this)
        setContentView(R.layout.location_settings)
    }

    override fun toolBar(toolbar: FEToolbar) {
        toolbar.setTitle(R.string.location_setting)
    }

    override fun bindData() {
        super.bindData()
        lastAutoLocation = SpUtil.get(PreferencesUtils.LOCATION_LOCUS_IS_REPORT, true)!!
        val isSignInWorkTime = intent?.getBooleanExtra("IS_LOCATION_SIGN_TIME", false) ?: false
        customLayout.visibility = if (isSignInWorkTime) View.GONE else View.VISIBLE
        mRlAutoLocation!!.visibility = if (isSignInWorkTime) View.VISIBLE else View.GONE
        mButAutoBox!!.isChecked = lastAutoLocation
        if (lastAutoLocation && !mGpsStateUtils!!.gpsIsOpen()) openGPSSetting()
    }

    override fun bindListener() {
        super.bindListener()
        mButAutoBox!!.setOnClickListener {
            SpUtil.put(PreferencesUtils.LOCATION_LOCUS_IS_REPORT, mButAutoBox!!.isChecked)
            if (mButAutoBox!!.isChecked) openGPSSetting()
            else LocationService.stopLocationService(this)
        }

        mButSignInRepidly!!.setOnClickListener {
            if (mButSignInRepidly!!.isChecked) startActivity(Intent(this, SignInRepidlySettingActivity::class.java))
        }
        customLayout.setOnClickListener { startActivity(Intent(this, SignInCustomModifyActivity::class.java)) }
    }

    override fun onPause() {
        super.onPause()
        FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.LocationSetting)
    }

    override fun onDestroy() {
        super.onDestroy()
        mGpsStateUtils!!.destroy()
    }

    override fun onRestart() {
        super.onRestart()
        restartAutoLocation()
    }

    private fun restartAutoLocation() {
        if (!lastAutoLocation) return
        if (mGpsStateUtils!!.gpsIsOpen()) openGps() else stopGps()
    }

    override fun onResume() {
        super.onResume()
        FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.LocationSetting)
    }

    /********定位搜索周边回调 */

    private fun openGPSSetting() {
        if (mGpsStateUtils!!.gpsIsOpen()) {
            openGps()
        } else {
            stopGps()
            mGpsStateUtils!!.openGPSSetting(getString(R.string.lbl_text_open_setting_gps))
        }
    }

    private fun openGps() {
        SpUtil.put(PreferencesUtils.LOCATION_LOCUS_IS_REPORT, true)
        LocationService.startLocationService(this, LocationService.REQUESTCODE)
        mButAutoBox!!.isChecked = true
    }

    private fun stopGps() {
        SpUtil.put(PreferencesUtils.LOCATION_LOCUS_IS_REPORT, false)
        mButAutoBox!!.isChecked = false
        LocationService.stopLocationService(this)
    }

    override fun onGpsState(isGpsState: Boolean) {
        if (isGpsState) openGps() else stopGps()
    }

    override fun cancleDialog() {
        stopGps()
    }

    override fun onDismiss() {
    }

}
