package cn.flyrise.feep.meeting7.selection.date

import android.support.v4.app.Fragment
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.meeting7.selection.SelectionView
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.selection.time.STATE_END
import cn.flyrise.feep.meeting7.selection.time.STATE_SECTION
import cn.flyrise.feep.meeting7.selection.time.STATE_START
import cn.flyrise.feep.meeting7.ui.RoomServiceConditions
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author ZYP
 * @since 2018-06-11 10:52
 */
class DateSelectionPresenter(val roomId: String, val selectionView: SelectionView) {

    private val repository = DateSelectionRepository(roomId)
    private var startDate: MSDateItem? = null
    private var endDate: MSDateItem? = null


    fun setSelectedDate(startDate: MSDateItem?, endDate: MSDateItem?) {
        this.startDate = startDate
        this.endDate = endDate
    }

    fun start() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        selectionView.loading(true)
        repository.obtainRoomServiceCondition(year, month)
                .doOnNext {
                    if (startDate != null && endDate != null) {
                        val s = Calendar.getInstance()
                        s.set(Calendar.YEAR, startDate!!.year)
                        s.set(Calendar.MONTH, startDate!!.month)
                        s.set(Calendar.DAY_OF_MONTH, startDate!!.day)

                        val e = Calendar.getInstance()
                        e.set(Calendar.YEAR, endDate!!.year)
                        e.set(Calendar.MONTH, endDate!!.month)
                        e.set(Calendar.DAY_OF_MONTH, endDate!!.day)

                        var isInRange = fun(item: MSDateItem): Boolean {
                            val t = Calendar.getInstance()
                            t.set(Calendar.YEAR, item!!.year)
                            t.set(Calendar.MONTH, item!!.month)
                            t.set(Calendar.DAY_OF_MONTH, item!!.day)
                            return (t.after(s) && t.before(e))
                        }

                        it.forEach {
                            if (it.year == startDate!!.year && it.month == startDate!!.month && it.day == startDate!!.day) {
                                it.state = STATE_START
                                return@forEach
                            }

                            if (it.year == endDate!!.year && it.month == endDate!!.month && it.day == endDate!!.day) {
                                it.state = STATE_END
                                return@forEach
                            }

                            if (isInRange(it)) {
                                it.state = STATE_SECTION
                            }
                        }
                        startDate = null
                        endDate = null
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { selectionView.loading(false) }
                .doOnError { selectionView.loading(false) }
                .subscribe({ selectionView.refreshBoard(it) }, { it.printStackTrace() })

    }

    fun previewUsageInDate(date: MSDateItem) {
        val key = repository.makeKey(date.year, date.month, date.day)
        val rooms = repository.obtainOccupiedRoom(key)
        if (CommonUtil.isEmptyList(rooms)) {
            FELog.w("Could not find the occupy room by in date {${date.year}-${date.month + 1}-${date.day}}")
            return
        }

        RoomServiceConditions.newInstance(rooms!!).show((selectionView as Fragment).childFragmentManager, "DateUsage")
    }

}