package cn.flyrise.feep.meeting7.selection.time

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.component.WheelSelectionView
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-22 15:26
 */
class TimeWheelSelectionFragment : DialogFragment() {

    private var title: String = ""
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0
    private var timeSelectedListener: ((Int, Int) -> Unit)? = null

    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0

    companion object {
        fun newInstance(title: String, year: Int, month: Int, day: Int,
                        hour: Int, minute: Int, selectedListener: (Int, Int) -> Unit): TimeWheelSelectionFragment {
            val instance = TimeWheelSelectionFragment()
            instance.title = title
            instance.year = year
            instance.month = month
            instance.day = day
            instance.hour = hour
            instance.minute = minute
            instance.timeSelectedListener = selectedListener
            return instance
        }
    }

    override fun onStart() {
        super.onStart()
        val heightPixels = context!!.resources.displayMetrics.heightPixels

        val window = dialog.window
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = heightPixels * 2 / 5
        window.attributes = params
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setWindowAnimations(R.style.NMSSelectionDialogAnimation)
        val contentView = inflater?.inflate(R.layout.nms_fragment_wheel_selection, container, false)
        bindView(contentView!!)
        return contentView
    }

    private fun bindView(view: View) {
        view.findViewById<View>(R.id.nmsTvCancel).setOnClickListener {
            dismiss()
        }

        view.findViewById<View>(R.id.nmsTvConfirm).setOnClickListener {
            if (timeSelectedListener != null) {
                timeSelectedListener!!.invoke(selectedHour, selectedMinute)
            }
            dismiss()
        }

        val tvTitle = view.findViewById<TextView>(R.id.nmsTvSelectionTitle)
        tvTitle.text = title

        val datasource = dataSource()
        var position = 0
        val selectedTime = String.format("%02d:%02d", hour, minute)
        if(hour == 0 && minute == 0){
            val d = datasource.get(0).split(":")
            selectedHour = CommonUtil.parseInt(d[0])
            selectedMinute = CommonUtil.parseInt(d[1])
        }else {
            for (i in datasource.indices) {
                if (TextUtils.equals(selectedTime, datasource.get(i))) {
                    val d = datasource.get(i).split(":")
                    position = i
                    selectedHour = CommonUtil.parseInt(d[0])
                    selectedMinute = CommonUtil.parseInt(d[1])
                    break
                }
            }
        }

        val wheelView = view.findViewById<WheelSelectionView>(R.id.nmsWheelSelectionView)
        wheelView.offset = 2
        wheelView.setItems(datasource)
        wheelView.setSeletion(position)
        wheelView.setOnWheelViewListener { selectedIndex, item ->
            val r = item.split(":")
            selectedHour = CommonUtil.parseInt(r[0])
            selectedMinute = CommonUtil.parseInt(r[1])
        }
    }

    private fun dataSource(): MutableList<String> {
        val dataSource = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        var currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        var currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val isToday = currentYear == year && currentMonth == month && currentDay == day

        val needAddToDisplay = fun(h: Int, m: Int): Boolean {
            if (isToday) {
                if (h < currentHour) return false
                if (h == currentHour && m < currentMinute) return false
            }
            return true
        }

        if (needAddToDisplay(0, 30)) dataSource.add("00:30")
        for (i in 1..23) {
            if (needAddToDisplay(i, 0)) dataSource.add(String.format("%02d:00", i))
            if (needAddToDisplay(i, 30)) dataSource.add(String.format("%02d:30", i))
        }
        if (needAddToDisplay(24, 0)) dataSource.add("24:00")
        return dataSource
    }


}