package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2017-02-19 20:46
 */
public class SalaryListResponse extends ResponseContent {

    @SerializedName("interval")
    public List<Map<String, String>> months;

}
