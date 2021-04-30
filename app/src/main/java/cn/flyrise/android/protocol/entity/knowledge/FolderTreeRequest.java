package cn.flyrise.android.protocol.entity.knowledge;


import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by k on 2016/9/7.
 */
public class FolderTreeRequest extends KnowledgeBaseRequest {

    public FolderTreeRequest(int folderTypes) {
        this.count = "0";
        if (folderTypes == KnowKeyValue.FOLDERTYPE_PERSON)
            this.method = "getPersonalFolderTree";
        else if (folderTypes == KnowKeyValue.FOLDERTYPE_UNIT)
            this.method = "getUnitFolderTree";
        else
            this.method = "getGroupFolderTree";
        this.obj = "docTreeBO";
    }
}
