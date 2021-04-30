package cn.flyrise.feep.meeting7.ui.bean

import android.text.TextUtils
import android.text.format.DateUtils
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.meeting7.selection.time.getHHmm
import cn.flyrise.feep.meeting7.selection.time.getMMdd
import cn.flyrise.feep.meeting7.selection.time.getMMddHHmm
import cn.flyrise.feep.meeting7.selection.time.isSameDate
import java.util.*
import java.util.Calendar.*

/**
 * @author ZYP
 * @since 2018-06-20 11:58
 */
class MeetingDescription {

    companion object {
        fun newInstance(item: MeetingEntity): MeetingDescription {
            val description = MeetingDescription()
            description.meetingId = item.meetingId
            description.topics = item.topics
            description.initiator = item.initiator
            description.startDate = item.startDate
            description.endDate = item.endDate
            description.roomName = item.roomName
            description.attendedFlag = item.attendedFlag

            description.startTime = DateUtil.str2Calendar(item.startDate)
            description.endTime = DateUtil.str2Calendar(item.endDate)
            return description
        }
    }

    var meetingId: String? = null
    var topics: String? = null
    var initiator: String? = null
    var startDate: String? = null
    var endDate: String? = null
    var roomName: String? = null
    var attendedFlag: String? = null

    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar

    fun getDisplayDate() = when {
        isToday() -> "今天"
        isTomorrow() -> "明天"
        else -> getMMdd(startTime)
    }

    fun getDisplayTime() = getHHmm(startTime)

    fun getDisplayStartDate() = getMMddHHmm(startTime)

    fun getDisplayEndDate() = getMMddHHmm(endTime)

    fun getDisplayStartEndTime() = getHHmm(startTime, endTime)

    fun isSameDay() = isSameDate(startTime, endTime)

    fun isToday() = DateUtils.isToday(startTime.timeInMillis)

    fun isTomorrow(): Boolean {
        val today = Calendar.getInstance()
        today.set(MILLISECOND, today.get(MILLISECOND) + 86400000)
        return (startTime.get(YEAR) == today.get(YEAR)
                && startTime.get(MONTH) == today.get(MONTH)
                && startTime.get(DAY_OF_MONTH) == today.get(DAY_OF_MONTH))
    }

    fun isOutOfDate() = TextUtils.equals(attendedFlag, "-1")

    fun isUntreated() = TextUtils.equals(attendedFlag, "0")

    fun isAttend() = TextUtils.equals(attendedFlag, "1")

    fun isNotAttend() = TextUtils.equals(attendedFlag, "2")

    fun isCancel() = TextUtils.equals(attendedFlag, "3")

    fun isUnknownStatus() = TextUtils.isEmpty(attendedFlag)

}


