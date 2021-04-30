package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2016/7/20 11:31
 */
public class AssociationSendRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    private String obj;
    private String method;
    private String count;

    @SerializedName("param1")
    private String guid;
    @SerializedName("param2")
    private Relationflow flow;


    public AssociationSendRequest(String guid, Relationflow flow) {
        this.guid = guid;
        this.flow = flow;
        this.obj = "relevanceService";
        this.method = "updateResForMobile";
        this.count = "2";
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

    public static class Relationflow {

        private List<SendAssociation> relationflow;

        public Relationflow(List<SendAssociation> relationflow) {
            this.relationflow = relationflow;
        }
    }

    public static class SendAssociation {
        private String viewName;
        private String type;
        private String bizId;

        public SendAssociation(String viewName, String type, String bizId) {
            this.viewName = viewName;
            this.type = type;
            this.bizId = bizId;
        }
    }

}
