package cn.flyrise.android.protocol.entity.workplan

import cn.flyrise.feep.core.network.request.RequestContent


/**
 * 新建统计规则
 */
class PlanNewRuleRequest : RequestContent() {

    override fun getNameSpace(): String = "WorkPlanRequest"

    val method = "AwokeSave"
    var id: String? = null//当要修改时，要把id传进来，如果是新增时这个字段可以不传
    var planType: String? = null//计划类型 1:日计划,2:周计划,3:月计划,4:其他计划
    var title: String? = null//标题
    var fqcy: String? = null//频率类型 1:每日,2:每周,3:每月
    var startTime: String? = null//开始时间 HH:mm
    var startDate: String? = null//开始时间 选择周计划或月计划时的日值
    var endTime: String? = null//结束时间 HH:mm
    var endDate: String? = null//结束时间 选择周计划或月计划时的日值
    var users: String? = null//选择提交人员的userId
    var awoke: String? = null//提醒时间 单位为小时
    var tips: String? = null//提示语
}