package cn.flyrise.feep.workplan7

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailResponse
import cn.flyrise.android.protocol.model.Reply
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.collection.CollectionFolderActivity
import cn.flyrise.feep.collection.CollectionFolderFragment
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.common.utils.PreferencesUtils
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.common.LuBan7
import cn.flyrise.feep.particular.views.ParticularReplyEditView
import cn.flyrise.feep.utils.Patches
import cn.flyrise.feep.workplan7.contract.PlanDetailContract
import cn.flyrise.feep.workplan7.fragment.PlanAttachmentFragment
import cn.flyrise.feep.workplan7.listener.PlanReplyListener
import cn.flyrise.feep.workplan7.presenter.PlanDetailPresenter
import cn.squirtlez.frouter.annotations.RequestExtras
import cn.squirtlez.frouter.annotations.Route
import kotlinx.android.synthetic.main.plan_activity_detail.*

@Route("/plan/detail")
@RequestExtras("EXTRA_MESSAGEID", "EXTRA_BUSINESSID")
class PlanDetailActivity : PlanPermissionsActiviity(), PlanDetailContract.IView {

    private val CODE_OPEN_ATTACHMENT_FOR_REPLY = 1024

    lateinit var mPresenter: PlanDetailContract.IPresenter
    private var attachmentView: View? = null

    private var mReplyLocalAttachments: ArrayList<String> = ArrayList()
    lateinit var mWindowManager: WindowManager
    private var mReplyEditView: ParticularReplyEditView? = null
    private var hindReplyViewTime: Long = 0
    private val mHandler = Handler()
    private lateinit var mToolbar: FEToolbar
    private var mVoiceInput: XunFeiVoiceInput? = null
    private var curReplyId: String? = null

    companion object {
        fun startActivity(activity: Activity, messageId: String, businessId: String) {
            val intent = Intent(activity, PlanDetailActivity::class.java)
            intent.putExtra(K.plan.EXTRA_MESSAGEID, messageId)
            intent.putExtra(K.plan.EXTRA_BUSINESSID, businessId)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan_activity_detail)
        SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        mToolbar = toolbar
        toolbar.title = getString(R.string.plan_detail_title)
    }

    override fun bindData() {
        super.bindData()
        mVoiceInput = XunFeiVoiceInput(this)
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mPresenter = PlanDetailPresenter(this, this, intent)
        mPresenter.requestDetailInfo()
        val schedule = FunctionManager.findModule(X.Func.Schedule)
        lyToSchedule.visibility = if (FunctionManager.isNative(X.Func.Schedule)
                && TextUtils.isEmpty(schedule.url)) View.VISIBLE else View.GONE
    }

    override fun bindListener() {
        super.bindListener()
        lyToCollaboration.setOnClickListener { mPresenter.plan2Collaboration(this) }
        lyToSchedule.setOnClickListener { mPresenter.plan2Schedule(this) }
        tvReply.setOnClickListener { displayReplyView(false, "", getString(R.string.submit)) }
        mVoiceInput?.setOnRecognizerDialogListener {
            val editText = mReplyEditView!!.getReplyEditText()
            val selection = editText.selectionStart
            XunFeiVoiceInput.setVoiceInputText(editText, it, selection)
        }
    }

    override fun displayHeadInfo(detail: WorkPlanDetailResponse) {
        lyHeadView.displayHeadContent(detail)
        mToolbar.setRightText(if (TextUtils.isEmpty(detail.favoriteId)) getString(R.string.plan_detail_task) else getString(R.string.plan_detail_task_cancel))
        isShowRightTask()
        mToolbar.setRightTextClickListener {
            if (TextUtils.isEmpty(detail.favoriteId)) {
                startActivityForResult(Intent(PlanDetailActivity@ this, CollectionFolderActivity::class.java).apply {
                    putExtra("mode", CollectionFolderFragment.MODE_SELECT)
                }, 886)
            } else {
                mPresenter.removeFromFavoriteFolder()
            }
        }
    }

    override fun onFavoriteStateChange(isAdd: Boolean) {
        mToolbar.rightText = if (isAdd) getString(R.string.plan_detail_task_cancel) else getString(R.string.plan_detail_task)
        isShowRightTask()
    }

    private fun isShowRightTask() {
        mToolbar.setRightTextVisbility(if (FunctionManager.hasPatch(Patches.PATCH_PLAN)) View.VISIBLE else View.GONE)
    }

    override fun displayContent(detail: WorkPlanDetailResponse) {
        val host = CoreZygote.getLoginUserServices().serverAddress
        scrollView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        lyContentView.setParticularContent(host, detail.content, true)
    }

