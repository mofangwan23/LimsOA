package cn.flyrise.feep.meeting7.selection.time

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.meeting7.selection.AbstractSelectionFragment
import cn.flyrise.feep.meeting7.selection.SelectableUIDelegate
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.bean.MSFinalAction
import cn.flyrise.feep.meeting7.selection.bean.MSTimeItem
import cn.flyrise.feep.meeting7.ui.bean.RoomInfo
import cn.flyrise.feep.meeting7.ui.component.DaySelectionView

/**
 * @author ZYP
 * @since 2018-06-13 09:42
 */
class TimeSelectionFragment : AbstractSelectionFragment() {

    private lateinit var daySelection: DaySelectionView
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    private var r: RoomInfo? = null
    private var presenter: TimeSelectionPresenter? = null

    companion object {
        fun newInstance(r: RoomInfo?): TimeSelectionFragment {
            val instance = TimeSelectionFragment()
            instance.r = r
            return instance
        }
    }

    override fun getRoomInfo() = r

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.nms_fragment_time_selection, container, false)
        bindView(view!!)
        return view
    }

    private fun bindView(view: View) {
        daySelection = view.findViewById(R.id.nmsDaySelection)
        daySelection.bindSelectedDay(r?.startYear ?: 0, r?.startMonth ?: 0, r?.startDay ?: 0)
        daySelection.setDayChangeListener { year, month, day ->
            if (selectedYear == year
                    && selectedMonth == month
                    && selectedDay == day) {
                return@setDayChangeListener
            }

            selectedYear = year
            selectedMonth = month
            selectedDay = day
            r?.onDateChange(selectedYear, selectedMonth, selectedDay)
            if (selectedDateChangeListener != null) {
                selectedDateChangeListener!!.invoke(r?.startDate(), r?.endDate())
            }
            presenter?.fetchRoomServiceCondition(selectedYear, selectedMonth, selectedDay)
        }

        recyclerView = view.findViewById(R.id.nmsRecyclerView) as RecyclerView
        recyclerView.layoutManager = GridLayoutManager(activity, 6)
        recyclerView.itemAnimator = DefaultItemAnimator()
        adapter = TimeSelectionAdapter()
        adapter.setOnOccupyDateItemClickListener { presenter?.previewUsageInTime(it as MSTimeItem) }
        this.adapter.setHasStableIds(true)
        recyclerView.adapter = adapter

        var startDate = r?.startDate()
        var endDate = r?.endDate()

        if (startDate != null && endDate != null) {
            startDate as MSTimeItem
            endDate as MSTimeItem

            if (startDate.hour == 0 && startDate.minute == 0
                    && endDate.hour == 0 && endDate.minute == 0) {
                startDate = null
                endDate = null
            }
        }

        uiDelegate = SelectableUIDelegate(this).apply {
            delegateSelectionUI(recyclerView, adapter)
            setSelectedDate(startDate, endDate)
            setOnDateChangeListener(selectedDateChangeListener)
            setOnSelectionInterceptListener(selectionInterceptListener)
        }

        presenter = TimeSelectionPresenter(r?.roomId!!, this)
        presenter?.setSelectedDate(startDate, endDate)
        presenter?.start(r?.startYear ?: 0, r?.startMonth ?: 0, r?.startDay ?: 0)
    }

    override fun refreshBoard(dateSource: List<MSDateItem>) {
        adapter.dataSource = dateSource
        adapter.notifyDataSetChanged()

        val startDate = uiDelegate?.startDate()
        val endDate = uiDelegate?.endDate()
        if (startDate == null || endDate == null) return

        var finalIndex = -1
        run loop@{
            adapter.dataSource?.forEachIndexed { index, msDateItem ->
                if (msDateItem.state == STATE_END) {
                    finalIndex = index
                    return@loop
                }
            }
        }

        if (finalIndex == -1) return
        uiDelegate?.setFinalAction(MSFinalAction(finalIndex, startDate, endDate))
        displayTimeTip()
    }

}