package cn.flyrise.android.shared.bean;

import java.io.Serializable;

public class AddressBookBean implements Serializable {
    private static final long serialVersionUID = -649762591852710910L;
    private String id;
    private String name;
    private String departmentName;
    private String imageHref;
    private String position;
    private String tel;
    private String phone;
    private String email;
    private String charType;
    private Boolean isChar;
    private String py;

    public String getPy () {
        return py;
    }

    public void setPy (String py) {
        this.py = py;
    }

    public Boolean getIsChar () {
        return isChar;
    }

    public void setIsChar (Boolean isChar) {
        this.isChar = isChar;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getDepartmentName () {
        return departmentName;
    }

    public void setDepartmentName (String departmentName) {
        this.departmentName = departmentName;
    }

    public String getImageHref () {
        return imageHref;
    }

    public void setImageHref (String imageHref) {
        this.imageHref = imageHref;
    }

    public String getPosition () {
        return position;
    }

    public void setPosition (String position) {
        this.position = position;
    }

    public String getTel () {
        return tel;
    }

    public void setTel (String tel) {
        this.tel = tel;
    }

    public String getPhone () {
        return phone;
    }

    public void setPhone (String phone) {
        this.phone = phone;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public String getCharType () {
        return charType;
    }

    public void setCharType (String charType) {
        this.charType = charType;
    }

    @Override
    public String toString () {
        return "id:" + getId () + "name:" + getName () + "departmentName:" + getDepartmentName () + "imagehref" + getImageHref () + "tel" + getTel () + "phone" + getPhone () + "commonGroup" + getPosition () + "email" + getEmail () + "chartype" + getCharType ();
    }
}
