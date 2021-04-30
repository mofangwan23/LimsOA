package cn.flyrise.feep.userinfo.modle;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by Administrator on 2017-5-8.
 */

public class UserModifyRequest extends RequestContent{
    @Override
    public String getNameSpace() {
        return "RemoteRequest";
    }

    private String obj;
    private String method;
    private String count;

    public String getObj() {
        return obj;
    }

    public void setObj(String obj) {
        this.obj = obj;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
