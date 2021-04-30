package cn.flyrise.feep.meeting7.selection.bean

import cn.flyrise.feep.meeting7.selection.bean.MSDateItem

/**
 * @author ZYP
 * @since 2018-06-14 17:01
 */
class MSMonthDateItem(year: Int, month: Int) : MSDateItem(year, month, 1) {
    fun getMonth(): String = "${year}年${month + 1}月"
}