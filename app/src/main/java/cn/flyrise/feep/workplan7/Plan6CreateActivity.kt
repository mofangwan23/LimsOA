package cn.flyrise.feep.workplan7

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.R
import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity
import cn.flyrise.feep.collaboration.activity.RichTextEditActivity
import cn.flyrise.feep.collaboration.utility.RichTextContentKeeper
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseEditableActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.workplan7.adapter.PlanAttachmentAdapter
import cn.flyrise.feep.workplan7.adapter.PlanUserLayoutAdapter
import cn.flyrise.feep.workplan7.contract.Plan6CreateContract
import cn.flyrise.feep.workplan7.contract.PlanCreateContract
import cn.flyrise.feep.workplan7.fragment.PlanAttachmentFragment
import cn.flyrise.feep.workplan7.model.PlanContent
import cn.flyrise.feep.workplan7.presenter.Plan6CreatePresenter
import cn.flyrise.feep.workplan7.view.PlanItemLayout
import com.borax12.materialdaterangepicker.DateTimePickerDialog
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.plan6_activity_create.*
import java.util.*
import java.util.concurrent.TimeUnit

class Plan6CreateActivity : BaseEditableActivity(), Plan6CreateContract.IView {

    lateinit var mPresenter: Plan6CreateContract.Presenter
    private var startCalendar: Calendar? = null
    private var endCalendar: Calendar? = null

    private var attachmentFragment: PlanAttachmentFragment? = null

    companion object {
        fun startActivity(context: Context, type: Int) {
            val intent = Intent(context, Plan7CreateActivity::class.java)
            intent.putExtra("workPlanType", type)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan6_activity_create)
        mPresenter = Plan6CreatePresenter(this, this)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        toolbar.title = getString(R.string.plan_create_work)
        toolbar.setNavigationOnClickListener { showExitDialog() }
    }

