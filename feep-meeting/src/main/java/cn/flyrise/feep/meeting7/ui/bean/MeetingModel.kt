package cn.flyrise.feep.meeting7.ui.bean

import android.content.Context
import android.text.TextUtils
import android.view.textservice.TextInfo
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl
import cn.flyrise.feep.core.network.request.FileRequest
import cn.flyrise.feep.core.network.request.FileRequestContent
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.core.network.uploader.UploadManager
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment
import cn.flyrise.feep.media.attachments.repository.AttachmentConverter
import cn.flyrise.feep.meeting7.protocol.PublishMeetingRequest
import cn.flyrise.feep.meeting7.repository.MeetingDataRepository
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * Model 层对象
 */
class MeetingModel(val r: RoomInfo) {

    private val repository = MeetingDataRepository()
    private var timesIndex: Int = 0
    private var timesKey: MutableList<String>? = null
    private var timesValue: MutableList<String>? = null

    private var typesIndex: Int = -1
    private var typesKey: MutableList<String>? = null
    private var typesValues: MutableList<String>? = null

    private var meetingId: String? = null               // 会议 ID，如果新增，则该字段为空。
    private var attachmentID: String? = null            // 附件 GUID
    private var compereId: String? = null               // 主持人ID
    private var compereName: String? = null             // 主持人 Name

    private var recorderId: String? = null              // 会议记录人 ID
    private var recorderName: String? = null            // 会议记录人 Name

    private var attendUsers: MutableList<AddressBook>? = null               // 参会人员
    private var attendUsersFromServer: MutableList<AddressBook> = ArrayList()            // 参会人员
    private var attachments: MutableList<Attachment> = ArrayList()          // 参会附件
    private var toBeDeletedAttachment: MutableList<String> = ArrayList()    // 服务器上待删除的附件

