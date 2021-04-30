package cn.flyrise.feep.main.modules

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.android.protocol.entity.SwitchCompanyRequest
import cn.flyrise.feep.FEMainActivity
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.model.Department
import cn.flyrise.feep.addressbook.source.AddressBookRepository
import cn.flyrise.feep.addressbook.utils.ContactsIntent
import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity
import cn.flyrise.feep.cordova.Activity.KnowledgeActivity
import cn.flyrise.feep.cordova.Activity.ScheduleActivity
import cn.flyrise.feep.cordova.utils.CordovaShowUtils
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.CordovaShowInfo
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.core.function.AppMenu
import cn.flyrise.feep.core.function.BadgeManager
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.email.NewAndReplyMailActivity
import cn.flyrise.feep.event.CompanyChangeEvent
import cn.flyrise.feep.form.FormListActivity
import cn.flyrise.feep.location.assistant.SignInRapidlyActivity
import cn.flyrise.feep.meeting7.ui.MeetingRoomActivity
import cn.flyrise.feep.schedule.NewScheduleActivity
import cn.flyrise.feep.utils.Patches
import cn.flyrise.feep.workplan7.PlanCreateMainActivity
import cn.flyrise.feep.x5.X5BrowserActivity
import kotlinx.android.synthetic.main.fragment_main_module_plus.*
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author zyp
 * @since 2018-07-25 11:35
 */
class MainModuleFragment : Fragment() {

