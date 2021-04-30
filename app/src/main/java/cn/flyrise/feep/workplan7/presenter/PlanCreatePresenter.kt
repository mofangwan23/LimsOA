package cn.flyrise.feep.workplan7.presenter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.utils.ContactsIntent
import cn.flyrise.feep.collaboration.utility.RichTextContentKeeper
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.DataKeeper
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment
import cn.flyrise.feep.media.attachments.repository.AttachmentConverter
import cn.flyrise.feep.media.common.LuBan7
import cn.flyrise.feep.workplan7.Plan7CreateActivity
import cn.flyrise.feep.workplan7.contract.PlanCreateContract
import cn.flyrise.feep.workplan7.model.PlanContent
import cn.flyrise.feep.workplan7.provider.PlanCreateProvider
import cn.flyrise.feep.workplan7.view.PlanItemLayout
import com.borax12.materialdaterangepicker.DateTimePickerDialog
import kotlinx.android.synthetic.main.plan7_activity_create.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

/**
 * author : klc
 * Msg :
 */
class PlanCreatePresenter(view: Plan7CreateActivity, context: Context) : PlanCreateContract.Presenter {

    private var dayBtTexts = context.resources.getStringArray(R.array.plan_days)
    private var weekBtTexts = context.resources.getStringArray(R.array.plan_weeks)
    private var monthBtTexts = context.resources.getStringArray(R.array.plan_months)

    val types = context.resources.getStringArray(R.array.plan_types)
    var btTexts = listOf(dayBtTexts, weekBtTexts, monthBtTexts)
    var type: Int = -1

    private var startCalendar: Calendar? = Calendar.getInstance()
    private var endCalendar: Calendar? = Calendar.getInstance()

    private val provider = PlanCreateProvider()
    private var planContent = PlanContent()

    private val mView: Plan7CreateActivity = view
    private val mContext: Context = context
    private var isWeekStartSunday: Boolean = true//一个星期的开始默认为星期天

    fun calendarNull() {
        startCalendar = null
        endCalendar = null
    }

    fun getPlanContent() = planContent

