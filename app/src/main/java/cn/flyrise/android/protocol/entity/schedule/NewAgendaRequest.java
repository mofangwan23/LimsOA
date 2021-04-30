package cn.flyrise.android.protocol.entity.schedule;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by yj on 2016/7/19.
 */
public class NewAgendaRequest extends RequestContent {

    public static final String NAMESPACE = "AgendaRequest";

    @SerializedName("method") public String method;
    @SerializedName("UE01") public String title; // 标题
    @SerializedName("UE05") public String content; // 内容
    @SerializedName("UE15") public String startTime; // 开始时间
    @SerializedName("UE10") public String endTime; // 结束时间
    @SerializedName("UE08") public String promptTime; // 提醒时间
    @SerializedName("UE06") public String repeatTime; // 重复时间
    @SerializedName("UE20") public String sharePerson; // 分享他人
    @SerializedName("UE12") public String attachmentId; // 附件ID
    @SerializedName("master_key") public String master_key; // 取的是UE00的值

    @Override public String getNameSpace() {
        return NAMESPACE;
    }
}
