package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.graphics.Color
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.flyrise.feep.meeting7.R

/**
 * @author ZYP
 * @since 2018-06-27 09:17
 */

const val STATE_EMPTY = 1
const val STATE_ERROR = 0

class StatusView : LinearLayout {

    private var tvText: TextView
    private var ivImage: ImageView
    private var btnRetry: Button
    private val emptyTextColor = Color.parseColor("#BEEAFF")
    private val errorTextColor = Color.parseColor("#676F73")

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        inflate(context, R.layout.nms_view_empty_status, this)
        tvText = findViewById(R.id.nmsTvEmptyText)
        ivImage = findViewById(R.id.nmsIvEmptyImage)
        btnRetry = findViewById(R.id.nmsBtnRetry)
    }


    fun setText(text: CharSequence) {
        tvText.text = text
    }

    fun setImageResources(@DrawableRes resId: Int) {
        ivImage.setImageResource(resId)
    }

    fun setStatus(status: Int) {
        if (status == STATE_EMPTY) {
            btnRetry.visibility = View.GONE
            tvText.text = "这里什么都没有"
            tvText.setTextColor(emptyTextColor)
            setImageResources(R.mipmap.nms_ic_empty_data)
        } else {
            btnRetry.visibility = View.VISIBLE
            tvText.text = "服务器加载出错"
            tvText.setTextColor(errorTextColor)
            setImageResources(R.mipmap.nms_ic_server_error)
        }
    }

    fun setOnRetryClickListener(clickListener: View.OnClickListener?) {
        btnRetry.setOnClickListener(clickListener)
    }


}