package cn.flyrise.feep.study.presenter

import android.text.TextUtils
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.study.activity.QuestionView
import cn.flyrise.feep.study.entity.GetQuestionRequest
import cn.flyrise.feep.study.entity.GetQuestionResponse
import cn.flyrise.feep.study.entity.QuestionSubmitRequest
import cn.flyrise.feep.study.entity.TrainingSignRequest
import cn.flyrise.feep.study.respository.ExamDataRepository
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class QuestionDetailPresenter(val view: QuestionView,val respository: ExamDataRepository) {

    fun getQuestionDetail(id: String?,trainTaskId: String?,infoId:String?){
//        view.showLoading()
        var request = TrainingSignRequest()
        request.setRecordId(id)
        request.setRequestType("1")
        request.setType("2")
        request.infoid = infoId
        request.setTrainTaskId(trainTaskId)
        FEHttpClient.getInstance().post(request, object : ResponseCallback<GetQuestionResponse>() {
            override fun onCompleted(response: GetQuestionResponse?) {
//                view.hideLoading()
                if (TextUtils.equals("0",response?.errorCode)){
                    view.showQuestionInfo(response!!)
                }else {
                    view.showQuestionsError()
                }
            }

            override fun onFailure(repositoryException: RepositoryException) {
                view.showQuestionsError()
//                view.hideLoading()
            }
        })
    }

    fun submitExam(id: String?,sDate: String?, eDate:String?, trainTaskId:String?, score:String?, ispass: String?, qIds:MutableList<String>?,
    statusIds:MutableList<String>?,qTypes:MutableList<String>?,userAnswers: MutableList<String>?,scores:MutableList<String>?){
//        view.showLoading()
        var request = QuestionSubmitRequest()
        request.setRecordId(id)
        request.setRequestType("1")
        request.setType("3")
        request.setSdate(sDate)
        request.setEdate(eDate)
        request.setTrainTaskId(trainTaskId)
        request.setScore(score)
        request.setIspass(ispass)

        request.setuId(CoreZygote.getLoginUserServices().userId)
        request.setpId(id)
        request.setqId(qIds)
        request.setStatus(statusIds)
        request.setqType(qTypes)
        request.setUser_answer(userAnswers)
        request.setScores(scores)

        respository.requestExamSubmit(request).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {  }
                .subscribe({
                    view.submitAnswerSuccess()
//                    view.hideLoading()
                }, {
                    it.printStackTrace()
                    view.submitAnswerFaile()
//                    view.hideLoading()
                })
    }

}