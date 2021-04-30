package cn.flyrise.feep.workplan7.contract

import cn.flyrise.feep.workplan7.model.WorkPlanListItemBean
import cn.flyrise.feep.workplan7.model.PlanFilterContent

/**
 * author : klc
 * Msg : 计划首页列表的接口
 */
interface PlanListContract {

	interface IView {

		fun refreshSuccess(data: ArrayList<WorkPlanListItemBean>, hasMore: Boolean)

		fun refreshFail()

		fun loadMoreSuccess(data: ArrayList<WorkPlanListItemBean>, hasMore: Boolean)

		fun loadMoreFail()

	}


	interface IPresenter {

		fun setFilterUser(userID: String?)

		fun setFilterContent(filter: PlanFilterContent?)

		fun getFilterContent(): PlanFilterContent?

		fun refresh()

		fun loadMore()

	}


}