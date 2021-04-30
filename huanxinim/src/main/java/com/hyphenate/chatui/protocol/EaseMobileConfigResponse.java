package com.hyphenate.chatui.protocol;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2017-04-25 11:10
 */
@Keep
public class EaseMobileConfigResponse extends ResponseContent {

    public Result result;

    public class Result {
        @SerializedName("COMPNAY_GUID") public String companyGUID;
        @SerializedName("APP_URL") public String appURL;
        @SerializedName("CLIENT_ID") public String clientId;
        @SerializedName("APP_KEY") public String appKey;
        @SerializedName("CLIENT_SECRET") public String clientSecret;

    }

}
