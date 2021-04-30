package cn.flyrise.feep.meeting7.selection.time

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.AbstractSelectionAdapter
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.selection.bean.MSTimeItem

/**
 * @author ZYP
 * @since 2018-06-13 10:05
 */
class TimeSelectionAdapter : AbstractSelectionAdapter() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msItem = dataSource?.get(position)

        if (holder is ViewHolder && msItem is MSTimeItem) {
            bindText(holder.tvTime, msItem.getTime(), msItem.state)
            bindTextColor(holder.tvTime, msItem)
            bindTextVisibility(holder.tvTime, msItem)
            bindTextBackground(holder.itemView, msItem)
            bindClickListener(holder.itemView, position, msItem)
        }
    }

    override fun getSectionColor(msItem: MSDateItem): Int = normalTextColor

    override fun getUnableDrawable(): Int = R.drawable.nms_state_normal

    override fun getUnableEndDrawable(): Int = R.drawable.nms_state_normal


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.nms_item_meeting_time, null)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvTime: TextView

        init {
            tvTime = itemView.findViewById(R.id.nmsTvTime)
        }
    }

}