package cn.flyrise.android.protocol.entity.knowledge;

/**
 * Created by k on 2016/9/7.
 */
public class DeleteFolderAndFileRequest extends KnowledgeBaseRequest {

    public String param1;
    public String param2;

    public DeleteFolderAndFileRequest(String folders, String fileIds) {
        this.count = "2";
        this.method="delFolderAndfile";
        this.obj="docTreeBO";
        param1=folders;
        param2=fileIds;
    }
}
