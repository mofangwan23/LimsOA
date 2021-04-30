package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2016/7/20 11:31
 */
public class AssociationKnowledgeListRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    private String obj;
    private String method;
    private String count;

    @SerializedName("param1")
    private String folderID;
    @SerializedName("param2")
    private String folderAttr;
    @SerializedName("param3")
    private String keyword;
    @SerializedName("param4")
    private String searchOption;
    @SerializedName("param5")
    private String currentPage;
    @SerializedName("param6")
    private String pageSize;


    public AssociationKnowledgeListRequest(String folderID, String folderAttr, String keyword, String currentPage, String pageSize) {
        this.folderID = folderID;
        this.folderAttr = folderAttr;
        this.keyword = keyword;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.obj = "relevanceService";
        this.method = "relFileShowForMobile";
        this.count = "6";
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }


}
