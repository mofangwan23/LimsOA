package cn.flyrise.feep.meeting7.selection.date

import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.meeting7.protocol.DailyRoomUsageResponse
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.selection.time.*
import cn.flyrise.feep.meeting7.protocol.MeetingUsageRequest
import cn.flyrise.feep.meeting7.selection.bean.MSDayDateItem
import cn.flyrise.feep.meeting7.selection.bean.MSMonthDateItem
import cn.flyrise.feep.meeting7.ui.bean.OccupyRoom
import cn.flyrise.feep.meeting7.ui.bean.RoomUsage
import rx.Observable
import rx.Subscriber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author ZYP
 * @since 2018-06-11 10:52
 */
class DateSelectionRepository(val roomId: String) {

    private var occupiedRoomCache = HashMap<String, MutableList<OccupyRoom>>()
    private var roomUsageCache = HashMap<String, OccupyRoom>()

    fun obtainOccupiedRoom(key: String): MutableList<OccupyRoom>? {
        val rooms = ArrayList<OccupyRoom>()
        val occupiedRooms = occupiedRoomCache.get(key)
        occupiedRooms?.forEach {
            if (!roomUsageCache.containsKey(it.id)) {
                return@forEach
            }

            val room = roomUsageCache.get(it.id)
            if (room == null || rooms.contains(room)) {
                return@forEach
            }
            rooms.add(room!!)
        }
        return rooms
    }

    fun obtainRoomServiceCondition(year: Int, month: Int): Observable<List<MSDateItem>> {
        val dataSource = prepareDefaultDataSource()
        return Observable.zip(prepareRemoteDataSource(year, month), Observable.just(dataSource), { usages, items ->
            val services = CoreZygote.getAddressBookServices()
            for (i in items) {
                if (i.state == STATE_BLANK
                        || i.state == STATE_UNABLE
                        || i.state == STATE_UNABLE_END) {
                    continue
                }

                val key = makeKey(i.year, i.month, i.day)
                if (!occupiedRoomCache.containsKey(key)) {
                    continue
                }

                val occupiedRooms = occupiedRoomCache.get(key)
                if (CommonUtil.isEmptyList(occupiedRooms)) {
                    continue
                }

                if (occupiedRooms!!.size == 1) {
                    val r = occupiedRooms.get(0)
                    i.state = r.state
//                    if (i.state == STATE_OCCUPY_END) {
//                        val address = services.queryUserInfo(r.userId)
//                        i.extra = address?.name ?: ""
//                    }
                    continue
                }

                i.state = STATE_OCCUPY_SECTION
            }
            items
        })
    }

    fun makeKey(year: Int, month: Int, day: Int) = String.format("%d???%02d???%02d???", year, month, day)

    /**
     * ????????????????????????
     */
    private fun prepareRemoteDataSource(year: Int, month: Int): Observable<MutableList<OccupyRoom>> {
        return Observable
                .create { f: Subscriber<in List<RoomUsage>> ->
                    val request = MeetingUsageRequest.requestAcrossDayUsage(roomId, "${year}", "${month + 1}")
                    FEHttpClient.getInstance().post(request, object : ResponseCallback<DailyRoomUsageResponse>() {
                        override fun onCompleted(response: DailyRoomUsageResponse?) {
                            f.onNext(response?.data?.usages)
                            f.onCompleted()
                        }

                        override fun onFailure(exception: RepositoryException?) {
                            exception?.exception()?.printStackTrace()
                            f.onNext(null)
                            f.onCompleted()
                        }
                    })
                }
                .map {
                    it?.forEach { roomUsageCache.put(it.id, OccupyRoom.newInstance(it)) }
                    it
                }
                .map {
                    val occupyRooms = ArrayList<OccupyRoom>()
                    if (CommonUtil.nonEmptyList(it)) {
                        for (r in it) {
                            val godOR = OccupyRoom.newInstance(r)
                            if (godOR.isSameDay()) {                        // ?????????
                                godOR.state = STATE_OCCUPY_SECTION
                                saveToCache(godOR.key(), godOR)
                                continue
                            }

                            var or = godOR.copy()
                            or.setDate(godOR.startYear, godOR.startMonth, godOR.startDay, godOR.startHour, godOR.startMinute, 23, 59)
                            or.state = STATE_OCCUPY_SECTION
                            saveToCache(or.key(), or)
                            or = or.next()

                            while (!or.isThatDay(godOR.endYear, godOR.endMonth, godOR.endDay)) {
                                or.state = STATE_OCCUPY_SECTION
                                saveToCache(or.key(), or)
                                or = or.next()
                            }

                            or.setDate(godOR.endYear, godOR.endMonth, godOR.endDay, 0, 0, godOR.endHour, godOR.endMinute)
                            or.state = STATE_OCCUPY_SECTION
                            saveToCache(or.key(), or)
                        }
                    }
                    occupyRooms
                }
    }

