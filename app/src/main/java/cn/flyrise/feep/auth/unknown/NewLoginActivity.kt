package cn.flyrise.feep.auth.unknown

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import cn.flyrise.feep.FEMainActivity
import cn.flyrise.feep.R
import cn.flyrise.feep.auth.EventObj
import cn.flyrise.feep.auth.SettingObj
import cn.flyrise.feep.auth.VpnAuthPresenter
import cn.flyrise.feep.auth.login.setting.LoginSettingActivity
import cn.flyrise.feep.auth.server.setting.ServerSettingActivity
import cn.flyrise.feep.auth.server.setting.SimpleTextWatcher
import cn.flyrise.feep.auth.views.BaseAuthActivity
import cn.flyrise.feep.auth.views.BaseThreeLoginActivity.EXTRA_CLEAR_SECURITY_SETTING
import cn.flyrise.feep.auth.vpn.setting.VpnSettingActivity
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.common.utils.PreferencesUtils
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils
import cn.flyrise.feep.more.PrivacyActivity
import cn.flyrise.feep.utils.FEUpdateVersionUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_sb_login.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author 社会主义接班人
 * @since 2018-08-07 16:25
 */
class NewLoginActivity : BaseAuthActivity() {

    private val enableLogin = fun() {
        if (!TextUtils.isEmpty(etAccount.text.toString()) && !TextUtils.isEmpty(etPassword.text.toString())) {
            layoutLogin.isEnabled = true
            layoutLogin.setBackgroundResource(R.drawable.nms_bg_blue_btn_round_enable)
        } else {
            layout_login.isEnabled = false
            layoutLogin.setBackgroundResource(R.drawable.nms_bg_blue_btn_round_unable)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_sb_login)
    }

    override fun bindView() {

        val clearSecuritySetting = intent.getBooleanExtra(EXTRA_CLEAR_SECURITY_SETTING, true)
        if (clearSecuritySetting) {
            SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false)
            SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false)
        }

        val isForgetPassword = intent.getBooleanExtra(BaseAuthActivity.EXTRA_FORGET_PASSWORD, false)
        mAuthPresenter = VpnAuthPresenter(this, isForgetPassword)
        mAuthPresenter.initAuthPresenter(mUserBean)
        if (mUserBean != null && !mUserBean.isAutoLogin) {
            mAuthPresenter.tryToShowUserKickDialog()
        }

        val isFirstLogin = intent.getBooleanExtra("isFirstLogin", false)
        ivLoginBack.visibility = if (isFirstLogin) View.VISIBLE else View.GONE

        etAccount.setText(mUserBean.loginName)
        etAccount.setSelection(mUserBean.loginName?.length ?: 0)

        if (mUserBean.isSavePassword) {
            val password = mUserBean.password
            etPassword.setText(password)
            etPassword.setSelection(password?.length ?: 0)
        }

        cbxRememberPwd.isChecked = mUserBean.isSavePassword
        cbxAutoLogin.isChecked = mUserBean.isAutoLogin

        val url = SpUtil.get(BaseAuthActivity.sExtraLogoUrl, "")
        if (!TextUtils.isEmpty(url)) {
            privacyLayout.setBackgroundColor(Color.parseColor("#50fafafa"))
            ivLoginBackground.visibility = View.VISIBLE
            Glide.with(context).load(url).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    loadImageFail(true)
                    return false;
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    loadImageFail(false)
                    return false;
                }

            }).into(ivLoginBackground)
        }
        privacyImg.isSelected = true
    }

    override fun bindListener() {
        ivLoginBack.setOnClickListener {
            isShowToast = false
            startActivity(Intent(this@NewLoginActivity, ServerSettingActivity::class.java))
        }
        ivNetworkSetting.setOnClickListener {
            isShowToast = false
            startActivity(Intent(this@NewLoginActivity, LoginSettingActivity::class.java))
        }
        ivAccountDel.setOnClickListener { etAccount.setText("") }
        ivPasswordDel.setOnClickListener { etPassword.setText("") }
        etAccount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etAccount.text.toString())) {
                ivAccountDel.visibility = View.VISIBLE
            } else {
                ivAccountDel.visibility = View.GONE
            }
        }
        etAccount.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivAccountDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
                etPassword.setText("")
                enableLogin()
            }
        })
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !TextUtils.isEmpty(etPassword.text.toString())) {
                ivPasswordDel.visibility = View.VISIBLE
            } else {
                ivPasswordDel.visibility = View.GONE
            }
        }
        etPassword.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                ivPasswordDel.visibility = if (s?.length ?: 0 > 0) View.VISIBLE else View.GONE
                enableLogin()
            }
        })
        etPassword.setOnEditorActionListener({ v, actionId, event ->
            val imm = v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isActive) {
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0)
            }
            startLogin()
            false
        })

        cbxPasswordVisibility.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        cbxRememberPwd.setOnCheckedChangeListener { _, isChecked ->
            (mAuthPresenter as VpnAuthPresenter).updateRememberPwd(isChecked)
            if (!isChecked && cbxAutoLogin.isChecked) {
                cbxAutoLogin.isChecked = false
            }
        }

        cbxAutoLogin.setOnCheckedChangeListener { _, isChecked ->
            (mAuthPresenter as VpnAuthPresenter).updateAutoLogin(isChecked)
            if (isChecked) {
                cbxRememberPwd.isChecked = true
            }
        }

        enableLogin()
        layoutLogin.setOnClickListener {
            viewMaskLayer.visibility = View.VISIBLE
            startLogin()
        }
        layout_login.setOnClickListener {
            val imm = layout_login.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(layout_login.getApplicationWindowToken(), 0)
        }

