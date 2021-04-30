package cn.flyrise.feep.addressbook.selection.presenter

import cn.flyrise.feep.addressbook.selection.ContactSelectionPresenter
import cn.flyrise.feep.addressbook.selection.ContactSelectionView
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.services.model.AddressBook

/**
 * @author ZYP
 * @since 2018-06-07 16:23
 */
abstract class SelectionPresenter() : ContactSelectionPresenter {

    protected var contacts: List<AddressBook?>? = null
    lateinit var selectionView: ContactSelectionView

    override fun search(keyword: String) {
        if (CommonUtil.isEmptyList(contacts)) return
        selectionView.showContacts(contacts?.filter { it!!.name.contains(keyword) },null)
    }

}