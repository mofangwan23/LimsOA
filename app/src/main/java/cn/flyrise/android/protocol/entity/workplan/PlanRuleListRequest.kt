package cn.flyrise.android.protocol.entity.plan7

import cn.flyrise.feep.core.network.request.RequestContent

/**
 * author : klc
 * Msg : 获取统计规则列表
 */
class PlanRuleListRequest() : RequestContent() {

    override fun getNameSpace(): String = "WorkPlanRequest"

    private val method: String = "awokeList"

}