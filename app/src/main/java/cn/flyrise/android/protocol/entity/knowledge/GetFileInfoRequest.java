package cn.flyrise.android.protocol.entity.knowledge;


/**
 * Created by k on 2016/9/9.
 */
public class GetFileInfoRequest extends KnowledgeBaseRequest {

    private String param1;

    public GetFileInfoRequest(String fileID) {
        count = "1";
        obj = "knowledgeService";
        method = "getFileViewById";
        param1 = fileID;
    }
}
