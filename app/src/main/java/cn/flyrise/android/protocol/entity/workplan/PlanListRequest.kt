package cn.flyrise.android.protocol.entity.workplan

import cn.flyrise.feep.core.network.request.RequestContent

/**
 * author : klc
 * data on 2018/6/15 11:27
 * Msg : 计划列表内容获取  给7.0使用的，包括了筛选条件
 */
class PlanListRequest() : RequestContent() {

	val method = "search"

	var mySend: String? = null //1为自己写的，0为人家发送给你的
	var pageSize: String? = null
	var page: String? = null

	//过滤的选项
	var userId: String? = null  //要搜索的userId,可以不用填
	var type: String? = null
	var startTime: String? = null
	var endTime: String? = null

	override fun getNameSpace(): String = "WorkPlanRequest"
}