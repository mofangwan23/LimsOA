package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.media.attachments.bean.Attachment
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-21 16:24
 */
class AttachmentLayout : ViewGroup {

    private val lineHeights = mutableListOf<Int>()
    private val attachmentViews = mutableListOf<MutableList<View>>()
    private var itemClickListener: ((Attachment, View) -> Unit)? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        var sizeHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val modeHeight = View.MeasureSpec.getMode(heightMeasureSpec)

        var maxHeight = 0
        var lineWidth = 0
        var lineHeight = 0
        val cCount = childCount

        if (cCount == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        for (i in 0..cCount - 1) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
//            val lp = child.layoutParams

            val childWidth = child.measuredWidth  // + lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight // + lp.topMargin + lp.bottomMargin

            if (lineWidth + childWidth > sizeWidth) {
                lineWidth = childWidth
                maxHeight += lineHeight
                lineHeight = childHeight
            }
            else {
                lineWidth += childWidth
                lineHeight = Math.max(lineHeight, childHeight)
            }

            if (i == cCount - 1) {
                maxHeight += lineHeight
            }
        }
        setMeasuredDimension(sizeWidth, if (modeHeight == View.MeasureSpec.EXACTLY) sizeHeight else maxHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        attachmentViews.clear()
        lineHeights.clear()

        val width = width
        var lineWidth = 0
        var lineHeight = 0

        var lineViews: MutableList<View> = ArrayList()
        val cCount = childCount
        if (cCount == 0) {
            return
        }

        for (i in 0..cCount - 1) {
            val child = getChildAt(i)
            val lp = child.layoutParams // as ViewGroup.MarginLayoutParams
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            if (childWidth + lineWidth > width) {
                lineHeights.add(lineHeight)
                attachmentViews.add(lineViews)
                lineWidth = 0
                lineViews = ArrayList()
            }

            lineWidth += childWidth
            lineHeight = Math.max(lineHeight, childHeight)
            lineViews.add(child)
        }
        lineHeights.add(lineHeight)
        attachmentViews.add(lineViews)

        var left = 0
        var top = 0
        val lineNums = attachmentViews.size
        for (i in 0..lineNums - 1) {
            lineViews = attachmentViews.get(i)
            lineHeight = lineHeights.get(i)

            for (j in lineViews.indices) {
                val child = lineViews[j]
                if (child.visibility == View.GONE) {
                    continue
                }
//                val lp = child.layoutParams // as ViewGroup.MarginLayoutParams
//
                val lc = left // + lp.leftMargin
                val tc = top  // + lp.topMargin
                val rc = lc + child.measuredWidth
                val bc = tc + child.measuredHeight

                child.layout(lc, tc, rc, bc)
                left += child.measuredWidth  // + lp.rightMargin + lp.leftMargin
            }
            left = 0
            top += lineHeight
        }
    }

    fun setOnItemClickListener(itemClickLIstener: ((Attachment, View) -> Unit)?) {
        this.itemClickListener = itemClickLIstener
    }

    fun setAttachments(attachments: List<Attachment>) {
        if (CommonUtil.isEmptyList(attachments)) return
        removeAllViews()

        attachments.forEach { attachment ->
            var attachmentView = AttachmentView.obtain(context, attachment)
            attachmentView.setOnClickListener {
                if (itemClickListener != null) {
                    itemClickListener!!.invoke(attachment, attachmentView)
                }
            }
            addView(attachmentView, childCount - 1)
        }

        this.invalidate()
    }
}