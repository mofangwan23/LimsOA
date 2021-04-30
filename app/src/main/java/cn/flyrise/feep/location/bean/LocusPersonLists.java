package cn.flyrise.feep.location.bean;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * 类描述：考勤轨迹人员表
 * @author 罗展健
 * @version 1.0
 */
@Keep
public class LocusPersonLists implements Serializable {

    private static final long serialVersionUID = 40L;
    private String userId;
    private String userName;
    private String userImageHref;          // 用户头像
    private String userPost;               // 用户岗位
    private String isChar;                 // 显示数据拼音的首字母

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImageHref() {
        return userImageHref;
    }

    public void setUserImageHref(String userImageHref) {
        this.userImageHref = userImageHref;
    }

    public String getUserPost() {
        return userPost;
    }

    public void setUserPost(String userPost) {
        this.userPost = userPost;
    }

    public String getIsChar() {
        return isChar;
    }

    public void setIsChar(String isChar) {
        this.isChar = isChar;
    }

}
