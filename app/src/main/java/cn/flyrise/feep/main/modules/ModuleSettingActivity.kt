package cn.flyrise.feep.main.modules

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.widget.LinearLayout
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.R
import cn.flyrise.feep.commonality.adapter.BaseFragmentPagerAdapter
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.core.function.AppMenu
import cn.flyrise.feep.core.function.Category
import cn.flyrise.feep.core.function.FunctionManager
import kotlinx.android.synthetic.main.activity_module_setting_v7.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author 社会主义接班人
 * @since 2018-07-26 11:55
 * 应用设置界面
 */
class ModuleSettingActivity : BaseActivity() {

    private var toolbar: FEToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module_setting_v7)
    }

    override fun onStart() {
        super.onStart()
        tabLayout?.post {
            try {
                val tabLayoutClass = tabLayout.javaClass
//                val tabStrip = tabLayoutClass.getDeclaredField("mTabStrip")
                val tabStrip = tabLayoutClass.getDeclaredField("slidingTabIndicator")
                tabStrip?.isAccessible = true
                val llTab = tabStrip.get(tabLayout) as LinearLayout
                val margin = PixelUtil.dipToPx(10.0f)
                for (i in 0 until llTab.childCount) {
                    val child = llTab.getChildAt(i)
                    val params = child.layoutParams as LinearLayout.LayoutParams
                    params.leftMargin = margin
                    params.rightMargin = margin
                    child.layoutParams = params
                    child.invalidate()
                }
            } catch (e: IllegalAccessException) {
            }catch (e:NoSuchFieldException ){

            }
        }
    }

    override fun toolBar(toolbar: FEToolbar?) {
        this.toolbar = toolbar
        this.toolbar?.title = getString(R.string.module_setting_modify_title)
        this.toolbar?.setLeftText(getString(R.string.module_setting_modify_cancle))
        this.toolbar?.leftTextView?.setTextColor(Color.parseColor("#FF28B9FF"))
        this.toolbar?.setLeftTextClickListener { finish() }
        this.toolbar?.rightText = getString(R.string.module_setting_modify_success)
        this.toolbar?.rightTextView?.setTextColor(Color.parseColor("#FF28B9FF"))
        this.toolbar?.setRightTextClickListener {

            val adapter = viewPager.adapter as BaseFragmentPagerAdapter
            val editResults = mutableMapOf<Category, List<AppMenu>?>().apply {
                adapter.fragments?.forEach {
                    val f = it as ModuleSettingPage
                    put(f.getCategory(), f.getDisplayMenus())
                }
            }

            LoadingHint.show(ModuleSettingActivityV7@ this)
            FunctionManager.saveDisplayOptions(editResults)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        LoadingHint.hide()
                        if (it == 200) {
                            FEToast.showMessage(getString(R.string.message_operation_alert))
                            val result = Intent()
                            result.putExtra("isMove", true)
                            setResult(Activity.RESULT_OK, result)
                            finish()
                            return@subscribe
                        }
                        FEToast.showMessage(getString(R.string.message_operation_fail))
                    }, { exception ->
                        LoadingHint.hide()
                        exception.printStackTrace()
                        FEToast.showMessage(getString(R.string.message_operation_fail))
                    })
        }
    }

    // 全部都可以去掉
    override fun bindView() {
        LoadingHint.show(this)
        Observable.unsafeCreate<MutableMap<Category, MutableList<AppMenu>>> { it.onNext(FunctionManager.getCustomCategoryMenus()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ customMenus ->
                    LoadingHint.hide()
                    val categories = FunctionManager.getCategories().apply {
                        val qc = Category.quickShortCut()
                        if (!contains(qc)) {
                            add(0, qc)
                        }
                    }

                    val categorySize = categories?.size ?: 0
                    if (categorySize <= 3) {
                        tabLayout.tabMode = TabLayout.MODE_FIXED
                        tabLayout.tabGravity = TabLayout.GRAVITY_CENTER
                    } else {
                        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
                    }

                    val fragments = mutableListOf<Fragment>()
                    val titles = mutableListOf<String>()
                    categories?.forEach {
                        if (it.isEditable) {
                            tabLayout.addTab(tabLayout.newTab().setTag(it).setText(it.value))
                            titles.add(it.value)
                            val displayApps = customMenus?.get(it)
                            val standardApps = FunctionManager.getStandardMenus(it.key)
                            if (CommonUtil.nonEmptyList(displayApps)) {
                                standardApps.removeAll(displayApps!!)
                            }
                            fragments.add(ModuleSettingPage.fucking(it, displayApps, standardApps))
                        }
                    }

                    val adapter = BaseFragmentPagerAdapter(supportFragmentManager, fragments).apply {
                        setTitles(titles)
                    }

                    viewPager.adapter = adapter
                    viewPager.offscreenPageLimit = fragments.size
                    viewPager.setCanScroll(true)
                    tabLayout.setupWithViewPager(viewPager)

                    val selectedIndex = intent.getIntExtra("selectedIndex", 0)
                    viewPager.currentItem = selectedIndex
                }, {
                    LoadingHint.hide()
                    it.printStackTrace()
                    FEToast.showMessage(getString(R.string.module_setting_modify_load_failed))
                    finish()
                })


        // 2. TabLayout 的点击事件
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tabLayout.selectedTabPosition
            }
        })
    }

    fun setViewPagerScroll(canScroll: Boolean) {
        viewPager.setCanScroll(canScroll)
    }

    fun setCompleteButtonEnable(enable: Boolean) {
        this.toolbar?.rightTextView?.isEnabled = enable
    }

    private fun getSelectedCategory() = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.tag as Category

}