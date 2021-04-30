package cn.flyrise.android.protocol.entity.knowledge;


import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by k on 2016/9/7.
 */
public class LoadRootFolderListRequest extends KnowledgeBaseRequest {

    private static final String GETPERSON = "getPersonalFolderList";
    private static final String GETUNIT = "getUnitFolderList";
    private static final String GETGROUP = "getGroupFolderList";

    private String param1;
    private String param2;

    public LoadRootFolderListRequest(int folderType, int mCurrentPage, int pageSize) {
        if (folderType == KnowKeyValue.FOLDERTYPE_PERSON)
            this.method = GETPERSON;
        else if (folderType == KnowKeyValue.FOLDERTYPE_UNIT)
            this.method = GETUNIT;
        else
            this.method = GETGROUP;
        this.count = "2";
        this.obj = "knowledgeService";
        this.param1 = String.valueOf(mCurrentPage);
        this.param2 = String.valueOf(pageSize);
    }
}
