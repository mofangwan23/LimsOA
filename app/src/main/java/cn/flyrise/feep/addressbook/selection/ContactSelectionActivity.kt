package cn.flyrise.feep.addressbook.selection

import android.os.Bundle
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.selection.presenter.SelectionPresenter
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.services.model.AddressBook

/**
 * @author ZYP
 * @since 2018-06-07 10:36
 * 7.0 版本新增的选人界面：支持单选、多选、搜索、拼音排序。
 *
 * Intent 支持的请求参数共三个：标题、模式、数据源
 * <ul>
 *
 * <li>标题：String
 * <p>
 *     intent.putExtra(SelectionContractKt.TITLE, "标题")
 * </p>
 * </li>
 *
 * <li>模式：Int
 * <p>
 *     intent.putExtra(SelectionContractKt.SELECTION_MODE, mode)
 *
 *     SelectionContractKt.SELECTION_SINGLE: 单选
 *     SelectionContractKt.SELECTION_MULTI: 多选
 *     SelectionContractKt.SELECTION_FINISH ：选完人员是否关闭，默认关闭界面
 * </p>
 * </li>
 *
 * <li>数据源：Int
 * <p>
 *     intent.putExtra(SelectionContractKt.DATASOURCE, dataSource)

 *     SelectionContractKt.DATASOURCE_LEADER_POINT：签到模块-领导选人
 *     SelectionContractKt.DATASOURCE_WORK_PLAN_RELATED：计划模块-关联员工
 * </p>
 * </li>
 * </ul>
 *
 * 返回的选择结果共有两个：userIds，userNames
 * SelectionContractKt.CONTACT_IDS：用户IDS，字符串类型，如果有多个，id之间使用 "," 隔开
 * SelectionContractKt.CONTACT_NAMES：用户名，字符串类型。该值仅在单选模式下有效，多选模式下，该值为空。
 * SelectionContractKt.CONTACT_DEFAULT：默认选中的人员
 */
class ContactSelectionActivity : BaseActivity() {

    private var selectionSearchPage: ContactSelectionSearchPage? = null
    private lateinit var selectionPage: ContactSelectionPage
    private var presenter: SelectionPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_selection)
    }

    override fun bindView() {
        selectionPage = newContactSelectionPage(intent)

        val dataSource = intent.getIntExtra(DATASOURCE, DATASOURCE_LEADER_POINT)
        presenter = newSelectionPresenter(dataSource)!!

        if (presenter == null) {
            throw RuntimeException("Error datasource {${dataSource}}, please delivery a correct datasource.")
        }

        selectionPage.presenter = presenter!!

        supportFragmentManager.beginTransaction()
                .add(R.id.layoutFragmentContainer, selectionPage)
                .show(selectionPage)
                .commit()
    }

    override fun bindData() {
        super.bindData()
        if (intent != null) selectionPage.setSelectedAddress(intent.getStringArrayListExtra(CONTACT_DEFAULT))
    }

    fun goToSeachPage() {
        if (selectionSearchPage == null) {
            selectionSearchPage = newContactSearchPage(presenter!!)
        }

        supportFragmentManager.beginTransaction()
                .add(R.id.layoutFragmentContainer, selectionSearchPage!!)
                .show(selectionSearchPage!!)
                .commit()
    }

    // 确认选择结果
    fun confirmSearchResult(addressBook: AddressBook) {
        if (selectionPage != null) {
            selectionPage.scrollToSelectedContact(addressBook)
        }

        if (selectionSearchPage == null) return
        supportFragmentManager.beginTransaction()
                .remove(selectionSearchPage!!)
                .commit()
    }

    override fun onBackPressed() {
        if (selectionSearchPage != null && selectionSearchPage!!.isVisible) {
            supportFragmentManager.beginTransaction()
                    .remove(selectionSearchPage!!)
                    .commit()
            return
        }
        super.onBackPressed()
    }
}






