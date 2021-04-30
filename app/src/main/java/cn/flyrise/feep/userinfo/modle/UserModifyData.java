package cn.flyrise.feep.userinfo.modle;

/**
 * 保存修改后的用户数据
 * Created by Administrator on 2017-5-8.
 */

public class UserModifyData {

    private final static String EDIT_SELF_FOR_MOBILE = "editSelfForMobile";

    private final static String USER_LOGIC = "userLogic";

    private final static String EDIT_COUNT = "1";

    private static UserModifyData userModifyData;

    private UserInfoModifyBean mModifyBean;

    public static UserModifyData getInstance() {
        if (userModifyData == null) {
            userModifyData = new UserModifyData();
        }
        return userModifyData;
    }

    public UserInfoModifyBean getModifyBean() {
        if (mModifyBean == null) {
            mModifyBean = new UserInfoModifyBean();
        }
        return mModifyBean;
    }

    public void setModifyBean(UserInfoModifyBean modifyBean) {
        this.mModifyBean = modifyBean;
    }

    public static UserModifyDetailRequest getDetailRequest() {
        UserModifyData modifyData = UserModifyData.getInstance();
        if (modifyData == null) {
            return new UserModifyDetailRequest();
        }
        UserInfoModifyBean modifyBean = modifyData.getModifyBean();

        UserModifyDetailRequest datailRequest = new UserModifyDetailRequest();
        datailRequest.setCount(EDIT_COUNT);
        datailRequest.setMethod(EDIT_SELF_FOR_MOBILE);
        datailRequest.setObj(USER_LOGIC);
        datailRequest.setModifyDetail(modifyBean);
        return datailRequest;
    }
}