    // 请求会议类型
    fun fetchMeetingTypes() {
        repository.requestMeetingTypes()
                .retry(3)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    typesKey = ArrayList(it?.size ?: 0)
                    typesValues = ArrayList(it?.size ?: 0)
                    it?.forEach {
                        typesKey!!.add(it.id)
                        typesValues!!.add(it.typeName)
                    }
                }, {
                    it.printStackTrace()
                    typesValues = ArrayList()
                })
    }

    // 请求会议提醒时间
    fun fetchPromptTimes() {
        repository.requestPromptTimes()
                .retry(3)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    timesKey = ArrayList(it?.size ?: 0)
                    timesValue = ArrayList(it?.size ?: 0)
                    it?.forEach {
                        timesKey!!.add(it.key)
                        timesValue!!.add(it.value)
                    }
                }, { it.printStackTrace() })
    }

    // 设置会议 ID，仅在修改会议的时候
    fun setMeetingId(meetingId: String?) {
        this.meetingId = meetingId
    }

    // 设置附件的 GUID
    fun setAttachmentId(attachmentID: String?) {
        this.attachmentID = attachmentID
    }

    fun setPromptTimes(p: Int) {
        timesIndex = p
    }

    fun setMeetingType(p: Int) {
        typesIndex = p
    }

    fun setCompere(userId: String, username: String) {
        this.compereId = userId
        this.compereName = username
    }

    fun setRecorder(userId: String, username: String) {
        this.recorderId = userId
        this.recorderName = username
    }

    fun setAttendUsers(users: MutableList<AddressBook>) {
        this.attendUsers = users
    }

    fun setAttendUserFromServer(users: MutableList<AddressBook>){
        this.attendUsersFromServer.addAll(users)
    }

    fun setAttachments(files: MutableList<String>) {
        if (CommonUtil.isEmptyList(files)) return

        val addFunc = fun(fs: List<String>) {
            val sas = AttachmentConverter.convertAttachments(fs)
            for (a in sas) {
                if (!attachments.contains(a)) {
                    attachments.add(a)
                }
            }
        }

        val originalPaths = getAttachmentPaths()
        if (originalPaths.size == 0) {
            addFunc(files)
            return
        }

        val intersection = mutableListOf<String>()
        intersection.addAll(originalPaths)
        intersection.removeAll(files)             // 1. 差集
        originalPaths.retainAll(files)            // 2. 交集
        files.removeAll(originalPaths)            // 3. 补集
        originalPaths.addAll(files)               // 4. 并集

        val toDeleteAttachments = AttachmentConverter.convertAttachments(intersection)
        toDeleteAttachments?.forEach { removeAttachment(it) }
        addFunc(originalPaths)
    }

    fun setNetworkAttachments(attachments: List<NetworkAttachment>?) {
        if (CommonUtil.nonEmptyList(attachments)) {
            this.attachments.addAll(attachments!!)
        }
    }

    fun getPromptTime() = timesValue?.get(timesIndex) ?: ""
    fun getPromptTimes() = timesValue
    fun getMeetingType() = if(typesValues?.size!! > 0 && typesIndex!=-1) typesValues?.get(typesIndex) ?: "" else ""
    fun getMeetingTypes() = typesValues
    fun getCompereId() = compereId
    fun getcompere() = compereName
    fun getRecorderId() = recorderId
    fun getRecorder() = recorderName
    fun getAttendUsers() = attendUsers
    fun getAttendUsersFromServer() = attendUsersFromServer
    fun getAttachments() = attachments
    fun getAttachmentSize() = attachments.size
    fun removeAttendee(a: AddressBook) = attendUsers?.remove(a)
    fun removeAttachment(a: Attachment) {
        attachments.remove(a)
        if (a is NetworkAttachment) {
            if (!toBeDeletedAttachment.contains(a.id)) {
                toBeDeletedAttachment.add(a.id)
            }
        }
    }

    fun getAttachmentPaths(): MutableList<String> {
        val paths = mutableListOf<String>()
        attachments.filter { TextUtils.isEmpty(it.id) }.forEach { paths.add(it.path) }
        return paths
    }

    fun publish(context: Context, address: String?,
                title: String, content: String, requestType: String, watcher: PublishWatcher?) {
        val roomId = r.roomId
        var room = r.roomName
        if (!TextUtils.isEmpty(address)) {
            room = address
        }

        val sonOfBitchGUID = if (TextUtils.isEmpty(attachmentID)) UUID.randomUUID().toString() else attachmentID
        val request = PublishMeetingRequest(requestType)
        if (!TextUtils.isEmpty(meetingId)) {
            request.id = meetingId
        }
        request.flag = "1"
        request.topics = title
        request.content = content
        request.roomId = if (TextUtils.isEmpty(roomId)) "-1" else roomId
        request.address = room
        request.compere = compereId
        request.recordMan = recorderId
        request.attendee = getAttendUserIds()
        request.meetingType = if(typesIndex!=-1) typesKey?.get(typesIndex) ?: "" else ""
        request.remindTime = timesKey?.get(timesIndex) ?: "30"
        request.attachments = sonOfBitchGUID

        request.startDate = String.format("%d-%02d-%02d %02d:%02d:%02d",
                r.startYear, r.startMonth + 1, r.startDay, r.startHour, r.startMinute, 0)
        request.endDate = String.format("%d-%02d-%02d %02d:%02d:%02d",
                r.endYear, r.endMonth + 1, r.endDay, r.endHour, r.endMinute, 0)
        request.res = ""

        val fileContent = FileRequestContent()
        fileContent.attachmentGUID = sonOfBitchGUID

        val selectedAttachments = getAttachmentPaths()
        fileContent.files = if (CommonUtil.isEmptyList(selectedAttachments)) ArrayList() else selectedAttachments

        val finalRequest = FileRequest()
        finalRequest.requestContent = request
        finalRequest.fileContent = fileContent

        if (toBeDeletedAttachment.size > 0) {
            finalRequest.fileContent.deleteFileIds = toBeDeletedAttachment
        }

        UploadManager(context)
                .fileRequest(finalRequest)
                .progressUpdateListener(object : OnProgressUpdateListenerImpl() {
                    override fun onPreExecute() {
                        watcher?.start()
                    }

                    override fun onProgressUpdate(currentBytes: Long, contentLength: Long, done: Boolean) {
                        val p = (currentBytes * 100 / contentLength * 1.0f).toInt()
                        watcher?.progress(p)
                    }
                })
                .responseCallback(object : ResponseCallback<ResponseContent>() {
                    override fun onCompleted(t: ResponseContent?) {
                        if (TextUtils.equals(t?.errorCode, "0")) {
                            watcher?.completed()
                        } else if (t?.errorCode.equals("-1012")) {
                            watcher?.errorTime(RuntimeException("Upload failure."))
                        } else {
                            watcher?.error(RuntimeException("Upload failure."))
                        }
                    }

                    override fun onFailure(e: RepositoryException?) {
                        watcher?.error(e?.exception())
                    }
                })
                .execute()
    }

    private fun getAttendUserIds(): String {
        val sb = StringBuilder()
        attendUsers?.forEachIndexed { index, addressBook ->
            if (index == 0) {
                sb.append(addressBook.userId)
                return@forEachIndexed
            }

            sb.append(",").append(addressBook.userId)
        }
        return sb.toString()
    }

    interface PublishWatcher {
        fun start()
        fun progress(p: Int)
        fun completed()
        fun error(e: Exception?)
        fun errorTime(e: Exception?)
    }

}