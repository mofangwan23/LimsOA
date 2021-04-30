package cn.flyrise.android.protocol.entity.knowledge;


/**
 * Created by k on 2016/9/7.
 * 单位、集团文档重命名前判断相同名字。
 */

public class CheckNameExistForRenameUnitRequest extends KnowledgeBaseRequest {
    public String param1;
    public String param2;
    public String param3;

    public CheckNameExistForRenameUnitRequest(String folderId, String newName,int folderType) {
        this.count = "3";
        this.method = "checkFolderName";
        this.obj = "docTreeBO";
        this.param1 = folderId;
        this.param2 = newName;
        this.param3 = String.valueOf(folderType);
    }
}
