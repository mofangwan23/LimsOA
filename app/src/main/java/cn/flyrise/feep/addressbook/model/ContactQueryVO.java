package cn.flyrise.feep.addressbook.model;

import java.util.List;

import cn.flyrise.feep.core.services.model.AddressBook;

/**
 * @author ZYP
 * @since 2016-12-12 10:03
 */
public class ContactQueryVO {

    public int totalCount;
    public int totalPage;
    public List<AddressBook> contacts;

}
