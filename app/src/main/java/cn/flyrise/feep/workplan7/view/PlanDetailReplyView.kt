package cn.flyrise.feep.workplan7.view

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import cn.flyrise.android.protocol.model.Reply
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.workplan7.adapter.PlanReplyListAdapter
import cn.flyrise.feep.workplan7.listener.PlanReplyListener
import kotlinx.android.synthetic.main.plan_view_detaila_reply.view.*

class PlanDetailReplyView : RelativeLayout {

	constructor(context: Context) : this(context, null)

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
		LayoutInflater.from(context).inflate(R.layout.plan_view_detaila_reply, this)
		replyListView.isNestedScrollingEnabled = false
		replyListView.layoutManager = LinearLayoutManager(context)
	}

	fun displayReply(activity: AppCompatActivity, replys: List<Reply>?,replyClickListener:PlanReplyListener) {
		if (CommonUtil.isEmptyList(replys)) {
			tvCount.text = activity.getString(R.string.plan_detail_reply_others)
			replyListView.visibility = View.GONE
			lyEmpty.visibility = View.VISIBLE
		}
		else {
			tvCount.text = "${activity.getString(R.string.plan_detail_reply_others)}(${replys!!.size})"
			replyListView.visibility = View.VISIBLE
			lyEmpty.visibility = View.GONE
		}
		val adapter = PlanReplyListAdapter(context)
		adapter.data = replys
		adapter.planReplyListener = replyClickListener
		replyListView.adapter = adapter
	}
}