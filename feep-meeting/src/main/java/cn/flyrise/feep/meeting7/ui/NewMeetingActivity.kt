package cn.flyrise.feep.meeting7.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.DataKeeper
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.AttachmentViewer
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.attachments.bean.TaskInfo
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration
import cn.flyrise.feep.media.attachments.listener.SimpleAttachmentViewerListener
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.presenter.MeetingPresenter
import cn.flyrise.feep.meeting7.presenter.TimePresenter
import cn.flyrise.feep.meeting7.ui.adapter.MeetingAttachmentAdapter
import cn.flyrise.feep.meeting7.ui.adapter.MeetingAttendeeAdapter
import cn.flyrise.feep.meeting7.ui.bean.MeetingModel
import cn.flyrise.feep.meeting7.ui.bean.MeetingUpdateTookKit
import cn.flyrise.feep.meeting7.ui.bean.PublishCompleted
import cn.flyrise.feep.meeting7.ui.bean.RoomInfo
import kotlinx.android.synthetic.main.nms_activity_new_meeting.*
import org.greenrobot.eventbus.EventBus

/**
 * @author ZYP
 * @since 2018-06-22 11:22
 */
interface MeetingView {

    fun meetingTypes(types: List<String>)               // 展示会议类型列表
    fun promptTimes(times: List<String>)                // 展示提醒时间列表
    fun compere(n: String)                              // 展示选中的主持人
    fun recorder(n: String)                             // 展示选中的记录人
    fun meetingType(n: String)                          // 展示选中的会议类型
    fun promptTime(n: String)                           // 展示选中的提醒时间

    fun attendUsers(u: MutableList<AddressBook>?)        // 展示选中的参会人员
    fun attachments(a: MutableList<Attachment>?)         // 展示选中的附件
    fun attendUsersOld(u: MutableList<AddressBook>?)     // 展示选中的参会人员
    fun showDeleteTextView(s: Boolean)

    fun meetingTime(m: MeetingModel)
    fun customMeetingTime(m: MeetingModel)
    fun roomInfo(l: String?, t: String?, c: String?)       // 会议室基本信息

    fun error(e: String)
    fun loading(display: Boolean)
    fun progress(p: Int)
    fun completed()
    /**
     * 针对修改的会议，对部分控件进行隐藏或者屏蔽点击功能
     */
    fun meetingStatus(visible: Boolean)
}

const val CODE_ATTENDEES = 10          // Intent requestCode：参会人员
const val CODE_ATTACHMENTS = 20        // Intent requestCode: 附件
const val CODE_COMPERE = 30            // Intent requestCode: 主持人
const val CODE_RECORDER = 40           // Intent requestCode: 记录人

/**
 * 创建会议
 * Intent请求参数：isCustomMeeting
 */
class NewMeetingActivity : NotTranslucentBarActivity(), MeetingView {

    private var dialog: FELoadingDialog? = null
    private lateinit var p: MeetingPresenter

    private var attendUserAdapter: MeetingAttendeeAdapter? = null
    private var attachmentAdapter: MeetingAttachmentAdapter? = null
    private lateinit var attachmentViewer: AttachmentViewer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomInfo = intent.getParcelableExtra("roomInfo") as RoomInfo
        p = MeetingPresenter(this, this, MeetingModel(roomInfo))
        setContentView(R.layout.nms_activity_new_meeting)

        val isCustomMeeting = intent.getBooleanExtra("isCustomMeeting", false)
        val isUpdateMeeting = intent.getBooleanExtra("isUpdateMeeting", false)
        p.start(isCustomMeeting, isUpdateMeeting)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        val isCustomMeeting = intent.getBooleanExtra("isCustomMeeting", false)
        val isUpdateMeeting = intent.getBooleanExtra("isUpdateMeeting", false)
        toolbar.title = when {
            isCustomMeeting -> getString(R.string.meeting7_new_custom_title)
            isUpdateMeeting -> getString(R.string.meeting7_modify_title)
            else -> getString(R.string.meeting7_create_title)
        }
        val requestType = when {
            isUpdateMeeting -> "meetingEdit"
            else -> "meetingAdd"
        }

        meetingStatus(isUpdateMeeting)

