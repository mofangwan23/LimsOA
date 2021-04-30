package cn.flyrise.android.protocol.entity.plan7

import cn.flyrise.feep.core.network.request.RequestContent

/**
 * author : klc
 * Msg : 计划提醒
 */
class PlanRuleRemindRequest(private var mId: String) : RequestContent() {

    override fun getNameSpace() = "WorkPlanRequest"

    private val method = "awoke"

    var id: String? = mId

}