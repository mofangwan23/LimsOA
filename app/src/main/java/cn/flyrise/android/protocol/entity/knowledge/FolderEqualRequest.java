package cn.flyrise.android.protocol.entity.knowledge;

/**
 * Created by klc
 */
public class FolderEqualRequest extends KnowledgeBaseRequest {

    public String param1;
    public String param2;
    public String param3;

    public FolderEqualRequest(String parentId, String folderIds, String fileType) {
        this.count = "3";
        this.method="checkCutFolders";
        this.obj="docTreeBO";
        this.param1=parentId;
        this.param2=folderIds;
        this.param3=fileType;
    }
}
