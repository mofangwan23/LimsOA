package cn.flyrise.feep.meeting7.selection.time

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.selection.bean.MSTimeItem
import kotlinx.android.synthetic.main.nms_dialog_meeting_time.*

/**
 * @author 社会主义接班人
 * @since 2018-09-14 11:39
 */
class TimeSelectionDialog : DialogFragment() {

    private var selectedPosition: Int = 0
    private var times: List<MSDateItem>? = null
    private var onTimeSelected: ((MSDateItem, MSDateItem) -> Unit)? = null

    companion object {
        fun newInstance(times: List<MSDateItem>, onTimeSelected: ((MSDateItem, MSDateItem) -> Unit)?) = TimeSelectionDialog().apply {
            this.times = times
            this.onTimeSelected = onTimeSelected
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
        return inflater.inflate(R.layout.nms_dialog_meeting_time, container, false)
    }

    // 垃圾代码~~
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        nmsTvCancel.setOnClickListener { dismiss() }
        nmsTvCreateMeeting.setOnClickListener {
            onTimeSelected?.invoke(times!!.get(selectedPosition * 2), times!!.get(selectedPosition * 2 + 1))
            dismiss()
        }


        if (times?.size == 2) {
            nmsIv1.setImageResource(R.mipmap.nms_ic_meeting_attendee_checked)
            val t1 = times?.get(0) as MSTimeItem
            val t2 = times?.get(1) as MSTimeItem
            nmsTv1.setText(String.format("%02d:%02d-%02d:%02d", t1.hour, t1.minute, t2.hour, t2.minute))
        } else if (times?.size == 6) {
            nmsLayout2.visibility = View.VISIBLE
            nmsLayout3.visibility = View.VISIBLE

            nmsIv1.setImageResource(R.mipmap.nms_ic_meeting_attendee_checked)
            nmsIv2.setImageResource(R.mipmap.nms_ic_meeting_attendee_uncheck)
            nmsIv3.setImageResource(R.mipmap.nms_ic_meeting_attendee_uncheck)

            val t1 = times?.get(0) as MSTimeItem
            val t2 = times?.get(1) as MSTimeItem
            val t3 = times?.get(2) as MSTimeItem
            val t4 = times?.get(3) as MSTimeItem
            val t5 = times?.get(4) as MSTimeItem
            val t6 = times?.get(5) as MSTimeItem
            nmsTv1.setText(String.format("%02d:%02d-%02d:%02d", t1.hour, t1.minute, t2.hour, t2.minute))
            nmsTv2.setText(String.format("%02d:%02d-%02d:%02d", t3.hour, t3.minute, t4.hour, t4.minute))
            nmsTv3.setText(String.format("%02d:%02d-%02d:%02d", t5.hour, t5.minute, t6.hour, t6.minute))
        }

        nmsLayout1.setOnClickListener { updateSelectedItem(0) }
        nmsLayout2.setOnClickListener { updateSelectedItem(1) }
        nmsLayout3.setOnClickListener { updateSelectedItem(2) }
    }

    private fun updateSelectedItem(position: Int) {
        selectedPosition = position
        val imageRes = fun(p: Int) =
                if (p == position) R.mipmap.nms_ic_meeting_attendee_checked
                else R.mipmap.nms_ic_meeting_attendee_uncheck

        nmsIv1.setImageResource(imageRes(0))
        nmsIv2.setImageResource(imageRes(1))
        nmsIv3.setImageResource(imageRes(2))
    }

}