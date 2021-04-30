package cn.flyrise.feep.workplan7.provider

import cn.flyrise.android.protocol.entity.plan7.PlanRuleListRequest
import cn.flyrise.android.protocol.entity.plan7.PlanRuleListResponse
import cn.flyrise.android.protocol.entity.plan7.PlanRuleRemindRequest
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.request.ResponseContent
import rx.Observable

class PlanStatisticsProvider {

    fun getStaticsList(): Observable<PlanRuleListResponse> {
        return Observable.create {
            val request = PlanRuleListRequest()
            FEHttpClient.getInstance().post(request, object : ResponseCallback<PlanRuleListResponse>() {
                override fun onCompleted(t: PlanRuleListResponse) {
                    if (t.errorCode == "0") {
                        it.onNext(t)
                    } else {
                        it.onError(Throwable("get getStaticsList error"))
                    }
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    super.onFailure(repositoryException)
                    it.onError(Throwable("get getStaticsList error"))
                }
            })
        }
    }

    fun remind(id: String): Observable<Boolean> {
        return Observable.create {
            val request = PlanRuleRemindRequest(id)
            FEHttpClient.getInstance().post(request, object : ResponseCallback<ResponseContent>() {
                override fun onCompleted(t: ResponseContent?) {
                    if (t != null && t.errorCode == "0") {
                        it.onNext(true)
                    } else {
                        it.onError(Throwable("remind error"))
                    }
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    super.onFailure(repositoryException)
                    it.onError(Throwable("remind error"))
                }
            })
        }
    }

}
