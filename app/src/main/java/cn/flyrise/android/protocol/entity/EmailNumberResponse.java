package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.model.EmailNumber;
import cn.flyrise.feep.core.network.request.ResponseContent;

public class EmailNumberResponse extends ResponseContent {

    public int isEmailSizes;
    public String userSize;
    public String precentUse;
    public String typevalue;

    /**
     * 邮件列表
     * 1. 第一个是内部邮箱
     * 2. 第二个是外部邮箱
     */
    public List<String> mailList;

    @SerializedName("InBox") public EmailNumber.InBox inBox;
    @SerializedName("Draft") public EmailNumber.Draft draft;
    @SerializedName("Sent") public EmailNumber.Sent sent;
    @SerializedName("Trash") public EmailNumber.Trash trash;

    public List<EmailNumber> getEmailNumbers() {
        List<EmailNumber> numbers = new ArrayList<>();
        if (inBox != null) {
            numbers.add(inBox);
        }

        if (draft != null) {
            numbers.add(draft);
        }

        if (sent != null) {
            numbers.add(sent);
        }

        if (trash != null) {
            numbers.add(trash);
        }

        return numbers;
    }
}
