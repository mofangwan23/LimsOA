package cn.flyrise.android.protocol.entity.knowledge;


/**
 * Created by k on 2016/9/9.
 */
public class GetReceiveListForMsgRequest extends KnowledgeBaseRequest {

    private String param1;
    public GetReceiveListForMsgRequest(String msgID) {
        count = "1";
        obj = "knowledgeService";
        method = "getPublishListForMsg";
        param1 = msgID;
    }
}
