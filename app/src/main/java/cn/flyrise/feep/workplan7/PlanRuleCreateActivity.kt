package cn.flyrise.feep.workplan7

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseEditableActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.DataKeeper
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.workplan7.adapter.PlanUserLayoutAdapter
import cn.flyrise.feep.workplan7.contract.PlanRuleCreateContract
import cn.flyrise.feep.workplan7.model.PlanStatisticsListItem
import cn.flyrise.feep.workplan7.presenter.PlanRuleCreatePresenter
import cn.flyrise.feep.workplan7.view.BottomWheelSelectionDialog
import kotlinx.android.synthetic.main.plan_activity_rule_create.*

/**
 * author : klc
 * Msg : 新建统计规则界面
 */
class PlanRuleCreateActivity : BaseEditableActivity(), PlanRuleCreateContract.IView, View.OnClickListener {

    private lateinit var typeTitles: List<String>
    private lateinit var frequencyTitles: List<String>
    private lateinit var mPresenter: PlanRuleCreatePresenter
    private var modifyItem: PlanStatisticsListItem? = null

    companion object {
        fun start(context: Context, item: PlanStatisticsListItem) {
            val intent = Intent(context, PlanRuleCreateActivity::class.java)
            intent.putExtra("data", GsonUtil.getInstance().toJson(item))
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan_activity_rule_create)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        toolbar.title = getString(R.string.plan_rule_create_title)
        toolbar.rightText = getString(R.string.plan_rule_save)
        toolbar.setRightTextColor(Color.parseColor("#28B9FF"))
        toolbar.setRightTextClickListener { mPresenter.submitRule() }
        toolbar.setNavigationOnClickListener { if (isHasWrote()) showExitDialog() else finish() }
    }

    override fun bindView() {
        super.bindView()
        userListView.isNestedScrollingEnabled = false
        userListView.layoutManager = GridLayoutManager(this, 6)
    }

    override fun bindData() {
        super.bindData()
        mPresenter = PlanRuleCreatePresenter(this, this)
        typeTitles = resources.getStringArray(R.array.plan_types).toList()
        frequencyTitles = resources.getStringArray(R.array.plan_list_frequencys).toList()
        mPresenter.setPlanType(K.plan.PLAN_TYPE_DAY)
        etRemindContent.setMaxNums(70)
        etTitle.setMaxNums(12)
        lyType.setContent(typeTitles[0])
    }

    override fun bindListener() {
        super.bindListener()
        lyType.setOnClickListener(this)
        lyFrequency.setOnClickListener(this)
        lyStartTime.setOnClickListener(this)
        lyEndTime.setOnClickListener(this)
        btRemind.setOnClickListener(this)
        lyUserTitle.setOnClickListener(this)
        lyRemindTime.setOnClickListener(this)
        delectRule.setOnClickListener {
            FEMaterialDialog.Builder(this)
                    .setMessage(getString(R.string.plan_rule_delete_hint))
                    .setNegativeButton(null, null)
                    .setPositiveButton(null, { mPresenter.deleteRule(modifyItem?.id!!) })
                    .build().show()
        }
        btRemind.isChecked = true
        initModifyData()
        showReminTimeLayout()
    }

