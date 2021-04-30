package cn.flyrise.feep.location.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.selection.*
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.UIUtil
import cn.flyrise.feep.event.EventContactSelection
import cn.flyrise.feep.location.adapter.SignInFragmentAdapter
import cn.flyrise.feep.location.views.SignInMonthStatisActivity
import kotlinx.android.synthetic.main.location_leader_kanban_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-11:19.
 * 考勤统计（有下属才能查看）
 */

class SignInLeaderTabStatisFragment : Fragment() {

    private var dayStatisFragment: LeaderDayStatisFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        return inflater.inflate(R.layout.location_leader_kanban_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val fragments = ArrayList<Fragment>()
        dayStatisFragment = LeaderDayStatisFragment()
        fragments.add(dayStatisFragment!!)
        fragments.add(LeaderMonthStatisFragment())
        mViewPager!!.adapter = SignInFragmentAdapter(getChildFragmentManager(), fragments)
        mTabLayout!!.addTab(mTabLayout!!.newTab())
        mTabLayout!!.addTab(mTabLayout!!.newTab())
        mTabLayout!!.setupWithViewPager(mViewPager)
        mTabLayout!!.getTabAt(0)!!.text = resources.getString(R.string.location_leader_statis_day)
        mTabLayout!!.getTabAt(1)!!.text = resources.getString(R.string.location_leader_statis_month)
        UIUtil.fixTabLayoutIndicatorWidth(mTabLayout, 45)
    }

    fun setLeader(isLeader: Boolean) {
        dayStatisFragment?.setLeader(isLeader)
    }

    fun setToolBarListener(context: Context, mToolbar: FEToolbar) {
        mToolbar.title = context.getString(R.string.location_month_statis_title)
        mToolbar.setLineVisibility(View.GONE)
        mToolbar.setRightIcon(R.drawable.location_leader_selected_person_icon)
        mToolbar.setRightImageClickListener {
            val intent = Intent(context, ContactSelectionActivity::class.java)
            intent.putExtra(SELECTION_MODE, SELECTION_SINGLE)
            intent.putExtra(DATASOURCE, DATASOURCE_LEADER_POINT)
            intent.putExtra(SELECTION_FINISH, false)
            startActivity(intent)
        }
        mToolbar.setLineVisibility(View.VISIBLE)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventContactSelecationSuccess(selection: EventContactSelection) {
        if (TextUtils.isEmpty(selection.userid)) return
        SignInMonthStatisActivity.start(context!!, caleandarToPicker(Calendar.getInstance()), selection.userid)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @SuppressLint("SimpleDateFormat")
    private fun caleandarToPicker(calendar: Calendar): String {
        val sdf = SimpleDateFormat("yyyy-MM")
        return sdf.format(calendar.time)
    }

}