    fun getPlanWeekStart() {
        provider.getPlanWeekStart()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isWeekStartSunday = TextUtils.equals("0", it)
                    mView.getWeekStartComplete()
                }, {
                    isWeekStartSunday = true
                    mView.getWeekStartComplete()
                })
    }

    //携带人员的新建，环信聊天中的新建
    override fun initReceiverUser(userIds: ArrayList<String>?) {
        if (CommonUtil.isEmptyList(userIds)) {
            return
        }
        val queryUserIds = CoreZygote.getAddressBookServices().queryUserIds(userIds)
        if (CommonUtil.isEmptyList(queryUserIds)) {
            return
        }
        planContent.receiver = queryUserIds
        mView.showReceiverUser(planContent.receiver)
    }

    override fun clickChooseUser(activity: Activity, requestCode: Int) {
        var persons: List<AddressBook>? = null
        when (requestCode) {
            PlanCreateContract.REQUSETCODE_RECEIVER -> persons = planContent.receiver
            PlanCreateContract.REQUESTCODE_CCUSER -> persons = planContent.cc
            PlanCreateContract.REQUESTCODE_NOTIFIER -> persons = planContent.notifier
        }
        if (CommonUtil.nonEmptyList(persons)) {
            DataKeeper.getInstance().keepDatas(requestCode, persons)
        }
        ContactsIntent(activity).targetHashCode(requestCode).requestCode(requestCode).userCompanyOnly()
                .isExceptOwn(false)
                .title(CommonUtil.getString(R.string.lbl_message_title_plan_choose)).withSelect().open()
    }

    override fun handleUserResult(requestCode: Int): Boolean {
        val data = DataKeeper.getInstance().getKeepDatas(requestCode)
        if (data == null) return false
        when (requestCode) {
            PlanCreateContract.REQUSETCODE_RECEIVER -> {
                planContent.receiver = data as List<AddressBook>
                mView.showReceiverUser(planContent.receiver)
                return true
            }
            PlanCreateContract.REQUESTCODE_CCUSER -> {
                planContent.cc = data as List<AddressBook>
                mView.showCCUser(planContent.cc)
                return true
            }
            PlanCreateContract.REQUESTCODE_NOTIFIER -> {
                planContent.notifier = data as List<AddressBook>
                mView.showNotifierUser(planContent.notifier)
                return true
            }
        }
        return false
    }

    override fun clickAttachment(activity: Activity, attachments: List<Attachment>?) {
        val localAttachment = ArrayList<String>()
        val newWorkAttachment = ArrayList<NetworkAttachment>()
        if (!CommonUtil.isEmptyList(attachments)) {
            for (attachment in attachments!!) {
                if (attachment is NetworkAttachment) newWorkAttachment.add(attachment)
                else localAttachment.add(attachment.path)
            }
        }
        LuBan7.pufferGrenades(activity, localAttachment, newWorkAttachment, PlanCreateContract.REQUSETCODE_ATTACHMENT)
    }

    override fun handleAttachmentResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == 101) {
            if (data != null) {
                val localAttachment: List<String>? = data.getStringArrayListExtra("extra_local_file")
                val newWorkAttachment: List<NetworkAttachment>? = data.getParcelableArrayListExtra("extra_network_file")
                val attachments = ArrayList<Attachment>()
                attachments.addAll(newWorkAttachment.orEmpty())
                attachments.addAll(AttachmentConverter.convertAttachments(localAttachment).orEmpty())
                mView.showAttachment(attachments)
            }
            return true
        }
        return false
    }

    private fun getCurrenInputData() {
        planContent.userId = CoreZygote.getLoginUserServices().userId
        planContent.type = this.type
        planContent.startTime = this.startCalendar
        planContent.endTime = this.endCalendar
        mView.getViewValue(planContent)
    }

    override fun createPlan() {
        getCurrenInputData()
        if (planContent.startTime == null) {
            mView.saveFail(mContext.getString(R.string.plan_create_start_time_none))
            return
        }
        if (planContent.endTime == null) {
            mView.saveFail(mContext.getString(R.string.plan_create_end_time_none))
            return
        }
        if (planContent.startTime?.timeInMillis ?: 0 > planContent.endTime?.timeInMillis ?: 0) {
            mView.saveFail(mContext.getString(R.string.plan_create_time_error))
            return
        }
        if (planContent.title.isNullOrEmpty()) {
            mView.saveFail(mContext.getString(R.string.plan_create_title_none))
            return
        }
        if (planContent.content.isNullOrEmpty()) {
            mView.saveFail(mContext.getString(R.string.plan_create_context_none))
            return
        }
        if (CommonUtil.isEmptyList(planContent.receiver)) {
            mView.saveFail(mContext.getString(R.string.plan_create_recevier_user_none))
            return
        }
        planContent.isCreate = true
        toSave()
    }

    override fun savePlan() {
        getCurrenInputData()
        mView.getViewValue(planContent)
        if (planContent.startTime == null) {
            mView.saveFail(mContext.getString(R.string.plan_create_start_time_none))
            return
        }
        if (planContent.endTime == null) {
            mView.saveFail(mContext.getString(R.string.plan_create_end_time_none))
            return
        }
        if (planContent.startTime?.timeInMillis ?: 0 > planContent.endTime?.timeInMillis ?: 0) {
            mView.saveFail(mContext.getString(R.string.plan_create_time_error))
            return
        }
        if (planContent.title.isNullOrEmpty()) {
            mView.saveFail(mContext.getString(R.string.plan_create_title_none))
            return
        }
        planContent.isCreate = false
        toSave()
    }

    private fun toSave() {
        provider.savePlan(mView.getActivity(), planContent, object : OnProgressUpdateListenerImpl() {
            override fun onPreExecute() {
                super.onPreExecute()
                mView.showLoading()
            }

            override fun onProgressUpdate(currentBytes: Long, contentLength: Long, done: Boolean) {
                val progress = (currentBytes * 100 / contentLength * 1.0f).toInt()
                mView.showProgress(progress)
            }
        }, object : ResponseCallback<ResponseContent>() {
            override fun onCompleted(t: ResponseContent?) {
                mView.hideLoading()
                if ("0" == t!!.errorCode)
                    mView.saveSuccess()
                else
                    mView.saveFail(mContext.getString(R.string.plan_rule_submit_error))
            }

            override fun onFailure(repositoryException: RepositoryException?) {
                super.onFailure(repositoryException)
                mView.hideLoading()
                mView.saveFail(mContext.getString(R.string.plan_rule_submit_error))
            }
        })
    }

    override fun getTempPlanData(planId: String) {//暂存获取详情
        mView.showLoading()
        provider.getPlanDetail(planId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            planContent = it
            type = planContent.type!!
            mView.showAttachment(if (CommonUtil.isEmptyList(planContent.originalAttachment)) null
            else planContent.originalAttachment!! as ArrayList<Attachment>)
            mView.showReceiverUser(planContent.receiver)
            mView.showCCUser(planContent.cc)
            mView.showNotifierUser(planContent.notifier)
            mView.showTempData(planContent)
            mView.lyTypeContent.visibility = View.VISIBLE
            mView.lyOtherDate.visibility = View.GONE
            mView.lyType.setContent(types[type - 1])
            initTempData(planContent)
            mView.hideLoading()
        }, { mView.hideLoading() })
    }

    @SuppressLint("SetTextI18n")
    fun timTextClick(interval: Int) {
        startCalendar = Calendar.getInstance()
        endCalendar = Calendar.getInstance()
        when (type) {
            K.plan.PLAN_TYPE_DAY -> {
                startCalendar!!.add(Calendar.DATE, interval)
                endCalendar!!.add(Calendar.DATE, interval)
                mView.tvDate.text = DateUtil.formatTime(startCalendar!!.timeInMillis, mContext.getString(R.string.plan_create_date_type))
            }
            K.plan.PLAN_TYPE_WEEK -> {
                startCalendar!!.add(Calendar.WEEK_OF_YEAR, interval)
                endCalendar!!.add(Calendar.WEEK_OF_YEAR, interval)
                startCalendar!!.set(Calendar.DAY_OF_WEEK, startCalendar!!.getActualMinimum(Calendar.DAY_OF_WEEK))
                startCalendar!!.add(Calendar.DATE, if (isWeekStartSunday) 0 else 1)
                endCalendar!!.set(Calendar.DAY_OF_WEEK, startCalendar!!.getActualMaximum(Calendar.DAY_OF_WEEK))
                endCalendar!!.add(Calendar.DATE, if (isWeekStartSunday) 0 else 1)
                val startTime: String = DateUtil.formatTime(startCalendar!!.timeInMillis, mContext.getString(R.string
                        .plan_create_week_type))
                val endTime: String = DateUtil.formatTime(endCalendar!!.timeInMillis, mContext.getString(R.string.plan_create_week_type))
                mView.tvDate.text = "$startTime-$endTime"
            }
            K.plan.PLAN_TYPE_MONTH -> {
                startCalendar!!.add(Calendar.MONTH, interval)
                endCalendar!!.add(Calendar.MONTH, interval)
                startCalendar!!.set(Calendar.DAY_OF_MONTH, startCalendar!!.getActualMinimum(Calendar.DAY_OF_MONTH))
                endCalendar!!.set(Calendar.DAY_OF_MONTH, endCalendar!!.getActualMaximum(Calendar.DAY_OF_MONTH))
                mView.tvDate.text = DateUtil.formatTime(startCalendar!!.timeInMillis, mContext.getString(R.string.plan_create_month_type))
            }
        }
    }

    fun openDateStartTimeDialog(layout: PlanItemLayout) {
        openDateTimeDialog(layout, startCalendar)
    }

    fun openDateEndTimeDialog(layout: PlanItemLayout) {
        openDateTimeDialog(layout, endCalendar)
    }

    private fun openDateTimeDialog(layout: PlanItemLayout, calendar: Calendar?) {
        DevicesUtil.tryCloseKeyboard(mView.getActivity())
        val dateTimePickerDialog = DateTimePickerDialog()
        dateTimePickerDialog.setDateTime(calendar)
        dateTimePickerDialog.setButtonCallBack(object : DateTimePickerDialog.ButtonCallBack {
            override fun onClearClick() {}

            override fun onOkClick(result: Calendar, dialog: DateTimePickerDialog) {
                if (layout == mView.lyStatTime) startCalendar = result else endCalendar = result
                layout.setContent(DateUtil.formatTime(result.timeInMillis, mContext.getString(R.string.plan_create_date_type)))
                dateTimePickerDialog.dismiss()
            }
        })
        dateTimePickerDialog.setTimeLevel(3)
        dateTimePickerDialog.show(mView.getActivity().fragmentManager, "dateTimePickerDialog")
    }

    fun tryTransformImagePath(): String? {
        if (!RichTextContentKeeper.getInstance().hasContent()) return null
        val compressImagePaths = RichTextContentKeeper.getInstance().compressImagePaths
        var richText = RichTextContentKeeper.getInstance().richTextContent
        if (CommonUtil.isEmptyList(compressImagePaths)) return richText
        for (path in compressImagePaths) {
            val url = ("/AttachmentServlet39?attachPK=" + RichTextContentKeeper.getInstance().getGUIDByLocalPath(path) + "&actionType=download")
            richText = richText.replace(path, url)
        }
        return richText
    }

    private fun initTempData(planContent: PlanContent) {//暂存更新时间，查过日期提示，并移到今天
        startCalendar = planContent.startTime
        endCalendar = planContent.endTime
        val calendar: Calendar = Calendar.getInstance()
        if (type == K.plan.PLAN_TYPE_OTHER) {
            mView.lyTypeContent.visibility = View.GONE
            mView.lyOtherDate.visibility = View.VISIBLE
            mView.lyStatTime.setContent(DateUtil.formatTime(startCalendar?.timeInMillis
                    ?: calendar.timeInMillis, mContext.getString(R.string.plan_create_date_type)))
            mView.lyEndTime.setContent(DateUtil.formatTime(endCalendar?.timeInMillis
                    ?: calendar.timeInMillis, mContext.getString(R.string.plan_create_date_type)))
            return
        }
        when (type) {
            K.plan.PLAN_TYPE_WEEK -> {//本周第一天
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK))
                calendar.add(Calendar.DATE, if (isWeekStartSunday) 0 else 1)
            }
            K.plan.PLAN_TYPE_MONTH -> {//本月第一天
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
            }
        }
        val date = differenceDay(calendar, startCalendar!!)
        when (date) {
            1L, 7L, in 28L..31L -> {//明天,下周，下月
                mView.setTimeBtColor(mView.btDateNet)
                timTextClick(1)
            }
            -1L, -7L, in -28L downTo -31L -> {//昨天,上周，上月
                mView.setTimeBtColor(mView.btDatePrev)
                timTextClick(-1)
            }
            0L -> {//今天
                mView.setTimeBtColor(mView.btDateNow)
                timTextClick(0)
            }
            else -> {//超过
                FEToast.showMessage(R.string.plan_out_of_date)
                mView.setTimeBtColor(mView.btDateNow)
                timTextClick(0)
            }
        }
        mView.typeButton?.forEachIndexed { index, textView -> textView.text = btTexts[type - 1][index] }
    }

    private fun differenceDay(startCalendar: Calendar, endCalendar: Calendar): Long {//相隔天数
        return (calendarZero(endCalendar).timeInMillis - calendarZero(startCalendar).timeInMillis) / (1000 * 24 * 60 * 60)
    }

    private fun calendarZero(calendar: Calendar): Calendar {//时分秒值为零，计算天
        calendar.set(Calendar.HOUR_OF_DAY, 0) // 时
        calendar.set(Calendar.MINUTE, 0) // 分
        calendar.set(Calendar.SECOND, 0) // 秒
        calendar.set(Calendar.MILLISECOND, 0)// 毫秒
        return calendar
    }
}