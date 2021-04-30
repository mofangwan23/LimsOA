package cn.flyrise.feep.addressbook.source;

import java.util.List;

import cn.flyrise.feep.addressbook.model.ContactInfo;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.Position;
import cn.flyrise.feep.core.services.model.AddressBook;
import rx.Observable;

/**
 * @author ZYP
 * @since 2017-01-09 14:51
 */
public interface IAddressBookDataSource {

    /**
     * 常用联系人
     */
    int TYPE_COMMON_USERS = 1;

    /**
     * 我的关注
     */
    int TYPE_MIME_ATTENTION = 2;

    /**
     * 我的下属
     */
    int TYPE_MIME_SUBORDINATES = 3;

    /**
     * 查询所有的用户基本信息
     */
    List<AddressBook> obtainAllAddressBooks();

    Department obtainHeadCompany();

    /**
     * 查询当前集团下所有子公司~
     */
    List<Department> obtainAllSubCompany();

    /**
     * 查询指定用户的兼职部门
     */
    List<Department> obtainPartTimeDepartment(String userId);

    /**
     * 查询指定用户所在的部门 id
     *
     * @param userId 用户 id
     */
    List<String> obtainDepartmentIdsWhereUserIn(String userId);

    /**
     * 根据指定的部门 id 查找部门详细信息
     *
     * @param deptId 部门 id
     */
    Department obtainDepartmentByDeptId(String deptId);

    /**
     * 查询当前用户所在的部门信息
     *
     * @param userId 用户 id
     */
    Department obtainDepartmentWhereUserIn(String userId);

    /**
     * 根据父部门的 id 查询其下子部门，不含子部门的子部门。
     *
     * @param parentDepartmentId 父部门 id
     */
    List<Department> obtainSubDepartments(String parentDepartmentId);

    /**
     * 查找指定部门下的所有子部门(id)，不包含子部门的子部门
     *
     * @param deptId 当前部门 id
     */
    List<String> obtainSubDepartmentIds(String deptId);

    /**
     * 根据条件查询联系人信息
     *
     * @param deptIds  部门 id，可以多个
     * @param position 岗位名称，可以没有
     */
    List<AddressBook> obtainStaff(List<String> deptIds, String position);

    /**
     * 查询用户，根据用户的 sortNo 排序，董事长位于前面
     */
    List<AddressBook> obtainStaffBySortNo(List<String> deptIds);

    /**
     * 联系人查找页面，根据用户数据的用户名，查找匹配的总数。
     *
     * @param nameLike 模糊查询的名字
     */
    int obtainContactCountByNameLike(String nameLike);

    /**
     * 模糊搜索联系人信息
     *
     * @param nameLike 联系人名字
     * @param offset   偏移量
     */
    List<AddressBook> obtainContactByNameLike(String nameLike, int offset);

    /**
     * 获取指定部门下的所有岗位
     *
     * @param deptIds 指定部门 ids
     */
    List<Position> obtainPositionsInDepartment(List<String> deptIds);

    /**
     * 查询用户的岗位
     *
     * @param userId 用户 id
     */
    Position obtainPositionWhichUserIs(String userId);

    /**
     * 根据指定的 userIds 查询用户信息
     *
     * @param userIds 指定的用户 id
     */
    List<AddressBook> obtainUserByIds(List<String> userIds);

    /**
     * 根据类型获取相关联系人信息
     *
     * @param type 类型，有两个值: 常用联系人 TYPE_COMMON_USERS; 我的关注 TYPE_MINE_ATTENTION
     */
    Observable<List<AddressBook>> obtainUserByType(int type);

    /**
     * 根据类型获取下属联系人信息
     */
    Observable<List<AddressBook>> obtainSubordinates();

    /**
     * 查询用户详细信息
     *
     * @param userId 用户 id
     */
    Observable<ContactInfo> obtainUserDetailInfo(String userId);

    /**
     * 查询用户基本信息（用于查询兼职部门)
     *
     * @param userId 用户 id
     * @param depId 部门 depId
     */
    Observable<ContactInfo> obtainUserDetailInfo(String userId,String depId);

    /**
     * 查询用户基本信息
     *
     * @param userId 用户 id
     */
    AddressBook obtainUserBaseInfo(String userId);

    /**
     * 更新用户头像
     *
     * @param userImageHref 用户头像
     */
    void updateUserImageHref(String userId, String userImageHref);

}
