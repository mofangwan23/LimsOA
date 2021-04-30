package cn.flyrise.feep.workplan7.presenter

import cn.flyrise.android.protocol.entity.workplan.PlanListRequest
import cn.flyrise.android.protocol.entity.workplan.PlanListResponse
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.workplan7.contract.PlanListContract
import cn.flyrise.feep.workplan7.model.PlanFilterContent
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class Plan7ListPresenter(private val mView: PlanListContract.IView, private val isReceive: Boolean) : PlanListContract.IPresenter {

	private val PAGESIZE = 20
	private var mNowPage: Int = 1
	private var filter: PlanFilterContent? = null

	override fun setFilterUser(userID: String?) {}

	override fun setFilterContent(filter: PlanFilterContent?) {
		this.filter = filter
	}

	override fun getFilterContent(): PlanFilterContent? = this.filter

	override fun refresh() {
		mNowPage = 1
		getPlanListData(filter, isReceive, mNowPage, PAGESIZE)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({
				mView.refreshSuccess(it.data!!, it.totalPage!!.toInt() > mNowPage)
			}, {
				mView.refreshFail()
			})
	}

	override fun loadMore() {
		mNowPage++
		getPlanListData(filter, isReceive, mNowPage, PAGESIZE)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({
				mView.loadMoreSuccess(it.data!!, it.totalPage!!.toInt() > mNowPage)
			}, {
				mView.refreshFail()
			})
	}

	private fun getPlanListData(filter: PlanFilterContent?, isReceive: Boolean, currentPage: Int, pageSize: Int):
			Observable<PlanListResponse> {
		return Observable.create {
			val request = PlanListRequest()
			request.mySend = if (isReceive) "0" else "1"
			request.page = currentPage.toString()
			request.pageSize = pageSize.toString()
			request.userId = filter?.userIDs
			request.type = filter?.type?.toString()
			request.startTime = filter?.startTime?.toString()
			request.endTime = filter?.endTime?.toString()
			FEHttpClient.getInstance().post(request, object : ResponseCallback<PlanListResponse>() {
				override fun onCompleted(t: PlanListResponse) {
					if (t.errorCode == "0") {
						it.onNext(t)
					}
					else {
						it.onError(Throwable(t.errorMessage))
					}
				}

				override fun onFailure(repositoryException: RepositoryException?) {
					super.onFailure(repositoryException)
					it.onError(Throwable("Get workPlan list error"))
				}
			})
		}
	}


}