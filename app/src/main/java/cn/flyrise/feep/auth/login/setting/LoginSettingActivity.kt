package cn.flyrise.feep.auth.login.setting

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.R
import cn.flyrise.feep.auth.SettingObj
import cn.flyrise.feep.auth.server.setting.ServerSettingRepository
import cn.flyrise.feep.auth.server.setting.SimpleTextWatcher
import cn.flyrise.feep.auth.views.CaptureActivity
import cn.flyrise.feep.auth.vpn.setting.VpnSettingActivity
import cn.flyrise.feep.core.base.component.BaseActivity
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
import cn.flyrise.feep.utils.ParseCaptureUtils
import kotlinx.android.synthetic.main.activity_login_setting.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.net.ssl.HttpsURLConnection

/**
 * @author 社会主义接班人
 * @since 2018-08-07 15:15
 * 为什么要搞这么多包
 */
interface LoginSettingView {
    fun loading(isDisplay: Boolean)
    fun display(s: String, p: String, iH: Boolean, iV: Boolean)
    fun saveSuccess()
    fun saveError()
}

class LoginSettingActivity : BaseActivity(), LoginSettingView {

    private lateinit var toolBar: FEToolbar
    private var isEditMode: Boolean = false
    private lateinit var p: LoginSettingPresenter

    private val swipeQrCode = fun() {
        FePermissions.with(this@LoginSettingActivity)
                .permissions(arrayOf(Manifest.permission.CAMERA))
                .rationaleMessage(resources.getString(R.string.permission_rationale_camera))
                .requestCode(PermissionCode.CAMERA)
                .request()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        p = LoginSettingPresenter(ServerSettingRepository(UserInfoTableUtils.find()), this)
        setContentView(R.layout.activity_login_setting)
        p.start()
    }

    override fun toolBar(toolbar: FEToolbar?) {
        this.toolBar = toolbar!!.apply {
            title = getString(R.string.login_setting_title)
            setNavigationOnClickListener { checkExitEditTextState() }
            setRightTextClickListener {
                if (isEditMode) {
                    val server = etServer.text.toString()
                    val port = etServerPort.text.toString()
                    if (TextUtils.isEmpty(server)) {
                        FEToast.showMessage(getString(R.string.login_setting_service_ip))
                        return@setRightTextClickListener
                    }

//                    val fPort = if (TextUtils.isEmpty(port)) "80" else port
                    p.save(server, port, isSSLOpen.isChecked)
                } else {
                    val server = etServer.text.toString()
                    if (!TextUtils.isEmpty(server)) {
                        etServer.setSelection(server.length)
                    }
                }
                isEditMode = !isEditMode
                changeEditMode(isEditMode)
            }
            rightTextView.apply {
                setText(getString(R.string.login_setting_edit))
                visibility = View.VISIBLE
                setTextColor(Color.parseColor("#FFFFFF"))
            }
        }
    }

    private fun checkExitEditTextState() = if (isEditMode) {
        FEMaterialDialog.Builder(this@LoginSettingActivity)
                .setMessage(getString(R.string.make_sure_back))
                .setPositiveButton(getString(R.string.login_setting_no_exit), null)
                .setNegativeButton(getString(R.string.confirm), { finish() })
                .build()
                .show()
    } else saveSetting()

    private fun saveSetting() {
        p.saveSetting()
        finish()
    }