    override fun displayAttachment(attachment: List<Attachment>?) {
        if (CommonUtil.isEmptyList(attachment)) {
            attachmentView?.visibility = View.GONE
            return
        }
        if (attachmentView == null) {
            val attachmentViewStub = findViewById<View>(R.id.viewStubAttachment) as ViewStub
            attachmentView = attachmentViewStub.inflate()
        }
        attachmentView?.visibility = View.VISIBLE
        val tvTile = attachmentView!!.findViewById<TextView>(R.id.tvAttachmentSize)
        tvTile.text = "附件(${attachment!!.size})"
        val fragment = PlanAttachmentFragment.getInstance(ArrayList(attachment), null, false, false)
        supportFragmentManager.beginTransaction()
                .replace(R.id.lyAttachmentContent, fragment)
                .show(fragment)
                .commit()
    }

    override fun displayReplyInfo(replys: List<Reply>?) {
        replyView.displayReply(this, replys, object : PlanReplyListener {
            override fun onReply(replyId: String) {
                displayReplyView(false, replyId, getString(R.string.submit))
            }
        })
    }

    override fun displayDetailFail() {
        FEToast.showMessage(getString(R.string.plan_loader_detail_error))
    }

    override fun showReplyButton(show: Boolean) {
        tvReply.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showLoading(show: Boolean) {
        if (show) LoadingHint.show(this) else LoadingHint.hide()
    }


    override fun showLoadingProgress(progress: Int) {
        LoadingHint.showProgress(progress)
    }

    override fun getActivity(): Activity = this

    @SuppressLint("ClickableViewAccessibility")
    private fun displayReplyView(withAttachment: Boolean, replyId: String?, btnText: String) {
        if (!TextUtils.equals(curReplyId, replyId)) {
            SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
        }
        curReplyId = replyId
        if (mReplyEditView != null) {
            return
        }
        mReplyEditView = ParticularReplyEditView(this)
        val params = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        mWindowManager.addView(mReplyEditView, params)

        mReplyEditView!!.setWithAttachment(withAttachment)
        mReplyEditView!!.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                hindReplyViewTime = System.currentTimeMillis()
                removeReplyEditView()
            }
            true
        }

        mReplyEditView!!.setOnKeyListener { v, keyCode, event ->
            FELog.i("OnKeyListener : " + event.action)
            if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                removeReplyEditView()
            }
            false
        }

        if (!TextUtils.isEmpty(btnText)) mReplyEditView!!.setSubmitButtonText(btnText)

        // 弹出软键盘
        mHandler.postDelayed(showKeyBordRunnable, 50)

        // 2. 设置附件图标点击事件
        mReplyEditView!!.setOnAttachmentButtonClickListener { view ->
            LuBan7.pufferGrenades(this, mReplyLocalAttachments, null, CODE_OPEN_ATTACHMENT_FOR_REPLY)
        }

        // 3. 设置语音输入图标点击事件
        mReplyEditView!!.setOnRecordButtonClickListener { view -> permissionRecord() }

        // 4. 提交回复
        mReplyEditView!!.setOnReplySubmitClickListener { view ->
            var replyContent = mReplyEditView!!.replyContent
            if (TextUtils.isEmpty(replyContent)) {
                FEToast.showMessage(resources.getString(R.string.reply_empty_msg))
                return@setOnReplySubmitClickListener
            } else {
                replyContent += resources.getString(R.string.fe_from_android_mobile)
                mPresenter.reply(replyId, replyContent, mReplyLocalAttachments)
            }
        }
    }

    private fun removeReplyEditView() {
        if (mReplyEditView == null) return
        mHandler.removeCallbacks(showKeyBordRunnable)
        DevicesUtil.hideKeyboard(mReplyEditView!!.replyEditText)
        if (mReplyEditView!!.windowToken != null) {
            mWindowManager.removeView(mReplyEditView)
        }
        mReplyEditView = null
    }

    override fun replySuccess() {
        LoadingHint.hide()
        FEToast.showMessage(resources.getString(R.string.reply_succ))
        if (CommonUtil.nonEmptyList<String>(mReplyLocalAttachments)) {
            mReplyLocalAttachments.clear()
            if (mReplyEditView != null) mReplyEditView!!.setAttachmentSize(0)
        }
        removeReplyEditView()
        mPresenter.requestDetailInfo()
        SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
    }

    override fun replyFail() {
        LoadingHint.hide()
        FEToast.showMessage(resources.getString(R.string.reply_failure))
    }

    private val showKeyBordRunnable = {
        mReplyEditView?.setFocusable()
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    override fun onRecordPermissionsResult() {
        mVoiceInput?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_OPEN_ATTACHMENT_FOR_REPLY && data != null) {
            mReplyLocalAttachments = data.getStringArrayListExtra("extra_local_file")
            mReplyEditView?.setAttachmentSize(if (CommonUtil.isEmptyList<String>(mReplyLocalAttachments)) 0 else mReplyLocalAttachments.size)
        } else if (requestCode == 886 && data != null) {
            val favoriteId = data.getStringExtra("favoriteId")
            if (!TextUtils.isEmpty(favoriteId)) {
                mPresenter.addToFavoriteFolder(favoriteId)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "")
    }

}