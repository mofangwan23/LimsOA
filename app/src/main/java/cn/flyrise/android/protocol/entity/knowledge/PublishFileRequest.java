package cn.flyrise.android.protocol.entity.knowledge;


/**
 * Created by k on 2016/9/9.
 */
public class PublishFileRequest extends KnowledgeBaseRequest {

    private String param1;
    private String param2;
    private String param3;
    private String param4;
    private String param5;
    private String param6;

    public PublishFileRequest(String fileIds, String folderId, String receiverUserIDs, String userId, String startTime, String endTime) {
        count = "6";
        obj = "fileOperator";
        method = "publish";
        param1 = fileIds;
        param2 = folderId;
        param3 = receiverUserIDs;
        param4 = userId;
        param5 = startTime;
        param6 = endTime;
    }
}
