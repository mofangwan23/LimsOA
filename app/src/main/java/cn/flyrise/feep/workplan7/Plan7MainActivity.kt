package cn.flyrise.feep.workplan7

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.view.View
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.PreferencesUtils
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.workplan7.fragment.PlanMainCreateFragment
import cn.flyrise.feep.workplan7.fragment.PlanStatisticsListFragment
import cn.flyrise.feep.workplan7.fragment.PlanTabListFragment
import kotlinx.android.synthetic.main.plan7_activity_main.*

class Plan7MainActivity : BaseActivity() {

    private var nowType: Int? = null
    private var mainFragment: PlanTabListFragment? = null
    private var newFragment: PlanMainCreateFragment? = null
    private var statisticsFragment: PlanStatisticsListFragment? = null

    var toolbar: FEToolbar? = null
    private var isShowNewRule = false

    companion object {

        val MAIN = 1
        val NEW = 2
        val STATISTIC = 3

        fun start(context: Context, default: Int) {
            start(context, default, null)
        }

        fun start(context: Context, default: Int, userId: ArrayList<String>?) {
            val intent = Intent(context, Plan7MainActivity::class.java)
            intent.putExtra("dafault_item", default)
            intent.putStringArrayListExtra("userIds", userId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan7_activity_main)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        toolbar.setRightTextColor(Color.parseColor("#28B9FF"))
        this.toolbar = toolbar
    }

    override fun bindData() {
        super.bindData()
        lyStatistics.visibility = if (SpUtil.get(PreferencesUtils.HAS_SUBORDINATES, false)) View.VISIBLE else View.GONE
    }

    override fun bindListener() {
        super.bindListener()
        disPlayFragment(intent.getIntExtra("dafault_item", MAIN))
        lyMain.setOnClickListener { disPlayFragment(MAIN) }
        lyNew.setOnClickListener { disPlayFragment(NEW) }
        lyStatistics.setOnClickListener { disPlayFragment(STATISTIC) }
    }

    private fun disPlayFragment(type: Int) {
        if (nowType == type) return else nowType = type
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        hideFragment(fragmentTransaction)
        when (type) {
            MAIN -> {
                if (mainFragment == null) {
                    mainFragment = PlanTabListFragment()
                    fragmentTransaction.add(R.id.flCenter, mainFragment!!)
                }
                fragmentTransaction.show(mainFragment!!)
                ivMain.setImageResource(R.drawable.plan_list_click)
                ivNew.setImageResource(R.drawable.plan_create)
                ivStatistics.setImageResource(R.drawable.plan_statistics)
                tvMain.setTextColor(Color.parseColor("#28B9FF"))
                tvNew.setTextColor(Color.parseColor("#7F7F7F"))
                tvStatistics.setTextColor(Color.parseColor("#7F7F7F"))
                toolbar!!.title = getString(R.string.plan_tab_list_title)
                toolbar!!.setRightTextVisbility(View.GONE)
            }
            NEW -> {
                if (newFragment == null) {
                    newFragment = PlanMainCreateFragment()
                    newFragment?.setUserIds(intent?.getStringArrayListExtra("userIds"))
                    fragmentTransaction.add(R.id.flCenter, newFragment!!)
                }
                fragmentTransaction.show(newFragment!!)
                ivMain.setImageResource(R.drawable.plan_list)
                ivNew.setImageResource(R.drawable.plan_create_click)
                ivStatistics.setImageResource(R.drawable.plan_statistics)
                tvMain.setTextColor(Color.parseColor("#7F7F7F"))
                tvNew.setTextColor(Color.parseColor("#28B9FF"))
                tvStatistics.setTextColor(Color.parseColor("#7F7F7F"))
                toolbar!!.title = getString(R.string.plan_main_create_title)
                toolbar!!.setRightTextVisbility(View.GONE)
            }
            STATISTIC -> {
                if (statisticsFragment == null) {
                    statisticsFragment = PlanStatisticsListFragment.getInstance {
                        if (nowType != STATISTIC) return@getInstance
                        isShowNewRule = it != 0
                        toolbar!!.rightText = if (isShowNewRule) getString(R.string.plan_rule_create) else ""
                        toolbar!!.setRightTextVisbility(if (isShowNewRule) View.VISIBLE else View.GONE)
                        toolbar!!.setRightTextClickListener { startActivity(Intent(this, PlanRuleCreateActivity::class.java)) }
                    }
                    fragmentTransaction.add(R.id.flCenter, statisticsFragment!!)
                }
                fragmentTransaction.show(statisticsFragment!!)
                ivMain.setImageResource(R.drawable.plan_list)
                ivNew.setImageResource(R.drawable.plan_create)
                ivStatistics.setImageResource(R.drawable.plan_statistics_click)
                tvMain.setTextColor(Color.parseColor("#7F7F7F"))
                tvNew.setTextColor(Color.parseColor("#7F7F7F"))
                tvStatistics.setTextColor(Color.parseColor("#28B9FF"))
                toolbar!!.title = getString(R.string.plan_statistics_title)
                toolbar!!.setRightTextVisbility(if (isShowNewRule) View.VISIBLE else View.GONE)
            }
        }
        fragmentTransaction.commit()
    }

    private fun hideFragment(fragmentTransaction: FragmentTransaction) {
        if (mainFragment != null) fragmentTransaction.hide(mainFragment!!)
        if (newFragment != null) fragmentTransaction.hide(newFragment!!)
        if (statisticsFragment != null) fragmentTransaction.hide(statisticsFragment!!)
    }

}
