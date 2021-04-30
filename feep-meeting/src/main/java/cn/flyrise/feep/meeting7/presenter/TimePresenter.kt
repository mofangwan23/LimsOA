package cn.flyrise.feep.meeting7.presenter

import android.support.v4.app.FragmentActivity
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.Selections
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.selection.date.DateSelectionFragment
import cn.flyrise.feep.meeting7.selection.time.*
import cn.flyrise.feep.meeting7.ui.bean.MeetingModel
import cn.flyrise.feep.meeting7.ui.component.TimeView
import com.borax12.materialdaterangepicker.DateTimePickerDialog
import java.util.*

/**
 * @author ZYP
 * @date 2018-07-04 11:22
 */
class TimePresenter(val v: TimeView, val m: MeetingModel) {

    /**
     * 当天：选择时间
     */
    fun selectTime() {
        val timeBoard = TimeSelectionFragment.newInstance(m.r)
        Selections.newInstance(timeBoard)
                .setOnDateConfirmListener { s, e -> dateTimeConfirm(s, e) }
                .show((v.context() as FragmentActivity).supportFragmentManager, "TimeBoard")
    }

    /**
     * 跨天：选择日期
     */
    fun selectDate() {
        val dateBoard = DateSelectionFragment.newInstance(m.r)
        Selections.newInstance(dateBoard)
                .setOnDateConfirmListener { s, e -> dateTimeConfirm(s, e) }
                .show((v.context() as FragmentActivity).supportFragmentManager, "DateBoard")
    }

    /**
     * 跨天：选择开始时间
     */
    fun selectStartTime() {
        if (m.r.startYear == 0) return
        TimeWheelSelectionFragment
                .newInstance("开始时间", m.r.startYear, m.r.startMonth,
                        m.r.startDay, m.r.startHour, m.r.startMinute) { hour, minute ->
                    m.r.startHour = hour
                    m.r.startMinute = minute
                    if(hour == 0 && minute == 0){
                        v.startTime("请选择")
                    }else{
                        v.startTime(String.format("%02d:%02d", hour, minute))
                    }
                    calcTime()
                }
                .show((v.context() as FragmentActivity).supportFragmentManager, "TimeSelect")
    }

    /**
     * 跨天：选择结束时间
     */
    fun selectEndTime() {
        if (m.r.endYear == 0) return
        if (m.r.startHour == 0 && m.r.startMinute == 0){
            v.showToast(R.string.nms_select_start_time_first)
            return
        }
        TimeWheelSelectionFragment
                .newInstance("结束时间", m.r.endYear, m.r.endMonth,
                        m.r.endDay, m.r.endHour, m.r.endMinute) { hour, minute ->
                    m.r.endHour = hour
                    m.r.endMinute = minute
                    if(hour == 0 && minute == 0){
                        v.endTime("请选择")
                    }else{
                        v.endTime(String.format("%02d:%02d", hour, minute))
                    }
                    calcTime()
                }
                .show((v.context() as FragmentActivity).supportFragmentManager, "TimeSelect")
    }

    /**
     * 自定义会议-跨天：选择开始时间
     */
    fun selectCStartDate() {
        openDateTimePicker(true, DateTimePickerDialog.TIME_LEVEL_MIN) {
            val t = String.format("%d年%02d月%02d日 %02d:%02d", m.r.startYear, m.r.startMonth + 1, m.r.startDay, m.r.startHour, m.r.startMinute)
            v.startTime(t)
            calcTime()
        }
    }

    /**
     * 自定义会议-跨天：选择结束时间
     */
    fun selectCEndDate() {
        openDateTimePicker(false, DateTimePickerDialog.TIME_LEVEL_MIN) {
            val t = String.format("%d年%02d月%02d日 %02d:%02d", m.r.endYear, m.r.endMonth + 1, m.r.endDay, m.r.endHour, m.r.endMinute)
            v.endTime(t)
            calcTime()
        }
    }

    /**
     * 自定义会议-当天：选择开始时间
     */
    fun selectCStartTime() {
        openDateTimePicker(true, -1) {
            val template = if (isSameYear(m.r.startYear)) "%02d月%02d日 %02d:%02d" else "次年%02d月%02d日 %02d:%02d"
            val t = String.format(template, m.r.startMonth + 1, m.r.startDay, m.r.startHour, m.r.startMinute)
            v.startTime(t)
            calcTime()
        }
    }

    /**
     * 自定义会议-当天：选择结束时间
     */
    fun selectCEndTime() {
        openDateTimePicker(false, -1) {
            val template = if (isSameYear(m.r.endYear)) "%02d月%02d日 %02d:%02d" else "次年%02d月%02d日 %02d:%02d"
            val t = String.format(template, m.r.endMonth + 1, m.r.endDay, m.r.endHour, m.r.endMinute)
            v.endTime(t)
            calcTime()
        }
    }

