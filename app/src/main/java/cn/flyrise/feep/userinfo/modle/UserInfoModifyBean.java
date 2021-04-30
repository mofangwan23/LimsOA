package cn.flyrise.feep.userinfo.modle;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017-5-8.
 */

public class UserInfoModifyBean {

    @SerializedName("su28")
    private String icon;

    @SerializedName("su16")
    private String sex;

    @SerializedName("BIRTHDAY-")
    private String birthday;

    @SerializedName("YEAR_BIRTHDAY-")
    private String yearBirthday;

    @SerializedName("su12")
    private String tel;

    @SerializedName("su13")
    private String insideTel;

    @SerializedName("su09")
    private String workTel;

    @SerializedName("su10")
    private String phone;

    @SerializedName("su11")
    private String email;

    @SerializedName("su18")
    private String location;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getYearBirthday() {
        return yearBirthday;
    }

    public void setYearBirthday(String yearBirthday) {
        this.yearBirthday = yearBirthday;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getInsideTel() {
        return insideTel;
    }

    public void setInsideTel(String contentTel) {
        this.insideTel = contentTel;
    }

    public String getWorkTel() {
        return workTel;
    }

    public void setWorkTel(String wordTel) {
        this.workTel = wordTel;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
