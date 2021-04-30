package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.Quantum

/**
 * @author ZYP
 * @since 2018-06-19 15:45
 */
class TimeQuantumView : RelativeLayout {

    private val tvStartTime: TextView
    private val tvEndTime: TextView

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        inflate(context, R.layout.nms_view_time_quantum, this)
        isClickable = true
        isFocusable = true
        tvStartTime = findViewById(R.id.nmsTvBookStartTime)
        tvEndTime = findViewById(R.id.nmsTvBookEndTime)
    }

    fun bindQuantum(quantum: Quantum) {
        this.tvStartTime.setText(quantum.startTime)
        this.tvEndTime.setText(quantum.endTime)
    }

}