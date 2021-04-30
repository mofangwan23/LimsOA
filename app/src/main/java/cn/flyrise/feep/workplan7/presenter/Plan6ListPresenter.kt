package cn.flyrise.feep.workplan7.presenter

import cn.flyrise.android.protocol.entity.ListRequest
import cn.flyrise.android.protocol.entity.ListResponse
import cn.flyrise.android.protocol.model.ListTable
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.workplan7.model.WorkPlanListItemBean
import cn.flyrise.feep.workplan7.contract.PlanListContract
import cn.flyrise.feep.workplan7.model.PlanFilterContent
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class Plan6ListPresenter(view: PlanListContract.IView, isReceive: Boolean) : PlanListContract.IPresenter {

	private val PAGESIZE = 20
	private var mNowPage: Int = 1
	private val mView: PlanListContract.IView = view
	private var userID: String? = if (isReceive) null else CoreZygote.getLoginUserServices().userId

	override fun setFilterUser(userID: String?) {
		this.userID = userID
	}

	override fun refresh() {
		mNowPage = 1
		getPlanListData(userID, mNowPage, PAGESIZE)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({
				val dataList = tableList2PlanList(it.table)
				mView.refreshSuccess(dataList, it.totalNums.toInt() > mNowPage * PAGESIZE)
			}, {
				mView.refreshFail()
			})
	}

	override fun loadMore() {
		mNowPage++
		getPlanListData(userID, mNowPage, PAGESIZE)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({
				val dataList = tableList2PlanList(it.table)
				mView.loadMoreSuccess(dataList, it.totalNums.toInt() > mNowPage * PAGESIZE)
			}, {
				mView.refreshFail()
			})
	}

	//nothing
	override fun setFilterContent(filter: PlanFilterContent?) {}

	override fun getFilterContent(): PlanFilterContent? {
		return null
	}

	private fun getPlanListData(userID: String?, currentPage: Int, pageSize: Int): Observable<ListResponse> {

		return Observable.create {
			val request = ListRequest()
			request.requestType = X.RequestType.OthersWorkPlan
			request.page = currentPage.toString()
			request.perPageNums = pageSize.toString()
			request.id = userID
			FEHttpClient.getInstance().post(request, object : ResponseCallback<ListResponse>() {
				override fun onCompleted(t: ListResponse) {
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

	private fun tableList2PlanList(listTable: ListTable): ArrayList<WorkPlanListItemBean> {
		val workPlanList = ArrayList<WorkPlanListItemBean>()
		for (tableRows in listTable.tableRows) {
			val item = WorkPlanListItemBean()
			for (tableRow in tableRows) {
				when (tableRow.name) {
					"id" -> item.id = tableRow.value
					"isNews" -> item.isNews = tableRow.value == "true"
					"title" -> item.title = tableRow.value
					"sendUser" -> item.sendUser = tableRow.value
					"sendTime" -> item.sendTime = tableRow.value
					"sectionName" -> item.sectionName = tableRow.value
					"status" -> item.status = tableRow.value
					"UserId" -> item.sendUserId = tableRow.value
					"type" -> item.id = tableRow.value
				}
			}
			workPlanList.add(item)
		}
		return workPlanList
	}
}