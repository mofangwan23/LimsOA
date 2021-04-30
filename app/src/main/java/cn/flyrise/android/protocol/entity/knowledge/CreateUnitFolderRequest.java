package cn.flyrise.android.protocol.entity.knowledge;


/**
 * Created by k on 2016/9/9.
 */
public class CreateUnitFolderRequest extends KnowledgeBaseRequest {

    private String param1;
    private String param2;
    private String param3;
    private String param4;
    private String param5;
    private String param6;
    private String param7;
    private String param8;

    public CreateUnitFolderRequest(String parentFolderID, String newName, String folderLevel, String userID, boolean isInherit) {
        count = "8";
        obj = "docTreeBO";
        method = "addFolderAndRelation";
        param1 = parentFolderID;
        param2 = newName;
        param3 = folderLevel;
        param4 = "";
        param5 = "";
        param6 = "";
        param7 = userID;
        param8 = String.valueOf(isInherit);
    }
}
