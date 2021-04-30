package cn.flyrise.android.protocol.entity.knowledge;

import cn.flyrise.feep.core.network.request.RequestContent;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by k on 2016/9/7.
 */
public class FolderAndFileListRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";
    private static final String GETPERSON = "getFolderAndFileByFolderId";
    private static final String GETUNIT = "getUnitFolderAndFileByFolderId";
    private static final String GETGROUP = "getGroupFolderAndFileByFolderId";
    private static final String OBJ = "knowledgeService";

    private int count;
    private String method;
    private String obj;
    private String param1;
    private String param2;
    private String param3;

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

    public FolderAndFileListRequest(int folderType, String folderID, int currentPage, int pageSize) {
        if (folderType == KnowKeyValue.FOLDERTYPE_PERSON)
            this.method = GETPERSON;
        else if(folderType == KnowKeyValue.FOLDERTYPE_UNIT)
            this.method = GETUNIT;
        else
            this.method = GETGROUP;
        this.count = 3;
        this.obj = OBJ;
        this.param1 = folderID;
        this.param2 = String.valueOf(currentPage);
        this.param3 = String.valueOf(pageSize);
    }
}
