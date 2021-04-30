package cn.flyrise.feep.auth.server.setting

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import cn.flyrise.feep.R
import cn.flyrise.feep.auth.EventObj
import cn.flyrise.feep.auth.SettingObj
import cn.flyrise.feep.auth.unknown.NewLoginActivity
import cn.flyrise.feep.auth.views.CaptureActivity
import cn.flyrise.feep.auth.vpn.setting.VpnSettingActivity
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.cookie.PersistentCookieJar
import cn.flyrise.feep.core.premission.FePermissions
import cn.flyrise.feep.core.premission.PermissionCode
import cn.flyrise.feep.core.premission.PermissionGranted
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils
import cn.flyrise.feep.more.PrivacyActivity
import cn.flyrise.feep.utils.ParseCaptureUtils
import kotlinx.android.synthetic.main.activity_server_setting.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.net.ssl.HttpsURLConnection

/**
 * @author 社会主义接班人
 * @since 2018-08-07 10:14
 */

interface ServerSettingView {
    fun display(server: String, port: String, isHttps: Boolean, isVpn: Boolean)
    fun saveSuccess()
    fun saveError()
}

class ServerSettingActivity : NotTranslucentBarActivity(), ServerSettingView {

    private lateinit var p: ServerSettingPresenter
    private var swipeQrCode = fun() {
        FePermissions.with(this@ServerSettingActivity)
                .permissions(arrayOf(Manifest.permission.CAMERA))
                .rationaleMessage(resources.getString(R.string.permission_rationale_camera))
                .requestCode(PermissionCode.CAMERA)
                .request()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        val sb = UserInfoTableUtils.find()
        p = ServerSettingPresenter(ServerSettingRepository(sb), this)
        setContentView(R.layout.activity_server_setting)
        p.start()
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        toolbar?.setLineVisibility(View.GONE)
        toolbar?.setBackgroundColor(Color.TRANSPARENT)
        toolbar?.setTitleTextColor(Color.BLACK)
    }

    override fun bindData() {
        layoutNext.isEnabled = false
        layoutNext.setOnClickListener {
            layoutNext.isEnabled = false
            progressBar.visibility = View.VISIBLE
            tvNext.text = "正在加载"
            p.next(etServerAddress.text.toString(),
                    etServerPort.text.toString(), isSSLOpen.isChecked)
        }
        privacyImg.isSelected = true
    }

