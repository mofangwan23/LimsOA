package cn.flyrise.feep.addressbook;

import android.content.Intent;

import cn.flyrise.android.protocol.model.CommonGroup;
import java.util.List;

import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.Position;

/**
 * @author ZYP
 * @since 2016-12-12 13:05
 */
public interface AddressBookContract {

	interface IView {

		/**
		 * 设置初始状态
		 * @param company 当前用户所在的单位公司
		 * @param department 当前用户所在的一级部门
		 * @param subDepartment 当前用户所在的二级部门
		 * @param position 当前用户的具体岗位
		 */
		void showInitialization(Department company, Department department,
				Department subDepartment, Position position, boolean isOnlyOneCompany);

		/**
		 * 显示联系人数据，已经排好序了
		 * @param addressBooks 联系人列表
		 */
		void showContacts(List<AddressBook> addressBooks);

		void showLoading();

		void hideLoading();

		void showCommonGroups(List<CommonGroup> commonGroups);

		void showCommonUsers(List<AddressBook> addressBooks);

		void noneCommonGroups();
	}

	interface IPresenter {

		/**
		 * 查询常用组
		 */
		void queryCommonlyGroup(String userId);

		void queryUserDepartmentInfos(String userId, Intent intent);

		void queryContacts(Department company, Department department, Department subDepartment,
				Position position);

		void queryCommonlyUserByGroupId(String groupId);
	}
}
