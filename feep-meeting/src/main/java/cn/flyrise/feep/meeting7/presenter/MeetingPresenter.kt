package cn.flyrise.feep.meeting7.presenter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.DataKeeper
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.core.function.ContactConfiguration
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.common.AttachmentUtils
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.time.isSameDate
import cn.flyrise.feep.meeting7.ui.*
import cn.flyrise.feep.meeting7.ui.bean.MeetingModel
import cn.flyrise.feep.meeting7.ui.bean.MeetingUpdateTookKit
import cn.squirtlez.frouter.FRouter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.*

class MeetingPresenter(val context: Context, val v: MeetingView, val m: MeetingModel) {

    private val expectTypes = arrayOf(".rar", ".zip", ".doc", ".docx", ".xls",
            ".xlsx", ".txt", ".pdf", ".zip", ".jpg", ".bmp", ".png", ".gif", ".jpeg", ".ppt", ".pptx", ".wav")

    fun start(isCustom: Boolean, isUpdate: Boolean) {
        m.fetchMeetingTypes()
        m.fetchPromptTimes()

        if (isUpdate) {
            val d = MeetingUpdateTookKit.getData()
            m.setMeetingId(d.meetingId)
            m.r.apply {
                type = if (isSameDate(d.startDate, d.endDate)) 0 else 1
                roomId = d.roomId
                roomName = d.roomName
                startYear = d.startDate!!.get(Calendar.YEAR)
                startMonth = d.startDate!!.get(Calendar.MONTH)
                startDay = d.startDate!!.get(Calendar.DAY_OF_MONTH)
                startHour = d.startDate!!.get(Calendar.HOUR_OF_DAY)
                startMinute = d.startDate!!.get(Calendar.MINUTE)

                endYear = d.endDate!!.get(Calendar.YEAR)
                endMonth = d.endDate!!.get(Calendar.MONTH)
                endDay = d.endDate!!.get(Calendar.DAY_OF_MONTH)
                endHour = d.endDate!!.get(Calendar.HOUR_OF_DAY)
                endMinute = d.endDate!!.get(Calendar.MINUTE)
            }

            if (TextUtils.isEmpty(d.roomId)
                    || TextUtils.equals(d.roomId, "-1")) {          // ????????????
                v.customMeetingTime(m)
            } else {
                v.meetingTime(m)
            }

            v.roomInfo(d.location, d.topics, d.content)             // ??????????????????????????????????????????
            m.setCompere(d.compereId ?: "", d.compere ?: "")        // ?????????
            v.compere(m.getcompere() ?: context.getString(R.string.meeting_create_selected))

            m.setRecorder(d.recorderId ?: "", d.recorder ?: "")     // ?????????
            v.recorder(m.getRecorder() ?: context.getString(R.string.meeting_create_selected))

            // ????????????
            Observable
                    .unsafeCreate<Int> {
                        while (CommonUtil.isEmptyList(m.getMeetingTypes())) {
                            Thread.sleep(100)
                        }

                        val p = m.getMeetingTypes()!!.indexOf(d.meetingType)
                        it.onNext(p)
                        it.onCompleted()
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        m.setMeetingType(it)
                        v.meetingType(m.getMeetingType())
                    }

            // ????????????
            val attendees = mutableListOf<AddressBook>()
            if (CommonUtil.nonEmptyList(d.attendUsers())) attendees.addAll(d.attendUsers()!!)
            if (CommonUtil.nonEmptyList(d.notAttendUsers())) attendees.addAll(d.notAttendUsers()!!)
            if (CommonUtil.nonEmptyList(d.untreatedUsers())) attendees.addAll(d.untreatedUsers()!!)

            val userId = CoreZygote.getLoginUserServices().userId
            val initiator = CoreZygote.getAddressBookServices().queryUserInfo(userId)
            if (attendees.contains(initiator)) {
                attendees.remove(initiator)
            }
            attendees.add(0, initiator)

            m.setAttendUsers(attendees)
            m.setAttendUserFromServer(attendees)
            v.attendUsers(attendees)
            v.attendUsersOld(attendees);

            // ????????????
            m.setAttachmentId(d.attachmentGUID)
            m.setNetworkAttachments(d.attachments)
            v.attachments(m.getAttachments())
        } else {
            if (isCustom) {
                v.customMeetingTime(m)
            } else {
                v.meetingTime(m)
            }

            // ???????????????????????????
            val attendees = mutableListOf<AddressBook>().apply {
                val userId = CoreZygote.getLoginUserServices().userId
                val initiator = CoreZygote.getAddressBookServices().queryUserInfo(userId)
                add(initiator)
            }
            m.setAttendUsers(attendees)
            m.setAttendUserFromServer(attendees)
            v.attendUsers(attendees)
            v.attendUsersOld(attendees);

        }
    }

