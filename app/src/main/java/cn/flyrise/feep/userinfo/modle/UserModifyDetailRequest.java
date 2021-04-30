package cn.flyrise.feep.userinfo.modle;

import com.google.gson.annotations.SerializedName;

/**
 * 用户详情修改
 * Created by Administrator on 2017-5-8.
 */

public class UserModifyDetailRequest extends UserModifyRequest{

    @SerializedName("param1")
    private UserInfoModifyBean modifyDetail;

    public UserInfoModifyBean getModifyDetail() {
        return modifyDetail;
    }

    public void setModifyDetail(UserInfoModifyBean modifyDetail) {
        this.modifyDetail = modifyDetail;
    }
}
