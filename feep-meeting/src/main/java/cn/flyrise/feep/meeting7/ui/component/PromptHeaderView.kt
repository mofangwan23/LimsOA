package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import cn.flyrise.feep.meeting7.R

/**
 * @author ZYP
 * @since 2018-06-15 14:41
 */
class PromptHeaderView : LinearLayout {

    val ivClose: ImageView

    constructor(context: Context) : this(context, null)

    constructor(context: Context, atts: AttributeSet?) : this(context, atts, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate(context, R.layout.nms_meeting_prompt_header_view, this)
        ivClose = findViewById(R.id.nmsIvX)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        ivClose.setOnClickListener(l)
    }

}