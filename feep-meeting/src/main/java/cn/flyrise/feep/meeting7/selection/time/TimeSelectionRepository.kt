package cn.flyrise.feep.meeting7.selection.time

import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.meeting7.protocol.DailyRoomUsageResponse
import cn.flyrise.feep.meeting7.protocol.MeetingUsageRequest
import cn.flyrise.feep.meeting7.selection.bean.MSTimeItem
import cn.flyrise.feep.meeting7.ui.bean.OccupyRoom
import cn.flyrise.feep.meeting7.ui.bean.RoomUsage
import rx.Observable
import rx.Subscriber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author ZYP
 * @since 2018-06-13 16:06
 */
class TimeSelectionRepository(val roomId: String) {

    private var occupiedRoomCache: HashMap<String, OccupyRoom> = HashMap()
    private val roomUsageMap: HashMap<String, OccupyRoom> = HashMap()

    fun obtainOccupiedRoom(key: String): OccupyRoom? {
        val or = occupiedRoomCache.get(key)
        val usage = roomUsageMap.get(or?.id)
        return usage
    }

    fun obtainRoomServiceCondition(year: Int, month: Int, day: Int): Observable<List<MSTimeItem>> {
        val dateTime = String.format("%d-%02d-%02d", year, month + 1, day)
        val dataSources = prepareDefaultDataSource(year, month, day)
        return Observable.zip(obtainRoomUsageCondition(dateTime), Observable.just(dataSources), { usages, items ->
//            val services = CoreZygote.getAddressBookServices()
            for (i in items) {
                val key = makeKey(i.year, i.month, i.day, i.hour, i.minute)
                if (!occupiedRoomCache.containsKey(key)) {
                    continue
                }

                val r = occupiedRoomCache.get(key)!!
                i.state = r.state
//                if (i.state == STATE_OCCUPY_END) {
//                    val address = services.queryUserInfo(r.userId)
//                    i.extra = address?.name ?: ""
//                }
            }
            items
        })
    }

    fun makeKey(year: Int, month: Int, day: Int, hour: Int, minute: Int) =
            String.format("%d年%02d月%02d日%02d时%02d分", year, month, day, hour, minute)

    /**
     * 获取当天会议室的使用情况
     * @roomId 会议室 ID
     * @dateTime 日期（格式：yyyy-MM-dd）
     */
    private fun obtainRoomUsageCondition(dateTime: String): Observable<List<OccupyRoom>> {
        return Observable
                .create { f: Subscriber<in List<RoomUsage>> ->
                    val request = MeetingUsageRequest.requestDailyUsage(roomId, dateTime)
                    FEHttpClient.getInstance().post(request, object : ResponseCallback<DailyRoomUsageResponse>() {
                        override fun onCompleted(response: DailyRoomUsageResponse?) {
                            f.onNext(response?.data?.usages)
                            f.onCompleted()
                        }

                        override fun onFailure(exception: RepositoryException?) {
                            f.onNext(null)
                            f.onCompleted()
                        }
                    })
                }
                .map {
                    it?.forEach {
                        it.startTime = "${dateTime} ${it.startTime}:00"
                        it.endTime = "${dateTime} ${it.endTime}:00"
                        roomUsageMap.put(it.id, OccupyRoom.newInstance(it))
                    }
                    it
                }
                .map {
                    val occupyRooms = ArrayList<OccupyRoom>()
                    if (CommonUtil.nonEmptyList(it)) {
                        val saveToCache = fun(key: String, room: OccupyRoom) {
                            if (!occupiedRoomCache.containsKey(key)) {
                                occupiedRoomCache.put(key, room)
                            }
                        }

                        for (r in it) {
                            // r = 10:30 这样的数据啊卧槽~~~
                            val godOR = OccupyRoom.newInstance(r)

                            var or = godOR.copy()
                            var hour = or.startHour
                            var minute = or.startMinute
                            if (minute > 0 && minute < 30) {
                                minute = 0
                            } else if (minute > 30) {
                                minute = 30
                            }
                            or.setTime(hour, minute)
                            or.state = STATE_OCCUPY_SECTION
                            saveToCache(or.timeKey(), or)
                            or = or.nextTime()

                            while (!or.isThatTime(godOR.startYear, godOR.startMonth, godOR.startDay, godOR.endHour, godOR.endMinute)) {
                                or.state = STATE_OCCUPY_SECTION
                                saveToCache(or.timeKey(), or)
                                or = or.nextTime()
                            }

                            hour = or.endHour
                            minute = or.endMinute

                            if (or.endHour == 24 && or.endMinute == 0) {
                                hour = 23
                                minute = 30
                            }

                            if (minute > 0 && minute < 30) {
                                minute = 30
                            } else if (minute > 30) {
                                minute = 0
                                hour += 1
                            }
                            or.setTime(hour, minute)
                            or.state = STATE_OCCUPY_SECTION
                            saveToCache(or.timeKey(), or)
                        }
                    }
                    occupyRooms
                }
    }

    /**
     * 准备默认数据
     */
    private fun prepareDefaultDataSource(year: Int, month: Int, day: Int): List<MSTimeItem> {
        val dataSources = mutableListOf<MSTimeItem>()

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)


        val isToday = currentYear == year && currentMonth == month && currentDay == day
        val timeState = fun(h: Int, m: Int): Int {
            if (isToday) {
                if (h < currentHour) return STATE_UNABLE
                if (h == currentHour && m < currentMinute) return STATE_UNABLE
            }

            return STATE_NORMAL
        }

        for (i in 0..23) {
            dataSources.add(MSTimeItem(year, month, day, i, 0, timeState(i, 0)))
            dataSources.add(MSTimeItem(year, month, day, i, 30, timeState(i, 30)))
        }
//        dataSources.add(MSTimeItem(year, month, day, 24, 0, timeState(24, 0)))
        return dataSources
    }

    private fun time(startTime: String, endTime: String): Time {
        val shm = startTime.split(":")
        val ehm = endTime.split(":")

        var sh = shm[0].toInt()
        var sm = shm[1].toInt()

        if (sm < 30) {
            sm = 0
        } else if (sm > 30) {
            sm = 0
            sh = sh + 1
        }

        if (sh > 24) sh = 24

        var eh = ehm[0].toInt()
        var em = ehm[1].toInt()
        if (em < 30) {
            em = 0
        } else if (em > 30) {
            em = 0
            eh = eh + 1
        }

        if (eh > 24) eh = 24
        return Time(sh, sm, eh, em)
    }

    private data class Time(val sh: Int, val sm: Int, val eh: Int, val em: Int)
}