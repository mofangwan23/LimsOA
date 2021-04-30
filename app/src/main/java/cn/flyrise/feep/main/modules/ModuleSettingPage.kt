package cn.flyrise.feep.main.modules

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.core.function.AppMenu
import cn.flyrise.feep.core.function.Category
import kotlinx.android.synthetic.main.fragment_module_setting.*

/**
 * @author 社会主义接班人
 * @since 2018-08-01 17:10
 */
class ModuleSettingPage : Fragment() {

    private lateinit var category: Category
    private var displayMenus: MutableList<AppMenu>? = null
    private var unDisplayMenus: MutableList<AppMenu>? = null
    private lateinit var displayAdapter: ModuleSettingDragAdapter
    private lateinit var unDisplayAdapter: ModuleSettingAdapter

    private var toast: Toast? = null

    companion object {
        fun fucking(c: Category, d: MutableList<AppMenu>?, ud: MutableList<AppMenu>?): ModuleSettingPage {
            return ModuleSettingPage().apply {
                category = c
                displayMenus = d
                unDisplayMenus = ud
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_module_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 3.上半部分的 GridView
        val updateState = {
            if (TextUtils.equals(category.key, "10086") && displayAdapter?.shortCutCounts < 4) {
                unDisplayAdapter.enableAddShortCuts(true)
            }
        }

        displayAdapter = ModuleSettingDragAdapter(activity, null).apply {
            setCategory(category)
            setModules(displayMenus)
            setOnModuleDeleteListener {
                unDisplayAdapter.appendModule(it)
                removeModule(it)
                setUnDisplayGridViewVisibily(unDisplayAdapter.count <= 0)
                setDisplayGridViewVisibily(displayAdapter.count <= 0)
                updateState()
            }
        }

        displayGridView.apply {
            adapter = displayAdapter
            setOnItemLongClickListener { _, _, _, _ ->
                (activity as ModuleSettingActivity).setViewPagerScroll(false)
                requestDisallowInterceptTouchEvent(true)
                displayGridView.isMove = true
                false
            }
            setOnDragCompletedListener {
                (activity as ModuleSettingActivity).setViewPagerScroll(true)
                requestDisallowInterceptTouchEvent(false)
            }
        }

        // 4.下半部分的 GridView
        unDisplayAdapter = ModuleSettingAdapter(null).apply {
            setCategory(category)
            setModules(unDisplayMenus)
            setOnModuleAddListener {
                if (TextUtils.equals(category.key, "10086")
                        && displayAdapter.shortCutCounts == 4) {
                    showToast()
                    return@setOnModuleAddListener
                }
                displayAdapter.appendModule(it) // 如果是快捷入口，是否已经四个了?
                if (TextUtils.equals(category.key, "10086") && displayAdapter.shortCutCounts == 4) {
                    enableAddShortCuts(false)
                }
                removeModule(it)
                setUnDisplayGridViewVisibily(unDisplayAdapter.count == 0)
                setDisplayGridViewVisibily(displayAdapter.count == 0)
            }
            setUnDisplayGridViewVisibily(unDisplayMenus?.size ?: 0 == 0)
            setDisplayGridViewVisibily(displayMenus?.size ?: 0 == 0)
        }
        unDisplayGridView.adapter = unDisplayAdapter
        updateState()
    }

    private fun setUnDisplayGridViewVisibily(isHide: Boolean) {
        unDisplayGridView.visibility = if (isHide) View.GONE else View.VISIBLE
        unDisplayHind.visibility = if (isHide) View.VISIBLE else View.GONE
    }

    private fun setDisplayGridViewVisibily(isHide: Boolean) {
        displayGridView.visibility = if (isHide) View.GONE else View.VISIBLE
        displayHind.visibility = if (isHide) View.VISIBLE else View.GONE
    }

    fun getDisplayMenus(): List<AppMenu> {
        if (TextUtils.equals(category.key, "10086")) {
            return displayAdapter.editedModules.filter { it.menuId != AppMenu.ID_EMPTY_SHORT_CUT }
        }
        return displayAdapter.editedModules
    }

    fun getCategory() = category

    private fun updateStyleAndHeight() {
        val contentHeight = layoutContent.getMeasuredHeight()
        val maxTopHeight = contentHeight / 2 - PixelUtil.dipToPx(37.0f)  // 一半的高度
        var gridViewHeight = getDragGridViewHeight()                           // DragGridView 的高度

        if (gridViewHeight > maxTopHeight) {
            gridViewHeight = maxTopHeight
        }

        val params = displayGridView.getLayoutParams()
        params.height = gridViewHeight
        displayGridView.setLayoutParams(params)
    }

    private fun getDragGridViewHeight(): Int {
        val numColumns = 4
        var totalHeight = 0

        var i = 0
        while (i < displayAdapter.getCount()) {                                 // 计算每一列的高度之和
            val listItem = displayAdapter.getView(i, null, displayGridView)     // 获取gridview的每一个item
            listItem.measure(0, 0)
            totalHeight += listItem.getMeasuredHeight()                         // 获取item的高度和
            i += numColumns
        }

        var rows = displayAdapter.getCount() / numColumns - 1
        if (displayAdapter.getCount() % numColumns != 0) {
            rows += 1
        }

        totalHeight += rows * PixelUtil.dipToPx(8f)
        return totalHeight
    }

    private fun showToast() {
        if (toast == null) {
            toast = Toast(activity).apply {
                duration = Toast.LENGTH_SHORT
                view = LayoutInflater.from(activity).inflate(R.layout.core_view_toast, null)
            }
        }

        toast?.view?.apply {
            findViewById<TextView>(R.id.toast_hint).setText(getString(R.string.module_setting_modify_quick))
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View?) {
                    (activity as? ModuleSettingActivity)?.setCompleteButtonEnable(true)
                }

                override fun onViewAttachedToWindow(v: View?) {
                    (activity as? ModuleSettingActivity)?.setCompleteButtonEnable(false)
                }
            })
        }
        toast?.show()
    }

}