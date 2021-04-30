package cn.flyrise.feep.meeting7.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import cn.flyrise.feep.meeting7.R
import kotlinx.android.synthetic.main.nms_dialog_attendees.*

/**
 * @author 社会主义接班人
 * @since 2018-08-09 16:42
 */

data class Statistics(
        val untreatedCount: Int,
        val notAttendCount: Int,
        val attendCount: Int,
        val selected: Int
)

class StatisticsDialog : DialogFragment() {

    private lateinit var statistics: Statistics
    private var onItemClick: ((Int) -> Unit)? = null
    private val executeClick = { i: Int ->
        onItemClick?.invoke(i)
        dismiss()
    }

    companion object {
        fun newInstance(s: Statistics, o: ((Int) -> Unit)) = StatisticsDialog().apply {
            statistics = s
            onItemClick = o
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog.window
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setWindowAnimations(R.style.NMSSelectionDialogAnimation)
        return inflater.inflate(R.layout.nms_dialog_attendees, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        nmsIvX.setOnClickListener { dismiss() }
        nmsTvUntreated.setText("未办理(${statistics.untreatedCount})")
        nmsTvNotAttend.setText("不参加(${statistics.notAttendCount})")
        nmsTvAttend.setText("参加(${statistics.attendCount})")

        nmsIvUntreated.setImageResource(
                if (statistics.selected == 0) R.mipmap.nms_ic_meeting_attendee_checked else R.mipmap.nms_ic_meeting_attendee_uncheck)
        nmsIvNotAttend.setImageResource(
                if (statistics.selected == 1) R.mipmap.nms_ic_meeting_attendee_checked else R.mipmap.nms_ic_meeting_attendee_uncheck)
        nmsIvAttend.setImageResource(
                if (statistics.selected == 2) R.mipmap.nms_ic_meeting_attendee_checked else R.mipmap.nms_ic_meeting_attendee_uncheck)

        nmsLayoutUntreated.setOnClickListener { executeClick(0) }
        nmsLayoutNotAttend.setOnClickListener { executeClick(1) }
        nmsLayoutAttend.setOnClickListener { executeClick(2) }

    }

}