    fun dateTimeConfirm(s: MSDateItem?, e: MSDateItem?) {
        if (s == null || e == null) return

        m.r.updateInfo(s, e)
        if (m.r.type == 0) {
            val template = if (isSameYear(m.r.startYear)) "%02d月%02d日 %02d:%02d-%02d:%02d" else "次年%02d月%02d日 %02d:%02d-%02d:%02d"
            val time = String.format(template, m.r.startMonth + 1, m.r.startDay, m.r.startHour, m.r.startMinute, m.r.endHour, m.r.endMinute)
            v.time(time)
            calcTime()
            return
        }

        val isToday = isToday(m.r.startYear, m.r.startMonth, m.r.startDay)
        val template = if (isToday) "%d年%02d月%02d日(今天)" else "%d年%02d月%02d日"

        val sText = String.format(template, m.r.startYear, m.r.startMonth + 1, m.r.startDay)
        val eText = String.format("%d年%02d月%02d日", m.r.endYear, m.r.endMonth + 1, m.r.endDay)
        v.date(sText, eText)
        calcTime()
    }

    fun setStartAndEndTime() {
        if(m.r.startHour == 0 && m.r.startMinute == 0){
            v.startTime("请选择")
        }else{
            v.startTime(String.format("%02d:%02d", m.r.startHour, m.r.startMinute))
        }
        if(m.r.endHour == 0 && m.r.endMinute == 0){
            v.endTime("请选择")
        }else{
            v.endTime(String.format("%02d:%02d", m.r.endHour, m.r.endMinute))
        }

    }

    fun calcTime() {
        if (m.r.type == 0) {
            val hours = hourBetween(m.r.startYear, m.r.startMonth,
                    m.r.startDay, m.r.startHour, m.r.startMinute, m.r.endHour, m.r.endMinute)
            if (hours > 0) {
                v.totalTime(String.format("共 %.1f 小时", hours))
            }
            return
        }

        val days = daysBetween(m.r.startYear, m.r.startMonth, m.r.startDay, m.r.endYear, m.r.endMonth, m.r.endDay)
        if (days > 0) {
            v.totalTime("共 ${days + 1} 天")
        }
    }

    private fun openDateTimePicker(isStart: Boolean, level: Int, onComplete: () -> Unit) {
        val calcMaxDate = fun(standard: Calendar): Calendar {
            val maxDate = standard.clone() as Calendar
            var endYear = maxDate.get(Calendar.YEAR)
            var endMonth = maxDate.get(Calendar.MONTH)
            for (i in 1..6) {
                endMonth += 1
                if (endMonth > 11) {
                    endMonth = 0
                    endYear += 1
                }
            }

            val endDay = getMonthDays(endYear, endMonth)
            maxDate.set(Calendar.YEAR, endYear)
            maxDate.set(Calendar.MONTH, endMonth)
            maxDate.set(Calendar.DAY_OF_MONTH, endDay)
            return maxDate
        }

        val minDate = Calendar.getInstance()
        val maxDate = calcMaxDate(minDate)
        val dateTime = Calendar.getInstance()

        if (isStart) {
            if (m.r.startYear > 0) dateTime.set(Calendar.YEAR, m.r.startYear)
            if (m.r.startMonth > 0) dateTime.set(Calendar.MONTH, m.r.startMonth)
            if (m.r.startDay > 0) dateTime.set(Calendar.DAY_OF_MONTH, m.r.startDay)

            if (level == -1) {
                dateTime.set(Calendar.HOUR_OF_DAY, m.r.startHour)
                dateTime.set(Calendar.MINUTE, m.r.startMinute)
            }
        } else {
            if(level == -1){
                if(m.r.startDay == 0 && m.r.startHour ==0 && m.r.startMinute == 0) {
                    v.showToast(R.string.nms_select_start_time_first)
                    return
                }
                dateTime.set(Calendar.HOUR_OF_DAY, m.r.endHour)
                dateTime.set(Calendar.MINUTE, m.r.endMinute)
            }else{
                if(m.r.startYear == 0 && m.r.startMonth == 0){
                    v.showToast(R.string.nms_select_start_time_first)
                    return
                }
                if (m.r.endYear > 0) dateTime.set(Calendar.YEAR, m.r.endYear)
                if (m.r.endMonth > 0) dateTime.set(Calendar.MONTH, m.r.endMonth)
                if (m.r.endDay > 0) dateTime.set(Calendar.DAY_OF_MONTH, m.r.endDay)
            }


        }

        // 日期最大就六个月
        val dialog = DateTimePickerDialog()
        dialog.setDateTime(dateTime)
        dialog.setMinCalendar(minDate)
        dialog.setMaxCalendar(maxDate)
        if (level == -1) dialog.setOnlyTime(true)
        dialog.setTimeLevel(DateTimePickerDialog.TIME_LEVEL_MIN)
        dialog.setButtonCallBack(object : DateTimePickerDialog.ButtonCallBack {
            override fun onClearClick() {}
            override fun onOkClick(c: Calendar, d: DateTimePickerDialog?) {
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)
                val hour = c.get(Calendar.HOUR_OF_DAY)
                val minute = c.get(Calendar.MINUTE)

                if (isStart) {
                    m.r.startYear = year
                    m.r.startMonth = month
                    m.r.startDay = day
                    m.r.startHour = hour
                    m.r.startMinute = minute
                } else {
                    m.r.endYear = year
                    m.r.endMonth = month
                    m.r.endDay = day
                    m.r.endHour = hour
                    m.r.endMinute = minute
                }

                onComplete!!.invoke()
                dialog.dismiss()
            }
        })
        dialog.show((v.context() as FragmentActivity).fragmentManager, "Dialog")
    }

}