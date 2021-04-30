package cn.flyrise.android.protocol.entity.workplan

import cn.flyrise.feep.core.network.request.ResponseContent

/**
 * 新建：陈冕;
 *日期： 2018-7-6-14:34.
 */
class PlanStatisticsDetailResponse : ResponseContent() {

    var caption: String? = null//标题
    var havePost: Array<String>? = null//按时提交
    var postLater: Array<String>? = null//迟交
    var noPost: Array<String>? = null//未提交
}