    private lateinit var mAdapter: MainModuleAdapter
    private lateinit var mTvCompany: TextView
    private var subscription: Subscription? = null
    private var companies: MutableList<Department>? = null
    private var company: Department? = null
    private var isHeaderExpand = true
    private var isShowFragment = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_module_plus, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val host = CoreZygote.getLoginUserServices().serverAddress
        mAdapter = MainModuleAdapter(activity, host).apply {
            setOnModuleClickListener click@{ _, m ->
                if (m.menuId == X.Func.Schedule && !FunctionManager.isNative(X.Func.Schedule) && TextUtils.isEmpty(m.appURL)) {
                    val intent = Intent(activity, ScheduleActivity::class.java)
                    val showInfo = CordovaShowInfo()
                    showInfo.type = X.Func.Schedule
                    intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(showInfo))
                    startActivity(intent)
                    return@click
                }
                if (m.menuId == AppMenu.ID_CATEGORY || m.menuId == AppMenu.ID_EMPTY) return@click
                val intentClass = FunctionManager.findClass(m.menuId) ?: X5BrowserActivity::class.java
                startActivity(Intent(activity, intentClass).apply {
                    putExtra("moduleId", m.menuId)
                })


            }
        }
        recyclerView.apply {
            layoutManager = GridLayoutManager(activity, 4)
            this.itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
            val statusBarHeight = DevicesUtil.getStatusBarHeight(getActivity())
            Observable.just(statusBarHeight)
                    .delay(500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val topMargin = headerBar.measuredHeight + it
                        headerBar.layoutParams.height = topMargin
                        headerBar.requestLayout()

                        val params = view_header_content.layoutParams as FrameLayout.LayoutParams
                        params.topMargin = topMargin
                        view_header_content.requestLayout()
                        view_header_bar_expand.setPadding(view_header_bar_expand.paddingLeft, it, view_header_bar_expand.paddingRight, 0)
                        view_header_bar_content.setPadding(view_header_bar_content.paddingLeft, it, view_header_bar_content.paddingRight, 0)
                    }
        }

        applicationLayout.apply {
            setParentInterceptTouchEventCallback {
                (recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition() == 0
            }
            setOnHeaderStatusChangeListener {
                isHeaderExpand = it
                if (isShowFragment && activity != null && (activity is FEMainActivity))
                    (activity as FEMainActivity).updateStatusBar(this@MainModuleFragment)
            }
        }

        val fetchCompanies = fun() {
            if (FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) {
                Observable
                        .unsafeCreate<MutableList<Department>> {
                            val userId = CoreZygote.getLoginUserServices().userId
                            val companies = AddressBookRepository.get().queryCompanyWhereUserIn(userId)
                            it.onNext(companies)
                            it.onCompleted()
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it?.size == 1) {
                                ivCompanySelected.visibility = View.GONE
                                mTvCompany.isEnabled = false
                            } else {
                                mTvCompany.isEnabled = true
                                ivCompanySelected.visibility = View.VISIBLE
                            }
                            companies = it
                        }
            } else {
                layoutModuleHeaderV6.visibility = View.VISIBLE
                layoutModuleHeaderV7.visibility = View.GONE
            }
        }

        mTvCompany = tvCompany

        layoutModuleHeaderV7.setOnClickListener {
            if (!FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) {
                // V7.0 以下版本
                return@setOnClickListener
            }

            if (CommonUtil.isEmptyList(companies)) {
                fetchCompanies()
                return@setOnClickListener
            }

            PopComanySelector(activity!!).apply {
                setCompany(company!!)
                setCompanies(companies!!)
                setOnCompanySelectListener {
                    company = it
                    mTvCompany.text = it.name
                    LoadingHint.show(activity)
                    notifyCompanyChange(it)
                    Sasigay.saveCompany(it)
                    Sasigay.saveCompanyToStorage(it)
                }
            }.show(view_header_bar_expand)
        }

        fetchCompanies()

        if (FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) {
            Observable
                    .unsafeCreate<Department> {
                        val userId = CoreZygote.getLoginUserServices().userId
                        val dept = AddressBookRepository.get().queryDepartmentWhereUserIn(userId)
                        company = AddressBookRepository.get().queryCompanyWhereDepartmentIn(dept.deptId)
                        it.onNext(company)
                        it.onCompleted()
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Sasigay.saveCompany(it)
                        mTvCompany.text = it.name
                    }, {
                        it.printStackTrace()
                    })
        }

        bindDataToViews()
    }

    fun isHeaderExpand() = isHeaderExpand

    private fun bindDataToViews() {
        // 1. 设置事件的处理
        val settingFunc = fun(_: View) { startActivityForResult(Intent(activity, ModuleSettingActivity::class.java), 888) }
        ivModuleSetting.setOnClickListener(settingFunc)
        ivModuleSettingSM.setOnClickListener(settingFunc)
        tvModuleSetting.setOnClickListener {
            startActivityForResult(Intent(activity, ModuleSettingActivity::class.java).apply {
                putExtra("selectedIndex", 1)
            }, 888)
        }

        // 2. 获取快捷方式
        val quickMenus = FunctionManager.getQuickMenus()

        // 动态设置这玩意的宽度
        if (CommonUtil.isEmptyList(quickMenus)) {
            view_header_content.visibility = View.GONE
            view_header_bar_shrink.visibility = View.GONE
            view_header_bar_expand.visibility = View.VISIBLE
            applicationLayout.setEnableScroll(false)
        } else {
            view_header_content.visibility = View.VISIBLE
            view_header_bar_shrink.visibility = View.VISIBLE
            view_header_bar_expand.visibility = View.VISIBLE
            applicationLayout.setEnableScroll(true)

            val quickFunc = fun(v: AppMenu) {
                when (v.menuId) {
                    X.Quick.NewCollaboration -> startActivity(Intent(activity, NewCollaborationActivity::class.java))
                    X.Quick.NewPlan -> PlanCreateMainActivity.start(activity)
                    X.Quick.NewSchedule -> if (FunctionManager.isNative(X.Func.Schedule)) {
                        startActivity(Intent(activity, NewScheduleActivity::class.java))
                    } else {
                        val intent = Intent(activity, ScheduleActivity::class.java)
                        val showInfo = CordovaShowInfo()
                        showInfo.type = X.Func.Schedule
                        showInfo.pageid = CordovaShowUtils.ADD_SCHEDULE
                        intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(showInfo))
                        startActivity(intent)
                    }
                    X.Quick.NewMail -> startActivity(Intent(activity, NewAndReplyMailActivity::class.java))
                    X.Quick.Location -> SignInRapidlyActivity.startSignInRapidly(activity!!)
                    X.Quick.NewMeeting -> startActivity(Intent(activity, MeetingRoomActivity::class.java))
                    X.Quick.Hyphenate -> ContactsIntent(activity).title(getString(R.string.lbl_message_title_chat)).startChat().open()
                    X.Quick.NewForm -> {
                        val module = FunctionManager.findModule(X.Func.NewForm)
                        startActivity(Intent().apply {
                            if (TextUtils.isEmpty(module?.url ?: "")) {
                                setClass(activity, FormListActivity::class.java)
                            } else {
                                setClass(activity, X5BrowserActivity::class.java)
                                putExtra("appointURL", module.url)
                                putExtra("moduleId", X.Func.Default)
                            }
                        })
                    }
                }
            }

            quickGridView.apply {
                adapter = ShortCutAdapter(context, quickMenus)
                setOnItemClickListener { _, _, i, _ ->
                    quickFunc(adapter.getItem(i) as AppMenu)
                }
            }

            ivQuickSM1.visibility = View.GONE
            ivQuickSM2.visibility = View.GONE
            ivQuickSM3.visibility = View.GONE
            ivQuickSM4.visibility = View.GONE
            quickMenus.forEachIndexed { index, menu ->
                when (index) {
                    0 -> ivQuickSM1.bind(menu, quickFunc)
                    1 -> ivQuickSM2.bind(menu, quickFunc)
                    2 -> ivQuickSM3.bind(menu, quickFunc)
                    3 -> ivQuickSM4.bind(menu, quickFunc)
                }
            }
        }

        // 3. 菜单
        Observable.unsafeCreate<List<AppMenu>> { it.onNext(FunctionManager.getAppMenu()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    LoadingHint.hide()
                    if (it?.size ?: 0 == 0) {
                        layoutEmptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        layoutEmptyView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        mAdapter.setModules(it)
                        fetchModuleBadge()
                    }
                }, {
                    it.printStackTrace()
                    FEToast.showMessage("应用加载失败")
                    LoadingHint.hide()
                })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        isShowFragment = !hidden
        if (hidden) {//隐藏应用界面时，展开顶部快捷入口
            applicationLayout.setEnableScroll(!CommonUtil.isEmptyList(FunctionManager.getQuickMenus()))
        }else {
            refreshAdapter()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 888 && data != null) {
            val isMove = data.getBooleanExtra("isMove", false)
            if (isMove) {
                LoadingHint.show(activity)
                refreshAdapter()
            }
        }
    }

    override fun onDestroyView() {
        subscription?.unsubscribe()
        super.onDestroyView()
    }

    private fun fetchModuleBadge() {
        subscription = BadgeManager.getInstance().fetchFunctionBadge()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    mAdapter.setModuleBadge(result)
                    (activity as FEMainActivity).setUnreadApplicationMessage(result != null && !result.isEmpty())
                }
    }

    private fun ImageView.bind(menu: AppMenu, clickFunc: (AppMenu) -> Unit) {
        visibility = View.VISIBLE
        setOnClickListener { clickFunc(menu) }

        val host = CoreZygote.getLoginUserServices().serverAddress
        if (TextUtils.isEmpty(menu.icon)) {
            setImageResource(menu.imageRes)
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        } else {
            clearColorFilter()
            var defaultImageRes = R.drawable.ic_unknown
            val sc = FunctionManager.getDefinedModuleRepository().getShortCut(menu.menuId)
            if (sc != null) {
                defaultImageRes = sc.imageRes
            }

            FEImageLoader.load(context, this,
                    if (menu.icon.startsWith(host)) menu.icon else host + menu.icon, defaultImageRes)
        }
    }

    fun notifyCompanyChange(company: Department) {
        if (TextUtils.isEmpty(company.deptId)) return
        EventBus.getDefault().post(CompanyChangeEvent(company))
        FEHttpClient.getInstance().post(SwitchCompanyRequest(company.deptId), object : ResponseCallback<ResponseContent>() {
            override fun onCompleted(t: ResponseContent?) {
                if (t != null) {
                    if (TextUtils.equals("0", t.errorCode)) {
                        refreshAdapter()
                    }
                }
            }
        })
    }

    private fun refreshAdapter(){
        FunctionManager.getInstance().fetchFunctions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mAdapter.setModuleBadge(it.appBadgeMap)
                    bindDataToViews()
                }, { it.printStackTrace() })
    }
}