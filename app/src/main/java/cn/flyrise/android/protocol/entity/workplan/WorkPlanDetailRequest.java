package cn.flyrise.android.protocol.entity.workplan;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-26 上午10:28:56 <br/>
 *          类说明 :
 */
public class WorkPlanDetailRequest extends RequestContent {
    public static final String NAMESPACE = "WorkPlanRequest";
    private String msgId = "";
    private String id;
    private String relatedUserID;

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getMsgId () {
        return msgId;
    }

    public void setMsgId (String msgId) {
        if (msgId != null) {
            this.msgId = msgId;
        }
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getRelatedUserID () {
        return relatedUserID;
    }

    public void setRelatedUserID (String relatedUserID) {
        this.relatedUserID = relatedUserID;
    }

    public static String getNamespace () {
        return NAMESPACE;
    }
}
