package cn.flyrise.feep.meeting7.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.presenter.MeetingRoomPresenter
import cn.flyrise.feep.meeting7.repository.MeetingDataRepository
import cn.flyrise.feep.meeting7.ui.adapter.MeetingRoomAdapter
import cn.flyrise.feep.meeting7.ui.bean.*
import cn.flyrise.feep.meeting7.ui.component.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author ZYP
 * @since 2018-06-19 17:53
 */

interface MeetingRoomView {
    fun onMeetingRoomLoadSuccess(meetingRooms: MutableList<MeetingRoomDescription>, isAppend: Boolean)
    fun onMeetingRoomLoadFailure(isFirstLoad: Boolean)
    fun onMeetingRoomDetailLoaded(roomDetail: MeetingRoomDetailData?)
    fun enableLoadMoreRooms(enable: Boolean)
    fun showLoading()
    fun hideLoading()
}

class MeetingRoomFragment : Fragment(), MeetingRoomView {

    private lateinit var daySelection: DaySelectionView
    private lateinit var statusView: StatusView
    private lateinit var recyclerView: XRecyclerView
    private var adapter: MeetingRoomAdapter? = null

    private var presenter: MeetingRoomPresenter? = null
    private var loadingDialog: FELoadingDialog? = null

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater?.inflate(R.layout.nms_fragment_meeting_room, container, false)
        bindView(contentView!!)
        return contentView
    }

    private fun bindView(view: View) {
        presenter = MeetingRoomPresenter(MeetingDataRepository(), this)
        statusView = view.findViewById(R.id.nmsStatusView)
        daySelection = view.findViewById(R.id.nmsDaySelection)
        daySelection.setDayChangeListener { year, month, day ->
            if (selectedYear == year
                    && selectedMonth == month
                    && selectedDay == day) {
                return@setDayChangeListener
            }

            selectedYear = year
            selectedMonth = month
            selectedDay = day
            presenter?.switchDate(selectedYear, selectedMonth, selectedDay)
        }

        selectedYear = daySelection.getYear()
        selectedMonth = daySelection.getMonth()
        selectedDay = daySelection.getDay()

        recyclerView = view.findViewById(R.id.nmsRecyclerView)
        recyclerView.setPullRefreshEnabled(false)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.itemAnimator = DefaultItemAnimator()
        adapter = MeetingRoomAdapter()
        recyclerView.adapter = adapter

        val footerView = LayoutInflater.from(activity).inflate(
                cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading, recyclerView, false)
        recyclerView.setFooterView(footerView, object : FooterViewCallback.SimpleFooterViewCallback() {
            override fun onSetNoMore(footerView: View?, noMore: Boolean) {
                if (noMore) recyclerView.setLoadingMoreEnabled(false)
            }
        })

        recyclerView.setLoadingListener(object : XRecyclerView.LoadingListener {
            override fun onRefresh() {}
            override fun onLoadMore() {
                presenter?.loadMoreMeetingRoom()
            }
        })

        adapter?.setMeetingRoomDetailFunc { presenter?.loadMeetingRoomDetail(it.roomId) }
        adapter?.setMeetingRoomBookFunc { d, q -> enterToRoomTimeBoard(d, q) }
        adapter?.setMeetingRoomBookPreviewFunc { enterToRoomTimeBoard(it, null) }

        recyclerView.setNoMore(true)
        presenter?.start()
    }

    fun refreshWhenNewMeetingCreate() {
        presenter?.switchDate(selectedYear, selectedMonth, selectedDay)
    }

    private fun enterToRoomTimeBoard(d: MeetingRoomDescription, q: Quantum?) {
        val r = RoomInfo()
        r.roomId = d.roomId
        r.roomName = d.name
        r.startYear = selectedYear
        r.endYear = selectedYear
        r.startMonth = selectedMonth
        r.endMonth = selectedMonth
        r.startDay = selectedDay
        r.endDay = selectedDay

        if (q != null) {
            val st = q!!.startTime.split(":")
            val et = q.endTime.split(":")

            r.startHour = st[0].toInt()
            r.startMinute = st[1].toInt()

            r.endHour = et[0].toInt()
            r.endMinute = et[1].toInt()
        }

        val intent = Intent(activity, TimeSelectionBoardActivity::class.java)
        intent.putExtra("roomInfo", r)
        activity!!.startActivity(intent)
    }


    override fun onMeetingRoomLoadSuccess(meetingRooms: MutableList<MeetingRoomDescription>, isAppend: Boolean) {
        if (isAppend) {
            adapter?.appendDataSource(meetingRooms)
            recyclerView.loadMoreComplete()
        }
        else {
            adapter?.setDataSources(meetingRooms)
            if (CommonUtil.isEmptyList(meetingRooms)) {
                statusView.visibility = View.VISIBLE
                statusView.setStatus(STATE_EMPTY)
            }
            else {
                statusView.visibility = View.GONE
            }
        }
        adapter?.notifyDataSetChanged()
    }

    override fun onMeetingRoomLoadFailure(isFirstLoad: Boolean) {
        FEToast.showMessage(getString(R.string.nms_data_load_failure))
        if (isFirstLoad) {          // 首次加载失败
            statusView.visibility = View.VISIBLE
            statusView.setStatus(STATE_ERROR)
            statusView.setOnRetryClickListener(View.OnClickListener {
                presenter?.start()
            })
            return
        }
    }

    override fun onMeetingRoomDetailLoaded(roomDetail: MeetingRoomDetailData?) {
        if (roomDetail == null) {
            FEToast.showMessage(getString(R.string.nms_data_load_failure))
            return
        }

        RoomDetailDialog.newInstance(roomDetail).show(activity!!.supportFragmentManager, "Dialog")
    }

    override fun enableLoadMoreRooms(enable: Boolean) {
        recyclerView.setNoMore(!enable)
        adapter?.notifyDataSetChanged()
    }

    override fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = FELoadingDialog.Builder(activity).setCancelable(false).create()
        }
        loadingDialog!!.show()
    }

    override fun hideLoading() {
        if (loadingDialog != null && loadingDialog!!.isShowing()) {
            loadingDialog!!.hide()
            loadingDialog = null
        }
    }


}