package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.Quantum

/**
 * @author ZYP
 * @since 2018-06-19 15:25
 * 会议室时刻段选择
 */
class TimeQuantumLayout : LinearLayout {

    private val timeQuantumContainer: LinearLayout
    private var maxWidth: Int = 0
    private var timeQuantumClickListener: ((quantum: Quantum) -> Unit)? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate(context, R.layout.nms_layout_time_quantum, this)
        timeQuantumContainer = findViewById(R.id.nmsLayoutTimeQuantum)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.maxWidth = w
    }

    fun setOnTimeQuantumClickListener(timeQuantumClickListener: (quantum: Quantum) -> Unit) {
        this.timeQuantumClickListener = timeQuantumClickListener
    }

    fun bindDataSources(quantums: MutableList<Quantum>) {
        if (maxWidth != 0) {
            invalidateUI(quantums)
            return
        }

        if (maxWidth == 0) {
            postDelayed({ invalidateUI(quantums) }, 500)
        }
    }

    private fun invalidateUI(quantums: MutableList<Quantum>) {
        if (CommonUtil.isEmptyList(quantums)) return
        timeQuantumContainer.removeAllViews()
        if (quantums!!.size == 1) {
            timeQuantumContainer.addView(newQuantumView(quantums!!.get(0), maxWidth))
            invalidate()

            return
        }

        if (quantums!!.size == 2) {
            val halfWidth = maxWidth / 2
            quantums!!.forEach { timeQuantumContainer.addView(newQuantumView(it, halfWidth)) }
            invalidate()
            return
        }

        val threeQuantumWidth = PixelUtil.dipToPx(context, 132f) * 3
        if (quantums!!.size == 3 && maxWidth >= threeQuantumWidth) {
            val oneThirdWidth = maxWidth / 3
            quantums!!.forEach { timeQuantumContainer.addView(newQuantumView(it, oneThirdWidth)) }
            invalidate()
            return
        }


        quantums!!.forEach { timeQuantumContainer.addView(newQuantumView(it, -3)) }
        invalidate()
    }

    private fun newQuantumView(quantum: Quantum, width: Int): TimeQuantumView {
        val quantumView = TimeQuantumView(context)
        if (width != -3) {
            var params = quantumView.layoutParams
            if (params == null) {
                params = RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            else {
                params.width = width
            }
            quantumView.layoutParams = params
        }

        FELog.i("StartTime = " + quantum.startTime + ", EndTime = " + quantum.endTime)
        quantumView.bindQuantum(quantum)
        quantumView.setOnClickListener {
            if (timeQuantumClickListener != null) {
                timeQuantumClickListener!!.invoke(quantum)
            }
        }
        return quantumView
    }


}