package cn.flyrise.android.protocol.entity.knowledge;


/**
 * Created by k on 2016/9/9.
 */
public class RenameUnitFolderRequest extends KnowledgeBaseRequest {

    /**
     *  "param1":"3526",//文件夹Id
     "param2":"图片",//修改后的文件夹名称
     "param3":"2",//文件夹的层次级别
     "param4":"1",//排序号
     "param5":"",//描述
     "param6":"",//关联部门
     "param7":"",//关联岗位
     "param8":"7574",//用户ID
     "param9":"001"//单位ID
     */

    private String param1;
    private String param2;
    private String param3;
    private String param4;
    private String param5;
    private String param6;
    private String param7;
    private String param8;

    public RenameUnitFolderRequest(String folderId, String newName, String folderLevel, String userID) {
        count = "8";
        obj = "docTreeBO";
        method = "updFolderAndRelation";
        param1 = folderId;
        param2 = newName;
        param3 = folderLevel;
        param4 = "1";
        param5 = "";
        param6 = "";
        param7 = "";
        param8 = userID;
    }
}
