package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.meeting7.R

/**
 * @author ZYP
 * @since 2018-06-21 17:00
 */
class AttachmentView : LinearLayout {

    companion object {
        fun obtain(context: Context, attachment: Attachment): AttachmentView {
            val attachmentView = AttachmentView(context)
            attachmentView.setAttachment(attachment)
            return attachmentView
        }
    }

    private var tvAttachment: TextView
    private var attachment: Attachment? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate(context, R.layout.nms_item_meeting_attachment, this)
        tvAttachment = findViewById(R.id.nmsTvAttachmentName)
    }

    private fun setAttachment(attachment: Attachment) {
        this.attachment = attachment
        this.tvAttachment.setText(attachment.name)
        this.invalidate()
    }

}