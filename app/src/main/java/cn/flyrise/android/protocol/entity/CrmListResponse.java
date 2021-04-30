package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.main.model.ExternalContact;

/**
 * @author ZYP
 * @since 2017-05-17 17:31
 * 这种垃圾 API 看着真她妈辣眼睛
 */
public class CrmListResponse extends ResponseContent {

    public Result result;

    public class Result {
        @SerializedName("CurrentPage") public int currentPage;
        @SerializedName("PageSize") public int pageSize;
        @SerializedName("TotalRow") public int totalRow;
        @SerializedName("TotalPage") public int totalPage;
        @SerializedName("FieldSet") public List<ExternalContact> externalContactList;
    }

}