    /**
     * ??????????????????
     */
    private fun prepareDefaultDataSource(): List<MSDateItem> {
        val dataSources = mutableListOf<MSDateItem>()
        val calendar = Calendar.getInstance()

        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        dataSources.addAll(createScheduleBoardByMonth(currentYear, currentMonth, currentDay))

        var nextYear = currentYear
        for (i in 1..MAX_MONTH_IN_BOARD) {
            var nextMonth = (currentMonth + i) % 12
            if (nextMonth == 0) {
                nextYear++
            }

            dataSources.addAll(createScheduleBoardByMonth(nextYear, nextMonth, 1))
        }
        return dataSources
    }

    /**
     * ??????????????????????????????
     */
    private fun createScheduleBoardByMonth(year: Int, month: Int, day: Int): List<MSDateItem> {
        val dataSources = mutableListOf<MSDateItem>()

        // ????????? Calendar ??????
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)

        val monthDays = getMonthDays(year, month)               // ????????????????????????
        calendar.set(Calendar.DAY_OF_MONTH, monthDays)
        val lastDayWeek = calendar.get(Calendar.DAY_OF_WEEK)    // ?????????????????????

        calendar.set(Calendar.DAY_OF_MONTH, day)                // ??????????????????
        val firstDayWeek = calendar.get(Calendar.DAY_OF_WEEK)   // ??????????????????

        // 1. ???????????????
        dataSources.add(MSMonthDateItem(year, month))

        // 2. ??????????????????????????????
        if (day == 1) {         // ?????????????????? 1 ?????????????????????????????????????????????
            (1..getDayBeforeFirstDay(calendar, firstDayWeek)).mapTo(dataSources) { MSDayDateItem(STATE_BLANK) }
        } else {                // ?????????????????? day??????????????????????????????????????????????????????????????????
            for (i in 1..getDayBeforeFirstDay(calendar, firstDayWeek)) {
                val d = if (calendar.firstDayOfWeek == Calendar.SUNDAY) day - (firstDayWeek - i) else day - (firstDayWeek - i) + 1
                val dateItem = MSDayDateItem(year, month, d)
                dateItem.state = STATE_UNABLE

                if (i == firstDayWeek - 1) {
                    dateItem.state = STATE_UNABLE_END
                }
                dataSources.add(dateItem)
            }
        }

        // 3. ??????????????????
        var weekInDay = firstDayWeek
        for (i in day..monthDays) {
            val dateItem = MSDayDateItem(year, month, i)
            dateItem.state = if (weekInDay == Calendar.SUNDAY || weekInDay == Calendar.SATURDAY) STATE_WEEKEND else STATE_NORMAL
            dateItem.week = weekInDay
            dataSources.add(dateItem)

            weekInDay++
            if (weekInDay > 7) weekInDay = 1
        }

        // 4. ?????????????????????????????????
        (1..getDayAfterLastDay(calendar, lastDayWeek)).mapTo(dataSources) { MSDayDateItem(STATE_BLANK) }

        return dataSources
    }

    /**
     * ?????????????????????????????????
     */
    private fun getDayBeforeFirstDay(calendar: Calendar, dayOfMonthWeek: Int): Int {
        val firstDayOfWeek = calendar.firstDayOfWeek
        if (firstDayOfWeek == Calendar.SUNDAY) {
            return dayOfMonthWeek - 1
        }

        if (firstDayOfWeek == Calendar.MONDAY) {
            if (dayOfMonthWeek >= 2) {
                return dayOfMonthWeek - firstDayOfWeek
            }
            return 7 - dayOfMonthWeek
        }

        return dayOfMonthWeek - 1
    }

    /**
     * ?????????????????????????????????
     */
    private fun getDayAfterLastDay(calendar: Calendar, dayOfMonthWeek: Int): Int {
        val firstDayOfWeek = calendar.firstDayOfWeek
        if (firstDayOfWeek == Calendar.SUNDAY) {
            return if (firstDayOfWeek == 7) 0 else 7 - dayOfMonthWeek
        }

        if (firstDayOfWeek == Calendar.MONDAY) {
            return if (dayOfMonthWeek == 1) 0 else 7 - dayOfMonthWeek + 1
        }
        return 7 - dayOfMonthWeek
    }

    /**
     * ???????????????
     */
    private fun saveToCache(key: String, r: OccupyRoom) {
        var rs = occupiedRoomCache.get(key)
        if (CommonUtil.nonEmptyList(rs)) {
            rs!!.add(r)
        } else {
            val rs = mutableListOf<OccupyRoom>()
            rs.add(r)
            occupiedRoomCache.put(key, rs)
        }
    }
}