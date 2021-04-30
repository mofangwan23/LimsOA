package cn.flyrise.android.protocol.entity.knowledge;

/**
 * Created by KLC on 2016/12/12.
 */

public class IsDocTypeRequest extends KnowledgeBaseRequest {
    private String param1;

    public IsDocTypeRequest(String folderiDs) {
        this.count = "1";
        this.method = "isDocType";
        this.obj = "docTreeBO";
        this.param1 = folderiDs;
    }
}
