package cn.flyrise.feep.userinfo.contract;

import android.content.Intent;

import java.util.List;

import cn.flyrise.feep.K;
import cn.flyrise.feep.addressbook.model.ContactInfo;
import cn.flyrise.feep.userinfo.modle.UserInfoDetailItem;

/**
 * Created by Administrator on 2017-4-28.
 */

public interface UserInfoContract {

    interface Presenter {

	    //因为女人对此信息敏感,所以隐藏, K.userInfo.DETAIL_BIRTHDAY
	    int[] types = {K.userInfo.DETAIL_ICON, K.userInfo.DETIAL_DEPERTMENT, K.userInfo.DETAIL_PHONE
			    , K.userInfo.DETAIL_TEL, K.userInfo.DETAIL_EMAIL
			    , K.userInfo.DETAIL_LOCATION};

        void initData();

        void initModifyBean(ContactInfo addressBook);

        void modifyView(Intent data);

        void completeAttachment(String path);
    }

    interface View {

        int MODIFY_TEXT = 1661;

        void setAdapter(List<UserInfoDetailItem> lists);

    }

}
