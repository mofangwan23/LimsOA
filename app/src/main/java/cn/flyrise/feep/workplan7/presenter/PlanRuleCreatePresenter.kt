package cn.flyrise.feep.workplan7.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import cn.flyrise.android.protocol.entity.workplan.PlanNewRuleRequest
import cn.flyrise.android.protocol.entity.workplan.PlanRuleDeleteRequest
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.utils.ContactsIntent
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.DataKeeper
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.event.EventPlanStatisticsListRefresh
import cn.flyrise.feep.workplan7.Plan7MainActivity
import cn.flyrise.feep.workplan7.contract.PlanRuleCreateContract
import cn.flyrise.feep.workplan7.model.PlanStatisticsListItem
import cn.flyrise.feep.workplan7.provider.PlanNewRuleProvider
import org.greenrobot.eventbus.EventBus
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class PlanRuleCreatePresenter(val mView: PlanRuleCreateContract.IView, val mContext: Context) : PlanRuleCreateContract.IPresenter {

    val openCodeUser = 102
    private var hourOfDayDisplayText = ArrayList<String>()//日
    private var dayOfWeekDisplayText = ArrayList<String>()//周
    private var dayOfMonthDisplayText = ArrayList<String>()//月
    private var remindOfHourDisplayText = ArrayList<String>()//提前多少时
    private var startHour: Int = 0
    private var startDate: Int = 0
    private var endHour: Int = 1
    private var endDate: Int = 0

    private var remindTime: Int? = 1//提前提醒时间

    private var planType: Int = K.plan.PLAN_TYPE_DAY
    private var frequency: Int = K.plan.PLAN_FREQUENCY_DAT
    private var requestId: String = "0"

    private var userList: List<AddressBook>? = null

    private val ruleProvider: PlanNewRuleProvider? = PlanNewRuleProvider()

    fun isExistUser() = !CommonUtil.isEmptyList(userList)

    init {
        val weekTitles: List<String> = mContext.resources.getStringArray(R.array.plan_rule_weeks).toList()
        for (i in 0..24) hourOfDayDisplayText.add("$i:00")
        for (i in 1..23) hourOfDayDisplayText.add(mContext.getString(R.string.plan_rule_next_day) + " ${i}:00")
        for (i in 1..23) remindOfHourDisplayText.add(mContext.getString(R.string.plan_rule_next_hour).format(i))
        for (i in 0..6) dayOfWeekDisplayText.add(weekTitles[i])
        for (i in 0..6) dayOfWeekDisplayText.add(mContext.getString(R.string.plan_rule_next) + weekTitles[i])
        for (i in 0..30) dayOfMonthDisplayText.add("${i + 1}" + mContext.getString(R.string.plan_rule_day))
        for (i in 0..30) dayOfMonthDisplayText.add(mContext.getString(R.string.plan_rule_next_month) + "${i + 1}" + mContext.getString(R.string.plan_rule_day))
    }

    override fun submitRule() {
        when {
            TextUtils.isEmpty(mView.getRuleTitle()) -> {
                FEToast.showMessage(mContext.getString(R.string.plan_rule_input_title_hint))
                return
            }
            CommonUtil.isEmptyList(userList) -> {
                FEToast.showMessage(mContext.getString(R.string.plan_rule_selected_personal_hint))
                return
            }
        }
        ruleProvider!!.submitRule(getRequest())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (!TextUtils.equals(it, "0")) return@subscribe
                    FEToast.showMessage(mContext.getString(R.string.plan_rule_submit_hint))
                    resultMainActivity()
                }
    }

    override fun deleteRule(id: String) {
        ruleProvider!!.deleteRule(PlanRuleDeleteRequest(id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    FEToast.showMessage(mContext.getString(R.string.plan_rule_delete_success))
                    resultMainActivity()
                }
    }

    private fun resultMainActivity() {
        if (CoreZygote.getApplicationServices().activityInStacks(Plan7MainActivity::class.java)) {
            val intent = Intent(mContext, Plan7MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            mContext.startActivity(intent)
        }
        (mView as Activity).finish()
        EventBus.getDefault().post(EventPlanStatisticsListRefresh())
    }

    private fun getRequest(): PlanNewRuleRequest {
        val request = PlanNewRuleRequest()
        request.id = requestId
        request.planType = planType.toString()
        request.title = mView.getRuleTitle()
        request.fqcy = frequency.toString()
        request.startTime = textToEndHour(startHour % 24)
        request.endTime = textToEndHour(endHour % 24)
        request.startDate = startDate.toString()
        request.endDate = if (frequency == K.plan.PLAN_FREQUENCY_DAT && endHour > 24) "1" else endDate.toString()
        val users = StringBuilder()
        userList?.map { it.userId }?.forEachIndexed { index, it ->
            users.append(it).append(if (index != userList!!.size - 1) "," else "")
        }
        request.users = users.toString()
        if (mView.isRemind()) {
            request.awoke = remindTime.toString()
            request.tips = if (TextUtils.isEmpty(mView.getRemindContent())) mContext.getString(R.string.plan_rule_remind_hint) else mView.getRemindContent()
        }
        return request
    }

    private fun textToEndHour(hour: Int): String? = when {
        hour in 0..9 -> "0$hour:00"
        hour >= 10 -> "$hour:00"
        else -> "0"
    }


    override fun setPlanType(type: Int) {
        this.planType = type
        startHour = 18
        endHour = 9
        when (planType) {
            K.plan.PLAN_TYPE_DAY -> {
                endHour = 24 + 9
                setTypeFrequency(K.plan.PLAN_FREQUENCY_DAT)
            }
            K.plan.PLAN_TYPE_WEEK -> {
                setTypeFrequency(K.plan.PLAN_FREQUENCY_WEEK)
                startDate = 5
                endDate = 7 + 1
            }
            K.plan.PLAN_TYPE_MONTH -> {
                setTypeFrequency(K.plan.PLAN_FREQUENCY_MONTH)
                startDate = 29
                endDate = 31 + 5
            }
            K.plan.PLAN_TYPE_OTHER -> {
                setTypeFrequency(K.plan.PLAN_FREQUENCY_DAT)
                setFrequencyType(frequency)
            }
        }
        mView.setTimeValue(getTimeStartDisplayText(startHour), getTimeEndDisplayText(endHour))
    }

    private fun setTypeFrequency(frequency: Int) {
        this.frequency = if (frequency > 3) 1 else frequency
        mView.setFrequencyValue(frequency, planType == K.plan.PLAN_TYPE_OTHER)
    }

    override fun getRemindHour(): Int = when {
        remindTime!! < endHour -> endHour - remindTime!!
        remindTime!! > endHour -> -(24 - (remindTime!! - endHour))
        else -> 0
    }

    fun getRemindTime() = remindTime

    fun setFrequencyType(frequency: Int) {
        this.frequency = frequency
        startHour = 18
        endHour = 9
        when (frequency) {
            K.plan.PLAN_FREQUENCY_DAT -> {
                endHour = 24 + 9
                setFrequency(K.plan.PLAN_FREQUENCY_DAT)
            }
            K.plan.PLAN_FREQUENCY_WEEK -> {
                setFrequency(K.plan.PLAN_FREQUENCY_WEEK)
                startDate = 5
                endDate = 7 + 1
            }
            K.plan.PLAN_FREQUENCY_MONTH -> {
                setFrequency(K.plan.PLAN_FREQUENCY_MONTH)
                startDate = 29
                endDate = 31 + 5
            }
        }
        mView.setTimeValue(getTimeStartDisplayText(startHour), getTimeEndDisplayText(endHour))
    }

    private fun setFrequency(frequency: Int) {
        mView.setFrequencyValue(frequency, planType == K.plan.PLAN_TYPE_OTHER)
    }

    override fun getFrequency() = this.frequency

    override fun setRemindTime(time: Int) {
        this.remindTime = time
    }

    override fun setStartTime(startDateText: String, startTimeText: String) {
        when (frequency) {
            K.plan.PLAN_FREQUENCY_DAT -> {
                this.startHour = hourOfDayDisplayText.indexOf(startTimeText) % 24
                if (endHour <= startHour) endHour = startHour + 1//结束时间和开始时间最少间隔1小时
            }
            K.plan.PLAN_FREQUENCY_WEEK -> {
                this.startHour = getHour(startTimeText)
                this.startDate = dayOfWeekDisplayText.indexOf(startDateText) + 1
                checkModifyEndTime()
            }
            K.plan.PLAN_FREQUENCY_MONTH -> {
                this.startHour = getHour(startTimeText)
                this.startDate = dayOfMonthDisplayText.indexOf(startDateText) + 1
                checkModifyEndTime()
            }
        }
        mView.setTimeValue(getTimeStartDisplayText(startHour), getTimeEndDisplayText(endHour))
    }

    private fun checkModifyEndTime() {
        if (endDate <= startDate) {
            endDate = startDate
            if (endHour <= startHour) endHour = startHour + 1
        }
    }

    override fun setEndTime(endDateText: String, endTimeText: String) {
        when (frequency) {
            K.plan.PLAN_FREQUENCY_DAT -> {
                val hourIndex = hourOfDayDisplayText.indexOf(endTimeText)
                this.endHour = hourIndex
            }
            K.plan.PLAN_FREQUENCY_WEEK -> {
                this.endHour = getHour(endTimeText)
                val dayOfWeek: Int = dayOfWeekDisplayText.indexOf(endDateText)
                this.endDate = dayOfWeek + 1
            }
            K.plan.PLAN_FREQUENCY_MONTH -> {
                this.endHour = getHour(endTimeText)
                val dayOfMonth = dayOfMonthDisplayText.indexOf(endDateText)
                this.endDate = dayOfMonth + 1
            }
        }
        mView.setTimeValue(getTimeStartDisplayText(startHour), getTimeEndDisplayText(endHour))
    }

    private fun getTimeStartDisplayText(hour: Int) = when (frequency) {
        K.plan.PLAN_FREQUENCY_DAT -> "$hour:00"
        K.plan.PLAN_FREQUENCY_WEEK -> "${dayOfWeekDisplayText[startDate - 1]} $hour:00"
        K.plan.PLAN_FREQUENCY_MONTH -> "${dayOfMonthDisplayText[startDate - 1]} $hour:00"
        else -> ""
    }

    private fun getTimeEndDisplayText(hour: Int) = when (frequency) {
        K.plan.PLAN_FREQUENCY_DAT -> if (hour > 24) mContext.getString(R.string.plan_rule_next_day) + " ${hour % 24}:00" else "$hour:00"
        K.plan.PLAN_FREQUENCY_WEEK -> "${dayOfWeekDisplayText[endDate - 1]} $hour:00"
        K.plan.PLAN_FREQUENCY_MONTH -> "${dayOfMonthDisplayText[endDate - 1]} $hour:00"
        else -> ""
    }

    fun getTimeRemindDisplayText(hour: Int) = when (frequency) {
        K.plan.PLAN_FREQUENCY_DAT -> if (hour > 24) mContext.getString(R.string.plan_rule_next_day) + " ${hour % 24}:00" else
            mContext.getString(R.string.plan_rule_every_day) + "$hour:00"
        K.plan.PLAN_FREQUENCY_WEEK ->
            "${dayOfWeekDisplayText[if (hour < 0) endDate - 2 else endDate - 1]} ${Math.abs(hour)}:00"
        K.plan.PLAN_FREQUENCY_MONTH ->
            "${dayOfMonthDisplayText[if (hour < 0) endDate - 2 else endDate - 1]} ${Math.abs(hour)}:00"
        else -> ""
    }

    private fun getHour(time: String): Int = time.split(":")[0].toInt()

    //日报时间
    override fun getHourOfDaySelection(isStartClick: Boolean) = if (isStartClick) {
        val startIndex = endHour - 24
        hourOfDayDisplayText.subList(if (startIndex <= 0) 0 else startIndex, 24)
    } else {
        val startIndex: Int = startHour + 1
        val endIndex: Int = startIndex + 24
        hourOfDayDisplayText.subList(startIndex, endIndex)
    }

    //周 日期 如果结束时间是24点，则对应的天数要往前后一天。
    fun getDayStartOfWeekSelection(isStartClick: Boolean) = if (isStartClick) {
        val index: Int = if (endHour == 24) endDate - 7 else endDate - 8
        dayOfWeekDisplayText.subList(if (index <= 0) 0 else index, 7)
    } else dayOfWeekDisplayText.subList(0, 7)


    //周 日期 如果开始时间是23点，则对应的天数要往前一天。
    fun getDayEndOfWeekSelection(isStartClick: Boolean) = if (isStartClick) {
        dayOfWeekDisplayText.subList(0, 7)
    } else {
        val startIndex: Int = startDate - 1
        var endIndex: Int = startIndex + 8
        if (startHour == 0) endIndex -= 1
        dayOfWeekDisplayText.subList(startIndex, endIndex)
    }

    //月 开始日期
    fun getDayStartOfMonthSelection(isStartClick: Boolean) = if (isStartClick) {
        val index: Int = if (endHour == 24) endDate - 30 else endDate - 31
        dayOfMonthDisplayText.subList(if (index <= 0) 0 else index, 31)
    } else dayOfMonthDisplayText.subList(0, 31)


    //月 结束日期
    fun getDayEndOfMonthSelection(isStartClick: Boolean) = if (isStartClick) {
        dayOfMonthDisplayText.subList(0, 31)
    } else {
        val startIndex: Int = startDate - 1
        var endIndex: Int = startIndex + 32
        if (startHour == 0) endIndex -= 1
        dayOfMonthDisplayText.subList(startIndex, endIndex)
    }

    //周、月开始时间 //当开始时间为23点的时候，则意味着要选择第二天的时间。
    fun getHourStartSelectionForWeekOrMonth(isStartClick: Boolean) = hourOfDayDisplayText.subList(
            when {
                !isStartClick -> 0
                frequency == K.plan.PLAN_FREQUENCY_WEEK -> if (endHour == 24 || endHour <= 7) 0 else endHour
                frequency == K.plan.PLAN_FREQUENCY_MONTH -> if (endHour == 24 || endHour <= 31) 0 else endHour
                else -> 0
            }, 24)

    //周、月结束时间
    fun getHourEndSelectionForWeekOrMonth(isStartClick: Boolean) = if (isStartClick) {
        hourOfDayDisplayText.subList(startHour + 1, 24 + 1)
    } else {
        hourOfDayDisplayText.subList(1, if (startHour == 0) 24 + 1 else startHour + 1)
    }

    override fun getHourStartNormalSelection(): List<String> = hourOfDayDisplayText.subList(0, 24)

    override fun getHourEndNormalSelection(): List<String> = hourOfDayDisplayText.subList(1, 25)

    override fun getRemindOfHourSelection() = when {
        Math.abs(endDate - startDate) > 1 -> remindOfHourDisplayText
        else -> remindOfHourDisplayText.subList(0, endHour - startHour)
    }

    override fun clickChooseUser(activity: Activity) {
        if (CommonUtil.nonEmptyList(userList)) {
            DataKeeper.getInstance().keepDatas(openCodeUser, userList)
        }
        ContactsIntent(activity).targetHashCode(openCodeUser).requestCode(openCodeUser).userCompanyOnly()
                .title(CommonUtil.getString(R.string.lbl_message_title_plan_choose)).withSelect().open()
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == openCodeUser && DataKeeper.getInstance().getKeepDatas(requestCode) != null) {
            userList = DataKeeper.getInstance().getKeepDatas(requestCode) as List<AddressBook>
            mView.showUserListInfo(userList)
        }
    }

    fun initModifyData(item: PlanStatisticsListItem) {
        requestId = item.id!!
        startHour = textToStartHour(item.startTime!!)
        startDate = if (TextUtils.isEmpty(item.startDate)) 0 else item.startDate?.toInt() ?: 0
        endHour = textToEndHour(item.endTime!!, item.endDate!!, item.fqcy!!)
        endDate = textToEndDate( item.endDate, item.fqcy!!);
        remindTime = if (TextUtils.isEmpty(item.awoke)) 1 else item.awoke?.toInt() ?: 1
        planType = item.planType!!
        frequency = item.fqcy!!
        userList = CoreZygote.getAddressBookServices().queryUserIds(item.users!!.split(","))

        mView.setTimeValue(getTimeStartDisplayText(startHour), getTimeEndDisplayText(endHour))
        if (!CommonUtil.isEmptyList(userList)) mView.showUserListInfo(userList)

//        FELog.i("-->>>>requestId:$requestId startHour:$startHour startDate:$startDate endHour:$endHour " +
//                "endDate:$endDate remindTime:$remindTime planType:$planType frequency:$frequency ")
    }

    private fun textToStartHour(startHour: String) = if (!TextUtils.isEmpty(startHour) && startHour.contains(":"))
        startHour.split(":")[0].toInt() else 0

    private fun textToEndHour(endHour: String, endDate: String, fqcy: Int) = when {
        fqcy == K.plan.PLAN_FREQUENCY_DAT && TextUtils.equals("1", endDate) -> {
            if (endHour.contains(":")) endHour.split(":")[0].toInt() + 24 else 1
        }
        else -> {
            if (endHour.contains(":")) endHour.split(":")[0].toInt() else 1
        }
    }

    private fun textToEndDate(endDate: String?, fqcy: Int) = when {
        fqcy == K.plan.PLAN_FREQUENCY_DAT && TextUtils.equals("1", endDate) -> 0
        else -> {
            if (TextUtils.isEmpty(endDate)) 0 else endDate?.toInt() ?: 0
        }
    }
}