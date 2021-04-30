package cn.flyrise.feep.workplan7

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.workplan7.fragment.PlanListFragment

/**
 * 计划6筛选后要重新打开一个界面来显示。
 */
class Plan6FilterResultActivity : BaseActivity() {

    companion object {
        fun startActivity(activity: Activity, userID: String) {
            val intent = Intent(activity, Plan6FilterResultActivity::class.java)
            intent.putExtra("userId", userID)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan6_activity_filter_result)
        val fragment = PlanListFragment.newInstance(true, intent.getStringExtra("userId"))
        supportFragmentManager.beginTransaction().add(R.id.lyContent, fragment).commit()
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        toolbar.title = getString(R.string.plan_work_title)
    }

}