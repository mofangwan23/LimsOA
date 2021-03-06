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
                    content: String?)                                                   // ????????????

    fun meetingTime(isAcrossDayMeeting: Boolean, startTime: String, endTime: String)    // ????????????

    fun initiatorOutOfDateStatus()                      // ?????????????????????
    fun initiatorStatus()                               // ???????????????

    fun outOfDateStatus()                               // ????????????
    fun attendStatus()                                  // ????????????
    fun notAttendStatus()                               // ???????????????
    fun untreatedStatus()                               // ???????????????
    fun canceledStatus()                                // ????????????
    fun unknownStatus()                                 // ????????????

    fun loading(display: Boolean)
    fun progress(progress: Int)
    fun result(isSuccess: Boolean)                          // ??????????????????
    fun getMeetingType(): String                            // ??????????????????

    fun reply(replyId: String?)                             // ??????
    fun replySuccess()
    fun replies(replies: List<MeetingReply>?)               // ??????????????????
    fun promptSuccess()                                     // ????????????

    fun cancelSuccess()                                     // ??????????????????
    fun attachments(attachments: List<NetworkAttachment>?)  // ????????????
    fun untreatedPrompt(isInitiator: Boolean, isOutOfDate: Boolean, isCancelStatus: Boolean)  // ????????????
    fun attendee(attendee: MutableList<AddressBook>?, text: String)  // ????????????
}

@Route("/meeting/detail")
@RequestExtras("meetingId","requestType")
class MeetingDetailActivity : NotTranslucentBarActivity(), MeetingDetailView {


    // ??????????????????~
    private lateinit var toolBar: FEToolbar
    private lateinit var statusView: StatusView
    private lateinit var tvCancel: TextView                  // ???????????????
    private lateinit var tvModify: TextView                  // ???????????????
    private lateinit var tvReply: TextView                   // ???????????????
    private lateinit var tvAttend: TextView                  // ???????????????
    private lateinit var tvNotAttend: TextView               // ??????????????????

    private lateinit var tvAttendState: TextView
    private lateinit var ivAttendState: ImageView

    private lateinit var tvTitle: TextView                   // ????????????
    private lateinit var tvInitiator: TextView               // ???????????????
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var tvStartTimeSupplement: TextView
    private lateinit var tvRoom: TextView                    // ???????????????
    private lateinit var tvLocation: TextView                // ???????????????
    private lateinit var tvType: TextView                    // ????????????
    private lateinit var tvCompere: TextView                 // ?????????
    private lateinit var tvRecorder: TextView                // ?????????
    private lateinit var tvContent: TextView                 // ????????????

    private lateinit var layoutAttachmentContainer: View            // ???????????? nmsLayoutAttachments ??????
    private lateinit var tvAttachmentSize: TextView

    private lateinit var rvAttendee: RecyclerView                   // ???????????????????????????
    private lateinit var attendeeAdapter: MeetingAttendeeAdapter
    private lateinit var tvStatistics: TextView                     // ???????????????????????????????????????
    private lateinit var layoutUntreatedPrompt: View                    // ????????????
    private lateinit var ivUntreatedPrompt: ImageView
    private lateinit var tvUntreatedPrompt: TextView

    private lateinit var rvReply: RecyclerView                      // ????????????
    private lateinit var replyAdapter: MeetingReplyAdapter
    private lateinit var emptyReplyView: View                       // ?????????
    private lateinit var tvReplyTitle: TextView                     // ??????????????????
    private var replyView: ReplyContentView? = null                             // ??????????????????

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

        // ????????????
        tvReplyTitle = findViewById(R.id.nmsTvReplySubTitle)
        emptyReplyView = findViewById(R.id.nmsLayoutEmptyReply)!!
        rvReply = findViewById(R.id.nmsReplyRecyclerView)
        rvReply.layoutManager = LinearLayoutManager(this)
        rvReply.itemAnimator = DefaultItemAnimator()
        rvReply.isNestedScrollingEnabled = false
        rvReply.setHasFixedSize(true);
        replyAdapter = MeetingReplyAdapter()
        rvReply.adapter = replyAdapter

        // ????????????
        tvStatistics = findViewById(R.id.nmsTvMeetingStatistics)
        layoutUntreatedPrompt = findViewById(R.id.nmsLayoutMeetingPrompt)!!
        ivUntreatedPrompt = findViewById(R.id.nmsIvMeetingPrompt)
        tvUntreatedPrompt = findViewById(R.id.nmsTvMeetingPrompt)