    // ??????????????????
    fun promptTimes() {
        val times = m.getPromptTimes()
        if (CommonUtil.isEmptyList(times)) {
            v.error("")
        } else {
            v.promptTimes(times!!)
        }
    }

    // ??????????????????
    fun meetingTypes() {
        val types = m.getMeetingTypes()
        if (CommonUtil.isEmptyList(types)) {
            v.error(context.getString(R.string.meeting7_create_no_choice_style))
        } else {
            v.meetingTypes(types!!)
        }
    }

    // ???????????????
    fun compere() {
        val compereId = m.getCompereId()
        val key = v.hashCode()
        DataKeeper.getInstance().keepDatas(key, null)
        if (!TextUtils.isEmpty(compereId)) {
            val compere = CoreZygote.getAddressBookServices().queryUserInfo(compereId)
            if (compere != null) {
                DataKeeper.getInstance().keepDatas(key, mutableListOf(compere))
            }
        } else {
            DataKeeper.getInstance().removeKeepData(key)
        }

        FRouter.build(v as Context, "/addressBook/list")
                .withString("address_title", context.getString(R.string.meeting_create_host_contact_title))
                .withBool("single_select", true)
                .withBool("select_mode", true)
                .withInt("data_keep", key)
                .requestCode(CODE_COMPERE)
                .go()
    }

    // ???????????????
    fun recorder() {
        val recorderId = m.getRecorderId()
        val key = v.hashCode()
        if (!TextUtils.isEmpty(recorderId)) {
            val compere = CoreZygote.getAddressBookServices().queryUserInfo(recorderId)
            if (compere != null) {
                DataKeeper.getInstance().keepDatas(key, mutableListOf(compere))
            }
        } else {
            DataKeeper.getInstance().removeKeepData(key)
        }

        FRouter.build(v as Context, "/addressBook/list")
                .withString("address_title", context.getString(R.string.meeting_create_record_contact_title))
                .withBool("single_select", true)
                .withBool("select_mode", true)
                .withInt("data_keep", key)
                .requestCode(CODE_RECORDER)
                .go()
    }

    // ??????????????????
    fun addAttendees() {
        val key = v.hashCode()
        if (CommonUtil.nonEmptyList(m.getAttendUsers())) {
            DataKeeper.getInstance().keepDatas(key, m.getAttendUsers())
        } else {
            DataKeeper.getInstance().removeKeepData(key)
        }

        ContactConfiguration.getInstance().releaseCache()
        if (CommonUtil.nonEmptyList(m.getAttendUsersFromServer())) {
            for (index in m.getAttendUsersFromServer()) {
                ContactConfiguration.getInstance().addUserCannotSelectButChecked(index.userId)
            }
        }
        FRouter.build(v as Context, "/addressBook/list")
                .withInt("data_keep", key)
                .withBool("except_self", true)
                .withString("address_title", context.getString(R.string.meeting_create_attend_contact_title))
                .withBool("select_mode", true)
                .requestCode(CODE_ATTENDEES)
                .go()
    }

    // ????????????
    fun addAttachments() {
        FRouter.build(v as Context, "/media/file/select")
                .withInt("extra_max_select_count", 20 - m.getAttachmentSize())                          // ??????????????????????????????
                .withStrings("extra_expect_type", expectTypes)                                          // ???????????????????????????????????????
                .withStringArray("extra_selected_files", m.getAttachmentPaths() as ArrayList<String>)   // ????????????????????????
                .withStrings("extra_except_path", arrayOf(CoreZygote.getPathServices().getUserPath()))  // ??????????????????
                .requestCode(CODE_ATTACHMENTS)
                .go()
    }

    // ????????????
    fun previewAttachment(attachment: Attachment) {
        val fileType = AttachmentUtils.getAttachmentFileType(Integer.valueOf(attachment.type))
        if (TextUtils.isEmpty(fileType)) {
            error(context.getString(R.string.meeting_create_won_t_support))
        } else {
            try {
                val intent = AttachmentUtils.getIntent(v as Context,attachment.path,fileType)
                (v as Context).startActivity(intent)
            } catch (exp: Exception) {
                error(context.getString(R.string.meeting_create_dont_open))
            }
        }
    }

