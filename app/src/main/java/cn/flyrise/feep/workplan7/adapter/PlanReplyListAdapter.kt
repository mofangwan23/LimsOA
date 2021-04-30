package cn.flyrise.feep.workplan7.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.android.protocol.model.Reply
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.workplan7.listener.PlanReplyListener
import kotlinx.android.synthetic.main.plan_view_reply_item.view.*
import java.text.SimpleDateFormat

class PlanReplyListAdapter(val context: Context) : RecyclerView.Adapter<PlanReplyListAdapter.ReplyViewHolder>() {

    var data: List<Reply>? = null
    var planReplyListener: PlanReplyListener? = null
    var simpleD: SimpleDateFormat? = null

    init {
        @SuppressLint("SimpleDateFormat")
        simpleD = SimpleDateFormat(CoreZygote.getContext().getString(cn.flyrise.feep.core.R.string.time_format_yMdhm))
    }

    override fun getItemCount(): Int = if (CommonUtil.isEmptyList(data)) 0 else data!!.size

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val view = holder.itemView
        val item = data!![position]
        val host = CoreZygote.getLoginUserServices().serverAddress
        FEImageLoader.load(context, view.ivHead, host + item.sendUserImageHref, item.sendUserID, item.sendUser)
        view.tvName.text = item.sendUser
        view.tvReplyContent.text = item.content
//        view.tvReplyTime.text = dateText(item.sendTime)
        view.tvReplyTime.text = DateUtil.formatTimeForDetail(item.sendTime)
        val adapter = PlanChildReplyListAdapter(simpleD)
        if (CommonUtil.isEmptyList(item.subReplies)) {
            view.childReplyList.adapter = adapter
            view.childReplyList.visibility = View.GONE
        } else {
            view.childReplyList.adapter = adapter
            adapter.data = item.subReplies
            view.childReplyList.visibility = View.VISIBLE
        }
        view.ivReply.setOnClickListener {
            planReplyListener?.onReply(item.id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plan_view_reply_item, parent, false)
        return ReplyViewHolder(view, context)
    }

    class ReplyViewHolder(containerView: View, context: Context) : RecyclerView.ViewHolder(containerView) {
        init {
            containerView.childReplyList.layoutManager = LinearLayoutManager(context)
            containerView.childReplyList.isNestedScrollingEnabled = false
        }
    }

    private fun dateText(text: String) = if (DateUtil.stringToDateTime(text) != null)
        simpleD?.format(DateUtil.stringToDateTime(text).time) ?: text else text
}