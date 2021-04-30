package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import cn.flyrise.feep.meeting7.R

/**
 * @author ZYP
 * @since 2018-07-02 10:42
 * 会议室开始-结束时间提示信息的 PopupWindow
 */
class TimeTipPopupWindow(val context: Context, val displayText: String) : PopupWindow(context) {

    fun show(anchorView: View){
        val contentView = LayoutInflater.from(context).inflate(R.layout.nms_view_time_tips, null, false)
        val tvText = contentView.findViewById<TextView>(R.id.nmsTvTimeTips)
        val ivImage = contentView.findViewById<ImageView>(R.id.nmsIvTimeTipsArrow)
        tvText.text = displayText

        this.width = ViewGroup.LayoutParams.WRAP_CONTENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this.isOutsideTouchable = true
        this.isTouchable = false
        this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        contentView.measure(makeDropDownMeasureSpec(this.width), makeDropDownMeasureSpec(this.height))
        this.contentView = contentView
        var xOffset = 0 // 水平的偏移量怎么计算?
        val suggestOffset = contentView.measuredWidth / 2 - anchorView.measuredWidth / 2
        val screenWidth = context.resources.displayMetrics.widthPixels

        val xy = IntArray(2)
        anchorView.getLocationOnScreen(xy)

        val leftUseableWidth = xy[0]
        val rightUseableWidth = screenWidth - xy[0] - anchorView.measuredWidth

        var params = ivImage.layoutParams as LinearLayout.LayoutParams
        if (leftUseableWidth <= 0) {
            xOffset = 0
            params.rightMargin = suggestOffset
        } else if (rightUseableWidth <= 0) {
            xOffset = 0
            params.leftMargin = suggestOffset
        } else {
            xOffset = -suggestOffset
        }

        val yOffset = -(contentView.measuredHeight + anchorView.measuredHeight)

        showAsDropDown(anchorView, xOffset, yOffset)
    }

    private fun makeDropDownMeasureSpec(measureSpec: Int): Int {
        val mode = if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
            View.MeasureSpec.UNSPECIFIED
        } else {
            View.MeasureSpec.EXACTLY
        }
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            var visibleFrame = Rect()
            anchor!!.getGlobalVisibleRect(visibleFrame)
            var height = anchor.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom
            setHeight(height)
        }
        super.showAsDropDown(anchor, xoff, yoff)

    }

}