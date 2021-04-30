package cn.flyrise.android.protocol.model;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-18 下午5:14:17 <br/>
 *          类说明 :
 */
public class MeetingSendSubReply extends RequestContent {
    public static final String NAMESPACE = "MeetingRequest";
    private String id;
    private String replyID;
    private String isSendMsg;
    private String content;
    private String requestType = "11";             // 会议子回复请求类型为11

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getReplyID () {
        return replyID;
    }

    public void setReplyID (String replyID) {
        this.replyID = replyID;
    }

    public String getIsSendMsg () {
        return isSendMsg;
    }

    public void setIsSendMsg (String isSendMsg) {
        this.isSendMsg = isSendMsg;
    }

    public String getContent () {
        return content;
    }

    public void setContent (String content) {
        this.content = content;
    }

    public String getRequestType () {
        return requestType;
    }

    public void setRequestType (String requestType) {
        this.requestType = requestType;
    }

    public static String getNamespace () {
        return NAMESPACE;
    }
}
