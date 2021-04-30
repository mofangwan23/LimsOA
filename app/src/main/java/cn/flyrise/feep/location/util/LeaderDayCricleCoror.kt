package cn.flyrise.feep.location.util

/**
 * 新建：陈冕;
 *日期： 2018-6-13-16:30.
 */

//去除0和100，剩下的按四分之一区分颜色
private val colorDatas = arrayOf("#FFE4E6E7", "#FF3B2F", "#FF943C", "#F2AC49", "#F2D649", "#43CA86")

fun resetColor(interval: Float) = when {
    interval == 0f -> colorDatas[0]
    interval > 0 && interval <= 1 / 4f -> colorDatas[1]
    interval > 1 / 4f && interval <= 2 / 4f -> colorDatas[2]
    interval > 2 / 4f && interval <= 3 / 4f -> colorDatas[3]
    interval > 3 / 4f && interval < 1 -> colorDatas[4]
    interval == 1f -> colorDatas[5]
    else -> colorDatas[0]
}