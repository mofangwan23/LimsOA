package cn.flyrise.feep.location.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import cn.flyrise.feep.R
import cn.flyrise.feep.location.util.LocationBitmapUtil
import kotlinx.android.synthetic.main.sign_in_main_tab_text_icon_layout.view.*

/**
 * 新建：陈冕;
 *日期： 2018-7-30-10:52.
 */
class SignInMenuView : LinearLayout {

    private var selectedColor: String = "#28B9FF"
    private var defaultColor: String = "#9DA3A6"
    private var menuImg: Int?
    private var text: String?
    private var mContext: Context?
    private var isSelected: Boolean? = false

    constructor(context: Context) : this(context, null)

    @SuppressLint("Recycle")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.sign_in_main_tab_text_icon_layout, this)
        this.mContext = context
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.SignInMenuView)
        text = typeArray.getString(R.styleable.SignInMenuView_menuText)
        menuImg = typeArray.getResourceId(R.styleable.SignInMenuView_menuIcon, 0)
        selectedColor = typeArray.getString(R.styleable.SignInMenuView_selectedColor)
        defaultColor = typeArray.getString(R.styleable.SignInMenuView_defaultColor)
        menuText.text = text
        setSelectedMenu(false)
    }

    fun setSelectedMenu(selected: Boolean) {
        isSelected = selected
        setColor()
    }

    private fun setColor() {
        val color = Color.parseColor(if (isSelected!!) selectedColor else defaultColor)
        menuIcon.setImageBitmap(LocationBitmapUtil.tintBitmap(mContext, menuImg!!, color))
        menuText.setTextColor(color)
    }

}