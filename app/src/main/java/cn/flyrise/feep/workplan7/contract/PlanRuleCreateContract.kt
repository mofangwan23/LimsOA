package cn.flyrise.feep.workplan7.contract

import android.app.Activity
import android.content.Intent
import cn.flyrise.feep.core.services.model.AddressBook

interface PlanRuleCreateContract {

    interface IView {
        fun setFrequencyValue(frequency: Int, showDownIcon: Boolean)
        fun setTimeValue(startTimeText: String, endTimeText: String)
        fun showUserListInfo(users: List<AddressBook>?)

        fun getRuleTitle(): String

        fun isRemind(): Boolean

        fun getRemindContent(): String
    }


    interface IPresenter {
        fun setPlanType(type: Int)
        fun setRemindTime(time: Int)
        fun setStartTime(startDateText: String, startTimeText: String)
        fun setEndTime(endDateText: String, endTimeText: String)

        fun getFrequency(): Int
        fun getRemindHour(): Int//提醒时间等于结束时间减去提前提醒小时
        fun getHourOfDaySelection(isStartClick: Boolean): List<String>

        fun getRemindOfHourSelection(): List<String>

        fun getHourStartNormalSelection(): List<String>//默认正常开始的时间（0-23）
        fun getHourEndNormalSelection(): List<String>//默认正常结束的时间（1-24）

        fun clickChooseUser(activity: Activity)
        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun submitRule()

        fun deleteRule(id: String)

    }
}