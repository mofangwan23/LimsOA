package cn.flyrise.feep.study.presenter

import android.text.TextUtils
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.study.activity.QuestionResultView
import cn.flyrise.feep.study.entity.GetQuestionResponse
import cn.flyrise.feep.study.entity.TrainingSignRequest

class QuestionResultPresenter(val view: QuestionResultView) {

    fun getQuestionResultDetail(id: String?,trainTaskId:String?,infoId:String?){
        var request = TrainingSignRequest()
        request.setRecordId(id)
        request.setType("9")
        request.setpId(id)
        request.infoid = infoId
        request.setTrainTaskId(trainTaskId)
        request.setuId(CoreZygote.getLoginUserServices().userId)
        FEHttpClient.getInstance().post(request, object : ResponseCallback<GetQuestionResponse>() {
            override fun onCompleted(response: GetQuestionResponse?) {
                if (TextUtils.equals("0",response?.errorCode)){
                    view.showQuestionResult(response!!)
                }
            }

            override fun onFailure(repositoryException: RepositoryException) {
                view.showError()
            }
        })
    }
}