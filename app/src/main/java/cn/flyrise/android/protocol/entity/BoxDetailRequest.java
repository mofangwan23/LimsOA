package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2016/7/12 18:02
 */
public class BoxDetailRequest extends RequestContent {

    /**
     * 删除邮件
     */
    public static final String OPERATOR_REMOVE = "remove";

    /**
     * 永久删除
     */
    public static final String OPERATOR_DELETE = "delete";

    /**
     * 恢复邮件
     */
    public static final String OPERATOR_RESTORE = "restore";

    public static final String TYPE_TRASH = "3";

    public static final String NAMESPACE = "BoxdetailRequest";

    @Override public String getNameSpace() {
        return NAMESPACE;
    }

    /**
     * InBox/Inner:收件箱
     * Sent:已发送
     * Draft:草稿箱
     * Trash:垃圾箱
     * InBox:外部收件箱
     */
    @SerializedName("boxname") public String boxName;

    @SerializedName("typevalue") public String typeValue;

    /**
     * 请求第几页
     */
    @SerializedName("PageNumber") public int pageNumber;

    @SerializedName("optmaillst") public String optMailList;

    /**
     * 目测仅用于外部邮箱
     */
    public String typeTrash;

    /**
     * 外部邮箱
     */
    public String mailname;

    /**
     * 请求的操作：恢复邮件/永久删除/删除邮件
     */
    public String operator;

    /**
     * 搜索的关键字
     */
    public String tit;


    public BoxDetailRequest() { }

    public BoxDetailRequest(String boxName, String typeValue) {
        this.boxName = boxName;
        this.typeValue = typeValue;
    }



}
