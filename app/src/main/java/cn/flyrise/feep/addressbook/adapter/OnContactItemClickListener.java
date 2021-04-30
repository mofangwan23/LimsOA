package cn.flyrise.feep.addressbook.adapter;

import cn.flyrise.feep.core.services.model.AddressBook;

/**
 * @author ZYP
 * @since 2016-12-13 11:27
 */
public interface OnContactItemClickListener {

    void onItemClick(AddressBook addressBook, int position);

}
