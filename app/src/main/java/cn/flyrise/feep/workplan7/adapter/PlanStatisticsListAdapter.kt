package cn.flyrise.feep.workplan7.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.workplan7.model.PlanStatisticsListItem
import kotlinx.android.synthetic.main.plan_view_statistics_item.view.*

/**
 * author : klc
 * Msg : 统计规则列表Adapter
 */
class PlanStatisticsListAdapter(context: Context) : BaseRecyclerAdapter() {

    private val mContext: Context = context
    private var data: ArrayList<PlanStatisticsListItem>? = null
    private val weekTexts = context.resources.getStringArray(R.array.plan_rule_weeks)
    var clickListener: ClickListener? = null
    private var types: Array<String>? = null

    init {
        types = context.resources.getStringArray(R.array.plan_types)
    }

    fun setData(data: List<PlanStatisticsListItem>?) {
        if (CommonUtil.isEmptyList(data)) {
            this.data = null
        } else {
            this.data = ArrayList(data)
        }
        this.notifyDataSetChanged()
    }

    fun addData(data: List<PlanStatisticsListItem>?) {
        if (!CommonUtil.isEmptyList(data)) {
            this.data?.addAll(data!!)
            notifyDataSetChanged()
        }
    }

    override fun getDataSourceCount(): Int = if (CommonUtil.isEmptyList(data)) 0 else data!!.size

    override fun onChildBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val view = (holder as ViewHolder).itemView
        val item = data!![position]
        view.tvTitle.text = String.format(mContext.getString(R.string.plan_statistics_type), types!![(item.planType ?: 1) - 1], item.title)
        view.tvSubmitTime.text = getPuleTime(item)
        view.tvSendUser.text = String.format(mContext.getString(R.string.plan_statistics_submit), getUserValue(item.users))
        view.tvUnSubmitCount.text = String.format(mContext.getString(R.string.plan_statistics_no_submit), item.noSumit)
        view.btRemind.isEnabled = item.noSumit!! > 0
        view.btRemind.text = mContext.getString(R.string.plan_classify_remind_hint)
        view.btRemind.setOnClickListener {
            if (item.noSumit!! > 0) clickListener?.onRemindClickListener(item.id!!)
        }
        view.planStatisticsItem.setOnClickListener {
            clickListener?.onItemClickListener(item)
        }
    }

    private fun getPuleTime(item: PlanStatisticsListItem): String {
        val startDate = item.startDate!!.toInt()
        val startTime = item.startTime!!.split(":")[0].toInt()
        val endDate = item.endDate!!.toInt()
        val endTime = item.endTime!!.split(":")[0].toInt()
        return when (item.fqcy!!.toInt()) {
            K.plan.PLAN_FREQUENCY_DAT -> {
                String.format(mContext.getString(R.string.plan_statistics_submit_time), item.startTime
                        , if (endDate == 1 || endTime < startTime) "${mContext.getString(R.string.plan_rule_next_day)}${item.endTime}" else item.endTime)
            }
            K.plan.PLAN_FREQUENCY_WEEK -> {
                String.format(mContext.getString(R.string.plan_statistics_submit_time), "${weekTexts[startDate % 7 - 1]}${item.startTime}"
                        , if (endDate > 7 || endDate < startDate) "${mContext.getString(R.string.plan_rule_next)}${weekTexts[endDate % 7 - 1]}" else weekTexts[endDate % 7 - 1] + item.endTime)
            }
            K.plan.PLAN_FREQUENCY_MONTH -> {
                String.format(mContext.getString(R.string.plan_statistics_submit_time), "${startDate % 31}日 ${item.startTime}"
                        , "${if (endDate > 31 || endDate < startDate) mContext.getString(R.string.plan_rule_next_month) else ""}${startDate % 31}${item.endTime}")
            }
            else -> ""
        }
    }

    private fun getUserValue(ids: String?): String {//提交人最多显示三个人名，多余的省略
        if (ids.isNullOrEmpty()) return ""
        val userLists: MutableList<AddressBook> = CoreZygote.getAddressBookServices().queryUserIds(ids!!.split(",").filterNot { TextUtils.isEmpty(it) })
        return StringBuilder().append(userLists.subList(0, if (userLists.size > 3) 3 else userLists.size)
                .map { it.name }
                .toString()
                .replace("[", "")
                .replace("]", ""))
                .append(if (userLists.size > 3) mContext.getString(R.string.plan_statistics_submit_etc) else "")
                .append(userLists.size)
                .append(mContext.getString(R.string.plan_statistics_submit_personal))
                .toString()
    }

    override fun onChildCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.plan_view_statistics_item, parent, false))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface ClickListener {
        fun onRemindClickListener(id: String)

        fun onItemClickListener(item: PlanStatisticsListItem)
    }
}