        toolbar.rightText = getString(R.string.meeting7_create_confirm)
        toolbar.rightTextView.setTextColor(Color.parseColor("#28B9FF"))
        toolbar.setRightTextClickListener {
            val title = nmsEtTitle.text.toString()
            val content = nmsEtContent.text.toString()
            if (nmsEtCustomRoom.visibility == View.VISIBLE) {
                val room = nmsEtCustomRoom.text.toString()
                p.release(room, title, content, requestType)
            } else {
                p.release(null, title, content, requestType)
            }
        }
        toolbar.setNavigationOnClickListener {
            FEMaterialDialog.Builder(this@NewMeetingActivity)
                    .setMessage(getString(R.string.meeting7_create_confirm_modify_hint))
                    .setNegativeButton(null, null)
                    .setPositiveButton(null) {
                        finish()
                    }
                    .build()
                    .show()
        }
    }

    override fun bindData() {
        nmsTvLocation.text = p.m.r.roomName
        nmsTvPromptTimes.text = "30分钟"

        attendUserAdapter = MeetingAttendeeAdapter(this)
        nmsRVAttendUsers.layoutManager = GridLayoutManager(this, 6)
        nmsRVAttendUsers.isNestedScrollingEnabled = false
        nmsRVAttendUsers.itemAnimator = DefaultItemAnimator()
        nmsRVAttendUsers.adapter = attendUserAdapter

        attachmentAdapter = MeetingAttachmentAdapter()
        nmsRVAttachments.layoutManager = LinearLayoutManager(this)
        nmsRVAttachments.isNestedScrollingEnabled = false
        nmsRVAttachments.itemAnimator = DefaultItemAnimator()
        nmsRVAttachments.adapter = attachmentAdapter

        val configuration = DownloadConfiguration.Builder()
                .owner(CoreZygote.getLoginUserServices().userId)
                .downloadDir(CoreZygote.getPathServices().knowledgeCachePath)
                .encryptDir(CoreZygote.getPathServices().safeFilePath)
                .decryptDir(CoreZygote.getPathServices().tempFilePath)
                .create()
        attachmentViewer = AttachmentViewer(this, configuration)
        attachmentViewer.setAttachmentViewerListener(object : SimpleAttachmentViewerListener() {
            override fun onDownloadFailed() {
                FEToast.showMessage(getString(R.string.meeting7_create_no_open_attachment))
                loading(false)
            }

            override fun onDecryptFailed() {
                FEToast.showMessage(getString(R.string.meeting7_create_no_open_attachment))
                loading(false)
            }

            override fun onDownloadProgressChange(progress: Int) {
                progress(progress)
            }

            override fun onDownloadBegin(taskInfo: TaskInfo?) {
                loading(true)
            }

            override fun prepareOpenAttachment(intent: Intent?) {
                loading(false)
                try {
                    startActivity(intent)
                } catch (exp: Exception) {
                    kotlin.error(getString(R.string.meeting7_create_no_open_attachment_hint))
                }
            }
        })
    }

    override fun bindListener() {
        nmsTvMeetingType.setOnClickListener { p?.meetingTypes() }           // 选择会议类型
        nmsTvPromptTimes.setOnClickListener { p?.promptTimes() }            // 选择提醒时间
        nmsTvCompere.setOnClickListener { p?.compere() }                    // 选择主持人
        nmsTvRecorder.setOnClickListener { p?.recorder() }                  // 选择记录人
        nmsIvAddAttendUsers.setOnClickListener { p?.addAttendees() }        // 选择参会人员
        nmsIvAddAttachments.setOnClickListener { p?.addAttachments() }      // 选择附件

        attendUserAdapter?.setClickFunc { p?.removeAttendUser(it) }         // 点击头像删除参会人员
        attachmentAdapter?.setDeleteFunc { p?.removeAttachment(it) }        // 删除附件
        attachmentAdapter?.setPreviewFunc {
            // 附件预览
            if (TextUtils.isEmpty(it.id)) {
                p?.previewAttachment(it)
            } else {
                attachmentViewer.openAttachment(it.path, it.id, it.name)
            }
        }

        nmsEtContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {                   // 内容字数统计
                nmsTvContentCounter.text = "${s?.length}/70"
            }
        })
    }

    override fun roomInfo(l: String?, t: String?, c: String?) {
        if (nmsEtCustomRoom.visibility == View.VISIBLE) {
            nmsEtCustomRoom.setText(l)
        } else {
            nmsTvLocation.text = l
        }
        nmsEtTitle.setText(t)
        nmsEtContent.setText(c)
    }

    override fun meetingTime(m: MeetingModel) {
        nmsTimeView.setPresenter(TimePresenter(nmsTimeView, m))
    }

    override fun customMeetingTime(m: MeetingModel) {
        nmsTimeView.visibility = View.GONE
        nmsLayoutLocation.visibility = View.GONE
        nmsCustomTimeView.visibility = View.VISIBLE
        nmsEtCustomRoom.visibility = View.VISIBLE
        nmsCustomTimeView.setPresenter(TimePresenter(nmsCustomTimeView, m))
    }

    override fun meetingTypes(types: List<String>) {
        FEMaterialDialog.Builder(this)
                .setWithoutTitle(true)
                .setItems(types.toTypedArray()) { dialog, v, position ->
                    p?.setMeetingType(position)
                    dialog.dismiss()
                }
                .build()
                .show()
    }

    override fun promptTimes(times: List<String>) {
        FEMaterialDialog.Builder(this)
                .setWithoutTitle(true)
                .setItems(times.toTypedArray()) { dialog, v, position ->
                    p?.setPromptTime(position)
                    dialog.dismiss()
                }
                .build()
                .show()
    }

    override fun compere(n: String) {
        nmsTvCompere.text = n
    }

    override fun recorder(n: String) {
        nmsTvRecorder.text = n
    }

    override fun meetingType(n: String) {
        nmsTvMeetingType.text = n
    }

    override fun promptTime(n: String) {
        nmsTvPromptTimes.text = n
    }

    override fun attendUsers(u: MutableList<AddressBook>?) {
        attendUserAdapter?.setAttendees(u)
        nmsTvAttendUserCount.text = getString(R.string.meeting7_create_attend_person) + if (u?.size ?: 0 > 0) "(${u!!.size})" else ""
    }

    override fun attendUsersOld(u: MutableList<AddressBook>?) {
        attendUserAdapter?.setAttendeesOld(u)
    }

    override fun attachments(a: MutableList<Attachment>?) {
        attachmentAdapter?.setAttachments(a)
        nmsTvAttachmentCount.text = getString(R.string.meeting7_create_file) + if (a?.size ?: 0 > 0) "(${a!!.size})" else ""
    }

    override fun showDeleteTextView(s: Boolean) {
        if (s) nmsTvAttendeeDelTip.visibility = View.VISIBLE else nmsTvAttendeeDelTip.visibility = View.INVISIBLE
    }

    override fun error(e: String) {
        var error = e
        if (TextUtils.isEmpty(e)) {
            error = getString(R.string.meeting7_create_error_reset)
        }
        FEToast.showMessage(error)
    }

    override fun loading(display: Boolean) {
        if (display) {
            if (dialog == null) {
                dialog = FELoadingDialog.Builder(this).setCancelable(false).create()
            }
            dialog!!.show()
        } else {
            if (dialog != null && dialog!!.isShowing()) {
                dialog!!.hide()
                dialog = null
            }
        }
    }

    override fun progress(p: Int) {
        if (dialog != null) {
            dialog!!.updateProgress(p)
        }
    }

    override fun completed() {
        FEToast.showMessage(getString(R.string.meeting7_create_success))
        EventBus.getDefault().post(PublishCompleted(200))
        if (intent.getBooleanExtra("isUpdateMeeting", false)) {
            val d = MeetingUpdateTookKit.getData()
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("meetingId", d.meetingId)
            })
            MeetingUpdateTookKit.saveData(null) // 清除垃圾数据
        }
        finish()
    }

    override fun meetingStatus(visible: Boolean) {
        nmsTimeView.setTimeTypeViewVisiable(visible)
        nmsTvMeetingType.isEnabled = !visible
        nmsTvPromptTimes.isEnabled = !visible
        nmsTvCompere.isEnabled = !visible
        nmsTvRecorder.isEnabled = !visible
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_ATTENDEES) {
            val users = DataKeeper.getInstance().getKeepDatas(hashCode()) as MutableList<AddressBook>
            p?.setAttendUsers(users)
        } else if (requestCode == CODE_COMPERE) {
            val userId = data?.getStringExtra("user_id") ?: ""
            val username = data?.getStringExtra("user_name") ?: ""
            if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(username)) {
                p?.setCompere(userId, username)
            }
        } else if (requestCode == CODE_RECORDER) {
            val userId = data?.getStringExtra("user_id") ?: ""
            val username = data?.getStringExtra("user_name") ?: ""
            if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(username)) {
                p?.setRecorder(userId, username)
            }
        } else if (requestCode == CODE_ATTACHMENTS && data != null) {
            val files = data.getStringArrayListExtra("SelectionData") as MutableList<String>
            p?.setAttachments(files)
        }
    }

    override fun onBackPressed() {
        FEMaterialDialog.Builder(this@NewMeetingActivity)
                .setMessage(getString(R.string.meeting7_create_confirm_modify_hint))
                .setNegativeButton(null, null)
                .setPositiveButton(null) {
                    super.onBackPressed()
                }
                .build()
                .show()
    }

}