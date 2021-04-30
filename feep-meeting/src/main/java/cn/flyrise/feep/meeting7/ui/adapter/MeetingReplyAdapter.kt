package cn.flyrise.feep.meeting7.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.common.AttachmentBeanConverter
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.MeetingReply
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author ZYP
 * @since 2018-06-28 15:19
 */
class MeetingReplyAdapter : RecyclerView.Adapter<MeetingReplyViewHolder>() {

    private var dataSource: List<MeetingReply>? = null
    private var host: String
    private var context: Context

    init {
        context = CoreZygote.getContext()
        host = CoreZygote.getLoginUserServices().serverAddress
    }

    fun setDataSources(dataSource: List<MeetingReply>) {
        this.dataSource = dataSource
        this.notifyDataSetChanged()
    }

    private var replyClickListener: ((MeetingReply) -> Unit)? = null
    fun setOnReplyClickListener(replyListener: ((MeetingReply) -> Unit)?) {
        this.replyClickListener = replyListener
    }

    private var attachmentClickListener: ((Attachment, View) -> Unit)? = null
    fun setOnAttachmentClickListener(attachmentClickListener: ((Attachment, View) -> Unit)?) {
        this.attachmentClickListener = attachmentClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MeetingReplyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.nms_item_meeting_reply, parent, false))

    override fun getItemCount(): Int {
        var count = 0
        if (CommonUtil.isEmptyList(dataSource)) {
            count = 0
        } else {
            count = dataSource!!.size
        }
        return count
    }

    override fun onBindViewHolder(holder: MeetingReplyViewHolder, position: Int) {
        val reply = dataSource!!.get(position)

        CoreZygote.getAddressBookServices().queryUserDetail(reply.sendUserID)
                .subscribe({
                    if (it != null) {
                        FEImageLoader.load(context, holder.ivAvatar, host + it.imageHref, it.userId, it.name)
                    } else {
                        FEImageLoader.load(context, holder.ivAvatar, host + reply.sendUserImg, reply.sendUserID, reply.sendUser)
                    }
                }, {
                    FEImageLoader.load(context, holder.ivAvatar, host + reply.sendUserImg, reply.sendUserID, reply.sendUser)
                })

        holder.tvName.text = reply.sendUser
        holder.tvContent.text = reply.content
        holder.tvTime.text = "${DateUtil.formatTimeForList(reply.sendTime)}      " +
                "${if (TextUtils.isEmpty(reply.feClient)) "" else "来自" + reply.feClient}"

        if (CommonUtil.isEmptyList(reply.attachments)) {
            holder.attachmentLayout.visibility = View.GONE
        } else {
            holder.attachmentLayout.visibility = View.VISIBLE
            holder.attachmentLayout.setOnItemClickListener(attachmentClickListener)
            holder.attachmentLayout.setAttachments(AttachmentBeanConverter.convert(reply.attachments))
        }

        if (CommonUtil.isEmptyList(reply.subReplies)) {
            holder.repliesLayout.visibility = View.GONE
        } else {
            holder.repliesLayout.visibility = View.VISIBLE
            holder.repliesLayout.setOnAttachmentClickListener(attachmentClickListener)
            holder.repliesLayout.setReplies(reply.subReplies)
        }

        holder.ivReply.setOnClickListener {
            if (replyClickListener != null) {
                replyClickListener!!.invoke(reply)
            }
        }

    }

}