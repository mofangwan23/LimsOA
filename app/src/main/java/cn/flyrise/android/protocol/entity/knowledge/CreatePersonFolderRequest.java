package cn.flyrise.android.protocol.entity.knowledge;



/**
 * Created by k on 2016/9/9.
 */
public class CreatePersonFolderRequest extends KnowledgeBaseRequest {

    private String param1;
    private String param2;
    private String param3;
    private String param4;
    private String param5;

    public CreatePersonFolderRequest(String folderId, String newName, String folderLevel, String userID) {
        count = "5";
        obj = "docTreeBO";
        method = "addFolder";
        param1 = folderId;
        param2 = newName;
        param3 = folderLevel;
        param4 ="";
        param5 = userID;
    }
}
