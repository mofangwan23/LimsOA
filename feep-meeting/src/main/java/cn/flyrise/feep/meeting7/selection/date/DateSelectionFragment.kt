package cn.flyrise.feep.meeting7.selection.date

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.meeting7.selection.AbstractSelectionFragment
import cn.flyrise.feep.meeting7.selection.SelectableUIDelegate
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.RoomInfo
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author ZYP
 * @since 2018-06-11 10:51
 * 会议看板，可以选择
 */
class DateSelectionFragment : AbstractSelectionFragment() {
    private var r: RoomInfo? = null
    private lateinit var presenter: DateSelectionPresenter

    companion object {
        fun newInstance(r: RoomInfo?): DateSelectionFragment {
            val instance = DateSelectionFragment()
            instance.r = r
            return instance
        }
    }

    override fun getRoomInfo() = r

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.nms_fragment_date_selection, container, false)
        bindView(view!!)
        return view
    }

    private fun bindView(view: View) {
        recyclerView = view.findViewById(R.id.nmsRecyclerView)
        val gridLayoutManager = GridLayoutManager(activity, 7)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val itemViewType = adapter.getItemViewType(position)
                return if (itemViewType == adapter.VIEW_TYPE_MONTH) 7 else 1
            }
        }

        recyclerView.layoutManager = gridLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        adapter = DateSelectionAdapter()
        adapter.setOnOccupyDateItemClickListener { presenter?.previewUsageInDate(it) }
        this.adapter.setHasStableIds(true)
        recyclerView.adapter = adapter

        val startDate = r?.startDate()
        val endDate = r?.endDate()
        uiDelegate = SelectableUIDelegate(this)
        uiDelegate!!.delegateSelectionUI(recyclerView, adapter)
        uiDelegate!!.setSelectedDate(startDate, endDate)
        uiDelegate!!.setOnDateChangeListener(selectedDateChangeListener)

        presenter = DateSelectionPresenter(r?.roomId!!, this)
        presenter?.setSelectedDate(startDate, endDate)
        presenter?.start()
        
    }

    override fun refreshBoard(dateSource: List<MSDateItem>) {
        adapter.dataSource = dateSource
        adapter.notifyDataSetChanged()
    }


}