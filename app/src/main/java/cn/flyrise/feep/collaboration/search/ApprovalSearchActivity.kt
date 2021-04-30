package cn.flyrise.feep.collaboration.search

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.R
import cn.flyrise.feep.commonality.adapter.BaseFragmentPagerAdapter
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.common.utils.UIUtil
import cn.flyrise.feep.core.function.FunctionManager
import cn.squirtlez.frouter.annotations.Route

/**
 * @author ZYP
 * @since 2018-05-16 09:34
 * 审批界面
 */
@Route("/collaboration/search")
class ApprovalSearchActivity : BaseActivity() {

    lateinit var mEtSearch: EditText
    lateinit var mIvDelete: ImageView
    lateinit var mTabLayout: TabLayout
    lateinit var mViewPager: ViewPager

    val mHandler: Handler = Handler(Looper.getMainLooper())

    val mSearchTask: Runnable = Runnable {
        val keyword = mEtSearch.text.toString()
        supportFragmentManager.fragments.forEach {
            it as ApprovalSearchFragment
            it.executeSearch(keyword)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approval_search)
    }

    override fun bindView() {
        mEtSearch = findViewById(R.id.etSearch) as EditText
        mIvDelete = findViewById(R.id.ivDeleteIcon) as ImageView
        mTabLayout = findViewById(R.id.tabLayout) as TabLayout
        mViewPager = findViewById(R.id.viewPager) as ViewPager
    }

    override fun bindData() {
        val keyword = intent.getStringExtra("keyword")
        val requestType = intent.getIntExtra("request_type", -1)

        val collaborationMenuInfos = FunctionManager.getAppSubMenu(X.Func.Collaboration)
        val titles = ArrayList<String>(collaborationMenuInfos.size)
        val fragments = ArrayList<Fragment>(collaborationMenuInfos.size)

        var defaultIndex = 0
        collaborationMenuInfos.forEachIndexed { index, menuInfo ->
            if (menuInfo.menuId == requestType) defaultIndex = index
            titles.add(menuInfo.menu)
            mTabLayout.newTab().text = menuInfo.menu
            fragments.add(ApprovalSearchFragment.newInstance(menuInfo, keyword))
        }

        val pagerAdapter = BaseFragmentPagerAdapter(supportFragmentManager, fragments)
        pagerAdapter.setTitles(titles)
        mViewPager.adapter = pagerAdapter
        mViewPager.offscreenPageLimit = fragments.size
        mTabLayout.setupWithViewPager(mViewPager)
        UIUtil.fixTabLayoutIndicatorWidth(mTabLayout)

        mViewPager.currentItem = defaultIndex

        if (!TextUtils.isEmpty(keyword)) {
            mEtSearch.setText(keyword)
            mEtSearch.setSelection(keyword.length)
            mHandler.postDelayed(mSearchTask, 300)
        } else {
            mHandler.postDelayed({ DevicesUtil.showKeyboard(mEtSearch) }, 500)
        }
    }

    override fun bindListener() {
        // 取消、关闭界面
        findViewById<TextView>(R.id.tvSearchCancel)?.setOnClickListener { finish() }

        // 清除文本
        mIvDelete.setOnClickListener { mEtSearch.setText("") }

        // 文本框监听
        mEtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(s)) {
                    mHandler.removeCallbacks(mSearchTask)
                    mHandler.postDelayed(mSearchTask, 500)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                if (TextUtils.isEmpty(keyword)) {
                    mIvDelete.visibility = View.GONE
                    supportFragmentManager.fragments.forEach {
                        it as ApprovalSearchFragment
                        it.executeClear()
                    }
                } else {
                    if (mIvDelete.visibility == View.GONE) {
                        mIvDelete.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    override fun statusBarColor(): Int {
        return Color.WHITE
    }
}