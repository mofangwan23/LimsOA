package cn.flyrise.android.protocol.entity.knowledge;


/**
 * Created by k on 2016/9/9.
 */
public class RenamePersonFolderRequest extends KnowledgeBaseRequest {

    private String param1;
    private String param2;

    public RenamePersonFolderRequest(String folderId, String newName) {
        count = "2";
        obj = "docTreeBO";
        method = "UpdFolderForMobile";
        param1 = folderId;
        param2 = newName;
    }
}
