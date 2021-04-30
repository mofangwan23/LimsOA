package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2017-05-17 17:27
 */
public class CrmListRequest extends RequestContent {

    @Override public String getNameSpace() {
        return "RemoteRequest";
    }

    public String obj = "crmService";
    public String method = "crmListForMobile";
    public String count = "4";

    @SerializedName("param1") public String keyword;        //搜索的关键字
    @SerializedName("param2") public String advanceKeyword; //高级搜索时用的条件,暂时留空
    @SerializedName("param3") public String pageSize;       //第几页
    @SerializedName("param4") public String pageCount;      //每页有多少条
}
