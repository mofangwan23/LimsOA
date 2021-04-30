package cn.flyrise.feep.meeting7.ui.bean

import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment
import cn.flyrise.feep.meeting7.selection.time.getHHmm
import cn.flyrise.feep.meeting7.selection.time.getYYYYMMddHHmm
import cn.flyrise.feep.meeting7.selection.time.isSameDate
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-22 10:01
 */
class MeetingDetail {

    var joinId: String? = null                          // 参会Id

    var compere: String? = null                         // 会议主持人
    var compereId: String? = null                       // 会议主持人Id
    var recorder: String? = null                        // 会议记录人
    var recorderId: String? = null                      // 会议记录人 Id
    var qrcode: QRCode? = null                          // 二维码内容
    var meetingId: String? = null                       // 会议Id

    var topics: String? = null                          // 主题
    var initiator: String? = null                       // 发起人
    var initiatorId: String? = null                     // 发起人Id

    var roomId: String? = null                          // 会议室 Id
    var roomName: String? = null                        // 地点
    var location: String? = null                        // 地址
    var meetingType: String? = null                     // 类型
    var meetingTypeId: String? = null
    var status: String? = null                          // 状态 -1:已过期,0:未处理,1:参加,2:不参加
    var content: String? = null                         // 内容

    var attachmentGUID: String? = null                  // 附件GUID
    var attachments: List<NetworkAttachment>? = null
    var replies: List<MeetingReply>? = null

    var startDate: Calendar? = null                      // 开始时间
    var endDate: Calendar? = null                        // 结束时间

    var startTime: Long?=null                            // 开始时间毫秒值
    var endTime: Long?=null                              // 结束时间毫秒值

    private var untreatedUsers: MutableList<AddressBook>? = null    // 未办理人员 0:未处理,1:参加,2:不参加
    private var attendUsers: MutableList<AddressBook>? = null       // 参加人员
    private var notAttendUsers: MutableList<AddressBook>? = null    // 不参加人员

    fun addUntreatedUsers(addressBook: AddressBook?) {
        if (addressBook == null) return
        if (untreatedUsers == null) {
            untreatedUsers = mutableListOf()
        }
        untreatedUsers!!.add(addressBook)
    }

    fun addAttendUsers(addressBook: AddressBook?) {
        if (addressBook == null) return
        if (attendUsers == null) {
            attendUsers = mutableListOf()
        }
        attendUsers!!.add(addressBook)
    }

    fun addNotAttendUsers(addressBook: AddressBook?) {
        if (addressBook == null) return
        if (notAttendUsers == null) {
            notAttendUsers = mutableListOf()
        }
        notAttendUsers!!.add(addressBook)
    }

    fun untreatedUsers() = untreatedUsers
    fun attendUsers() = attendUsers
    fun notAttendUsers() = notAttendUsers

    fun isSameDay() = isSameDate(startDate, endDate)
    fun getSameDayStartTime() = getHHmm(startDate, endDate)
    fun getStartDateTime() = getYYYYMMddHHmm(startDate!!)
    fun getEndDateTime() = getYYYYMMddHHmm(endDate!!)
    fun getStartTimeSupplement(): String {
        val dayOfWeek = startDate!!.get(Calendar.DAY_OF_WEEK)
        val week = when (dayOfWeek) {
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            else -> "周日"
        }

        return String.format("%d年%02d月%02d日(%s)",
                startDate!!.get(Calendar.YEAR),
                startDate!!.get(Calendar.MONTH) + 1,
                startDate!!.get(Calendar.DAY_OF_MONTH), week)
    }


}