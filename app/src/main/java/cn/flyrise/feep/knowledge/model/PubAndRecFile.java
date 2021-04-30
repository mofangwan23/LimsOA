package cn.flyrise.feep.knowledge.model;

import android.support.annotation.Keep;

/**
 * Created by klc
 */
@Keep
public class PubAndRecFile extends ListBaseItem {

    /**
     * id : 8BD61FA6-E627-1179-1C77-4EC51137C1A1
     * filename : 201610260241163517.doc
     * title : 8o4vgj2kcmmk
     * edituser : 李金明
     * filetype : .doc
     * uploaduser : 7574
     * filepath : //FEdocument//2016//10//26
     * publishuser : 李金明
     * publishid : 1943
     * filesize : 2672
     * ENDTIME : 不限
     * enddate : 不限
     * startdate : 2016-10-26 14:42:00.0
     */

    public String id;
    public String filename;
    public String title;
    public String edituser;
    public String filetype;
    public String uploaduser;
    public String filepath;
    public String publishuser;
    public String publishid;
    public String filesize;
    public String ENDTIME;
    public String enddate;
    public String startdate;
    public String roleid;

    public String getRealFileName() {
        return title + filetype;
    }
}
