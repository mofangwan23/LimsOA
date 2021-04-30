package cn.flyrise.feep.meeting7.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.*
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.core.dialog.FEMaterialEditTextDialog
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.NetworkAttachmentListFragment
import cn.flyrise.feep.media.attachments.SingleAttachmentActivity
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment
import cn.flyrise.feep.media.common.LuBan7
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.presenter.MeetingDetailPresenter
import cn.flyrise.feep.meeting7.repository.MeetingDataRepository
import cn.flyrise.feep.meeting7.selection.time.normalTextColor
import cn.flyrise.feep.meeting7.selection.time.unableTextColor
import cn.flyrise.feep.meeting7.ui.adapter.MeetingAttendeeAdapter
import cn.flyrise.feep.meeting7.ui.adapter.MeetingReplyAdapter
import cn.flyrise.feep.meeting7.ui.bean.MeetingReply
import cn.flyrise.feep.meeting7.ui.bean.MeetingUpdateTookKit
import cn.flyrise.feep.meeting7.ui.bean.RoomInfo
import cn.flyrise.feep.meeting7.ui.component.ReplyContentView
import cn.flyrise.feep.meeting7.ui.component.STATE_ERROR
import cn.flyrise.feep.meeting7.ui.component.StatusView
import cn.squirtlez.frouter.FRouter
import cn.squirtlez.frouter.annotations.RequestExtras
import cn.squirtlez.frouter.annotations.Route
import kotlinx.android.synthetic.main.nms_activity_meeting_detail.*

/**
 * @author ZYP
 * @since 2018-06-21 09:16
 *
 */
interface MeetingDetailView {

    fun description(title: String, initiator: String,
                    meetingRoom: String,
                    roomLocation: String?,
                    meetingType: String?,
                    compere: String?,
                    recorder: String?,
                    content: String?)                                                   // 会议简介

    fun meetingTime(isAcrossDayMeeting: Boolean, startTime: String, endTime: String)    // 会议时间

    fun initiatorOutOfDateStatus()                      // 发起者过期状态
    fun initiatorStatus()                               // 发起者状态

    fun outOfDateStatus()                               // 过期状态
    fun attendStatus()                                  // 参加状态
    fun notAttendStatus()                               // 不参加状态
    fun untreatedStatus()                               // 未处理状态
    fun canceledStatus()                                // 取消状态
    fun unknownStatus()                                 // 未知状态

    fun loading(display: Boolean)
    fun progress(progress: Int)
    fun result(isSuccess: Boolean)                          // 详情获取结果
    fun getMeetingType(): String                            // 获取会议类型

    fun reply(replyId: String?)                             // 回复
    fun replySuccess()
    fun replies(replies: List<MeetingReply>?)               // 他人回复列表
    fun promptSuccess()                                     // 提醒成功

    fun cancelSuccess()                                     // 会议取消成功
    fun attachments(attachments: List<NetworkAttachment>?)  // 会议附件
    fun untreatedPrompt(isInitiator: Boolean, isOutOfDate: Boolean, isCancelStatus: Boolean)  // 参会统计
    fun attendee(attendee: MutableList<AddressBook>?, text: String)  // 人员展示
}

@Route("/meeting/detail")
@RequestExtras("meetingId","requestType")
class MeetingDetailActivity : NotTranslucentBarActivity(), MeetingDetailView {


    // 辣眼睛辣眼睛~
    private lateinit var toolBar: FEToolbar
    private lateinit var statusView: StatusView
    private lateinit var tvCancel: TextView                  // 底部：取消
    private lateinit var tvModify: TextView                  // 底部：修改
    private lateinit var tvReply: TextView                   // 底部：回复
    private lateinit var tvAttend: TextView                  // 底部：参加
    private lateinit var tvNotAttend: TextView               // 底部：不参加

    private lateinit var tvAttendState: TextView
    private lateinit var ivAttendState: ImageView