        //?????????
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
            if (intent.resolveActivityInfo(this.getPackageManager(), PackageManager.MATCH_DEFAULT_ONLY) != null) {//?????????intent??????
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
     * ??????????????????????????????
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
     * ??????????????????????????????
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
     * ?????????????????????
     */
    override fun outOfDateStatus() {
        ivAttendState.visibility = View.VISIBLE
        ivAttendState.setImageResource(R.mipmap.nms_ic_state_finished)
    }

    /**
     * ?????????????????????
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
     * ????????????????????????
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
     * ????????????????????????
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
     * ???????????????
     */
    override fun canceledStatus() {
        ivAttendState.visibility = View.VISIBLE
        ivAttendState.setImageResource(R.mipmap.nms_ic_state_cancel)  // ???????????????
    }

    /**
     * ????????????
     */
    override fun unknownStatus() {
        ivAttendState.visibility = View.GONE
        tvAttendState.visibility = View.GONE
    }

    /**
     * ????????????
     */
    override fun description(title: String, initiator: String,
                             meetingRoom: String, roomLocation: String?,
                             meetingType: String?, compere: String?, recorder: String?, content: String?) {

        tvTitle.text = title
        tvRoom.text = meetingRoom

        // ?????????
        if (TextUtils.isEmpty(initiator)) {
            nmsLayoutMeetingInitiator.visibility = View.GONE
        } else {
            nmsLayoutMeetingInitiator.visibility = View.VISIBLE
            tvInitiator.text = initiator
        }

        // ????????????
        val roomLocationLayout = findViewById<View>(R.id.nmsLayoutMeetingLocation)
        if (TextUtils.isEmpty(roomLocation)) {
            roomLocationLayout.visibility = View.GONE
        } else {
            roomLocationLayout.visibility = View.VISIBLE
            tvLocation.text = roomLocation
        }

        // ????????????
        val typeLayout = findViewById<View>(R.id.nmsLayoutMeetingType)
        if (TextUtils.isEmpty(roomLocation)) {
            typeLayout.visibility = View.GONE
        } else {
            typeLayout.visibility = View.VISIBLE
            tvType.text = meetingType
        }

        // ?????????
        val compereLayout = findViewById<View>(R.id.nmsLayoutMeetingCompere)
        if (TextUtils.isEmpty(roomLocation)) {
            compereLayout.visibility = View.GONE
        } else {
            compereLayout.visibility = View.VISIBLE
            tvCompere.text = compere
        }

        // ?????????
        val recorderLayout = findViewById<View>(R.id.nmsLayoutMeetingRecord)
        if (TextUtils.isEmpty(recorder)) {
            recorderLayout.visibility = View.GONE
        } else {
            recorderLayout.visibility = View.VISIBLE
            tvRecorder.text = recorder
        }

        // ??????
        if (TextUtils.isEmpty(content)) {
            tvContent.setTextColor(unableTextColor)
            tvContent.text = getString(R.string.meeting7_create_remind_hint)
        } else {
            tvContent.setTextColor(normalTextColor)
            tvContent.text = content
        }
    }

    /**
     * ????????????: ?????????????????????
     */
    override fun meetingTime(isAcrossDayMeeting: Boolean, startTime: String, endTime: String) {
        if (!isAcrossDayMeeting) {  // ??????
            tvEndTime.visibility = View.GONE
            tvStartTimeSupplement.visibility = View.VISIBLE

            tvStartTime.text = startTime
            tvStartTimeSupplement.text = endTime
            return
        }

        // ??????
        tvEndTime.visibility = View.VISIBLE
        tvStartTimeSupplement.visibility = View.GONE
        tvStartTime.text = startTime
        tvEndTime.text = endTime
    }

    /**
     * ????????????
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
     * ???????????????????????????
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
     * ???????????????????????????
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
     * ????????????
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

        // ????????????
        replyAdapter.setOnAttachmentClickListener { attachment, view ->
            startActivity(Intent(this@MeetingDetailActivity, SingleAttachmentActivity::class.java).apply {
                putExtra("NetworkAttachment", attachment)
            })
        }

        // ?????????
        replyAdapter.setOnReplyClickListener { reply(it.id) }
    }

    /**
     * ??????????????????
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
     * ????????????
     */
    override fun promptSuccess() {
        FEToast.showMessage(getString(R.string.meeting7_detail_remind_success))
        layoutUntreatedPrompt.isEnabled = false
        tvUntreatedPrompt.setTextColor(Color.parseColor("#8B8C8C"))
        ivUntreatedPrompt.setImageResource(R.mipmap.nms_ic_unprocess_prompt_unable)
    }

    /**
     * ??????????????????
     */
    override fun cancelSuccess() {
        FEToast.showMessage(getString(R.string.meeting7_detail_cancel_success))
        finish()
    }

    /**
     * ????????????????????????
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
     * ?????? PopupWindow ??????????????????
     */
    override fun reply(replyId: String?) {
        if (!TextUtils.equals(curReplyId, replyId)) {
            SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
        }
        curReplyId = replyId
        displayReplyWindow(replyId, "", getString(R.string.meeting7_detail_reply)) { presenter?.replyMeeting(it) }
    }

    /**
     * ????????????
     */
    private fun attend() {
        if (!TextUtils.equals(curReplyId, "??????")) {
            SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
        }
        curReplyId ="??????"
        displayReplyWindow(null, getString(R.string.meeting7_detail_reply_attend_time)
                , getString(R.string.meeting7_detail_reply_attend_time)) { presenter?.attendMeeting(it) }
    }

    /**
     * ???????????????
     */
    private fun notAttend() {
        if (!TextUtils.equals(curReplyId, "?????????")) {
            SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
        }
        curReplyId ="?????????"
        displayReplyWindow(null, getString(R.string.meeting7_detail_reply_no_attend_time)
                , getString(R.string.meeting7_detail_reply_no_attend)) { presenter?.notAttendMeeting(it) }
    }

    /**
     * ????????????
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

        // 1. ????????????????????????
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

        // ???????????????
        handler.postDelayed(keyboardTask, 50)

        // 2. ??????????????????????????????
        replyView?.setAttachmentClickListener(View.OnClickListener {
            LuBan7.pufferGrenades(this@MeetingDetailActivity, presenter?.selectedAttachments, null, 100)
        })

        // 3. ??????
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