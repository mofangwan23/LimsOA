package cn.flyrise.feep.location.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.location.Sign
import cn.flyrise.feep.location.bean.SignInMonthStatisItem
import cn.flyrise.feep.location.util.LocationBitmapUtil
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-14-20:39.
 */

class SignInMonthStatisBar : BaseSuspensionBar {

    private val mSuspensionBarTitle: TextView
    private val mSuspensionBarSum: TextView
    private val mSuspensionBarIcon: ImageView
    private var days = ArrayList<Int>()//单位是天的
    private var redItems = ArrayList<Int>()//红字
    private var mContext: Context? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        days.addAll(Sign.state.suffixDays)
        redItems.addAll(Sign.state.redFonts)
        LayoutInflater.from(mContext).inflate(R.layout.location_month_summary_bar, this)
        mSuspensionBar = this.findViewById(R.id.location_summar_head)
        mSuspensionBarTitle = this.findViewById(R.id.item_title)
        mSuspensionBarSum = this.findViewById(R.id.item_sum)
        mSuspensionBarIcon = this.findViewById(R.id.head_right_head_icon)
    }

    fun updateSuspensionBar(item: SignInMonthStatisItem?) {
        if (item == null) return
        mSuspensionBarTitle.text = item.sumTitle
        setSummaryTextColor(item)
        mSuspensionBarSum.text = String.format(resources.getString(if (days.contains(item.sumId))
            R.string.location_month_summary_day
        else
            R.string.location_month_summary_second), if (CommonUtil.isEmptyList(item.subItems)) "0" else item.subItems.size.toString() + "")
        setSummaryIconColor(item)
    }

    private fun setSummaryIconColor(item: SignInMonthStatisItem) {
        if (CommonUtil.isEmptyList(item.subItems)) {//灰色
            mSuspensionBarIcon.setImageBitmap(LocationBitmapUtil.tintBitmap(mContext, R.drawable.icon_address_filter_down, Color.parseColor("#CDCDCD")))
            return
        }
        if (item.isSwitch) {//向上
            mSuspensionBarIcon.setImageBitmap(LocationBitmapUtil.rotateBitmap(mContext, R.drawable.icon_address_filter_down))
            return
        }
        mSuspensionBarIcon.setImageDrawable(resources.getDrawable(R.drawable.icon_address_filter_down))
    }

    private fun setSummaryTextColor(item: SignInMonthStatisItem) {
        if (CommonUtil.isEmptyList(item.subItems)) {
            mSuspensionBarSum.setTextColor(Color.parseColor("#CDCDCD"))
            return
        }
        mSuspensionBarSum.setTextColor(if (redItems.contains(item.sumId))
            Color.parseColor("#E60026")
        else
            Color.parseColor("#191919"))
    }
}
