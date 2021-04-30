package cn.flyrise.feep.auth.login.setting

import cn.flyrise.feep.auth.EventObj
import cn.flyrise.feep.auth.server.setting.ServerSettingRepository
import org.greenrobot.eventbus.EventBus
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author 社会主义接班人
 * @since 2018-08-08 14:09
 */
class LoginSettingPresenter(val r: ServerSettingRepository, val v: LoginSettingView) {

    fun start() {
        v.display(r.sb.serverAddress ?: "", r.sb.serverPort ?: "", r.sb.isHttps, r.sb.isVPN)
    }

    fun saveVPN(isVpn: Boolean, vpnServer: String?, vpnPort: String?, vpnAccount: String?, vpnPassword: String?) {
        r.updateVpn(isVpn, vpnServer, vpnPort, vpnAccount, vpnPassword)
    }

    fun updateHttps(isHttps: Boolean) {
        r.updateHttps(isHttps)
    }

    fun save(server: String, port: String?, isHttps: Boolean) {
        r.updateHttps(isHttps)
        r.updateServer(server, port)

        if (r.sb.isVPN) {//开启VPN情况下，直接保存设置信息
            r.initHttpClient()
            r.saveUserSetting()
            v.saveSuccess()
            return
        }

        v.loading(true)
        val ob = if (r.sb.isHttps) {
            r.downloadHttpsCertificate().flatMap { r.tryCollectionToServer() }
        } else {
            r.tryCollectionToServer()
        }

        ob.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    v.loading(false)
                    if (it == 200) {
                        r.saveUserSetting()
                        v.saveSuccess()
                    } else {
                        v.saveError()
                    }
                }, {
                    v.loading(false)
                    v.saveError()
                })
    }

    fun saveSetting() {
        r.saveUserSetting()
        EventBus.getDefault().post(EventObj(200))
    }

    fun vpnSetting() = r.sb


}