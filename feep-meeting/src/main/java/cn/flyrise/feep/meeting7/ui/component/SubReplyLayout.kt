package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.meeting7.ui.bean.MeetingReply

/**
 * @author ZYP
 * @since 2018-06-28 15:11
 */
class SubReplyLayout : LinearLayout {

    private var onAttachmentClickListener: ((Attachment, View) -> Unit)? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setOnAttachmentClickListener(itemCLickListener: ((Attachment, View) -> Unit)?) {
        this.onAttachmentClickListener = itemCLickListener
    }

    fun setReplies(replies: List<MeetingReply>) {
        if (CommonUtil.isEmptyList(replies)) return

        removeAllViews()
        replies.forEach {
            val subReplyView = SubReplyView(context)
            subReplyView.setOnAttachmentClickListener(onAttachmentClickListener)
            subReplyView.setReply(it)

            addView(subReplyView)
        }
    }
}