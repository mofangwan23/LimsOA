package cn.flyrise.android.protocol.entity.knowledge;

/**
 * Created by k on 2016/9/7.
 */
public class FilePermissionRequest extends KnowledgeBaseRequest {

    public String param1;

    public FilePermissionRequest(String fileId) {
        this.count = "1";
        this.method="hasPermission";
        this.obj="knowledgeService";
        this.param1=fileId;
    }
}