    override fun bindListener() {
        val enableNext = fun() {
            if (!TextUtils.isEmpty(etServerAddress.text.toString())) {
//                    && !TextUtils.isEmpty(etServerPort.text.toString())) {
                layoutNext.isEnabled = true
                layoutNext.setBackgroundResource(R.drawable.nms_bg_blue_btn_round_enable)
            } else {
                layoutNext.isEnabled = false
                layoutNext.setBackgroundResource(R.drawable.nms_bg_blue_btn_round_unable)
            }
        }

        etServerAddress.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etServerAddress.text.toString())) {
                ivServerAddressDel.visibility = View.VISIBLE
            } else {
                ivServerAddressDel.visibility = View.GONE
            }
        }

        etServerPort.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etServerPort.text.toString())) {
                ivServerPortDel.visibility = View.VISIBLE
            } else {
                ivServerPortDel.visibility = View.GONE
            }
        }


        etServerAddress.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivServerAddressDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
                enableNext()
            }
        })

        etServerPort.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivServerPortDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
                enableNext()
            }
        })

        ivServerAddressDel.setOnClickListener { etServerAddress.setText("") }
        ivServerPortDel.setOnClickListener { etServerPort.setText("") }

        layoutMoreSetting.setOnClickListener { showMoreSetting(layoutOtherSetting.visibility == View.GONE) }

        vpnSettingLayout.setOnClickListener {
            startActivity(Intent(this@ServerSettingActivity, VpnSettingActivity::class.java).apply {
                val sb = p.vpnSetting()
                putExtra("isVpn", sb.isVPN)
                putExtra("vpnServer", sb.vpnAddress)
                putExtra("vpnPort", sb.vpnPort)
                putExtra("vpnAccount", sb.vpnUsername)
                putExtra("vpnPassword", sb.vpnPassword)
            })
        }
        ivQrCodeSwipe.setOnClickListener { swipeQrCode() }

        privacyImg.setOnClickListener {
            privacyImg.isSelected = !privacyImg.isSelected
//            viewMaskLayer.visibility = if (privacyImg.isSelected) View.GONE else View.VISIBLE
            layoutNext.isSelected = !privacyImg.isSelected
            setLayoutEnabled(privacyImg.isSelected)
        }

        privacyTv.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }
    }

    private fun setLayoutEnabled(isSelected: Boolean) {
        layoutNext.isEnabled = isSelected
        layoutMoreSetting.isEnabled = isSelected
        layoutOtherSetting.isEnabled = isSelected
        etServerPort.isEnabled = isSelected
        ivServerPortDel.isEnabled = isSelected
        etServerAddress.isEnabled = isSelected
        ivQrCodeSwipe.isEnabled = isSelected
        ivServerAddressDel.isEnabled = isSelected
        isSSLOpen.isEnabled = isSelected
        vpnSettingLayout.isEnabled = isSelected
    }

    private fun showMoreSetting(isShow: Boolean) {
        if (isShow) {
            layoutOtherSetting.visibility = View.VISIBLE
            tvMoreSetting.text = "收起"
            imgMoreSetting.setImageResource(R.drawable.icon_arrow_up)
        } else {
            layoutOtherSetting.visibility = View.GONE
            tvMoreSetting.text = "更多设置"
            imgMoreSetting.setImageResource(R.drawable.icon_arrow_down)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10009 && data != null) {
            val crd = ParseCaptureUtils.parseData(data, requestCode)
            if (crd == null || TextUtils.isEmpty(crd.ip)) {
                FEToast.showMessage(getString(R.string.lbl_text_scan_failed))
                return
            }
            if (!TextUtils.isEmpty(crd.ip)) {
                etServerAddress.setText(crd.ip)
                etServerAddress.setSelection(crd.ip.length)
            }
            if (!TextUtils.isEmpty(crd.port)) {
                etServerPort.setText(crd.port)
                ivServerPortDel.visibility = View.GONE
            }
            isSSLOpen.isChecked = crd.isHttps
            tvVpnStatus.text = if (crd.isOpenVpn) "已开启" else "已关闭"
            showMoreSetting(crd.isHttps || crd.isOpenVpn)
            p.saveVpnSetting(crd.isOpenVpn, crd.vpnAddress, crd.vpnPort, crd.vpnName, crd.vpnPassword)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingChange(obj: SettingObj) {
        p.saveVpnSetting(obj.isVpn, obj.vpnServer, obj.vpnPort, obj.vpnName, obj.vpnPassword)
        tvVpnStatus.text = if (obj.isVpn) "已开启" else "已关闭"
    }

    @PermissionGranted(PermissionCode.CAMERA)
    open fun onCameraPermissionGranted() {
        if (DevicesUtil.isCameraCanUsed(this@ServerSettingActivity)) {
            startActivityForResult(Intent(this@ServerSettingActivity, CaptureActivity::class.java), 10009)
        }
    }

    override fun display(server: String, port: String, isHttps: Boolean, isVpn: Boolean) {
        if (!TextUtils.isEmpty(server)) {
            etServerAddress.setText(server)
            etServerAddress.setSelection(server.length)
        }
        if (!TextUtils.isEmpty(port)) {
            etServerPort.setText(port)
            ivServerPortDel.visibility = View.GONE
        }
        showMoreSetting(isHttps || isVpn)
        isSSLOpen.isChecked = isHttps
        tvVpnStatus.text = if (isVpn) "已开启" else "已关闭"
    }

    override fun saveSuccess() {
        startActivity(Intent(this@ServerSettingActivity, NewLoginActivity::class.java).apply {
            putExtra("isFirstLogin", true)
        })

        val okhttpClient = FEHttpClient.getInstance().okHttpClient
        if (isSSLOpen.isChecked) {
            HttpsURLConnection.setDefaultSSLSocketFactory(okhttpClient.sslSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier(okhttpClient.hostnameVerifier())
        }

        val cookieJar = okhttpClient.cookieJar()
        if (cookieJar != null && cookieJar is PersistentCookieJar) {
            cookieJar.clear()
        }

        EventBus.getDefault().post(EventObj(200))
        finish()
    }

    override fun saveError() {
        progressBar.visibility = View.GONE
        layoutNext.isEnabled = true
        tvNext.text = "下一步"
        FEToast.cancel()
        if (isFinishing) return
        FEMaterialDialog.Builder(this@ServerSettingActivity)
                .setMessage("服务器或端口输入错误")
                .setPositiveButton("重新输入", null)
                .setNegativeButton("扫码输入", { swipeQrCode() })
                .setCancelable(true)
                .build()
                .show()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        // unable back pressed.
    }
}

open class SimpleTextWatcher : TextWatcher {
    override fun afterTextChanged(s: Editable?) {}
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}