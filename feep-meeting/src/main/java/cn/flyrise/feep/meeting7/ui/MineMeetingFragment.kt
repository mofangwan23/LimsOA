package cn.flyrise.feep.meeting7.ui

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.UIUtil
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.time.MINE_TYPE_ALL
import cn.flyrise.feep.meeting7.selection.time.MINE_TYPE_SPONSOR
import cn.flyrise.feep.meeting7.selection.time.MINE_TYPE_TAKE_PART
import cn.squirtlez.frouter.FRouter

/**
 * @author ZYP
 * @since 2018-06-19 09:37
 */
class MineMeetingFragment : Fragment() {

    private var untreatedCallback: ((Int) -> Unit)? = null
    private lateinit var vp: ViewPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.nms_fragment_mine_meeting, container, false)
        bindView(contentView!!)
        return contentView
    }

    fun setUntreatedCallback(untreatedCallback: (Int) -> Unit) {
        this.untreatedCallback = untreatedCallback
    }

    private fun bindView(contentView: View) {
        contentView.findViewById<ViewGroup>(R.id.nmsLayoutMeetingSearch).setOnClickListener {
            FRouter.build(activity, "/meeting/search").go()
        }

        val titles = arrayListOf(getString(R.string.meeting7_mine_all), getString(R.string.meeting7_mine_cur_user), getString(R.string.meeting7_mine_cur_user_send))
        val tabLayout = contentView.findViewById<TabLayout>(R.id.nmsTabLayout)
        tabLayout.newTab().text = titles[0]
        tabLayout.newTab().text = titles[1]
        tabLayout.newTab().text = titles[2]

        val fragments = mutableListOf<Fragment>()
        fragments.add(MineMeetingPage.newInstance(MINE_TYPE_ALL, untreatedCallback))
        fragments.add(MineMeetingPage.newInstance(MINE_TYPE_TAKE_PART, untreatedCallback))
        fragments.add(MineMeetingPage.newInstance(MINE_TYPE_SPONSOR, untreatedCallback))

        vp = contentView.findViewById(R.id.nmsViewPager)
        vp.adapter = BaseFragmentPagerAdapter(childFragmentManager, fragments, titles)
        vp.offscreenPageLimit = fragments.size
        tabLayout.setupWithViewPager(vp)
        UIUtil.fixTabLayoutIndicatorWidth(tabLayout, 50)
    }

    fun swtichToMineSponsorPage() {         // 切换到我的发起界面。并刷新数据
        for (i in 0 until (vp.adapter?.count ?: 1)) {
            ((vp.adapter as FragmentPagerAdapter).getItem(i) as MineMeetingPage).refresh()
        }
        vp.currentItem = 2
    }
}

class BaseFragmentPagerAdapter(fm: FragmentManager, val fragments: List<Fragment>, val titles: List<String>) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? = if (CommonUtil.isEmptyList(fragments)) null else fragments[position]
    override fun getCount(): Int = if (CommonUtil.isEmptyList(fragments)) 0 else fragments.size
    override fun getPageTitle(position: Int) = if (CommonUtil.isEmptyList(titles)) super.getPageTitle(position) else titles!![position]
}