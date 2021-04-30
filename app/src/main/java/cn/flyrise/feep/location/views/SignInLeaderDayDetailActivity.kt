package cn.flyrise.feep.location.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.location.Sign
import cn.flyrise.feep.location.adapter.SignInFragmentAdapter
import cn.flyrise.feep.location.fragment.LeaderDayStatisDetailFragment
import kotlinx.android.synthetic.main.location_leader_day_detail_layout.*
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-11:19.
 * 日统计详情/签到明细
 */

class SignInLeaderDayDetailActivity : BaseActivity() {

    private val fragments = ArrayList<Fragment>()
    private var mCurrentMonth: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_leader_day_detail_layout)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        toolbar.title = resources.getString(R.string.location_leader_sign_in_title)
    }

    override fun bindData() {
        super.bindData()
        mCurrentMonth = intent.getStringExtra(MONTH)
        fragments.add(LeaderDayStatisDetailFragment.getInstace(mCurrentMonth!!, Sign.state.NO_SIGN, {
            mTabLayout?.getTabAt(0)?.text = resources.getString(R.string.location_leader_sign_no) + "($it)"
        }))
        fragments.add(LeaderDayStatisDetailFragment.getInstace(mCurrentMonth!!, Sign.state.ALREADY_SIGN, {
            mTabLayout?.getTabAt(1)?.text = resources.getString(R.string.location_leader_sign_already) + "($it)"
        }))
        mViewPager!!.adapter = SignInFragmentAdapter(supportFragmentManager, fragments)
        mTabLayout!!.addTab(mTabLayout!!.newTab())
        mTabLayout!!.addTab(mTabLayout!!.newTab())
        mTabLayout!!.setupWithViewPager(mViewPager)
        mTabLayout!!.getTabAt(0)!!.text = resources.getString(R.string.location_leader_sign_no)
        mTabLayout!!.getTabAt(1)!!.text = resources.getString(R.string.location_leader_sign_already)
    }

    companion object {

        private val MONTH = "current_month"

        fun start(context: Context, month: String) {
            val intent = Intent(context, SignInLeaderDayDetailActivity::class.java)
            intent.putExtra(MONTH, month)
            context.startActivity(intent)
        }
    }
}
