package cn.flyrise.feep.meeting7.ui.adapter

import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment
import cn.flyrise.feep.media.common.AttachmentUtils
import cn.flyrise.feep.media.common.FileCategoryTable
import cn.flyrise.feep.meeting7.R


class MeetingAttachmentAdapter : RecyclerView.Adapter<MeetingAttachmentAdapter.ViewHolder>() {

    private var attachments: MutableList<Attachment>? = null

    private var deleteFunc: ((Attachment) -> Unit)? = null
    private var previewFunc: ((Attachment) -> Unit)? = null

    fun setAttachments(a: MutableList<Attachment>?) {
        this.attachments = a
        this.notifyDataSetChanged()
    }

    fun setDeleteFunc(func: ((Attachment) -> Unit)?) {
        this.deleteFunc = func
    }

    fun setPreviewFunc(func: ((Attachment) -> Unit)) {
        this.previewFunc = func
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.nms_item_meeting_attachments, parent, false))

    override fun getItemCount() = attachments?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attachment = attachments!!.get(position)
        holder.ivThumbnail.setImageResource(FileCategoryTable.getIcon(attachment.type))
        holder.tvAttachmentName.setText(attachment.name)

        if (attachment is NetworkAttachment) {
            val attachmentFile = AttachmentUtils.getDownloadedAttachment(attachment)
            if (attachmentFile != null) {
                holder.tvAttachmentSize.visibility = View.VISIBLE
                holder.tvAttachmentSize.text = Formatter.formatFileSize(CoreZygote.getContext(), attachmentFile.length())
            } else {
                holder.tvAttachmentSize.visibility = View.GONE
            }
        } else {
            holder.tvAttachmentSize.visibility = View.VISIBLE
            holder.tvAttachmentSize.text = Formatter.formatFileSize(CoreZygote.getContext(), attachment.size)
        }

        holder.ivDeleteIcon.setOnClickListener { deleteFunc?.invoke(attachment) }
        holder.itemView.setOnClickListener { previewFunc?.invoke(attachment) }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivThumbnail: ImageView
        var ivDeleteIcon: ImageView
        var tvAttachmentName: TextView
        var tvAttachmentSize: TextView

        init {
            ivThumbnail = itemView.findViewById(R.id.nmsIvIcon)
            ivDeleteIcon = itemView.findViewById(R.id.nmsIvX)
            tvAttachmentName = itemView.findViewById(R.id.nmsTvName)
            tvAttachmentSize = itemView.findViewById(R.id.nmsTvSize)
        }
    }
}