    private lateinit var tvTitle: TextView                   // 会议主题
    private lateinit var tvInitiator: TextView               // 会议发起人
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var tvStartTimeSupplement: TextView
    private lateinit var tvRoom: TextView                    // 会议室名称
    private lateinit var tvLocation: TextView                // 会议室地址
    private lateinit var tvType: TextView                    // 会议类型
    private lateinit var tvCompere: TextView                 // 主持人
    private lateinit var tvRecorder: TextView                // 记录人
    private lateinit var tvContent: TextView                 // 会议内容

    private lateinit var layoutAttachmentContainer: View            // 会议附件 nmsLayoutAttachments 容器
    private lateinit var tvAttachmentSize: TextView

    private lateinit var rvAttendee: RecyclerView                   // 参会人员的网格列表
    private lateinit var attendeeAdapter: MeetingAttendeeAdapter
    private lateinit var tvStatistics: TextView                     // 会议人员统计（办理未办理）
    private lateinit var layoutUntreatedPrompt: View                    // 提醒一下
    private lateinit var ivUntreatedPrompt: ImageView
    private lateinit var tvUntreatedPrompt: TextView

    private lateinit var rvReply: RecyclerView                      // 回复列表
    private lateinit var replyAdapter: MeetingReplyAdapter
    private lateinit var emptyReplyView: View                       // 空回复
    private lateinit var tvReplyTitle: TextView                     // 他人回复数量
    private var replyView: ReplyContentView? = null                             // 底部回复控件

    private lateinit var btHaoshitong: Button

    private var curReplyId: String? = null

    private lateinit var wm: WindowManager
    private var loadingDialog: FELoadingDialog? = null
    private var presenter: MeetingDetailPresenter? = null
    private var handler = Handler()
    private var keyboardTask: Runnable = Runnable {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setContentView(R.layout.nms_activity_meeting_detail)
        SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        this.toolBar = toolbar
        this.toolBar.title = getString(R.string.meeting7_detail_title)
    }

    override fun bindView() {
        statusView = findViewById(R.id.nmsStatusView)
        tvTitle = findViewById(R.id.nmsTvMeetingTitle)
        tvInitiator = findViewById(R.id.nmsTvMeetingInitiator)
        tvStartTime = findViewById(R.id.nmsTvMeetingStartTime)
        tvEndTime = findViewById(R.id.nmsTvMeetingEndTime)
        tvStartTimeSupplement = findViewById(R.id.nmsTvStartTimeSupplement)
        tvRoom = findViewById(R.id.nmsTvMeetingRoom)
        tvLocation = findViewById(R.id.nmsTvMeetingLocation)
        tvType = findViewById(R.id.nmsTvMeetingType)
        tvCompere = findViewById(R.id.nmsTvMeetingCompere)
        tvRecorder = findViewById(R.id.nmsTvMeetingRecord)
        tvContent = findViewById(R.id.nmsTvMeetingContent)

        tvAttendState = findViewById(R.id.nmsTvMeetingProcessState)
        ivAttendState = findViewById(R.id.nmsIvMeetingAttendState)

        tvCancel = findViewById(R.id.nmsTvMeetingCancel)
        tvModify = findViewById(R.id.nmsTvMeetingModify)
        tvReply = findViewById(R.id.nmsTvMeetingReply)
        tvAttend = findViewById(R.id.nmsTvMeetingAttend)
        tvNotAttend = findViewById(R.id.nmsTvMeetingNotAttend)

        layoutAttachmentContainer = findViewById(R.id.nmsLayoutAttachmentsContainer)!!
        tvAttachmentSize = findViewById(R.id.nmsTvMeetingAttachmentSize)

        // 回复相关
        tvReplyTitle = findViewById(R.id.nmsTvReplySubTitle)
        emptyReplyView = findViewById(R.id.nmsLayoutEmptyReply)!!
        rvReply = findViewById(R.id.nmsReplyRecyclerView)
        rvReply.layoutManager = LinearLayoutManager(this)
        rvReply.itemAnimator = DefaultItemAnimator()
        rvReply.isNestedScrollingEnabled = false
        rvReply.setHasFixedSize(true);
        replyAdapter = MeetingReplyAdapter()
        rvReply.adapter = replyAdapter

        // 参会统计
        tvStatistics = findViewById(R.id.nmsTvMeetingStatistics)
        layoutUntreatedPrompt = findViewById(R.id.nmsLayoutMeetingPrompt)!!
        ivUntreatedPrompt = findViewById(R.id.nmsIvMeetingPrompt)
        tvUntreatedPrompt = findViewById(R.id.nmsTvMeetingPrompt)

        //好视通
        btHaoshitong = findViewById(R.id.bt_haoshitong)

        attendeeAdapter = MeetingAttendeeAdapter(this)
        rvAttendee = (findViewById(R.id.nmsAttendeeRecyclerView) as RecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@MeetingDetailActivity, 6)
            itemAnimator = DefaultItemAnimator()
            isNestedScrollingEnabled = false
            adapter = attendeeAdapter
        }

