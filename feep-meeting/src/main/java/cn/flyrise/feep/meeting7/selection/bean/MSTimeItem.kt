package cn.flyrise.feep.meeting7.selection.bean

import android.text.TextUtils
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-14 17:01
 */
class MSTimeItem : MSDateItem {

    var hour: Int = 0
    var minute: Int = 0

    constructor()

    constructor(year: Int, month: Int, day: Int, hour: Int, minute: Int, state: Int) {
        this.year = year
        this.month = month
        this.day = day
        this.hour = hour
        this.minute = minute
        this.state = state
    }

    fun getTime(): String {
        return "${hour}:${if (minute == 0) "00" else "${minute}"}"
    }

    companion object {
        fun newInstance(yyyyMMddhhmm: String?): MSTimeItem? {
            if (TextUtils.isEmpty(yyyyMMddhhmm)) {
                return null
            }

            val token = StringTokenizer(yyyyMMddhhmm)
            val item = MSTimeItem()
            item.year = token.nextToken().toInt()
            item.month = token.nextToken().toInt()
            item.day = token.nextToken().toInt()
            item.hour = token.nextToken().toInt()
            item.minute = token.nextToken().toInt()
            return item
        }
    }

}