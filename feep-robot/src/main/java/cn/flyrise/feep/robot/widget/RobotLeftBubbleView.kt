package cn.flyrise.feep.robot.widget

import android.graphics.*
import android.graphics.drawable.Drawable
import cn.flyrise.feep.core.common.utils.PixelUtil

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-15:50.
 * 箭头朝做的气泡
 */

class RobotLeftBubbleView : Drawable() {

    private val mPaint: Paint
    private val mArrowWidth = 15f
    private val mAngle = 140f
//    private val mArrowPosition: Float
    private val mArrowHeight: Float
    private val rect = RectF()
    private val path = Path()

    init {
        mPaint = Paint()
//        mArrowPosition = PixelUtil.dipToPx(28f).toFloat()
        mArrowHeight = PixelUtil.dipToPx(12f).toFloat()
    }

    override fun draw(canvas: Canvas) {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.strokeWidth = 2f
        mPaint.color = Color.parseColor("#ffffff")
        val bounds = bounds
        rect.left = 0f
        rect.top = 0f
        rect.right = bounds.width().toFloat()
        rect.bottom = bounds.height().toFloat()
        canvas.drawPath(setUpLeftPath(), mPaint)
    }

    private fun setUpLeftPath(): Path {

        val mArrowPosition = rect.bottom  - mArrowWidth;

        path.moveTo(mArrowWidth + rect.left + mAngle, rect.top)
        path.lineTo(rect.width() - mAngle, rect.top)
        path.arcTo(RectF(rect.right - mAngle, rect.top, rect.right, mAngle + rect.top), 270f, 90f)

        path.lineTo(rect.right, rect.bottom - mAngle)
        path.arcTo(RectF(rect.right - mAngle, rect.bottom - mAngle, rect.right, rect.bottom), 0f, 90f)

        path.lineTo(rect.left + mArrowWidth, rect.bottom)
        path.arcTo(RectF(rect.left + mArrowWidth, rect.bottom, rect.left + mArrowWidth, rect.bottom), 90f, 90f)

        path.lineTo(rect.left + mArrowWidth, mArrowHeight + mArrowPosition);
        path.lineTo(rect.left, mArrowPosition + mArrowHeight / 2);
        path.lineTo(rect.left + mArrowWidth, mArrowPosition);

        path.lineTo(rect.left + mArrowWidth, rect.top + mAngle)
        path.arcTo(RectF(rect.left + mArrowWidth, rect.top, mAngle + rect.left + mArrowWidth, mAngle + rect.top), 180f, 90f)
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
