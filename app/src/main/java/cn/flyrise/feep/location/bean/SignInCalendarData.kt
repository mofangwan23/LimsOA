package cn.flyrise.feep.location.bean

/**
 * 新建：陈冕;
 *日期： 2018-8-7-9:41.
 */
class SignInCalendarData {

    var userId: String? = null
    var day: String? = null
    var isLeader: Boolean = false//是否为领导，领导可以查看月汇总（老版本可以切换下属）
    var isTrack: Boolean = false//是否为外勤人员
    var isAllowSwicth: Boolean = true //是否允许切换日期，默认允许（月汇总跳转到考勤月历不允许切换）
    var existMap: Map<Int, String>? = null//能够查看详情的子项
    var selectedSumId: Int? = null

    fun isExistMapNull() = existMap?.isEmpty() ?: true
}