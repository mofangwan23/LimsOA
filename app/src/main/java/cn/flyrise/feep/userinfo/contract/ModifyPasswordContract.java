package cn.flyrise.feep.userinfo.contract;

/**
 * Created by Administrator on 2017-4-26.
 */

public interface ModifyPasswordContract {

    interface presenter {

        String REGEX_PASSWORD = "^[\\w_]{6,16}";

        String EDIT_PASSWORD_FOR_MOBILE = "editPasswordForMobile";

        String USER_LOGIC = "userLogic";

        String PASSWORD_COUNT = "2";

        String SUCCESS_COUNT = "0";

        void successModifyPassword(String password, String mText);

    }

    interface View {

        void finishModify();

        void showLoading();

        void hideLoading();

        void inputError(String errorText);
    }


}
