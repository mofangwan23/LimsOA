package cn.flyrise.feep.meeting7.selection

import android.support.v7.widget.RecyclerView
import android.view.View
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.selection.bean.MSFinalAction
import cn.flyrise.feep.meeting7.selection.bean.MSMonthDateItem
import cn.flyrise.feep.meeting7.selection.bean.MSTimeItem
import cn.flyrise.feep.meeting7.selection.memo.Caretaker
import cn.flyrise.feep.meeting7.selection.memo.Memorable
import cn.flyrise.feep.meeting7.selection.time.*
import cn.flyrise.feep.meeting7.ui.component.TimeTipPopupWindow

/**
 * @author ZYP
 * @since 2018-06-13 21:49
 */

interface SelectableUI {
    fun promptDateOccupy()                  // 提醒选择的日期已经被占用
}

class SelectableUIDelegate(val selectionUI: SelectableUI) {

    private var caretaker = Caretaker()
    private var startDate: MSDateItem? = null
    private var endDate: MSDateItem? = null
    private var finalAction: MSFinalAction? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AbstractSelectionAdapter

    private var dateChangeListener: ((MSDateItem?, MSDateItem?) -> Unit)? = null
    private var selectionInterceptListener: ((List<MSDateItem>) -> Unit)? = null

    private var popupWindow: TimeTipPopupWindow? = null

    fun getFinalAction(): MSFinalAction? = finalAction

    fun setFinalAction(finalAction: MSFinalAction) {
        this.finalAction = finalAction
    }

    fun setOnDateChangeListener(listener: ((MSDateItem?, MSDateItem?) -> Unit)?) {
        this.dateChangeListener = listener
    }

    fun setOnSelectionInterceptListener(listener: ((List<MSDateItem>) -> Unit)?) {
        this.selectionInterceptListener = listener
    }

    fun setSelectedDate(startDate: MSDateItem?, endDate: MSDateItem?) {
        this.startDate = startDate
        this.endDate = endDate
    }

    fun delegateSelectionUI(recyclerView: RecyclerView, adapter: AbstractSelectionAdapter) {
        if (recyclerView == null || adapter == null) {
            throw NullPointerException("Please init the RecyclerView and Adapter first.") as Throwable
        }

        this.recyclerView = recyclerView
        this.adapter = adapter
        // 处理点击事件
        this.adapter.setOnDateItemClickListener itemClick@{ position, msItem, anchroView ->
            if (startDate != null && endDate != null) {
                // 恢复操作
                restoreSectionState(true)
            }

            // 记录开始节点
            if (startDate == null) {
                if (isInterceptSelectionEvent(msItem, position)) {
                    interceptSelectionEvent(msItem, position)
                    return@itemClick
                }
                caretaker.recordStartState((msItem as Memorable).createMemento(position))
                msItem.state = STATE_START
                startDate = msItem
                adapter.notifyItemChanged(position)
                popupStartTip(anchroView)
                if (dateChangeListener != null) {
                    dateChangeListener!!.invoke(startDate, null)
                }
                return@itemClick
            }

            val startState = caretaker.getStartState()

            // 重置开始节点
            if (position < startState!!.position) {
                (startDate as Memorable).restoreState(startState)
                adapter.notifyItemChanged(startState.position)

                if (isInterceptSelectionEvent(msItem, position)) {
                    interceptSelectionEvent(msItem, position)
                    return@itemClick
                }
                caretaker.recordStartState((msItem as Memorable).createMemento(position))
                msItem.state = STATE_START
                startDate = msItem
                adapter.notifyItemChanged(position)
                popupStartTip(anchroView)
                if (dateChangeListener != null) {
                    dateChangeListener!!.invoke(startDate, null)
                }
                return@itemClick
            }

            if (startState!!.position == position) {    // 点击的是同一个
                startDate = null
                msItem.state = STATE_NORMAL
                adapter.notifyItemChanged(position)
                return@itemClick
            }

            // 记录结束节点
            caretaker.recordEndState((msItem as Memorable).createMemento(position))
            msItem.state = STATE_END
            endDate = msItem
            adapter.notifyItemChanged(position)

            // 记录中间部分的数据
            var hasOccupy = false
            for (i in startState.position + 1..position - 1) {
                val sectionItem = adapter.getItem(i)!!
                if (sectionItem is MSMonthDateItem) continue
                if (sectionItem.state == STATE_BLANK) continue
                if (sectionItem.state == STATE_OCCUPY_SECTION) {
                    hasOccupy = true
                    break
                }

                caretaker.recordSectionState((sectionItem as Memorable).createMemento(i))
                sectionItem.state = STATE_SECTION
            }

            if (hasOccupy) {
                selectionUI.promptDateOccupy()
                restoreSectionState(false)
                if (dateChangeListener != null) {
                    dateChangeListener!!.invoke(null, null)
                }
                return@itemClick
            }

            adapter.notifyItemRangeChanged(startState.position + 1, position - startState.position - 1)
            popupEndTip(anchroView, startDate!!, endDate!!)
            setFinalAction(MSFinalAction(position, startDate!!, endDate!!))
            if (dateChangeListener != null) {
                dateChangeListener!!.invoke(startDate, endDate)
            }
        }
    }

