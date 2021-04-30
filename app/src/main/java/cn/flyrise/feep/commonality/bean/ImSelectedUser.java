package cn.flyrise.feep.commonality.bean;

import android.support.annotation.Keep;

/**
 * Created by Administrator on 2016-6-28.
 */
@Keep
public class ImSelectedUser {
    public String userId;
    public String imId;
    public String userName;
    public String department;

    public void setImSelectedUser(String userId, String imId, String userName, String department) {
        this.userId = userId;
        this.imId = imId;
        this.userName = userName;
        this.department = department;
    }
}
