package cn.flyrise.android.protocol.entity.knowledge;

/**
 * Created by k on 2016/9/7.
 */
public class MoveFolderAndFileRequest extends KnowledgeBaseRequest {

    public String param1;
    public String param2;
    public String param3;

    public MoveFolderAndFileRequest(String folderId, String folderIDs, String fileIDs) {
        this.count = "3";
        this.method="moveFolderAndFiles";
        this.obj="docTreeBO";
        this.param1=folderId;
        this.param2=folderIDs;
        this.param3=fileIDs;
    }
}
