package cn.flyrise.feep.core.services;

import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2017-02-25 13:30
 * 为子模块准备的联系人信息查询接口
 * 根据给定的 userId 返回查询到的用户信息，{@link AddressBook}。
 */
public interface IAddressBookServices {

	AddressBook queryUserInfo(String userId);

	List<AddressBook> queryUserIds(List<String> userIds);

	String getActualUserId(String userId);

	List<AddressBook> queryContactName(String userName, int offset);//名字查找联系人

	List<AddressBook> queryAllAddressBooks();//查询全部联系人

	Observable<AddressBook> queryUserDetail(String userId);//查找用户详情，包含离职人员

}
