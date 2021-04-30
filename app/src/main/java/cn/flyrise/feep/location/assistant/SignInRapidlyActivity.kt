package cn.flyrise.feep.location.assistant

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.location.Sign
import cn.flyrise.feep.location.dialog.SignInLoadingHint
import cn.flyrise.feep.location.dialog.SignInResultDialog
import cn.flyrise.feep.location.event.EventPhotographFinish
import cn.flyrise.feep.location.event.EventPhotographSignSuccess
import cn.flyrise.feep.location.presenter.SignInRapidlyPresenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 新建：陈冕;
 *日期： 2018-8-7-10:09.
 * 急速打卡
 */
@SuppressLint("Registered")
class SignInRapidlyActivity : SignInPermissionsActivity() {

    private var mPresenter: SignInRapidlyPresenter? = null
    private var mDialog: SignInResultDialog? = null
    private var mLoadingHint: SignInLoadingHint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        mLoadingHint = SignInLoadingHint()
        mLoadingHint?.setOnDismissListener {
            FELog.i("-->>>rapidly-loading-dismiss")
        }
        mLoadingHint?.setOnKeyDownListener { finish() }
        super.onCreate(savedInstanceState)
    }

    override fun permissionsSuccess() {
        FELog.i("-->>>rapidly-开始")
        mPresenter = SignInRapidlyPresenter(this)
        mPresenter?.requestWQT()
    }

    override fun networkError() {
        hideLoading()
        startFailure(getString(R.string.lbl_retry_network_connection), Sign.error.network)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventPhotoResult(signSuccess: EventPhotographSignSuccess) {
        mPresenter?.photoRequestHistory(signSuccess.isTakePhotoError, signSuccess.signSuccess)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventPhotoFinish(signSuccess: EventPhotographFinish) {
        finish()
    }

    fun startSuccess(time: String, isLeader: Boolean, address: String) {
        mDialog = SignInResultDialog().setContext(this).setListener { if (it) mPresenter?.requestWQT() else finish() }
                .setTime(time).setSuccessText(address).setLeader(isLeader)
        mDialog?.show(supportFragmentManager, "signSuccess")
    }


    fun startFailure(text: String, errorType: Int) {
        mDialog = SignInResultDialog().setContext(this).setListener { if (it) mPresenter?.requestWQT() else finish() }
                .setError(text).setErrorType(errorType)
        mDialog?.show(supportFragmentManager, "signError")
    }

    fun showLoading() {
        mLoadingHint?.showDialog(this, false)
    }

    fun hideLoading() {
        mLoadingHint?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        FELog.i("-->>>rapidly关闭")
        mPresenter?.onDestroy()
        mPresenter = null
    }

    companion object {

        fun startSignInRapidly(context: Context) {
            val intent = Intent(context, SignInRapidlyActivity::class.java)
            context.startActivity(intent)
        }
    }
}