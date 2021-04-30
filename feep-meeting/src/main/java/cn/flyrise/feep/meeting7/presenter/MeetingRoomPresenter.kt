package cn.flyrise.feep.meeting7.presenter

import android.content.Intent
import cn.flyrise.feep.meeting7.repository.MeetingDataRepository
import cn.flyrise.feep.meeting7.ui.MeetingRoomView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-26 14:50
 */
class MeetingRoomPresenter(val repository: MeetingDataRepository, val roomView: MeetingRoomView) {


    private var currentPage = 1
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    fun start() {
        val c = Calendar.getInstance()
        selectedYear = c.get(Calendar.YEAR)
        selectedMonth = c.get(Calendar.MONTH)
        selectedDay = c.get(Calendar.DAY_OF_MONTH)

        currentPage = 1
        obtainMeetingRoomData(getSelectedDateString(), currentPage)
    }

    fun switchDate(year: Int, month: Int, day: Int) {
        selectedYear = year
        selectedMonth = month
        selectedDay = day
        currentPage = 1
        obtainMeetingRoomData(getSelectedDateString(), currentPage)
    }

    fun loadMoreMeetingRoom() {
        currentPage++
        obtainMeetingRoomData(getSelectedDateString(), currentPage)
    }

    fun loadMeetingRoomDetail(roomId: String) {
        repository.requestMeetingRoomDetail(roomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { roomView.onMeetingRoomDetailLoaded(it) }
    }

    private fun obtainMeetingRoomData(meetingRoomInDate: String, page: Int) {
        if (page == 1) roomView.showLoading()

        repository.requestMeetingRoomList(meetingRoomInDate, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (page == 1) roomView.hideLoading()
                    roomView.enableLoadMoreRooms(it.hasNextPage)
                    roomView.onMeetingRoomLoadSuccess(it.meetingRooms, page > 1)
                }, {
                    it.printStackTrace()
                    if (page > 1) currentPage--
                    if (page == 1) {
                        roomView.hideLoading()
                        roomView.enableLoadMoreRooms(false)
                    }
                    roomView.onMeetingRoomLoadFailure(page == 1)
                })
    }

    private fun getSelectedDateString() = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
}