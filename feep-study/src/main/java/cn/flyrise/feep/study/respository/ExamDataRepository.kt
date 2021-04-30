package cn.flyrise.feep.study.respository

import android.text.TextUtils
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.study.entity.ExamSubmitResponse
import cn.flyrise.feep.study.entity.QuestionSubmitRequest
import cn.flyrise.feep.study.entity.TrainingSignRequest
import cn.flyrise.feep.study.entity.TrainingSignResponse
import rx.Observable

class ExamDataRepository {
    /**
     * 请求我的试卷列表接口
     */
    fun requestExamMyPaperList(request: TrainingSignRequest): Observable<TrainingSignResponse> {
        return Observable.create {f: rx.Subscriber<in TrainingSignResponse> ->
            FEHttpClient.getInstance().post(request, object : ResponseCallback<TrainingSignResponse>() {
                override fun onCompleted(response: TrainingSignResponse?) {
                    if (response == null || !TextUtils.equals(response.errorCode, "0")) {
                        f.onError(RuntimeException("Fetch meeting list faliure."))
                    } else {
                        f.onNext(response)
                    }
                }

                override fun onFailure(repository: RepositoryException?) {
                    f.onError(RuntimeException(repository?.errorMessage()))
                }
            })
        }
    }

    /**
     * 试卷提交接口
     */
    fun requestExamSubmit(request: QuestionSubmitRequest): Observable<ExamSubmitResponse> {
        return Observable.create {f: rx.Subscriber<in ExamSubmitResponse> ->
            FEHttpClient.getInstance().post(request, object : ResponseCallback<ExamSubmitResponse>() {
                override fun onCompleted(response: ExamSubmitResponse?) {
                    if (response == null || !TextUtils.equals(response.errorCode, "0")) {
                        f.onError(RuntimeException("Fetch meeting list faliure."))
                    } else {
                        f.onNext(response)
                    }
                }

                override fun onFailure(repository: RepositoryException?) {
                    f.onError(RuntimeException(repository?.errorMessage()))
                }
            })
        }
    }

}