    private fun isInterceptSelectionEvent(msItem: MSDateItem, position: Int): Boolean {
        // 是否需要对选择事件进行拦截
        if (msItem is MSTimeItem) {
            if (position == 0) {
                // 第 0 个元素。检查下一个元素是否被占用，如果是，拦截他妈的
                val nextItem = adapter.getItem(position + 1)
                if (nextItem?.state == STATE_OCCUPY_SECTION) {
                    return true
                }
            } else if (position == adapter.itemCount - 1) {
                // 最后一个元素，检查上一个是否被占用
                val preItem = adapter.getItem(position - 1)
                if (preItem?.state == STATE_OCCUPY_SECTION) {
                    return true
                }
            } else {
                val preItem = adapter.getItem(position - 1)
                val nextItem = adapter.getItem(position + 1)

                if ((preItem?.state == STATE_UNABLE || preItem?.state == STATE_UNABLE_END || preItem?.state == STATE_OCCUPY_SECTION)
                        && nextItem?.state == STATE_OCCUPY_SECTION) {
                    return true
                }
            }
        }
        return false
    }

    private fun interceptSelectionEvent(msItem: MSDateItem, position: Int) {
        var preItem: MSDateItem? = null
        var nextItem: MSDateItem? = null
        if (position > 0 && position < adapter.itemCount - 1) {
            preItem = adapter.getItem(position - 1)
            nextItem = adapter.getItem(position + 1)
        } else if (position == adapter.itemCount - 1) {
            preItem = adapter.getItem(position - 1)
        } else if (position == 0) {
            nextItem = adapter.getItem(position + 1)
        }

        val datePair = mutableListOf<MSDateItem>().apply {
            if (preItem != null && nextItem != null) {
                add(preItem)
                add(msItem)
                add(msItem)
                add(nextItem!!)
                add(preItem)
                add(nextItem)
                return@apply
            }

            if (preItem != null && nextItem == null) {
                add(preItem)
                add(msItem)
            }

            if (nextItem != null && preItem == null) {
                add(msItem)
                add(nextItem)
            }
        }

        if (selectionInterceptListener != null) {
            selectionInterceptListener!!.invoke(datePair)
        }
    }

    private fun popupStartTip(anchorView: View) {
        if (popupWindow != null) popupWindow!!.dismiss()
        popupWindow = TimeTipPopupWindow(anchorView.context, "请选择会议结束时间")
        popupWindow!!.show(anchorView)
    }

    fun popupEndTip(anchorView: View, s: MSDateItem, e: MSDateItem) {
        if (popupWindow != null) popupWindow!!.dismiss()
        if (s is MSTimeItem && e is MSTimeItem) {
            val hours = hourBetween(s.year, s.month, s.day, s.hour, s.minute, e.hour, e.minute)
            popupWindow = TimeTipPopupWindow(anchorView.context, String.format("共 %.1f 小时", hours))
            popupWindow!!.show(anchorView)
            return
        }

        val days = daysBetween(s.year, s.month, s.day, e.year, e.month, e.day)
        popupWindow = TimeTipPopupWindow(anchorView.context, "共 ${days + 1} 天")
        popupWindow!!.show(anchorView)
    }

    private fun restoreSectionState(withStartState: Boolean) {
        val startState = caretaker.getStartState()
        val endState = caretaker.getEndState()

        if (startState == null && endState == null) {
            restoreToOriginal()
            return
        }

        if (withStartState) {
            (startDate as Memorable).restoreState(startState!!)
            startDate = null
        }

        (endDate as Memorable).restoreState(endState!!)
        endDate = null

        caretaker.getSectionStates().forEach {
            (adapter.getItem(it.position) as Memorable).restoreState(it)
        }

        caretaker.restoreToOriginalState(withStartState)
        adapter.notifyItemRangeChanged(startState!!.position, endState.position - startState.position + 1)
    }

    private fun restoreToOriginal() {
        this.startDate = null
        this.endDate = null
        if (CommonUtil.isEmptyList(adapter.dataSource)) {
            return
        }

        adapter.dataSource!!.forEach {
            if (it.state == STATE_START
                    || it.state == STATE_END
                    || it.state == STATE_SECTION) {
                it.state = STATE_NORMAL
            }
        }

        adapter.notifyDataSetChanged()
    }

    fun startDate() = startDate
    fun endDate() = endDate

    fun popupDismiss() {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
        }
    }
}