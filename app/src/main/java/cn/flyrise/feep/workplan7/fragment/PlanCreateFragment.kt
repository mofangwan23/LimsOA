package cn.flyrise.feep.workplan7.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.workplan7.WorkPlanWaitSendActivity
import cn.flyrise.feep.workplan7.Plan7CreateActivity
import kotlinx.android.synthetic.main.plan_fragment_main_create.*

/**
 * author : klc
 * Msg : 计划首页（新建计划）
 */
class PlanMainCreateFragment : Fragment(), View.OnClickListener {

    private var userIds: ArrayList<String>? = null

    fun setUserIds(userIds: ArrayList<String>?) {
        this.userIds = userIds
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.plan_fragment_main_create, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lyDay.setOnClickListener(this)
        lyWeek.setOnClickListener(this)
        lyMonth.setOnClickListener(this)
        lyOther.setOnClickListener(this)
        lyWaitSend.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            lyDay -> Plan7CreateActivity.startActivity(activity!!, K.plan.PLAN_TYPE_DAY, userIds)
            lyWeek -> Plan7CreateActivity.startActivity(activity!!, K.plan.PLAN_TYPE_WEEK, userIds)
            lyMonth -> Plan7CreateActivity.startActivity(activity!!, K.plan.PLAN_TYPE_MONTH, userIds)
            lyOther -> Plan7CreateActivity.startActivity(activity!!, K.plan.PLAN_TYPE_OTHER, userIds)
            lyWaitSend -> startActivity(Intent(activity, WorkPlanWaitSendActivity::class.java))
        }
    }

}