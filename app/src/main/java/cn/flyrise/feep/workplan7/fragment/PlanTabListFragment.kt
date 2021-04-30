package cn.flyrise.feep.workplan7.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.commonality.adapter.BaseFragmentPagerAdapter
import cn.flyrise.feep.core.common.utils.UIUtil
import kotlinx.android.synthetic.main.plan_fragment_main_tab.*


class PlanTabListFragment : Fragment() {

    lateinit var receiveFragment: PlanListFragment
    lateinit var sendFragment: PlanListFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.plan_fragment_main_tab, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val tabTiles: List<String> = resources.getStringArray(R.array.plan_list_tabs).toList()
        receiveFragment = PlanListFragment.newInstance(true)
        sendFragment = PlanListFragment.newInstance(false)
        val fragments: ArrayList<Fragment> = arrayListOf(receiveFragment as Fragment, sendFragment as Fragment)
        tabTiles.forEach { tabLayout.newTab().text = it }
        val adapter = BaseFragmentPagerAdapter(activity!!.supportFragmentManager, fragments)
        adapter.setTitles(tabTiles)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        UIUtil.fixTabLayoutIndicatorWidth(tabLayout,40)
    }
}