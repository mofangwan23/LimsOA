package cn.flyrise.android.protocol.entity.knowledge;

/**
 * Created by k on 2016/9/9.
 */
public class RenameFileRequest extends KnowledgeBaseRequest {

    public String param1;
    public String param2;

    public RenameFileRequest(String fileId, String newName){
        count="2";
        obj="fileOperator";
        method="renameFile";
        param1=fileId;
        param2=newName;
    }
}
