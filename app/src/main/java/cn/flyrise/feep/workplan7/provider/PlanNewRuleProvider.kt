package cn.flyrise.feep.workplan7.provider

import cn.flyrise.android.protocol.entity.workplan.PlanNewRuleRequest
import cn.flyrise.android.protocol.entity.workplan.PlanRuleDeleteRequest
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.request.ResponseContent
import rx.Observable

/**
 * 新建：陈冕;
 *日期： 2018-7-2-11:16.
 */
class PlanNewRuleProvider {

    fun submitRule(request: PlanNewRuleRequest): Observable<String> {
        return Observable.create({
            FEHttpClient.getInstance().post(request, object : ResponseCallback<ResponseContent>() {
                override fun onCompleted(t: ResponseContent) {
                    it.onNext(t.errorCode)
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    super.onFailure(repositoryException)
                }
            })

        })
    }

    fun deleteRule(request: PlanRuleDeleteRequest): Observable<String> {
        return Observable.create({
            FEHttpClient.getInstance().post(request, object : ResponseCallback<ResponseContent>() {
                override fun onCompleted(t: ResponseContent) {
                    it.onNext(t.errorCode)
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    super.onFailure(repositoryException)
                }
            })

        })
    }


}