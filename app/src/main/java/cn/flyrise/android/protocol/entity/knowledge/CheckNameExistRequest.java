package cn.flyrise.android.protocol.entity.knowledge;

import com.google.gson.annotations.SerializedName;

/**
 * Created by k on 2016/9/7.
 */
public class CheckNameExistRequest extends KnowledgeBaseRequest {

    @SerializedName("param1")
    private String parentFolderID;
    @SerializedName("param2")
    private String newName;
    @SerializedName("param3")
    private String folderType;

    public CheckNameExistRequest(String parentFolderID, String newName,int folderType) {
        this.count = "3";
        this.method = "isnamerepeat";
        this.obj = "docTreeBO";
        this.parentFolderID = parentFolderID;
        this.newName = newName;
        this.folderType = String.valueOf(folderType);
    }
}
