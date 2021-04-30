package com.hyphenate.chatui.protocol;

import android.support.annotation.Keep;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2017-04-25 11:08
 */
@Keep
public class EaseMobileConfigRequest extends RequestContent {
    @Override public String getNameSpace() {
        return "RemoteRequest";
    }

    public String obj;
    public String method;
    public String count;

    public EaseMobileConfigRequest() {
        obj = "easemobConfigImpl";
        method = "getEasemobParam";
        count = "0";
    }

}
