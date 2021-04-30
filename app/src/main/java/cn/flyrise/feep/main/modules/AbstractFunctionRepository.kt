package cn.flyrise.feep.main.modules

import android.content.Context
import android.text.TextUtils
import cn.flyrise.android.protocol.model.MainMenu
import cn.flyrise.feep.FEApplication
import cn.flyrise.feep.R
import cn.flyrise.feep.R.drawable.*
import cn.flyrise.feep.R.string.*
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.common.X.MainMenu.*
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.CommonUtil.getString
import cn.flyrise.feep.core.function.*
import java.util.*

/**
 * @author 社会主义接班人
 * @since 2018-07-19 19:04
 */
abstract class AbstractFunctionRepository(val c: Context) : IFunctionRepository {

    private var modules: MutableList<Module> = mutableListOf()              // 应用模块信息
    private var allModuleFromServer: MutableList<Module>? = null                   // 服务端的全部模块
    private var subModules: MutableMap<String, MutableList<SubModule>>? = null     // 子模块信息（目前仅有协同的子模块）
    private var patches: MutableList<Int>? = null                           // 补丁列表
    private var categories: MutableList<Category>? = null                   // 应用分类
    protected var customSortRules: MutableList<Int>? = null                 // 自定义排序
    private var quickShortCut: MutableList<Int>? = null                     // 用户自定义的快捷方式
    private var allQuickShortCut: MutableList<ShortCut>? = null             // 当前用户所有的全部快捷方式

    override fun save(response: FunctionModuleResponse) {
        this.patches = response.patches
        this.subModules = response.moduleChildren
        this.categories = response.category
        this.customSortRules = response.customIds
        this.quickShortCut = response.quick
        this.allQuickShortCut = response.quickAll

        // 接下来对应用中心的数据进行处理
        val app = (c.applicationContext as FEApplication).apply {
//            hasDownloadManager = false
//            isSupportFileEncrypt = false
            isModify = false
            isOnSite = false
        }
        val transformId = fun(mId: String): Int {
            try {
                return mId.toInt()
            } catch (e: NumberFormatException) {
                return -1
            }
        }

        val applySetting = fun(mId: Int) {
            when (mId) {
//                11 -> app.hasDownloadManager = true
//                31 -> app.isSupportFileEncrypt = true
                33 -> app.isModify = true
                34 -> app.isOnSite = true
            }
        }

        this.allModuleFromServer = response.modules
        this.modules.clear()                                // 预防账号切换导致数据异常
        response.modules?.forEach {
            val mId = transformId(it.id)
            if (mId == -1) return@forEach
            applySetting(mId)

            if (!isDisplay(mId)) return@forEach
            this.modules.add(it)
        }
    }

    override fun getModules() = this.modules
    override fun getModule(mId: Int) = allModuleFromServer?.find { it.moduleId == mId }
    override fun hasModule(mId: Int) = allModuleFromServer?.find { it.moduleId == mId } != null
    override fun isNative(mId: Int) = allModuleFromServer?.find { it.moduleId == mId }?.isNative ?: false

    override fun getCategories() = categories
    override fun hasCategory(c: Category?) = categories?.find { TextUtils.equals(c?.key, it.key) } != null
    override fun getCategory(cId: String?) = categories?.find { TextUtils.equals(cId, it.key) }

    override fun getAppShortCuts() = quickShortCut
    override fun getAllAppShortCuts() = allQuickShortCut
    override fun getQuick(quickId: Int) = allQuickShortCut?.find { it.id == quickId }

    override fun getAppSortRules() = customSortRules
    override fun hasPatch(p: Int) = patches?.find { it == p } == p

    override fun getTopMenu(): MutableList<AppTopMenu> {
        val m1 = AppTopMenu(getString(top_message), Message, icon_main_message, icon_main_message_on)
//        val m2 = AppTopMenu(getString(top_associate), Associate, icon_main_circle, icon_main_circle_on)
//        val m3 = AppTopMenu(getString(top_application), Application, icon_main_application, icon_main_application_on)
//        val m4 = AppTopMenu(getString(top_contact), Contact, icon_main_contacts, icon_main_contacts_on)

        val m2 = AppTopMenu(getString(study_on_line), Study, dj_icon_index_gray, dj_icon_index_red)
        val m3 = AppTopMenu(getString(exam_on_line), EXAM, dj_icon_index_gray, dj_icon_index_red)
        val m4 = AppTopMenu(getString(top_mine), Mine, icon_main_mine, icon_main_mine_on)

        val app = c.applicationContext as FEApplication
        if (app.userInfo == null || CommonUtil.isEmptyList(app.userInfo.bottomMenu)) {
            return Arrays.asList(m1, m2, m3, m4)
        }

        val topMenus = mutableListOf<AppTopMenu>()

//        val nameFunc = fun(topMenu: AppTopMenu, mainMenu: MainMenu): AppTopMenu {
//            if (!TextUtils.isEmpty(mainMenu.name)) {
//                topMenu.menu = mainMenu.name
//            }
//            return topMenu
//        }

        topMenus.add(m1)
        topMenus.add(m2)
        topMenus.add(m3)
        topMenus.add(m4)

//        app.userInfo.bottomMenu.forEach {
//            if (TextUtils.isEmpty(it.name)) return@forEach
//            when (it.id) {
//                Study -> topMenus.add(nameFunc(m1, it))
////                Associate -> topMenus.add(nameFunc(m2, it))
////                Application -> topMenus.add(nameFunc(m3, it))
////                Contact -> topMenus.add(nameFunc(m4, it))
//                Mine -> topMenus.add(nameFunc(m2, it))
//            }
//        }
        return topMenus
    }

    override fun getSubMenus(moduleId: Int): MutableList<AppSubMenu>? {
        // 目前（2018年）仅支持协同的子菜单
        if (moduleId != X.Func.Collaboration) return null

        val func = fun(mId: Int) = when (mId) {
            0 -> AppSubMenu(0, getString(R.string.approval_todo))
            1 -> AppSubMenu(1, getString(R.string.approval_done))
            4 -> AppSubMenu(4, getString(R.string.approval_sended))
            23 -> AppSubMenu(23, getString(R.string.approval_jijian))
            24 -> AppSubMenu(24, getString(R.string.approval_pingjian))
            25 -> AppSubMenu(25, getString(R.string.approval_yuejian))
            else -> null
        }

        val menus = subModules?.get("${X.Func.Collaboration}")
        val subMenus = mutableListOf<AppSubMenu>()
        menus?.forEach {
            val subMenu = func(CommonUtil.parseInt(it.id))
            if (subMenu == null) return@forEach
            subMenus.add(subMenu!!)
        }

        if (!subMenus.isEmpty()) return subMenus
        return Arrays.asList(func(0), func(1), func(4))
    }

    fun isDisplay(mId: Int) = when (mId) {        // 不在应用中心显示的菜单
        0, 1, 2, 3, 4, 7, 8, 10, 11, 16, 17, 18, 19, 20, 22, 26, 30, 31, 33, 34, 42 -> false
        else -> true
    }

    override fun initRepository() {}

    override fun emptyData() {
        modules.clear()                 // 应用模块信息
        allModuleFromServer?.clear()  // 服务端的全部模块
        subModules?.clear()           // 子模块信息（目前仅有协同的子模块）
        patches?.clear()              // 补丁列表
        categories?.clear()          // 应用分类
        customSortRules?.clear()    // 自定义排序
        quickShortCut?.clear()      // 用户自定义的快捷方式
        allQuickShortCut?.clear()   // 当前用户所有的全部快捷方式
    }
}