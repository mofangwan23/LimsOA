package cn.flyrise.feep.addressbook.model

import cn.flyrise.feep.core.services.model.AddressBook

/**
 * @author ZYP
 * @since 2018-06-08 17:11
 */
data class AddressBooks(
        val deptIds: MutableList<String?>?,
        val userIds: MutableList<String?>,
        val addressBooks: MutableSet<AddressBook?>?,
        val deptUsers: MutableSet<AddressBook?>?)