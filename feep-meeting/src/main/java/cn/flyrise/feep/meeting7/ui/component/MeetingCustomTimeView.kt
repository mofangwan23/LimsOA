package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.presenter.TimePresenter
import kotlinx.android.synthetic.main.nms_view_meeting_custom_time.view.*

class MeetingCustomTimeView : LinearLayout, TimeView {
    override fun setTimeTypeViewVisiable(b: Boolean) {
        if(b)nmsTimeType.visibility = View.VISIBLE else nmsTimeType.visibility = View.GONE
    }

    override fun showToast(i: Int) {
        FEToast.showMessage(context.getString(i))
    }

    private var p: TimePresenter? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate(context, R.layout.nms_view_meeting_custom_time, this)

        nmsTimeType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.nmsTimeTypeSameDay) {
                p?.m?.r?.type = 0
                nmsLayoutStartTime.visibility = View.VISIBLE
                nmsLayoutEndTime.visibility = View.VISIBLE
                nmsLayoutStartDate.visibility = View.GONE
                nmsLayoutEndDate.visibility = View.GONE
                nmsTvStartTime.text = context?.getString(R.string.meeting_create_selected)
                nmsTvEndTime.text = context?.getString(R.string.meeting_create_selected)
            } else {
                p?.m?.r?.type = 1
                nmsLayoutStartTime.visibility = View.GONE
                nmsLayoutEndTime.visibility = View.GONE
                nmsLayoutStartDate.visibility = View.VISIBLE
                nmsLayoutEndDate.visibility = View.VISIBLE
                nmsTvStartDate.text = context?.getString(R.string.meeting_create_selected)
                nmsTvEndDate.text = context?.getString(R.string.meeting_create_selected)
            }
            nmsTvDurationTime.visibility = View.GONE
        }

        nmsTvStartDate.setOnClickListener { p?.selectCStartDate() }
        nmsTvEndDate.setOnClickListener { p?.selectCEndDate() }

        nmsTvStartTime.setOnClickListener { p?.selectCStartTime() }
        nmsTvEndTime.setOnClickListener { p?.selectCEndTime() }
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

    override fun startTime(t: String) {
        if (nmsLayoutStartDate.visibility == View.VISIBLE) {
            nmsTvStartDate.text = t
        } else {
            nmsTvStartTime.text = t
        }
    }

    override fun endTime(t: String) {
        if (nmsLayoutEndDate.visibility == View.VISIBLE) {
            nmsTvEndDate.text = t
        } else {
            nmsTvEndTime.text = t
        }
    }

    override fun totalTime(time: String) {
        nmsTvDurationTime.visibility = View.VISIBLE
        nmsTvDurationTime.text = time
    }

    override fun context() = context
    override fun time(t: String) {}
    override fun date(s: String, e: String) {}

}