    private fun initModifyData() {
        modifyItem = GsonUtil.getInstance().fromJson(intent?.getStringExtra("data"), PlanStatisticsListItem::class.java)
        if (modifyItem == null) return
        lyType.setContent(typeTitles[modifyItem?.planType!! - 1])
        lyFrequency.setContent(frequencyTitles[modifyItem?.fqcy!! - 1])
        lyFrequency.getRightImageView().visibility = if (modifyItem?.planType == K.plan.PLAN_TYPE_OTHER) View.VISIBLE else View.GONE
        etTitle.setText(modifyItem?.title)
        btRemind.isChecked = !TextUtils.isEmpty(modifyItem?.tips)
        etRemindContent.setText(modifyItem?.tips)
        mPresenter.initModifyData(modifyItem!!)
        delectRule.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when (v) {
            lyType -> openTypeDialog()
            lyFrequency -> openFrequencyDialog()
            btRemind -> showReminTimeLayout()
            lyUserTitle -> lyUserTitle.setOnClickListener { mPresenter.clickChooseUser(this) }
            lyRemindTime -> openReminTimeDialog()
            lyStartTime -> when (mPresenter.getFrequency()) {
                K.plan.PLAN_FREQUENCY_DAT -> openDayTimeDialog(true)
                K.plan.PLAN_FREQUENCY_WEEK -> openWeekOrMonthTimeDialog(true, true)
                K.plan.PLAN_FREQUENCY_MONTH -> openWeekOrMonthTimeDialog(false, true)
            }

            lyEndTime -> when (mPresenter.getFrequency()) {
                K.plan.PLAN_FREQUENCY_DAT -> openDayTimeDialog(false)
                K.plan.PLAN_FREQUENCY_WEEK -> openWeekOrMonthTimeDialog(true, false)
                K.plan.PLAN_FREQUENCY_MONTH -> openWeekOrMonthTimeDialog(false, false)
            }
        }
    }

    override fun isRemind() = btRemind.isChecked

    override fun getRuleTitle() = etTitle.getText()

    override fun getRemindContent() = etRemindContent.getText()

    private fun openTypeDialog() {
        val dialog = BottomWheelSelectionDialog()
        dialog.title = getString(R.string.plan_rule_selected_type)
        dialog.addValue(typeTitles, -1)
        dialog.onClickListener = {
            mPresenter.setPlanType(typeTitles.indexOf(it[0]) + 1)
            lyType.setContent(it[0])
        }
        dialog.show(supportFragmentManager, "openTypeDialog")
    }

    private fun openFrequencyDialog() {
        if (lyFrequency.getRightImageView().visibility == View.GONE) return
        val dialog = BottomWheelSelectionDialog()
        dialog.title = getString(R.string.plan_rule_selected_frequency)
        dialog.addValue(frequencyTitles, -1)
        dialog.onClickListener = { mPresenter.setFrequencyType(frequencyTitles.indexOf(it[0]) + 1) }
        dialog.show(supportFragmentManager, "openFrequencyDialog")
    }

    override fun setFrequencyValue(frequency: Int, showDownIcon: Boolean) {
        lyFrequency.setContent(frequencyTitles[frequency - 1])
        lyFrequency.getRightImageView().visibility = if (showDownIcon) View.VISIBLE else View.GONE
    }

    private fun openDayTimeDialog(isStart: Boolean) {
        val selectTimes: List<String> = mPresenter.getHourOfDaySelection(isStart)
        val dialog = BottomWheelSelectionDialog()
        dialog.title = getString(R.string.plan_rule_selected_time)
        dialog.addValue(selectTimes, -1)
        dialog.onClickListener = {
            mPresenter.setRemindTime(1)//重置提醒时间
            if (isStart) {
                mPresenter.setStartTime("", it[0])
            } else {
                mPresenter.setEndTime("", it[0])
            }
        }
        dialog.show(supportFragmentManager, "openFrequencyDialog")
    }

