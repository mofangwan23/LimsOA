package cn.flyrise.feep.workplan7

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.*
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity
import cn.flyrise.feep.collaboration.activity.RichTextEditActivity
import cn.flyrise.feep.collaboration.utility.RichTextContentKeeper
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseEditableActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.DataKeeper
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.email.views.UrlImageParser
import cn.flyrise.feep.event.EventPlanListRefresh
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.workplan7.adapter.PlanAttachmentAdapter
import cn.flyrise.feep.workplan7.adapter.PlanUserLayoutAdapter
import cn.flyrise.feep.workplan7.contract.PlanCreateContract
import cn.flyrise.feep.workplan7.fragment.PlanAttachmentFragment
import cn.flyrise.feep.workplan7.model.PlanContent
import cn.flyrise.feep.workplan7.presenter.PlanCreatePresenter
import cn.flyrise.feep.workplan7.view.BottomWheelSelectionDialog
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.plan7_activity_create.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * author : klc
 * Msg : 新增计划界面
 */
class Plan7CreateActivity : BaseEditableActivity(), PlanCreateContract.IView {

    private val IMAGE_STYLE = "<style type='text/css'>" +
            "body{word-wrap: break-word!important;" +
            "word-break:break-all!important;" +
            "text-align:justify!important;" +
            "text-justify:inter-ideograph!important;}" +
            "img{width:50%!important;}" +
            "</style>"

    private lateinit var mPresenter: PlanCreatePresenter
    private val numTitleMax = 50

    private var attachmentFragment: PlanAttachmentFragment? = null
    var typeButton: ArrayList<TextView>? = null

    companion object {

        fun startActivity(context: Context, type: Int) {
            startActivity(context, type, null)
        }

        fun startActivity(context: Context, type: Int, userIds: ArrayList<String>?) {
            val intent = Intent(context, Plan7CreateActivity::class.java)
            intent.putExtra("workPlanType", type)
            intent.putStringArrayListExtra("userIds",userIds)
            context.startActivity(intent)
        }

        fun startActivity(context: Context, planId: String) {
            val intent = Intent(context, Plan7CreateActivity::class.java)
            intent.putExtra("planId", planId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan7_activity_create)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        toolbar.title = getString(R.string.plan_create_work)
        toolbar.setNavigationOnClickListener { if (isHasWrote()) showExitDialog() else finish() }
    }

    override fun bindView() {
        super.bindView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            etContent.visibility = View.GONE
            planWebView.visibility = View.VISIBLE
            contentHint.visibility = View.VISIBLE
        }
        receiverListView.isNestedScrollingEnabled = false
        receiverListView.layoutManager = GridLayoutManager(this, 6)!!
        ccListView.isNestedScrollingEnabled = false
        ccListView.layoutManager = GridLayoutManager(this, 6)
        notificationListView.isNestedScrollingEnabled = false
        notificationListView.layoutManager = GridLayoutManager(this, 6)
        planWebView.loadDataWithBaseURL("", "", "text/html; charset=utf-8", "UTF-8", null)
    }

    override fun bindData() {
        super.bindData()
        mPresenter = PlanCreatePresenter(this, this)
        mPresenter.getPlanWeekStart()
        typeButton = arrayListOf(btDatePrev, btDateNow, btDateNet)
        mPresenter.initReceiverUser(intent?.getStringArrayListExtra("userIds"))
    }

    override fun getWeekStartComplete() {
        val planId: String? = intent?.getStringExtra("planId")
        if (planId.isNullOrEmpty()) {
            setTypeValue(intent.getIntExtra("workPlanType", K.plan.PLAN_FREQUENCY_DAT))
        } else {
            mPresenter.getTempPlanData(planId!!)
        }
    }

