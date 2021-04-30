package cn.flyrise.feep.workplan7.model

import cn.flyrise.feep.core.network.request.RequestContent
/**
 * 请求当前星期第一天是星期几：0为星期日，1为星期一
 * */
class AttendanceRequest : RequestContent() {

    override fun getNameSpace() = "AttendanceRequest"

    val method = "getFirstWeekDay"

}