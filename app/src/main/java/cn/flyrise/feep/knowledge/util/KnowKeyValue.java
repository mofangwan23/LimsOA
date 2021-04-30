package cn.flyrise.feep.knowledge.util;

/**
 * Created by klc on 2016/11/28.
 */

public interface KnowKeyValue {

    int FOLDERTYPE_PERSON = 2;  // 个人文件夹
    int FOLDERTYPE_UNIT = 3;    // 单位文件夹
    int FOLDERTYPE_GROUP = 1;   // 集团文件夹
    int FOLDERTYPE_ALL = 588;   // 登录用户所能看到的全部

    String PERSONROOTFOLDERID = "4";
    String UNITROOTFOLDERID = "3";
    String GROUPROOTFOLDERID = "2";
    int LOADPAGESIZE = 20;

    //extra
    String EXTRA_FOLDERMANAGER = "EXTRA_FOLDERMANAGER";

    //extra
    String EXTRA_MOVEFILEID = "EXTRA_MOVEFILEID";
    String EXTRA_MOVEFOLDERID = "EXTRA_MOVEFOLDERID";
    String EXTRA_MOVEPARENTID = "EXTRA_MOVEPARENTID";
    String EXTRA_FOLDERTYPES = "EXTRA_FOLDERTYPES";
    String EXTRA_FOLDERID = "EXTRA_FOLDERID";
    String EXTRA_PUBLISHFILEID = "EXTRA_PUBLISHFILEID";
    String EXTRA_PUBLISHFILEPARENTID = "EXTRA_PUBLISHFILEPARENTID";
    String EXTRA_RECEIVERMSAID = "EXTRA_RECEIVERMSAID";
    String EXTRA_ISPICFOLDER = "EXTRA_ISPICFOLDER";
    String EXTRA_ISDOCFOLDER = "EXTRA_ISDOCFOLDER";
    String EXTRA_FILETYPE = "EXTRA_FILETYPE";
    String EXTRA_PHOTOPATH = "EXTRA_PHOTOPATH";


    int STARTMOVECODE = 0X1;
    int STARTPUBLISHCODE = 0X2;
    int STARTUPLOADCODE = 0X3;
    int START_SELECT_FILE_CODE = 0X4;
    int START_SELECT_IMAGE_CODE = 0X5;
    int STARTCAMERACODE = 0X6;


    int PUBLISTTYPE = 0X30;
    int RECLISTTYPE = 0x31;

}
