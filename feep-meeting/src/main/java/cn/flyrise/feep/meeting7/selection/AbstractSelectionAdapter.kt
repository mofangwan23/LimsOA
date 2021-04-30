package cn.flyrise.feep.meeting7.selection

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.selection.bean.MSMonthDateItem
import cn.flyrise.feep.meeting7.selection.time.*

/**
 * @author ZYP
 * @since 2018-06-13 22:07
 */
abstract class AbstractSelectionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_MONTH = 1
    val VIEW_TYPE_DAY = 2

    var dataSource: List<MSDateItem>? = null
    protected var itemClickListener: ((Int, MSDateItem, View) -> Unit)? = null
    protected var occupyItemClickListener: ((MSDateItem) -> Unit)? = null

    override fun getItemCount(): Int = if (dataSource == null || dataSource!!.isEmpty()) 0 else dataSource!!.size

    fun setOnDateItemClickListener(itemClickListener: (Int, MSDateItem, View) -> Unit) {
        this.itemClickListener = itemClickListener
    }

    fun setOnOccupyDateItemClickListener(listener: (MSDateItem) -> Unit) {
        this.occupyItemClickListener = listener
    }

    fun getItem(position: Int): MSDateItem? = dataSource?.get(position)

    protected fun bindText(textView: TextView, text: String, state: Int) {
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0F); //22SP
    }

    protected fun bindTextVisibility(textView: TextView, msItem: MSDateItem) {
        textView.visibility = when (msItem.state) {
            STATE_BLANK -> View.INVISIBLE
            else -> View.VISIBLE
        }
        if(msItem.day == 0 || msItem.day == -1){
            textView.visibility = View.INVISIBLE
        }
    }

    protected fun bindTextColor(textView: TextView, msItem: MSDateItem) {
        textView.setTextColor(when (msItem.state) {
            STATE_UNABLE, STATE_UNABLE_END, STATE_WEEKEND -> unableTextColor
            STATE_START, STATE_END -> Color.WHITE
            STATE_SECTION -> getSectionColor(msItem)
            STATE_OCCUPY_SECTION -> occupyTextColor
            else -> normalTextColor
        })
    }

    protected fun bindTextBackground(background: View, msItem: MSDateItem) {
        background.setBackgroundResource(when (msItem.state) {
            STATE_UNABLE -> getUnableDrawable()
            STATE_UNABLE_END -> getUnableEndDrawable()

            STATE_START -> R.drawable.nms_state_start_self
            STATE_END -> R.drawable.nms_state_end_self
            STATE_SECTION -> R.drawable.nms_state_section_self

//            STATE_OCCUPY_START -> R.drawable.nms_state_start_other
//            STATE_OCCUPY_END -> R.drawable.nms_state_end_other
            STATE_OCCUPY_SECTION -> R.drawable.nms_state_section_other
//            STATE_OCCUPY_SINGLE -> R.drawable.nms_state_occupy_single

            else -> R.drawable.nms_state_normal
        })
    }

    protected fun bindClickListener(itemView: View, position: Int, msItem: MSDateItem) {
        itemView.setOnClickListener {
            if (msItem is MSMonthDateItem) return@setOnClickListener

            if (msItem.state == STATE_BLANK
                    || msItem.state == STATE_UNABLE
                    || msItem.state == STATE_UNABLE_END) {
                return@setOnClickListener
            }

            if (msItem.state == STATE_OCCUPY_SECTION) {
                if (occupyItemClickListener != null) {
                    occupyItemClickListener!!.invoke(msItem)
                }
                return@setOnClickListener
            }
            if (itemClickListener != null) {
                FELog.i("-->>>>viewTag:" + position)
                it.isFocusable = true
                it.filterTouchesWhenObscured = true
                itemClickListener!!.invoke(position, msItem, it)
            }

        }
    }

    override fun getItemId(position: Int) = position.toLong()

    abstract fun getSectionColor(msItem: MSDateItem): Int

    abstract fun getUnableDrawable(): Int

    abstract fun getUnableEndDrawable(): Int

}