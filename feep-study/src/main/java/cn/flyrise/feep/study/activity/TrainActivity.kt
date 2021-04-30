package cn.flyrise.feep.study.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.media.attachments.NetworkAttachmentListFragment
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment
import cn.flyrise.feep.study.entity.*
import cn.flyrise.feep.study.view.ElcSignVerifyManager
import cn.flyrise.study.R
import kotlinx.android.synthetic.main.activity_stu_train.*
import kotlinx.android.synthetic.main.stu_activity_train_sign.*
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

interface TrainDetailView {
    fun showTrainDetailInfor(data: TrainDetailResponse.DataBean)

    fun showTrainDetailError()

    fun showLoading()

    fun hideLoading()
}

class TrainActivity : BaseActivity(), TrainDetailView {
    private var mLoadingDialog: FELoadingDialog? = null
    private var startTime: Long = 0
    private var startRead: Boolean = false
    private var isFromVideo: Boolean = false
    private var attachmentId: String = ""
    private var detailData: TrainDetailResponse.DataBean? = null
    override fun showTrainDetailInfor(data: TrainDetailResponse.DataBean) {
        detailData = data
        tvTitle.text = "培训主题：" + if (data.theme == null) "暂无" else data.theme
        tvStartTime.text = "培训开始时间：" + if (data.sDate == null) "暂无" else data.sDate
        tvEndTime.text = "培训结束时间：" + if (data.eDate == null) "暂无" else data.eDate
        tvType.text = "培训分类：" + if (data.trainType == null) "暂无" else data.trainType
        tvTeacher.text = "培训老师：" + if (data.trainTeacher == null) "暂无" else data.trainTeacher
        tvPlace.text = "培训地点：" + if (data.place == null) "暂无" else data.place
        tvTrainMethod.text = "培训方式：" + if (data.trainingMethod == null) "暂无" else data.trainingMethod
        tvAssMethod.text = "考核方式：" + if (data.assMethod == null) "暂无" else data.assMethod

        val attachmentBeanList = mutableListOf<NetworkAttachment>()
        if (CommonUtil.nonEmptyList(data.video)) {
            for (i in 0 until data.video.size) {
                var networkAttachment = NetworkAttachment()
                networkAttachment.id = data.video[i].id
                val host = if (CoreZygote.getLoginUserServices() == null) "" else CoreZygote.getLoginUserServices().serverAddress
                if (!TextUtils.isEmpty(data.video[i].path)) {
                    if (data.video[i].path.contains("http")) {
                        networkAttachment.path = data.video[i].path
                    } else {
                        networkAttachment.path = host + data.video[i].path
                    }
                }
                networkAttachment.name = data.video[i].realname
                networkAttachment.type = getAttachmentType(data.video[i].type)
                attachmentBeanList.add(networkAttachment)
            }

            val fragment = NetworkAttachmentListFragment.newInstance(true, attachmentBeanList
            ) { attachment: Attachment? ->
                attachmentId = if (TextUtils.isEmpty(attachment?.id)) "" else attachment?.id!!
                startTime = System.currentTimeMillis()
                return@newInstance false
            }
            supportFragmentManager.beginTransaction()
                    .replace(R.id.stuLayoutAttachments, fragment)
                    .show(fragment)
                    .commitAllowingStateLoss()
        }

    }

    override fun showTrainDetailError() {
        FEToast.showMessage("详情加载失败")
    }

