package cn.flyrise.feep.userinfo.modle;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * 用户密码修改
 * Created by Administrator on 2017-5-8.
 */

@Keep
public class UserModifyPasswordRequest extends UserModifyRequest {

    @SerializedName("param1")
    private String password;

    @SerializedName("param2")
    private String newPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
