package cn.flyrise.feep.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.FragmentTransaction
import android.view.View
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.PreferencesUtils
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.location.event.EventMainTabShowFragment
import cn.flyrise.feep.location.fragment.SignInCalendarFragment
import cn.flyrise.feep.location.fragment.SignInLeaderTabStatisFragment
import cn.flyrise.feep.location.fragment.SignInMainFragment
import cn.flyrise.feep.utils.Patches
import cn.squirtlez.frouter.annotations.Route
import kotlinx.android.synthetic.main.sign_in_main_tab_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 新建：陈冕;
 *日期： 2018-7-30-9:57.
 * 签到选项卡主界面
 * 7.0并且是领导的情况下才会显示统计界面
 */
@Route("/location/main")
class SignInMainTabActivity : BaseActivity() {

    private var mToolBar: FEToolbar? = null
    private var mainFragment: SignInMainFragment? = null
    private var calendarFragment: SignInCalendarFragment? = null
    private var leaderStatisFragment: SignInLeaderTabStatisFragment? = null
    private var currentView: View? = null
    private var isLeader: Boolean = false
    private val mHandler = Handler()

    companion object {

        val showMain = 1
        val showRecord = 2
        val showStatistics = 3
        fun start(context: Context, showIndex: Int) {
            val intent = Intent(context, SignInMainTabActivity::class.java)
            intent.putExtra("SHOW_INDEX", showIndex)
            if (CoreZygote.getApplicationServices().activityInStacks(SignInMainTabActivity::class.java)) {
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }

        fun start(context: Context, isOpenCustom: Boolean) {
            val intent = Intent(context, SignInMainTabActivity::class.java)
            intent.putExtra("is_open_custom", isOpenCustom)
            if (CoreZygote.getApplicationServices().activityInStacks(SignInMainTabActivity::class.java)) {
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.sign_in_main_tab_layout)
        val data = intent?.getIntExtra("SHOW_INDEX", showMain)
        showFragment(showMain)
        mHandler.postDelayed({ showFragment(data) }, 300)
    }

    private fun showFragment(showIndex: Int? = showMain) {
        setFragmentView(when (showIndex) {
            showRecord -> signInRecord
            showStatistics -> signInStatistics
            else -> signInMain
        })
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        mToolBar = toolbar
    }

    override fun bindData() {
        super.bindData()
        signInStatistics.visibility = if (getLeader()) View.VISIBLE else View.GONE
    }

    private fun getLeader() = FunctionManager.hasPatch(Patches.PATCH_SIGN_IN_STATICS)
            && SpUtil.get(PreferencesUtils.HAS_SUB_SUBORDINATES, false)

    override fun bindListener() {
        super.bindListener()
        signInMain.setOnClickListener { setFragmentView(it) }
        signInRecord.setOnClickListener { setFragmentView(it) }
        signInStatistics.setOnClickListener { setFragmentView(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mainFragment?.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("CommitTransaction")
    private fun setFragmentView(view: View) {
        currentView = view
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        hindFragment(ft)
        when (currentView) {
            signInMain -> {
                if (mainFragment == null) {
                    mainFragment = SignInMainFragment()
                    mainFragment!!.setOpenCustom(intent?.getBooleanExtra("is_open_custom", false))
                    mainFragment!!.setLeaderListner {
                        isLeader = it
                        calendarFragment?.setLeader(it)
                        leaderStatisFragment?.setLeader(it)
                    }
                    ft.add(fragment_layout.id, mainFragment!!)
                }
                ft.show(mainFragment!!)
            }
            signInRecord -> {
                if (calendarFragment == null) {
                    calendarFragment = SignInCalendarFragment()
                    ft.add(fragment_layout.id, calendarFragment!!)
                }
                calendarFragment!!.setLeader(getLeader())
                calendarFragment!!.refreshRequestSignHistory()
                ft.show(calendarFragment!!)
            }
            signInStatistics -> {
                if (leaderStatisFragment == null) {
                    leaderStatisFragment = SignInLeaderTabStatisFragment()
                    ft.add(fragment_layout.id, leaderStatisFragment!!)
                }
                leaderStatisFragment!!.setLeader(getLeader())
                ft.show(leaderStatisFragment!!)
            }
        }
        ft.commitAllowingStateLoss()
        setSelectedMenu()
        setMainToolbar()
    }

    private fun hindFragment(ft: FragmentTransaction) {
        if (mainFragment != null) ft.hide(mainFragment!!)
        if (calendarFragment != null) ft.hide(calendarFragment!!)
        if (leaderStatisFragment != null) ft.hide(leaderStatisFragment!!)

    }

    private fun setMainToolbar() {
        when (currentView) {
            signInMain -> mainFragment?.setToolbarListener(this, mToolBar!!)
            signInRecord -> calendarFragment?.setToolBarListener(this, mToolBar!!)
            signInStatistics -> leaderStatisFragment?.setToolBarListener(this, mToolBar!!)
        }
    }

    private fun setSelectedMenu() {
        signInMain.setSelectedMenu(false)
        signInRecord.setSelectedMenu(false)
        signInStatistics.setSelectedMenu(false)
        when (currentView) {
            signInMain -> signInMain.setSelectedMenu(true)
            signInRecord -> signInRecord.setSelectedMenu(true)
            signInStatistics -> signInStatistics.setSelectedMenu(true)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventShowFragment(event: EventMainTabShowFragment) {
        showFragment(event.showIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}