    override fun bindView() {
        super.bindView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            etContent.visibility = View.GONE
            planWebView.visibility = View.VISIBLE
            contentHint.visibility = View.VISIBLE
        }
        receiverListView.isNestedScrollingEnabled = false
        receiverListView.layoutManager = GridLayoutManager(this, 6)
        ccListView.isNestedScrollingEnabled = false
        ccListView.layoutManager = GridLayoutManager(this, 6)
        notificationListView.isNestedScrollingEnabled = false
        notificationListView.layoutManager = GridLayoutManager(this, 6)
        planWebView.loadDataWithBaseURL("", "", "text/html; charset=utf-8", "UTF-8", null)
    }

    override fun bindListener() {
        super.bindListener()
        lyStatTime.setOnClickListener { openDateTimeDialog(lyStatTime, startCalendar) }
        lyEndTime.setOnClickListener { openDateTimeDialog(lyEndTime, endCalendar) }
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
        lyAttachmentTitle.setOnClickListener { mPresenter.clickAttachment(this, attachmentFragment?.attachments) }
    }

    /****************************************时间************************************/
    private fun openDateTimeDialog(layout: PlanItemLayout, calendar: Calendar?) {
        DevicesUtil.tryCloseKeyboard(this)
        val dateTimePickerDialog = DateTimePickerDialog()
        dateTimePickerDialog.setDateTime(calendar)
        if (layout == lyStatTime && endCalendar != null) {
            dateTimePickerDialog.setMaxCalendar(endCalendar)
        } else if (startCalendar != null) {
            dateTimePickerDialog.setMinCalendar(startCalendar)
        }
        dateTimePickerDialog.setButtonCallBack(object : DateTimePickerDialog.ButtonCallBack {
            override fun onClearClick() {}

            override fun onOkClick(result: Calendar, dialog: DateTimePickerDialog) {
                if (layout == lyStatTime) {
                    startCalendar = result
                } else {
                    endCalendar = result
                }
                layout.setContent(DateUtil.formatTime(result.timeInMillis, "yyyy年MM月dd日"))
                dateTimePickerDialog.dismiss()
            }
        })
        dateTimePickerDialog.setTimeLevel(3)
        dateTimePickerDialog.show(fragmentManager, "dateTimePickerDialog")
    }

    /****************************************时间End************************************/


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            mPresenter.handleUserResult(requestCode) -> return
            mPresenter.handleAttachmentResult(requestCode, resultCode, data) -> return
            requestCode == PlanCreateContract.REQUSETCODE_CONTENT -> showRichContent(tryTransformImagePath())
        }
    }


    override fun showReceiverUser(users: List<AddressBook>?) {
        val title = "${getString(R.string.plan_create_recevier_user)}${if (CommonUtil.isEmptyList(users)) "" else "（${users!!.size}）"}"
        lyReceiverTitle.setTitle(title)
        receiverListView.adapter = PlanUserLayoutAdapter(this, users)
    }

    override fun showCCUser(users: List<AddressBook>?) {
        val title = "${getString(R.string.plan_create_cc_user)}${if (CommonUtil.isEmptyList(users)) "" else "（${users!!.size}）"}"
        lyCCTitle.setTitle(title)
        ccListView.adapter = PlanUserLayoutAdapter(this, users)
    }

    override fun showNotifierUser(users: List<AddressBook>?) {
        val title = "${getString(R.string.plan_create_notifier)}${if (CommonUtil.isEmptyList(users)) "" else "（${users!!.size}）"}"
        lyNotificationTitle.setTitle(title)
        notificationListView.adapter = PlanUserLayoutAdapter(this, users)
    }

    override fun showAttachment(attachments: ArrayList<Attachment>?) {
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
        val title = "${getString(R.string.plan_create_attachment)}${if (CommonUtil.isEmptyList(attachments)) "" else "（${attachments!!.size}）"}"
        lyAttachmentTitle.setTitle(title)
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
            planWebView.loadDataWithBaseURL("", "", "text/html; charset=utf-8", "UTF-8",
                    null)
        } else {
            contentHint.visibility = View.GONE
            planWebView.loadDataWithBaseURL(
                    CoreZygote.getLoginUserServices().serverAddress,
                    NewCollaborationActivity.IMAGE_STYLE + content,
                    "text/html; charset=utf-8",
                    "UTF-8",
                    null
            )
        }
    }

    private fun tryTransformImagePath(): String? {
        if (!RichTextContentKeeper.getInstance().hasContent()) {
            return null
        }
        val compressImagePaths = RichTextContentKeeper.getInstance().compressImagePaths
        var richText = RichTextContentKeeper.getInstance().richTextContent
        if (CommonUtil.isEmptyList(compressImagePaths)) {
            return richText
        }
        for (path in compressImagePaths) {
            val url =
                    ("/AttachmentServlet39?attachPK=" + RichTextContentKeeper.getInstance().getGUIDByLocalPath(path) + "&actionType=download")
            richText = richText.replace(path, url)
        }
        return richText
    }

    /****************************************富文本End************************************/

    override fun getViewValue(planContent: PlanContent) {
        planContent.startTime = startCalendar
        planContent.endTime = endCalendar
        planContent.title = etTitle.editableText.toString()
        if (etContent.visibility == View.VISIBLE) {
            planContent.content = etContent.editableText.toString()
        } else {
            planContent.content = tryTransformImagePath()
        }
        planContent.attachments = attachmentFragment?.attachments
//        planContent.type = 4
        planContent.userId = CoreZygote.getLoginUserServices().userId
    }

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
        FEToast.showMessage(getString(R.string.plan_save_success))
        finish()
    }

    override fun saveFail(errorMsg: String) {
        FEToast.showMessage(errorMsg)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun getActivity() = this

    override fun onDestroy() {
        RichTextContentKeeper.getInstance().removeCache()
        RichTextContentKeeper.getInstance().removeCompressImagePath()
        super.onDestroy()
    }
}