package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.common.AttachmentBeanConverter
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.MeetingReply

/**
 * @author ZYP
 * @since 2018-06-28 14:49
 */
class SubReplyView : LinearLayout {

    private var tvName: TextView
    private var tvTime: TextView
    private var tvContent: TextView
    private var attachmentLayout: AttachmentLayout
    private var onAttachmentClickListener: ((Attachment, View) -> Unit)? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        inflate(context, R.layout.nms_item_meeting_sub_reply, this)
        tvName = findViewById(R.id.nmsTvName)
        tvTime = findViewById(R.id.nmsTvTime)
        tvContent = findViewById(R.id.nmsTvContent)
        attachmentLayout = findViewById(R.id.nmsAttachmentsLayout)
    }

    fun setOnAttachmentClickListener(itemCLickListener: ((Attachment, View) -> Unit)?) {
        this.onAttachmentClickListener = itemCLickListener
    }

    fun setReply(reply: MeetingReply) {
        this.tvName.text = "${reply.sendUser}:"
        this.tvContent.text = reply.content
        this.tvTime.text = "${DateUtil.formatTimeForList(reply.sendTime)}      " +
                "${if (TextUtils.isEmpty(reply.feClient)) "" else "来自" + reply.feClient}"

        if (CommonUtil.isEmptyList(reply.attachments)) {
            this.attachmentLayout.visibility = View.GONE
        } else {
            this.attachmentLayout.visibility = View.VISIBLE
            this.attachmentLayout.setOnItemClickListener(onAttachmentClickListener)
            this.attachmentLayout.setAttachments(AttachmentBeanConverter.convert(reply.attachments))
        }
        this.invalidate()
    }

}