    override fun bindData() {
        ivQrCodeSwipe.setOnClickListener { swipeQrCode() }
        isSSLOpen.setOnCheckedChangeListener { _, isChecked -> p.updateHttps(isChecked) }
        vpnSettingLayout.setOnClickListener {
            startActivity(Intent(this@LoginSettingActivity, VpnSettingActivity::class.java).apply {
                val sb = p.vpnSetting()
                putExtra("isVpn", sb.isVPN)
                putExtra("vpnServer", sb.vpnAddress)
                putExtra("vpnPort", sb.vpnPort)
                putExtra("vpnAccount", sb.vpnUsername)
                putExtra("vpnPassword", sb.vpnPassword)
            })
        }

        ivServerDel.setOnClickListener { etServer.setText("") }
        ivServerPortDel.setOnClickListener { etServerPort.setText("") }
        etServer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etServer.text.toString()) && isEditMode) {
                ivServerDel.visibility = View.VISIBLE
            } else {
                ivServerDel.visibility = View.GONE
            }
        }
        etServerPort.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etServerPort.text.toString()) && isEditMode) {
                ivServerPortDel.visibility = View.VISIBLE
            } else {
                ivServerPortDel.visibility = View.GONE
            }
        }
        etServer.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivServerDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
            }
        })
        etServerPort.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivServerPortDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
            }
        })
        ivServerDel.setOnClickListener { etServer.setText("") }
        ivServerPortDel.setOnClickListener { etServerPort.setText("") }
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
                etServer.setText(crd.ip)
                etServer.clearFocus()
                etServer.setSelection(crd.ip.length)
            }
            if (!TextUtils.isEmpty(crd.port)) {
                etServerPort.setText(crd.port)
                etServerPort.clearFocus()
                etServerPort.setSelection(crd.port.length)
                ivServerPortDel.visibility = View.GONE
            }

            isSSLOpen.isChecked = crd.isHttps
            tvVpnStatus.text = getString(if (crd.isOpenVpn) R.string.login_setting_open else R.string.login_setting_off)
            p.saveVPN(crd.isOpenVpn, crd.vpnAddress, crd.vpnPort, crd.vpnName, crd.vpnPassword)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    @PermissionGranted(PermissionCode.CAMERA)
    open fun onCameraPermissionGranted() {
        if (DevicesUtil.isCameraCanUsed(this@LoginSettingActivity)) {
            startActivityForResult(Intent(this@LoginSettingActivity, CaptureActivity::class.java), 10009)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingChange(obj: SettingObj) {
        p.saveVPN(obj.isVpn, obj.vpnServer, obj.vpnPort, obj.vpnName, obj.vpnPassword)
        tvVpnStatus.text = getString(if (obj.isVpn) R.string.login_setting_open else R.string.login_setting_off)
    }

    override fun display(server: String, port: String, isHttps: Boolean, isVpn: Boolean) {
        isSSLOpen.isChecked = isHttps
        tvVpnStatus.text = getString(if (isVpn) R.string.login_setting_open else R.string.login_setting_off)

        layoutServerSetting.visibility = View.VISIBLE
        etServer.setText(server)
        etServerPort.setText(port)
        isEditMode = TextUtils.isEmpty(server) && TextUtils.isEmpty(port)
        changeEditMode(isEditMode)
    }

    private fun changeEditMode(isEdit: Boolean) {
        if (isEdit) {
            toolBar.rightTextView.text = getString(R.string.login_setting_save)
            ivTransparent.visibility = View.GONE
            etServer.isCursorVisible = true
            etServerPort.isCursorVisible = true
            if (!TextUtils.isEmpty(etServer.text.toString())) {
                ivServerDel.visibility = View.VISIBLE
            }
            ivQrCodeSwipe.visibility = View.VISIBLE
            ivQrCodeSwipe.setImageResource(R.mipmap.core_icon_zxing)
        } else {
            etServer.isCursorVisible = false
            etServerPort.isCursorVisible = false
            ivServerDel.visibility = View.GONE
            ivServerPortDel.visibility = View.GONE
            toolBar.rightTextView.text = getString(R.string.login_setting_edit)
            ivTransparent.visibility = View.VISIBLE
            ivQrCodeSwipe.visibility = View.GONE
            ivQrCodeSwipe.setImageResource(R.mipmap.core_icon_zxing_unable)
        }
    }

    override fun loading(isDisplay: Boolean) {
        if (isDisplay) {
            LoadingHint.show(this@LoginSettingActivity)
        } else {
            LoadingHint.hide()
        }
    }

    override fun saveSuccess() {
        isEditMode = false
        changeEditMode(false)

        val okhttpClient = FEHttpClient.getInstance().okHttpClient
        if (isSSLOpen.isChecked) {
            HttpsURLConnection.setDefaultSSLSocketFactory(okhttpClient.sslSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier(okhttpClient.hostnameVerifier())
        }

        val cookieJar = okhttpClient.cookieJar()
        if (cookieJar != null && cookieJar is PersistentCookieJar) {
            cookieJar.clear()
        }

        FEToast.showMessage(getString(R.string.login_setting_save_success))
        p.saveSetting()
        finish()
    }

    override fun saveError() {
        if (this.isFinishing) return
        isEditMode = true
        changeEditMode(true)
        FEMaterialDialog.Builder(this@LoginSettingActivity)
                .setMessage(getString(R.string.login_setting_service_ip_error))
                .setPositiveButton(getString(R.string.login_setting_reset_ip), null)
                .setNegativeButton(getString(R.string.login_setting_zxing_input), { swipeQrCode() })
                .setCancelable(true)
                .build()
                .show()
    }

//    override fun onBackPressed() {
//        p.saveSetting()
//        super.onBackPressed()
//    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            checkExitEditTextState()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


}