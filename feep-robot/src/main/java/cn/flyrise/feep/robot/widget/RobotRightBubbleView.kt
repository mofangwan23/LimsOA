package cn.flyrise.feep.robot.widget

import android.graphics.*
import android.graphics.drawable.Drawable
import cn.flyrise.feep.core.common.utils.PixelUtil


/**
 * 新建：陈冕;
 * 日期： 2018-5-15-15:50.
 * 箭头朝右的气泡
 */

class RobotRightBubbleView : Drawable() {

    private val mPaint: Paint
    private val mArrowWidth = 15f
    private val mAngle = 140f
    //    private var mArrowPosition: Float
    private val mArrowHeight: Float
    private val rect = RectF()
    private val path = Path()

    init {
        mPaint = Paint()
//        mArrowPosition = PixelUtil.dipToPx(10f).toFloat()
        mArrowHeight = PixelUtil.dipToPx(12f).toFloat()
    }

    override fun draw(canvas: Canvas) {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.strokeWidth = 2f
        mPaint.color = Color.parseColor("#4FC5FF")
        val bounds = bounds
        rect.left = 0f
        rect.top = 0f
        rect.right = bounds.width().toFloat()
        rect.bottom = bounds.height().toFloat()
        canvas.drawPath(setUpRightPath(), mPaint)
    }

    private fun setUpRightPath(): Path {

        val mArrowPosition = rect.bottom - mArrowWidth;

        path.moveTo(rect.left + mAngle, rect.top)
        path.lineTo(rect.width() - mAngle - mArrowWidth, rect.top)
        path.arcTo(RectF(rect.right - mAngle - mArrowWidth, rect.top, rect.right - mArrowWidth, mAngle + rect.top), 270f, 90f)

        path.lineTo(rect.right - mArrowWidth, mArrowPosition)
        path.lineTo(rect.right, mArrowPosition + mArrowHeight)
        path.lineTo(rect.right - mArrowWidth, mArrowPosition + mArrowHeight)

        path.lineTo(rect.right - mArrowWidth, rect.bottom)
        path.arcTo(RectF(rect.right - mArrowWidth, rect.bottom, rect.right - mArrowWidth, rect.bottom), 0f, 90f)

        path.arcTo(RectF(rect.left, rect.bottom - mAngle, mAngle + rect.left, rect.bottom), 90f, 90f)

        path.arcTo(RectF(rect.left, rect.top, mAngle + rect.left, mAngle + rect.top), 180f, 90f)
        path.close()
        return path
    }

    override fun setAlpha(i: Int) {
        mPaint.alpha = i
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}
