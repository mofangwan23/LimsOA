package cn.flyrise.feep.meeting7.selection.time

import android.support.v4.app.Fragment
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.meeting7.selection.SelectionView
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.selection.bean.MSTimeItem
import cn.flyrise.feep.meeting7.ui.RoomServiceConditions
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*


/**
 * @author ZYP
 * @since 2018-06-13 17:41
 */
class TimeSelectionPresenter(val roomId: String, val selectionView: SelectionView) {

    private val repository: TimeSelectionRepository = TimeSelectionRepository(roomId)
    private var startDate: MSDateItem? = null
    private var endDate: MSDateItem? = null

    fun setSelectedDate(startDate: MSDateItem?, endDate: MSDateItem?) {
        this.startDate = startDate
        this.endDate = endDate
    }

    fun start(ty: Int, tm: Int, td: Int) {
        if (ty == 0 && tm == 0 && td == 0) {
            val c = Calendar.getInstance()
            val y = c.get(Calendar.YEAR)
            val m = c.get(Calendar.MONTH)
            val d = c.get(Calendar.DAY_OF_MONTH)
            fetchRoomServiceCondition(y, m, d)
        } else {
            fetchRoomServiceCondition(ty, tm, td)
        }
    }

    fun fetchRoomServiceCondition(year: Int, month: Int, day: Int) {
        selectionView.loading(true)
        repository.obtainRoomServiceCondition(year, month, day)
                .doOnNext {
                    if (startDate != null && endDate != null) {
                        val s = startDate as MSTimeItem
                        val e = endDate as MSTimeItem

                        if (s.year == year && s.month == month && s.day == day
                                && e.year == year && e.month == month && e.day == day) {

                            it.forEach {
                                if (it.state == STATE_BLANK) return@forEach
//                                if (it.hour == 0 && it.minute == 0 && it.state == STATE_BLANK) return@forEach
//                                if (s.hour == 0 && s.minute == 0 && it.hour == 0) {
//                                    // 修正狗日的服务端数据
//                                    it.state = STATE_START
//                                    return@forEach
//                                }

                                if (it.hour == s.hour) {
                                    if (it.minute == s.minute) {
                                        it.state = STATE_START
                                    } else if (it.minute > s.minute) {
                                        it.state = STATE_SECTION
                                    }
                                } else if (it.hour == e.hour) {
                                    if (it.minute == e.minute) {
                                        it.state = STATE_END
                                    } else if (it.minute < e.minute) {
                                        it.state = STATE_SECTION
                                    }
                                } else if (it.hour > s.hour && it.hour < e.hour) {
                                    it.state = STATE_SECTION
                                }
                            }

                            startDate = null
                            endDate = null
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    selectionView.loading(false)
                    selectionView.refreshBoard(it)
                }, {
                    selectionView.loading(false)
                    it.printStackTrace()
                })
    }

    fun previewUsageInTime(time: MSTimeItem) {
        val key = repository.makeKey(time.year, time.month, time.day, time.hour, time.minute)
        val room = repository.obtainOccupiedRoom(key)
        if (room == null) {
            FELog.w("Could not find the occupy room by in date {${time.year}-${time.month + 1}-${time.day} ${time.hour}:${time.minute}}")
            return
        }

        RoomServiceConditions.newInstance(room).show((selectionView as Fragment).childFragmentManager, "RoomUsage")

    }
}