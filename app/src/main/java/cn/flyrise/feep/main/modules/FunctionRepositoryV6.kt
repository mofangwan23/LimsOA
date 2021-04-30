package cn.flyrise.feep.main.modules

import android.content.Context
import android.support.annotation.Keep
import android.text.TextUtils
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.core.function.*
import cn.flyrise.feep.utils.Patches
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import rx.Observable
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

/**
 * @author 社会主义接班人
 * @since 2018-07-23 10:38
 * FEv6.6 及以下版本的应用中心数据管理
 */
@Keep
class FunctionRepositoryV6(c: Context, val p: IPreDefinedModuleRepository) : AbstractFunctionRepository(c), IFunctionProxy {

    private var optionsStoragePath = CoreZygote.getPathServices().userPath + File.separator + CommonUtil.getMD5("module_setting_v6")
    private var customMenuMap: MutableMap<Category, MutableList<AppMenu>>? = null
    private val quickCategory: Category
    private val basicCategory: Category

    init {
        quickCategory = Category().apply {
            key = "10086"
            value = "快捷入口"
            editable = "1"
        }

        basicCategory = Category().apply {
            key = "1"
            value = "全部应用"
            editable = "1"
        }
    }

    override fun initRepository() {
        val defaultCustomMenuMap = fun() = mutableMapOf<Category, MutableList<AppMenu>>().apply {
            put(quickCategory, mutableListOf<AppMenu>().apply {
                defaultShortCut().forEach {
                    add(AppMenu.fromShortCut(it, p.getShortCut(it.id)))
                }
            })
            put(basicCategory, mutableListOf<AppMenu>().apply {
                modules.forEach {
                    add(AppMenu.fromModule(it).apply {
                        imageRes = p.getV7Icon(menuId)
                    })
                }
            })
        }

        val optionsFile = File(optionsStoragePath)
        if (!optionsFile.exists()) {
            customMenuMap = defaultCustomMenuMap()
            return
        }

        var bytes: ByteArray? = null
        FileInputStream(optionsFile).apply {
            val len = optionsFile.length()
            bytes = ByteArray(len.toInt())
            read(bytes)
            close()
        }

        if (bytes?.size ?: 0 == 0) {
            customMenuMap = defaultCustomMenuMap()
            return
        }

        try {
            customMenuMap = mutableMapOf<Category, MutableList<AppMenu>>().apply {
                val gson = GsonUtil.getInstance().getGson()
                JsonParser().parse(String(bytes!!)).asJsonArray.forEach {
                    var category: Category? = null
                    var menus: MutableList<AppMenu>? = null
                    it.asJsonArray.forEach {
                        if (it is JsonObject) {
                            category = gson.fromJson(it, Category::class.java)
                        } else if (it is JsonArray) {
                            menus = gson.fromJson(it, object : TypeToken<List<AppMenu>>() {}.type)
                        }
                    }
                    put(category!!, menus!!)
                }
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
            customMenuMap = defaultCustomMenuMap()
        }
    }

    /**
     * 获取首页应用中心用于显示的应用
     */
    override fun getAppMenus(): List<AppMenu> {
        // 1. 获取当前服务器上的应用数据
        val standardMenus = getStandardMenus(basicCategory.key)

        // 2. 获取当前设备上用户保存过的自定义应用数据
        val customMenus = getCustomMenus(basicCategory.key)

        // 3. 不存在自定义排列过的数据
        if (customMenus.isEmpty()
                && customMenuMap != null
                && customMenuMap!!.isEmpty()) {
            return mutableListOf()
        }

        // 4. 已当前用户自定义排列过的数据为主，但是要移除掉服务器上不存在的部分
        val invalidMenus = mutableListOf<AppMenu>().apply {
            customMenus!!.forEach {
                if (!standardMenus.contains(it)) {
                    add(it)
                }
            }
        }

        // 5. 以自定义的为主，
        val displayMenus = mutableListOf<AppMenu>().apply {
            addAll(customMenus!!)
            removeAll(invalidMenus)
        }

        // 6. 补全空白
        val mod = displayMenus.size % 4
        if (mod > 0) {
            for (i in 0..3 - mod) {
                displayMenus.add(AppMenu.blankMenu())
            }
        }

        return displayMenus
    }

    override fun getQuickMenus(): List<AppMenu> {
        if (customMenuMap == null || customMenuMap?.isEmpty() != false) {
            return mutableListOf<AppMenu>().apply {
                defaultShortCut().forEach {
                    add(AppMenu.fromShortCut(it, p.getShortCut(it.id)))
                }
            }
        }

        return customMenuMap?.get(quickCategory) ?: mutableListOf<AppMenu>().apply {
            defaultShortCut().forEach {
                add(AppMenu.fromShortCut(it, p.getShortCut(it.id)))
            }
        }
    }

    override fun getCustomCategoryMenus() = mutableMapOf<Category, MutableList<AppMenu>>().apply {
        if (customMenuMap != null) putAll(customMenuMap!!)
    }

    override fun getStandardMenus(category: String) = mutableListOf<AppMenu>().apply {
        if (TextUtils.equals(category, "10086")) {
            allShortCuts().forEach {
                add(AppMenu.fromShortCut(it, p.getShortCut(it.id)))
            }
        } else {
            modules.forEach {
                add(AppMenu.fromModule(it).apply {
                    imageRes = p.getV7Icon(menuId)
                })
            }
        }
    }

    override fun getCustomMenus(category: String) = mutableListOf<AppMenu>().apply {
        val key = if (TextUtils.equals(category, "10086")) quickCategory else basicCategory
        val menus = customMenuMap?.get(key) ?: return@apply
        addAll(menus)
    }

    override fun saveDisplayOptions(customs: Map<Category, List<AppMenu>>?): Observable<Int> {
        return Observable.unsafeCreate {
            if (customs?.isEmpty() != false) {
                it.onNext(200)
                it.onCompleted()
                return@unsafeCreate
            }

            val optionsFile = File(optionsStoragePath)
            if (optionsFile.exists()) optionsFile.delete()

            FileWriter(optionsFile).apply {
                val menusJson = GsonUtil.getInstance().toJson(customs)
                write(menusJson)
                flush()
                close()
            }

            it.onNext(200)
            it.onCompleted()
        }
    }

    override fun getCategories() = mutableListOf<Category>().apply {
        add(quickCategory)
        add(basicCategory)
    }

    private fun defaultShortCut(): List<ShortCut> {
        val sc = allShortCuts()
        if (sc.size < 4) {
            return sc
        }
        return mutableListOf<ShortCut>().apply {
            add(sc[0])
            add(sc[1])
            add(sc[2])
            add(sc[3])
        }
    }

    private fun allShortCuts() = mutableListOf<ShortCut>().apply {
        if (hasPatch(Patches.PATCH_HUANG_XIN)) {
            add(ShortCut().apply {
                id = X.Quick.Hyphenate
                name = "聊天"
            })
        }
        if (hasModule(X.Func.Plan)) {
            add(ShortCut().apply {
                id = X.Quick.NewPlan
                name = "计划"
            })
        }
        if (hasModule(X.Func.Approval)) {
            add(ShortCut().apply {
                id = X.Quick.NewCollaboration
                name = "协同"
            })
        }
        if (hasModule(X.Func.Location)) {
            add(ShortCut().apply {
                id = X.Quick.Location
                name = "签到"
            })
        }
        if (hasModule(X.Func.NewForm)) {
            add(ShortCut().apply {
                id = X.Quick.NewForm
                name = "流程"
                url = getModule(X.Func.NewForm)?.url
            })
        }
        if (hasModule(X.Func.Schedule)) {
            add(ShortCut().apply {
                id = X.Quick.NewSchedule
                name = "日程"
            })
        }
        if (hasModule(X.Func.Mail)) {
            add(ShortCut().apply {
                id = X.Quick.NewMail
                name = "邮件"
            })
        }
    }

    override fun emptyData() {
        super.emptyData()
        customMenuMap?.clear()
    }
}