    /**
     * ????????????
     * @param address: ??????????????????????????????????????????????????????
     * @param title: ????????????
     * @param content: ????????????
     */
    fun release(address: String?, title: String, content: String, requestType: String) {
        if (TextUtils.isEmpty(m.r.roomId)
                || TextUtils.equals(m.r.roomId, "-1")) {
            if (CommonUtil.isBlankText(address)) {
                v.error(context.getString(R.string.meeting_create_address))
                return
            }
        }

        if (m.r.startYear == 0 && m.r.startMonth == 0 && m.r.startDay == 0 && m.r.startHour == 0 && m.r.startMinute == 0) {
            v.error(context.getString(R.string.meeting_create_start_time))
            return
        }

        if (m.r.endYear == 0 && m.r.endMonth == 0 && m.r.endDay == 0 && m.r.endHour == 0 && m.r.endMinute == 0) {
            v.error(context.getString(R.string.meeting_create_end_time))
            return
        }

        if (CommonUtil.isBlankText(title)) {
            v.error(context.getString(R.string.meeting_create_title))
            return
        }

        if (CommonUtil.isBlankText(content)) {
            v.error(context.getString(R.string.meeting_create_content))
            return
        }
        if (CommonUtil.isEmptyList(m.getAttendUsers())) {
            v.error(context.getString(R.string.meeting_create_attend_contact))
            return
        }
        if (CommonUtil.isBlankText(m.getCompereId())) {
            v.error(context.getString(R.string.meeting_create_compere))
            return
        }
        if (m.r.isStartTimeStale()) {
            v.error(context.getString(R.string.meeting_create_start_time_stale))
            return
        }
        if (!m.r.isTimeValidate()) {
            v.error(context.getString(R.string.meeting_create_time_validate))
            return
        }
        // ??????????????????????????????????????????


        // ????????????????????????
        m.publish(v as Context, address, title, content, requestType, object : MeetingModel.PublishWatcher {
            override fun start() {
                v.loading(true)
            }

            override fun progress(p: Int) {
                v.progress(p)
            }

            override fun completed() {
                v.loading(false)
                v.completed()
            }

            override fun error(e: java.lang.Exception?) {
                v.loading(false)
                v.error(context.getString(R.string.meeting_create_submit_error))
            }

            override fun errorTime(e: java.lang.Exception?) {
                v.loading(false)
                v.error(context.getString(R.string.meeting_create_occupy))
            }
        })
    }

    fun setPromptTime(p: Int) {
        m.setPromptTimes(p)
        v.promptTime(m.getPromptTime())
    }

    fun setMeetingType(p: Int) {
        m.setMeetingType(p)
        v.meetingType(m.getMeetingType())
    }

    fun setCompere(userId: String, username: String) {
        m.setCompere(userId, username)
        v.compere(username)
    }

    fun setRecorder(userId: String, username: String) {
        if (TextUtils.equals(m.getRecorderId(), userId)) {
            m.setRecorder("", "")
            v.recorder(context.getString(R.string.meeting_create_selected))
            return
        }
        m.setRecorder(userId, username)
        v.recorder(username)
    }

    fun setAttendUsers(users: MutableList<AddressBook>) {
        val userId = CoreZygote.getLoginUserServices().userId
        val initiator = CoreZygote.getAddressBookServices().queryUserInfo(userId)
        if (users.contains(initiator)) {
            users.remove(initiator)
        }
        users.add(0, initiator)
        m.setAttendUsers(users)
        v.attendUsers(users)
        v.showDeleteTextView(users.size > m.getAttendUsersFromServer().size)
    }

    fun removeAttendUser(user: AddressBook) {
        val userId = CoreZygote.getLoginUserServices().userId
        if (TextUtils.equals(userId, user.userId)) {
            FEMaterialDialog.Builder(v as Context)
                    .setCancelable(true)
                    .setMessage(context.getString(R.string.meeting_create_default_send_user))
                    .setPositiveButton(context.getString(R.string.meeting_create_yes), null)
                    .build()
                    .show()
            return
        }
        val attendUsers = m.getAttendUsersFromServer()
        for (addressBook: AddressBook in attendUsers) {
            if (TextUtils.equals(addressBook.userId, user.userId)) {
                FEMaterialDialog.Builder(v as Context)
                        .setCancelable(true)
                        .setMessage(context.getString(R.string.meeting_create_cannot_delete_attended_user))
                        .setPositiveButton(context.getString(R.string.meeting_create_yes), null)
                        .build()
                        .show()
                return
            }
        }

        m.removeAttendee(user)
        v.attendUsers(m.getAttendUsers())
        v.showDeleteTextView(m.getAttendUsers()!!.size > m.getAttendUsersFromServer().size)
    }

    fun setAttachments(attachments: MutableList<String>) {
        m.setAttachments(attachments)
        v.attachments(m.getAttachments())
    }

    fun removeAttachment(attachment: Attachment) {
        m.removeAttachment(attachment)
        v.attachments(m.getAttachments())
    }

}