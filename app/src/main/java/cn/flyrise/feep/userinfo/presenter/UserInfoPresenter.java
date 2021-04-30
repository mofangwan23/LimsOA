package cn.flyrise.feep.userinfo.presenter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.UserDetailsRequest;
import cn.flyrise.android.protocol.entity.UserDetailsResponse;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.model.AddressBookVO;
import cn.flyrise.feep.addressbook.model.ContactInfo;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.userinfo.contract.UserInfoContract;
import cn.flyrise.feep.userinfo.modle.UserInfoDetailItem;
import cn.flyrise.feep.userinfo.modle.UserInfoModifyBean;
import cn.flyrise.feep.userinfo.modle.UserModifyData;
import cn.flyrise.feep.userinfo.views.UserInfoActivity;

/**
 * Created by Administrator on 2017-4-28.
 */

public class UserInfoPresenter implements UserInfoContract.Presenter {

    private Context mContext;

    private UserInfoContract.View mView;

    private List<UserInfoDetailItem> listBeans;

    public UserInfoPresenter(Context context) {
        mView = (UserInfoActivity) context;
        mContext = context;
    }

    public UserInfoPresenter(Context context, Fragment fragment) {
        mView = (UserInfoContract.View) fragment;
        mContext = context;
    }

    @Override
    public void modifyView(Intent data) {
        int type = data.getIntExtra("MODIFY_TYPE", -1);
        String modifyContent = data.getStringExtra("MODIFY_CONTENT");
        if (type == -1) {
            return;
        }
        if (listBeans == null) {
            return;
        }

        List<UserInfoDetailItem> detailBeans = new ArrayList<>();
        for (UserInfoDetailItem detailBean : listBeans) {
            if (detailBean.itemType == type) {
                detailBean.content = modifyContent;
            }
            detailBeans.add(detailBean);
        }
        mView.setAdapter(detailBeans);
    }

    @Override
    public void initModifyBean(ContactInfo addressBook) {
        if (addressBook == null) {
            return;
        }
        UserModifyData modifyData = UserModifyData.getInstance();
        if (modifyData == null) {
            return;
        }
        UserInfoModifyBean modifyBean = modifyData.getModifyBean();
        modifyBean.setEmail(addressBook.email);
        modifyBean.setWorkTel(addressBook.tel);
        modifyBean.setPhone(addressBook.phone);
        modifyBean.setLocation(addressBook.address);
        modifyBean.setBirthday(addressBook.brithday);
        modifyData.setModifyBean(modifyBean);
    }

    @Override
    public void initData() {
        LoadingHint.show(mContext);
        UserDetailsRequest request = new UserDetailsRequest();
        FEHttpClient.getInstance().post(request, new ResponseCallback<UserDetailsResponse>() {
            @Override
            public void onCompleted(UserDetailsResponse response) {
                if (response == null || !TextUtils.equals(response.getErrorCode(), "0")) {
                    FEToast.showMessage(CommonUtil.getString(R.string.contact_fetch_error));
                    return;
                }
                AddressBookVO result = response.getResult();
                ContactInfo contactInfo=new ContactInfo();

                contactInfo.userId = result.getId();
                contactInfo.name = result.getName();
                contactInfo.imageHref = result.getImageHref();
                contactInfo.position = result.getPosition();
                contactInfo.pinyin = result.getPinyin();
                contactInfo.deptName = result.getDepartmentName();
                contactInfo.imid = result.getImid();

                contactInfo.tel = result.getTel();
                contactInfo.address = result.getAddress();
                contactInfo.email = result.getEmail();
                contactInfo.phone = result.getPhone();
                contactInfo.phone1 = result.getPhone1();
                contactInfo.phone2 = result.getPhone2();
                contactInfo.brithday = result.getBrithday();

                userInfoData(contactInfo);
                initModifyBean(contactInfo);
                if (LoadingHint.isLoading()) {
                    LoadingHint.hide();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                FEToast.showMessage(CommonUtil.getString(R.string.contact_fetch_error));
            }
        });
    }

    private void userInfoData(ContactInfo addressBook) {
        String[] titles = mContext.getResources().getStringArray(R.array.userinfo_detail_list);
        List<String> contents = getContents(addressBook);
        if (contents == null || contents.size() != titles.length) {
            return;
        }
        listBeans = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            listBeans.add(getBean(types[i], titles[i], contents.get(i)));
        }
        mView.setAdapter(listBeans);
    }

    private List<String> getContents(ContactInfo addressBook) {
        String imageUrl = CoreZygote.getLoginUserServices().getUserImageHref();
        List<String> contents = new ArrayList<>();
        contents.add(imageUrl);
        contents.add(addressBook.deptName);
//        contents.add(addressBook.brithday);
        contents.add(addressBook.phone);
        contents.add(addressBook.tel);
        contents.add(addressBook.email);
        contents.add(addressBook.address);
        return contents;
    }

    private UserInfoDetailItem getBean(int type, String title, String content) {
        UserInfoDetailItem bean = new UserInfoDetailItem();
        bean.itemType = type;
        bean.title = title;
        bean.content = content;
        return bean;
    }

    public void completeAttachment(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        notifierAdapter(path);
    }

    private void notifierAdapter(String path) {
        if (listBeans == null || TextUtils.isEmpty(path)) {
            return;
        }
        List<UserInfoDetailItem> beans = new ArrayList<>();
        for (UserInfoDetailItem bean : listBeans) {
            if (bean.itemType == K.userInfo.DETAIL_ICON) {
                bean.content = path;
            }
            beans.add(bean);
        }
        mView.setAdapter(beans);
    }
}
