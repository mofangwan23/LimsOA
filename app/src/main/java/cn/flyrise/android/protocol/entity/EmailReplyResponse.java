package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.flyrise.android.protocol.model.Accessory;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2016/7/20 09:45
 */
public class EmailReplyResponse extends ResponseContent {

    @SerializedName("sa01") public String guid;
    @SerializedName("mailID") public String mailId;
    @SerializedName("Title") public String title;
    @SerializedName("context") public String content;

    public String boxname;
    public String afilename;
    public String typename;
    public String type;

    public String bTransmit;
    public String tfromname;

    /**
     * 收件人名字
     */
    public String fromname;

    /**
     * 收件人 Id
     */
    public String su00;

    /**
     * 抄送人名字
     */
    public String cc;

    /**
     * 抄送人 Id
     */
    public String su00cc;

    /**
     * 密送人名字
     */
    public String bcc;

    /**
     * 密送人 Id
     */
    public String su00bcc;

    public String mailname1;
    public String mailname;

    public List<Accessory> accessoryList;

}
