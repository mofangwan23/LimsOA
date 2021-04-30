package cn.flyrise.feep.userinfo.contract;

import android.text.InputFilter;
import android.widget.EditText;

/**
 * Created by Administrator on 2017-4-26.
 */

public interface ModifyContract {

    interface presenter {

        String SUCCESS_COUNT = "0";

        String PHONE_LINE = "-";

        String REGEX_CHINA_MOBILE ="1(3[4-9]|4[7]|5[012789]|8[278])\\d{8}";

        String REGEX_CHINA_UNICOM = "1(3[0-2]|5[56]|8[56])\\d{8}";

        String REGEX_CHINA_TELECOM = "(?!00|015|013)(0\\d{9,11})|(1(33|53|80|89)\\d{8})";

        String PHONE_REGEX = "^1[3456789]\\d{9}$";//验证手机号码

        String WORK_TEL_REGEX = "(0[12][01234578]|0\\d{3})*-*\\d{7,8}";//验证办公号码

        String ADDRESS_TYPE = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$";

        String EMAIL_TYPE= "^[a-zA-Z0-9-@_.]+$";//邮箱字符

        int addressMaxNums = 75;

        void getTextToPhone(CharSequence s, int start, int before);

        void setTextToPhone(String phone);

        String getPhoneToText(String phone);

        void successModifyText(String mText);

        boolean getPhoneType();

        void setBeforeText(int type, String beforeText);

        boolean regexText(int type, String beforeText);

        InputFilter[] addressFileter();

        int getCharacterNum(String text);
    }

    interface View {

        int getType();

        EditText getContextEt();

        boolean isSubmitText();

        void showLoading();

        void hideLoading();

        void successModify();
    }


}
