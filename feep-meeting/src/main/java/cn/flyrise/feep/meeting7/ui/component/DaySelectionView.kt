package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import cn.flyrise.feep.meeting7.selection.time.getMonthDays
import cn.flyrise.feep.meeting7.selection.time.normalTextColor
import cn.flyrise.feep.meeting7.selection.time.unableTextColor
import cn.flyrise.feep.meeting7.R
import com.borax12.materialdaterangepicker.DateTimePickerDialog
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-19 17:06
 */
class DaySelectionView : RelativeLayout {

    private val tvForwardDay: TextView
    private val tvNextDay: TextView
    private val tvSelectedDay: TextView
    private val layoutSelectedDay: View

    private var thisYear: Int
    private var thisMonth: Int
    private var thisDay: Int

    private var year: Int
    private var month: Int
    private var day: Int

    fun getYear() = year
    fun getMonth() = month
    fun getDay() = day

    private val startCalendar: Calendar
    private val minCalendar: Calendar
    private val endCalendar: Calendar

    private var dayChangeListener: ((Int, Int, Int) -> Unit)? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate(context, R.layout.nms_view_day_selection, this)
        tvForwardDay = findViewById(R.id.nmsTvForwardDay)
        tvNextDay = findViewById(R.id.nmsTvNextDay)
        tvSelectedDay = findViewById(R.id.nmsTvSelectedDay)
        layoutSelectedDay = findViewById(R.id.nmsLayoutSelectedDay)

        startCalendar = Calendar.getInstance()
        endCalendar = Calendar.getInstance()
        minCalendar = startCalendar.clone() as Calendar

        year = startCalendar.get(Calendar.YEAR)
        month = startCalendar.get(Calendar.MONTH)
        day = startCalendar.get(Calendar.DAY_OF_MONTH)

        var endYear = year
        var endMonth = month

        for (i in 1..6) {
            endMonth += 1
            if (endMonth > 11) {
                endMonth = 0
                endYear += 1
            }
        }

        val endDay = getMonthDays(endYear, endMonth)
        endCalendar.set(Calendar.YEAR, endYear)
        endCalendar.set(Calendar.MONTH, endMonth)
        endCalendar.set(Calendar.DAY_OF_MONTH, endDay)

        thisYear = year
        thisMonth = month
        thisDay = day

        tvSelectedDay.text = selectedDayString()
        bindListener()
        enableForwardDay(false)
    }

    private fun bindListener() {
        this.layoutSelectedDay.setOnClickListener {
            if (!(context is FragmentActivity)) {
                return@setOnClickListener
            }

            startCalendar.set(Calendar.YEAR, year)
            startCalendar.set(Calendar.MONTH, month)
            startCalendar.set(Calendar.DAY_OF_MONTH, day)

            val dateTimePickerDialog = DateTimePickerDialog()
            dateTimePickerDialog.setDateTime(startCalendar)
            dateTimePickerDialog.setMinCalendar(minCalendar)
            dateTimePickerDialog.setMaxCalendar(endCalendar)

            dateTimePickerDialog.setCanClear(false)
            dateTimePickerDialog.setTimeLevel(DateTimePickerDialog.TIME_LEVEL_DAY)
            dateTimePickerDialog.setButtonCallBack(object : DateTimePickerDialog.ButtonCallBack {
                override fun onClearClick() {}
                override fun onOkClick(c: Calendar, dialog: DateTimePickerDialog?) {
                    bindSelectedDay(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                    dialog?.dismiss()
                }
            })
            dateTimePickerDialog.show((context as FragmentActivity).fragmentManager, "dateTimePickerDialog")
        }

        tvForwardDay.setOnClickListener {
            if (day == 1 && month == 0) {
                year = year - 1
                month = 11
                day = getMonthDays(year, month)
                notifyDayChange()
                return@setOnClickListener
            }

            if (day == 1 && month > 0) {
                month = month - 1
                day = getMonthDays(year, month)
                notifyDayChange()
                return@setOnClickListener
            }

            day = day - 1
            notifyDayChange()
        }

        tvNextDay.setOnClickListener {
            val lastDayInMonth = getMonthDays(year, month)
            if (day == lastDayInMonth && month == 11) {
                day = 1
                month = 0
                year = year + 1
                notifyDayChange()
                return@setOnClickListener
            }


            if (day == lastDayInMonth && month < 11) {
                day = 1
                month = month + 1
                notifyDayChange()
                return@setOnClickListener
            }

            day = day + 1
            notifyDayChange()
        }
    }

    fun bindSelectedDay(year: Int, month: Int, day: Int) {
        if (year == 0) return
        this.year = year
        this.month = month
        this.day = day
        this.notifyDayChange()
    }

    fun setDayChangeListener(dayChangeListener: (Int, Int, Int) -> Unit) {
        this.dayChangeListener = dayChangeListener
    }

    private fun notifyDayChange() {
        this.enableForwardDay(thisYear != year || thisMonth != month || thisDay != day)
        this.tvSelectedDay.text = selectedDayString()
        if (this.dayChangeListener != null) {
            this.dayChangeListener!!.invoke(year, month, day)
        }
    }

    private fun enableForwardDay(enable: Boolean) {
        if (enable) {
            tvForwardDay.setTextColor(normalTextColor)
            tvForwardDay.isClickable = true
        } else {
            tvForwardDay.setTextColor(unableTextColor)
            tvForwardDay.isClickable = false
        }
    }

    private fun selectedDayString() =
            if (thisYear != year || thisMonth != month || thisDay != day) String.format("%02d月%02d日", month + 1, day)
            else String.format("%02d月%02d日(今天)", month + 1, day)


}