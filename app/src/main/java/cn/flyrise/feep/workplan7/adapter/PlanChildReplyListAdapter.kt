package cn.flyrise.feep.workplan7.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.android.protocol.model.Reply
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import kotlinx.android.synthetic.main.plan_view_reply_child_item.view.*
import java.text.SimpleDateFormat

class PlanChildReplyListAdapter(val simpleD: SimpleDateFormat?) : RecyclerView.Adapter<PlanChildReplyListAdapter.ChildReplyViewHolder>() {

    var data: List<Reply>? = null

    override fun getItemCount(): Int = if (CommonUtil.isEmptyList(data)) 0 else data!!.size

    override fun onBindViewHolder(holder: ChildReplyViewHolder, position: Int) {
        val view = holder.itemView
        val item = data!![position]
        view.tvName.text = item.sendUser + "："
        view.tvReplyContent.text = item.content
//        view.tvReplyTime.text = dateText(item.sendTime)
        view.tvReplyTime.text = DateUtil.formatTimeForDetail(item.sendTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plan_view_reply_child_item, parent, false)
        return ChildReplyViewHolder(view)
    }

    class ChildReplyViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView)

    private fun dateText(text: String) = simpleD?.format(DateUtil.str2Calendar(text).time) ?: text
}