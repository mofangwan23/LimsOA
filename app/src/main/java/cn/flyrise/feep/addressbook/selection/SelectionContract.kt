package cn.flyrise.feep.addressbook.selection

import android.content.Intent
import cn.flyrise.feep.addressbook.selection.presenter.LeaderPointPresenter
import cn.flyrise.feep.addressbook.selection.presenter.SelectionPresenter
import cn.flyrise.feep.addressbook.selection.presenter.SubordinatesPresenter
import cn.flyrise.feep.addressbook.selection.presenter.WorkPlanRelatedPresenter
import cn.flyrise.feep.core.services.model.AddressBook

/**
 * @author ZYP
 * @since 2018-06-07 13:56
 */

const val TITLE = "title"                       // 标题
const val DATASOURCE = "dataSource"             // 数据源
const val DATASOURCE_LEADER_POINT = 1           // 数据源：领导选人
const val DATASOURCE_WORK_PLAN_RELATED = 2      // 数据源：计划关联

const val SELECTION_MODE = "selectionMode"      // 选择模式：单选、多选
const val SELECTION_SINGLE = 1                  // 单选
const val SELECTION_MULTI = 2                   // 多选
const val SELECTION_FINISH = "selectionFinish" //选完人员是否关闭，默认关闭

const val CONTACT_IDS = "userIds"               // 用户ID，如果有多个，使用 "," 隔开
const val CONTACT_NAMES = "userNames"           // 用户命，如果有多个，同上

const val CONTACT_DEFAULT = "contact_default" //默认选中的人员 list<userId>

const val IS_SHOW_SEARCH = "is_show_search" //是否隐藏搜索框,默认显示


/**
 * 根据 Intent 传入的 dataSource，创建合适的 Presenter.
 */
fun newSelectionPresenter(dataSource: Int): SelectionPresenter? {
    return when (dataSource) {
        DATASOURCE_LEADER_POINT -> LeaderPointPresenter()
        DATASOURCE_WORK_PLAN_RELATED -> WorkPlanRelatedPresenter()
        else -> null
    }
}

//创建联系人下属页面
fun newSubordinatesPresenter(dataSource: Int): SelectionPresenter? {
    return when (dataSource) {
        DATASOURCE_LEADER_POINT -> SubordinatesPresenter()
        else -> null
    }
}

// 创建联系人选择界面
fun newContactSelectionPage(intent: Intent): ContactSelectionPage {
    val page = ContactSelectionPage()
    page.intent = intent
    return page
}

// 创建联系人搜索界面
fun newContactSearchPage(presenter: SelectionPresenter): ContactSelectionSearchPage {
    val page = ContactSelectionSearchPage()
    page.presenter = presenter
    return page
}


interface ContactSelectionView {

    fun showContacts(addressBooks: List<AddressBook?>?,deptUser: List<AddressBook?>?)

    fun showLoading()

    fun hideLoading()

}

interface ContactSelectionPresenter {

    fun start()

    fun search(keyword: String)

}