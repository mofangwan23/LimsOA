package cn.flyrise.feep.workplan7.model

/**
 * author : klc
 * Msg : 统计规则列表Item
 */
class PlanStatisticsListItem {

    var id: String? = null //id
    var planType: Int? = null  //计划类型 1:日计划,2:周计划,3:月计划,4:其他计划
    var fqcy: Int? = null//频率类型 1:每日,2:每周,3:每月
    var title: String? = null //标题
    //开始时间  X-X-X-X 这种数据格式，第一位（1,2）1表示是本日，还是下一天，类推。  第二位表示是周几or几号。  第三四位 表示时分
    var startTime: String? = null
    var startDate: String? = null//开始时间 选择周计划或月计划时的日值
    var endTime: String? = null //结束时间
    var endDate: String? = null//结束时间 选择周计划或月计划时的日值

    var noSumit: Int? = null //未提交人数
    var users: String? = null //提交人数  7290,7291,7293...该种格式

    var awoke: String? = null//提醒时间 单位为小时
    var tips: String? = null//提示语
}