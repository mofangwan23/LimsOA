package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2016/7/19 11:15
 */
public class CommonResponse extends ResponseContent {

    @SerializedName("FE_GUID") public String guid;
}
