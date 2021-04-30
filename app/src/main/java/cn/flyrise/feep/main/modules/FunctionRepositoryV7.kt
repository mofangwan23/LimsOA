package cn.flyrise.feep.main.modules

import android.content.Context
import android.support.annotation.Keep
import android.text.TextUtils
import cn.flyrise.android.protocol.entity.SaveAppCustomSortRequest
import cn.flyrise.feep.core.function.*
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.request.ResponseContent
import rx.Observable

/**
 * @author 社会主义接班人
 * @since 2018-07-23 10:38
 * FEv7.0 及以上版本的应用中心数据管理
 */
@Keep
class FunctionRepositoryV7(c: Context, val p: IPreDefinedModuleRepository) : AbstractFunctionRepository(c), IFunctionProxy {

    /**
     * 获取首页应用中心用于显示的应用
     */
    override fun getAppMenus(): List<AppMenu> {
        if (categories?.size == 0) return mutableListOf()
        if (appSortRules?.size == 0) return mutableListOf()
        return transformToList(transformToMap(mutableListOf<Module>().apply {
            appSortRules?.forEach {
                val menu = getModule(it) ?: return@forEach
                if (TextUtils.isEmpty(menu.category)) return@forEach
                if (!isDisplay(menu.moduleId)) return@forEach
                add(menu)
            }
        }))
    }

    /**
     * 获取应用快捷入口列表
     */
    override fun getQuickMenus() = mutableListOf<AppMenu>().apply {
        appShortCuts?.forEach {
            val sc = getQuick(it) ?: return@forEach
            add(AppMenu.fromShortCut(sc, p.getShortCut(it)))
        }
    }

    /**
     * 获取自定义菜单、包含快捷菜单
     */
    override fun getCustomCategoryMenus() = mutableMapOf<Category, MutableList<AppMenu>>().apply {
        putAll(transformToMap(mutableListOf<Module>().apply {
            appSortRules?.forEach {
                val menu = getModule(it) ?: return@forEach
                if (TextUtils.isEmpty(menu.category)) return@forEach
                if (!isDisplay(menu.moduleId)) return@forEach
                add(menu)
            }
        }))
        var quickCategory = getCategory("10086")
        if (quickCategory == null) {
            quickCategory = Category().apply {
                key = "10086"
                value = "快捷入口"
                editable = "1"
            }
        }
        put(quickCategory, mutableListOf<AppMenu>().apply {
            appShortCuts?.forEach {
                val sc = getQuick(it) ?: return@forEach
                add(AppMenu.fromShortCut(sc, p.getShortCut(it)))
            }
        })
    }

    /**
     * 获取当前服务端器上的标准菜单
     */
    override fun getStandardMenus(category: String) = mutableListOf<AppMenu>().apply {
        if (TextUtils.equals(category, "10086")) {
            allAppShortCuts?.forEach {
                add(AppMenu.fromShortCut(it, p.getShortCut(it.id)))
            }
        } else {
            modules.forEach {
                if (TextUtils.equals(category, it.category)) {
                    add(AppMenu.fromModule(it).apply {
                        imageRes = p.getV7Icon(menuId)
                    })
                }
            }
        }
    }

    /**
     * 按类别获取用户自定义排列的菜单
     */
    override fun getCustomMenus(category: String) = mutableListOf<AppMenu>().apply {
        if (TextUtils.equals(category, "10086")) {
            appShortCuts?.forEach {
                val sc = getQuick(it) ?: return@forEach
                add(AppMenu.fromShortCut(sc, p.getShortCut(it)))
            }
        } else {
            appSortRules?.forEach {
                val m = getModule(it) ?: return@forEach
                if(!isDisplay(m.moduleId)) return@forEach
                if (TextUtils.equals(category, m.category)) {
                    add(AppMenu.fromModule(m).apply {
                        imageRes = p.getV7Icon(menuId)
                    })
                }
            }
        }
    }

    /**
     * 保存用户自定义的显示设置
     */
    override fun saveDisplayOptions(customMenus: Map<Category, List<AppMenu>>?): Observable<Int> {
        return Observable.unsafeCreate {
            if (customMenus?.isEmpty() ?: true) {
                it.onNext(200)
                it.onCompleted()
                return@unsafeCreate
            }

            val menuIds = mutableListOf<Int>().apply {
                categories?.forEach {
                    if (TextUtils.equals(it.key, "10086")) return@forEach
                    val menus = customMenus?.get(it) ?: return@forEach
                    menus.forEach { add(it.menuId) }
                }
            }

            val quickIds = mutableListOf<Int>().apply {
                val quickCategory = categories?.find { TextUtils.equals(it.key, "10086") } ?: return@apply
                val menus = customMenus?.get(quickCategory) ?: return@apply
                menus.forEach { add(it.menuId) }
            }

            val request = SaveAppCustomSortRequest().apply {
                this.method = "appsConfig"
                this.customIds = StringBuilder().apply {
                    menuIds.forEachIndexed { index, i ->
                        append(if (index == 0) i else ",$i")
                    }
                }.toString()
                this.quickIds = StringBuilder().apply {
                    quickIds.forEachIndexed { index, i ->
                        append(if (index == 0) i else ",$i")
                    }
                }.toString()
            }

            FEHttpClient.getInstance().post(request, object : ResponseCallback<ResponseContent>() {
                override fun onCompleted(response: ResponseContent?) {
                    if (response != null && TextUtils.equals(response.errorCode, "0")) {
                        customSortRules = menuIds
                        it.onNext(200)
                    } else {
                        it.onNext(404)
                    }
                    it.onCompleted()
                }

                override fun onFailure(repositoryException: RepositoryException) {
                    it.onNext(404)
                    it.onCompleted()
                }
            })
        }
    }

    private fun transformToMap(modules: List<Module>) = mutableMapOf<Category, MutableList<AppMenu>>().apply {
        modules?.forEach {
            val c = getCategory(it.category) ?: return@forEach

            if (containsKey(c)) {
                get(c)!!.add(AppMenu.fromModule(it).apply {
                    editable = c.isEditable
                    imageRes = p.getV7Icon(menuId)
                })
                return@forEach
            }

            put(c, mutableListOf<AppMenu>().apply {
                add(AppMenu.fromModule(it).apply {
                    editable = c.isEditable
                    imageRes = p.getV7Icon(menuId)
                })
            })
        }
    }

    private fun transformToList(menuMap: Map<Category, List<AppMenu>>) = mutableListOf<AppMenu>().apply {
        categories?.forEach {
            if (TextUtils.equals(it.key, "10086")) return@forEach
            val menus = menuMap?.get(it)
            if (menus?.size ?: 0 == 0) return@forEach
            add(AppMenu.categoryMenu(it))               // 1. 添加类别
            addAll(menus!!)                             // 2. 添加子菜单
            val mod = menus.size % 4                    // 3. 补全空白
            if (mod > 0) {
                for (i in 0..3 - mod) {
                    add(AppMenu.blankMenu())
                }
            }
        }
    }
}