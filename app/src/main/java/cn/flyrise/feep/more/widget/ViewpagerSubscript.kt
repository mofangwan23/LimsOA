package cn.flyrise.feep.more.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.utils.PixelUtil

class ViewpagerSubscript : RelativeLayout {

    private var viewLayout: LinearLayout? = null
    private var mLayoutSelected: View? = null
    private var mParamsSelected: LinearLayout.LayoutParams? = null
    private var mLayouDefault: View? = null
    private var mParamsDefault: LinearLayout.LayoutParams? = null
    private val imageNums = 4

    constructor(mContext: Context) : this(mContext, null)
    constructor(mContext: Context?, set: AttributeSet?) : this(mContext, set, 0)
    constructor(mContext: Context?, set: AttributeSet?, attr: Int) : super(mContext, set, attr) {
        inflate(mContext, R.layout.viewpager_subscript_layout, this)
        viewLayout = this.findViewById(R.id.viewLayout)
        setViewItems(0)
    }

    private fun setViewItems(curr: Int) {
        viewLayout?.removeAllViews()
        for (item in 0..imageNums) {
            if (item == curr) {
                mLayoutSelected = View(context)
                mLayoutSelected?.setBackgroundColor(Color.parseColor("#26B7FF"))
                mParamsSelected = LinearLayout.LayoutParams(PixelUtil.dipToPx(20f), PixelUtil.dipToPx(5f))
                mParamsSelected?.leftMargin = PixelUtil.dipToPx(if (item == 0) 0f else 10f)
                viewLayout?.addView(mLayoutSelected, mParamsSelected)
            } else {
                mLayouDefault = View(context)
                mLayouDefault?.setBackgroundColor(Color.parseColor("#E3E5E6"))
                mParamsDefault = LinearLayout.LayoutParams(PixelUtil.dipToPx(10f), PixelUtil.dipToPx(5f))
                mParamsDefault?.leftMargin = PixelUtil.dipToPx(if (item == 0) 0f else 10f)
                viewLayout?.addView(mLayouDefault, mParamsDefault)
            }
        }
    }

    fun showIndex(index: Int) {
        setViewItems(index)
    }
}