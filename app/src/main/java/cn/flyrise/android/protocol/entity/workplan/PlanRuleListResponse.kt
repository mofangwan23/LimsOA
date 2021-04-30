package cn.flyrise.android.protocol.entity.plan7

import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.workplan7.model.PlanStatisticsListItem

/**
 * author : klc
 * data on 2018/6/15 14:45
 * Msg :
 */
class PlanRuleListResponse : ResponseContent() {

	var data: List<PlanStatisticsListItem>? = null

}