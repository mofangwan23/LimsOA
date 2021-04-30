package cn.flyrise.feep.main.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2017-05-17 17:35
 */
@Keep
public class ExternalContact {

    @SerializedName("CP00") public String contactId;            // Id
    @SerializedName("CP01") public String name;                 // 姓名
    @SerializedName("PY") public String pinyin;                 // 拼音
    @SerializedName("CP02") public String position;             // 职务
    @SerializedName("CP03") public String department;           // 部门
    @SerializedName("CP04") public String company;              // 公司
    @SerializedName("CP09") public String phone;                // 手机
    @SerializedName("CP22") public String connectContact;       // 关联客户

}
