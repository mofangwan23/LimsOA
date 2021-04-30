package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.collaboration.matter.MatterListActivity;
import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2016/7/20 11:31
 */
public class MatterListRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    private String obj;
    private String method;
    private String count;

    @SerializedName("param1")
    private String keyword;
    @SerializedName("param2")
    private String searchOption;
    @SerializedName("param3")
    private String currentPage;
    @SerializedName("param4")
    private String pageSize;

    public MatterListRequest(String keyword, int currentPage, int pageSize, int matterType) {
        this.obj = "relevanceService";
        if (matterType == MatterListActivity.MATTER_FLOW) {
            this.method = "inforLinkForMobile";
        }
        else if (matterType == MatterListActivity.MATTER_MEETING) {
            this.method = "meetingPortalForMobile";
        }
        else if (matterType == MatterListActivity.MATTER_SCHEDULE) {
            this.method = "schedulePortalForMobile";
        }
        this.count = "4";
        this.keyword = keyword;
        this.searchOption = "";
        this.currentPage = String.valueOf(currentPage);
        this.pageSize = String.valueOf(pageSize);
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }


}
