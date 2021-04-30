package cn.flyrise.feep.meeting7.selection.date

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.AbstractSelectionAdapter
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.selection.bean.MSDayDateItem
import cn.flyrise.feep.meeting7.selection.bean.MSMonthDateItem
import cn.flyrise.feep.meeting7.selection.time.normalTextColor
import cn.flyrise.feep.meeting7.selection.time.unableTextColor
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-11 11:31
 */
class DateSelectionAdapter : AbstractSelectionAdapter() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_MONTH) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.nms_item_meeting_month, null)
            return MonthViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.nms_item_meeting_day, null)
            return DayViewHolder(view);
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var msItem = dataSource?.get(position)
        if (holder is MonthViewHolder) {
            msItem as MSMonthDateItem
            holder.tvMonth.text = msItem.getMonth()
            holder.monthLine.visibility = if (position == 0) View.GONE else View.VISIBLE
            return
        }

        holder as DayViewHolder
        msItem as MSDayDateItem

        bindText(holder.tvDay, "${msItem.day}", msItem.state)   // 绑定文本
        bindTextColor(holder.tvDay, msItem)                                   // 绑定文本颜色
        bindTextVisibility(holder.tvDay, msItem)                              // 绑定文本可见性
        bindTextBackground(holder.itemView, msItem)                           // 绑定 item 背景颜色
        bindClickListener(holder.itemView, position, msItem)                  // 绑定 item 点击事件
    }

    override fun getItemViewType(position: Int): Int {
        val item = dataSource?.get(position)
        return if (item is MSMonthDateItem) VIEW_TYPE_MONTH else VIEW_TYPE_DAY
    }

    override fun getSectionColor(msItem: MSDateItem): Int {
        if (msItem is MSDayDateItem) {
            return if (msItem.week == Calendar.SUNDAY || msItem.week == Calendar.SATURDAY) unableTextColor else normalTextColor
        }
        return normalTextColor
    }

    override fun getUnableDrawable(): Int = R.drawable.nms_state_unable

    override fun getUnableEndDrawable(): Int = R.drawable.nms_state_unable_end

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvMonth: TextView
        var monthLine: View

        init {
            tvMonth = itemView.findViewById(R.id.nmsTvMonth)
            monthLine = itemView.findViewById(R.id.nmsMonthSplitLine)
        }
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvDay: TextView

        init {
            tvDay = itemView.findViewById(R.id.nmsTvDay)
        }
    }

}