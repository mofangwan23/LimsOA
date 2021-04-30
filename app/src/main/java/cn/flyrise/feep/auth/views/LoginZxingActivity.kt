package cn.flyrise.feep.auth.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.PreferencesUtils
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.request.ResponseContent

/**
 * 新建：陈冕;
 *日期： 2018-6-15-14:33.
 * 扫码登录pc端
 */
class LoginZxingActivity : BaseActivity() {

    companion object {
        fun startActivity(activity: Activity, data: String) {
            val intent = Intent(activity, LoginZxingActivity::class.java)
            intent.putExtra("url", data)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_zxing_layout)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        toolbar!!.title = getString(R.string.login_zxing_title)
    }

    override fun bindListener() {
        super.bindListener()
        findViewById<TextView>(R.id.cancel_login)!!.setOnClickListener { doCancel() }//取消登录

        findViewById<TextView>(R.id.confirm_login)!!.setOnClickListener {
            doRequest()
        }
    }

    private fun doRequest() {
        val data = intent.getStringExtra("url")
        var url = ""
        if (data.contains("https://") || data.contains("http://")) {
            url = data
        } else {
            url = SpUtil.get(PreferencesUtils.USER_IP, "http://oa.flyrise.cn:8089")!! + data
        }
        LoadingHint.show(this)
        FEHttpClient.getInstance().post(url, null, object : ResponseCallback<ResponseContent>() {
            override fun onCompleted(response: ResponseContent) {
                LoadingHint.hide()
                if (response.errorCode == "0") {
                    FEToast.showMessage("登录成功")
                    finish()
                } else {
                    onFailure(null)
                }
            }

            override fun onFailure(repositoryException: RepositoryException?) {
                super.onFailure(repositoryException)
                FEToast.showMessage("登录失败")
                LoadingHint.hide()
                finish()
            }
        })
    }

    private fun doCancel() {
        var split = intent.getStringExtra("url").split("&")
        var uuid: String? = ""
        if (split!!.size > 0) {
            uuid = split[1]
        }
        var url = SpUtil.get(PreferencesUtils.USER_IP, "") + "/oauth?type=scan_cancel&" + uuid
        FEHttpClient.getInstance().post(url, null, object : ResponseCallback<ResponseContent>() {
            override fun onCompleted(response: ResponseContent) {
                LoadingHint.hide()
                FEToast.showMessage("取消成功")
                finish()
            }

            override fun onFailure(repositoryException: RepositoryException?) {
                super.onFailure(repositoryException)
                FEToast.showMessage("取消失败")
                LoadingHint.hide()
                finish()
            }
        })

    }

}