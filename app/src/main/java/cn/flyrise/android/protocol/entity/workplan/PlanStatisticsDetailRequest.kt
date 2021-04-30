package cn.flyrise.android.protocol.entity.workplan

import cn.flyrise.feep.core.network.request.RequestContent

/**
 * 新建：陈冕;
 *日期： 2018-7-6-14:32.
 */
class PlanStatisticsDetailRequest(val id: String, val date: String) : RequestContent() {

    override fun getNameSpace() = "WorkPlanRequest"

    val method: String = "awokePostList"

}