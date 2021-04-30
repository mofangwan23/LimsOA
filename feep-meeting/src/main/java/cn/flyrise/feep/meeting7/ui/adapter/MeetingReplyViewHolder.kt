package cn.flyrise.feep.meeting7.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.component.AttachmentLayout
import cn.flyrise.feep.meeting7.ui.component.SubReplyLayout

/**
 * @author ZYP
 * @since 2018-06-28 15:04
 */
class MeetingReplyViewHolder : RecyclerView.ViewHolder {

    var ivAvatar: ImageView
    var ivReply: ImageView
    var tvName: TextView
    var tvContent: TextView
    var tvTime: TextView
    var attachmentLayout: AttachmentLayout
    var repliesLayout: SubReplyLayout

    constructor(itemView: View) : super(itemView) {
        ivAvatar = itemView.findViewById(R.id.nmsIvAvatar)
        ivReply = itemView.findViewById(R.id.nmsIvReply)

        tvName = itemView.findViewById(R.id.nmsTvName)
        tvTime = itemView.findViewById(R.id.nmsTvTime)
        tvContent = itemView.findViewById(R.id.nmsTvContent)
        attachmentLayout = itemView.findViewById(R.id.nmsAttachmentsLayout)
        repliesLayout = itemView.findViewById(R.id.nmsLayoutSubReply)
    }

}