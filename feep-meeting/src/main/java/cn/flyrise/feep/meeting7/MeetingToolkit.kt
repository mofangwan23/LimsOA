package cn.flyrise.feep.meeting7.selection.time

import android.graphics.Color
import java.util.*
import java.util.Calendar.*

/**
 * @author ZYP
 * @since 2018-06-11 10:53
 */
const val STATE_BLANK = 0       // 空白数据
const val STATE_UNABLE = 1      // 已经过去的时间段，不能点击
const val STATE_UNABLE_END = 2  // 已经过去的时间段最后一个，结束
const val STATE_NORMAL = 3      // 正常
const val STATE_START = 4       // 开始点
const val STATE_END = 5         // 结束点
const val STATE_SECTION = 6     // 开始-结束中间部分的点
const val STATE_WEEKEND = 7     // 周末
//const val STATE_OCCUPY_START = 8        // 占用段-开始
//const val STATE_OCCUPY_END = 9          // 占用段-结束
const val STATE_OCCUPY_SECTION = 10     // 占用段-中间部分
//const val STATE_OCCUPY_SINGLE = 11      // 占用段-独自占用
const val MAX_MONTH_IN_BOARD = 6        // 日期看板展示的最大月份数

const val MINE_TYPE_ALL = "1"           // 我的会议-全部
const val MINE_TYPE_TAKE_PART = "2"     // 我的会议-我参与
const val MINE_TYPE_SPONSOR = "3"       // 我的会议-我发起
const val MINE_TYPE_DONT_DEAL = "4"     // 我的会议-未办理
const val SP_HIDE_PULL_DOWN_PROMPT = "hidePullDownPrompt" // 会议列表顶部是否显示黄色提示

val normalTextColor = Color.parseColor("#04121A")       // 正常的文本颜色
val unableTextColor = Color.parseColor("#9DA3A6")       // 不可用的文本颜色
val occupyTextColor = Color.argb(51, 23, 25, 26)       // 占用文本颜色

val MONTH_DAYS = arrayOf(arrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31),
        arrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31))

/**
 * 获取指定月份的天数
 * @param year 年份
 * @param month 月份
 */
fun getMonthDays(year: Int, month: Int): Int =
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) MONTH_DAYS[1][month]
        else MONTH_DAYS[0][month]

fun getHHmm(calendar: Calendar) = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

fun getHHmm(start: Calendar?, end: Calendar?): String {
    if (start == null || end == null) return ""
    return String.format("%02d:%02d-%02d:%02d",
            start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE),
            end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE))
}

fun getMMdd(calendar: Calendar) = String.format("%02d月%02d日", calendar.get(Calendar.MONTH) + 1, calendar.get(DAY_OF_MONTH))

fun getMMddHHmm(calendar: Calendar) = String.format("%02d月%02d日 %02d:%02d",
        calendar.get(MONTH) + 1, calendar.get(DAY_OF_MONTH),
        calendar.get(HOUR_OF_DAY), calendar.get(MINUTE))

fun getYYYYMMddHHmm(calendar: Calendar) = String.format("%d年%02d月%02d日 %02d:%02d",
        calendar.get(YEAR), calendar.get(MONTH) + 1, calendar.get(DAY_OF_MONTH),
        calendar.get(HOUR_OF_DAY), calendar.get(MINUTE))

fun isSameDate(startTime: Calendar?, endTime: Calendar?): Boolean {
    if (startTime == null || endTime == null) return false
    return startTime.get(YEAR) == endTime.get(YEAR)
            && startTime.get(MONTH) == endTime.get(MONTH)
            && startTime.get(DAY_OF_MONTH) == endTime.get(DAY_OF_MONTH)
}

fun isToday(year: Int, month: Int, day: Int): Boolean {
    val c = Calendar.getInstance()
    val y = c.get(Calendar.YEAR)
    val m = c.get(Calendar.MONTH)
    val d = c.get(Calendar.DAY_OF_MONTH)

    return y == year && m == month && d == day
}

fun isSameYear(year: Int): Boolean {
    val c = Calendar.getInstance()
    return c.get(Calendar.YEAR) == year
}

fun daysBetween(sY: Int, sM: Int, sD: Int, eY: Int, eM: Int, eD: Int): Long {
    val s = Calendar.getInstance()
    s.set(YEAR, sY)
    s.set(MONTH, sM)
    s.set(DAY_OF_MONTH, sD)

    val e = Calendar.getInstance()
    e.set(YEAR, eY)
    e.set(MONTH, eM)
    e.set(DAY_OF_MONTH, eD)

    return (e.timeInMillis - s.timeInMillis) / 86400000
}

fun hourBetween(year: Int, month: Int, day: Int, sH: Int, sM: Int, eH: Int, eM: Int): Float {
    val s = Calendar.getInstance()
    s.set(YEAR, year)
    s.set(MONTH, month)
    s.set(DAY_OF_MONTH, day)
    s.set(HOUR_OF_DAY, sH)
    s.set(MINUTE, sM)

    val e = Calendar.getInstance()
    e.set(YEAR, year)
    e.set(MONTH, month)
    e.set(DAY_OF_MONTH, day)
    e.set(HOUR_OF_DAY, eH)
    e.set(MINUTE, eM)

    return (e.timeInMillis - s.timeInMillis) * 1.0f / (1000 * 60 * 60)

}