        tvStatistics.setOnClickListener {
            val text = tvStatistics.text
            val s = Statistics(presenter?.untreatedCount() ?: 0,
                    presenter?.notAttendCount() ?: 0,
                    presenter?.attendCount() ?: 0, when {
                text.startsWith(getString(R.string.meeting7_detail_not_handled)) -> 0
                text.startsWith(getString(R.string.meeting7_detail_no_attend)) -> 1
                text.startsWith(getString(R.string.meeting7_detail_attend)) -> 2
                else -> 0
            })

            StatisticsDialog.newInstance(s) { presenter?.fetchAttendees(it) }.show(supportFragmentManager, "ABCD")
        }

        btHaoshitong.setOnClickListener{
            val intent = Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            val cn = ComponentName("com.inpor.fastmeetingcloud", "com.inpor.fastmeetingcloud.activity.GuideActivity");
            intent.setComponent(cn);
            if (intent.resolveActivityInfo(this.getPackageManager(), PackageManager.MATCH_DEFAULT_ONLY) != null) {//启动的intent存在
                startActivity(intent);
            } else {
                FEToast.showMessage(getString(R.string.meeting7_detail_app_not_install))
            }
        }
    }

    override fun bindData() {
        val meetingId = intent.getStringExtra("meetingId")
        if (TextUtils.isEmpty(meetingId)) {
            FEToast.showMessage(getString(R.string.meeting7_detail_id_no_exit))
            finish()
            return
        }
        presenter = MeetingDetailPresenter(MeetingDataRepository(), this)
        presenter?.setRequestType(intent.getStringExtra("requestType"))
        presenter?.start(meetingId)

    }

    /**
     * 会议发起者的正常状态
     */
    override fun initiatorStatus() {

        tvCancel.visibility = View.VISIBLE
        tvModify.visibility = View.VISIBLE

        ivAttendState.visibility = View.VISIBLE
        ivAttendState.setImageResource(R.mipmap.nms_ic_state_attend)

        tvCancel.setOnClickListener { cancel() }
        tvModify.setOnClickListener {
            MeetingUpdateTookKit.saveData(presenter?.getMeetingDetail())
            val intent = Intent(MeetingDetailActivity@ this, NewMeetingActivity::class.java)
            intent.putExtra("isUpdateMeeting", true)
            intent.putExtra("roomInfo", RoomInfo())
            startActivityForResult(intent, 888)
        }

        toolBar.rightText = getString(R.string.meeting7_detail_zxing_title)
        toolBar.setRightTextClickListener {
            if (presenter?.qrCode() != null) {
                val qrCode = GsonUtil.getInstance().toJson(presenter!!.qrCode())
                FRouter.build(MeetingDetailActivity@ this, "/meeting/qrcode")
                        .withString("qrCode", qrCode)
                        .withString("title", presenter?.getMeetingDetail()?.topics)
                        .go()
            } else {
                FEToast.showMessage(getString(R.string.meeting7_detail_zxing_get_failed))
            }
        }
    }

    /**
     * 会议发起者的过期状态
     */
    override fun initiatorOutOfDateStatus() {
        ivAttendState.visibility = View.VISIBLE
        ivAttendState.setImageResource(R.mipmap.nms_ic_state_finished)
//        tvModify.visibility=View.VISIBLE
//        tvCancel.visibility = View.VISIBLE
//        tvModify.setBackgroundColor(Color.parseColor("#BFEAFF"))
//        tvCancel.setBackgroundColor(Color.parseColor("#BFEAFF"))
//        tvModify.isEnabled = false
//        tvCancel.isEnabled = false
    }

    /**
     * 参与者过期状态
     */
    override fun outOfDateStatus() {
        ivAttendState.visibility = View.VISIBLE
        ivAttendState.setImageResource(R.mipmap.nms_ic_state_finished)
    }

    /**
     * 参与者参加状态
     */
    override fun attendStatus() {
        tvAttendState.visibility = View.GONE
        ivAttendState.visibility = View.VISIBLE
        ivAttendState.setImageResource(R.mipmap.nms_ic_state_attend)

        tvReply.visibility = View.VISIBLE
        tvAttend.visibility = View.GONE
        tvNotAttend.visibility = View.GONE
        tvReply.setOnClickListener { reply(null) }
    }

    /**
     * 参与者不参加状态
     */
    override fun notAttendStatus() {
        tvAttendState.visibility = View.GONE
        ivAttendState.visibility = View.VISIBLE
        ivAttendState.setImageResource(R.mipmap.nms_ic_state_no_attend)

        tvReply.visibility = View.VISIBLE
        tvAttend.visibility = View.GONE
        tvNotAttend.visibility = View.GONE
        tvReply.setOnClickListener { reply(null) }
    }

    /**
     * 参与者未处理状态
     */
    override fun untreatedStatus() {
        tvAttendState.visibility = View.VISIBLE
        ivAttendState.visibility = View.INVISIBLE

        tvReply.visibility = View.VISIBLE
        tvAttend.visibility = View.VISIBLE
        tvNotAttend.visibility = View.VISIBLE

        tvReply.setOnClickListener { reply(null) }
        tvAttend.setOnClickListener { attend() }
        tvNotAttend.setOnClickListener { notAttend() }
    }

    /**
     * 已取消状态
     */
    override fun canceledStatus() {
        ivAttendState.visibility = View.VISIBLE
        ivAttendState.setImageResource(R.mipmap.nms_ic_state_cancel)  // 改成已取消
    }

    /**
     * 未知状态
     */
    override fun unknownStatus() {
        ivAttendState.visibility = View.GONE
        tvAttendState.visibility = View.GONE
    }

    /**
     * 会议描述
     */
    override fun description(title: String, initiator: String,
                             meetingRoom: String, roomLocation: String?,
                             meetingType: String?, compere: String?, recorder: String?, content: String?) {

        tvTitle.text = title
        tvRoom.text = meetingRoom

        // 发起人
        if (TextUtils.isEmpty(initiator)) {
            nmsLayoutMeetingInitiator.visibility = View.GONE
        } else {
            nmsLayoutMeetingInitiator.visibility = View.VISIBLE
            tvInitiator.text = initiator
        }

        // 会议地址
        val roomLocationLayout = findViewById<View>(R.id.nmsLayoutMeetingLocation)
        if (TextUtils.isEmpty(roomLocation)) {
            roomLocationLayout.visibility = View.GONE
        } else {
            roomLocationLayout.visibility = View.VISIBLE
            tvLocation.text = roomLocation
        }

        // 会议类型
        val typeLayout = findViewById<View>(R.id.nmsLayoutMeetingType)
        if (TextUtils.isEmpty(roomLocation)) {
            typeLayout.visibility = View.GONE
        } else {
            typeLayout.visibility = View.VISIBLE
            tvType.text = meetingType
        }

        // 主持人
        val compereLayout = findViewById<View>(R.id.nmsLayoutMeetingCompere)
        if (TextUtils.isEmpty(roomLocation)) {
            compereLayout.visibility = View.GONE
        } else {
            compereLayout.visibility = View.VISIBLE
            tvCompere.text = compere
        }

        // 记录人
        val recorderLayout = findViewById<View>(R.id.nmsLayoutMeetingRecord)
        if (TextUtils.isEmpty(recorder)) {
            recorderLayout.visibility = View.GONE
        } else {
            recorderLayout.visibility = View.VISIBLE
            tvRecorder.text = recorder
        }

        // 内容
        if (TextUtils.isEmpty(content)) {
            tvContent.setTextColor(unableTextColor)
            tvContent.text = getString(R.string.meeting7_create_remind_hint)
        } else {
            tvContent.setTextColor(normalTextColor)
            tvContent.text = content
        }
    }

    /**
     * 会议时间: 区分当天和跨天
     */
    override fun meetingTime(isAcrossDayMeeting: Boolean, startTime: String, endTime: String) {
        if (!isAcrossDayMeeting) {  // 当天
            tvEndTime.visibility = View.GONE
            tvStartTimeSupplement.visibility = View.VISIBLE

            tvStartTime.text = startTime
            tvStartTimeSupplement.text = endTime
            return
        }

        // 跨天
        tvEndTime.visibility = View.VISIBLE
        tvStartTimeSupplement.visibility = View.GONE
        tvStartTime.text = startTime
        tvEndTime.text = endTime
    }

    /**
     * 会议附件
     */
    override fun attachments(attachments: List<NetworkAttachment>?) {
        if (CommonUtil.isEmptyList(attachments)) {
            layoutAttachmentContainer.visibility = View.GONE
            return
        }

        layoutAttachmentContainer.visibility = View.VISIBLE
        tvAttachmentSize.setText(getString(R.string.meeting7_create_file_title)+"(${attachments?.size})")

        val fragment = NetworkAttachmentListFragment.newInstance(true, attachments, null)
        supportFragmentManager.beginTransaction()
                .replace(R.id.nmsLayoutAttachments, fragment)
                .show(fragment)
                .commit()
    }

    /**
     * 参会统计：通知一下
     */
    override fun untreatedPrompt(isInitiator: Boolean, isOutOfDate: Boolean, isCancelStatus: Boolean) {
        if (!isInitiator || isOutOfDate || isCancelStatus) {
            layoutUntreatedPrompt.visibility = View.GONE
            return
        }

        layoutUntreatedPrompt.visibility = View.VISIBLE
        layoutUntreatedPrompt.setOnClickListener { presenter?.promptUntreatedUsers() }
    }

    /**
     * 参会统计：人员刷新
     */
    override fun attendee(attendee: MutableList<AddressBook>?, text: String) {
        tvStatistics.text = text
        attendeeAdapter.setAttendees(attendee)
        if (CommonUtil.isEmptyList(attendee)) {
            if (layoutUntreatedPrompt.visibility == View.VISIBLE) {
                layoutUntreatedPrompt.visibility = View.GONE
            }
        }
    }

    /**
     * 回复区域
     */
    override fun replies(replies: List<MeetingReply>?) {
        if (CommonUtil.isEmptyList(replies)) {
            tvReplyTitle.text = getString(R.string.meeting7_detail_reply_title)
            emptyReplyView.visibility = View.VISIBLE
            return
        }

        emptyReplyView.visibility = View.GONE
        tvReplyTitle.text = getString(R.string.meeting7_detail_reply_title)+"(${replies!!.size})"
        replyAdapter.setDataSources(replies)

        // 显示附件
        replyAdapter.setOnAttachmentClickListener { attachment, view ->
            startActivity(Intent(this@MeetingDetailActivity, SingleAttachmentActivity::class.java).apply {
                putExtra("NetworkAttachment", attachment)
            })
        }

        // 子回复
        replyAdapter.setOnReplyClickListener { reply(it.id) }
    }

    /**
     * 会议回复成功
     */
    override fun replySuccess() {
        FEToast.showMessage(getString(R.string.meeting7_detail_reply_success))
        if (replyView != null) {
            val editText = replyView!!.getEditText()!!
            editText.setText("")
            DevicesUtil.hideKeyboard(editText)

            if (replyView!!.windowToken != null) {
                wm.removeView(replyView)
            }

            replyView!!.setAttachmentSize(0)
        }

        replyView = null
        presenter?.clearSelectedAttachment()
        SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
    }

    /**
     * 提醒成功
     */
    override fun promptSuccess() {
        FEToast.showMessage(getString(R.string.meeting7_detail_remind_success))
        layoutUntreatedPrompt.isEnabled = false
        tvUntreatedPrompt.setTextColor(Color.parseColor("#8B8C8C"))
        ivUntreatedPrompt.setImageResource(R.mipmap.nms_ic_unprocess_prompt_unable)
    }

    /**
     * 会议取消成功
     */
    override fun cancelSuccess() {
        FEToast.showMessage(getString(R.string.meeting7_detail_cancel_success))
        finish()
    }

    /**
     * 会议详情获取结果
     */
    override fun result(isSuccess: Boolean) {
        if (isSuccess) {
            statusView.visibility = View.GONE
            nmsScrollView.visibility = View.VISIBLE
            return
        }

        nmsScrollView.visibility = View.GONE
        FEToast.showMessage(getString(R.string.meeting7_detail_load_failed))
        statusView.visibility = View.VISIBLE
        statusView.setStatus(STATE_ERROR)
        statusView.setOnRetryClickListener(View.OnClickListener {
            presenter?.start(intent.getStringExtra("meetingId"))
        })
    }

    override fun loading(display: Boolean) {
        if (display) {
            if (loadingDialog == null) {
                loadingDialog = FELoadingDialog.Builder(this).setCancelable(true).create()
            }
            loadingDialog!!.show()
            return
        }

        if (loadingDialog != null && loadingDialog!!.isShowing()) {
            loadingDialog!!.hide()
            loadingDialog = null
        }
    }

    override fun progress(progress: Int) {
        if (loadingDialog != null) {
            loadingDialog!!.updateProgress(progress)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && data != null) {
            presenter?.selectedAttachments = data.getStringArrayListExtra("extra_local_file")
            (replyView as ReplyContentView).setAttachmentSize(presenter?.selectedAttachments?.size
                    ?: 0)
        } else if (requestCode == 888 && data != null) {
            val meetingId = data.getStringExtra("meetingId")
            if (TextUtils.isEmpty(meetingId)) {
                return
            }
            presenter?.start(meetingId)
        }
    }

    override fun getMeetingType(): String {
        var type = intent.getStringExtra("meetingType")
        if (type == null) type = ""
        return type
    }

    /**
     * 弹出 PopupWindow 进行内容回复
     */
    override fun reply(replyId: String?) {
        if (!TextUtils.equals(curReplyId, replyId)) {
            SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
        }
        curReplyId = replyId
        displayReplyWindow(replyId, "", getString(R.string.meeting7_detail_reply)) { presenter?.replyMeeting(it) }
    }

    /**
     * 参加会议
     */
    private fun attend() {
        if (!TextUtils.equals(curReplyId, "参加")) {
            SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
        }
        curReplyId ="参加"
        displayReplyWindow(null, getString(R.string.meeting7_detail_reply_attend_time)
                , getString(R.string.meeting7_detail_reply_attend_time)) { presenter?.attendMeeting(it) }
    }

    /**
     * 不参加会议
     */
    private fun notAttend() {
        if (!TextUtils.equals(curReplyId, "不参加")) {
            SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
        }
        curReplyId ="不参加"
        displayReplyWindow(null, getString(R.string.meeting7_detail_reply_no_attend_time)
                , getString(R.string.meeting7_detail_reply_no_attend)) { presenter?.notAttendMeeting(it) }
    }

    /**
     * 取消会议
     */
    private fun cancel() {
        FEMaterialEditTextDialog.Builder(this)
                .setCancelable(false)
                .setDefaultText(getString(R.string.meeting7_detail_cancel_hint))
                .setTitle(getString(R.string.meeting7_detail_cancel))
                .setPositiveButton(getString(R.string.meeting7_detail_no), null)
                .setNegativeButton(getString(R.string.meeting7_detail_cancel), { dialog, input, check ->
                    var i = input.length
                    if(input.length>10){
                        FEToast.showMessage(getString(R.string.meeting7_detail_hint_text_error))
                        return@setNegativeButton
                    }
                    presenter?.cancelMeeting(input)
                })
                .build()
                .show()
    }

    private fun displayReplyWindow(replyId: String?, defaultContent: String?, title: String, func: (String) -> Unit) {
        val isReplyFunc = TextUtils.isEmpty(defaultContent)
        replyView = ReplyContentView(this).apply {
            setTitle(title)
            if (!isReplyFunc) {
                setHint(defaultContent)
            }
        }

        if (isReplyFunc) {
            replyView?.getEditText()?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val len = s?.trim()?.length ?: 0
                    replyView?.setSubmitEnable(len != 0)
                }
            })
            replyView?.setSubmitEnable(false)
        }

        replyView?.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                window.apply {
                    attributes = window.attributes.apply {
                        addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                        dimAmount = 1.0f
                        alpha = 1.0f
                    }
                }
            }

            override fun onViewAttachedToWindow(v: View?) {
                window.apply {
                    attributes = window.attributes.apply {
                        addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                        dimAmount = 0.7f
                        alpha = 0.7f
                    }
                }
            }
        })

        // 1. 初始化底部回复框
        val params = WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM

        wm.addView(replyView, params)
        replyView?.setFocusable(true)

        val close = {
            handler.removeCallbacks(keyboardTask)
            DevicesUtil.hideKeyboard(replyView?.getEditText())
            if (replyView?.windowToken != null) {
                wm.removeView(replyView)
            }
            replyView = null
        }

        replyView?.setOnTouchListener({ view, event ->
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                close()
            }
            true
        })
        replyView?.setOnKeyListener({ v, keyCode, event ->
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
                close()
            }
            false
        })
        replyView?.setCloseClickListener(View.OnClickListener { close() })

        // 弹出软键盘
        handler.postDelayed(keyboardTask, 50)

        // 2. 设置附件图标点击事件
        replyView?.setAttachmentClickListener(View.OnClickListener {
            LuBan7.pufferGrenades(this@MeetingDetailActivity, presenter?.selectedAttachments, null, 100)
        })

        // 3. 提交
        replyView?.setSubmitClickListener(View.OnClickListener {
            var content = replyView?.getContent()
            if (TextUtils.isEmpty(content)) {
                content = defaultContent
                if (TextUtils.isEmpty(content)) {
                    FEToast.showMessage(getString(R.string.meeting7_detail_input_reply_text))
                    return@OnClickListener
                }
            }

            val replyContent = content ?: ""
            if (TextUtils.isEmpty(replyId)) {
                func(replyContent)
            } else {
                presenter?.replyToSomeone(replyContent, replyId!!)
            }

            if (loadingDialog != null) {
                loadingDialog?.setOnDismissListener {
                    presenter?.cancelUploadTask()
                    replyView?.setAttachmentSize(0)
                    loadingDialog?.removeDismissListener()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
    }

}