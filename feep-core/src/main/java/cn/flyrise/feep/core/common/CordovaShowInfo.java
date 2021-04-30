package cn.flyrise.feep.core.common;

/**
 * Created by Administrator on 2016-12-27.
 */
public class CordovaShowInfo {

    public String msgId;// 如果是某一个msgId
    public String id;// 对应业务的Id
    public String pageid;// 新建日程
    public String duduData;//嘟嘟
    public int type = -1;//返回类型

    public String url;//服务端传过来调往详情的地址

    public CordovaShowInfo(int type) {
        this.type = type;
    }

    public CordovaShowInfo() { }
}
