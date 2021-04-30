package cn.flyrise.android.protocol.entity.knowledge;

/**
 * Created by KLC on 2016/12/12.
 */

public class IsPicTypeRequest extends KnowledgeBaseRequest {

    private String param1;

    public IsPicTypeRequest(String folderIDs) {
        this.count = "1";
        this.method = "isPicType";
        this.obj = "docTreeBO";
        this.param1 = folderIDs;
    }
}
