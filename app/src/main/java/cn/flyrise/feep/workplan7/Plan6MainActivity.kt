package cn.flyrise.feep.workplan7

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.View
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.selection.ContactSelectionActivity
import cn.flyrise.feep.addressbook.selection.TITLE
import cn.flyrise.feep.commonality.adapter.BaseFragmentPagerAdapter
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.UIUtil
import cn.flyrise.feep.workplan7.fragment.PlanListFragment
import kotlinx.android.synthetic.main.plan6_activity_main.*

class Plan6MainActivity : BaseActivity() {

    lateinit var receiveFragment: PlanListFragment
    lateinit var sendFragment: PlanListFragment
    private var toolbar: FEToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan6_activity_main)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        this.toolbar = toolbar
        toolbar.rightText = getString(R.string.plan_filter_title)
        toolbar.title = getString(R.string.plan_tab_list_title)
        toolbar.setRightTextClickListener {
            val intent = Intent(this, ContactSelectionActivity::class.java)
            intent.putExtra(TITLE, getString(R.string.plan_filter_users))
            intent.putExtra(
                    cn.flyrise.feep.addressbook.selection.DATASOURCE,
                    cn.flyrise.feep.addressbook.selection.DATASOURCE_WORK_PLAN_RELATED
            )
            intent.putExtra(cn.flyrise.feep.addressbook.selection.SELECTION_MODE, cn.flyrise.feep.addressbook.selection.SELECTION_MULTI)
            startActivityForResult(intent, 100)
        }
    }


    override fun bindData() {
        super.bindData()
        val tabTiles = listOf(getString(R.string.plan_user_received), getString(R.string.plan_user_send))
        receiveFragment = PlanListFragment.newInstance(true)
        sendFragment = PlanListFragment.newInstance(false)
        val fragments = arrayListOf(receiveFragment as Fragment, sendFragment as Fragment)
        for (title in tabTiles) {
            val tab = tabLayout.newTab()
            tab.text = title
        }
        val adapter = BaseFragmentPagerAdapter(supportFragmentManager, fragments)
        adapter.setTitles(tabTiles)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                toolbar?.setRightTextVisbility(if (position == 1) View.GONE else View.VISIBLE)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })

        btCreate.setOnClickListener {
            startActivity(Intent(this, Plan6CreateActivity::class.java))
        }
        UIUtil.fixTabLayoutIndicatorWidth(tabLayout, 40)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val userID = data?.getStringExtra(cn.flyrise.feep.addressbook.selection.CONTACT_IDS)
            Plan6FilterResultActivity.startActivity(this, userID!!)
        }
    }
}