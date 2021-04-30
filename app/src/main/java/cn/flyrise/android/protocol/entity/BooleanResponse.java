package cn.flyrise.android.protocol.entity;


import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by k on 2016/9/7.
 */
public class BooleanResponse extends ResponseContent {
    @SerializedName("result")
    public boolean isSuccess;
}
