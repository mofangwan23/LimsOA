package cn.flyrise.feep.workplan7.contract

import cn.flyrise.feep.workplan7.model.PlanStatisticsListItem

interface PlanStaticsListContract {

	interface IView {

		fun refreshSuccess(data: List<PlanStatisticsListItem>?, hasMore: Boolean)

		fun refreshFail()

		fun loadMoreSuccess(data: List<PlanStatisticsListItem>, hasMore: Boolean)

		fun loadMoreFail()

		fun showLoading(show: Boolean)

		fun remindError()
	}


	interface IPresenter {

		fun refresh()

		fun loadMore()

		fun remind(id: String)

	}
}