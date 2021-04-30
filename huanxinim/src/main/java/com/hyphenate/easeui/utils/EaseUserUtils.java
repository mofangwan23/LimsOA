package com.hyphenate.easeui.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EaseUserUtils {

    public static void setUserAvatar(Context context,String userId, ImageView imageView) {
        if (imageView != null) {
            CoreZygote.getAddressBookServices().queryUserDetail(userId)
                    .subscribe(userInfo -> {
                        if (userInfo == null) {
                            FEImageLoader.load(context, imageView, R.drawable.ease_default_avatar);
                        }
                        else {
                            String host = CoreZygote.getLoginUserServices().getServerAddress();
                            FEImageLoader.load(context, imageView, host + userInfo.imageHref, userId, userInfo.name);
                        }
                    }, error -> {
                        FEImageLoader.load(context, imageView, R.drawable.ease_default_avatar);
                    });
        }
    }
    /**
     * set user avatar
     *
     */
    public static void setUserAvatar(Context context, EMMessage message, ImageView imageView) {
        if (imageView == null) {
            return;
        }
        ILoginUserServices services = CoreZygote.getLoginUserServices();
        if (services == null) {
            return;
        }
        String host = services.getServerAddress();
        if (message.direct() == EMMessage.Direct.SEND) {
            FEImageLoader.load(context, imageView, host + services.getUserImageHref(), services.getUserId(), services.getUserName());
        } else {
            CoreZygote.getAddressBookServices().queryUserDetail(message.getFrom())
                    .subscribe(userInfo -> {
                        if (userInfo == null) {
                            FEImageLoader.load(context, imageView, R.drawable.ease_default_avatar);
                        }
                        else {
                            FEImageLoader.load(context, imageView, host + userInfo.imageHref, message.getFrom(), userInfo.name);
                        }
                    }, error -> {
                        FEImageLoader.load(context, imageView, R.drawable.ease_default_avatar);
                    });
        }
    }

    /**
     * set user's nickname
     */
    public static void setUserNick(String userId, TextView textView) {
        if (textView != null) {
            textView.setText(getUserNick(userId));
        }
    }

    public static String getUserNick(String userId) {
        if (CoreZygote.getAddressBookServices() == null) {
            return userId;
        }
        AddressBook userInfo = CoreZygote.getAddressBookServices().queryUserInfo(userId);
        if (userInfo == null) {
            return userId;
        } else {
            return userInfo.name;
        }
    }

}
