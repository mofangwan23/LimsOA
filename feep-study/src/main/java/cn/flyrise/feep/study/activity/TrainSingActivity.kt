package cn.flyrise.feep.study.activity

import android.os.Bundle
import android.text.TextUtils
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.study.entity.*
import cn.flyrise.study.R
import kotlinx.android.synthetic.main.stu_activity_train_sign.*

class TrainSingActivity: BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stu_activity_train_sign)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        toolbar?.title = "签到"
    }

    override fun bindView() {
        super.bindView()
        signStatus.text = "正在为您签到中..."
    }

    override fun bindData() {
        super.bindData()
        val request = TrainingSignRequest()
        request.setType("12")
        request.setRecordId(intent.getStringExtra("code"))
        request.setuId(CoreZygote.getLoginUserServices().userId)
        FEHttpClient.getInstance().post(request, object : ResponseCallback<TrainFinishResponse>() {
            override fun onCompleted(response: TrainFinishResponse?) {
                response?.result?.let {
                    signStatus.text = response.result?.mes
                }

            }
            override fun onFailure(repositoryException: RepositoryException) {
                signStatus.text = "签到失败，请重新扫码～"
            }
        })
    }

}