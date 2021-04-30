package cn.flyrise.feep.addressbook;

import java.util.List;

import cn.flyrise.feep.core.services.model.AddressBook;

/**
 * @author ZYP
 * @since 2016-12-12 14:00
 */
public interface ContactSearchContract {

    interface IView {

        void showContacts(List<AddressBook> contacts);

        void addContacts(List<AddressBook> contacts);

        void addFooterView();

        void removeFooterView();

    }

    interface IPresenter {

        void executeQuery(String nameLike);

        void loadMoreContact(String nameLike);

    }

}
