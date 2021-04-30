package cn.flyrise.feep.addressbook.model;

import android.support.annotation.Keep;

import java.io.Serializable;
import java.util.List;
@Keep
public class ThreeContactBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<AddressTreeDepartmentBean> orgs;                  // 全部联系人数据
    private List<AddressTreeDepartmentBean> dept;                  // 部门
    private List<AddressBookVO> persons;               // 人员
    private List<AddressBookVO> tags;                  // 关注
    private List<AddressBookVO> commons;               // 常用联系人

    private String allVersion;
    private String personsVersion;

    public String getAllVersion() {
        return allVersion;
    }

    public void setAllVersion(String allVersion) {
        this.allVersion = allVersion;
    }

    public String getPersonsVersion() {
        return personsVersion;
    }

    public void setPersonsVersion(String personsVersion) {
        this.personsVersion = personsVersion;
    }

    public List<AddressTreeDepartmentBean> getDept() {
        return dept;
    }

    public void setDept(List<AddressTreeDepartmentBean> dept) {
        this.dept = dept;
    }

    public List<AddressBookVO> getPersons() {
        return persons;
    }

    public void setPersons(List<AddressBookVO> persons) {
        this.persons = persons;
    }

    public List<AddressBookVO> getTags() {
        return tags;
    }

    public void setTags(List<AddressBookVO> tags) {
        this.tags = tags;
    }

    public List<AddressBookVO> getCommons() {
        return commons;
    }

    public void setCommons(List<AddressBookVO> commons) {
        this.commons = commons;
    }

    public List<AddressTreeDepartmentBean> getOrgs() {
        return orgs;
    }

    public void setOrgs(List<AddressTreeDepartmentBean> orgs) {
        this.orgs = orgs;
    }

    @Override
    public String toString() {
        return "tags:" + getTags() + "commons:" + getCommons() + "--mAddressTreeDepartmentBean:" + orgs.toString();
    }

}
