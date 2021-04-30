package cn.flyrise.feep.location.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.location.Sign
import cn.flyrise.feep.location.bean.SignInLeaderMonthDetail
import cn.flyrise.feep.location.util.LocationBitmapUtil
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * 新建：陈冕;
 * 日期： 2018-5-14-20:39.
 */

class SignInLeaderMonthDetailBar : BaseSuspensionBar {

    private val mTvUserName: TextView
    private val mTvUserDepart: TextView
    private val mSuspensionBarSum: TextView
    private val mImgUserIcon: ImageView
    private val mSuspensionBarIcon: ImageView
    private var days = mutableListOf<Int>()//单位是天的
    private var mContext: Context? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        days.addAll(Sign.state.suffixDays)
        LayoutInflater.from(mContext).inflate(R.layout.location_leader_month_summary_detail_bar, this)
        mSuspensionBar = this.findViewById(R.id.location_summar_head)
        mImgUserIcon = this.findViewById(R.id.user_icon)
        mTvUserName = this.findViewById(R.id.user_name)
        mTvUserDepart = this.findViewById(R.id.user_department)
        mSuspensionBarSum = this.findViewById(R.id.item_sum)
        mSuspensionBarIcon = this.findViewById(R.id.head_right_icon)
    }

    fun updateSuspensionBar(item: SignInLeaderMonthDetail?, type: Int?) {
        if (item == null) return
        CoreZygote.getAddressBookServices().queryUserDetail(item.userId)
                .subscribe({
                    if (it != null) {
                        mTvUserName.text = it.name
                        mTvUserDepart.text = it.deptName
                        FEImageLoader.load(mContext, mImgUserIcon, CoreZygote.getLoginUserServices().serverAddress + it.imageHref, it.userId, it.name)
                    } else {
                        FEImageLoader.load(mContext, mImgUserIcon, R.drawable.administrator_icon)
                    }
                }, {
                    FEImageLoader.load(mContext, mImgUserIcon, R.drawable.administrator_icon)
                })

//        val addressBook = CoreZygote.getAddressBookServices().queryUserInfo(item.userId)

        setSummaryTextColor(item)
        mSuspensionBarSum.text = String.format(resources.getString(if (days.contains(type))
            R.string.location_month_summary_day
        else
            R.string.location_month_summary_second), if (CommonUtil.isEmptyList(item.dateItems)) "0" else item.dateItems.size.toString() + "")
        setSummaryIconColor(item)
    }

    private fun setSummaryIconColor(item: SignInLeaderMonthDetail) {
        if (CommonUtil.isEmptyList(item.dateItems)) {//灰色
            mSuspensionBarIcon.setImageBitmap(LocationBitmapUtil.tintBitmap(mContext, R.drawable.icon_address_filter_down, Color.parseColor("#CDCDCD")))
            return
        }
        if (item.isSwitch) {//向上
            mSuspensionBarIcon.setImageBitmap(LocationBitmapUtil.rotateBitmap(mContext, R.drawable.icon_address_filter_down))
            return
        }
        mSuspensionBarIcon.setImageDrawable(resources.getDrawable(R.drawable.icon_address_filter_down))
    }

    private fun setSummaryTextColor(item: SignInLeaderMonthDetail) {
        mSuspensionBarSum.setTextColor(Color.parseColor(if (CommonUtil.isEmptyList(item.dateItems)) "#CDCDCD" else "#8B8C8C"))
    }

}
