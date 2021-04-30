package cn.flyrise.feep.addressbook.model;

import android.support.annotation.Keep;

import java.io.Serializable;
import java.util.List;

@Keep
public class AddressTreeDepartmentBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    private String id;

    private String name;

    private String unitcode;
    private String level;
    private List<AddressTreeDepartmentBean> subNodes;
    private List<AddressBookVO> users;

    private String fatherId;

    public AddressTreeDepartmentBean(int id, int fatherId, String name) {
        this.id = String.valueOf(id);
        this.fatherId = String.valueOf(fatherId);
        this.name = name;
    }

    public AddressTreeDepartmentBean(String id, String fatherId, String name) {
        this.id = id;
        this.fatherId = fatherId;
        this.name = name;
    }

    public List<AddressBookVO> getUsers() {
        return users;
    }

    public String getFatherId() {
        return fatherId;
    }

    public List<AddressTreeDepartmentBean> getSubNodes() {
        return subNodes;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnitcode() {
        return unitcode;
    }

    public String getLevel() {
        return level;
    }

}
