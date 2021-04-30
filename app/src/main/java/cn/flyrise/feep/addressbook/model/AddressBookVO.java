package cn.flyrise.feep.addressbook.model;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * 项目名称：Fe-Pt5.1 类名称：AddressBookVO 类描述： 创建人：Yang 创建时间：2014-9-23 上午9:24:39 修改人：Yang 修改时间：2014-9-23 上午9:24:39 修改备注：
 *
 * @version 1.0
 */
@Keep
public class AddressBookVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String departmentName;
    private String imageHref;
    private String position;
    private String tel;
    private String phone;
    private String email;
    private String charType;
    private String py;
    private String deptPY;
    private boolean IsChar;
    private String address;
    private String phone1;
    private String phone2;
    private String pinyin;
    private String sex;
    private String brithday;
    private String imid;

    public AddressBookVO() {
    }

    public AddressBookVO(String imageHref, String name, String id) {
        this.imageHref = imageHref;
        this.name = name;
        this.id = id;
    }

    public boolean getIsChar() {
        return IsChar;
    }

    public void setIsChar(boolean isChar) {
        IsChar = isChar;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getDeptPY() {
        return deptPY;
    }

    public void setDeptPY(String deptPY) {
        this.deptPY = deptPY;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getImageHref() {
        return imageHref;
    }

    public void setImageHref(String imageHref) {
        this.imageHref = imageHref;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCharType() {
        return charType;
    }

    public void setCharType(String charType) {
        this.charType = charType;
    }

    public String getPy() {
        return py;
    }

    public void setPy(String py) {
        this.py = py;
    }

    @Override
    public String toString() {
        return "id:" + getId() + "name:" + getName() + "departmentName:" + getDepartmentName() + "--commonGroup" + getPosition() + "--email" + getEmail() + "--ischar" + getIsChar() + "--py:" + getPy()+"--im:"+getImid();
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBrithday() {
        return brithday;
    }

    public void setBrithday(String brithday) {
        this.brithday = brithday;
    }

    public String getImid() {
        return imid;
    }

    public void setImid(String imid) {
        this.imid = imid;
    }


}