    override fun showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = FELoadingDialog.Builder(this).setCancelable(false).create()
        }
        mLoadingDialog?.show()
    }

    override fun hideLoading() {
        if (mLoadingDialog?.isShowing != null) {
            mLoadingDialog?.hide()
            mLoadingDialog = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stu_train)
        requestData()
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        toolbar?.title = "培训详情"
    }

    override fun bindView() {
        super.bindView()
        if (intent.getIntExtra("type", 0) == 2) {
            tvFinish.visibility = View.GONE
        }

        swipeRefreshLayout.setOnRefreshListener {
            requestData()
            Observable.timer(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _ -> swipeRefreshLayout?.isRefreshing = false }
        }

        tvFinish.setOnClickListener {
            verifyTrain()
        }
    }

    private fun requestData() {
        showLoading()
        val request = TrainingSignRequest()
        request.setType("5")
        request.setRecordId(intent.getStringExtra("taskId"))
        FEHttpClient.getInstance().post(request, object : ResponseCallback<TrainDetailResponse>() {
            override fun onCompleted(response: TrainDetailResponse?) {
                if (response != null && TextUtils.equals(response.errorCode, "0")) {
                    showTrainDetailInfor(response.data)
                } else {
                    showTrainDetailError()
                }
                hideLoading()
            }

            override fun onFailure(repository: RepositoryException?) {
                hideLoading()
            }
        })
    }

    private fun showSignDialog() {
        val verifyManager = ElcSignVerifyManager(this)
        verifyManager.setRequestType("SIGN")
        verifyManager.setServerId(detailData?.serverId)
        verifyManager.setMaster_key(detailData?.master_key)
        verifyManager.setTableName(detailData?.tableName)
        verifyManager.startVerify(object : ElcSignVerifyManager.ElcVerifyCallback {
            override fun onSuccess() {
                finishTrain()
            }

            override fun onFail(msg: String?) {
                FEToast.showMessage(msg)
            }
        })
    }

    private fun finishTrain() {
        showLoading()
        val request = TrainingSignRequest()
        request.setType("6")
        request.setRecordId(intent.getStringExtra("taskId"))
        request.setSignDate(DateUtil.formatTimeForHms(System.currentTimeMillis()))
        FEHttpClient.getInstance().post(request, object : ResponseCallback<TrainFinishResponse>() {
            override fun onCompleted(response: TrainFinishResponse?) {
                hideLoading()
                response?.result?.let {
                    FEToast.showMessage(it.mes)
                }
            }

            override fun onFailure(repository: RepositoryException?) {
                finish()
                hideLoading()
            }
        })
    }

    /**
     * 签到验证-电子签名验证-培训签到
     */
    private fun verifyTrain() {
        showLoading()
        val request = TrainingSignRequest()
        request.setType("15")
        request.setRecordId(intent.getStringExtra("taskId"))
        request.setSignDate(DateUtil.formatTimeForHms(System.currentTimeMillis()))
        FEHttpClient.getInstance().post(request, object : ResponseCallback<TrainFinishResponse>() {
            override fun onCompleted(response: TrainFinishResponse?) {
                hideLoading()
                response?.result?.let {
                    if (response.result?.code == 0) {
                        showSignDialog()
                    } else {
                        FEToast.showMessage(response.result.mes)
                    }
                }
            }

            override fun onFailure(repository: RepositoryException?) {
                finish()
                hideLoading()
            }
        })
    }

    fun goBack() {
        EventBus.getDefault().post(RefreshBean())
        finish()
    }

    private fun getAttachmentType(type: String?): String? {
        if (!TextUtils.isEmpty(type)) {
            if (type!!.contains("mp4") || type.contains("wav")
                    || type.contains("wmv") || type.contains("rmvb")
                    || type.contains("flv") || type.contains("MP4")) {
                return "video"
            }
        }
        return type
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10010 && data != null) {
            val time = data.getStringExtra("takeTime")
            val attachmentId = data.getStringExtra("dataId")
            recordTrain(time, attachmentId)
            isFromVideo = true
        }
    }

    private fun recordTrain(time: String, dataId: String) {
        showLoading()
        val request = TrainingSignRequest()
        request.setType("11")
        request.setSpeed(time)
        request.setPersonTaskId(intent.getStringExtra("taskId"))
        request.setDataId(dataId)
        request.setDataType(if (isFromVideo) "0" else "1")
        FEHttpClient.getInstance().post(request, object : ResponseCallback<TrainFinishResponse>() {
            override fun onCompleted(response: TrainFinishResponse?) {
                hideLoading()
                response?.result?.let {
                    FEToast.showMessage(it.mes)
                }

            }

            override fun onFailure(repository: RepositoryException?) {
                hideLoading()
                FEToast.showMessage("记录失败！")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (startRead && !isFromVideo && !TextUtils.isEmpty(attachmentId)) {
            recordTrain((System.currentTimeMillis() - startTime).toString(), attachmentId)
        }
    }

    override fun onPause() {
        super.onPause()
        startRead = true
    }


}