//        privacyImg.setOnClickListener {
//            privacyImg.isSelected = !privacyImg.isSelected
//            viewMaskLayer.visibility = if (privacyImg.isSelected) View.GONE else View.VISIBLE
//            layoutLogin.isSelected = !privacyImg.isSelected
//        }

        privacyTv.setOnClickListener {
            isShowToast = false
            startActivity(Intent(this, PrivacyActivity::class.java))
        }
    }

    @Subscribe
    fun onNetworkSettingChange(obj: EventObj) {
        if (obj.code == 200) {
            mAuthPresenter.notifyUserInfoChange()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSettingChange(obj: SettingObj) {
        (mAuthPresenter as VpnAuthPresenter).userBean.apply {
            isVPN = obj.isVpn
            vpnAddress = obj.vpnServer
            vpnPort = obj.vpnPort
            vpnUsername = obj.vpnName
            vpnPassword = obj.vpnPassword
        }
        mUserBean.isVPN = obj.isVpn
        mUserBean.vpnAddress = obj.vpnServer
        mUserBean.vpnPort = obj.vpnPort
        mUserBean.vpnPassword = obj.vpnPassword
        UserInfoTableUtils.insert(mUserBean)
    }

    private fun startLogin() {
        DevicesUtil.tryCloseKeyboard(this)
        val loginName = etAccount.getText().toString()
        val password = etPassword.getText().toString()
        mAuthPresenter.startLogin(loginName, password)
    }

    override fun showLoading() {
        layoutLogin.isEnabled = false
        progressBar.visibility = View.VISIBLE
        tvLogin.text = "正在加载"
    }

    override fun hideLoading() {
        enableLogin()
        progressBar.visibility = View.GONE
        tvLogin.text = "登录"
        viewMaskLayer.visibility = View.GONE
    }

    override fun loginSuccess() {
        isShowToast = false
        val intent = Intent(this, FEMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun loginError(errorMessage: String?) {
        viewMaskLayer.visibility = View.GONE
        FEToast.cancel()
        super.loginError(errorMessage)
        enableLogin()
        progressBar.visibility = View.GONE
        tvLogin.text = "登录"
    }

    override fun interceptVpnErrorMessage(errorMessage: String?): Boolean {
        viewMaskLayer.visibility = View.GONE
        FEToast.cancel()
        enableLogin()
        progressBar.visibility = View.GONE
        tvLogin.text = "登录"
        FEMaterialDialog.Builder(this)
                .setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton("去设置") {
                    isShowToast = false
                    startActivity(Intent(this@NewLoginActivity, VpnSettingActivity::class.java).apply {
                        val sb = (mAuthPresenter as VpnAuthPresenter).userBean
                        putExtra("isVpn", sb.isVPN)
                        putExtra("vpnServer", sb.vpnAddress)
                        putExtra("vpnPort", sb.vpnPort)
                        putExtra("vpnAccount", sb.vpnUsername)
                        putExtra("vpnPassword", sb.vpnPassword)
                    })
                }
                .build()
                .show()
        return true
    }

    override fun toUpdate(errorMessage: String?) {
        viewMaskLayer.visibility = View.GONE
        FEToast.cancel()
        enableLogin()
        progressBar.visibility = View.GONE
        tvLogin.text = "登录"
        FEMaterialDialog.Builder(this)
                .setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton("去更新") {
                    FEUpdateVersionUtils(this, FEUpdateVersionUtils.IgnoreVersionListener {

                    }).showUpdateVersionDialog()
                }
                .build()
                .show()
    }

    override fun resetPassword() {
        etPassword?.setText("")
    }

    override fun onResume() {
        super.onResume()
        isShowToast = true
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun vpnError() {
        super.vpnError()
        viewMaskLayer.visibility = View.GONE
    }

    fun loadImageFail(fail: Boolean) {
        view_login_account_head.visibility = if (fail) View.GONE else View.VISIBLE
        view_login_account_tail.visibility = if (fail) View.GONE else View.VISIBLE
        view_login_pwd_head.visibility = if (fail) View.GONE else View.VISIBLE
        view_login_pwd_tail.visibility = if (fail) View.GONE else View.VISIBLE
        view_line_account.visibility = if (fail) View.VISIBLE else View.GONE
        view_line_pwd.visibility = if (fail) View.VISIBLE else View.GONE
        if (fail) {
            etAccount.setHintTextColor(Color.parseColor("#FFCFD0D1"))
            etPassword.setHintTextColor(Color.parseColor("#FFCFD0D1"))
            cbxRememberPwd.setHintTextColor(Color.parseColor("#FF8B8C8C"))
            cbxAutoLogin.setHintTextColor(Color.parseColor("#FF8B8C8C"))
        } else {
            etAccount.setHintTextColor(Color.parseColor("#8017191A"))
            etPassword.setHintTextColor(Color.parseColor("#8017191A"))
            cbxRememberPwd.setHintTextColor(Color.parseColor("#8017191A"))
            cbxAutoLogin.setHintTextColor(Color.parseColor("#8017191A"))
        }

    }

}