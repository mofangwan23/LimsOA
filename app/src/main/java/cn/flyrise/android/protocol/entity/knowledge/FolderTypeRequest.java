package cn.flyrise.android.protocol.entity.knowledge;

/**
 * Created by KLC on 2016/12/12.
 */

public class FolderTypeRequest extends KnowledgeBaseRequest{

    public String param1;

    public FolderTypeRequest(String folderID) {
        this.count = "1";
        this.method = "isMyPicOrMyDoc";
        this.obj = "docTreeBO";
        this.param1 = folderID;
    }

}
