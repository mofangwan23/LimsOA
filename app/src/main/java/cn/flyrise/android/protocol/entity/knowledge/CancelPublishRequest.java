package cn.flyrise.android.protocol.entity.knowledge;

/**
 * Created by klc on 2016/10/28.
 */

public class CancelPublishRequest extends KnowledgeBaseRequest {

    public String param1;

    public CancelPublishRequest(String fileIDs) {
        this.count = "1";
        this.method="cancelfubu";
        this.obj="fileAutoPOF";
        this.param1=fileIDs;
    }

}
