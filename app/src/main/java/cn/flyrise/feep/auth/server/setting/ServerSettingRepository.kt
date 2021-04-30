package cn.flyrise.feep.auth.server.setting

import cn.flyrise.android.shared.bean.UserBean
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils
import rx.Observable
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author 社会主义接班人
 * @since 2018-08-08 10:33
 */
class ServerSettingRepository(val sb: UserBean) {

    // 下载 Https 证书
    fun downloadHttpsCertificate(): Observable<Int> {
        return Observable.unsafeCreate {
            val keyStoreFilePath = "${CoreZygote.getPathServices().getKeyStoreDirPath()}${File.separator}FEKey.keystore"
            val keyStoreFile = File(keyStoreFilePath).apply {
                if (exists()) delete()
            }

            try {
                val downloadURL = "http://${sb.serverAddress}:${sb.serverPort}${CoreZygote.getPathServices().keyStoreUrl}"
                val url = URL(downloadURL)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 5000
                urlConnection.readTimeout = 10000
                urlConnection.setRequestProperty("User-agent", CoreZygote.getUserAgent())
                if (urlConnection.responseCode == 200) {
                    val inputStream = urlConnection.inputStream
                    val bos = BufferedOutputStream(FileOutputStream(keyStoreFile) as OutputStream?)
                    val buf = ByteArray(2048)
                    var len = inputStream.read(buf)
                    while (len != -1) {
                        bos.write(buf, 0, len)
                        len = inputStream.read(buf)
                    }
                    inputStream.close()
                    bos.close()
                }
                it.onNext(200)
            } catch (exp: Exception) {
                exp.printStackTrace()
                it.onNext(500)
            } finally {
                it.onCompleted()
            }
        }
    }

    fun updateServer(server: String, port: String?) {
        sb.serverAddress = server
        sb.serverPort = port
    }

    fun updateHttps(isHttps: Boolean) {
        sb.isHttps = isHttps
    }

    fun updateVpn(isVpn: Boolean, vpnServer: String?, vpnPort: String?, vpnAccount: String?, vpnPassword: String?) {
        sb.isVPN = isVpn
        sb.vpnAddress = vpnServer
        sb.vpnPort = vpnPort
        sb.vpnUsername = vpnAccount
        sb.vpnPassword = vpnPassword
    }

    // 尝试连接服务器，验证当前睿智用户输入的服务器、端口是否有误
    fun tryCollectionToServer(): Observable<Int> {
        return Observable.unsafeCreate {
            initHttpClient()
            FEHttpClient.getInstance().post(null, null, object : ResponseCallback<ResponseContent>() {
                override fun onCompleted(t: ResponseContent?) {
                    it.onNext(200)
                }

                override fun onFailure(repository: RepositoryException?) {
                    FEToast.cancel()
                    it.onNext(500)
                }
            })
        }
    }

    // 保存睿智用户输入的信息
    fun saveUserSetting() {
        UserInfoTableUtils.insert(sb)
    }

    fun initHttpClient() {
        FEHttpClient.Builder(CoreZygote.getContext()).address(sb.serverAddress).port(sb.serverPort).isHttps(sb.isHttps)
                .keyStore(CoreZygote.getPathServices().keyStoreFile).build()
    }


}