    override fun bindListener() {
        super.bindListener()
        lyType.setOnClickListener {
            val dialog = BottomWheelSelectionDialog()
            dialog.title = getString(R.string.plan_rule_selected_type)
            dialog.addValue(mPresenter.types.toList(), -1)
            dialog.onClickListener = { setTypeValue(mPresenter.types.indexOf(it[0]) + 1) }
            dialog.show(supportFragmentManager, "openFrequencyDialog")
        }
        btDatePrev.setOnClickListener {
            setTimeBtColor(btDatePrev)
            mPresenter.timTextClick(-1)
        }
        btDateNow.setOnClickListener {
            setTimeBtColor(btDateNow)
            mPresenter.timTextClick(0)
        }
        btDateNet.setOnClickListener {
            setTimeBtColor(btDateNet)
            mPresenter.timTextClick(1)
        }
        lyStatTime.setOnClickListener { mPresenter.openDateStartTimeDialog(lyStatTime) }
        lyEndTime.setOnClickListener { mPresenter.openDateEndTimeDialog(lyEndTime) }
        lyReceiverTitle.setOnClickListener { mPresenter.clickChooseUser(this, PlanCreateContract.REQUSETCODE_RECEIVER) }
        lyCCTitle.setOnClickListener { mPresenter.clickChooseUser(this, PlanCreateContract.REQUESTCODE_CCUSER) }
        lyNotificationTitle.setOnClickListener { mPresenter.clickChooseUser(this, PlanCreateContract.REQUESTCODE_NOTIFIER) }
        lyContent.setOnClickListener { openRichContentActivity() }
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                openRichContentActivity()
                return false
            }
        })
        planWebView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        RxView.clicks(btSend).throttleFirst(1, TimeUnit.SECONDS).subscribe({ mPresenter.createPlan() }, { it.printStackTrace() })
        RxView.clicks(btSave).throttleFirst(1, TimeUnit.SECONDS).subscribe({ mPresenter.savePlan() }, { it.printStackTrace() })
        lyAttachmentTitle.setOnClickListener { mPresenter.clickAttachment(this, attachmentFragment?.attachments) }

        etTitle.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var editable = etTitle.text
                val len = editable.length
                if (len > numTitleMax) {
                    var selEndIndex = Selection.getSelectionEnd(editable)
                    val str = editable.toString()
                    val newStr = str.substring(0, numTitleMax)
                    etTitle.setText(newStr)
                    editable = etTitle.text
                    val newLen = editable.length
                    if (selEndIndex > newLen) {
                        selEndIndex = editable.length
                    }
                    Selection.setSelection(editable, selEndIndex)
                } else {
                    tvNum.setText("$len/$numTitleMax")
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun setTypeValue(type: Int) {
        if (mPresenter.type == type) return
        mPresenter.type = type
        lyType.setContent(mPresenter.types[type - 1])
        if (type == K.plan.PLAN_TYPE_OTHER) {
            lyTypeContent.visibility = View.GONE
            lyOtherDate.visibility = View.VISIBLE
            mPresenter.calendarNull()
        } else {
            lyTypeContent.visibility = View.VISIBLE
            lyOtherDate.visibility = View.GONE
            typeButton?.forEachIndexed { index, textView -> textView.text = mPresenter.btTexts[type - 1][index] }
            lyStatTime.setContent("")
            lyEndTime.setContent("")
            setTimeBtColor(btDateNow)
            mPresenter.timTextClick(0)
        }
    }

    fun setTimeBtColor(clickTv: TextView) {
        typeButton?.forEach {
            it.setBackgroundResource(if (it == clickTv) R.drawable.button_circle_blue else R.drawable.button_circle_gray)
            it.setTextColor(Color.parseColor(if (it == clickTv) "#ffffff" else "#8B8C8C"))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            mPresenter.handleUserResult(requestCode) -> return
            mPresenter.handleAttachmentResult(requestCode, resultCode, data) -> return
            requestCode == PlanCreateContract.REQUSETCODE_CONTENT -> showRichContent(mPresenter.tryTransformImagePath())
        }
    }

    override fun showReceiverUser(users: List<AddressBook>?) {
        lyReceiverTitle.setTitle("${getString(R.string.plan_create_recevier_user)}${if (CommonUtil.isEmptyList(users)) "" else "(${users!!.size})"}")
        receiverListView.adapter = PlanUserLayoutAdapter(this, users)
    }

    override fun showCCUser(users: List<AddressBook>?) {
        lyCCTitle.setTitle("${getString(R.string.plan_create_cc_user)}${if (CommonUtil.isEmptyList(users)) "" else "(${users!!.size})"}")
        ccListView.adapter = PlanUserLayoutAdapter(this, users)
    }

    override fun showNotifierUser(users: List<AddressBook>?) {
        lyNotificationTitle.setTitle("${getString(R.string.plan_create_notifier)}${if (CommonUtil.isEmptyList(users)) "" else "(${users!!.size})"}")
        notificationListView.adapter = PlanUserLayoutAdapter(this, users)
    }

    override fun showAttachment(attachments: ArrayList<Attachment>?) {
        lyAttachmentTitle.setTitle("${getString(R.string.plan_create_attachment)}${if (CommonUtil.isEmptyList(attachments)) "" else "" + "(${attachments!!.size})"}")
        attachmentFragment = PlanAttachmentFragment.getInstance(attachments, null, false, true)
        attachmentFragment?.mDelCallback = object : PlanAttachmentAdapter.OnItemDelCallBack {
            override fun onDel(attachment: Attachment) {
                setAttachmentSize(attachmentFragment!!.attachments)
            }
        }
        supportFragmentManager.beginTransaction().replace(R.id.lyAttachmentContent, attachmentFragment!!).show(attachmentFragment!!)
                .commit()
    }

    private fun setAttachmentSize(attachments: List<Attachment>?) {
        lyAttachmentTitle.setTitle("${getString(R.string.plan_create_attachment)}${if (CommonUtil.isEmptyList(attachments)) "" else "(${attachments!!.size})"}")
    }

    /****************************************富文本************************************/
    private fun openRichContentActivity() {
        val intent = Intent(this, RichTextEditActivity::class.java)
        intent.putExtra("title", getString(R.string.lbl_content_hint))
        startActivityForResult(intent, PlanCreateContract.REQUSETCODE_CONTENT)
    }

    private fun showRichContent(content: String?) {
        if (TextUtils.isEmpty(content)) {
            contentHint.visibility = View.VISIBLE
            planWebView.loadDataWithBaseURL("", "", "text/html; charset=utf-8", "UTF-8", null)
        } else {
            contentHint.visibility = View.GONE
            planWebView.loadDataWithBaseURL(CoreZygote.getLoginUserServices().serverAddress, NewCollaborationActivity.IMAGE_STYLE + content, "text/html; charset=utf-8", "UTF-8", null
            )
        }
    }

    override fun displayWaitSendData(planContent: PlanContent) {
        etTitle.setText(planContent.title)
        if (planWebView.visibility == View.VISIBLE) {
            showRichContent(planContent.content)
            RichTextContentKeeper.getInstance().richTextContent = tryAddHostToImageBeforeEdit(planContent.content)
        } else {
            etContent.setText(Html.fromHtml(planContent.content, UrlImageParser(etContent, CoreZygote.getLoginUserServices().serverAddress), null))
        }
        setTypeValue(planContent.type!!.toInt())
    }

    private fun tryAddHostToImageBeforeEdit(content: String?): String? {
        if (content.isNullOrEmpty()) {
            return content
        }
        var result: String? = content
        val host = CoreZygote.getLoginUserServices().serverAddress
        val regex = "<img.*?src=\"(.*?)\".*?(/>|></img>|>)"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(content)
        while (matcher.find()) {
            val group = matcher.group(1)
            if (group.startsWith("http") || group.startsWith("https")) {
                continue
            }
            result = result?.replace(group, host + group)
        }
        return result
    }

    override fun getViewValue(planContent: PlanContent) {
        planContent.title = etTitle.getText().toString().trim()
        planContent.content = getEdContent()
        planContent.attachments = attachmentFragment?.attachments
    }

    private fun getEdContent() = if (etContent.visibility == View.VISIBLE) etContent.editableText.toString().trim() else mPresenter
            .tryTransformImagePath()

    override fun showLoading() {
        LoadingHint.show(this)
    }

    override fun showProgress(progress: Int) {
        LoadingHint.showProgress(progress)
    }

    override fun hideLoading() {
        LoadingHint.hide()
    }

    override fun saveSuccess() {
        FEToast.showMessage(getString(R.string.plan_rule_submit_hint))
        finish()
        EventBus.getDefault().post(EventPlanListRefresh())
    }

    override fun saveFail(errorMsg: String) {
        FEToast.showMessage(errorMsg)
    }

    override fun showTempData(planContent: PlanContent) {
        etTitle.setText(planContent.title)
        val index = planContent.title?.length ?: 0
        etTitle.setSelection(if (index > numTitleMax) numTitleMax else index)
        if (!TextUtils.isEmpty(planContent.content)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                etContent.visibility = View.GONE
                contentHint.visibility = View.GONE
                planWebView.setVisibility(View.VISIBLE)
                planWebView.loadDataWithBaseURL(CoreZygote.getLoginUserServices().serverAddress, IMAGE_STYLE + planContent.content, "text/html; charset=utf-8", "UTF-8", null)
                RichTextContentKeeper.getInstance().richTextContent = tryAddHostToImageBeforeEdit(planContent.content)
            } else {
                planWebView.setVisibility(View.GONE)
                etContent.visibility = View.VISIBLE
                etContent.setText(Html.fromHtml(planContent.content, UrlImageParser(etContent, CoreZygote.getLoginUserServices().serverAddress), null))
            }
        }
    }

    override fun getActivity() = this

    override fun onDestroy() {
        DataKeeper.getInstance().removeKeepData(PlanCreateContract.REQUSETCODE_RECEIVER)
        DataKeeper.getInstance().removeKeepData(PlanCreateContract.REQUESTCODE_CCUSER)
        DataKeeper.getInstance().removeKeepData(PlanCreateContract.REQUESTCODE_NOTIFIER)
        RichTextContentKeeper.getInstance().removeCache()
        RichTextContentKeeper.getInstance().removeCompressImagePath()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isHasWrote()) {
                showExitDialog()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun isHasWrote() = !TextUtils.isEmpty(etTitle.getText())
            || !TextUtils.isEmpty(getEdContent())
            || !CommonUtil.isEmptyList(attachmentFragment?.attachments)
            || !CommonUtil.isEmptyList(mPresenter.getPlanContent().receiver)
            || !CommonUtil.isEmptyList(mPresenter.getPlanContent().cc)
            || !CommonUtil.isEmptyList(mPresenter.getPlanContent().notifier)
}
