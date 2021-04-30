package cn.flyrise.android.protocol.entity.schedule;

import com.google.gson.annotations.SerializedName;

public class AgendaDetailData {

    @SerializedName("UE01") public String title;        // 标题
    @SerializedName("UE15") public String startTime;    // 开始时间
    @SerializedName("UE10") public String endTime;      // 结束时间
    @SerializedName("UE06") public String repeatTime;   // 重复时间
    @SerializedName("UE08") public String promptTime;   // 提醒时间

    @SerializedName("UE20") public String shareOther;  // 分享他人
    @SerializedName("UE05") public String content;     // 内容
    @SerializedName("UE11") public String sendUserId;    // 发送人 id
    @SerializedName("UE12") public String attachmentId;  // 附件ID
    @SerializedName("UE00") public String marsterKey;  // 用于 master_key


    @Override public String toString() {
        return "AgendaDetailData{" +
                "title='" + title + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", promptTime='" + promptTime + '\'' +
                ", repeatTime='" + repeatTime + '\'' +
                ", shareOther='" + shareOther + '\'' +
                ", content='" + content + '\'' +
                ", sendUserId='" + sendUserId + '\'' +
                ", marsterKey='" + marsterKey + '\'' +
		        ", attachmentId='" + attachmentId + '\'' +
                '}';
    }
}
