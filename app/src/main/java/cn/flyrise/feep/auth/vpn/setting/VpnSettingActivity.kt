package cn.flyrise.feep.auth.vpn.setting

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import cn.flyrise.feep.R
import cn.flyrise.feep.auth.SettingObj
import cn.flyrise.feep.auth.server.setting.SimpleTextWatcher
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import kotlinx.android.synthetic.main.activity_vpn_setting.*
import org.greenrobot.eventbus.EventBus

/**
 * @author 社会主义接班人
 * @since 2018-08-07 15:19
 */
class VpnSettingActivity : NotTranslucentBarActivity() {

    private lateinit var toolBar: FEToolbar
    private val enableSave = { tv: TextView ->
        val isEnable = isVpnOpen.isChecked
                && !TextUtils.isEmpty(etVpnServer.text.toString()?.trim())
                && !TextUtils.isEmpty(etVpnServerPort.text.toString()?.trim())
                && !TextUtils.isEmpty(etVpnAccount.text.toString()?.trim())
                && !TextUtils.isEmpty(etVpnPassword.text.toString()?.trim())

        tv.isEnabled = isEnable
        tv.setTextColor(Color.parseColor(if (isEnable) "#28B9FF" else "#BFEAFF"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vpn_setting)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        this.toolBar = toolbar!!
        this.toolBar.title = "VPN"
        this.toolBar.rightText = "保存"
        this.toolBar.setRightTextClickListener {
            EventBus.getDefault().post(SettingObj().apply {
                isVpn = isVpnOpen.isChecked
                vpnServer = if (isVpn) etVpnServer.text.toString() else ""
                vpnPort = if (isVpn) etVpnServerPort.text.toString() else ""
                vpnName = if (isVpn) etVpnAccount.text.toString() else ""
                vpnPassword = if (isVpn) etVpnPassword.text.toString() else ""
            })
            FEToast.showMessage(getString(R.string.make_svae_success))
            finish()
        }
        this.toolBar.setNavigationOnClickListener {
            //            if (isVpnOpen.isChecked) {
            backDialog()
            return@setNavigationOnClickListener
//            } else {
//                EventBus.getDefault().post(SettingObj().apply {
//                    isVpn = false
//                    vpnServer = ""
//                    vpnPort = ""
//                    vpnName = ""
//                    vpnPassword = ""
//                })
//                finish()
//            }
        }
    }

    private fun backDialog() {
        FEMaterialDialog.Builder(this@VpnSettingActivity)
                .setMessage(getString(R.string.make_sure_back))
                .setPositiveButton(getString(R.string.make_svae_think), null)
                .setNegativeButton(getString(R.string.make_back), {
                    finish()
                })
                .build()
                .show()
    }

    override fun bindData() {
        val isVpn = intent.getBooleanExtra("isVpn", false)
        val vpnServer = intent.getStringExtra("vpnServer")
        val vpnPort = intent.getStringExtra("vpnPort")
        val vpnAccount = intent.getStringExtra("vpnAccount")
        val vpnPassword = intent.getStringExtra("vpnPassword")

        if (isVpn) {
            toolBar.rightTextView.visibility = View.VISIBLE
            layoutVpnSetting.visibility = View.VISIBLE
        } else {
            toolBar.rightTextView.visibility = View.GONE
            layoutVpnSetting.visibility = View.GONE
        }

        isVpnOpen.isChecked = isVpn
        etVpnServer.setText(vpnServer)
        if (!TextUtils.isEmpty(vpnServer)) {
            etVpnServer.setSelection(vpnServer.length)
        }

        etVpnServerPort.setText(vpnPort)
        etVpnAccount.setText(vpnAccount)
        etVpnPassword.setText(vpnPassword)

        ivVpnServerDel.setOnClickListener { etVpnServer.setText("") }
        ivVpnServerPortDel.setOnClickListener { etVpnServerPort.setText("") }
        ivVpnAccountDel.setOnClickListener { etVpnAccount.setText("") }
        ivVpnPasswordDel.setOnClickListener { etVpnPassword.setText("") }

        etVpnServer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etVpnServer.text.toString())) {
                ivVpnServerDel.visibility = View.VISIBLE
            } else {
                ivVpnServerDel.visibility = View.GONE
            }
        }
        etVpnServer.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivVpnServerDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
                enableSave(toolBar.rightTextView)
            }
        })

        etVpnServerPort.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etVpnServerPort.text.toString())) {
                ivVpnServerPortDel.visibility = View.VISIBLE
            } else {
                ivVpnServerPortDel.visibility = View.GONE
            }
        }
        etVpnServerPort.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivVpnServerPortDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
                enableSave(toolBar.rightTextView)
            }
        })

        etVpnAccount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etVpnAccount.text.toString())) {
                ivVpnAccountDel.visibility = View.VISIBLE
            } else {
                ivVpnAccountDel.visibility = View.GONE
            }
        }
        etVpnAccount.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivVpnAccountDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
                enableSave(toolBar.rightTextView)
            }
        })

        etVpnPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etVpnPassword.text.toString())) {
                ivVpnPasswordDel.visibility = View.VISIBLE
            } else {
                ivVpnPasswordDel.visibility = View.GONE
            }
        }
        etVpnPassword.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivVpnPasswordDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
                enableSave(toolBar.rightTextView)
            }
        })

        cbxPasswordVisibility.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etVpnPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
            } else {
                etVpnPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            }
        }
        isVpnOpen.setOnCheckedChangeListener { _, isChecked ->
            layoutVpnSetting.visibility = if (isChecked) View.VISIBLE else View.GONE
            toolBar.rightTextView.visibility = View.VISIBLE
            if (!isVpnOpen.isChecked) {

                toolBar.rightTextView.isEnabled = true
                toolBar.rightTextView.setTextColor(Color.parseColor("#28B9FF"))
            }else{
                enableSave(toolBar.rightTextView)
                toolBar.rightTextView.setTextColor(Color.parseColor("#BFEAFF"))
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backDialog()
        }
        return true
    }
}