package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.presenter.TimePresenter
import cn.flyrise.feep.meeting7.selection.time.isSameYear
import cn.flyrise.feep.meeting7.selection.time.isToday
import kotlinx.android.synthetic.main.nms_view_meeting_time.view.*

interface TimeView {
    fun setPresenter(p: TimePresenter)
    fun time(t: String)
    fun date(s: String, e: String)
    fun startTime(t: String)
    fun endTime(t: String)
    fun totalTime(time: String)
    fun context(): Context
    fun showToast(i: Int)
    fun setTimeTypeViewVisiable(b: Boolean)
}

class MeetingTimeView : LinearLayout, TimeView {
    override fun setTimeTypeViewVisiable(isUpdateMeeting: Boolean) {
        if(!isUpdateMeeting) nmsllMeetingTime.visibility = View.VISIBLE else nmsllMeetingTime.visibility = View.GONE
        nmsTvStartEndTime.isEnabled = !isUpdateMeeting
    }

    override fun showToast(i: Int) {
        FEToast.showMessage(context.getString(i))
    }

    private var p: TimePresenter? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate(context, R.layout.nms_view_meeting_time, this)

        nmsTvStartEndTime.setOnClickListener { p?.selectTime() }
        nmsTvStartDate.setOnClickListener { p?.selectDate() }
        nmsTvEndDate.setOnClickListener { p?.selectDate() }
        nmsTvStartTime.setOnClickListener { p?.selectStartTime() }
        nmsTvEndTime.setOnClickListener { p?.selectEndTime() }
        nmsTimeType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.nmsTimeTypeSameDay) {
                p?.m?.r?.type = 0
                nmsLayoutStartEndTime.visibility = View.VISIBLE
                nmsLayoutStartDate.visibility = View.GONE
                nmsLayoutEndDate.visibility = View.GONE
                nmsTvStartEndTime.text = context?.getString(R.string.meeting7_create_selected_title)
            } else {
                p?.m?.r?.type = 1
                nmsLayoutStartEndTime.visibility = View.GONE
                nmsLayoutStartDate.visibility = View.VISIBLE
                nmsLayoutEndDate.visibility = View.VISIBLE

                nmsTvStartDate.text = context?.getString(R.string.meeting7_create_selected_title)
                nmsTvStartTime.text =context?.getString(R.string.meeting7_create_selected_title)
                nmsTvEndDate.text = context?.getString(R.string.meeting7_create_selected_title)
                nmsTvEndTime.text = context?.getString(R.string.meeting7_create_selected_title)
            }
            nmsTvDurationTime.visibility = View.GONE
        }
    }

    override fun setPresenter(p: TimePresenter) {
        this.p = p
        if (p.m.r.type == 1) {
            nmsTimeType.check(R.id.nmsTimeTypeAcrossDay)
        } else {
            nmsTimeType.check(R.id.nmsTimeTypeSameDay)
        }
        postDelayed({
            p.dateTimeConfirm(p.m.r.startDate(), p.m.r.endDate())
            p.setStartAndEndTime()
        }, 1000)
    }

    override fun time(t: String) {
        nmsTvStartEndTime.text = t
    }

    override fun date(s: String, e: String) {
        nmsTvStartDate.text = s
        nmsTvEndDate.text = e
    }

    override fun startTime(t: String) {
        nmsTvStartTime.text = t
    }

    override fun endTime(t: String) {
        nmsTvEndTime.text = t
    }

    override fun totalTime(time: String) {
        nmsTvDurationTime.visibility = View.VISIBLE
        nmsTvDurationTime.text = time
    }

    override fun context() = context
}