    private fun openWeekOrMonthTimeDialog(isWeek: Boolean, isStart: Boolean) {
        val dialog = BottomWheelSelectionDialog()
        dialog.title = getString(R.string.plan_rule_selected_time)
        val leftItem: List<String>
        val normalHour: List<String>
        val startHour: List<String>
        val endHour: List<String>
        if (isStart) {
            leftItem = if (isWeek) mPresenter.getDayStartOfWeekSelection(isStart) else mPresenter.getDayStartOfMonthSelection(isStart)
            normalHour = mPresenter.getHourStartNormalSelection()
            startHour = mPresenter.getHourStartSelectionForWeekOrMonth(true) //如果是开始时间，开始时间的结束时间需要受开始时间的影响。
            endHour = mPresenter.getHourStartSelectionForWeekOrMonth(false)
        } else {
            leftItem = if (isWeek) mPresenter.getDayEndOfWeekSelection(isStart) else mPresenter.getDayEndOfMonthSelection(isStart)
            normalHour = mPresenter.getHourEndNormalSelection()
            startHour = mPresenter.getHourEndSelectionForWeekOrMonth(true) //如果是结束时间，结束时间的开始时间需要受开始时间的影响。
            endHour = mPresenter.getHourEndSelectionForWeekOrMonth(false)
        }
        dialog.addValue(leftItem, -1)
        dialog.addValue(startHour, -1)
        var displayHourText: List<String> = startHour
        dialog.onSelectionListener = { wheelViews, scrollView ->
            mPresenter.setRemindTime(1)//重置提醒时间
            if (scrollView == wheelViews[0]) when {
                wheelViews[0].seletedIndex == 0 && displayHourText != startHour -> {
                    wheelViews[1].setItems(startHour)
                    displayHourText = startHour
                }
                wheelViews[0].seletedIndex == leftItem.size - 1 && displayHourText != endHour -> {
                    wheelViews[1].setItems(endHour)
                    displayHourText = endHour
                }
                displayHourText != normalHour -> {
                    wheelViews[1].setItems(normalHour)
                    displayHourText = normalHour
                }
            }
        }
        dialog.onClickListener = { if (isStart) mPresenter.setStartTime(it[0], it[1]) else mPresenter.setEndTime(it[0], it[1]) }
        dialog.show(supportFragmentManager, "openWeekOrMonthTimeDialog")
    }

    override fun setTimeValue(startTimeText: String, endTimeText: String) {
        lyStartTime.setContent(startTimeText)
        lyEndTime.setContent(endTimeText)
        updataRemindTimeVale(mPresenter.getRemindTime())
        updataRemindTimeHint()
        tvTimeHint.text = when (mPresenter.getFrequency()) {
            K.plan.PLAN_FREQUENCY_MONTH -> getString(R.string.plan_rule_frequency_month).format(startTimeText, endTimeText)
            K.plan.PLAN_FREQUENCY_WEEK -> getString(R.string.plan_rule_frequency_week).format(startTimeText, endTimeText)
            K.plan.PLAN_FREQUENCY_DAT -> getString(R.string.plan_rule_frequency_day).format(startTimeText, endTimeText)
            else -> ""
        }
    }

    private fun updataRemindTimeVale(remindTime: Int?) {
        if (!btRemind.isChecked) return
        lyRemindTime.setContent(getString(R.string.plan_rule_remind_front).format(remindTime))
    }

    private fun updataRemindTimeHint() {
        if (!btRemind.isChecked) return
        tvRemindContentHint.text = getString(R.string.plan_rule_remind_hour).format(mPresenter.getTimeRemindDisplayText(mPresenter.getRemindHour()))
    }

    private fun showReminTimeLayout() {
        updataRemindTimeVale(mPresenter.getRemindTime())
        updataRemindTimeHint()
        lyRemindContent.visibility = if (btRemind.isChecked) View.VISIBLE else View.GONE
    }

    private fun openReminTimeDialog() {
        val dialog = BottomWheelSelectionDialog()
        dialog.title = getString(R.string.plan_rule_remind_title)
        dialog.addValue(mPresenter.getRemindOfHourSelection(), -1)
        dialog.onClickListener = {
            mPresenter.setRemindTime(mPresenter.getRemindOfHourSelection().indexOf(it[0]) + 1)
            updataRemindTimeVale(mPresenter.getRemindTime())
            updataRemindTimeHint()
        }
        dialog.show(supportFragmentManager, "openFrequencyDialog")
    }

    override fun showUserListInfo(users: List<AddressBook>?) {
        lyUserTitle.setContent(getString(R.string.plan_rule_reciver_user) + if (CommonUtil.isEmptyList(users)) "" else "（${users!!.size}）")
        userListView.adapter = PlanUserLayoutAdapter(this, users)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPresenter.handleActivityResult(requestCode, resultCode, data)
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

    private fun isHasWrote() = !TextUtils.isEmpty(getRuleTitle())
            || !TextUtils.isEmpty(getRemindContent())
            || mPresenter.isExistUser()


    override fun onDestroy() {
        DataKeeper.getInstance().removeKeepData(mPresenter.openCodeUser)
        super.onDestroy()
    }
}