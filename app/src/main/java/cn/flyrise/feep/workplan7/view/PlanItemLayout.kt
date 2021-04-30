package cn.flyrise.feep.workplan7.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.flyrise.feep.R
import kotlinx.android.synthetic.main.plan_view_itemlayout.view.*

/**
 * author : klc
 * data on 2018/6/19 9:25
 * Msg : 计划模块的item 布局
 *       最左边标题，中间是文件内容（默认是右对齐），加一个图片
 */
class PlanItemLayout : LinearLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.plan_view_itemlayout, this)
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.planItemAttr)

        val title = typeArray.getString(R.styleable.planItemAttr_PlanItemAttrTitle)
        if (!title.isNullOrEmpty()) tvTitle.text = title

        val contentHint = typeArray.getString(R.styleable.planItemAttr_PlanItemAttrContentHint)
        if (!contentHint.isNullOrEmpty()) tvContent.hint = contentHint

        val content = typeArray.getString(R.styleable.planItemAttr_PlanItemAttrContent)
        if (!content.isNullOrEmpty()) setContent(content)

        val drawable = typeArray.getDrawable(R.styleable.planItemAttr_planItemRightIcon)
        if (drawable != null) ivRight.setImageDrawable(drawable)


    }

    fun setTitle(title: String) {
        tvTitle.text = title
    }

    fun setContent(content: String) {
        tvContent.text = content
    }

    fun isContentEmpty() = TextUtils.isEmpty(tvContent.text.toString())//内容为空设置初始值

    fun getRightImageView(): ImageView = ivRight

    fun getContentTextView(): TextView = tvContent


}