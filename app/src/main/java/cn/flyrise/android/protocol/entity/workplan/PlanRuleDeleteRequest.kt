package cn.flyrise.android.protocol.entity.workplan

import cn.flyrise.feep.core.network.request.RequestContent

/**
 * 新建：陈冕;
 *日期： 2018-7-13-14:46.
 * 统计规则删除
 */
class PlanRuleDeleteRequest(var id: String) : RequestContent() {

    override fun getNameSpace() = "WorkPlanRequest"

    val method: String = "awokeDelete"
}