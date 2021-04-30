package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import cn.flyrise.feep.core.common.utils.PixelUtil.dipToPx

/**
 * @author ZYP
 * @since 2018-07-02 10:39
 */
class RoomIndicator : View {

    private val paint: Paint
    private val selectColor: Int
    private val normalColor: Int
    private val defaultRadius: Int
    private val indicatorWidth: Int

    private var totalSize = 0
    private var defaultIndex = 0

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        defaultRadius = dipToPx(4f)
        indicatorWidth = dipToPx(30f)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)

        normalColor = Color.parseColor("#E4E6E7")
        selectColor = Color.parseColor("#28B9FF")

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = resources.displayMetrics.widthPixels
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = dipToPx(20f)
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    fun setTotalSize(totalSize: Int) {
        this.totalSize = totalSize
    }

    fun setSelection(index: Int) {
        this.defaultIndex = index
        this.invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (totalSize == 0) return
        val totalIndicatorWidth = indicatorWidth * totalSize
        val xOffset = width / 2 - totalIndicatorWidth / 2

        for (i in 1..totalSize) {
            paint.color = if (i - 1 == defaultIndex) selectColor else normalColor
            val x = xOffset + i * indicatorWidth - indicatorWidth / 2
            canvas?.drawCircle(x.toFloat(), (height / 2).toFloat(), defaultRadius.toFloat(), paint)
        }
    }

}