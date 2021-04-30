package cn.flyrise.android.protocol.entity.workplan

import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.workplan7.model.WorkPlanListItemBean

/**
 * author : klc
 * Msg : 计划列表响应数据
 */
class PlanListResponse : ResponseContent() {

	var totalPage: Int? = null
	var totalNums: Int? = null

	var data: ArrayList<WorkPlanListItemBean>? = null

}