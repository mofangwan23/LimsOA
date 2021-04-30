package cn.flyrise.feep.meeting7.presenter

import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.meeting7.selection.time.SP_HIDE_PULL_DOWN_PROMPT
import cn.flyrise.feep.meeting7.protocol.MeetingListRequest
import cn.flyrise.feep.meeting7.protocol.MeetingListResponse
import cn.flyrise.feep.meeting7.repository.MeetingDataRepository
import cn.flyrise.feep.meeting7.ui.MineMeetingView
import cn.flyrise.feep.meeting7.ui.bean.MeetingDescription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author ZYP
 * @since 2018-06-25 10:38
 */
class MineMeetingPresenter(val meetingType: String,
                           val mineView: MineMeetingView,
                           val repository: MeetingDataRepository) {

    private var untreated = 0           // 未读数
    private var totalPage = 0           // 往下总页数
    private var totalForwardPage = 0    // 往上总页数

    private var currentPage = 1         // 当前页码
    private var currentForwardPage = 0  // 当前往上页码

    private lateinit var loadDataRequest: MeetingListRequest
    private var loadStaleDataRequest: MeetingListRequest? = null
    private var recentlyMeetingId: String? = null

    fun start() {
        loadDataRequest = MeetingListRequest()
        loadDataRequest.meetingType = meetingType
        loadDataRequest.isOver = false
        loadDataRequest.page = currentPage

        currentPage = 1             // 数据刷新
        currentForwardPage = 0

        val hidePullDownPrompt = SpUtil.get(SP_HIDE_PULL_DOWN_PROMPT, false)
        mineView.showLoading()
        repository.requestMeetingList(loadDataRequest)
                .map { mapAndRecordMeetingListInfo(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (CommonUtil.nonEmptyList(it)) {
                        recentlyMeetingId = it.get(0).meetingId
                    }
                }
                .subscribe({
                    if (!hidePullDownPrompt) {
                        mineView.displayPromptHeader()
                    }
                    mineView.hideLoading()
                    updateHeaderState()
                    updateFooterState()
                    updateUntreatedCount()
                    mineView.loadDataSourceSuccess(it, true)
                }, {
                    it.printStackTrace()
                    currentPage = 1
                    currentForwardPage = 0
                    mineView.hideLoading()
                    mineView.loadDataSourceFailure(true)
                })

    }

    // 上拉加载下一页数据
    fun loadMoreDataSource() {
        loadDataRequest.page = currentPage
        repository.requestMeetingList(loadDataRequest)
                .map { mapAndRecordMeetingListInfo(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mineView.loadDataSourceSuccess(it, false)
                    updateFooterState()
                    updateUntreatedCount()
                }, {
                    currentPage--
                    mineView.loadDataSourceFailure(false)
                })
    }

    // 下拉加载过期数据
    fun loadStaleDataSource() {
        if (loadStaleDataRequest == null) {
            loadStaleDataRequest = MeetingListRequest()
            loadStaleDataRequest!!.meetingType = meetingType
            loadStaleDataRequest!!.isOver = true
        }

        loadStaleDataRequest!!.page = currentForwardPage
        repository.requestMeetingList(loadStaleDataRequest!!)
                .map { mapAndRecordMeetingListInfo(it) }
                .doOnNext { it.forEach { it.attendedFlag = "-1" } }     // 仅在开发阶段
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mineView.loadStaleDataSourceSuccess(it)
                    updateHeaderState()
                }, {
                    currentForwardPage--
                    mineView.loadStaleDataSourceFailure()
                })
    }

    private fun updateHeaderState() {
        if (currentForwardPage >= totalForwardPage) {
            mineView.enableLoadStaleData(false)
        } else {
            currentForwardPage++
            mineView.enableLoadStaleData(true)
        }
    }

    private fun updateFooterState() {
        if (currentPage >= totalPage) {
            mineView.enableLoadMoreData(false)
        } else {
            currentPage++
            mineView.enableLoadMoreData(true)
        }
    }

    private fun updateUntreatedCount() {
        mineView.displayUntreatedCount(untreated)
    }

    private fun mapAndRecordMeetingListInfo(response: MeetingListResponse): MutableList<MeetingDescription> {
        this.untreated = response.untreated
        this.totalPage = response.totalPage
        this.totalForwardPage = response.overTotalPage

        val meetings = mutableListOf<MeetingDescription>()
        response.data.forEach { meetings.add(MeetingDescription.newInstance(it)) }
        return meetings
    }
}