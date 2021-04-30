package cn.flyrise.feep.workplan7.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailResponse
import cn.flyrise.android.protocol.model.User
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.image.loader.FEImageLoader
import kotlinx.android.synthetic.main.plan_view_detail_head.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class PlanDetailHeadView : RelativeLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.plan_view_detail_head, this)
        ivMoreDetail.setOnClickListener {
            if (lyMoreDetail.visibility == View.VISIBLE) {
                lyMoreDetail.visibility = View.GONE
                tvSenderHint.visibility = View.VISIBLE
                ivMoreDetail.setImageResource(R.drawable.plan_down)
            } else {
                lyMoreDetail.visibility = View.VISIBLE
                tvSenderHint.visibility = View.GONE
                ivMoreDetail.setImageResource(R.drawable.plan_up)
            }
        }
    }

    fun displayHeadContent(detail: WorkPlanDetailResponse) {
        displayUserHead(detail)
        tvTitle.text = detail.title
        tvSenderHint.text = detail.sendUser
        tvSender.text = detail.sendUser
        displayUserContent(detail.receiveUsers, lyReceiver, tvReceiver)
        displayUserContent(detail.ccUsers, lyCCUser, tvCCUser)
        displayUserContent(detail.noticeUsers, lyNotifier, tvNotifier)
        displayTime(detail.startTime, tvStartTime)
        displayTime(detail.endTime, tvEndTime)
    }

    private fun displayUserHead(detail: WorkPlanDetailResponse) {
        CoreZygote.getAddressBookServices().queryUserDetail(detail.sendUserID)
                .subscribe({
                    if (it != null) {
                        FEImageLoader.load(context, ivHead, CoreZygote.getLoginUserServices().serverAddress + it.imageHref, it.userId, it.name)
                    } else {
                        FEImageLoader.load(context, ivHead, R.drawable.administrator_icon)
                    }
                }, {
                    FEImageLoader.load(context, ivHead, R.drawable.administrator_icon)
                })
    }

    private fun displayUserContent(users: List<User>?, layout: LinearLayout, tvContent: TextView) {
        if (CommonUtil.isEmptyList(users)) {
            layout.visibility = View.GONE
            return
        }
        layout.visibility = View.VISIBLE
        val userNames = StringBuilder()
        for (user in users!!) {
            userNames.append(user.name).append(",")
        }
        userNames.deleteCharAt(userNames.length - 1)
        if(TextUtils.isEmpty(userNames)){
            layout.visibility = View.GONE
            return
        }
        tvContent.text = userNames.toString()
    }

    private fun displayTime(time: String, tvTime: TextView) {
        val date = DateUtil.strToDate(time, "yyyy-MM-dd")
        tvTime.text = DateUtil.formatTime(date.time, "yyyy年MM月dd日")
    }
}