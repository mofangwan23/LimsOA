package cn.flyrise.feep.meeting7.presenter

import android.content.Context
import android.text.TextUtils
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl
import cn.flyrise.feep.core.network.request.FileRequest
import cn.flyrise.feep.core.network.request.FileRequestContent
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.core.network.uploader.UploadManager
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.common.AttachmentBeanConverter
import cn.flyrise.feep.meeting7.protocol.MeetingReplyRequest
import cn.flyrise.feep.meeting7.protocol.SubReplyRequest
import cn.flyrise.feep.meeting7.repository.MeetingDataRepository
import cn.flyrise.feep.meeting7.ui.MeetingDetailView
import cn.flyrise.feep.meeting7.ui.bean.DetailChange
import cn.flyrise.feep.meeting7.ui.bean.MeetingDetail
import org.greenrobot.eventbus.EventBus
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-28 10:12
 */
class MeetingDetailPresenter(val repository: MeetingDataRepository, val dv: MeetingDetailView) {

    private var meetingId: String? = null
    private lateinit var d: MeetingDetail
    private var uploadManager: UploadManager? = null
    private var type: String? = null
    var selectedAttachments: MutableList<String>? = null

    fun setRequestType(type: String?) {
        this.type = type
    }

    fun start(meetingId: String) {
        dv.loading(true)
        this.meetingId = meetingId
        this.repository.requestMeetingDetail(meetingId, this.type ?: "")
                .map {
                    val sb = MeetingDetail()
                    sb.meetingId = it.meetingId
                    sb.joinId = it.meetingJoinId
                    sb.compere = it.meetingCompere
                    sb.compereId = it.meetingCompereId
                    sb.recorder = it.meetingRecorder
                    sb.recorderId = it.meetingRecorderId
                    sb.qrcode = it.qrCode
                    sb.topics = it.topics
                    sb.initiator = it.initiator
                    sb.initiatorId = it.initiatorId
                    sb.roomId = it.roomId
                    sb.roomName = it.roomName
                    sb.location = it.meetingAddress
                    sb.status = it.meetingStatus
                    sb.meetingType = it.meetingType
                    sb.meetingTypeId = it.meetingTypeId
                    sb.content = it.content
                    sb.attachmentGUID = it.attachmentGUID
                    sb.attachments = AttachmentBeanConverter.convert(it.attachments)

                    sb.replies = it.replies
                    sb.startDate = DateUtil.str2Calendar(it.startDate)
                    sb.endDate = DateUtil.str2Calendar(it.endDate)

                    sb.startTime = ((sb.startDate) as Calendar).timeInMillis

                    val abs = CoreZygote.getAddressBookServices()
                    for (u in it.attendUsers) {
                        val user = abs.queryUserInfo(u.userId)
                        if (user == null) continue;

                        when (u.attendStatus) {
                            "0" -> sb.addUntreatedUsers(user)
                            "1" -> sb.addAttendUsers(user)
                            "2" -> sb.addNotAttendUsers(user)
                        }
                    }
                    sb
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    dv.loading(false)
                    d = it
                    dv.result(true)
                    applyUIDraw()
                }, {
                    dv.loading(false)
                    it.printStackTrace()
                    dv.result(false)
                })
    }

    private fun applyUIDraw() {
        val loginUserId = CoreZygote.getLoginUserServices().userId
        val isInitiator = TextUtils.equals(loginUserId, d.initiatorId)        // 当前用户是否是会议发起人
        val isOutOfDate: Boolean
        if (d.startTime!! - System.currentTimeMillis() < 0) {
            isOutOfDate = true
        } else {
            isOutOfDate = false
        }

        val isCancelStatus = TextUtils.equals(d.status, "3");

        // 初始化底部和会议状态等基本信息
        if (isInitiator) {
            if (TextUtils.equals(d.status, "3")) {
                dv.canceledStatus()
            } else if (TextUtils.equals(d.status, "-1")) {
                dv.outOfDateStatus()
            } else {
                dv.initiatorStatus()
            }
        } else {
            when (d.status) {
                "-1" -> dv.outOfDateStatus()
                "0" -> dv.untreatedStatus()
                "1" -> dv.attendStatus()
                "2" -> dv.notAttendStatus()
                "3" -> dv.canceledStatus()
                else -> dv.unknownStatus()
            }
        }


        // 初始化会议简介
        dv.description(d.topics!!, d.initiator!!, d.roomName!!,
                d.location, d.meetingType, d.compere, d.recorder, d.content)

        if (d.isSameDay()) {
            dv.meetingTime(false, d.getSameDayStartTime(), d.getStartTimeSupplement())
        } else {
            dv.meetingTime(true, d.getStartDateTime(), d.getEndDateTime())
        }

        // 初始化附件
        dv.attachments(d.attachments)

        // 初始化回复
        dv.replies(d.replies)

        // 初始化参会统计
        dv.untreatedPrompt(isInitiator, isOutOfDate, isCancelStatus)
        if (isInitiator && CommonUtil.nonEmptyList(d.untreatedUsers())) {
            dv.attendee(d.untreatedUsers(), "未办理(${size(d.untreatedUsers())})")
        } else {
            dv.attendee(d.attendUsers(), "参加(${size(d.attendUsers())})")
        }

    }

    fun qrCode() = d.qrcode

    fun fetchAttendees(position: Int) {
        when (position) {
            0 -> dv.attendee(d.untreatedUsers(), "未办理(${size(d.untreatedUsers())})")
            1 -> dv.attendee(d.notAttendUsers(), "不参加(${size(d.notAttendUsers())})")
            2 -> dv.attendee(d.attendUsers(), "参加(${size(d.attendUsers())})")
        }
    }

    fun untreatedCount() = d.untreatedUsers()?.size ?: 0
    fun notAttendCount() = d.notAttendUsers()?.size ?: 0
    fun attendCount() = d.attendUsers()?.size ?: 0

    fun replyToSomeone(content: String, replyId: String) {
        val guid = UUID.randomUUID().toString()
        val fileRequest = FileRequest()

        if (CommonUtil.nonEmptyList(selectedAttachments)) {
            val fileContent = FileRequestContent()
            fileContent.attachmentGUID = guid
            fileContent.files = selectedAttachments
            fileRequest.fileContent = fileContent
        }
        val replyRequest = SubReplyRequest()
        replyRequest.id = meetingId
        replyRequest.replyID = replyId
        replyRequest.content = content
        replyRequest.attachmentGUID = guid
        fileRequest.requestContent = replyRequest

        uploadManager = UploadManager(dv as Context)
                .fileRequest(fileRequest)
                .progressUpdateListener(object : OnProgressUpdateListenerImpl() {
                    override fun onPreExecute() {
                        dv.loading(true)
                    }

                    override fun onProgressUpdate(currentBytes: Long, contentLength: Long, done: Boolean) {
                        val progress = (currentBytes * 100 / contentLength * 1.0f).toInt()
                        dv.progress(progress)
                    }

                    override fun onFailExecute(ex: Throwable) {
                        dv.loading(false)
                        FEToast.showMessage("操作失败，请重试！")
                    }
                })
                .responseCallback(object : ResponseCallback<ResponseContent>(dv) {
                    override fun onCompleted(responseContent: ResponseContent) {
                        dv.loading(false)
                        if (!TextUtils.equals(ResponseContent.OK_CODE, responseContent.errorCode)) {
                            FEToast.showMessage("操作失败，请重试！")
                            return
                        }
                        dv.replySuccess()
                        start(meetingId!!)
                    }

                    override fun onFailure(repositoryException: RepositoryException) {
                        dv.loading(false)
                        FEToast.showMessage("操作失败，请重试！")
                    }
                })
        uploadManager!!.execute()
    }

    fun cancelUploadTask() {
        if (uploadManager != null) {
            uploadManager!!.cancelTask()
        }
        selectedAttachments?.clear()
    }

    fun clearSelectedAttachment() {
        if (CommonUtil.nonEmptyList(selectedAttachments)) {
            this.selectedAttachments!!.clear()
        }
    }

    fun replyMeeting(content: String) {
        executeReply(content, "0") {
            dv.replySuccess()
            start(meetingId!!)
        }
    }

    fun attendMeeting(content: String) {
        executeReply(content, "1") {
            dv.replySuccess()
            notifyChange()
            start(meetingId!!)
        }
    }

    fun notAttendMeeting(content: String) {
        executeReply(content, "2") {
            dv.replySuccess()
            notifyChange()
            start(meetingId!!)
        }
    }

    fun cancelMeeting(content: String?) {
        dv.loading(true)
        repository.cancelMeeting(d.meetingId!!, content!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    dv.loading(false)
                    dv.cancelSuccess()
                    notifyChange()
                }, {
                    dv.loading(false)
                    FEToast.showMessage("操作失败，请重试！")
                })
    }

    fun promptUntreatedUsers() {
        dv.loading(true)
        repository.promptUntreatedUsers(d.meetingId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    dv.loading(false)
                    dv.promptSuccess()
                }, {
                    dv.loading(false)
                    FEToast.showMessage("提醒失败")
                })
    }

    fun getMeetingDetail() = d

    private fun executeReply(content: String, status: String, callback: () -> Unit) {
        val guid = UUID.randomUUID().toString()
        val fileRequest = FileRequest()

        if (CommonUtil.nonEmptyList(selectedAttachments)) {
            val fileContent = FileRequestContent()
            fileContent.attachmentGUID = guid
            fileContent.files = selectedAttachments
            fileRequest.fileContent = fileContent
        }

        val replyRequest = MeetingReplyRequest()
        replyRequest.id = d.meetingId
        replyRequest.meetingId = d.joinId
        replyRequest.meetingStatus = status        // 回复
        replyRequest.meetingContent = content
        replyRequest.meetingAnnex = guid
        fileRequest.requestContent = replyRequest

        uploadManager = UploadManager(dv as Context)
                .fileRequest(fileRequest)
                .progressUpdateListener(object : OnProgressUpdateListenerImpl() {
                    override fun onPreExecute() {
                        dv.loading(true)
                    }

                    override fun onProgressUpdate(currentBytes: Long, contentLength: Long, done: Boolean) {
                        val progress = (currentBytes * 100 / contentLength * 1.0f).toInt()
                        dv.progress(progress)
                    }

                    override fun onFailExecute(ex: Throwable) {
                        dv.loading(false)
                        FEToast.showMessage("操作失败，请重试！")
                    }
                })
                .responseCallback(object : ResponseCallback<ResponseContent>(dv) {
                    override fun onCompleted(responseContent: ResponseContent) {
                        dv.loading(false)
                        if (!TextUtils.equals(ResponseContent.OK_CODE, responseContent.errorCode)) {
                            FEToast.showMessage("操作失败，请重试！")
                            return
                        }
                        callback.invoke()
                    }

                    override fun onFailure(repositoryException: RepositoryException) {
                        dv.loading(false)
                        FEToast.showMessage("操作失败，请重试！")
                    }
                })
        uploadManager?.execute()
    }

    private fun notifyChange() {
        EventBus.getDefault().post(DetailChange(dv.getMeetingType()))
    }

    private fun size(attendees: MutableList<AddressBook>?) = if (CommonUtil.isEmptyList(attendees)) 0 else attendees!!.size

}