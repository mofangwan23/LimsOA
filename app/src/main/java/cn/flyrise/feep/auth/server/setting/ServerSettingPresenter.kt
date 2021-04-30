package cn.flyrise.feep.auth.server.setting

import cn.flyrise.android.shared.bean.UserBean
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author 社会主义接班人
 * @since 2018-08-08 10:33
 */
class ServerSettingPresenter(val r: ServerSettingRepository, val v: ServerSettingView) {

    fun start() {
        v.display(r.sb.serverAddress ?: "", r.sb.serverPort ?: "", r.sb.isHttps, r.sb.isVPN)
    }

    fun saveVpnSetting(isVpn: Boolean, vpnServer: String?, vpnPort: String?, vpnAccount: String?, vpnPassword: String?) {
        r.updateVpn(isVpn, vpnServer, vpnPort, vpnAccount, vpnPassword)
    }

    fun next(server: String, port: String, isHttps: Boolean) {
        r.updateHttps(isHttps)
        r.updateServer(server, port)

        if (r.sb.isVPN) {
            r.initHttpClient()
            r.saveUserSetting()
            v.saveSuccess()
            return
        }

        val ob = if (r.sb.isHttps) {
            r.downloadHttpsCertificate().flatMap { r.tryCollectionToServer() }
        } else {
            r.tryCollectionToServer()
        }

        ob.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it == 200) {
                        r.saveUserSetting()
                        v.saveSuccess()
                    } else {
                        v.saveError()
                    }
                }, {
                    v.saveError()
                })
    }

    fun vpnSetting() = r.sb

}